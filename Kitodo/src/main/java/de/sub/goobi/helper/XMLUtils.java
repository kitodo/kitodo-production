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
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * The class XMLUtils contains an omnium-gatherum of functions that work on XML.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class XMLUtils {

    /**
     * Private constructor to hide the implicit public one.
     */
    private XMLUtils() {

    }

    /**
     * The method documentToByteArray() converts an org.w3c.dom.Document to a
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
        if (indent != null) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", indent.toString());
        }
        transformer.transform(new DOMSource(data), new StreamResult(result));
        return result.toByteArray();
    }

    /**
     * The function getFirstChildWithTagName() returns the first child node from
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
        for (Node element = data.getFirstChild(); element != null; element = element.getNextSibling()) {
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
     * @throws IOException
     *             if a DocumentBuilder cannot be created which satisfies the
     *             configuration requested—which never happens because we use
     *             the default configuration here and that is definitely
     *             supported, but in case - wrap it in IOException
     */
    public static Document load(InputStream data) throws SAXException, IOException {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(data);
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * The function newDocument() is a convenience method to obtain a new
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
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

}
