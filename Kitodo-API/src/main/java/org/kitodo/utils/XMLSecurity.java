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

package org.kitodo.utils;

import java.io.InputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.SchemaFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Provides factory instances that are hardened against XML External Entity
 * (XXE) injection and unrestricted document type definitions.
 */
public final class XMLSecurity {

    private static final Logger logger = LogManager.getLogger(XMLSecurity.class);

    private static final String DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    private static final String EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";

    private XMLSecurity() {
    }

    /**
     * Create and return a DocumentBuilderFactory that rejects DOCTYPE declarations and
     * external entity resolution.
     *
     * @return hardened DocumentBuilderFactory
     * @throws ParserConfigurationException if a feature cannot be set
     */
    public static DocumentBuilderFactory newDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(DISALLOW_DOCTYPE_DECL, true);
        factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
        factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    /**
     * Create and return a TransformerFactory that restricts access to external DTDs
     * and stylesheets to prevent XML External Entity (XXE) injection.
     *
     * @return hardened TransformerFactory
     */
    public static TransformerFactory newTransformerFactory() {
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (IllegalArgumentException e) {
            logger.warn("Unable to restrict external access on TransformerFactory '{}': {}",
                    factory.getClass().getName(), e.getMessage());
        }
        return factory;
    }

    /**
     * Create and return a SchemaFactory that rejects external DTD access to prevent
     * XML External Entity (XXE) injection during XML validation.
     *
     * @return hardened SchemaFactory
     */
    public static SchemaFactory newSchemaFactory() {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        } catch (IllegalArgumentException | SAXNotRecognizedException | SAXNotSupportedException e) {
            logger.warn("Unable to restrict external access on SchemaFactory '{}': {}",
                    factory.getClass().getName(), e.getMessage());
        }
        return factory;
    }

    /**
     * Create and return an XMLInputFactory with DTD support and external entity
     * resolution disabled to prevent XML External Entity (XXE) injection.
     *
     * @return hardened XMLInputFactory
     */
    public static XMLInputFactory newXmlInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return factory;
    }

    /**
     * Create and return a SAXParserFactory that rejects DOCTYPE declarations and
     * external entity resolution to prevent XML External Entity (XXE) injection.
     *
     * @return hardened SAXParserFactory
     */
    public static SAXParserFactory newSaxParserFactory() {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        try {
            factory.setFeature(DISALLOW_DOCTYPE_DECL, true);
            factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
            factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new IllegalStateException("Unable to harden SAXParserFactory", e);
        }
        return factory;
    }

    /**
     * Create and return a SAXSource that rejects DOCTYPE declarations and external
     * entity resolution to prevent XML External Entity (XXE) injection during
     * transformation of the given input stream.
     *
     * @param inputStream input stream containing the XML document to transform
     * @return hardened SAXSource
     * @throws ParserConfigurationException if a feature cannot be set
     * @throws SAXException if the SAX parser cannot be created
     */
    public static SAXSource newSecureSource(InputStream inputStream) throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setFeature(DISALLOW_DOCTYPE_DECL, true);
        factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
        factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        XMLReader reader = factory.newSAXParser().getXMLReader();
        return new SAXSource(reader, new InputSource(inputStream));
    }
}
