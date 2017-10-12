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

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

/** Tests {@code org.kitodo.lugh.MemoryResult}. */
public class MemoryResultTest {

    /** Tests {@code HashSet.add(ObjectType e)}. */
    @Test
    public void testAddObjectType() {
        MemoryResult r = new MemoryResult();

        assertEquals(0, r.size());

        r.add(new MemoryNode(Mets.METS_HDR));

        assertEquals(1, r.size());
    }

    /** Tests {@code HashSet.clear()}. */
    @Test
    public void testClear() {
        MemoryResult r = new MemoryResult();
        r.add(Mets.ADMID);
        r.add(Mods.EXTENT);
        r.clear();
        assertEquals(0, r.size());
    }

    /** Tests {@code count(Class<? extends ObjectType>, int)}. */
    @Test
    public void testCount() {
        fail("Not yet implemented.");
    }

    /** Tests {@code subset(Class<T>)}. */
    @Test
    public void testIdentifiableNodes() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        Set<IdentifiableNode> expected = new HashSet<>();
        expected.add(new MemoryNamedNode("http://example.org/foo"));
        expected.add(new MemoryNodeReference("http://example.org/bar"));

        assertEquals(expected, r.subset(IdentifiableNode.class));
    }

    /** Tests {@code leaves()}. */
    @Test
    public void testLeaves() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        Set<String> expected = new HashSet<>();
        expected.add("http://example.org/bar");
        expected.add("Hello world!");

        assertEquals(expected, r.leaves());
    }

    /** Tests {@code leaves(String)}. */
    @Test
    public void testLeavesString() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        Set<String> expectedOneOf = new HashSet<>();
        expectedOneOf.add("http://example.org/bar ; Hello world!");
        expectedOneOf.add("Hello world! ; http://example.org/bar");

        assertTrue(expectedOneOf.contains(r.leaves(" ; ")));
    }

    /** Tests {@code MemoryResult()} constructor. */
    @Test
    public void testMemoryResult() {
        MemoryResult r = new MemoryResult();
        assertEquals(0, r.size());
    }

    /**
     * Tests {@code MemoryResult(Collection<? extends ObjectType>)} constructor.
     */
    @Test
    public void testMemoryResultCollectionOfQextendsObjectType() {
        MemoryResult other = new MemoryResult();
        other.add(new MemoryNode(Mets.METS_HDR));
        other.add(Mods.IDENTIFIER);

        MemoryResult r = new MemoryResult(other);
        assertEquals(other.size(), r.size());
        assertEquals(other, r);
    }

    /** Tests {@code MemoryResult(int)} constructor. */
    @Test
    public void testMemoryResultInt() {
        MemoryResult r = new MemoryResult(42);
        assertEquals(0, r.size());
    }

    /** Tests {@code MemoryResult(ObjectType)} constructor. */
    @Test
    public void testMemoryResultObjectType() {
        MemoryResult expected = new MemoryResult();
        expected.add(new MemoryNode(Mets.METS_HDR));

        MemoryResult r = new MemoryResult(new MemoryNode(Mets.METS_HDR));

        assertEquals(expected.size(), r.size());
        assertEquals(expected, r);
    }

    /** Tests {@code subset(Class<T>)}. */
    @Test
    public void testNodes() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        Set<Node> expected = new HashSet<>();
        expected.add(new MemoryNamedNode("http://example.org/foo"));
        expected.add(new MemoryNode("http://example.org/baz"));

        assertEquals(expected, r.subset(Node.class));
    }

    /** Tests {@code strings()}. */
    @Test
    public void testStrings() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));
        r.add(MemoryLiteral.createLeaf("public static void main(String[] args)", null));

        Set<String> expected = new HashSet<>();
        expected.add("Hello world!");
        expected.add("public static void main(String[] args)");

        assertEquals(expected, r.strings());
    }

    /** Tests {@code strings(String)}. */
    @Test
    public void testStringsString() {
        fail("Not yet implemented.");
    }

}
