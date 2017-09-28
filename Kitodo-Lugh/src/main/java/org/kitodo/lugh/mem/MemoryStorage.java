package org.kitodo.lugh.mem;

import java.util.Collection;

import org.apache.jena.rdf.model.Model;
import org.kitodo.lugh.*;

public class MemoryStorage implements Storage {

    public static final MemoryStorage INSTANCE = new MemoryStorage();

    /**
     * Private constructor. You cannot create instances from this, because they
     * don’t have a state. Use {@link INSTANCE} instead.
     */
    private MemoryStorage() {
    }

    @Override
    public Result createResultFrom(Model model, boolean alwaysAll) {
        return MemoryResult.createFrom(model, alwaysAll);
    }

    @Override
    public LangString newLangString(String value, String languageTag) {
        return new MemoryLangString(value, languageTag);
    }

    @Override
    public Literal newLiteral(String value, NodeReference type) {
        return new MemoryLiteral(value, type);
    }

    @Override
    public Literal newLiteral(String value, String type) {
        return new MemoryLiteral(value, type);
    }

    @Override
    public NamedNode newNamedNode(String identifier) {
        return new MemoryNamedNode(identifier);
    }

    @Override
    public Node newNode() {
        return new MemoryNode();
    }

    @Override
    public Node newNode(NodeReference type) {
        return new MemoryNode(type);
    }

    @Override
    public Node newNode(String type) {
        return new MemoryNode(type);
    }

    @Override
    public NodeReference newNodeReference(String url) {
        return new MemoryNodeReference(url);
    }

    @Override
    public ObjectType newObjectType(String value, String lang) {
        return MemoryLiteral.create(value, lang);
    }

    @Override
    public Result newResult() {
        return new MemoryResult();
    }

    @Override
    public Result newResult(Collection<? extends ObjectType> arg0) {
        return new MemoryResult(arg0);
    }

    @Override
    public Result newResult(int capacity) {
        return new MemoryResult(capacity);
    }

    @Override
    public Result newResult(ObjectType element) {
        return new MemoryResult(element);
    }

}
