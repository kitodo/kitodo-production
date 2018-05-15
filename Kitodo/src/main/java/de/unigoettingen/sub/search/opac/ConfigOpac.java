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

package de.unigoettingen.sub.search.opac;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.Helper;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.constants.FileNames;

@XmlRootElement(name = "catalogueConfiguration")
public class ConfigOpac {
    private static final Logger logger = LogManager.getLogger(ConfigOpac.class);
    private static final String ERROR_READ = "errorReading";
    private static XMLConfiguration config;

    private static XMLConfiguration getConfig() throws FileNotFoundException {
        if (config != null) {
            return config;
        }
        String configPfad = FilenameUtils.concat(ConfigCore.getKitodoConfigDirectory(),
            FileNames.OPAC_CONFIGURATION_FILE);

        if (!new File(configPfad).exists()) {
            throw new FileNotFoundException("File not found: " + configPfad);
        }
        try {
            config = new XMLConfiguration(configPfad);
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }

    /**
     * Returns all configured catalogue titles from the config file.
     *
     * @return all catalogue titles
     */
    public static List<String> getAllCatalogueTitles() {
        List<String> myList = new ArrayList<>();
        try {
            int countCatalogues = getConfig().getMaxIndex("catalogue");
            for (int i = 0; i <= countCatalogues; i++) {
                String title = getConfig().getString("catalogue(" + i + ")[@title]");
                myList.add(title);
            }
        } catch (FileNotFoundException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[]{"opac-config"}, logger, e);
        }
        return myList;
    }

    /**
     * return all configured Doctype-Titles from Configfile.
     */
    private static ArrayList<String> getAllDoctypeTitles() {
        ArrayList<String> myList = new ArrayList<>();
        try {
            int countTypes = getConfig().getMaxIndex("doctypes.type");
            for (int i = 0; i <= countTypes; i++) {
                String title = getConfig().getString("doctypes.type(" + i + ")[@title]");
                myList.add(title);
            }
        } catch (FileNotFoundException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[]{"opac-config"}, logger, e);
        }
        return myList;
    }

    /**
     * Returns all configured media types from the config file.
     *
     * @return all media types
     */
    public static ArrayList<ConfigOpacDoctype> getAllDoctypes() {
        ArrayList<ConfigOpacDoctype> myList = new ArrayList<>();
        try {
            for (String title : getAllDoctypeTitles()) {
                myList.add(getDoctypeByName(title));
            }
        } catch (FileNotFoundException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[]{"opac-config"}, logger, e);
        }
        return myList;
    }

    /**
     * get doctype from title.
     */
    @SuppressWarnings("unchecked")
    public static ConfigOpacDoctype getDoctypeByName(String inTitle) throws FileNotFoundException {
        int countCatalogues = getConfig().getMaxIndex("doctypes.type");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = getConfig().getString("doctypes.type(" + i + ")[@title]");
            if (title.equals(inTitle)) {
                /* Sprachen erfassen */
                HashMap<String, String> labels = new HashMap<>();
                int countLabels = getConfig().getMaxIndex("doctypes.type(" + i + ").label");
                for (int j = 0; j <= countLabels; j++) {
                    String language = getConfig().getString("doctypes.type(" + i + ").label(" + j + ")[@language]");
                    String value = getConfig().getString("doctypes.type(" + i + ").label(" + j + ")");
                    labels.put(language, value);
                }
                String inRulesetType = getConfig().getString("doctypes.type(" + i + ")[@rulesetType]");
                String inTifHeaderType = getConfig().getString("doctypes.type(" + i + ")[@tifHeaderType]");
                boolean periodical = getConfig().getBoolean("doctypes.type(" + i + ")[@isPeriodical]");
                boolean multiVolume = getConfig().getBoolean("doctypes.type(" + i + ")[@isMultiVolume]");
                boolean containedWork = getConfig().getBoolean("doctypes.type(" + i + ")[@isContainedWork]");
                boolean newspaper;
                try {
                    newspaper = getConfig().getBoolean("doctypes.type(" + i + ")[@isNewspaper]");
                } catch (NoSuchElementException noParameterIsNewspaper) {
                    newspaper = false;
                }
                ArrayList<String> mappings = (ArrayList<String>) getConfig()
                        .getList("doctypes.type(" + i + ").mapping");

                return new ConfigOpacDoctype(inTitle, inRulesetType, inTifHeaderType, periodical, multiVolume,
                        containedWork, newspaper, labels, mappings);
            }
        }
        return null;
    }

    /**
     * The function getRestrictionsForCatalogue() returns the content of all
     * <kbd>&lt;restriction&gt;</kbd> elements from the
     * <kbd>&lt;catalogue&gt;</kbd> entry with the given <kbd>title</kbd> from
     * <kbd>kitodo_opac.xml</kbd>.
     *
     * <p>
     * The function will return an empty list if there are no such entries for
     * the given catalogue.
     * </p>
     *
     * @param title
     *            Title parameter of the <kbd>&lt;catalogue&gt;</kbd> entry to
     *            examine
     * @return List
     */
    public static List<String> getRestrictionsForCatalogue(String title) throws FileNotFoundException {
        List<String> result = new LinkedList<>();
        @SuppressWarnings("unchecked")
        List<HierarchicalConfiguration> catalogues = getConfig().configurationsAt("catalogue");
        for (HierarchicalConfiguration catalogue : catalogues) {
            if (title.equals(catalogue.getString("[@title]"))) {
                result.addAll(Arrays.asList(catalogue.getStringArray("restriction")));
                break;
            }
        }
        return result;
    }

    /**
     * Returns all configured catalogue titles from the config file. The Jersey
     * API cannot invoke static methods, so we need this wrapper method.
     *
     * @return all catalogue titles
     */
    @XmlElement(name = "interface")
    public List<String> getInterface() {
        return getAllCatalogueTitles();
    }

    /**
     * Returns all configured media types from the config file. The Jersey API
     * cannot invoke static methods, so we need this wrapper method.
     *
     * @return all media types
     */
    @XmlElement(name = "mediaType")
    public List<ConfigOpacDoctype> getMediaType() {
        return getAllDoctypes();
    }
}
