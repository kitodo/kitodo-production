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

package org.kitodo.dataaccess.storage.memory;

import org.kitodo.dataaccess.IdentifiableNode;
import org.kitodo.dataaccess.LangString;
import org.kitodo.dataaccess.Literal;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.NodeReference;
import org.kitodo.dataaccess.ObjectType;
import org.kitodo.dataaccess.Storage;

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

    @Override
    public LangString createLangString(String value, String languageTag) {
        return new MemoryLangString(value, languageTag);
    }

    @Override
    public ObjectType createLeaf(String value, String lang) {
        return MemoryLiteral.createLeaf(value, lang);
    }

    /**
     *
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

    @Override
    public Literal createLiteral(String value, String type) {
        return new MemoryLiteral(value, (type != null) && !type.isEmpty() ? new MemoryNodeReference(type) : null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Node & IdentifiableNode> T createNamedNode(String identifier) {
        return (T) new MemoryNamedNode(identifier);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Node & IdentifiableNode> T createNamedNode(String identifier, String type) {
        return (T) new MemoryNamedNode(identifier, type);
    }

    @Override
    public Node createNode() {
        return new MemoryNode();
    }

    @Override
    public Node createNode(NodeReference type) {
        return new MemoryNode(type);
    }

    @Override
    public Node createNode(String type) {
        return new MemoryNode(type);
    }

    @Override
    public NodeReference createNodeReference(String url) {
        return new MemoryNodeReference(url);
    }
}
