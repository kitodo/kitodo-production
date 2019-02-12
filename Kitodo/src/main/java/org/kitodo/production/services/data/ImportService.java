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

package org.kitodo.production.services.data;

import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.DOMOutputter;
import org.kitodo.api.externaldatamanagement.ExternalDataImportInterface;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.OPACConfig;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.exceptions.ConfigException;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ImportService {

    private static final Logger logger = LogManager.getLogger(ImportService.class);

    private static ImportService instance = null;
    private static ExternalDataImportInterface importModule;

    /**
     * Return singleton variable of type ImportService.
     *
     * @return unique instance of ImportService
     */
    public static ImportService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (ImportService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new ImportService();
                }
            }
        }
        return instance;
    }

    /**
     * Load ExternalDataImportInterface implementation with KitodoServiceLoader and perform given query string
     * with loaded module.
     *
     * @param searchField field to query
     * @param searchTerm  given search term
     * @param catalogName catalog to search
     * @return search result
     */
    public SearchResult performSearch(String searchField, String searchTerm, String catalogName) {
        importModule = initializeImportModule();
        try {
            OPACConfig.getOPACConfiguration(catalogName);
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: OPAC '" + catalogName + "' is not supported!");
        }
        return importModule.search(catalogName, searchField, searchTerm, 10);
    }

    private ExternalDataImportInterface initializeImportModule() {
        KitodoServiceLoader<ExternalDataImportInterface> loader =
                new KitodoServiceLoader<>(ExternalDataImportInterface.class);
        return loader.loadModule();
    }

    /**
     * Load search fields of catalog with given name 'opac' from OPAC configuration file and return them as a list
     * of Strings.
     *
     * @param opac name of catalog whose search fields are loaded
     * @return list containing search fields
     */
    public List<String> getAvailableSearchFields(String opac) {
        try {
            HierarchicalConfiguration searchFields = OPACConfig.getSearchFields(opac);
            List<String> fields = new ArrayList<>();
            for (HierarchicalConfiguration searchField : searchFields.configurationsAt("searchField")) {
                fields.add(searchField.getString("[@label]"));
            }
            return fields;
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: OPAC '" + opac + "' is not supported!");
        }
    }

    /**
     * Load catalog names from OPAC configuration file and return them as a list of Strings.
     *
     * @return list of catalog names
     */
    public List<String> getAvailableCatalogs() {
        try {
            return OPACConfig.getCatalogs();
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: no supported OPACs found in configuration file!");
        }
    }

    /**
     * Get the full record with the given ID from the catalog.
     *
     * @param opac The ID of the catalog that will be queried.
     * @param id   The ID of the record that will be imported.
     * @return The queried record transformed into Kitodo internal format.
     */
    public Document getSelectedRecord(String opac, String id) {
        importModule = initializeImportModule();
        Document sruResponse = importModule.getFullRecordById(opac, id);
        File xsltFile = new File(ConfigCore.getParameter(ParameterCore.DIR_XSLT) + OPACConfig.getXsltMappingFile(opac));
        return transformXmlByXslt(convertDocumentToString(sruResponse), xsltFile);
    }

    private Document transformXmlByXslt(String xmlString, File stylesheetFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setNamespaceAware(true);
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            DOMOutputter outputter = new DOMOutputter();
            StreamSource transformSource = new StreamSource(stylesheetFile);
            TransformerFactoryImpl transformerFactoryImpl = new TransformerFactoryImpl();
            File outputFile = File.createTempFile("transformed", "xml");
            try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                Transformer xsltTransformer = transformerFactoryImpl.newTransformer(transformSource);
                TransformerHandler handler = ((SAXTransformerFactory) SAXTransformerFactory.newInstance()).newTransformerHandler();
                handler.setResult(new StreamResult(outputStream));
                Result saxResult = new SAXResult(handler);
                SAXSource saxSource = new SAXSource(new InputSource(new StringReader(xmlString)));
                xsltTransformer.transform(saxSource, saxResult);
            }
            return outputter.output(saxBuilder.build(outputFile));
        } catch (JDOMException | IOException | TransformerException e) {
            throw new ConfigException("Error in transforming the response in intern format : ", e);
        }
    }

    /**
     * Convert given Document 'doc' to String and return it.
     *
     * @param doc the Document to be converted
     * @return the String content of the given Document
     */
    private static String convertDocumentToString(Document doc) {
        try {
            StringWriter writer = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new ConfigException("This document '" + doc.getTextContent() + "' can not be converted to String");
        }
    }
}
