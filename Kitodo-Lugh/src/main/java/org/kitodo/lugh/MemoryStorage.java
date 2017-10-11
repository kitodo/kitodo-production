package org.kitodo.lugh;

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

}
