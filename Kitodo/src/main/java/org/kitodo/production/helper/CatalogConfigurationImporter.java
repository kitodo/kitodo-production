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

package org.kitodo.production.helper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.ImportConfigurationType;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.api.schemaconverter.MetadataFormatConversion;
import org.kitodo.config.KitodoConfig;
import org.kitodo.config.OPACConfig;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.data.database.beans.SearchField;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.InvalidPortException;
import org.kitodo.exceptions.MandatoryParameterMissingException;
import org.kitodo.exceptions.MappingFilesMissingException;
import org.kitodo.exceptions.UndefinedMappingFileException;
import org.kitodo.production.services.ServiceManager;

/**
 * This class provides a way to convert catalog import configurations from existing 'kitodo_opac.xml'
 * configuration files into new 'ImportConfiguration' objects.
 */
public class CatalogConfigurationImporter {

    private static final Logger logger = LogManager.getLogger(CatalogConfigurationImporter.class);
    private static final String FILE_UPLOAD_DEFAULT_POSTFIX = " - "
            + Helper.getTranslation("newProcess.fileUpload.heading");
    private static final String SEARCH_FIELD = "searchField";
    private static final String LABEL = "[@label]";
    private static final String VALUE = "[@value]";
    private static final String MAPPING_FILES = "mappingFiles";

    /**
     * Convert catalog configuration with title 'opacTitle' to new 'ImportConfiguration' object. Also creates
     * corresponding SearchField objects and assigns MappingFile objects according to original XML configuration.
     * @param catalogName title of catalog configuration in XML file
     * @throws DAOException when available MappingFile objects could not be loaded from database or new
     *                      ImportConfiguration object could not be saved to database
     * @throws UndefinedMappingFileException when XML catalog configuration contains mapping file for which no
     *                                       corresponding MappingFile instance can be found
     * @throws MappingFilesMissingException when XML catalog configuration does not contain mandatory 'mappingFiles'
     *                                       element.
     * @throws InvalidPortException when XML catalog configuration contains an invalid port value.
     */
    private void convertOpacConfig(String catalogName, List<String> currentConfigurations) throws DAOException,
            UndefinedMappingFileException, MappingFilesMissingException, MandatoryParameterMissingException,
            InvalidPortException, URISyntaxException, IOException {
        HierarchicalConfiguration opacConfiguration = OPACConfig.getCatalog(catalogName);
        String fileUploadTitle = catalogName + FILE_UPLOAD_DEFAULT_POSTFIX;
        if (OPACConfig.getFileUploadConfig(catalogName) && !currentConfigurations.contains(fileUploadTitle)) {
            createFileUploadConfiguration(catalogName, fileUploadTitle);
        }
        ImportConfiguration importConfiguration = null;
        if (Objects.nonNull(opacConfiguration)) {
            importConfiguration = new ImportConfiguration();
            importConfiguration.setTitle(catalogName);
            importConfiguration.setDescription(OPACConfig.getOPACDescription(catalogName));
            importConfiguration.setConfigurationType(ImportConfigurationType.OPAC_SEARCH.toString());
            importConfiguration.setReturnFormat(OPACConfig.getReturnFormat(catalogName).toUpperCase());
            setMetadataFormat(importConfiguration, catalogName);
            setParentMappingFile(importConfiguration);
            setSearchInterfaceType(importConfiguration, catalogName);
            setUrl(importConfiguration, catalogName);
            setItemFields(importConfiguration, catalogName);
            importConfiguration.setDefaultImportDepth(OPACConfig.getDefaultImportDepth(catalogName));
            setCredentials(importConfiguration, catalogName);
            importConfiguration.setIdPrefix(OPACConfig.getIdentifierPrefix(catalogName));
            importConfiguration.setParentElementTrimMode(OPACConfig.getParentIDTrimMode(catalogName));
            importConfiguration.setQueryDelimiter(OPACConfig.getQueryDelimiter(catalogName));
            importConfiguration.setPrestructuredImport(OPACConfig.isPrestructuredImport(catalogName));
            if (SearchInterfaceType.SRU.name().equals(importConfiguration.getInterfaceType())
                    || SearchInterfaceType.CUSTOM.name().equals(importConfiguration.getInterfaceType())) {
                setSearchFields(importConfiguration, catalogName);
            }
            importConfiguration.setMappingFiles(getMappingFiles(importConfiguration));
            importConfiguration.setPrestructuredImport(OPACConfig.isPrestructuredImport(catalogName));
        }
        ServiceManager.getImportConfigurationService().saveToDatabase(importConfiguration);
    }

    private void setUrl(ImportConfiguration importConfiguration, String opacTitle)
            throws MandatoryParameterMissingException, InvalidPortException {
        importConfiguration.setScheme(OPACConfig.getScheme(opacTitle));
        importConfiguration.setHost(OPACConfig.getHost(opacTitle));
        importConfiguration.setPath(OPACConfig.getPath(opacTitle));
        importConfiguration.setPort(getPort(opacTitle));
    }

    private void setCredentials(ImportConfiguration importConfiguration, String opacTitle) {
        try {
            importConfiguration.setUsername(OPACConfig.getUsername(opacTitle));
            importConfiguration.setPassword(OPACConfig.getPassword(opacTitle));
        } catch (IllegalArgumentException e) {
            logger.info("No credentials configured for configuration '" + opacTitle + "'.");
        }
    }

    private void setSearchInterfaceType(ImportConfiguration importConfiguration, String opacTitle)
            throws MandatoryParameterMissingException {
        // use new "CUSTOM" type if SearchInterfaceType is null or could not be recognized
        SearchInterfaceType type = OPACConfig.getInterfaceType(opacTitle);
        if (Objects.nonNull(type) && Arrays.asList(SearchInterfaceType.values()).contains(type)) {
            importConfiguration.setInterfaceType(type.name());
        } else {
            importConfiguration.setInterfaceType(SearchInterfaceType.CUSTOM.name());
        }
        if (SearchInterfaceType.SRU.equals(type)) {
            importConfiguration.setSruVersion(OPACConfig.getSruVersion(opacTitle));
            importConfiguration.setSruRecordSchema(OPACConfig.getSruRecordSchema(opacTitle));
        } else if (SearchInterfaceType.OAI.equals(type)) {
            importConfiguration.setOaiMetadataPrefix(OPACConfig.getOaiMetadataPrefix(opacTitle));
        }
    }

    private void setMetadataFormat(ImportConfiguration importConfiguration, String opacTitle) {
        // set MetadataFormat to "OTHER" if configuration contains a value different from the options in the
        // "MetadataFormat.java" enum!
        String metadataFormat = OPACConfig.getMetadataFormat(opacTitle).toUpperCase();
        if (Arrays.stream(MetadataFormat.values()).map(MetadataFormat::name).collect(Collectors.toList())
                .contains(metadataFormat)) {
            importConfiguration.setMetadataFormat(metadataFormat);
        } else {
            importConfiguration.setMetadataFormat(MetadataFormat.OTHER.name());
        }
        importConfiguration.setMetadataRecordIdXPath(MetadataFormat.getDefaultRecordIdXpath(metadataFormat));
        importConfiguration.setMetadataRecordTitleXPath(MetadataFormat.getDefaultRecordTitleXpath(metadataFormat));
    }

    private void setSearchFields(ImportConfiguration importConfiguration, String opacTitle) {
        importConfiguration.setSearchFields(getSearchFields(importConfiguration));
        for (SearchField searchField : importConfiguration.getSearchFields()) {
            if (OPACConfig.getDefaultSearchField(opacTitle).equals(searchField.getLabel())) {
                importConfiguration.setDefaultSearchField(searchField);
                break;
            }
        }
        for (SearchField searchField : importConfiguration.getSearchFields()) {
            if (OPACConfig.getIdentifierParameter(opacTitle).equals(searchField.getValue())) {
                importConfiguration.setIdSearchField(searchField);
                break;
            }
        }
    }

    private void setItemFields(ImportConfiguration importConfiguration, String opacTitle) {
        importConfiguration.setItemFieldXpath(OPACConfig.getExemplarFieldXPath(opacTitle));
        importConfiguration.setItemFieldOwnerSubPath(OPACConfig.getExemplarFieldOwnerXPath(opacTitle));
        importConfiguration.setItemFieldOwnerMetadata(OPACConfig.getExemplarFieldOwnerMetadata(opacTitle));
        importConfiguration.setItemFieldSignatureSubPath(OPACConfig.getExemplarFieldSignatureXPath(opacTitle));
        importConfiguration.setItemFieldSignatureMetadata(OPACConfig.getExemplarFieldSignatureMetadata(opacTitle));
    }

    private void createFileUploadConfiguration(String catalogTitle, String fileUploadConfigurationTitle)
            throws DAOException, UndefinedMappingFileException, MappingFilesMissingException, URISyntaxException,
            IOException {
        ImportConfiguration fileUploadConfiguration = new ImportConfiguration();
        fileUploadConfiguration.setConfigurationType(ImportConfigurationType.FILE_UPLOAD.name());
        fileUploadConfiguration.setTitle(catalogTitle);
        fileUploadConfiguration.setDescription(OPACConfig.getOPACDescription(catalogTitle));
        fileUploadConfiguration.setReturnFormat(OPACConfig.getReturnFormat(catalogTitle).toUpperCase());
        setMetadataFormat(fileUploadConfiguration, catalogTitle);
        setParentMappingFile(fileUploadConfiguration);
        fileUploadConfiguration.setMappingFiles(getMappingFiles(fileUploadConfiguration));
        // update title to include "File upload" postfix! (original title is required until here to load mapping files!)
        fileUploadConfiguration.setTitle(fileUploadConfigurationTitle);
        ServiceManager.getImportConfigurationService().saveToDatabase(fileUploadConfiguration);
    }

    private void setParentMappingFile(ImportConfiguration config) throws DAOException {
        for (MappingFile mappingFile : ServiceManager.getMappingFileService().getAll()) {
            if (mappingFile.getFile().equals(OPACConfig.getXsltMappingFileForParentInRecord(config.getTitle()))) {
                config.setParentMappingFile(mappingFile);
                break;
            }
        }
    }

    private List<SearchField> getSearchFields(ImportConfiguration configuration) {
        List<SearchField> searchFields = new LinkedList<>();
        for (HierarchicalConfiguration searchFieldConfig : OPACConfig.getSearchFields(configuration.getTitle())
                .configurationsAt(SEARCH_FIELD)) {
            SearchField searchField = new SearchField();
            searchField.setLabel(searchFieldConfig.getString(LABEL));
            searchField.setValue(searchFieldConfig.getString(VALUE));
            searchField.setImportConfiguration(configuration);
            searchFields.add(searchField);
        }
        return searchFields;
    }

    private List<MappingFile> getMappingFiles(ImportConfiguration configuration) throws UndefinedMappingFileException,
            DAOException, MappingFilesMissingException, URISyntaxException, IOException {
        List<MappingFile> mappingFiles = new LinkedList<>();
        List<MappingFile> allMappingFiles = ServiceManager.getMappingFileService().getAll();
        try {
            OPACConfig.getCatalog(configuration.getTitle()).configurationAt(MAPPING_FILES);
            for (String filename : OPACConfig.getXsltMappingFiles(configuration.getTitle())) {
                mappingFiles.add(getConfiguredMappingFile(allMappingFiles, filename, configuration));
            }
        } catch (IllegalArgumentException e) {
            logger.info("No 'mappingFiles' element found in catalog configuration '" + configuration.getTitle()
                    + "', trying to determine default mapping files.");
            String formatName = OPACConfig.getMetadataFormat(configuration.getTitle());
            MetadataFormat metadataFormat = MetadataFormat.getMetadataFormat(formatName);
            List<MetadataFormatConversion> defaultConversions = MetadataFormatConversion
                    .getDefaultConfigurationFileName(metadataFormat);

            if (Objects.nonNull(defaultConversions)) {
                // add default metadata files to ImportConfiguration
                URI xsltDir = Paths.get(KitodoConfig.getParameter("directory.xslt")).toUri();
                for (MetadataFormatConversion metadataFormatConversion : defaultConversions) {
                    MappingFile mappingFile = null;
                    // Check, if default conversion has already been converted to MappingFile object
                    for (MappingFile currentFile : allMappingFiles) {
                        if (currentFile.getFile().equals(metadataFormatConversion.getFileName())) {
                            mappingFile = currentFile;
                            mappingFile.getImportConfigurations().add(configuration);
                            break;
                        }
                    }
                    // Create new MappingFile object if current default conversion has not yet been converted
                    if (Objects.isNull(mappingFile)) {
                        mappingFile = getMappingFile(xsltDir, metadataFormatConversion, formatName);
                        mappingFile.getImportConfigurations().add(configuration);
                    }
                    mappingFiles.add(mappingFile);
                }
            } else {
                throw new MappingFilesMissingException(formatName);
            }
        }
        return mappingFiles;
    }

    private MappingFile getConfiguredMappingFile(List<MappingFile> allMappingFiles, String filename,
                                                 ImportConfiguration configuration)
            throws UndefinedMappingFileException {
        MappingFile mappingFile = null;
        // Find mapping file object by filename
        for (MappingFile currentFile : allMappingFiles) {
            if (currentFile.getFile().equals(filename)) {
                mappingFile = currentFile;
                mappingFile.getImportConfigurations().add(configuration);
                break;
            }
        }
        if (Objects.isNull(mappingFile)) {
            // Happens when user skips conversion of a mapping file used in the current import configuration!
            throw new UndefinedMappingFileException(filename);
        }
        return mappingFile;
    }

    private MappingFile getMappingFile(URI xsltDir, MetadataFormatConversion metadataFormatConversion,
                                             String formatName)
            throws IOException, URISyntaxException, DAOException {
        MappingFile mappingFile = new MappingFile();
        URI xsltFile = xsltDir.resolve(new URI(metadataFormatConversion.getFileName()));
        if (!new File(xsltFile).exists() && Objects.nonNull(metadataFormatConversion.getSource())) {
            downloadXSLTFile(metadataFormatConversion.getSource(), xsltFile);
        }
        mappingFile.setFile(metadataFormatConversion.getFileName());
        mappingFile.setTitle(metadataFormatConversion.getFileName());
        mappingFile.setPrestructuredImport(false);
        mappingFile.setInputMetadataFormat(formatName);
        mappingFile.setOutputMetadataFormat(metadataFormatConversion.getTargetFormat().name());
        mappingFile.setImportConfigurations(new LinkedList<>());
        ServiceManager.getMappingFileService().saveToDatabase(mappingFile);
        return mappingFile;
    }

    /**
     * Retrieve and return 'port' value of catalog with name 'catalogName' from 'kitodo_opac.xml' configuration file.
     *
     * @param catalogName name of catalog
     * @return port value as integer
     * @throws InvalidPortException if port value retrieved from 'kitodo_opac.xml' is not an integer between 0 and 65535
     */
    public static Integer getPort(String catalogName) throws InvalidPortException {
        try {
            String portString = OPACConfig.getPort(catalogName);
            if (StringUtils.isNotBlank(portString)) {
                try {
                    int port = Integer.parseInt(portString);
                    if (port < 0 || port > 65535) {
                        throw new InvalidPortException(portString);
                    }
                    return port;
                } catch (NumberFormatException e) {
                    throw new InvalidPortException(portString);
                }
            }
        } catch (MandatoryParameterMissingException e) {
            // ignore exception because "port" is not mandatory
        }
        return null;
    }

    /**
     * Retrieve MappingFile objects from database for all catalog configurations in given list 'catalogs'.
     * @param catalogs list of catalog names as Strings
     * @return map containing mapping file names as keys and
     *         - corresponding MappingFile objects as values if it exists
     *         - null otherwise
     * @throws DAOException when MappingFile objects could not be loaded from database
     */
    public HashMap<String, MappingFile> getAllMappingFiles(List<String> catalogs) throws DAOException {
        HashMap<String, MappingFile> allMappingFiles = new HashMap<>();
        for (String catalogName : catalogs) {
            try {
                List<String> mappingFileNames = OPACConfig.getXsltMappingFiles(catalogName);
                String parentMappingFilename = OPACConfig.getXsltMappingFileForParentInRecord(catalogName);
                if (Objects.nonNull(parentMappingFilename) && !mappingFileNames.contains(parentMappingFilename)) {
                    mappingFileNames.add(parentMappingFilename);
                }
                for (String xsltFilename : mappingFileNames) {
                    if (!allMappingFiles.containsKey(xsltFilename)) {
                        allMappingFiles.put(xsltFilename, null);
                        for (MappingFile mappingFile : ServiceManager.getMappingFileService().getAll()) {
                            if (mappingFile.getFile().equals(xsltFilename)) {
                                allMappingFiles.put(xsltFilename, mappingFile);
                                break;
                            }
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                logger.error("Unable to import OPAC configuration '" + catalogName + "' (" + e.getMessage() + ")");
            }
        }
        return allMappingFiles;
    }

    /**
     * Import all catalog configurations in given list 'catalogs' from configuration file 'kitodo_opac.xml' to new
     * ImportConfigurations. Return a map containing the results of the import process. The map contains the catalog
     * names as keys and potential error messages as values. Map entries with empty values mark successful catalog
     * configuration imports.
     *
     * @param catalogs list of catalog configurations to import from 'kitodo_opac.xml' by name
     * @return success map of catalog configuration import process
     * @throws DAOException when existing ImportConfigurations could not be retrieved from database
     */
    public HashMap<String, String> importCatalogConfigurations(List<String> catalogs) throws DAOException {
        HashMap<String, String> conversions = new HashMap<>();
        List<String> currentConfigurations = ServiceManager.getImportConfigurationService().getAll()
                .stream().map(ImportConfiguration::getTitle).collect(Collectors.toList());
        for (String catalog : catalogs) {
            if (currentConfigurations.contains(catalog)) {
                conversions.put(catalog, Helper.getTranslation("importConfig.migration.error.configurationExists"));
            } else {
                try {
                    convertOpacConfig(catalog, currentConfigurations);
                    conversions.put(catalog, null);
                } catch (UndefinedMappingFileException | MappingFilesMissingException
                         | MandatoryParameterMissingException | InvalidPortException | URISyntaxException
                         | IOException e) {
                    conversions.put(catalog, e.getMessage());
                } catch (DAOException e) {
                    if (Objects.nonNull(e.getCause()) && Objects.nonNull(e.getCause().getCause())) {
                        conversions.put(catalog, e.getCause().getCause().getMessage());
                    } else {
                        conversions.put(catalog, e.getMessage());
                    }
                }
            }
        }
        return conversions;
    }

    /**
     * Download XSLT file from given URL 'source' and save it to file located at given URI 'target'.
     * @param source URL of XSLT file to download.
     * @param target URI of location where downloaded files should be saved.
     * @throws IOException when file cannot be saved to provided location
     */
    public static void downloadXSLTFile(URL source, URI target) throws IOException {
        if (Objects.nonNull(source) && Objects.nonNull(target)) {
            FileUtils.copyURLToFile(source, new File(target));
        }
    }
}
