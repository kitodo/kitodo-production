/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2013 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 * 
 * Visit the websites for more information.
 *     		- http://www.kitodo.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
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

}
