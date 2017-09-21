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
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

/**
 * An anonymous linked data Node. The most nodes are anonymous. In Java, they
 * are identified by the variable that holds them. In the data, they are often
 * identified by a certain value held by a certain relation. Therefore the short
 * class name “Node” was used for the anonymous node class, and not the name
 * “AnonymousNode”.
 * <p>
 * The class offers functions known from Java’s collections, and they are
 * intended to intuitively behave the same way.
 *
 * @author Matthias Ronge
 */
public interface Node extends AccessibleObject, NodeType {

    /**
     * The index of the first child node that is referenced by index.
     */
    public static final short FIRST_INDEX = 1;

    /**
     * Adds a node to the nodes referenced by index. The add function implements
     * the traditional behaviour that the node is added with an index that is
     * one higher that the highest index in use before.
     *
     * @param element
     *            Element to add
     * @return this node, for in-line use
     */
    public Node add(ObjectType element);

    /**
     * Compares two nodes for equality.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj);

    /**
     * Resolves the given graph path against this node. The node passed in must
     * be a graph path.
     *
     * @param graphPath
     *            graph path to resolve
     * @return the results from resolving
     */
    public Result find(Node graphPath);

    /**
     * Gets a literal referenced by relation.
     *
     * @param relation
     *            relation to look up
     * @return the value of the literal.
     */
    public Result get(NodeReference relation);

    /**
     * Returns all nodes referenced by the given relations which have all the
     * data from the conditions nodes. {@code reference} may be empty meaning
     * <em>any relation</em>. {@code condition} may be empty meaning <em>any
     * non-empty result</em>.
     *
     * @param relations
     *            referencing relation
     * @param conditions
     *            a subset of data to be present on found nodes
     * @return all nodes referenced by the given relations which fulfil the
     *         conditions
     */
    public Result get(Collection<String> relations, Collection<ObjectType> conditions);

    /**
     * Gets all elements referenced by a relation.
     *
     * @param relation
     *            relation to look up
     * @return the elements referenced by this relation
     */
    public Result get(String relation);

    /**
     * Returns all outgoing relations from this node.
     *
     * @return all outgoing relations
     */
    public Set<String> getRelations();

    /**
     * Returns the semantic web class of this node.
     *
     * @throws NoSuchElementException
     *             if there is no named node
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    @Override
    public String getType();

    /**
     * Returns a hash value of this object.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode();

    /**
     * Returns true if this node does not contain any relations.
     *
     * @return whether the node is empty
     */
    public boolean isEmpty();

    /**
     * Returns an iterator to iterate all referenced Nodes. Literals will be
     * skipped. All literals can be retrieved using the values() method.
     *
     * @return an iterator over all child nodes
     */
    public Iterator<ObjectType> iterator();

    /**
     * Returns the last (largest) index in use to reference elements by index,
     * or {@code null} if there is no such.
     *
     * @return the largest index in use
     */
    public Long last();

    /**
     * Returns whether this node has all the data from the conditions node. This
     * means, that the node contains all the data contained in the condition
     * node. It may have a different address, and it may have <em>more</em> data
     * as well.
     *
     * @param condition
     *            a node which may be a subset of the information contained in
     *            this node
     * @return whether this node fulfils the set of conditions
     */
    @Override
    public boolean matches(ObjectType condition);

    /**
     * Adds a node by relation.
     *
     * @param relation
     *            Relation the object shall be added under
     * @param object
     *            object to add
     * @return this, for in-line use
     */
    public Node put(NodeReference relation, ObjectType object);

    /**
     * Adds a literal by relation.
     *
     * @param relation
     *            Relation the object shall be added under
     * @param object
     *            object to add
     * @return this, for in-line use
     */
    public Node put(NodeReference relation, String object);

    /**
     * Adds a node by relation.
     *
     * @param relation
     *            Relation the object shall be added under
     * @param object
     *            object to add
     * @return this, for in-line use
     */
    public Node put(String relation, ObjectType object);

    /**
     * Adds a literal by relation.
     *
     * @param relation
     *            Relation the object shall be added under
     * @param object
     *            object to add
     * @return this, for in-line use
     */
    public Node put(String relation, String object);

    /**
     * Replaces all objects that are named nodes, but don’t have content, by
     * node references. A clean-up method for internal use.
     *
     * @param recursive
     *            if true, also invokes the function on all child nodes
     */
 //   void replaceAllNamedNodesWithNoDataByNodeReferences(boolean recursive);

    /**
     * Creates a Jena model from this node.
     *
     * @return a Jena model representing this node
     */
    public Model toModel();

    /**
     * Adds this node to an existing Jena model.
     *
     * @param model
     *            a Jena model to add this node to
     */
    public void toModel(Model model);

    /**
     * Converts this node to an RDFNode as part of a Jena model.
     *
     * @param model
     *            model to create objects in
     * @return an RDFNode representing this node
     */
    @Override
    public RDFNode toRDFNode(Model model);
}
