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
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.exceptions.MandatoryParameterMissingException;
import org.kitodo.exceptions.ParameterNotFoundException;

/**
 * The class OPACConfig is currently not used because OPAC configurations are now saved to
 * the database instead of an XML configuration file.
 *
 * <p>
 * NOTE: the class is not removed because it will be used to create an "OPAC configuration importer"
 *       that converts existing "kitodo_opac.xml" files to the new format saved to the database.
 */
public class OPACConfig {
    private static final Logger logger = LogManager.getLogger(OPACConfig.class);
    private static XMLConfiguration config;
    private static final String TRUE = "true";
    private static final String DEFAULT = "[@default]";
    private static final int DEFAULT_IMPORT_DEPTH = 2;
    private static final String DEFAULT_RETURN_FORMAT = "XML";
    private static final String DESCRIPTION = "description";
    private static final String NOT_AVAILABLE = "N/A";
    private static final String HOST = "host";
    private static final String SCHEME = "scheme";
    private static final String PATH = "path";
    private static final String PORT = "port";

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
            throw new ParameterNotFoundException("No configuration found for catalog '" + catalogName + "'!");
        } else {
            String parameterValue = opacConfiguration.getString(parameter);
            if (StringUtils.isBlank(parameterValue)) {
                throw new ParameterNotFoundException("Parameter '" + parameter
                        + "' not found in OPAC configuration for catalog '" + catalogName + "'!");
            }
            return parameterValue;
        }
    }

    /**
     * Retrieve and return the value of the "prestructured" flag of a specific catalog,
     * controlling whether the import of a process' structure is completely handled by the
     * import XSLT file or not.
     *
     * @param catalogName String identifying the catalog by its title
     * @return whether the "prestructured" value is set for the given catalog or not
     */
    public static boolean isPrestructuredImport(String catalogName) {
        return getCatalog(catalogName).containsKey("prestructured")
                && getCatalog(catalogName).getBoolean("prestructured");
    }

    /**
     * Retrieve the "interfaceType" of the catalog identified by it title.
     * @param catalogName String identifying the catalog by its title
     * @return String name of interfaceType
     */
    public static SearchInterfaceType getInterfaceType(String catalogName) {
        String interfaceType = getCatalog(catalogName).getString("interfaceType");
        for (SearchInterfaceType type : SearchInterfaceType.values()) {
            if (type.getTypeString().equals(interfaceType)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Retrieve the "description" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by title
     * @return String description for catalog's "config"
     */
    public static String getOPACDescription(String catalogName) {
        HierarchicalConfiguration catalogConfiguration = getCatalog(catalogName);
        List<ConfigurationNode> descriptionAttributes = catalogConfiguration.getRoot().getAttributes(DESCRIPTION);
        if (descriptionAttributes.isEmpty()) {
            return NOT_AVAILABLE;
        } else {
            return descriptionAttributes.stream().map(cn -> cn.getValue().toString())
                    .collect(Collectors.joining(System.lineSeparator()));
        }
    }

    /**
     * Retrieve the "config" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by title
     * @return HierarchicalConfiguration for catalog's "config"
     */
    private static HierarchicalConfiguration getOPACConfiguration(String catalogName) {
        return getCatalog(catalogName).configurationAt("config");
    }

    private static String getUrlConfigPart(String catalogName, String parameter)
            throws MandatoryParameterMissingException {
        HierarchicalConfiguration config = getOPACConfiguration(catalogName);
        if (Objects.nonNull(config)) {
            for (HierarchicalConfiguration param : config.configurationsAt("param")) {
                if (parameter.equals(param.getString("[@name]"))) {
                    return param.getString("[@value]");
                }
            }
        }
        throw new MandatoryParameterMissingException(parameter);
    }

    /**
     * Get host parameter of catalog configuration with name 'catalogName'.
     * @param catalogName name of catalog configuration
     * @return host value as String
     */
    public static String getHost(String catalogName) throws MandatoryParameterMissingException {
        return getUrlConfigPart(catalogName, HOST);
    }

    /**
     * Get scheme parameter of catalog configuration with name 'catalogName'.
     * @param catalogName name of catalog configuration
     * @return scheme value as String
     */
    public static String getScheme(String catalogName) throws MandatoryParameterMissingException {
        return getUrlConfigPart(catalogName, SCHEME);
    }

    /**
     * Get path parameter of catalog configuration with name 'catalogName'.
     * @param catalogName name of catalog configuration
     * @return path value as String
     */
    public static String getPath(String catalogName) throws MandatoryParameterMissingException {
        return getUrlConfigPart(catalogName, PATH);
    }

    /**
     * Get port parameter of catalog configuration with name 'catalogName'.
     * @param catalogName name of catalog configuration
     * @return port value as Integer
     */
    public static String getPort(String catalogName) throws MandatoryParameterMissingException {
        return getUrlConfigPart(catalogName, PORT);
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
     * Retrieve the default "searchField" of the catalog identified by its title 'catalogName'. If multiple fields are
     * configured as "default", the first is returned.
     * @param catalogName String identifying the catalog by title
     * @return String name of catalogs default "searchField" if it exists; empty String otherwise
     */
    public static String getDefaultSearchField(String catalogName) {
        for (HierarchicalConfiguration searchField : getSearchFields(catalogName).configurationsAt("searchField")) {
            if (TRUE.equals(searchField.getString(DEFAULT))) {
                String defaultSearchField = searchField.getString("[@label]");
                if (StringUtils.isNotBlank(defaultSearchField)) {
                    return defaultSearchField;
                }
            }
        }
        return "";
    }

    /**
     * Retrieve the name of the default "catalog" configured in "kitodo_opac.xml". If no catalog is configured as
     * default catalog, return an empty String.
     * @return String name of default catalog or empty String if no default catalog is configured.
     */
    public static String getDefaultCatalog() {
        for (String catalog : getCatalogs()) {
            if (TRUE.equals(getCatalog(catalog).getString(DEFAULT))) {
                return catalog;
            }
        }
        return "";
    }

    /**
     * Retrieve the "urlParameters" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by its title
     * @return HierarchicalConfiguration for catalog's "urlParameters"
     */
    public static HierarchicalConfiguration getUrlParameters(String catalogName) {
        return getCatalog(catalogName).configurationAt("urlParameters");
    }

    private static String getUrlParameter(String catalogName, String parameter)
            throws MandatoryParameterMissingException {
        HierarchicalConfiguration urlParameters = getCatalog(catalogName).configurationAt("urlParameters");
        if (Objects.nonNull(urlParameters)) {
            for (HierarchicalConfiguration param : urlParameters.configurationsAt("param")) {
                if (parameter.equals(param.getString("[@name]"))) {
                    return param.getString("[@value]");
                }
            }
        }
        throw new MandatoryParameterMissingException(parameter);
    }

    public static String getSruVersion(String catalogName) throws MandatoryParameterMissingException {
        return getUrlParameter(catalogName, "version");
    }

    public static String getSruRecordSchema(String catalogName) throws MandatoryParameterMissingException {
        return getUrlParameter(catalogName, "recordSchema");
    }

    public static String getOaiMetadataPrefix(String catalogName) throws MandatoryParameterMissingException {
        return getUrlParameter(catalogName, "metadataPrefix");
    }

    /**
     * Retrieve the "fileUpload" configuration of the catalog identified by its title.
     * @param catalogName String identifying the catalog by its title
     * @return boolean if catalog is configured to use FileUpload
     */
    public static boolean getFileUploadConfig(String catalogName) {
        return getCatalog(catalogName).containsKey("fileUpload")
                && getCatalog(catalogName).getBoolean("fileUpload");
    }

    /**
     * Retrieve list of "mappingFiles" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by its title
     * @return List of Strings containing mapping files names for catalog
     */
    public static List<String> getXsltMappingFiles(String catalogName) {
        return getCatalog(catalogName).configurationAt("mappingFiles").configurationsAt("file").stream()
                .map(c -> c.getRoot().getValue().toString()).collect(Collectors.toList());
    }

    /**
     * Retrieve the "parentMappingFile" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by its title
     * @return String containing given catalogs "parentMappingFile"
     */
    public static String getXsltMappingFileForParentInRecord(String catalogName) {
        return getCatalog(catalogName).getString("parentMappingFile");
    }

    /**
     * Retrieve the "queryDelimiter" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by its title
     * @return configuration for catalog's "queryDelimiter"
     */
    public static String getQueryDelimiter(String catalogName) {
        return getCatalog(catalogName).getString("queryDelimiter");
    }

    /**
     * Retrieve the "parentElement" of the catalog identified by its title.
     * @param catalogName String identifying the catalog by its title
     * @return HierarchicalConfiguration for catalog's "parentElement"
     */
    public static String getParentIDElement(String catalogName) {
        for (HierarchicalConfiguration field : getSearchFields(catalogName).configurationsAt("searchField")) {
            if (TRUE.equals(field.getString("[@parentElement]"))) {
                String parentIDElement = field.getString("[@label]");
                if (StringUtils.isNotBlank(parentIDElement)) {
                    return parentIDElement;
                }
            }
        }
        return null;
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
     * Load the identifier prefix that needs to be prepended to IDs in order retrieving
     * individual records by ID from the specified catalog.
     * @param catalogName String identifying the catalog by its title
     * @return String containing identifier prefix
     */
    public static String getIdentifierPrefix(String catalogName) {
        return getCatalog(catalogName).getString("identifierParameter[@prefix]");
    }

    /**
     * Load the name of the metadata type that is used to store the catalog ID
     * of a specific record in the internal metadata format.
     * @param catalogName String identifying the catalog by its title
     * @return HierarchicalConfiguration for catalog's "identifierMetadata"
     */
    public static String getIdentifierMetadata(String catalogName) {
        return getCatalog(catalogName).getString("identifierMetadata[@value]");
    }

    /**
     * Load the "exemplarField" of the catalog used to retrieve exemplar record information
     * from individual records of that catalog.
     * @param catalogName String identifying the catalog by its title
     * @return String XPath for exemplar record information
     */
    public static String getExemplarFieldXPath(String catalogName) {
        return getCatalog(catalogName).getString("exemplarField[@xpath]");
    }

    /**
     * Load the "ownerSubPath" of the catalog used to retrieve the owner of a exemplar record
     * from individual records of that catalog.
     * @param catalogName String identifying the catalog by its title
     * @return String XPath for owner information about an exemplar record
     */
    public static String getExemplarFieldOwnerXPath(String catalogName) {
        return getCatalog(catalogName).getString("exemplarField[@ownerSubPath]", null);
    }

    /**
     * Load the "signatureSubPath" of the catalog used to retrieve the signature of a exemplar record
     * from individual records of that catalog.
     * @param catalogName String identifying the catalog by its title
     * @return String XPath for signature information about an exemplar record
     */
    public static String getExemplarFieldSignatureXPath(String catalogName) {
        return getCatalog(catalogName).getString("exemplarField[@signatureSubPath]");
    }

    /**
     * Load the "ownerMetadata" of the catalog used to store the exemplar record owner
     * of individual records of that catalog.
     * @param catalogName String identifying the catalog by its title
     * @return String metadata name for exemplar record owner information
     */
    public static String getExemplarFieldOwnerMetadata(String catalogName) {
        return getCatalog(catalogName).getString("exemplarField[@ownerMetadata]");
    }

    /**
     * Load the "signatureMetadata" of the catalog used to store the exemplar record signature
     * of individual records of that catalog.
     * @param catalogName String identifying the catalog by its title
     * @return String metadata name for exemplar record signature information
     */
    public static String getExemplarFieldSignatureMetadata(String catalogName) {
        return getCatalog(catalogName).getString("exemplarField[@signatureMetadata]");
    }

    /**
     * Get username.
     * @param catalogName OPAC for which to get username.
     * @return username
     */
    public static String getUsername(String catalogName) {
        return getCatalog(catalogName).configurationAt("credentials").getString("username");
    }

    /**
     * Get FTP password.
     * @param catalogName OPAC for which to get FTP password.
     * @return FTP password
     */
    public static String getPassword(String catalogName) {
        return getCatalog(catalogName).configurationAt("credentials").getString("password");
    }

    /**
     * If a mappingFile for parentInRecord is configured.
     * @param catalogName OPAC for witch to get the config
     * @return true, if mapping file is configured
     */
    public static boolean isParentInRecord(String catalogName) {
        return StringUtils.isNotBlank(getXsltMappingFileForParentInRecord(catalogName));
    }

    /**
     * Return default import depth of catalog 'catalogName' if configured. Return DEFAULT_IMPORT_DEPTH 2 otherwise.
     * @param catalogName name of catalog
     * @return default import depth of catalog
     */
    public static int getDefaultImportDepth(String catalogName) {
        return getCatalog(catalogName).getInt("defaultImportDepth", DEFAULT_IMPORT_DEPTH);
    }

    /**
     * Return 'returnFormat' of catalog 'catalogName' if configured. Return DEFAULT_RETURN_FORMAT "XML" otherwise.
     * @param catalogName name of catalog
     * @return 'returnFormat' of catalog
     */
    public static String getReturnFormat(String catalogName) {
        return getCatalog(catalogName).getString("returnFormat", DEFAULT_RETURN_FORMAT);
    }

    /**
     * Return 'metadataFormat' of catalog 'catalogName.
     * @param catalogName name of catalog
     * @return 'metadataFormat' of catalog
     */
    public static String getMetadataFormat(String catalogName) {
        return getCatalog(catalogName).getString("metadataFormat");
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
            throw new ConfigException("Unable to find configuration of catalog '" + catalogName + "'!");
        }
    }

    private static XMLConfiguration getConfig() {
        if (config != null) {
            return config;
        }
        try {
            config = getKitodoOpacConfiguration();
        } catch (ConfigurationException e) {
            logger.error(e);
            config = new XMLConfiguration();
        }
        config.setListDelimiter('&');
        config.setReloadingStrategy(new FileChangedReloadingStrategy());
        return config;
    }

    /**
     * Retrieve and return the XML configuration of Kitodo OPACs from the 'kitodo_opac.xml' file.
     * @return XMLConfiguration containing the catalog configurations of the 'kitodo_opac.xml' file
     * @throws ConfigurationException if 'kitodo_opac.xml' file does not exist
     */
    public static XMLConfiguration getKitodoOpacConfiguration() throws ConfigurationException {
        KitodoConfigFile kitodoConfigOpacFile = KitodoConfigFile.OPAC_CONFIGURATION;
        if (!kitodoConfigOpacFile.exists()) {
            String message = "File not found: " + kitodoConfigOpacFile.getAbsolutePath();
            throw new ConfigException(message, new FileNotFoundException(message));
        }
        return new XMLConfiguration(kitodoConfigOpacFile.getFile());
    }
}
