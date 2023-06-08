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

package org.kitodo.production.editor;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.enums.KitodoConfigFile;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@Named("XMLEditor")
@ViewScoped
public class XMLEditor implements Serializable {

    private static final Logger logger = LogManager.getLogger(XMLEditor.class);
    private KitodoConfigFile configurationFile;
    private String currentConfigurationFile = "";
    private String xmlConfigurationString = "";
    private static DocumentBuilder documentBuilder = null;

    /**
     * Constructor.
     */
    public XMLEditor() {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error("ERROR: unable to instantiate document builder: {}", e.getMessage());
        }
    }

    /**
     * Load first configuration file when bean is created.
     */
    @PostConstruct
    public void loadInitialConfiguration() {
        loadProjectConfiguration();
    }

    /**
     * Get the XML configuration string.
     *
     * @return the XML configuration string
     */
    public String getXMLConfiguration() {
        return this.xmlConfigurationString;
    }

    /**
     * Set the XML configuration string.
     *
     * @param configuration
     *            the XML configuration string
     */
    public void setXMLConfiguration(String configuration) {
        this.xmlConfigurationString = configuration;
    }

    /**
     * Get name of configuration file currently loaded into frontend editor.
     *
     * @return configuration file name
     */
    public String getCurrentConfigurationFile() {
        return currentConfigurationFile;
    }

    /**
     * Load the content of the XML configuration file with the given name
     * 'configurationFile'.
     *
     * @param configurationFile
     *            name of the configuration to be loaded
     */
    public void loadXMLConfiguration(String configurationFile) {
        try {
            currentConfigurationFile = configurationFile;
            this.configurationFile = KitodoConfigFile.getByName(configurationFile);
            try (Stream<String> lines = Files.lines(this.configurationFile.getFile().toPath())) {
                this.xmlConfigurationString = lines.collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            String errorMessage = "ERROR: Unable to load configuration file '" + configurationFile + "'.";
            logger.error("{} {}", errorMessage, e.getMessage());
            this.xmlConfigurationString = errorMessage;
        }
    }

    /**
     * Save the String 'xmlConfigurationString' to the XML file denoted by
     * 'configurationFile'.
     */
    public void saveXMLConfiguration() {
        logger.info("Saving configuration to file {}", currentConfigurationFile);
        try {
            Document document = documentBuilder.parse(new InputSource(new StringReader(this.xmlConfigurationString)));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);
            try (FileOutputStream outputStream = new FileOutputStream(configurationFile.getFile(), false);
                    PrintWriter printWriter = new PrintWriter(outputStream)) {
                StreamResult streamResult = new StreamResult(printWriter);
                transformer.transform(domSource, streamResult);
            } catch (TransformerException e) {
                logger.error("ERROR: transformation failed: {}", e.getMessage());
            }
        } catch (TransformerConfigurationException e) {
            logger.error("ERROR: transformer configuration exception: {}", e.getMessage());
        } catch (FileNotFoundException e) {
            logger.error("ERROR: file not found: " + e.getMessage());
        } catch (IOException e) {
            logger.error("ERROR: could not save XML configuration: {}", e.getMessage());
        } catch (SAXException e) {
            logger.error("ERROR: error parsing given XML string: {}", e.getMessage());
        }
    }

    /**
     * Check and return whether the given String 'xmlCode' contains well formed XML
     * code or not.
     *
     * @param facesContext
     *            the current FacesContext
     * @param uiComponent
     *            the component containing the String that is being validated
     * @param xmlCode
     *            XML code that will be validated
     * @return whether 'xmlCode' is well formed or not
     */
    public boolean validateXMLConfiguration(FacesContext facesContext, UIComponent uiComponent, String xmlCode) {
        if (!Objects.equals(documentBuilder, null)) {
            InputSource inputSource = new InputSource(new StringReader(xmlCode));
            try {
                documentBuilder.parse(inputSource);
                return true;
            } catch (SAXParseException e) {
                // parse method throwing an SAXParseException means given xml code is not well
                // formed!
                String errorString = "Error while parsing XML: line = " + e.getLineNumber() + ", column = "
                        + e.getColumnNumber() + ": " + e.getMessage();
                FacesMessage errorMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "XML parsing error",
                        errorString);
                facesContext.addMessage(uiComponent.getClientId(), errorMessage);
                logger.error(errorString);
                return false;
            } catch (SAXException | IOException e) {
                logger.error("{}: {}", e.getClass().getSimpleName(), e.getMessage());
                return false;
            }
        } else {
            logger.error("ERROR: document builder is null!");
            return false;
        }
    }

    /**
     * Load the project XML configuration file and display its content in the
     * editor.
     */
    public void loadProjectConfiguration() {
        loadXMLConfiguration(KitodoConfigFile.PROJECT_CONFIGURATION.getName());
    }

}
