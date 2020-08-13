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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.production.helper.Helper;

public class ConfigOpac {
    private static final Logger logger = LogManager.getLogger(ConfigOpac.class);
    private static final String ERROR_READ = "errorReading";
    private static final String CATALOGUE = "catalogue";
    private static final String DOCTYPES_TYPE = "doctypes.type";
    private static final String OPAC_CONFIG = "configurationOPAC";
    private static XMLConfiguration config;

    private static XMLConfiguration getConfig() throws FileNotFoundException {
        if (Objects.nonNull(config)) {
            return config;
        }

        KitodoConfigFile opacConfiguration = KitodoConfigFile.OPAC_CONFIGURATION;

        if (!opacConfiguration.exists()) {
            throw new FileNotFoundException("File not found: " + opacConfiguration.getAbsolutePath());
        }
        try {
            config = new XMLConfiguration(opacConfiguration.getFile());
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }

    /**
     * Returns all configured catalog titles from the config file.
     *
     * @return all catalog titles
     */
    public static List<String> getAllCatalogueTitles() {
        return getTitles(CATALOGUE);
    }

    /**
     * Return all configured Doctype-Titles from Config file.
     */
    private static List<String> getAllDoctypeTitles() {
        return getTitles(DOCTYPES_TYPE);
    }

    private static List<String> getTitles(String label) {
        List<String> titles = new ArrayList<>();
        try {
            int countTypes = getConfig().getMaxIndex(label);
            for (int i = 0; i <= countTypes; i++) {
                String title = getConfig().getString(label + "(" + i + ")[@title]");
                titles.add(title);
            }
        } catch (FileNotFoundException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[]{Helper.getTranslation(OPAC_CONFIG)}, logger, e);
        }
        return titles;
    }

    /**
     * Returns all configured media types from the config file.
     *
     * @return all media types
     */
    public static List<ConfigOpacDoctype> getAllDoctypes() {
        List<ConfigOpacDoctype> myList = new ArrayList<>();
        try {
            for (String title : getAllDoctypeTitles()) {
                myList.add(getDoctypeByName(title));
            }
        } catch (FileNotFoundException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_READ, new Object[]{Helper.getTranslation(OPAC_CONFIG)}, logger, e);
        }
        return myList;
    }

    /**
     * get doctype from title.
     */
    public static ConfigOpacDoctype getDoctypeByName(String inTitle) throws FileNotFoundException {
        int countCatalogues = getConfig().getMaxIndex(DOCTYPES_TYPE);
        for (int i = 0; i <= countCatalogues; i++) {
            String title = getConfig().getString(DOCTYPES_TYPE + "(" + i + ")[@title]");
            if (title.equals(inTitle)) {
                String inRulesetType = getConfig().getString(DOCTYPES_TYPE + "(" + i + ")[@rulesetType]");
                String inTifHeaderType = getConfig().getString(DOCTYPES_TYPE + "(" + i + ")[@tifHeaderType]");
                return new ConfigOpacDoctype(inTitle, inRulesetType, inTifHeaderType);
            }
        }
        return null;
    }
}
