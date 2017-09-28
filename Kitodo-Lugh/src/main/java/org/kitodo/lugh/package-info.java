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

/**
 * Lugh is a library to handle Linked Data in Java in the way well-known from
 * Java’s collections interface. Linked Data objects are multimaps. A multimap
 * is a {@code Map} which can take several values for each key
 * ({@code Map<K, Collection<V>>}). In Lugh, they are represented by the
 * {@link Node} interface.
 * <p>
 * A node can either be an <em>anonymous</em> node or have a unique name, which
 * makes it a {@link NamedNode}. The name must be an URI. (URIs comprise URLs
 * and comparable identifiers, such as URN.) The other way round, an URI
 * ({@link IdentifiableNode}) can either be a node of the current data set
 * ({@code instanceof NamedNode}), or just a link to anything else
 * ({@link NodeReference}).
 * <p>
 * One particularity of Linked Data is that the <em>keys</em> <strong>must
 * be</strong> URIs. (However, we use plain old {@code String}s to store them.)
 * <p>
 * The <em>values</em> that can be associated with a node can either be nodes,
 * or literals. The property “either nodes or literals” is represented by the
 * {@link ObjectType} interface. Literals cannot immediately be identified by an
 * URI.
 * <p>
 * The {@link Literal} interface is intended to be used to represent technical
 * data ({@code int}s, {@code String}s, …). For literals representing
 * human-readable text, there is the specialized subclass {@link LangString}
 * which takes the language as an additional attribute.
 * <p>
 * The {@link AccessibleObject} interface represents both nodes and literals
 * that are part of the current data set, but not {@link NodeReference}s.
 * <p>
 * Interface hierarchy:
 *
 * <pre>
 *                        ObjectType
 *                         /     \
 *                   NodeType    AccessibleObject
 *                  /        \  /        |
 *       IdentifiableNode    Node     Literal
 *         /          \     /            |
 * NodeReference    NamedNode        LangString
 * </pre>
 * <p>
 * <strong>Types</strong>
 * <p>
 * Linked data objects can be typed. Types must be understood as
 * {@link interface}s rather than classes. A node can have no type at all, as
 * well as it can have several types.
 * <p>
 * <strong>List support</strong>
 * <p>
 * Linked Data nodes are multimaps, but they can emulate lists by means of
 * {@code rdfs:ContainerMembershipProperty}. For convenience, Lugh provides
 * well-known list API functions, such as {@code add()} or
 * {@code get(long index)}, which handle the index management internally.
 * <p>
 * <strong>Creating Java objects</strong>
 * <p>
 * Just like Java’s collections API, Lugh makes strong use of interfaces. To
 * create an object, an <em>implementation</em> of the interface is required.
 * Lugh ships with a memory-based implementation.
 * <p>
 * Compare:
 *
 * <pre>
 * Map m = new HashMap(); // Map: interface, HashMap: class
 * Node n = new MemoryNode(); // Node: interface, MemoryNode: class
 * </pre>
 * <p>
 * <strong>Reading from Linked Data objects</strong>
 * <p>
 * Because a node is a {@code Map<K, Collection<ObjectType>>}, the result of a
 * get operation is a collection of {@code ObjectType}. For convenience, the
 * collection is wrapped in a {@link Result} which provides getters for the
 * different {@code nodes()} or {@code literals()}, which provide the casting.
 * In principle, it is better to use the set getters and iterate over their
 * outcome, if that is meaningfully possible, even if—at the moment—only one
 * result object is expected.
 *
 * <pre>
 * for (Node n : node.get(whatever).nodes()) {
 *     // do something with ‘n’
 * }
 * </pre>
 * 
 * However, in some cases, only exactly one object or literal is expected. To
 * provide for this situation, there are also getters for just this purpose
 * (i.e. {@code node()}, {@code literal()}, …). These getters will throw an
 * exception if there is no object, or if there are several objects.
 * <p>
 * Exception hierarchy:
 *
 * <pre>
 *        ( java.lang.Exception )
 *                  |
 *          LinkedDataException
 *             /          \
 * NoDataException    AmbiguousDataException
 * </pre>
 * <p>
 * <strong>Exception management</strong>
 * <p>
 * In case the code cannot deal with the exception at that point, the exception
 * should be declared and handled by the caller. However, sometimes a decision
 * has already been made upon checking the presence of certain information. In
 * that case <strong>and only in that case</strong> the …{@code expectable()}
 * getters can and should be used. These getters will throw unchecked exceptions
 * ({@code NoSuchElementException} or {@code BufferOverflowException}) instead.
 * <p>
 * Compare:
 *
 * <pre>
 * Node n = node.get(whatever).node(); // throws checked exception
 *
 * Result r = node.get(whatever);
 * if(r.isUniqueNode()) {
 *     Node n = r.nodeExpectable();    // throws unchecked exception
 * </pre>
 * <p>
 * <strong>The name</strong>
 * <p>
 * The name “Lugh” also refers to an Irish god that takes the role of a youthful
 * warrior hero in the pantheon known as Tuatha Dé Danann.
 * <p>
 *
 * @see "https://en.wikipedia.org/wiki/Linked_data"
 *
 * @author Matthias Ronge
 */
package org.kitodo.lugh;
