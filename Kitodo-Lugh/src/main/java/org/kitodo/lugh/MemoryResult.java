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

import java.util.*;

/**
 * The results returned by a call to a getter on a node.
 */
public class MemoryResult extends HashSet<ObjectType> implements Result {
    /**
     * Constant defining an empty result.
     */
    static final Result EMPTY = new MemoryResult() {
        private static final String IMMUTABILITY = "The empty Result is immutable.";
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
    private static int cap(int capacity) {
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
     * Create an empty result.
     */
    MemoryResult() {
        super();
    }

    /**
     * Create a result with elements.
     *
     * @param arg0
     *            result elements
     */
    MemoryResult(Collection<? extends ObjectType> arg0) {
        super(arg0 != null ? arg0 : Collections.<ObjectType>emptyList());
    }

    /**
     * Create a result for the given amount of elements.
     *
     * @param capacity
     *            elements to be added
     */
    MemoryResult(int capacity) {
        super(cap(capacity));
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

    /**
     * Count the elements in this result that can be cast to the given class, at
     * least up to the given limit. If there are more elements, the function may
     * or may not return a value equal to or larger than limit.
     *
     * @param clazz
     *            object class to count
     * @param limit
     *            limit to count up to, at least
     * @return the number of elements in this result that can be cast to the
     *         given class
     */
    @Override
    public long count(Class<? extends ObjectType> clazz, int limit) {
        if (ObjectType.class.equals(clazz)) {
            return super.size();
        }
        long result = 0;
        for (ObjectType entry : this) {
            if (clazz.isAssignableFrom(entry.getClass())) {
                result++;
                limit--;
            }
            if (limit <= 0) {
                break;
            }
        }
        return result;
    }

    /**
     * Returns all leaves from this node, joined by the given separator. Leaves
     * are literals or node references’ URIs.
     *
     * @return all literals from this node as String
     */
    @Override
    public Set<String> leaves() {
        return strings(true);
    }

    /**
     * Returns all leaves from this node, joined by the given separator. Leaves
     * are literals or node references’ URIs.
     *
     * @param separator
     *            separator to use
     * @return all literals from this node as String
     */
    @Override
    public String leaves(String separator) {
        return strings(separator, true);
    }

    /**
     * Returns all the literals as strings. References to other nodes are not
     * returned.
     *
     * @return the literal
     */
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

    /**
     * Returns all literals from this node, joined by the given separator.
     *
     * @param separator
     *            separator to use
     * @return all literals from this node as String
     */
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

    /**
     * Creates a subset by type check. Used for filtering.
     *
     * @param clazz
     *            Subclass to create a subset of
     * @param <T>
     *            subclass type
     * @return a subset of a given type
     */
    @Override
    @SuppressWarnings("unchecked") // The compiler does not understand that
                                   // isAssignableFrom() does the type check
    public <T extends ObjectType> Set<T> subset(Class<T> clazz) {
        Set<T> result = new HashSet<>();
        for (ObjectType entry : this) {
            if (clazz.isAssignableFrom(entry.getClass())) {
                result.add((T) entry);
            }
        }
        return result;
    }

}
