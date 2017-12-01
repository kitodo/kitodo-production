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

package org.kitodo.dataaccess;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

/**
 * An anonymous linked data Node. The most nodes are anonymous. In Java, they
 * are identified by the variable that holds them. In the data, they are often
 * identified by a certain value held by a certain relation. Therefore the short
 * class name “Node” was used for the anonymous node class, and not the name
 * “AnonymousNode”.
 *
 * <p>
 * The class offers functions known from Java’s collections, and they are
 * intended to intuitively behave the same way.
 *
 */
public abstract class Node implements AccessibleObject, NodeType {

    /**
     * The index of the first child node that is referenced by index.
     */
    public static final short FIRST_INDEX = 1;

    /**
     * Adds a node to the nodes referenced by index. The add function implements
     * the traditional add behavior, that is all nodes with an index equal to
     * the given index or higher are moved upwards by one. If the index list has
     * holes, only the next elements are moved up until a hole is encountered.
     *
     * @param index
     *            numeric position to add the element
     * @param element
     *            element to add
     */
    public abstract void add(long index, ObjectType element);

    /**
     * Adds a node to the nodes referenced by index. The add function implements
     * the traditional behavior that the node is added with an index that is one
     * higher that the highest index in use before.
     *
     * @param <T>
     *            possible subclass of Node. When invoked on a named node, will
     *            return a named node instead.
     *
     * @param element
     *            Element to add
     * @return this node, for method chaining
     */
    public abstract <T extends Node> T add(ObjectType element);

    /**
     * Convenience method to {@link #add(ObjectType)} a collection of nodes by a
     * single method call. The elements are added in the order that they are
     * returned by the collection’s iterator.
     *
     * @param elements
     *            collection of nodes to add
     */
    public abstract void addAll(Collection<? extends ObjectType> elements);

    /**
     * Adds an element as the first to the list of elements referencey by
     * number. The add function implements the traditional add behaviour, that
     * is all nodes with an index equal to the given index or higher are moved
     * upwards by one. If the index list has holes, only the next elements are
     * moved up until a hole is encountered.
     *
     * @param element
     *            element to add
     */
    public abstract void addFirst(ObjectType element);

    /**
     * Returns the node as an unordered node. All ordered ({@code rdf:_1},
     * {@code rdf:_2}, …) and {@code rdf:value} relations are replaced by
     * relations labeled with the type of the child node, which is removed from
     * the child node, iff the child node has exactly one type.
     *
     * <p>
     * If the remaining node only contains one {@code rdf:value} relation, only
     * the object form this relation will be returned. This is why the method’s
     * return type is {@linkplain ObjectType}, not {@linkplain Node}.
     *
     * <p>
     * This method is convenient to import sets of objects from XML files, where
     * they are referenced numerically also if their order isn’t important.
     *
     * @param removeType
     *            remove the type from this node (probably {@code false}, but
     *            may be convenient)
     * @return the node as unordered
     */
    public abstract ObjectType asUnordered(boolean removeType);

    /**
     * Tests whether an element is directly referenced by this node.
     *
     * @param o
     *            object to search
     * @return whether the object is contained
     */
    public abstract boolean contains(Object o);

    /**
     * Tests whether this node has an outgoing edge labeled by the given label.
     *
     * @param label
     *            label to look for
     * @return whether the object is contained
     */
    public boolean containsKey(IdentifiableNode label) {
        return containsKey(label.getIdentifier());
    }

    /**
     * Tests whether this node has an outgoing edge labeled by the given label.
     *
     * @param label
     *            label to look for
     * @return whether the object is contained
     */
    public abstract boolean containsKey(String label);

    /**
     * Provides the entrySet of this Node for iteration.
     *
     * @return the edges
     */
    public abstract Set<Entry<String, Collection<ObjectType>>> entrySet();

    /**
     * Resolves the given graph path against this node. The node passed in must
     * be a graph path.
     *
     * @param graphPath
     *            graph path to resolve
     * @return the results from resolving
     */
    public abstract Result find(Node graphPath);

    /**
     * Finds the first index in use to reference elements by index.
     *
     * @return the first index in use
     */
    public abstract Optional<Long> first();

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
    public abstract Result get(Collection<String> relations, Collection<ObjectType> conditions);

    /**
     * Gets a literal referenced by relation.
     *
     * @param relation
     *            relation to look up
     * @return the value of the literal.
     */
    public Result get(IdentifiableNode relation) {
        return get(relation.getIdentifier());
    }

    /**
     * Gets all nodes referenced by relation which have a certain type.
     *
     * @param relation
     *            relation to look up
     * @param type
     *            node type to filter by
     * @return the nodes
     */
    public Result get(IdentifiableNode relation, IdentifiableNode type) {
        return get(relation.getIdentifier(), type.getIdentifier());
    }

    /**
     * Returns all nodes referenced by the given relation which have an
     * identifier described by relation and value. A null value for relation
     * means all relations. This is a convenience method to simplify use of the
     * filtering method.
     *
     * @param relation
     *            relation whose nodes are to be inspected. May be null to
     *            inspect all relations.
     * @param identifierRelation
     *            identifier referencing relation
     * @param identifierValue
     *            identifier value
     * @return found nodes
     */
    public abstract Result get(IdentifiableNode relation, IdentifiableNode identifierRelation, String identifierValue);

    /**
     * Gets all nodes referenced by relation which have a certain type.
     *
     * @param relation
     *            relation to look up
     * @param type
     *            node type to filter by
     * @return the nodes
     */
    public Result get(IdentifiableNode relation, String type) {
        return get(relation.getIdentifier(), type);
    }

    /**
     * Returns a node by its index.
     *
     * @param index
     *            Node index to return
     * @return the node referenced by that index
     */
    public abstract Result get(long index);

    /**
     * Gets all elements referenced by a relation.
     *
     * @param relation
     *            relation to look up
     * @return the elements referenced by this relation
     */
    public abstract Result get(String relation);

    /**
     * Gets all nodes referenced by relation which have a certain type.
     *
     * @param relation
     *            relation to look up
     * @param type
     *            node type to filter by
     * @return the nodes
     */
    public Result get(String relation, IdentifiableNode type) {
        return get(relation, type.getIdentifier());
    }

    /**
     * Returns all nodes referenced by the given relation which have an
     * identifier described by relation and value. A null value for relation
     * means all relations. This is a convenience method to simplify use of the
     * filtering method.
     *
     * @param relation
     *            relation whose nodes are to be inspected. May be null to
     *            inspect all relations.
     * @param identifierRelation
     *            identifier referencing relation
     * @param identifierValue
     *            identifier value
     * @return found nodes
     */
    public abstract Result get(String relation, IdentifiableNode identifierRelation, ObjectType identifierValue);

    /**
     * Gets all nodes referenced by relation which have a certain type.
     *
     * @param relation
     *            relation to look up
     * @param type
     *            node type to filter by
     * @return the nodes
     */
    public abstract Result get(String relation, String type);

    /**
     * Returns an identifiable held as object.
     *
     * @param identifier
     *            to identify the object
     * @return the identifier
     * @throws NoSuchElementException
     *             if not found
     * @throws NoSuchMethodError
     *             if several ones found
     */
    public abstract IdentifiableNode getByIdentifier(String identifier);

    /**
     * Returns the first child node whose type is equal to the given url.
     *
     * @param url
     *            url of the child to look for
     * @return the first child of the url type
     * @throws NoSuchElementException
     *             if there is none
     */
    public Result getByType(IdentifiableNode url) {
        return getByType(url.getIdentifier());
    }

    /**
     * Returns the first child node whose type is equal to the given url and
     * which has an attribute with the given name and value. This can be used to
     * look up a child by its identifier, given an identifying instance and the
     * desired value of it.
     *
     * @param rdfType
     *            url of the child to look for
     * @param idType
     *            url of the identifier
     * @param idValue
     *            value of the identifier
     * @return the first child of the url type
     */
    public Result getByType(IdentifiableNode rdfType, IdentifiableNode idType, Literal idValue) {
        return getByType(rdfType.getIdentifier(), idType.getIdentifier(), idValue.getValue());
    }

    /**
     * Returns all child nodes whose type is equal to the given url and which
     * have an attribute with the given name and value. This can be used to look
     * up children by its identifier, given an identifying instance and the
     * desired value of it.
     *
     * @param rdfType
     *            url of the child to look for
     * @param idType
     *            url of the identifier
     * @param idValue
     *            value of the identifier
     * @return the first child of the url type
     */
    public Result getByType(IdentifiableNode rdfType, IdentifiableNode idType, String idValue) {
        return getByType(rdfType.getIdentifier(), idType.getIdentifier(), idValue);
    }

    /**
     * Returns the first child node whose type is equal to the given url.
     *
     * @param type
     *            url of the child to look for
     * @return the first child of the url type
     */
    public abstract Result getByType(String type);

    /**
     * Returns the first child node whose type is equal to the given url and
     * which has an attribute with the given name and value. This can be used to
     * look up a child by its identifier, given an identifying instance and the
     * desired value of it.
     *
     * @param rdfType
     *            url of the child to look for
     * @param idType
     *            url of the identifier
     * @param idValue
     *            value of the identifier
     * @return the first child of the url type
     */
    public abstract Result getByType(String rdfType, String idType, String idValue);

    /**
     * Returns a list of all nodes referenced by a list membership relation.
     *
     * @return all nodes referenced by list membership relation
     */
    public abstract List<Result> getEnumerated();

    /**
     * Returns the first node referenced by numeric index, or {@code null} if no
     * element is referenced by index or the first element referenced by index
     * is not a node.
     *
     * @return the first node referenced by index
     */
    public abstract Result getFirst();

    /**
     * Returns the first node referenced by numeric index, or {@code null} if no
     * element is referenced by index or the last element referenced by index is
     * not a node.
     *
     * @return the first node referenced by index
     */
    public abstract Result getLast();

    /**
     * Returns all outgoing relations from this node.
     *
     * @return all outgoing relations
     */
    public abstract Set<String> getRelations();

    /**
     * Checks whether this node has the given type.
     *
     * @param type
     *            type to check for
     * @return whether this node has the type
     */
    public boolean hasType(IdentifiableNode type) {
        return hasType(type.getIdentifier());
    }

    /**
     * Checks whether this node has the given type.
     *
     * @param type
     *            type to check for
     * @return whether this node has the type
     */
    public abstract boolean hasType(String type);

    /**
     * Returns true if this node does not contain any relations.
     *
     * @return whether the node is empty
     */
    public abstract boolean isEmpty();

    /**
     * Returns an iterator to iterate all referenced Nodes. Literals will be
     * skipped. All literals can be retrieved using the values() method.
     *
     * @return an iterator over all child nodes
     */
    public abstract Iterator<ObjectType> iterator();

    /**
     * Returns the last (largest) index in use to reference elements by index.
     * If there is no such, the optional is empty.
     *
     * @return the largest index in use
     */
    public abstract Optional<Long> last();

    /**
     * Adds a node by relation.
     *
     * @param <T>
     *            possible subclass of Node. When invoked on a named node, will
     *            return a named node instead.
     *
     * @param relation
     *            Relation the object shall be added under
     * @param object
     *            object to add
     * @return this, for method chaining
     */
    @SuppressWarnings("unchecked")
    public <T extends Node> T put(IdentifiableNode relation, ObjectType object) {
        put(relation.getIdentifier(), object);
        return (T) this;
    }

    /**
     * Adds a literal by relation.
     *
     * @param <T>
     *            possible subclass of Node. When invoked on a named node, will
     *            return a named node instead.
     *
     * @param relation
     *            Relation the object shall be added under
     * @param object
     *            object to add
     * @return this, for method chaining
     */
    @SuppressWarnings("unchecked")
    public <T extends Node> T put(IdentifiableNode relation, String object) {
        put(relation.getIdentifier(), object);
        return (T) this;
    }

    /**
     * Adds a node by relation.
     *
     * @param <T>
     *            possible subclass of Node. When invoked on a
     *            {@code NamedNode}, will return a named node instead.
     *
     * @param relation
     *            Relation the object shall be added under
     * @param object
     *            object to add
     * @return this, for method chaining
     */
    public abstract <T extends Node> T put(String relation, ObjectType object);

    /**
     * Adds a literal by relation.
     *
     * @param <T>
     *            possible subclass of Node. When invoked on a named node, will
     *            return a named node instead.
     *
     * @param relation
     *            Relation the object shall be added under
     * @param object
     *            object to add
     * @return this, for method chaining
     */
    public abstract <T extends Node> T put(String relation, String object);

    /**
     * Adds all of the objects by the given relation.
     *
     * @param relation
     *            relation to add the objects on
     * @param objects
     *            objects to add
     */
    public void putAll(IdentifiableNode relation, Collection<? extends ObjectType> objects) {
        putAll(relation.getIdentifier(), objects);
    }

    /**
     * Adds all of the objects by the given relation.
     *
     * @param relation
     *            relation to add the objects on
     * @param objects
     *            objects to add
     */
    public abstract void putAll(String relation, Collection<? extends ObjectType> objects);

    /**
     * Removes an object from all relations. If a relations becomes
     * destinationless by this it will be removed, too.
     *
     * @param object
     *            Object to remove
     * @return whether the collection was changed
     */
    public abstract boolean remove(Object object);

    /**
     * Removes all objects linked by the given relation.
     *
     * @param relation
     *            relation to remove
     * @return the previously linked objects
     */
    public Collection<ObjectType> removeAll(IdentifiableNode relation) {
        return removeAll(relation.getIdentifier());
    }

    /**
     * Removes all objects linked by the given relation.
     *
     * @param relation
     *            relation to remove
     * @return the previously linked objects
     */
    public abstract Collection<ObjectType> removeAll(String relation);

    /**
     * Removes the first collection of elements of the enumerated elements.
     */
    public abstract void removeFirst();

    /**
     * Removes the first occurrence of an object from the enumerated elements.
     *
     * @param object
     *            object to remove
     * @return if the collection was changed
     */
    public abstract boolean removeFirstOccurrence(Object object);

    /**
     * Removes the last object from the enumerated elements.
     */
    public abstract void removeLast();

    /**
     * Removes the last occurrence of an object from the enumerated elements.
     *
     * @param object
     *            object to remove
     * @return if the collection was changed
     */
    public abstract boolean removeLastOccurrence(Object object);

    /**
     * Removes all relations of the given kind and replaces them by relations to
     * the set of objects provided.
     *
     * @param relation
     *            relation to replace
     * @param objects
     *            new objects for this relation
     * @return the objects previously related
     */
    public abstract Collection<ObjectType> replace(String relation, Set<ObjectType> objects);

    /**
     * Replaces an element index with a new element, removing all elements at
     * this index if there were several.
     *
     * @param index
     *            index of elements to replace
     * @param element
     *            new element for this index
     */
    public abstract void set(long index, ObjectType element);

    /**
     * Sets a literal as only child of this node. Removes all other nodes, but
     * will keep attribute literals and node references.
     *
     * <p>
     * This is a convenience method to <em>set text content</em> when building
     * XML-like data structure. All <em>tag content</em> will be removed, but
     * <em>attributes will be kept</em>.
     * {@code Storage.createNode(ns + "SomeType").setValue("value");} should be
     * written to XML as {@code <ns:SomeType>value</ns:SomeType>}.
     *
     * @param <T>
     *            possible subclass of Node. When invoked on a named node, will
     *            return a named node instead.
     *
     * @param value
     *            literal value to set
     * @return this, for method chaining
     */
    public abstract <T extends Node> T setValue(String value);

    /**
     * Returns the number of elements in this node, or Intexer.MAX_VALUE if more
     * than Integer.MAX_VALUE.
     *
     * @return the number of elements in this node
     */
    public abstract int size();
}
