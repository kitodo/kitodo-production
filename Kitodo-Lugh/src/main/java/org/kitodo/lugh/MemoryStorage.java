package org.kitodo.lugh;

import java.util.*;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.*;
import org.kitodo.lugh.vocabulary.*;

/**
 * MemoryStorage is a storage implementation that is held in memory.
 */
public class MemoryStorage implements Storage {

    /**
     * The singleton instance of this factory.
     */
    public static final MemoryStorage INSTANCE = new MemoryStorage();

    /**
     * Private constructor. You cannot create instances from this, because they
     * don’t have a state. Use {@link INSTANCE} instead.
     */
    private MemoryStorage() {
    }

    /**
     * Creates a new language-tagged string.
     *
     * @param value
     *            literal value
     * @param languageTag
     *            locale code
     * @return the created language-tagged string
     */
    @Override
    public LangString createLangString(String value, String languageTag) {
        return new MemoryLangString(value, languageTag);
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
    @Override
    public Literal createLiteral(String value, IdentifiableNode type) {
        if (type instanceof MemoryNodeReference) {
            return new MemoryLiteral(value, (MemoryNodeReference) type);
        } else {
            return createLiteral(value, type != null ? type.getIdentifier() : null);
        }
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
    @Override
    public Literal createLiteral(String value, String type) {
        return new MemoryLiteral(value, (type != null) && !type.isEmpty() ? new MemoryNodeReference(type) : null);
    }

    /**
     * Creates a new named node.
     *
     * @param identifier
     *            the name URI of this node
     * @return the created named node
     */
    @Override
    public NamedNode createNamedNode(String identifier) {
        return new MemoryNamedNode(identifier);
    }

    /**
     * Creates an empty node.
     *
     * @return the created node
     */
    @Override
    public Node createNode() {
        return new MemoryNode();
    }

    /**
     * Create a node with a type attribute set.
     *
     * @param type
     *            node type
     * @return the created node
     */
    @Override
    public Node createNode(NodeReference type) {
        return new MemoryNode(type);
    }

    /**
     * Create a node with a type attribute set.
     *
     * @param type
     *            node type
     * @return the created node
     */
    @Override
    public Node createNode(String type) {
        return new MemoryNode(type);
    }

    /**
     * Creates a node reference.
     *
     * @param url
     *            URI to reference
     * @return the created node reference
     */
    @Override
    public NodeReference createNodeReference(String url) {
        return new MemoryNodeReference(url);
    }

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
    @Override
    public ObjectType createObjectType(String value, String lang) {
        return MemoryLiteral.create(value, lang);
    }

    /**
     * Create an empty result.
     *
     * @return the created result
     */
    @Override
    public Result createResult() {
        return new MemoryResult();
    }

    /**
     * Create a result with elements.
     *
     * @param arg0
     *            result elements
     * @return the created result
     */
    @Override
    public Result createResult(Collection<? extends ObjectType> arg0) {
        return new MemoryResult(arg0);
    }

    /**
     * Create a result for the given amount of elements.
     *
     * @param capacity
     *            elements to be added
     * @return the created result
     */
    @Override
    public Result createResult(int capacity) {
        return new MemoryResult(capacity);
    }

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
    @Override
    public Result createResult(Model model, boolean alwaysAll) {
        HashMap<String, Node> result = new HashMap<>();
        HashMap<String, MemoryNode> resolver = new HashMap<>();

        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement statement = iter.nextStatement();

            Resource subject = statement.getSubject();
            String subjectIdentifier = subject.toString();
            Property predicate = statement.getPredicate();
            RDFNode object = statement.getObject();

            if (!resolver.containsKey(subjectIdentifier)) {
                MemoryNode newNode = subject.asNode().isBlank() ? new MemoryNode()
                        : new MemoryNamedNode(subjectIdentifier);
                resolver.put(subjectIdentifier, newNode);
                result.put(subjectIdentifier, newNode);
            }

            Node subjectNode = resolver.get(subjectIdentifier);
            ObjectType objectNode;
            if (object.isResource()) {
                String objectIdentifier = object.toString();
                if (!resolver.containsKey(objectIdentifier)) {
                    MemoryNode newNode = object.asNode().isBlank() ? new MemoryNode()
                            : new MemoryNamedNode(objectIdentifier);
                    resolver.put(objectIdentifier, newNode);
                    result.put(objectIdentifier, newNode);
                }
                objectNode = resolver.get(objectIdentifier);
                result.remove(objectIdentifier);
            } else {
                org.apache.jena.rdf.model.Literal literalObject = object.asLiteral();
                String datatypeURI;
                if (literalObject.isWellFormedXML()) {
                    objectNode = new MemoryLiteral(literalObject.toString(), RDF.XML_LITERAL);
                } else if (!literalObject.getLanguage().isEmpty()) {
                    objectNode = new MemoryLangString(literalObject.getValue().toString(), literalObject.getLanguage());
                } else if ((literalObject.getDatatype() == null)
                        || XMLSchema.STRING.getIdentifier().equals(datatypeURI = literalObject.getDatatypeURI())) {
                    objectNode = new MemoryLiteral(literalObject.getValue().toString(), RDF.PLAIN_LITERAL);
                } else {
                    objectNode = new MemoryLiteral(literalObject.getValue().toString(), datatypeURI);
                }
            }
            subjectNode.put(predicate.toString(), objectNode);
        }

        for (Entry<String, MemoryNode> entries : resolver.entrySet()) {
            entries.getValue().replaceAllNamedNodesWithNoDataByNodeReferences(false);
        }

        return new MemoryResult(alwaysAll || result.isEmpty() ? resolver.values() : result.values());
    }

    /**
     * Create a result with one element.
     *
     * @param element
     *            result element
     * @return the created result
     */
    @Override
    public Result createResult(ObjectType element) {
        return new MemoryResult(element);
    }

}
