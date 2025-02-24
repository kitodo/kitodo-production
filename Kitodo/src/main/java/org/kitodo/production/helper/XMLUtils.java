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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.constants.StringConstants;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The class XMLUtils contains an omnium-gatherum of functions that work on XML.
 */
public class XMLUtils {

    /**
     * Private constructor to hide the implicit public one.
     */
    private XMLUtils() {

    }

    /**
     * Converts an org.w3c.dom.Document to a
     * ByteArray for Downloading
     *
     * @param data
     *            The document to convert
     * @param indent
     *            No of spaces to use for indenting. Use “null” to disable
     * @return the XML data as byte[]
     * @throws TransformerException
     *             when it is not possible to create a Transformer instance or
     *             if an unrecoverable error occurs during the course of the
     *             transformation
     */
    public static byte[] documentToByteArray(Document data, Integer indent) throws TransformerException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        if (Objects.nonNull(indent)) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent.toString());
        }
        transformer.transform(new DOMSource(data), new StreamResult(result));
        return result.toByteArray();
    }

    /**
     * Returns the first child node from
     * a node, identified by its node name.
     *
     * @param data
     *            Document or Element whose children shall be examined
     * @param tagName
     *            name of the node to find
     * @return first child node with that node name
     * @throws NoSuchElementException
     *             if no child node with that name can be found
     */
    public static Element getFirstChildWithTagName(Node data, String tagName) {
        for (Node element = data.getFirstChild(); Objects.nonNull(element); element = element.getNextSibling()) {
            if (!(element instanceof Element)) {
                continue;
            }
            if (element.getNodeName().equals(tagName)) {
                return (Element) element;
            }
        }
        throw new NoSuchElementException(tagName);
    }

    /**
     * The function is a convenience method load a DOM Document object
     * from an input stream.
     *
     * @param data
     *            InputStream to read from
     * @return the DOM Document encoded in the input stream’s data
     * @throws SAXException
     *             if any parse errors occur
     * @throws IOException
     *             if any IO errors occur
     * @throws IOException
     *             if a DocumentBuilder cannot be created which satisfies the
     *             configuration requested—which never happens because we use
     *             the default configuration here and that is definitely
     *             supported, but in case - wrap it in IOException
     */
    public static Document load(InputStream data) throws SAXException, IOException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return documentBuilderFactory.newDocumentBuilder().parse(data);
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * The function is a convenience method to obtain a new
     * instance of a DOM Document object.
     *
     * @return A new DOM Document
     * @throws IOException
     *             if a DocumentBuilder cannot be created which satisfies the
     *             configuration requested—which never happens because we use
     *             the default configuration here and that is definitely
     *             supported, but in case - wrap it in IOException
     */
    public static Document newDocument() throws IOException {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return documentBuilderFactory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Parse given String 'xmlString' and create Document from it.
     *
     * @param xmlString the String that will be parsed and converted to a Document
     * @return Document
     *          the Document created from the given String 'xmlString'
     * @throws ParserConfigurationException thrown when DocumentBuilder cannot be created
     * @throws IOException thrown when input stream cannot be parsed
     * @throws SAXException thrown when input stream cannot be parsed
     */
    public static Document parseXMLString(String xmlString) throws IOException, ParserConfigurationException,
            SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        xmlString = removeBom(xmlString);
        return builder.parse(new InputSource(new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8))));
    }

    /**
     * Compile given String "xpathString" and check whether it contains a valid XPath (e.g. if the syntax is correct)
     * Throws an exception if syntax is incorrect. Can be used to validate XPaths.
     *
     * @param xpathString String to be checked for correct XPath syntax
     * @throws XPathExpressionException if provided String does not contain valid XPath syntax
     */
    public static void validateXPathSyntax(String xpathString) throws XPathExpressionException {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        xpath.compile(xpathString);
    }

    /**
     * Remove potential BOM character because XML parser do not handle it properly.
     * @param xmlStringWithBom String with potential BOM character
     * @return xml String without BOM character
     */
    public static String removeBom(String xmlStringWithBom) {
        if (StringUtils.isNotBlank(xmlStringWithBom) && Objects.equals(xmlStringWithBom.charAt(0), ByteOrderMark.UTF_BOM)) {
            return xmlStringWithBom.substring(1);
        }
        return xmlStringWithBom;
    }

    /**
     * Retrieve and return list of XML elements by their tag name, attribute name and attribute value from given
     * Document.
     *
     * @param document XML document from which elements are retrieved
     * @param tagName tag name of elements to retrieve
     * @param attributeName attribute name of elements to retrieve
     * @param attributeValue attribute value of elements to retrieve
     * @return list of matching elements
     */
    public static List<Element> getElementsByTagNameAndAttributeValue(Document document, String tagName,
                                                                      String attributeName, String attributeValue) {
        List<Element> elements = new ArrayList<>();
        NodeList nodes = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element element = (Element) nodes.item(i);
            String attributeString = element.getAttribute(attributeName);
            if (attributeValue.equals(attributeString)) {
                elements.add(element);
            }
        }
        return elements;
    }

    /**
     * Create and return String representation of given XML element 'Element'.
     *
     * @param element XML element for which String representation is created and returned
     * @return String representation of given XML element 'Element'
     * @throws TransformerException when creating String representation of XML element fails
     */
    public static String elementToString(Element element) throws TransformerException {
        StringWriter stringWriter = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(element), new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    /**
     * Create DataRecord from given String 'xmlContent' usings settings from given ImportConfiguration
     * 'importConfigurations'.
     *
     * @param xmlContent String containing XML for which a DataRecord is created
     * @param importConfiguration ImportConfiguration containing settings to be used for creating DataRecord
     * @return DataRecord created for given String 'xmlContent'
     */
    public static DataRecord createRecordFromXMLElement(String xmlContent, ImportConfiguration importConfiguration) {
        DataRecord record = new DataRecord();
        record.setMetadataFormat(
                MetadataFormat.getMetadataFormat(importConfiguration.getMetadataFormat()));
        record.setFileFormat(FileFormat.getFileFormat(importConfiguration.getReturnFormat()));
        record.setOriginalData(xmlContent);
        return record;
    }

    /**
     * Retrieve and return number of EAD elements with given level "eadLevel", e.g. "<c level='file'/>" from given
     * String "xmlString".
     *
     * @param xmlString String containing XML to be parsed for elements
     * @param eadLevel EAD level of elements to be counted
     * @return number of EAD elements with given level
     * @throws XMLStreamException when parsing XML string fails
     */
    public static int getNumberOfEADElements(String xmlString, String eadLevel) throws XMLStreamException {
        int count = 0;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(xmlString));
        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (StringConstants.C_TAG_NAME.equals(reader.getLocalName())
                        && eadLevel.equals(reader.getAttributeValue("", StringConstants.LEVEL))) {
                    count++;
                }
            }
        }
        return count;
    }

}
