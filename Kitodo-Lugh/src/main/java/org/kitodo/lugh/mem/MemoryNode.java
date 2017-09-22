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

package org.kitodo.lugh.mem;

import java.nio.BufferOverflowException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.*;
import org.kitodo.lugh.*;
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
     * The index of the first child node that is referenced by index.
     */
    public static final short FIRST_INDEX = 1;

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
    MemoryNode(NodeReference type) {
        if (type != null) {
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
                    new LinkedList<>(Arrays.asList(new ObjectType[] {MemoryLiteral.create(type, null) })));
        }
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
    @Override
    public MemoryNode add(ObjectType element) {
        Long last = last();
        String key = RDF.toURL(last != null ? last + 1 : FIRST_INDEX);
        edges.put(key, new LinkedList<>(Arrays.asList(new ObjectType[] {element })));
        return this;
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
     * Gets a literal referenced by relation.
     *
     * @param relation
     *            relation to look up
     * @return the value of the literal.
     */
    @Override
    public Result get(NodeReference relation) {
        return get(relation.getIdentifier());
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
     * Returns the last (largest) index in use to reference elements by index,
     * or {@code null} if there is no such.
     *
     * @return the largest index in use
     */
    @Override
    public Long last() {
        Indices range = range();
        return range == null ? null : range.last;
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
     * @return whether this node fulfils the set of conditions
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
    @Override
    public MemoryNode put(NodeReference relation, ObjectType object) {
        put(relation.getIdentifier(), object);
        return this;
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
    @Override
    public MemoryNode put(NodeReference relation, String object) {
        put(relation.getIdentifier(), object);
        return this;
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
    @Override
    public MemoryNode put(String relation, ObjectType object) {
        if (relation.equals(RDF.ABOUT.getIdentifier())) {
            throw new IllegalArgumentException(
                    "Forbidden to put http://www.w3.org/1999/02/22-rdf-syntax-ns#about entries. Use new NamedNode(identityURL [, data]); instead.");
        }
        if (edges.containsKey(relation)) {
            edges.get(relation).add(object);
        } else {
            edges.put(relation, new LinkedList<>(Arrays.asList(new ObjectType[] {object })));
        }
        return this;
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
    @Override
    public MemoryNode put(String relation, String object) {
        this.put(relation, MemoryLiteral.create(object, null));
        return this;
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
     * Creates a Jena model from this node.
     *
     * @return a Jena model representing this node
     */
    @Override
    public Model toModel() {
        Model model = ModelFactory.createDefaultModel();
        toModel(model);
        return model;
    }

    /**
     * Adds this node to an existing Jena model.
     *
     * @param model
     *            a Jena model to add this node to
     */
    @Override
    public void toModel(Model model) {
        Resource subject = createRDFSubject(model);
        for (Entry<String, Collection<ObjectType>> ent : edges.entrySet()) {
            Property relation = model.createProperty(ent.getKey());
            for (ObjectType object : ent.getValue()) {
                subject.addProperty(relation, object.toRDFNode(model));
            }
        }
    }

    /**
     * Converts this node to an RDFNode as part of a Jena model.
     *
     * @param model
     *            model to create objects in
     * @return an RDFNode representing this node
     */
    @Override
    public RDFNode toRDFNode(Model model) {
        Resource subject = createRDFSubject(model);
        for (Entry<String, Collection<ObjectType>> entry : edges.entrySet()) {
            Property relation = model.createProperty(entry.getKey());
            for (ObjectType object : entry.getValue()) {
                subject.addProperty(relation, object.toRDFNode(model));
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
                } else if (y instanceof MemoryNodeReference) {
                    out.append(" = ");
                    out.append(((NodeReference) y).getIdentifier());
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
