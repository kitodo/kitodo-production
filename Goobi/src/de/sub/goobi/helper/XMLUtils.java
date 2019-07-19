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

package de.sub.goobi.helper;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.NoSuchElementException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The class XMLUtils contains an omnium-gatherum of functions that work on XML.
 *
 * @author Matthias Ronge
 */
public class XMLUtils {

    /**
     * The method documentToByteArray() converts an org.w3c.dom.Document to a
     * ByteArray for Downloading
     *
     * @param data
     *            The document to convert
     * @param indent
     *            No of spaces to use for indenting. Use “null” to disable
     * @return the XML data as byte[]
     *
     * @throws TransformerException
     *             when it is not possible to create a Transformer instance or
     *             if an unrecoverable error occurs during the course of the
     *             transformation
     */
    public static byte[] documentToByteArray(Document data, Integer indent) throws TransformerException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        if (indent != null) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent.toString());
        }
        transformer.transform(new DOMSource(data), new StreamResult(result));
        return result.toByteArray();
    }

    /**
     * The function getFirstChildWithTagName() returns the first child node from
     * a node, identified by its node name
     *
     * @param data
     *            Document or Element whose children shall be examined
     * @param tagName
     *            name of the node to find
     * @return first child node with that node name
     * @throws NoSuchElementException
     *             if no child node with that name can be found
     */
    public static Element getFirstChildWithTagName(Node data, String tagName) throws NoSuchElementException {
        for (Node element = data.getFirstChild(); element != null; element = element.getNextSibling()) {
            if (!(element instanceof Element))
                continue;
            if (element.getNodeName().equals(tagName))
                return (Element) element;
        }
        throw new NoSuchElementException(tagName);
    }

    /**
     * The function load() is a convenience method load a DOM Document object
     * from an input stream.
     *
     * @param data
     *            InputStream to read from
     * @return the DOM Document encoded in the input stream’s data
     * @throws SAXException
     *             if any parse errors occur
     * @throws IOException
     *             if any IO errors occur
     * @throws RuntimeException
     *             if a DocumentBuilder cannot be created which satisfies the
     *             configuration requested—which never happens because we use
     *             the default configuration here and that is definitely
     *             supported
     */
    public static Document load(InputStream data) throws SAXException, IOException {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * The function newDocument() is a convenience method to obtain a new
     * instance of a DOM Document object.
     *
     * @return A new DOM Document
     * @throws RuntimeException
     *             if a DocumentBuilder cannot be created which satisfies the
     *             configuration requested—which never happens because we use
     *             the default configuration here and that is definitely
     *             supported
     */
    public static Document newDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Validates the XML contained in the given String 'xmlString' against the
     * schema definition file with the given filepath 'xsdFilePath' and returns
     * whether the validation was successful or not, i.e. whether the XML in
     * 'xmlString' is valid or not.
     *
     * @param xmlString
     *              String containing the XML document that is validated
     * @param xsdFilePath
     *              filepath of the schema definition file against which the XML will be validated
     * @return whether the given XML is valid according to the referenced schema definition file
     * @throws IOException
     *              if XML schema definition file cannot be found
     */
    public static boolean validateXML(String xmlString, String xsdFilePath) throws IOException {
        try (InputStream xsdFileStream = new FileInputStream(xsdFilePath)) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new StreamSource(xsdFileStream));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlString)));
            return true;
        } catch (SAXException e) {
            return false;
        }
    }

}
