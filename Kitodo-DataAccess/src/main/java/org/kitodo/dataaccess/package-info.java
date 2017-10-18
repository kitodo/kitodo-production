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
 * Kitodo - Data Access is a library to handle Linked Data in Java in the way
 * well-known from Java’s collections interface. Linked Data objects are
 * multimaps. A multimap is a {@code Map} which can take several values for each
 * key ({@code Map<K, Collection<V>>}). In Kitodo - Data Access, they are
 * represented by the {@link org.kitodo.dataaccess.Node} interface.
 *
 * <p>
 * A node can either be an <em>anonymous</em> node or have a unique name, which
 * makes it a named node ({@code <T extends Node & IdentifiableNode>}). The name
 * must be an URI. (URIs comprise URLs and comparable identifiers, such as URN.)
 * The other way round, an URI ({@link org.kitodo.dataaccess.IdentifiableNode})
 * can either be a node of the current data set ({@code instanceof Node}), or
 * just a link to anything else ({@code instanceof}
 * {@link org.kitodo.dataaccess.NodeReference}).
 *
 * <p>
 * One particularity of Linked Data is that the <em>keys</em> <strong>must
 * be</strong> URIs. (However, we use plain old {@code String}s to store them.)
 *
 * <p>
 * The <em>values</em> that can be associated with a node can either be nodes,
 * or literals. The property “either nodes or literals” is represented by the
 * {@link org.kitodo.dataaccess.ObjectType} interface. Literals cannot
 * immediately be identified by an URI.
 *
 * <p>
 * The {@link org.kitodo.dataaccess.Literal} interface is intended to be used to
 * represent technical data ({@code int}s, {@code String}s, …). For literals
 * representing human-readable text, there is the specialized subclass
 * {@link org.kitodo.dataaccess.LangString} which takes the language as an
 * additional attribute.
 *
 * <p>
 * The {@link org.kitodo.dataaccess.AccessibleObject} interface represents both
 * nodes and literals that are part of the current data set, but not
 * {@link org.kitodo.dataaccess.NodeReference}s.
 *
 * <p>
 * <em>Interface hierarchy:</em>
 *
 * <pre>
 *                              &lt; ObjectType &gt;
 *                                 /      \
 *                       &lt; NodeType &gt;    &lt; AccessibleObject &gt;
 *                        /         \    /         |
 *         &lt; IdentifiableNode &gt;    { Node }    &lt; Literal &gt;
 *            /            \        /              |
 * &lt; NodeReference &gt;    (( named node ))     &lt; LangString &gt;
 * </pre>
 *
 * <small>&lt; &gt; − Interface<br>
 * { } − abstract class<br>
 * (( )) − concept; no equivalent in code</small>
 *
 * <p>
 * <strong>Types</strong>
 *
 * <p>
 * Linked data objects can be typed. Types must be understood as
 * {@link interface}s rather than classes. A node can have no type at all, as
 * well as it can have several types.
 *
 * <p>
 * <strong>List support</strong>
 *
 * <p>
 * Linked Data nodes are multimaps, but they can emulate lists by means of
 * {@code rdfs:ContainerMembershipProperty}. For convenience, Kitodo - Data
 * Access provides well-known list API functions, such as {@code add()} or
 * {@code get(long index)}, which handle the index management internally.
 *
 * <p>
 * <strong>Creating Java objects</strong>
 *
 * <p>
 * Just like Java’s collections API, Kitodo - Data Access makes strong use of
 * interfaces. To create an object, an <em>implementation</em> of the interface
 * is required. Kitodo - Data Access ships with a memory-based implementation
 * ({@link org.kitodo.dataaccess.storage.memory}) which you can use to create
 * objects that reside on the JVM’s heap. *
 *
 * <p>
 * <em>Storage-implementation-agnostically creating Java objects</em>
 *
 * <p>
 * It is also possible to write code that creates objects without knowledge of
 * the storage implementation by using the {@link org.kitodo.dataaccess.Storage}
 * interface:
 *
 * <pre>
 * function createAnEmptyMetsDiv(Storage storage) {
 *     return storage.createNode(Mets.DIV);
 * }
 * </pre>
 *
 * <p>
 * The function can later be called with the implementation to work with:
 *
 * <pre>
 * Node div = createAnEmptyMetsDiv(MemoryStorage.INSTANCE);
 * </pre>
 *
 * <p>
 * <strong>Reading from Linked Data objects</strong>
 *
 * <p>
 * Because a node is a {@code Map<K, Collection<ObjectType>>}, the result of a
 * get operation is a collection of {@code ObjectType}. For convenience, the
 * collection is wrapped in a {@link org.kitodo.dataaccess.Result} which
 * provides getters for the different {@code nodes()} or {@code literals()},
 * which provide the casting. In principle, it is better to use the set getters
 * and iterate over their outcome, if that is meaningfully possible, even if—at
 * the moment—only one result object is expected. Since the result set does not
 * provide a particular order, a
 * {@linkplain java.util.Collection#parallelStream()} is used.
 *
 * <pre>
 * node.get(whatever).forEachNode(n -> {
 *     // do something with ‘n’
 * });
 * </pre>
 *
 * <p>
 * However, in some cases, only exactly one object or literal is expected. To
 * provide for this situation, there are also getters for just this purpose
 * (i.e. {@code node()}, {@code literal()}, …). These getters will throw an
 * exception if there is no object, or if there are several objects.
 *
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
 *
 * <p>
 * <strong>Exception management</strong>
 *
 * <p>
 * In case the code cannot deal with the exception at that point, the exception
 * should be declared and handled by the caller. However, sometimes a decision
 * has already been made upon checking the presence of certain information. In
 * that case <strong>and only in that case</strong> the …{@code expectable()}
 * getters can and should be used. These getters will throw unchecked exceptions
 * ({@code NoSuchElementException} or {@code BufferOverflowException}) instead.
 *
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
 *
 * <p>
 * <strong>Reading and writing Linked Data files</strong>
 *
 * <p>
 * Kitodo - Data Access uses the Apache Jena framework to read and write common
 * Linked Data formats such as RDF-XML or Turtle.
 *
 * <p>
 * <em>Loading a file:</em>
 *
 * <pre>
 * Model model = ModelFactory.createDefaultModel();
 * model.read("data.ttl");
 * Result result = new MemoryResult(model, false);
 * </pre>
 *
 * <p>
 * A data file may contain unrelated nodes, thus a load operation creates a
 * {@code Result} that must be queried. In the common case that the result only
 * contains one top node, it can be accessed using
 * {@code Node node = new MemoryResult(model, false).node();}.
 *
 * <p>
 * <em>Saving a file:</em>
 *
 * <pre>
 * SerializationFormat.RDF_XML_ABBREV.write(node, new Namespaces(), new File("out.xml"));
 * </pre>
 *
 * <p>
 *
 * @see "https://en.wikipedia.org/wiki/Linked_data"
 *
 * @author Matthias Ronge
 */
package org.kitodo.dataaccess;
