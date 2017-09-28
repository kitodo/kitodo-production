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

import java.util.Collection;

import org.apache.jena.rdf.model.Model;

/**
 * Factory interface to create data objects in a given storage implementation.
 *
 * @author Matthias Ronge
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
     * @param identifier
     *            the name URI of this node
     * @return the created named node
     */
    NamedNode createNamedNode(String identifier);

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
    ObjectType createObjectType(String value, String lang);

    /**
     * Create an empty result.
     *
     * @return the created result
     */
    Result createResult();

    /**
     * Create a result with elements.
     *
     * @param arg0
     *            result elements
     * @return the created result
     */
    Result createResult(Collection<? extends ObjectType> arg0);

    /**
     * Create a result for the given amount of elements.
     *
     * @param capacity
     *            elements to be added
     * @return the created result
     */
    Result createResult(int capacity);

    /**
     * Creates nodes from a Jena model. Returns all nodes not referenced from
     * anywhere (the “top nodes”), or all nodes if there aren’t any “top nodes”.
     *
     * @param model
     *            model to read out
     * @param alwaysAll
     *            if true, returns all nodes from the model, independent of
     *            whether there are “top nodes” or not
     * @return all nodes not referenced from anywhere, or really all nodes
     */
    Result createResult(Model model, boolean alwaysAll);

    /**
     * Create a result with one element.
     *
     * @param element
     *            result element
     * @return the created result
     */
    Result createResult(ObjectType element);
}
