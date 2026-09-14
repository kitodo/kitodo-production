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
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

/**
 * Provides factory instances that are hardened against XML External Entity
 * (XXE) injection and unrestricted document type definitions. Every helper
 * fails closed: if a required security feature cannot be set it returns an
 * error rather than a silently unhardened instance. Schema resolution is
 * additionally restricted to local files, so xs:import/xs:include cannot reach
 * the network while the bundled local schema imports continue to resolve.
 */
public final class XMLSecurity {

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
     * @throws IllegalStateException if the hardening properties cannot be set
     */
    public static TransformerFactory newTransformerFactory() {
        TransformerFactory factory = TransformerFactory.newInstance();
        try {
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Unable to harden TransformerFactory " + factory.getClass().getName(), e);
        }
        return factory;
    }

    /**
     * Create and return a SchemaFactory that rejects external DTD access and restricts
     * external schema resolution to local files only, to prevent XML External Entity
     * (XXE) injection and remote schema retrieval during XML validation. The bundled
     * local schema imports (e.g. mods-3-4.xsd) continue to resolve, while network
     * schemas are denied.
     *
     * @return hardened SchemaFactory
     * @throws IllegalStateException if the hardening properties cannot be set
     */
    public static SchemaFactory newSchemaFactory() {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file");
        } catch (IllegalArgumentException | SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new IllegalStateException(
                    "Unable to harden SchemaFactory " + factory.getClass().getName(), e);
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
     * Create and return a hardened SAXSource that rejects DOCTYPE declarations and
     * external entity resolution to prevent XML External Entity (XXE) injection during
     * transformation of the given input stream.
     *
     * @param inputStream input stream containing the XML document to transform
     * @return hardened SAXSource
     * @throws ParserConfigurationException if a feature cannot be set
     * @throws SAXException if the SAX parser cannot be created
     */
    public static SAXSource newSecureSource(InputStream inputStream) throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature(DISALLOW_DOCTYPE_DECL, true);
        factory.setFeature(EXTERNAL_GENERAL_ENTITIES, false);
        factory.setFeature(EXTERNAL_PARAMETER_ENTITIES, false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        XMLReader reader = factory.newSAXParser().getXMLReader();
        return new SAXSource(reader, new InputSource(inputStream));
    }

    /**
     * Create and return a Validator from the given Schema with external DTD access
     * blocked and external schema resolution limited to local files, to prevent
     * XML External Entity (XXE) injection and remote schema retrieval during
     * validation.
     *
     * DOCTYPE declarations must be rejected by the caller, e.g. by feeding the
     * input through newSecureSource(), since the JAXP Validator does not
     * universally support disallow-doctype-decl.
     *
     * @param schema compiled XML schema
     * @return hardened Validator
     * @throws SAXException if the Validator cannot be created
     */
    public static Validator newSecureValidator(Schema schema) throws SAXException {
        Validator validator = schema.newValidator();
        try {
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "file");
        } catch (IllegalArgumentException | SAXNotRecognizedException | SAXNotSupportedException e) {
            throw new IllegalStateException("Unable to harden Validator", e);
        }
        return validator;
    }
}
