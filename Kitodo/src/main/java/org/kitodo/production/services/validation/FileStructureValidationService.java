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

package org.kitodo.production.services.validation;

import static org.kitodo.api.validation.State.ERROR;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.SearchInterfaceType;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.filestructure.FileStructureValidationInterface;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.xml.sax.SAXException;

public class FileStructureValidationService {

    private static final Logger logger = LogManager.getLogger(FileStructureValidationService.class);

    FileStructureValidationInterface validationModule = getValidationModule();
    private static final String METS_XSD = "mets.xsd";
    private static final String KITODO_XSD = "kitodo.xsd";

    private FileStructureValidationInterface getValidationModule() {
        KitodoServiceLoader<FileStructureValidationInterface> loader = new KitodoServiceLoader<>(
                FileStructureValidationInterface.class);
        return loader.loadModule();
    }

    private ValidationResult validateXmlFile(File xmlFile, URI xsdFileUri) throws IOException, SAXException {
        return validationModule.validate(xmlFile.toURI(), xsdFileUri);
    }

    private ValidationResult validateXmlFile(String xmlContent, Collection<String> xsdFileNames) throws IOException, SAXException {
        if (xsdFileNames.isEmpty()) {
            return null;
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<URI> schemaUris = new ArrayList<>();
        for (String filename : xsdFileNames) {
            if (StringUtils.isNotBlank(filename)) {
                URL schemaUrl = classLoader.getResource("schemata/" + filename);
                if (Objects.nonNull(schemaUrl)) {
                    try {
                        schemaUris.add(schemaUrl.toURI());
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    throw new RuntimeException("Schema file " + filename + " not found.");
                }
            }
        }
        return validationModule.validate(xmlContent, schemaUris);
    }

    /**
     * Validates the given ruleset against its schema definition to ensure its structure
     * and content conform to the required standards.
     *
     * @param ruleset the {@code Ruleset} instance to be validated
     * @throws IOException if an I/O operation fails during validation
     * @throws SAXException if the ruleset schema definition is malformed
     * @throws FileStructureValidationException if the structure of the ruleset is invalid
     */
    public void validateRuleset(Ruleset ruleset) throws IOException, SAXException, FileStructureValidationException {
        URL rulesetSchemaUrl = Thread.currentThread().getContextClassLoader().getResource("schemata/ruleset.xsd");
        String rulesetDirectory = ConfigCore.getParameter(ParameterCore.DIR_RULESETS);
        ValidationResult validationResult = validateFileAgainstSchemaByUrl(ruleset.getFile(), rulesetSchemaUrl, rulesetDirectory);
        if (Objects.nonNull(validationResult) && !validationResult.getResultMessages().isEmpty()) {
            throw new FileStructureValidationException(Helper.getTranslation("validation.rulesetValidationErrorTitle",
                    ruleset.getFile()), validationResult);
        }
    }

    private ValidationResult validateFileAgainstSchemaByUrl(String filename, URL schemaUrl, String containingDirectory)
            throws IOException, SAXException {
        if (StringUtils.isNotBlank(containingDirectory) && Objects.nonNull(schemaUrl)) {
            File xmlFile = new File(containingDirectory + filename);
            try {
                return validateXmlFile(xmlFile, schemaUrl.toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    /**
     * Validates the ruleset associated with the template identified by the given template ID.
     * The method retrieves the template using the provided ID and validates its ruleset to ensure it is valid
     * in accordance with the ruleset XML schema definition.
     *
     * @param templateId the ID of the template whose associated ruleset is to be validated
     * @throws FileStructureValidationException if the structure of the ruleset is invalid
     */
    public void validateRulesetByTemplateId(int templateId) throws FileStructureValidationException {
        Template template;
        try {
            template = ServiceManager.getTemplateService().getById(templateId);
        } catch (DAOException e) {
            ValidationResult result = new ValidationResult(ERROR, Collections.singletonList(e.getLocalizedMessage()));
            String errorMessage = Helper.getTranslation("errorLoadingOne", Helper.getTranslation("template"),
                    String.valueOf(templateId));
            throw new FileStructureValidationException(errorMessage, result);
        }
        try {
            validateRuleset(template.getRuleset());
        } catch (SAXException | IOException e) {
            ValidationResult result = new ValidationResult(ERROR, Collections.singletonList(e.getLocalizedMessage()));
            String errorMessage = Helper.getTranslation("errorLoadingOne", Helper.getTranslation("ruleset"),
                    String.valueOf(template.getRuleset().getId()));
            throw new FileStructureValidationException(errorMessage, result);
        }
    }

    /**
     * Validates the given mapping file to ensure its structure and content conform to the required standards.
     *
     * @param mappingFile the {@code MappingFile} instance to be validated
     * @throws IOException if an I/O operation fails during validation
     * @throws SAXException if the mapping file XML schema definition is malformed
     * @throws FileStructureValidationException if the structure of the mapping file is invalid
     */
    public void validateMappingFile(MappingFile mappingFile) throws IOException, SAXException, FileStructureValidationException {
        URL xsltSchemaUrl = Thread.currentThread().getContextClassLoader().getResource("schemata/xslt20.xsd");
        String mappingFileDirectory = ConfigCore.getParameter(ParameterCore.DIR_XSLT);
        ValidationResult validationResult = validateFileAgainstSchemaByUrl(mappingFile.getFile(), xsltSchemaUrl, mappingFileDirectory);
        if (Objects.nonNull(validationResult) && !validationResult.getResultMessages().isEmpty()) {
            throw new FileStructureValidationException(Helper.getTranslation("validation.mappingFileValidationError",
                    mappingFile.getFile()), validationResult);
        }
    }

    /**
     * Validates a list of mapping files to ensure their structure and content conform to the required standards.
     *
     * @param mappingFiles a list of MappingFile objects to be validated
     * @throws IOException if an I/O operation fails during validation
     * @throws SAXException if a mapping file XML schema definition is malformed
     * @throws FileStructureValidationException if the structure of a mapping file is invalid
     */
    public void validateMappingFiles(List<MappingFile> mappingFiles) throws IOException, SAXException, FileStructureValidationException {
        for (MappingFile mappingFile : mappingFiles) {
            validateMappingFile(mappingFile);
        }
    }

    /**
     * Validate the internal record given as XML string 'xmlString' against the Kitodo XML schema definition.
     * If the internal record was created using pre-structured import, it is supposed to also contain the METS container
     * if the internal format and thus should be validated against the METS schema definition as well.
     *
     * @param xmlString String containing the XML representation of the internal record
     * @param validateMets flag indicating whether the METS part of the internal record should be validated as well
     *                     (true for records created with "prestructured import")
     * @param mappingFiles String containing the list of mapping file names used to create the internal record
     * @throws IOException
     *            when loading schema definition for metadata file validation fails
     * @throws SAXException
     *            when schema definition for metadata file validation contains invalid XML syntax
     * @throws FileStructureValidationException
     *            when validating the metadata file fails
     */
    public void validateInternalRecord(String xmlString, boolean validateMets, String mappingFiles) throws IOException,
            SAXException, FileStructureValidationException {
        Collection<String> schemata = new ArrayList<>();
        // always validate internalRecord against kitodo schema
        schemata.add(KITODO_XSD);
        // only validate against METS schema as well if the record was created using "prestructured import"
        if (validateMets) {
            schemata.add(METS_XSD);
        }
        ValidationResult validationResult = validateXmlFile(xmlString, schemata);
        if (Objects.nonNull(validationResult) && !validationResult.getResultMessages().isEmpty()) {
            logger.info("Validation errors for internal record: {}", validationResult.getResultMessages());
            if (StringUtils.isBlank(mappingFiles)) {
                throw new FileStructureValidationException(Helper.getTranslation(
                        "validation.internalDataRecordValidationError"), validationResult);
            } else {
                throw new FileStructureValidationException(Helper.getTranslation(
                        "validation.internalDataRecordAndMappingFilesValidationError", mappingFiles), validationResult);
            }
        }
    }

    /**
     * Validate the external XML record returned from a search interface against the schema definitions corresponding
     * to the metadata format configured in the given import configuration.
     *
     * @param xmlContent XML content of the external record
     * @param importConfiguration Import configuration specifying the metadata format and search interface type
     * @param identifier record identifier for potential error messages
     * @throws IOException when loading the schema definition file(s) for the validation fails
     * @throws SAXException when parsing the schema definition file(s) for the validation fails
     */
    public void validateExternalRecord(String xmlContent, ImportConfiguration importConfiguration, String identifier)
            throws IOException, SAXException, FileStructureValidationException {
        Collection<String> schemaFiles = new ArrayList<>(SearchInterfaceType.getSchemaFile(importConfiguration.getInterfaceType()));
        String schemaDefinition = MetadataFormat.getMetadataFormat(importConfiguration.getMetadataFormat()).getSchemaDefinitionFileName();
        if (StringUtils.isNotBlank(schemaDefinition)) {
            schemaFiles.add(schemaDefinition);
        }
        ValidationResult validationResult = validateXmlFile(xmlContent, schemaFiles);
        if (Objects.nonNull(validationResult) && !validationResult.getResultMessages().isEmpty()) {
            throw new FileStructureValidationException(Helper.getTranslation("validation.externalDataRecordValidationError",
                    identifier,
                    importConfiguration.getInterfaceType() + "/" + importConfiguration.getMetadataFormat(),
                    String.join(", ", schemaFiles)),
                    validationResult, true);
        }
    }
}
