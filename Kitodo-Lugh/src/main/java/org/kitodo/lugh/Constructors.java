package org.kitodo.lugh;

import java.util.Collection;

import org.apache.jena.rdf.model.Model;

public interface Constructors {
	/**
	 * Creates a new localised Literal.
	 *
	 * @param value
	 *            literal value
	 * @param languageTag
	 *            locale code
	 */
	public LangString createLangString(String value, String languageTag); 

	/**
	 * Creates a new Literal with a value and a type.
	 *
	 * @param value
	 *            literal value
	 * @param type
	 *            literal type, one of RDF.HTML, RDF.PLAIN_LITERAL, RDF.XML_LITERAL,
	 *            or a literal type defined in XMLSchema.
	 */
	public Literal createLiteral(String value, NodeReference type);

	/**
	 * Creates a literal object from a String. If a language is given, a LangString
	 * will be created, otherwise a plain literal.
	 *
	 * @param value
	 *            the literal value
	 * @param lang
	 *            language, may be {@code ""} but not {@code null}
	 * @return the literal object
	 */
	public Literal createLiteral(String value, String lang);

	/**
	 * Creates a new named node.
	 *
	 * @param identifier
	 *            the name URI of this node
	 */
	public NamedNode createNamedNode(String identifier);

	/**
	 * Creates an empty node.
	 */
	Node createNode();

	/**
	 * Create a node with a type attribute set.
	 *
	 * @param type
	 *            node type
	 */
	Node createNode(NodeReference type);

	/**
	 * Create a node with a type attribute set.
	 *
	 * @param type
	 *            node type
	 */
	public Node createNode(String type);

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
	public ObjectType createNodeReferenceOrLiteral(String value, String lang);

	/**
	 * Create an empty result.
	 */
	Result createResult();

	/**
	 * Create a result with elements.
	 *
	 * @param arg0
	 *            result elements
	 */
	Result createResult(Collection<? extends ObjectType> arg0);

	/**
	 * Create a result for the given amount of elements.
	 *
	 * @param capacity
	 *            elements to be added
	 */
	Result createResult(int capacity);

	/**
	 * Create a result with one element.
	 *
	 * @param element
	 *            result element
	 */
	Result createResult(ObjectType element);

	/**
	 * Creates nodes from a Jena model. Returns all nodes not referenced from
	 * anywhere (the “top nodes”), or all nodes if there aren’t any “top nodes”.
	 *
	 * @param model
	 *            model to read out
	 * @param alwaysAll
	 *            if true, returns all nodes from the model, independent of whether
	 *            there are “top nodes” or not
	 * @return all nodes not referenced from anywhere, or really all nodes
	 */
	Result createResultFrom(Model model, boolean alwaysAll);
}
