/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import org.apache.commons.configuration.*;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;

class OpacCatalogues {
    private static XMLConfiguration config;

    protected static XMLConfiguration getConfig() {
        if (config != null) {
            return config;
        }
        String configPfad = FilenameUtils.concat(PicaPlugin.getConfigDir(), PicaPlugin.OPAC_CONFIGURATION_FILE);
        if (!new File(configPfad).exists()) {
            String message = "File not found: ".concat(configPfad);
            throw new RuntimeException(message, new FileNotFoundException(message));
        }
        try {
            config = new XMLConfiguration(configPfad);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }

    static List<String> getAllCatalogues(){
        List<String> catalogueTitles = new ArrayList<>();
        XMLConfiguration conf = getConfig();
        for(int i = 0; i <= conf.getMaxIndex("catalogue"); i++){
            catalogueTitles.add(conf.getString("catalogue(" + i + ")[@title]"));
        }
        return catalogueTitles;
    }


    /**
     * find Catalogue in Opac-Configurationlist
     * ================================================================
     */
    @SuppressWarnings("unchecked")
    static Catalogue getCatalogueByName(String inTitle) {
        XMLConfiguration config = getConfig();
        int countCatalogues = config.getMaxIndex("catalogue");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = config.getString("catalogue(" + i + ")[@title]");
            if (title.equals(inTitle)) {
                String description = config.getString("catalogue(" + i + ").config[@description]");
                String scheme = "http";
                if (config.getString("catalogue(" + i + ").config[@scheme]") != null) {
                    scheme = config.getString("catalogue(" + i + ").config[@scheme]");
                }
                config.getString("catalogue(" + i + ").config[@scheme]");
                String address = config.getString("catalogue(" + i + ").config[@address]");
                String database = config.getString("catalogue(" + i + ").config[@database]");
                String ucnf = config.getString("catalogue(" + i + ").config[@ucnf]", "");
                if (!ucnf.equals("")) {
                    ucnf = "&" + ucnf;
                }
                int port = config.getInt("catalogue(" + i + ").config[@port]");
                String charset = "iso-8859-1";
                if (config.getString("catalogue(" + i + ").config[@charset]") != null) {
                    charset = config.getString("catalogue(" + i + ").config[@charset]");
                }

                // Opac-Beautifier einlesen und in Liste zu jedem Catalogue packen

                ArrayList<Setvalue> beautyList = new ArrayList<>();
                for (int j = 0; j <= config.getMaxIndex("catalogue(" + i + ").beautify.setvalue"); j++) {
                    /* Element, dessen Wert geändert werden soll */
                    String prefix = "catalogue(" + i + ").beautify.setvalue(" + j + ")";
                    String tag = config.getString(prefix + "[@tag]");
                    String subtag = config.getString(prefix + "[@subtag]");
                    String value = config.getString(prefix + "[@value]").replaceAll("\u2423", " ");
                    String mode = config.getString(prefix + "[@mode]", "replace");

                    // Elemente, die bestimmte Werte haben müssen, als Prüfung, ob das zu ändernde Element geändert werden soll

                    ArrayList<Condition> proofElements = new ArrayList<>();
                    for (int k = 0; k <= config.getMaxIndex(prefix + ".condition"); k++) {
                        String tempK = prefix + ".condition(" + k + ")";
                        Condition oteProof = new Condition(
                                config.getString(tempK + "[@tag]"), config.getString(tempK + "[@subtag]"),
                                config.getString(tempK + "[@value]").replaceAll("\u2423", " "), config
                                        .getString(tempK + "[@mode]", "matches"));
                        proofElements.add(oteProof);
                    }
                    beautyList.add(new Setvalue(tag, subtag, value, mode, proofElements));
                }

                // Read resolve rules

                Collection<ResolveRule> resolveRules = new LinkedList<>();
                for (HierarchicalConfiguration resolve : (List<HierarchicalConfiguration>) config
                        .configurationsAt("catalogue(" + i + ").resolve")) {
                    resolveRules.add(new ResolveRule(resolve));
                }

                return new Catalogue(title, description, scheme, address, database, port, charset, ucnf, beautyList,
                        resolveRules);
            }
        }
        return null;
    }

    /**
     * return all configured Doctype-Titles from Configfile
     * ================================================================
     */
    private static ArrayList<String> getAllDoctypeTitles() {
        ArrayList<String> myList = new ArrayList<>();
        int countTypes = getConfig().getMaxIndex("doctypes.type");
        for (int i = 0; i <= countTypes; i++) {
            String title = getConfig().getString("doctypes.type(" + i + ")[@title]");
            myList.add(title);
        }
        return myList;
    }

    /**
     * return all configured Doctype-Titles from Configfile
     * ================================================================
     */
    static ArrayList<Type> getDoctypes() {
        ArrayList<Type> myList = new ArrayList<>();
        for (String title : getAllDoctypeTitles()) {
            myList.add(getDoctypeByName(title));
        }
        return myList;
    }

    /**
     * get doctype from mapping of opac response first check if there is a
     * special mapping for this
     * ================================================================
     */
    static Type getDoctypeByMapping(String inMapping, String inCatalogue) {
        int countCatalogues = getConfig().getMaxIndex("catalogue");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = getConfig().getString("catalogue(" + i + ")[@title]");
            if (title.equals(inCatalogue)) {

                // alle speziell gemappten DocTypes eines Kataloges einlesen

                HashMap<String, String> labels = new HashMap<>();
                int countLabels = getConfig().getMaxIndex("catalogue(" + i + ").specialmapping");
                for (int j = 0; j <= countLabels; j++) {
                    String type = getConfig().getString("catalogue(" + i + ").specialmapping[@type]");
                    String value = getConfig().getString("catalogue(" + i + ").specialmapping");
                    labels.put(value, type);
                }
                if (labels.containsKey(inMapping)) {
                    return getDoctypeByName(labels.get(inMapping));
                }
            }
        }

        // falls der Katalog kein spezielles Mapping für den Doctype hat, jetzt in den Doctypes suchen

        for (String title : getAllDoctypeTitles()) {
            Type tempType = getDoctypeByName(title);
            if (tempType.getMappings().contains(inMapping)) {
                return tempType;
            }
        }
        return null;
    }

    /**
     * get doctype from title
     * ================================================================
     */
    @SuppressWarnings("unchecked")
    private static Type getDoctypeByName(String inTitle) {
        int countCatalogues = getConfig().getMaxIndex("doctypes.type");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = getConfig().getString("doctypes.type(" + i + ")[@title]");
            if (title.equals(inTitle)) {
                boolean periodical = getConfig().getBoolean("doctypes.type(" + i + ")[@isPeriodical]");
                boolean multiVolume = getConfig().getBoolean("doctypes.type(" + i + ")[@isMultiVolume]");
                boolean containedWork = getConfig().getBoolean("doctypes.type(" + i + ")[@isContainedWork]");
                ArrayList<String> mappings = (ArrayList<String>) getConfig()
                        .getList("doctypes.type(" + i + ").mapping");

                return new Type(title, periodical, multiVolume, containedWork, mappings);
            }
        }
        return null;
    }
}
