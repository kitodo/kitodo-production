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

    /** {@inheritDoc} */
    @Override
    public LangString createLangString(String value, String languageTag) {
        return new MemoryLangString(value, languageTag);
    }

    /**
     * {@inheritDoc} This implementation overrides the default implementation,
     * because if the type is a MemoryNodeReference, it can be reused.
     */
    @Override
    public Literal createLiteral(String value, IdentifiableNode type) {
        if (type instanceof MemoryNodeReference) {
            return new MemoryLiteral(value, (MemoryNodeReference) type);
        } else {
            return createLiteral(value, type != null ? type.getIdentifier() : null);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Literal createLiteral(String value, String type) {
        return new MemoryLiteral(value, (type != null) && !type.isEmpty() ? new MemoryNodeReference(type) : null);
    }

    /** {@inheritDoc} */
    @Override
    public ObjectType createLiteralType(String value, String lang) {
        return MemoryLiteral.create(value, lang);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public <NamedNode extends Node & IdentifiableNode> NamedNode createNamedNode(String identifier) {
        return (NamedNode) new MemoryNamedNode(identifier);
    }

    /** {@inheritDoc} */
    @Override
    public Node createNode() {
        return new MemoryNode();
    }

    /** {@inheritDoc} */
    @Override
    public Node createNode(NodeReference type) {
        return new MemoryNode(type);
    }

    /** {@inheritDoc} */
    @Override
    public Node createNode(String type) {
        return new MemoryNode(type);
    }

    /** {@inheritDoc} */
    @Override
    public NodeReference createNodeReference(String url) {
        return new MemoryNodeReference(url);
    }

    /** {@inheritDoc} */
    @Override
    public Result createResult() {
        return new MemoryResult();
    }

    /** {@inheritDoc} */
    @Override
    public Result createResult(Collection<? extends ObjectType> arg0) {
        return new MemoryResult(arg0);
    }

    /** {@inheritDoc} */
    @Override
    public Result createResult(int capacity) {
        return new MemoryResult(capacity);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public Result createResult(ObjectType element) {
        return new MemoryResult(element);
    }

}
