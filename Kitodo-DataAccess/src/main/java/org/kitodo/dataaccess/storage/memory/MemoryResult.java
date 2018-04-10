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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.kitodo.dataaccess.Literal;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.NodeReference;
import org.kitodo.dataaccess.ObjectType;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.Result;
import org.kitodo.dataaccess.XMLSchema;

/**
 * The results returned by a call to a getter on a node.
 */
public class MemoryResult extends HashSet<ObjectType> implements Result {
    /**
     * Constant defining an empty result.
     */
    static final MemoryResult EMPTY = new MemoryResult() {
        private static final String IMMUTABILITY = "The empty MemoryResult is immutable.";
        private static final long serialVersionUID = 1L;

        @Override
        public boolean add(ObjectType e) {
            throw new UnsupportedOperationException(IMMUTABILITY);
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException(IMMUTABILITY);
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException(IMMUTABILITY);
        }
    };

    private static final long serialVersionUID = 1L;

    /**
     * Calculate the initial capacity of the hash set.
     *
     * @param capacity
     *            current number of objects
     * @return the initial capacity of the hash set
     */
    private static int calculateCapacity(int capacity) {
        long result = (long) Math.ceil((capacity + 12) / 0.75);
        if (result > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if (result < 16) {
            return 16;
        }
        return (int) result;
    }

    /**
     * Parses a Jena model. Returns all nodes not referenced from anywhere (the
     * “top nodes”), or all nodes if there aren’t any “top nodes”.
     *
     * <p>
     * This method is {@code static final} because it is used from within a
     * constructor.
     *
     * @param model
     *            model to read out
     * @param alwaysAll
     *            if true, returns all nodes from the model, independent of
     *            whether there are “top nodes” or not
     * @return all nodes not referenced from anywhere, or really all nodes
     */
    private static final Collection<MemoryNode> parseModel(Model model, boolean alwaysAll) {
        HashMap<String, MemoryNode> result = new HashMap<>();
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
            entries.getValue().replaceAllMemoryNamedNodesWithNoDataByNodeReferences(false);
        }

        return alwaysAll || result.isEmpty() ? resolver.values() : result.values();
    }

    /**
     * Create an empty result.
     */
    MemoryResult() {
        super();
    }

    /**
     * Create a result with elements.
     *
     * @param c
     *            result elements
     */
    MemoryResult(Collection<? extends ObjectType> c) {
        super(c != null ? c : Collections.<ObjectType>emptyList());
    }

    /**
     * Create a result for the given amount of elements.
     *
     * @param capacity
     *            elements to be added
     */
    MemoryResult(int capacity) {
        super(calculateCapacity(capacity));
    }

    /**
     * Creates nodes from a Jena model. The memory result will directly
     * reference all nodes not referenced from anywhere else in the model (the
     * “top nodes”), or all nodes if there aren’t any “top nodes”.
     *
     * @param model
     *            model to read out
     * @param alwaysAll
     *            if true, the memory result directly references all nodes from
     *            the model, independent of whether there are “top nodes” or not
     */
    public MemoryResult(Model model, boolean alwaysAll) {
        super(parseModel(model, alwaysAll));
    }

    /**
     * Create a result with an element.
     *
     * @param element
     *            result element
     */
    MemoryResult(ObjectType element) {
        this(Arrays.asList(new ObjectType[] {element }));
    }

    @Override
    public long countUntil(long atLeastUntil, Class<?>... filterClasses) {
        if ((atLeastUntil == Integer.MAX_VALUE) && (filterClasses.length == 0)) {
            return super.size();
        }
        long result = 0;
        WITH_NEXT_ENTRY: for (ObjectType entry : this) {
            for (Class<?> clazz : filterClasses) {
                if (!clazz.isAssignableFrom(entry.getClass())) {
                    continue WITH_NEXT_ENTRY;
                }
            }
            result++;
            atLeastUntil--;
            if (atLeastUntil <= 0) {
                break;
            }
        }
        return result;
    }

    @Override
    public Set<String> leaves() {
        return strings(true);
    }

    @Override
    public String leaves(String separator) {
        return strings(separator, true);
    }

    @Override
    public Set<String> strings() {
        return strings(false);
    }

    /**
     * Returns all the literals as strings.
     *
     * @param allLeaves
     *            if true, references to other nodes are returned as well
     *
     * @return the literal
     */
    private Set<String> strings(boolean allLeaves) {
        HashSet<String> result = new HashSet<>((int) Math.ceil(super.size() / 0.75));
        for (ObjectType literal : this) {
            if (literal instanceof Literal) {
                result.add(((Literal) literal).getValue());
            } else if (allLeaves && (literal instanceof NodeReference)) {
                result.add(((NodeReference) literal).getIdentifier());
            }
        }
        return result;
    }

    @Override
    public String strings(String separator) {
        return strings(separator, false);
    }

    /**
     * Returns all literals from this node, joined by the given separator.
     *
     * @param separator
     *            separator to use
     * @param allLeaves
     *            return all leaves
     * @return all literals from this node as String
     */
    private String strings(String separator, boolean allLeaves) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (ObjectType literal : this) {
            if (literal instanceof Literal) {
                if (first) {
                    first = false;
                } else {
                    result.append(separator);
                }
                result.append(((Literal) literal).getValue());
            } else if (allLeaves && (literal instanceof NodeReference)) {
                if (first) {
                    first = false;
                } else {
                    result.append(separator);
                }
                result.append(((NodeReference) literal).getIdentifier());
            }
        }
        return result.toString();
    }

    @Override
    @SuppressWarnings("unchecked") // The compiler does not understand that
    // isAssignableFrom() does the type check
    public <T extends ObjectType> Set<T> subset(Class<T> returnClass, Class<?>... secondaryClasses) {
        Set<T> result = new HashSet<>();
        WITH_NEXT_ENTRY: for (ObjectType entry : this) {
            if (returnClass.isAssignableFrom(entry.getClass())) {
                for (Class<?> clazz : secondaryClasses) {
                    if (!clazz.isAssignableFrom(entry.getClass())) {
                        continue WITH_NEXT_ENTRY;
                    }
                }
                result.add((T) entry);
            }
        }
        return result;
    }

}
