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

package org.kitodo.lugh;

/**
 * Factory interface to create data objects in a given storage implementation.
 */
public interface Storage {
    /**
     * Creates a new language-tagged string.
     *
     * @param value
     *            literal value
     * @param languageTag
     *            locale code
     * @return the created language-tagged string
     */
    LangString createLangString(String value, String languageTag);

    /**
     * Creates a literal object from a String. If the literal starts with
     * {@code http://}, a node reference is created, otherwise if a language is
     * given, a LangString will be created, otherwise a plain literal.
     *
     * @param value
     *            the literal value
     * @param lang
     *            language, may be {@code ""} but not {@code null}
     * @return the literal object
     */
    ObjectType createLeaf(String value, String lang);

    /**
     * Creates a new Literal with a value and a type.
     *
     * @param value
     *            literal value
     * @param type
     *            literal type, one of RDF.HTML, RDF.PLAIN_LITERAL,
     *            RDF.XML_LITERAL, or a literal type defined in XMLSchema.
     * @return the created literal
     */
    default Literal createLiteral(String value, IdentifiableNode type) {
        return createLiteral(value, type.getIdentifier());
    }

    /**
     * Creates a new Literal with a value and a type.
     *
     * @param value
     *            literal value
     * @param type
     *            literal type, one of RDF.HTML, RDF.PLAIN_LITERAL,
     *            RDF.XML_LITERAL, or a literal type defined in XMLSchema.
     * @return the created literal
     */
    Literal createLiteral(String value, String type);

    /**
     * Creates a new named node.
     *
     * @param <NamedNode>
     *            The concept of a named node
     *
     * @param identifier
     *            the name URI of this node
     * @return the created named node
     */
    <NamedNode extends Node & IdentifiableNode> NamedNode createNamedNode(String identifier);

    /**
     * Creates a new named node with a type.
     *
     * @param <NamedNode>
     *            The concept of a named node
     *
     * @param identifier
     *            the name URI of this node
     * @param type
     *            node type
     * @return the created named node
     */
    default <NamedNode extends Node & IdentifiableNode> NamedNode createNamedNode(String identifier,
            NodeReference type) {
        return createNamedNode(identifier, type.getIdentifier());
    }

    /**
     * Creates a new named node with a type.
     *
     * @param <NamedNode>
     *            The concept of a named node
     *
     * @param identifier
     *            the name URI of this node
     * @param type
     *            node type
     * @return the created named node
     */
    <NamedNode extends Node & IdentifiableNode> NamedNode createNamedNode(String identifier, String type);

    /**
     * Creates an empty node.
     *
     * @return the created node
     */
    Node createNode();

    /**
     * Create a node with a type attribute set.
     *
     * @param type
     *            node type
     * @return the created node
     */
    default Node createNode(NodeReference type) {
        return createNode(type.getIdentifier());
    }

    /**
     * Create a node with a type attribute set.
     *
     * @param type
     *            node type
     * @return the created node
     */
    Node createNode(String type);

    /**
     * Creates a node reference.
     *
     * @param url
     *            URI to reference
     * @return the created node reference
     */
    NodeReference createNodeReference(String url);
}
