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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.NodeReference;
import org.kitodo.dataaccess.ObjectType;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.Storage;
import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Reads an XML document into a linked data tree structure.
 */
public class XMLReader {

    /**
     * The types that will be converted to a literal and not a node.
     */
    private static final Map<String, NodeReference> LITERAL_TYPES = new HashMap<String, NodeReference>(4) {
        private static final long serialVersionUID = 1L;
        {
            put(RDF.PLAIN_LITERAL.getIdentifier(), RDF.PLAIN_LITERAL);
            put(RDF.LANG_STRING.getIdentifier(), RDF.LANG_STRING);
            put(RDF.XML_LITERAL.getIdentifier(), RDF.XML_LITERAL);
            put(RDF.HTML.getIdentifier(), RDF.HTML);
        }
    };

    /**
     * Private constructor to hide the implicit public one.
     */
    private XMLReader() {

    }

    /**
     * Returns an attribute from an element by its URL. If the attribute is of
     * the same namespace as the element, the attribute is allowed to miss the
     * prefix. If the element is local, the external document namespace must be
     * taken into consideration
     *
     * @param element
     *            element to retrieve the attribute from
     * @param attributeURL
     *            URL of the attribute to retrieve
     * @param documentNS
     *            the document namespace
     * @return the attribute value, or "" if absent
     */
    private static String getAttributeFromElement(Element element, String attributeURL, String documentNS) {
        String attributeNS = Namespaces.namespaceOfForXMLFile(attributeURL);
        String attributeName = Namespaces.localNameOf(attributeURL);
        String result = element.getAttributeNS(attributeNS, attributeName);
        if (result.isEmpty()) {
            String elementNS = element.getNamespaceURI();
            if (elementNS == null) {
                elementNS = documentNS;
            }
            if (attributeNS.equals(elementNS)) {
                result = element.getAttribute(attributeName);
            }
        }
        return result;
    }

    /**
     * Returns the type of the relation element or "" if this is not the case.
     *
     * @param element
     *            elemnt to examine
     * @param documentNS
     *            the document namespace
     * @return the type of the relation element, or ""
     */
    private static String getRelationElementType(Element element, String documentNS) {
        if (!RDF.PREDICATE.getIdentifier().equals(getType(element, documentNS).orElse(null))) {
            return "";
        }
        return getAttributeFromElement(element, RDF.VALUE.getIdentifier(), documentNS);
    }

    /**
     * Returns the type URL of an attribute.
     *
     * @param attribute
     *            attribute whose type is to return
     * @param element
     *            element the attribute is attached to
     * @param documentNS
     *            document namespace, as fallback
     * @return the type URL
     */
    private static String getType(Attr attribute, Element element, String documentNS) {
        String ns = attribute.getNamespaceURI();
        if (ns == null) {
            ns = element.getNamespaceURI();
        }
        String name = attribute.getLocalName();
        if (ns == null) {
            ns = documentNS;
        }
        return ns == null ? name : ns.endsWith("/") ? ns.concat(name) : ns + '#' + name;
    }

    /**
     * Returns the type URL of an element.
     *
     * @param element
     *            element whose type is to return
     * @param documentNS
     *            the address of the document, as fall-back namespace for local
     *            XML elements and attributes
     * @return the type URL
     */
    private static Optional<String> getType(Element element, String documentNS) {
        String ns = element.getNamespaceURI();
        if (ns == null) {
            ns = documentNS;
        }
        String name = element.getLocalName();
        if (ns == null) {
            return Optional.of(name);
        } else {
            String type = ns.endsWith("/") ? ns.concat(name) : ns + '#' + name;
            return Optional.ofNullable(type.equals(RDF.DESCRIPTION) ? null : type);
        }
    }

    /**
     * Parse an element to a literal.
     *
     * @param element
     *            element to parse
     * @param type
     *            the type of literal to create, one of RDF.PLAIN_LITERAL,
     *            RDF.LANG_STRING, RDF.XML_LITERAL, or RDF.HTML.
     * @param lang
     *            language set on some parent node, if a RDF.LANG_STRING is to
     *            be created but there is no local language tag
     * @param space
     *            whether or not to trim white space around the element
     * @param storage
     *            storage to read the XML into
     * @return the literal node
     */
    private static ObjectType parseLiteralElement(Element element, NodeReference type, String lang, Space space,
            Storage storage) {

        String value = space.trim(element.getTextContent());
        if (!type.equals(RDF.LANG_STRING)) {
            return storage.createLiteral(value, type);
        } else {
            return storage.createLangString(value, lang);
        }
    }

    /**
     * Parse an element to a Node. The element must represent a node in the
     * linked data graph. This is true for the root (which must be the subject
     * of its assertions and thus a node) or any element proven not to be a bag
     * element (which then is the object of a rdf:_n relation with its XML
     * parent node as subject of the relation).
     *
     * @param element
     *            element to parse
     * @param lang
     *            inherited xml:lang attribute value
     * @param space
     *            whether to preserve white space (defaults to false)
     * @param documentNS
     *            the address of the document, as fall-back namespace for local
     *            XML elements and attributes
     * @param root
     *            whether this is the XML’s root (top) element
     * @param storage
     *            storage to read the XML into
     * @return the populated node
     */
    private static Node parseNodeElement(Element element, String lang, Space space, String documentNS, boolean root,
            Storage storage) {
        String name = element.getAttributeNS(Namespaces.namespaceOfForXMLFile(RDF.ABOUT),
            Namespaces.localNameOf(RDF.ABOUT));
        if (root && name.isEmpty()) {
            name = documentNS;
        }
        Node result = (name == null) || name.isEmpty() ? storage.createNode() : storage.createNamedNode(name);
        Optional<String> nodeType = getType(element, documentNS);
        nodeType.ifPresent((value) -> {
            if (!value.isEmpty()) {
                result.put(RDF.TYPE, value);
            }
        });

        /*
         * Processing the attributes: For each attribute create a literal.
         */

        for (Attr attribute : new AttrIterable(element.getAttributes())) {
            String type = getType(attribute, element, documentNS);
            if (type.startsWith(Namespaces.XMLNS_NAMESPACE)) {
                continue;
            }
            if (type.equals(XML.LANG.getIdentifier())) {
                lang = attribute.getValue();
            }
            if (type.equals(XML.SPACE.getIdentifier())) {
                space = Space.resolve(attribute.getValue().toLowerCase());
            }
            result.replace(type, wrapElementInSet(storage.createLeaf(attribute.getValue(), lang)));
        }

        /*
         * Processing the children.
         */
        long count = 0;
        for (org.w3c.dom.Node child : new NodeIterable(element.getFirstChild())) {

            // Either the child is an XML element
            if (child instanceof Element) {

                String relation = getRelationElementType((Element) child, documentNS);
                String literalType = getType((Element) child, documentNS).orElse(null);

                if (!relation.isEmpty()) {
                    // The child may be expressing a relation, that, due to its
                    // complexity, cannot be expressed as attribute
                    result.replace(relation, parseRelationElement((Element) child, lang, space, documentNS, storage));
                } else if (LITERAL_TYPES.containsKey(literalType)) {
                    // or a literal type
                    result.replace(RDF.toURL(++count), wrapElementInSet(
                        parseLiteralElement((Element) child, LITERAL_TYPES.get(literalType), lang, space, storage)));
                } else {
                    // or the child is an ordered element (represents a node,
                    // linked by a numeric relation representing its element
                    // index)
                    result.replace(RDF.toURL(++count),
                        wrapElementInSet(parseNodeElement((Element) child, lang, space, documentNS, false, storage)));
                }
            } else if (child instanceof Text) {
                // or the child is an XML literal
                String value = space.trim(child.getTextContent());
                if (!value.isEmpty()) {
                    result.replace(RDF.toURL(++count), wrapElementInSet(storage.createLeaf(value, lang)));
                }
            } else if (child instanceof Comment) {
                // Ignore XML comments
                continue;
            } else {
                throw new ClassCastException(
                        "Found unknown subclass of org.w3c.dom.Node: " + child.getClass().getName());
            }
        }
        return result;
    }

    /**
     * Parse an element to a set of Nodes. The element must represent a relation
     * in the linked data graph. This is true for any element proven to be a bag
     * element.
     *
     * @param element
     *            element to parse
     * @param lang
     *            inherited xml:lang attribute value
     * @param space
     *            whether to preserve white space
     * @param documentNS
     *            the address of the document, as fall-back namespace for local
     *            XML elements and attributes
     * @param storage
     *            storage to read the XML into
     * @return the populated node
     */
    private static Set<ObjectType> parseRelationElement(Element element, String lang, Space space, String documentNS,
            Storage storage) {
        Set<ObjectType> result = new HashSet<>();

        /*
         * Processing the attributes: The XML element represents a predicate and
         * thus should not have any attributes to be converted.
         */

        for (Attr attribute : new AttrIterable(element.getAttributes())) {
            String url = getType(attribute, element, documentNS);
            if ((url.equals(RDF.PROPERTY.getIdentifier()) && attribute.getValue().equals(RDF.BAG.getIdentifier()))
                    || url.equals(RDF.VALUE.getIdentifier()) || url.startsWith(Namespaces.XMLNS_NAMESPACE)) {
                continue;
            }
            if (url.equals(XML.LANG.getIdentifier())) {
                lang = attribute.getValue();
                continue;
            }
            if (url.equals(XML.SPACE.getIdentifier())) {
                space = Space.resolve(attribute.getValue().toLowerCase());
                continue;
            }
            throw new IllegalStateException(
                    "Cannot add relation " + url + " to relation " + getType(element, documentNS) + ".");
        }

        /*
         * Processing the children: Each child of a predicate element is a node
         * element.
         */

        for (org.w3c.dom.Node child : new NodeIterable(element.getFirstChild())) {

            // Either the child is an XML element
            if (child instanceof Element) {
                Element childElement = (Element) child;
                String literalType = getType((Element) child, documentNS).orElse(null);

                // Either the child is a literal
                if ((childElement.getAttributes().getLength() == 0) && LITERAL_TYPES.containsKey(literalType)) {
                    String value = space.trim(childElement.getTextContent());
                    if (RDF.LANG_STRING.getIdentifier().equals(literalType)) {
                        String ownLang = childElement.getAttributeNS(
                            Namespaces.namespaceOfForXMLFile(XML.LANG.getIdentifier()),
                            Namespaces.localNameOf(XML.LANG.getIdentifier()));
                        result.add(storage.createLangString(value, !ownLang.isEmpty() ? ownLang : lang));
                    } else {
                        result.add(storage.createLiteral(value, literalType));
                    }
                    child = child.getNextSibling();
                    continue;
                }

                // or the child is a node. In this case, check if the attributes
                // allow creation of a literalType
                String literalLang = lang;
                Space literalSpace = space;
                boolean canCreateLiteralType = true;

                for (Attr attribute : new AttrIterable(element.getAttributes())) {
                    String type = getType(attribute, element, documentNS);
                    if (type.startsWith(Namespaces.XMLNS_NAMESPACE)) {
                        continue;
                    } else if (type.equals(XML.LANG.getIdentifier())) {
                        literalLang = attribute.getValue();
                    } else if (type.equals(XML.SPACE.getIdentifier())) {
                        literalSpace = Space.resolve(attribute.getValue().toLowerCase());
                    } else {
                        canCreateLiteralType = false;
                        break;
                    }
                }

                // It may then be a literal which is too complex to store as
                // text content
                if (canCreateLiteralType && LITERAL_TYPES.containsKey(literalType)) {
                    result.add(parseLiteralElement((Element) child, LITERAL_TYPES.get(literalType), literalLang,
                        literalSpace, storage));

                } else {
                    // or a real node
                    result.add(parseNodeElement((Element) child, lang, space, documentNS, false, storage));
                }
            } else if (child instanceof Text) {
                // or the child is an XML literal
                String value = space.trim(child.getTextContent());
                if (value.length() > 0) {
                    result.add(storage.createLeaf(value, lang));
                }
            } else if (child instanceof Comment) {
                // Ignore XML comments
                continue;
            } else {
                throw new ClassCastException(
                        "Found unknown subclass of org.w3c.dom.Node: " + child.getClass().getName());
            }
        }
        return result;
    }

    /**
     * Static method to get the elements from an XML stream. This is used in the
     * constructors from file. The stream will be closed before the method
     * exits.
     *
     * @param input
     *            input to parse. Will be closed afterwards.
     * @param encoding
     *            character encoding to use
     * @return the root element
     * @throws SAXException
     *             if the XML is not well-formed
     * @throws IOException
     *             if a read operation fails
     */
    private static final Element parseXML(InputStream input, Optional<String> encoding)
            throws SAXException, IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            InputSource source = new InputSource(input);
            encoding.ifPresent(value -> source.setEncoding(value));
            return factory.newDocumentBuilder().parse(source).getDocumentElement();
        } catch (ParserConfigurationException e) {
            // Java can be assumed to support the parser’s default configuration
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Parses an XML DOM tree to a linked data tree.
     *
     * @param root
     *            root node of the XML DOM tree
     * @param documentNS
     *            the address of the document, as fall-back namespace for local
     *            XML elements and attributes
     * @param storage
     *            storage to read the XML into
     * @return the root node of a linked data tree
     */
    private static Node toNode(Element root, String documentNS, Storage storage) {
        if (documentNS.endsWith("#")) {
            documentNS = documentNS.substring(0, documentNS.length() - 1);
        }
        return parseNodeElement(root, "", Space.DEFAULT, documentNS, true, storage);
    }

    /**
     * Creates a Node from a file.
     *
     * @param path
     *            file to read
     * @param storage
     *            storage implementation to read the XML file into
     * @return the linked data node
     * @throws SAXException
     *             if the file is not wellformed
     * @throws IOException
     *             if the reading fails
     */
    public static Node toNode(File path, Storage storage) throws SAXException, IOException {
        String namespace = Namespaces.namespaceFromURI(path.getCanonicalFile().toURI());
        try (FileInputStream inputStream = new FileInputStream(path)) {
            return toNode(parseXML(inputStream, Optional.empty()), namespace, storage);
        }
    }

    /**
     * Creates a Node from an arbitrary input resource.
     *
     * @param bytes
     *            input data
     * @param encoding
     *            character encoding to use
     * @param documentNS
     *            the address of the document, as fall-back namespace for local
     *            XML elements and attributes
     * @param storage
     *            storage implementation to read the XML file into
     * @return the linked data node
     * @throws SAXException
     *             if the file is not well-formed
     * @throws IOException
     *             if the reading fails
     */
    public static Node toNode(InputStream bytes, Optional<String> encoding, String documentNS, Storage storage)
            throws SAXException, IOException {
        return toNode(parseXML(bytes, encoding), documentNS, storage);
    }

    /**
     * Create a Node from an XML String.
     *
     * @param data
     *            String to parse
     * @param documentNS
     *            the address of the document, as fall-back namespace for local
     *            XML elements and attributes
     * @param storage
     *            storage implementation to read the XML file into
     * @return the created Node
     * @throws SAXException
     *             if the XML is semantically wrong
     */
    public static Node toNode(String data, String documentNS, Storage storage) throws SAXException {
        try (ByteArrayInputStream bytes = new ByteArrayInputStream(data.getBytes("UTF-16"))) {
            return toNode(bytes, Optional.of("UTF-16"), documentNS, storage);
        } catch (IOException e) {
            // there is no IOException to expect when reading from a String
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Wraps an element in a set.
     *
     * @param element
     *            element to wrap
     * @return a HashSet containing the element
     */
    private static Set<ObjectType> wrapElementInSet(ObjectType element) {
        return new HashSet<>(Arrays.asList(new ObjectType[] {element }));
    }
}
