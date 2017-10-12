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

/**
 * An anonymous linked data Node. The most nodes are anonymous. In Java, they
 * are identified by the variable that holds them. In the data, they are often
 * identified by a certain value held by a certain relation. Therefore the short
 * class name “Node” was used for the anonymous node class, and not the name
 * “AnonymousNode”.
 * <p>
 * The class offers functions known from Java’s collections, and they are
 * intended to intuitively behave the same way.
 */
public class MemoryNode extends Node {

    /**
     * Iterator to iterate through the edges and return all directly referenced
     * nodes. Leaves that are not nodes are not returned.
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
    public MemoryNode() {
    }

    /**
     * Create a node with a type attribute set.
     *
     * @param type
     *            node type
     */
    public MemoryNode(IdentifiableNode type) {
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
    public MemoryNode(String type) {
        if (type != null) {
            assert URI_SCHEME.matcher(type).find() : "Type isn’t a valid URI.";
            edges.put(RDF.TYPE.getIdentifier(),
                    new LinkedList<>(Arrays.asList(new ObjectType[] {new MemoryNodeReference(type) })));
        }
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked") // this is always instanceof Node
    @Override
    public <T extends Node> T add(ObjectType element) {
        String key = RDF.toURL(last().orElse((long) FIRST_INDEX - 1) + 1);
        edges.put(key, new LinkedList<>(Arrays.asList(new ObjectType[] {element })));
        return (T) this;
    }

    /** {@inheritDoc} */
    @Override
    public void addAll(Collection<? extends ObjectType> elements) {
        Long pos = last().orElse((long) FIRST_INDEX - 1);
        for (ObjectType element : elements) {
            edges.put(RDF.toURL(++pos), new HashSet<>(Arrays.asList(new ObjectType[] {element })));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addFirst(ObjectType element) {
        add(FIRST_INDEX, element);
    }

    /** {@inheritDoc} */
    @Override
    public ObjectType asUnordered(boolean removeType) {
        Node result = this instanceof IdentifiableNode ? new MemoryNamedNode(((IdentifiableNode) this).getIdentifier())
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

                        catch (NoSuchElementException | BufferOverflowException e) {
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

    /** {@inheritDoc} */
    @Override
    public boolean contains(Object o) {
        for (Entry<String, Collection<ObjectType>> k : edges.entrySet()) {
            if (k.getValue().contains(o)) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean containsKey(String key) {
        return edges.containsKey(key);
    }

    /**
     * Creates an anonymous resource. Overridden in
     * {@link MemoryNamedNode#createRDFSubject(Model)} to create a named
     * resource.
     *
     * @param model
     *            model to create the resource in
     * @return the created resource
     */
    protected Resource createRDFSubject(Model model) {
        return model.createResource();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public MemoryResult find(Node graphPath) {
        return (MemoryResult) GraphPath.apply(new MemoryResult(this), graphPath);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Long> first() {
        Indices range = range();
        return range == null ? Optional.empty() : Optional.of(range.first);
    }

    /** {@inheritDoc} */
    @Override
    public MemoryResult get(Collection<String> relations, Collection<ObjectType> conditions) {
        MemoryResult result = new MemoryResult();
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

    /** {@inheritDoc} */
    @Override
    public MemoryResult get(long index) {
        return new MemoryResult(get(RDF.toURL(index)));
    }

    /** {@inheritDoc} */
    @Override
    public MemoryResult get(String relation) {
        return new MemoryResult(edges.get(relation));
    }

    /** {@inheritDoc} */
    @Override
    public MemoryResult get(String relation, IdentifiableNode identifierRelation, ObjectType identifierValue) {
        return get(relation != null ? Arrays.asList(new String[] {relation }) : null,
                Arrays.asList(new ObjectType[] {new MemoryNode().put(identifierRelation, identifierValue) }));
    }

    /** {@inheritDoc} */
    @Override
    public MemoryResult get(String relation, String type) {
        Collection<ObjectType> objects = edges.get(relation);
        MemoryResult result = new MemoryResult(objects.size());
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

    /** {@inheritDoc} */
    @Override
    public IdentifiableNode getByIdentifier(String identifier) {
        MemoryResult found = new MemoryResult();
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

    /** {@inheritDoc} */
    @Override
    public MemoryResult getByType(String type) {
        MemoryResult found = new MemoryResult();
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

    /** {@inheritDoc} */
    @Override
    public MemoryResult getByType(String rdfType, String idType, String idValue) {
        MemoryResult result = new MemoryResult();
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public MemoryResult getFirst() {
        Optional<Long> first = first();
        if (first.isPresent()) {
            return get(first.orElseThrow(() -> {
                return new IllegalStateException();
            }));
        } else {
            return MemoryResult.EMPTY;
        }
    }

    /** {@inheritDoc} */
    @Override
    public MemoryResult getLast() {
        Optional<Long> last = last();
        if (last.isPresent()) {
            return get(last.orElseThrow(() -> {
                return new IllegalStateException();
            }));
        } else {
            return MemoryResult.EMPTY;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Set<String> getRelations() {
        return edges.keySet();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public boolean hasType(String type) {
        for (IdentifiableNode identifiableNode : get(RDF.TYPE).identifiableNodes()) {
            if (type.equals(identifiableNode.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return edges.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public Iterator<ObjectType> iterator() {
        return new ChildNodeIterator();
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Long> last() {
        Indices range = range();
        return range == null ? Optional.empty() : Optional.of(range.last);
    }

    /** {@inheritDoc} */
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
            Collection<ObjectType> conditions = new LinkedList<>();
            ((Node) condition).get(relation).forEach(conditions::add);
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked") // this is always instanceof Node
    @Override
    public <T extends Node> T put(String relation, String object) {
        this.put(relation, MemoryLiteral.createLeaf(object, null));
        return (T) this;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public Collection<ObjectType> removeAll(String relation) {
        return edges.remove(relation);
    }

    /** {@inheritDoc} */
    @Override
    public void removeFirst() {
        first().ifPresent(pos -> {
            String current = RDF.toURL(pos);
            edges.remove(current);
            String next;
            while (edges.containsKey(next = RDF.toURL(++pos))) {
                edges.put(current, edges.remove(next));
                current = next;
            }
        });
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void removeLast() {
        last().ifPresent((value) -> {
            edges.remove(RDF.toURL(value));
        });
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
                if ((object instanceof MemoryNamedNode) && ((MemoryNamedNode) object).isEmpty()) {
                    replacedObjects.add(new MemoryNodeReference(((MemoryNamedNode) object).getIdentifier()));
                    objectsIterator.remove();
                } else if (recursive && (object instanceof MemoryNode)) {
                    ((MemoryNode) object).replaceAllNamedNodesWithNoDataByNodeReferences(recursive);
                }
            }
            objects.addAll(replacedObjects);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void set(int index, ObjectType element) {
        edges.put(RDF.toURL(index), new HashSet<>(Arrays.asList(new ObjectType[] {element })));
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("unchecked") // this is always instanceof Node
    public <T extends Node> T setValue(String value) {
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
        return (T) this;
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public RDFNode toRDFNode(Model model, Boolean addNamedNodesRecursively) {
        Resource subject = createRDFSubject(model);
        if (!(this instanceof IdentifiableNode) || !Boolean.FALSE.equals(addNamedNodesRecursively)) {
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
