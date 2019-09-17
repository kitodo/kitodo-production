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

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.ParameterNotFoundException;

public class OPACConfig {
    private static final Logger logger = LogManager.getLogger(OPACConfig.class);
    private static XMLConfiguration config;

    /**
     * Private constructor.
     */
    private OPACConfig() {
    }

    /**
     * Retrieve and return value of configuration parameter with name 'parameter'
     * from configuration of OPAC with name 'catalogName'.
     *
     * @param catalogName String identifying the catalog by title
     * @param parameter String identifying the parameter by name
     * @return value of parameter
     */
    public static String getConfigValue(String catalogName, String parameter) throws ParameterNotFoundException {
        HierarchicalConfiguration opacConfiguration = getCatalog(catalogName);
        if (Objects.isNull(opacConfiguration)) {
            throw new ParameterNotFoundException("No configuration foudn for catalog '" + catalogName + "'!");
        } else {
            String parameterValue = opacConfiguration.getString(parameter);
            if (parameterValue.isEmpty()) {
                throw new ParameterNotFoundException("Parameter '" + parameter
                        + "' not found in OPAC configuration for catalog '" + catalogName + "'!");
            }
            return parameterValue;
        }
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
     * Retrieve the "importDepth" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by its title
     * @return int value of importDepth
     */
    public static int getImportDepth(String catalogName) {
        return getCatalog(catalogName).getInt("importDepth");
    }

    /**
     * Retrieve 'trimMode' attribute value of the "parentElement" node for
     * the OPAC with the given name 'catalogName' from the OPAC configuration file.
     * @param catalogName String identifying the catalog by its title
     * @return trim mode for the parent
     */
    public static String getParentIDTrimMode(String catalogName) {
        return getCatalog(catalogName).getString("parentElement[@trimMode]");
    }

    /**
     * Load the "identifierParameter" of the catalog used to retrieve specific
     * individual records from that catalog.
     * @param catalogName String identifying the catalog by its title
     * @return HierarchicalConfiguration for catalog's "identifierParameter"
     */
    public static String getIdentifierParameter(String catalogName) {
        return getCatalog(catalogName).getString("identifierParameter[@value]");
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
    private static HierarchicalConfiguration getCatalog(String catalogName) {
        XMLConfiguration conf = getConfig();
        int countCatalogues = conf.getMaxIndex("catalogue");
        HierarchicalConfiguration catalog = null;
        for (int i = 0; i <= countCatalogues; i++) {
            String title = conf.getString("catalogue(" + i + ")[@title]");
            if (title.equals(catalogName)) {
                catalog = conf.configurationAt("catalogue(" + i + ")");
            }
        }
        if (Objects.nonNull(catalog)) {
            return catalog;
        } else {
            throw new ConfigException(catalogName);
        }
    }

    private static XMLConfiguration getConfig() {
        if (config != null) {
            return config;
        }
        KitodoConfigFile kitodoConfigOpacFile = KitodoConfigFile.OPAC_CONFIGURATION;
        if (!kitodoConfigOpacFile.exists()) {
            String message = "File not found: " + kitodoConfigOpacFile.getAbsolutePath();
            throw new ConfigException(message, new FileNotFoundException(message));
        }
        try {
            config = new XMLConfiguration(kitodoConfigOpacFile.getFile());
        } catch (ConfigurationException e) {
            logger.error(e);
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }
}
