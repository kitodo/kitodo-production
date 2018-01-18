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

package org.kitodo.dataaccess.format.xml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.kitodo.dataaccess.AccessibleObject;
import org.kitodo.dataaccess.IdentifiableNode;
import org.kitodo.dataaccess.LangString;
import org.kitodo.dataaccess.Literal;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.NodeReference;
import org.kitodo.dataaccess.ObjectType;
import org.kitodo.dataaccess.RDF;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Transforms a linked data structure to an XML file.
 *
 * @author Matthias Ronge
 */
public class XMLWriter {
    /**
     * String to pass to the transformer to switch on indenting.
     */
    private static final String TRANSFORMER_INDENT_TRUE = "yes";

    /**
     * String to pass to the transformer to set the indenting width in
     * characters.
     */
    private static final String TRANSFORMER_INDENT_VALUE = "{http://xml.apache.org/xslt}indent-amount";

    /**
     * Returns the type of the node, or {@code rdf:Description} if not set.
     *
     * @param node
     *            node whose type shall be returned
     * @return the type of the node, or {@code rdf:Description}
     */
    private static String getTypeOrDefault(AccessibleObject node) {
        try {
            return node.getType();
        } catch (NoSuchElementException e) {
            return RDF.DESCRIPTION;
        }
    }

    /**
     * Populates a DOM element with the data from this node.
     *
     * @param node
     *            node to transform
     * @param xml
     *            the dom document, needed to create the children
     * @param abbr
     *            the namespace handler initialized at the root node
     * @param out
     *            element to populate
     * @throws NoSuchElementException
     *             if a relation links to an empty set of objects. (If so, the
     *             object model is corrupt.)
     */
    private static void nodeToElement(Node node, Document xml, Namespaces abbr, Element out) {
        TreeMap<Long, ObjectType> orderedElements = new TreeMap<>();
        HashMap<String, Collection<ObjectType>> bagElements = new HashMap<>();
        long first = Long.MAX_VALUE;
        long last = 0;

        if ((node instanceof Node) && (node instanceof IdentifiableNode)) {
            out.setAttribute(abbr.abbreviateAttribute(getTypeOrDefault(node), RDF.ABOUT),
                ((IdentifiableNode) node).getIdentifier());
        }

        for (Entry<String, Collection<ObjectType>> entry : node.entrySet()) {
            String relation = entry.getKey();
            Collection<ObjectType> objects = entry.getValue();
            ObjectType object = objects.iterator().next();
            Optional<Long> index = RDF.sequenceNumberOf(relation);
            if (index.isPresent()) {
                if (objects.size() <= 1) {
                    orderedElements.put(index.get(), object);
                    if (first > index.get()) {
                        first = index.get();
                    }
                    if (last < index.get()) {
                        last = index.get();
                    }
                } else {
                    throw new SeveralElementsForSameIndexException(
                            "Too many elements with index " + index.get() + ", must not be more than one");
                }
            } else {
                if ((objects.size() <= 1) && (object instanceof Literal) && !(object instanceof LangString)) {
                    out.setAttribute(abbr.abbreviateAttribute(getTypeOrDefault(node), relation),
                        ((Literal) object).getValue());
                } else if ((objects.size() <= 1) && (object instanceof NodeReference)) {
                    if (!RDF.TYPE.getIdentifier().equals(relation)) {
                        out.setAttribute(abbr.abbreviateAttribute(getTypeOrDefault(node), relation),
                            ((NodeReference) object).getIdentifier());
                    }
                } else {
                    bagElements.put(relation, objects);
                }
            }
        }

        if ((first != Node.FIRST_INDEX) && !orderedElements.isEmpty()) {
            throw new IndexOutOfBoundsException(
                    "First element at illegal index " + first + ", must be " + Node.FIRST_INDEX);
        }
        if (orderedElements.size() != last) {
            throw new IndexOutOfBoundsException(orderedElements.size() + " elements found, but " + last + " expected.");
        }

        for (Entry<Long, ObjectType> entry : orderedElements.entrySet()) {
            ObjectType object = entry.getValue();
            if (object instanceof Node) {
                out.appendChild(toElement(object, xml, abbr));
            } else if (object instanceof Literal) {
                out.appendChild(xml.createTextNode(((Literal) object).getValue()));
            } else if (object instanceof NodeReference) {
                out.appendChild(xml.createTextNode(((NodeReference) object).getIdentifier()));
            } else {
                throw new ClassCastException(object.getClass().getName());
            }
        }

        for (Entry<String, Collection<ObjectType>> statement : bagElements.entrySet()) {
            String elementURL = statement.getKey();
            Element predicate = xml.createElement(abbr.abbreviateElement(RDF.PREDICATE.getIdentifier()));
            predicate.setAttribute(abbr.abbreviateAttribute(RDF.PREDICATE.getIdentifier(), RDF.VALUE.getIdentifier()),
                elementURL);
            predicate.setAttribute(
                abbr.abbreviateAttribute(RDF.PREDICATE.getIdentifier(), RDF.PROPERTY.getIdentifier()),
                RDF.BAG.getIdentifier());
            for (ObjectType object : statement.getValue()) {
                predicate.appendChild(toElement(object, xml, abbr));
            }
            out.appendChild(predicate);
        }
    }

    /**
     * Transforms a node to a DOM document.
     *
     * @param node
     *            node to transform
     * @param namespaces
     *            An external Mapping of namespaces to abbreviations. May be
     *            null.
     * @return the DOM document
     */
    private static Document toDocument(Node node, Map<String, String> namespaces) {
        Namespaces abbr = namespaces instanceof Namespaces ? (Namespaces) namespaces : new Namespaces(namespaces);
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            String message = e.getMessage();
            throw new RuntimeException(message != null ? message : e.getClass().getSimpleName(), e);
        }
        String qualifiedName = abbr.abbreviateElement(getTypeOrDefault(node));
        Document document = builder.getDOMImplementation()
                .createDocument(Namespaces.namespaceOfForXMLFile(getTypeOrDefault(node)), qualifiedName, null);
        Element root = document.getDocumentElement();
        if ((node instanceof Node) && (node instanceof IdentifiableNode)) {
            root.setAttribute(abbr.abbreviateAttribute(getTypeOrDefault(node), RDF.ABOUT),
                ((IdentifiableNode) node).getIdentifier());
        }
        nodeToElement(node, document, abbr, root);
        for (Entry<String, String> nsAttr : abbr.namespaceSetForXMLFile()) {
            root.setAttribute(nsAttr.getKey(), nsAttr.getValue());
        }
        return document;
    }

    /**
     * Transforms this node to a DOM element.
     *
     * @param object
     *            object to convert
     *
     * @param document
     *            DOM document to use to create the element
     * @param abbr
     *            a namespace handler initialised with the document root
     * @return this node as a DOM element
     */
    private static Element toElement(ObjectType object, Document document, Namespaces abbr) {
        String type = object instanceof AccessibleObject ? getTypeOrDefault((AccessibleObject) object)
                : XLink.HREF.getIdentifier();
        Element result = document.createElement(abbr.abbreviateElement(type));
        if (object instanceof Node) {
            nodeToElement((Node) object, document, abbr, result);
        } else if (object instanceof Literal) {
            if (object instanceof LangString) {
                result.setAttribute(abbr.abbreviateAttribute(type, XML.LANG.getIdentifier()),
                    ((LangString) object).getLocale().toLanguageTag());
            }
            String value = ((Literal) object).getValue();
            if (Space.requiresPreservation(value)) {
                result.setAttribute(abbr.abbreviateAttribute(type, XML.SPACE.getIdentifier()),
                    Space.PRESERVE.getValue());
            }
            result.setTextContent(value);
        }
        return result;
    }

    /**
     * Outputs this node as an XML document to a file.
     *
     * @param node
     *            node to convert
     *
     * @param file
     *            File to write to.
     * @param indent
     *            number of spaces of each indent level. A negative number
     *            disables wrapping
     * @param namespaces
     *            an external defined mapping of namespaces to abbreviations
     * @throws IOException
     *             if the writing fails
     */
    public static void toFile(Node node, File file, int indent, Map<String, String> namespaces) throws IOException {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            if (indent > -1) {
                transformer.setOutputProperty(OutputKeys.INDENT, TRANSFORMER_INDENT_TRUE);
                transformer.setOutputProperty(TRANSFORMER_INDENT_VALUE, Integer.toString(indent));
            }
            try (OutputStream out = new FileOutputStream(file)) {
                transformer.transform(new DOMSource(toDocument(node, namespaces)), new StreamResult(out));
                out.flush();
            }
        } catch (TransformerException e) {
            String message = e.getMessage();
            throw new RuntimeException(message != null ? message : e.getClass().getSimpleName(), e);
        }
    }

    /**
     * Outputs this node as an XML document to a String list.
     *
     * @param node
     *            node to convert
     *
     * @param indent
     *            number of spaces of each indent level. A negative number
     *            disables wrapping
     * @param namespaces
     *            an external defined mapping of namespaces to abbreviations
     * @param charset
     *            The charset name to be used in the XML declaration. The
     *            charset is also actively used in the transformation process,
     *            thus unmappable characters will get lost. Defaults to UTF-8.
     * @return a String list holding this node as XML
     */
    public static List<String> toLines(Node node, int indent, Map<String, String> namespaces,
            Optional<String> charset) {
        return Arrays.asList(toString(node, indent, namespaces, charset).split("\r?\n"));
    }

    /**
     * Outputs this node as an XML document to a String.
     *
     * @param node
     *            node to convert
     *
     * @param indent
     *            number of spaces of each indent level. A negative number
     *            disables wrapping
     * @param namespaces
     *            an external defined mapping of namespaces to abbreviations
     * @param optionalCharset
     *            The charset name to be used in the XML declaration. The
     *            charset is also actively used in the transformation process,
     *            thus unmappable characters will get lost. Defaults to UTF-8.
     * @return a String list holding this node as XML
     */
    public static String toString(Node node, int indent, Map<String, String> namespaces,
            Optional<String> optionalCharset) {
        String charset = optionalCharset.orElse(StandardCharsets.UTF_8.toString());

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            if (indent > -1) {
                transformer.setOutputProperty(OutputKeys.INDENT, TRANSFORMER_INDENT_TRUE);
                transformer.setOutputProperty(TRANSFORMER_INDENT_VALUE, Integer.toString(indent));
            }
            transformer.setOutputProperty(OutputKeys.ENCODING, charset);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            transformer.transform(new DOMSource(toDocument(node, namespaces)), new StreamResult(buffer));
            return new String(buffer.toByteArray(), charset);
        } catch (TransformerException | UnsupportedEncodingException e) {
            String message = e.getMessage();
            throw new IllegalArgumentException(message != null ? message : e.getClass().getSimpleName(), e);
        }
    }
}
