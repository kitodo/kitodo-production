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
 * Just like Java’s collections API, Kitodo - Data Access makes strong use of
 * interfaces. To do the business, an <em>implementation</em> of the interface
 * is required. The memory storage implementation is the default implementation
 * of the Kitodo Data Access data access API.
 *
 * <p>
 * <strong>Creating Java objects</strong>
 *
 * <p>
 * Creating Java objects using the memory storage is much like you are used to
 * from Java’s Collections API.
 *
 * <p>
 * Compare:
 *
 * <pre>
 * Map m = new HashMap(); // Map: interface, HashMap: class
 * Node n = new MemoryNode(); // Node: interface, MemoryNode: class
 * </pre>
 *
 * <p>
 * <em>Implementation object hierarchy:</em>
 *
 * <pre>
 *                                      &lt; ObjectType &gt;
 *                                        /       \
 *                             &lt; NodeType &gt;       &lt; AccessibleObject &gt;
 *                             /          \      /                  \
 *             &lt; IdentifiableNode &gt;       { Node }                &lt; Literal &gt;
 *               /              \            |                      /      \
 *    &lt; NodeReference &gt;          \     [ MemoryNode ]   &lt; LangString &gt;   [ MemoryLiteral ]
 *            |                   \        /                       \           /
 * [ MemoryNodeReference ]   [ MemoryNamedNode ]               [ MemoryLangString ]
 * </pre>
 *
 * <small>&lt; &gt; − Interface<br>
 * { } − abstract class<br>
 * [ ] − class</small>
 */
package org.kitodo.dataaccess.storage.memory;
