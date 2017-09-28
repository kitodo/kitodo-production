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

import java.nio.BufferOverflowException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.*;
import org.kitodo.lugh.vocabulary.RDF;

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
public class MemoryNode implements Node {

    /**
     * Iterator to iterate through the edges and return all directly referenced
     * nodes. Leaves that are not nodes are not returned.
     *
     * @author Matthias Ronge
     */
    private class ChildNodeIterator implements Iterator<ObjectType> {
        /**
         * The next node found.
         */
        ObjectType found;
        /**
         * Inner iterator to iterate over the set of objects of a relation.
         */
        Iterator<ObjectType> inner;
        /**
         * Outer iterator to iterate over the map of relations.
         */
        Iterator<Entry<String, Collection<ObjectType>>> outer;

        /**
         * Creates a new ChildNodeIterator.
         */
        private ChildNodeIterator() {
            outer = edges.entrySet().iterator();
        }

        /**
         * Looks for a next node.
         *
         * @return a next node, or null if there is none
         */
        private ObjectType findNextNode() {
            while (true) {
                if ((inner == null) || !inner.hasNext()) {
                    if (outer.hasNext()) {
                        inner = outer.next().getValue().iterator();
                    } else {
                        return null;
                    }
                }
                while (inner.hasNext()) {
                    return inner.next();
                }
            }
        }

        /**
         * Returns whether there is a next node retrievable.
         */
        @Override
        public boolean hasNext() {
            if (found == null) {
                found = findNextNode();
            }
            return found != null;
        }

        /**
         * Returns the next node.
         *
         * @throws NoSuchElementException
         *             if there is none.
         */
        @Override
        public ObjectType next() {
            if (found == null) {
                found = findNextNode();
            }
            if (found == null) {
                throw new NoSuchElementException();
            }
            ObjectType n = found;
            found = null;
            return n;
        }

        /**
         * The iterator does not support removing.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * The pair of the first and last index in use, as return type of the
     * {@code range()} function.
     *
     * @author Matthias Ronge
     */
    private class Indices {
        /**
         * The first index in use.
         */
        private final long first;
        /**
         * The last index in use.
         */
        private final long last;

        /**
         * Creates a new pair of indices.
         *
         * @param first
         *            first index in use
         * @param last
         *            last index in use
         */
        public Indices(long first, long last) {
            this.first = first;
            this.last = last;
        }
    }

    /**
     * The edges leading from this node to other nodes and leaves.
     */
    protected final HashMap<String, Collection<ObjectType>> edges = new HashMap<>();

    /**
     * Creates an empty node.
     */
    MemoryNode() {
    }

    /**
     * Create a node with a type attribute set.
     *
     * @param type
     *            node type
     */
    MemoryNode(IdentifiableNode type) {
        if (type != null) {
            assert URI_SCHEME.matcher(type.getIdentifier()).find() : "Type isn’t a valid URI.";
            edges.put(RDF.TYPE.getIdentifier(), new LinkedList<>(Arrays.asList(new ObjectType[] {type })));
        }
    }

    /**
     * Create a node with a type attribute set.
     *
     * @param type
     *            node type
     */
    MemoryNode(String type) {
        if (type != null) {
            assert URI_SCHEME.matcher(type).find() : "Type isn’t a valid URI.";
            edges.put(RDF.TYPE.getIdentifier(),
                    new LinkedList<>(Arrays.asList(new ObjectType[] {new MemoryNodeReference(type) })));
        }
    }

    /**
     * Adds a node to the nodes refereced by index. The add function implements
     * the traditional add behaviour, that is all nodes with an index equal to
     * the given index or higher are moved upwards by one. If the index list has
     * holes, only the next elements are moved up until a hole is encountered.
     *
     * @param index
     *            numeric position to add the element
     * @param element
     *            element to add
     */
    @Override
    public void add(int index, ObjectType element) {
        // find first index >= index that is free
        long freeIndex = index;
        while (edges.containsKey(RDF.toURL(freeIndex))) {
            freeIndex++;
        }

        // move all elements till to the free index up by 1
        for (long i = freeIndex; i >= index; i--) {
            edges.put(RDF.toURL(i), edges.remove(RDF.toURL(i - 1)));
        }

        // add the given element at the given position
        edges.put(RDF.toURL(index), new HashSet<>(Arrays.asList(new ObjectType[] {element })));
    }

    /**
     * Adds a node to the nodes referenced by index. The add function implements
     * the traditional behaviour that the node is added with an index that is
     * one higher that the highest index in use before.
     *
     * @param element
     *            Element to add
     * @return this node, for in-line use
     */
    @SuppressWarnings("unchecked") // this is always instanceof Node
    @Override
    public <T extends Node> T add(ObjectType element) {
        String key = RDF.toURL(last().orElse((long) FIRST_INDEX - 1) + 1);
        edges.put(key, new LinkedList<>(Arrays.asList(new ObjectType[] {element })));
        return (T) this;
    }

    /**
     * Convenience method to {@link #add(ObjectType)} a collection of nodes by a
     * single method call. The elements are added in the order that they are
     * returned by the collection’s iterator.
     *
     * @param elements
     *            collection of nodes to add
     */
    @Override
    public void addAll(Collection<? extends ObjectType> elements) {
        Long pos = last().orElse((long) FIRST_INDEX - 1);
        for (ObjectType element : elements) {
            edges.put(RDF.toURL(++pos), new HashSet<>(Arrays.asList(new ObjectType[] {element })));
        }
    }

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
    @Override
    public void addFirst(ObjectType element) {
        add(FIRST_INDEX, element);
    }

    /**
     * Returns the node as an unordered node. All ordered ({@code rdf:_1},
     * {@code rdf:_2}, …) and {@code rdf:value} relations are replaced by
     * relations labelled with the type of the child node, which is removed from
     * the child node, iff the child node has exactly one type.
     * <p>
     * If the remaining node only contains one {@code rdf:value} relation, only
     * the object form this relation will be returned. This is why the method’s
     * return type is {@linkplain ObjectType}, not {@linkplain Node}.
     * <p>
     * This method is convenient to import sets of objects from XML files, where
     * they are referenced numerically also if their order isn’t important.
     *
     * @param removeType
     *            remove the type from this node (probably {@code false}, but
     *            may be convenient)
     * @return the node as unordered
     */
    @Override
    public ObjectType asUnordered(boolean removeType) {
        Node result = this instanceof NamedNode ? new MemoryNamedNode(((NamedNode) this).getIdentifier())
                : new MemoryNode();
        for (Entry<String, Collection<ObjectType>> edge : edges.entrySet()) {

            // If the child is attached numerically or by RDF:value ...

            String relation = edge.getKey();
            if (removeType && relation.equals(RDF.TYPE.getIdentifier())) {
                continue;
            }
            if (relation.startsWith(RDF.SEQ_NO_PREFIX) || relation.equals(RDF.VALUE.getIdentifier())) {
                for (ObjectType object : edge.getValue()) {
                    if (object instanceof Node) {

                        // ... and the child has exactly one type:

                        try {
                            String type = ((Node) object).getType();

                            // process grandchildren and remove type from child

                            object = ((Node) object).asUnordered(true);

                            // mount child by its type instead

                            result.put(type, object);
                        }

                        // if the child has no or several types, just copy it

                        // TODO: When switching to a never Java version,
                        // collapse duplicate catch block

                        catch (NoSuchElementException e) {
                            if (object instanceof Node) {
                                result.put(relation, ((Node) object).asUnordered(false));
                            } else {
                                result.put(relation, object);
                            }
                        } catch (BufferOverflowException e) {
                            if (object instanceof Node) {
                                result.put(relation, ((Node) object).asUnordered(false));
                            } else {
                                result.put(relation, object);
                            }
                        }
                    }

                    // Attach any literals as rdf:value

                    else {
                        result.put(RDF.VALUE.getIdentifier(), object);
                    }
                }
            }

            // Nodes that are not attached numerically or by value will just be
            // copied.

            else {
                result.putAll(relation, edge.getValue());
            }
        }

        // If the result is one single rdf:value, just return the value

        if ((result.entrySet().size() == 1) && result.containsKey(RDF.VALUE.getIdentifier())) {
            Result values = result.get(RDF.VALUE.getIdentifier());
            if (values.isUnique()) {
                return values.expectable();
            }
        }

        return result;
    }

    /**
     * Tests whether an element is directly referenced by this node.
     *
     * @param o
     *            object to search
     * @return whether the object is contained
     */
    @Override
    public boolean contains(Object o) {
        for (Entry<String, Collection<ObjectType>> k : edges.entrySet()) {
            if (k.getValue().contains(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tests whether this node has an outgoing edge labeled by the given label.
     *
     * @param label
     *            label to look for
     * @return whether the object is contained
     */
    @Override
    public boolean containsKey(String label) {
        return edges.containsKey(label);
    }

    /**
     * Creates an anonymous resource. Overridden in {@link NamedNode} to create
     * a named resource.
     *
     * @param model
     *            model to create the resource in
     * @return the created resource
     */
    protected Resource createRDFSubject(Model model) {
        return model.createResource();
    }

    /**
     * Provides the entrySet of this Node for iteration.
     *
     * @return the edges
     */
    @Override
    public Set<Entry<String, Collection<ObjectType>>> entrySet() {
        return edges.entrySet();
    }

    /**
     * Compares two nodes for equality.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MemoryNode other = (MemoryNode) obj;
        if (edges == null) {
            if (other.edges != null) {
                return false;
            }
        } else if (!edges.equals(other.edges)) {
            return false;
        }
        return true;
    }

    /**
     * Resolves the given graph path against this node. The node passed in must
     * be a graph path.
     *
     * @param graphPath
     *            graph path to resolve
     * @return the results from resolving
     */
    @Override
    public Result find(Node graphPath) {
        return GraphPath.apply(new MemoryResult(this), graphPath, MemoryStorage.INSTANCE);
    }

    /**
     * Finds the first index in use to reference elements by index.
     *
     * @return the first index in use
     */
    @Override
    public Optional<Long> first() {
        Indices range = range();
        return range == null ? Optional.empty() : Optional.of(range.first);
    }

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
    @Override
    public Result get(Collection<String> relations, Collection<ObjectType> conditions) {
        Result result = new MemoryResult();
        if (relations.isEmpty()) {
            relations = new LinkedList<>(Arrays.asList(new String[] {GraphPath.ANY_PREDICATE.getIdentifier() }));
        }
        for (String relation : relations) {
            Iterator<ObjectType> nodes = GraphPath.ANY_PREDICATE.getIdentifier().equals(relation) ? iterator()
                    : edges.containsKey(relation) ? edges.get(relation).iterator()
                            : Collections.<ObjectType>emptyIterator();
            while (nodes.hasNext()) {
                ObjectType node = nodes.next();
                if (conditions.isEmpty()) {
                    result.add(node);
                } else {
                    if (node instanceof AccessibleObject) {
                        Collection<ObjectType> remainingConditions = new LinkedList<>(conditions);
                        for (Iterator<ObjectType> i = remainingConditions.iterator(); i.hasNext();) {
                            ObjectType condition = i.next();
                            if (((AccessibleObject) node).matches(condition)) {
                                i.remove();
                            }
                        }
                        if (remainingConditions.isEmpty()) {
                            result.add(node);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a node by its index.
     *
     * @param index
     *            Node index to return
     * @return the node referenced by that index
     */
    @Override
    public Result get(long index) {
        return new MemoryResult(get(RDF.toURL(index)));
    }

    /**
     * Gets all elements referenced by a relation.
     *
     * @param relation
     *            relation to look up
     * @return the elements referenced by this relation
     */
    @Override
    public Result get(String relation) {
        return new MemoryResult(edges.get(relation));
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
    @Override
    public Result get(String relation, IdentifiableNode identifierRelation, ObjectType identifierValue) {
        return get(relation != null ? Arrays.asList(new String[] {relation }) : null,
                Arrays.asList(new ObjectType[] {new MemoryNode().put(identifierRelation, identifierValue) }));
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
    @Override
    public Result get(String relation, String type) {
        Collection<ObjectType> objects = edges.get(relation);
        Result result = new MemoryResult(objects.size());
        for (ObjectType object : objects) {
            if (object instanceof Node) {
                Node nodeObject = (Node) object;
                if (type.equals(nodeObject.getType())) {
                    result.add(nodeObject);
                }
            }
        }
        return result;
    }

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
    @Override
    public IdentifiableNode getByIdentifier(String identifier) {
        Result found = new MemoryResult();
        for (Entry<String, Collection<ObjectType>> predicate : edges.entrySet()) {
            for (ObjectType object : predicate.getValue()) {
                if (object instanceof IdentifiableNode) {
                    IdentifiableNode nodeObject = (IdentifiableNode) object;
                    String id = nodeObject.getIdentifier();
                    if (id.equals(identifier)) {
                        found.add(nodeObject);
                    }
                }
            }
        }
        switch (found.size()) {
            case 0:
                throw new NoSuchElementException();
            case 1:
                return found.identifiableNodeExpectable();
            default:
                throw new NoSuchMethodError("Merging nodes not yet implemented.");
        }
    }

    /**
     * Returns the first child node whose type is equal to the given url.
     *
     * @param type
     *            url of the child to look for
     * @return the first child of the url type
     */
    @Override
    public Result getByType(String type) {
        Result found = new MemoryResult();
        for (Entry<String, Collection<ObjectType>> predicate : edges.entrySet()) {
            OBJECTS: for (ObjectType object : predicate.getValue()) {
                if (object instanceof Node) {
                    Node nodeObject = (Node) object;
                    Result result = nodeObject.get(RDF.TYPE);
                    for (IdentifiableNode typeNode : result.identifiableNodes()) {
                        if (typeNode.getIdentifier().equals(type)) {
                            found.add(nodeObject);
                            continue OBJECTS;
                        }
                    }
                }
            }
        }
        return found;
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
    @Override
    public Result getByType(String rdfType, String idType, String idValue) {
        Result result = new MemoryResult();
        for (Entry<String, Collection<ObjectType>> entry : edges.entrySet()) {
            for (ObjectType value : entry.getValue()) {
                if (value instanceof Node) {
                    Node node = (Node) value;
                    if (node.hasType(rdfType) && node.get(idType).strings().contains(idValue)) {
                        result.add(node);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a list of all nodes referenced by a list membership relation.
     *
     * @return all nodes referenced by list membership relation
     */
    @Override
    public List<Result> getEnumerated() {
        Indices range = range();
        if (range == null) {
            return Collections.emptyList();
        }
        ArrayList<Result> result = new ArrayList<>((int) range.last);
        for (long i = 0; i < (range.first - 1); i++) {
            result.add(null);
        }
        for (long i = range.first; i <= range.last; i++) {
            result.add(get(i));
        }
        return result;
    }

    /**
     * Returns the first node referenced by numeric index, or {@code null} if no
     * element is referenced by index or the first element referenced by index
     * is not a node.
     *
     * @return the first node referenced by index
     */
    @Override
    public Result getFirst() {
        Optional<Long> first = first();
        if (first.isPresent()) {
            return get(first.orElseThrow(() -> {
                return new IllegalStateException();
            }));
        } else {
            return MemoryResult.EMPTY;
        }
    }

    /**
     * Returns the first node referenced by numeric index, or {@code null} if no
     * element is referenced by index or the last element referenced by index is
     * not a node.
     *
     * @return the first node referenced by index
     */
    @Override
    public Result getLast() {
        Optional<Long> last = last();
        if (last.isPresent()) {
            return get(last.orElseThrow(() -> {
                return new IllegalStateException();
            }));
        } else {
            return MemoryResult.EMPTY;
        }
    }

    /**
     * Returns all outgoing relations from this node.
     *
     * @return all outgoing relations
     */
    @Override
    public Set<String> getRelations() {
        return edges.keySet();
    }

    /**
     * Returns the semantic web class of this node.
     *
     * @throws NoSuchElementException
     *             if there is no named node
     * @throws BufferOverflowException
     *             if there are several possible answers
     */
    @Override
    public String getType() {
        return get(RDF.TYPE).identifiableNodeExpectable().getIdentifier();
    }

    /**
     * Returns a hash value of this object.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + (edges == null ? 0 : edges.hashCode());
        return result;
    }

    /**
     * Checks whether this node has the given type.
     *
     * @param type
     *            type to check for
     * @return whether this node has the type
     */
    @Override
    public boolean hasType(String type) {
        for (IdentifiableNode identifiableNode : get(RDF.TYPE).identifiableNodes()) {
            if (type.equals(identifiableNode.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if this node does not contain any relations.
     *
     * @return whether the node is empty
     */
    @Override
    public boolean isEmpty() {
        return edges.isEmpty();
    }

    /**
     * Returns an iterator to iterate all referenced Nodes. Literals will be
     * skipped. All literals can be retrieved using the values() method.
     *
     * @return an iterator over all child nodes
     */
    @Override
    public Iterator<ObjectType> iterator() {
        return new ChildNodeIterator();
    }

    /**
     * Returns the edges Strings.
     *
     * @return all relations
     */
    @Override
    public Set<String> keySet() {
        return edges.keySet();
    }

    /**
     * Returns the last (largest) index in use to reference elements by index,
     * or {@code null} if there is no such.
     *
     * @return the largest index in use
     */
    @Override
    public Optional<Long> last() {
        Indices range = range();
        return range == null ? Optional.empty() : Optional.of(range.last);
    }

    /**
     * Returns whether this node has all the data from the conditions node. This
     * means, that the node contains all the data contained in the condition
     * node. It may have a different address, and it may have <em>more</em> data
     * as well.
     *
     * @param condition
     *            a node which may be a subset of the information contained in
     *            this node
     * @return whether this node fulfills the set of conditions
     */
    @Override
    public boolean matches(ObjectType condition) {
        if (condition == null) {
            return true;
        }
        if (!(condition instanceof Node)) {
            return false;
        }
        for (String relation : ((Node) condition).getRelations()) {
            Iterator<ObjectType> candidates;
            if (relation.equals(GraphPath.ANY_PREDICATE.getIdentifier())) {
                candidates = iterator();
            } else {
                Collection<ObjectType> related = edges.get(relation);
                if (related == null) {
                    return false;
                }
                candidates = related.iterator();
            }
            Collection<ObjectType> conditions = ((Node) condition).get(relation);
            while (candidates.hasNext() && !conditions.isEmpty()) {
                ObjectType candidate = candidates.next();
                if (candidate instanceof AccessibleObject) {
                    for (Iterator<ObjectType> i = conditions.iterator(); i.hasNext();) {
                        ObjectType currentCondition = i.next();
                        if (((AccessibleObject) candidate).matches(currentCondition)) {
                            i.remove();
                        }
                    }
                } else {
                    conditions.remove(candidate);
                }
            }
            if (!conditions.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a node by relation.
     *
     * @param relation
     *            Relation the object shall be added under
     * @param object
     *            object to add
     * @return this, for in-line use
     */
    @SuppressWarnings("unchecked") // this is always instanceof Node
    @Override
    public <T extends Node> T put(String relation, ObjectType object) {
        if (relation.equals(RDF.ABOUT.getIdentifier())) {
            throw new IllegalArgumentException(
                    "Forbidden to put http://www.w3.org/1999/02/22-rdf-syntax-ns#about entries. Use new NamedNode(identityURL [, data]); instead.");
        }
        if (edges.containsKey(relation)) {
            edges.get(relation).add(object);
        } else {
            edges.put(relation, new LinkedList<>(Arrays.asList(new ObjectType[] {object })));
        }
        return (T) this;
    }

    /**
     * Adds a literal by relation.
     *
     * @param relation
     *            Relation the object shall be added under
     * @param object
     *            object to add
     * @return this, for in-line use
     */
    @SuppressWarnings("unchecked") // this is always instanceof Node
    @Override
    public <T extends Node> T put(String relation, String object) {
        this.put(relation, MemoryLiteral.create(object, null));
        return (T) this;
    }

    /**
     * Adds all of the objects by the given relation.
     *
     * @param relation
     *            relation to add the objects on
     * @param objects
     *            objects to add
     */
    @Override
    public void putAll(String relation, Collection<? extends ObjectType> objects) {
        if (relation.equals(RDF.ABOUT.getIdentifier())) {
            throw new IllegalArgumentException("Forbidden to put rdf:about entries. Use NamedNode instead.");
        }
        if (objects.isEmpty()) {
            return;
        }
        if (edges.containsKey(relation)) {
            edges.get(relation).addAll(objects);
        } else {
            edges.put(relation, new HashSet<ObjectType>(objects));
        }
    }

    /**
     * Returns the first and last numeric index used in this node to reference
     * numeric elements. Returns {@code null} if no numeric indexes are in use
     * at all.
     *
     * @return first and last numeric index
     */
    private Indices range() {
        Long last = null;
        Long first = null;
        for (Entry<String, Collection<ObjectType>> a : edges.entrySet()) {
            Long value = RDF.sequenceNumberOf(a.getKey());
            if (value != null) {
                if ((first == null) || (value < first)) {
                    first = value;
                }
                if ((last == null) || (value > last)) {
                    last = value;
                }
            }
        }
        return (first == null) || (last == null) ? null : new Indices(first, last);
    }

    /**
     * Removes an object from all relations. If a relations becomes
     * destinationless by this it will be removed, too.
     *
     * @param object
     *            Object to remove
     * @return whether the collection was changed
     */
    @Override
    public boolean remove(Object object) {
        boolean result = false;
        Iterator<Entry<String, Collection<ObjectType>>> relations = edges.entrySet().iterator();
        while (relations.hasNext()) {
            Entry<String, Collection<ObjectType>> relation = relations.next();
            boolean modified = relation.getValue().remove(object);
            if (modified) {
                if (relation.getValue().isEmpty()) {
                    relations.remove();
                }
                result = modified;
            }
        }
        return result;
    }

    /**
     * Removes all objects linked by the given relation.
     *
     * @param relation
     *            relation to remove
     * @return the previously linked objects
     */
    @Override
    public Collection<ObjectType> removeAll(String relation) {
        return edges.remove(relation);
    }

    /**
     * Removes the first collection of elements of the enumerated elements.
     */
    @Override
    public void removeFirst() {
        first().ifPresent((pos) -> {
            String current = RDF.toURL(pos);
            edges.remove(current);
            String next;
            while (edges.containsKey(next = RDF.toURL(++pos))) {
                edges.put(current, edges.remove(next));
                current = next;
            }
        });
    }

    /**
     * Removes the first occurence of an object from the enumerated elements.
     *
     * @param object
     *            object to remove
     * @return if the collection was changed
     */
    @Override
    public boolean removeFirstOccurrence(Object object) {
        Indices range = range();
        if (range == null) {
            return false;
        }
        for (long i = range.first; i <= range.last; i++) {
            String current = RDF.toURL(i);
            Collection<ObjectType> objects = edges.get(current);
            if (objects.remove(object)) {
                if (objects.size() == 0) {
                    edges.remove(current);
                    long pos = i;
                    String next;
                    while (edges.containsKey(next = RDF.toURL(++pos))) {
                        edges.put(current, edges.remove(next));
                        current = next;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the last object from the enumerated elements.
     */
    @Override
    public void removeLast() {
        last().ifPresent((value) -> {
            edges.remove(RDF.toURL(value));
        });
    }

    /**
     * Removes the last occurence of an object from the enumerated elements.
     *
     * @param object
     *            object to remove
     * @return if the collection was changed
     */
    @Override
    public boolean removeLastOccurrence(Object object) {
        Indices range = range();
        if (range == null) {
            return false;
        }
        for (long i = range.last; i >= range.first; i--) {
            String current = RDF.toURL(i);
            Collection<ObjectType> objects = edges.get(current);
            if (objects.remove(object)) {
                if (objects.size() == 0) {
                    edges.remove(current);
                    long pos = i;
                    String next;
                    while (edges.containsKey(next = RDF.toURL(++pos))) {
                        edges.put(current, edges.remove(next));
                        current = next;
                    }
                }
                return true;
            }
        }
        return false;
    }

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
    @Override
    public Collection<ObjectType> replace(String relation, Set<ObjectType> objects) {
        return edges.put(relation, objects);
    }

    /**
     * Replaces all objects that are named nodes, but don’t have content, by
     * node references. A clean-up method for internal use.
     *
     * @param recursive
     *            if true, also invokes the function on all child nodes
     */
    void replaceAllNamedNodesWithNoDataByNodeReferences(boolean recursive) {
        for (Entry<String, Collection<ObjectType>> edge : edges.entrySet()) {
            Collection<ObjectType> objects = edge.getValue();
            LinkedList<NodeReference> replacedObjects = new LinkedList<>();
            Iterator<ObjectType> objectsIterator = objects.iterator();
            while (objectsIterator.hasNext()) {
                ObjectType object = objectsIterator.next();
                if ((object instanceof MemoryNamedNode) && ((NamedNode) object).isEmpty()) {
                    replacedObjects.add(new MemoryNodeReference(((NamedNode) object).getIdentifier()));
                    objectsIterator.remove();
                } else if (recursive && (object instanceof MemoryNode)) {
                    ((MemoryNode) object).replaceAllNamedNodesWithNoDataByNodeReferences(recursive);
                }
            }
            objects.addAll(replacedObjects);
        }
    }

    /**
     * Replaces an element index with a new element, removing all elements at
     * this index if there were several.
     *
     * @param index
     *            index of elements to replace
     * @param element
     *            new element for this index
     */
    @Override
    public void set(int index, ObjectType element) {
        edges.put(RDF.toURL(index), new HashSet<>(Arrays.asList(new ObjectType[] {element })));
    }

    /**
     * Sets a literal as only child of this node. Removes all other nodes, but
     * will keep attribute literals and node references.
     * <p>
     * This is a convenience method to <em>set text content</em> when building
     * XML-like data structure. All <em>tag content</em> will be removed, but
     * <em>attributes will be kept</em>.
     * {@code new Node(ns + "SomeType").setValue("value");} should be written to
     * XML as {@code <ns:SomeType>value</ns:SomeType>}.
     *
     * @param value
     *            literal value to set
     * @return this, for in-line use
     */
    public Node setValue(String value) {
        Iterator<Entry<String, Collection<ObjectType>>> outer = edges.entrySet().iterator();
        while (outer.hasNext()) {
            Entry<String, Collection<ObjectType>> nextOuter = outer.next();
            if (RDF.sequenceNumberOf(nextOuter.getKey()) != null) {
                outer.remove();
            } else {
                Collection<ObjectType> values = nextOuter.getValue();
                Iterator<ObjectType> inner = values.iterator();
                while (inner.hasNext()) {
                    if (inner.next() instanceof Node) {
                        inner.remove();
                    }
                }
                if (values.isEmpty()) {
                    outer.remove();
                }
            }
        }
        edges.put(RDF.toURL(FIRST_INDEX),
                new HashSet<>(Arrays.asList(new ObjectType[] {new MemoryLiteral(value, RDF.PLAIN_LITERAL) })));
        return this;
    }

    /**
     * Returns the number of elements in this node, or Intexer.MAX_VALUE if more
     * than Integer.MAX_VALUE.
     *
     * @return the number of elements in this node
     */
    @Override
    public int size() {
        long result = 0;
        for (Entry<String, Collection<ObjectType>> e : edges.entrySet()) {
            result += e.getValue().size();
            if (result > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            }
        }
        return (int) result;
    }

    /**
     * Converts this node to an RDFNode as part of a Jena model.
     *
     * @param model
     *            model to create objects in
     * @param addNamedNodesRecursively
     *            if true, named nodes will not be treated specially, if false,
     *            only their identifier will be added. If null, this node will
     *            be added if it is a named node, but named node children will
     *            only be referenced.
     * @return an RDFNode representing this node
     */
    @Override
    public RDFNode toRDFNode(Model model, Boolean addNamedNodesRecursively) {
        Resource subject = createRDFSubject(model);
        if (!(this instanceof NamedNode) || !Boolean.FALSE.equals(addNamedNodesRecursively)) {
            for (Entry<String, Collection<ObjectType>> entry : edges.entrySet()) {
                Property relation = model.createProperty(entry.getKey());
                for (ObjectType object : entry.getValue()) {
                    subject.addProperty(relation,
                            object.toRDFNode(model, Boolean.TRUE.equals(addNamedNodesRecursively)));
                }
            }
        }
        return subject;
    }

    /**
     * Returns a concise but informative textual representation of this node.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        toStringRecursive(result, 0);
        return result.toString();
    }

    /**
     * Pretty-prints this node into a StringBuilder. This method is called from
     * the toString function.
     *
     * @param out
     *            StringBuilder to print to
     * @param indent
     *            number of whitespaces to indent
     */
    private void toStringRecursive(StringBuilder out, int indent) {
        String spc = new String(new char[indent]).replace("\0", " ");

        TreeMap<Long, Collection<ObjectType>> elements = new TreeMap<>();
        TreeMap<String, Collection<ObjectType>> attributes = new TreeMap<>();

        if (this instanceof IdentifiableNode) {
            out.append(spc);
            out.append('[');
            out.append(((IdentifiableNode) this).getIdentifier());
            out.append("]\n");
        }

        for (Entry<String, Collection<ObjectType>> e : edges.entrySet()) {
            Long index = RDF.sequenceNumberOf(e.getKey());
            if (index == null) {
                attributes.put(e.getKey(), e.getValue());
            } else {
                elements.put(index, e.getValue());
            }
        }

        for (Entry<String, Collection<ObjectType>> x : attributes.entrySet()) {
            for (ObjectType y : x.getValue()) {
                out.append(spc);
                out.append(x.getKey());
                if (y instanceof MemoryNode) {
                    out.append(" {\n");
                    ((MemoryNode) y).toStringRecursive(out, indent + 2);
                    out.append(spc);
                    out.append("}\n");
                } else {
                    out.append(" = ");
                    out.append(y.toString());
                    out.append('\n');
                }
            }
        }

        for (Entry<Long, Collection<ObjectType>> x : elements.entrySet()) {
            for (ObjectType y : x.getValue()) {
                out.append(spc);
                out.append(RDF.toURL(x.getKey()));
                if (y instanceof MemoryNode) {
                    out.append(" {\n");
                    ((MemoryNode) y).toStringRecursive(out, indent + 2);
                    out.append(spc);
                    out.append("}\n");
                } else if (y instanceof IdentifiableNode) {
                    out.append(" = ");
                    out.append(((IdentifiableNode) y).getIdentifier());
                    out.append('\n');
                } else {
                    out.append(" = ");
                    out.append(y.toString());
                    out.append('\n');
                }
            }
        }

    }
}
