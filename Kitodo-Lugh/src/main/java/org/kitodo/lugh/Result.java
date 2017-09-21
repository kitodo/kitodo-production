/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General private License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.lugh;

import java.nio.BufferOverflowException;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * The results returned by a call to a getter on a node.
 *
 * @author Matthias Ronge
 */
public interface Result extends Set<ObjectType> {

    /**
     * Add an element to the result.
     */
    @Override
    public boolean add(ObjectType e);

    /**
     * Empty the result.
     */
    @Override
    public void clear();

    /**
     * Returns the identifiable node. This should only be called if its presence
     * was previously checked by {@link #isUniqueIdentifiableNode()}.
     *
     * @return the accessible node
     * @throws NoSuchElementException
     *             if there is no named node
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    public IdentifiableNode identifiableNodeExpectable();

    /**
     * Returns all identifiable nodes.
     *
     * @return all identifiable nodes
     */
    public Set<IdentifiableNode> identifiableNodes();


    /**
     * True if there is exactly one node in the result. If this is true,
     * {@link #identifiableNodeExpectable()} can be called.
     *
     * @return whether there is exactly one node
     */
    public boolean isUniqueIdentifiableNode();

    /**
     * Returns the accessible node.
     *
     * @return the accessible node
     * @throws LinkedDataException
     *             {@link NoDataException} if there is no accessible node,
     *             {@link AmbiguousDataException} if there are several possible
     *             answers
     */
    public Node node() throws LinkedDataException;

    /**
     * Returns all accessible nodes.
     *
     * @return all accessible nodes
     */
    public Set<Node> nodes();

    /**
     * Delete a node from the result.
     */
    @Override
    public boolean remove(Object o);

    /**
     * Returns all the literals as strings. References to other nodes are not
     * returned.
     *
     * @return the literal
     */
    public Set<String> strings();

    /**
     * Returns all the literals as strings.
     *
     * @param allLeaves
     *            if true, references to other nodes are returned as well
     *
     * @return the literal
     */
    public Set<String> strings(boolean allLeaves);

    /**
     * Returns all literals from this node, joined by the given separator.
     *
     * @param separator
     *            separator to use
     * @param allLeaves
     *            return all leaves
     * @return all literals from this node as String
     */
    public String strings(String separator, boolean allLeaves);
}
