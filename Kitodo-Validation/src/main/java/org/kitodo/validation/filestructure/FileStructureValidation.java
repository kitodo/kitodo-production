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

package org.kitodo.validation.filestructure;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.validation.State;
import org.kitodo.api.validation.ValidationResult;
import org.kitodo.api.validation.filestructure.FileStructureValidationInterface;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Implementation of the FileStructureValidationInterface. Validates an XML file or string against a given xsd schema.
 * Based on <a href="https://www.baeldung.com/java-validate-xml-xsd"/>
 */
public class FileStructureValidation implements FileStructureValidationInterface {

    private static final Logger logger = LogManager.getLogger(FileStructureValidation.class);

    @Override
    public ValidationResult validate(String xmlContent, URI xsdFileUri) throws SAXException, IOException {
        Collection<URI> schemaUris = Collections.singletonList(xsdFileUri);
        Validator xmlValidator = initializeXmlValidator(schemaUris);
        return validateStreamSource(new StreamSource(new StringReader(xmlContent)), xmlValidator, "N/A", schemaUris);
    }

    @Override
    public ValidationResult validate(URI xmlFileUri, URI xsdFileUri) throws SAXException, IOException {
        Collection<URI> schemaUris = Collections.singletonList(xsdFileUri);
        Validator xmlValidator = initializeXmlValidator(schemaUris);
        return validateStreamSource(new StreamSource(new File(xmlFileUri)), xmlValidator, xmlFileUri.getPath(), schemaUris);
    }

    @Override
    public ValidationResult validate(String xmlContent, Collection<URI> xsdFiles) throws IOException, SAXException {
        Validator xmlValidator = initializeXmlValidator(xsdFiles);
        return validateStreamSource(new StreamSource(new StringReader(xmlContent)), xmlValidator, "N/A", xsdFiles);
    }

    private ValidationResult validateStreamSource(StreamSource source, Validator validator, String xmlPath, Collection<URI> xsdPaths)
            throws IOException {
        try {
            validator.validate(source);
        } catch (SAXException e) {
            logger.error("XML file '{}' is not valid against schemata '{}': {}", xmlPath, xsdPaths.stream()
                    .map(URI::getPath).collect(Collectors.joining(", ")), e.getMessage());
        }

        List<SAXParseException> xmlValidationErrors = ((FileStructureValidationErrorHandler)validator.getErrorHandler())
                .getValidationErrors();
        if (xmlValidationErrors.isEmpty()) {
            return new ValidationResult(State.SUCCESS, Collections.emptyList());
        } else {
            return new ValidationResult(State.ERROR, xmlValidationErrors.stream()
                    .map(exception -> String.format("Line: %s, Column: %s: %s",
                            exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage()))
                    .distinct()
                    .collect(Collectors.toList()));
        }
    }

    private Validator initializeXmlValidator(Collection<URI> xsdFilePaths) throws SAXException {
        FileStructureValidationErrorHandler xmlValidationErrorHandler = new FileStructureValidationErrorHandler();
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source[] sources = new Source[xsdFilePaths.size()];
        for (int i = 0; i < xsdFilePaths.size(); i++) {
            sources[i] = new StreamSource(new File(xsdFilePaths.toArray(new URI[0])[i]));
        }
        Schema schema = schemaFactory.newSchema(sources);
        Validator xmlValidator = schema.newValidator();
        xmlValidator.setErrorHandler(xmlValidationErrorHandler);
        return xmlValidator;
    }

}
