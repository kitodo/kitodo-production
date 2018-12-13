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

package org.kitodo.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.config.enums.ParameterAPI;

public class OPACConfig {
    private static final Logger logger = LogManager.getLogger(OPACConfig.class);
    private static XMLConfiguration config;

    /**
     * Private constructor.
     */
    private OPACConfig() {
    }

    /**
     * Retrieve the "config" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by title
     * @return HierarchicalConfiguration for catalog's "config"
     */
    public static HierarchicalConfiguration getOPACConfiguration(String catalogName) {
        return getCatalog(catalogName).configurationAt("config");
    }

    /**
     * Retrieve the "searchFields" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by title
     * @return HierarchicalConfiguration for catalog's "searchFields"
     */
    public static HierarchicalConfiguration getSearchFields(String catalogName) {
        return getCatalog(catalogName).configurationAt("searchFields");
    }

    /**
     * Retrieve the "urlParameters" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by its title
     * @return HierarchicalConfiguration for catalog's "urlParameters"
     */
    public static HierarchicalConfiguration getUrlParameters(String catalogName) {
        return getCatalog(catalogName).configurationAt("urlParameters");
    }

    /**
     * Retrieve the "mappingFile" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by its title
     * @return HierarchicalConfiguration for catalog's "mappingFile"
     */
    public static String getXsltMappingFile(String catalogName) {
        return getCatalog(catalogName).getString("mappingFile");
    }

    /**
     * Retrieve the list of catalogs' titles from config file.
     * @return List of Strings containing all catalog titles.
     */
    public static List<String> getCatalogs() {
        List<String> catalogueTitles = new ArrayList<>();
        XMLConfiguration conf = getConfig();
        for (int i = 0; i <= conf.getMaxIndex("catalogue"); i++) {
            catalogueTitles.add(conf.getString("catalogue(" + i + ")[@title]"));
        }
        return catalogueTitles;
    }

    /**
     * Retrieve the configuration for the passed catalog name from config file.
     * @param catalogName String identifying the catalog by attribute "title"
     * @return HierarchicalConfiguration for single catalog
     */
    public static HierarchicalConfiguration getCatalog(String catalogName) {
        XMLConfiguration conf = getConfig();
        int countCatalogues = conf.getMaxIndex("catalogue");
        for (int i = 0; i <= countCatalogues; i++) {
            String title = conf.getString("catalogue(" + i + ")[@title]");
            if (title.equals(catalogName)) {
                return conf.configurationAt("catalogue(" + i + ")");
            }
        }

        return null;
    }

    private static XMLConfiguration getConfig() {
        if (config != null) {
            return config;
        }
        String configPfad = FilenameUtils.concat(KitodoConfig.getParameter(ParameterAPI.DIR_XML_CONFIG),
                KitodoConfigFile.OPAC_CONFIGURATION.getName());
        if (!new File(configPfad).exists()) {
            String message = "File not found: ".concat(configPfad);
            throw new RuntimeException(message, new FileNotFoundException(message));
        }
        try {
            config = new XMLConfiguration(configPfad);
        } catch (ConfigurationException e) {
            logger.error(e);
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }
}
