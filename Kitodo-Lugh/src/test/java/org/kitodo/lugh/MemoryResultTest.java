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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.BufferOverflowException;
import java.util.*;

import org.junit.Test;
import org.kitodo.lugh.vocabulary.*;

public class MemoryResultTest {

    @Test
    public void testAddObjectType() {
        MemoryResult r = new MemoryResult();

        assertEquals(0, r.size());

        r.add(new MemoryNode(Mets.METS_HDR));

        assertEquals(1, r.size());
    }

    @Test
    public void testClear() {
        MemoryResult r = new MemoryResult();
        r.add(Mets.ADMID);
        r.add(Mods.EXTENT);
        r.clear();
        assertEquals(0, r.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIdentifiableNodeExpectable0() {
        MemoryResult r = new MemoryResult();

        r.identifiableNodeExpectable();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIdentifiableNodeExpectable0ButLiteral() {
        MemoryResult r = new MemoryResult(MemoryLiteral.createLiteral("Hello world!", "en"));

        r.identifiableNodeExpectable();
    }

    @Test
    public void testIdentifiableNodeExpectable1MemoryNamedNode() {
        IdentifiableNode expected;
        MemoryResult r = new MemoryResult(expected = new MemoryNamedNode("http://example.org/fooBar"));

        assertEquals(expected, r.identifiableNodeExpectable());
    }

    @Test
    public void testIdentifiableNodeExpectable1MemoryNodeReference() {
        IdentifiableNode expected;
        MemoryResult r = new MemoryResult(expected = new MemoryNodeReference("http://example.org/fooBar"));

        assertEquals(expected, r.identifiableNodeExpectable());
    }

    @Test(expected = BufferOverflowException.class)
    public void testIdentifiableNodeExpectable2() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNamedNode("http://example.org/bar"));

        r.identifiableNodeExpectable();
    }

    @Test(expected = BufferOverflowException.class)
    public void testIdentifiableNodeExpectable2WhereOneIsALiteral() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        r.identifiableNodeExpectable();
    }

    @Test(expected = BufferOverflowException.class)
    public void testIdentifiableNodeExpectable2WhereOneIsIdentifiable() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNode("http://example.org/bar"));

        r.identifiableNodeExpectable();
    }

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

        assertEquals(expected, r.identifiableNodes());
    }

    @Test
    public void testIsUniqueIdentifiableNode0() {
        MemoryResult r = new MemoryResult();

        assertFalse(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode0ButLiteral() {
        MemoryResult r = new MemoryResult(MemoryLiteral.createLiteral("Hello world!", "en"));

        assertFalse(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode1MemoryNamedNode() {
        MemoryResult r = new MemoryResult(new MemoryNamedNode("http://example.org/fooBar"));

        assertTrue(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode1MemoryNodeReference() {
        MemoryResult r = new MemoryResult(new MemoryNodeReference("http://example.org/fooBar"));

        assertTrue(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode2() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNamedNode("http://example.org/bar"));

        assertFalse(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode2WhereOneIsALiteral() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        assertFalse(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode2WhereOneIsIdentifiable() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNode("http://example.org/bar"));

        assertFalse(r.isUniqueIdentifiableNode());
    }

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

    @Test
    public void testMemoryResult() {
        MemoryResult r = new MemoryResult();
        assertEquals(0, r.size());
    }

    @Test
    public void testMemoryResultCollectionOfQextendsObjectType() {
        MemoryResult other = new MemoryResult();
        other.add(new MemoryNode(Mets.METS_HDR));
        other.add(Mods.IDENTIFIER);

        MemoryResult r = new MemoryResult(other);
        assertEquals(other.size(), r.size());
        assertEquals(other, r);
    }

    @Test
    public void testMemoryResultInt() {
        MemoryResult r = new MemoryResult(42);
        assertEquals(0, r.size());
    }

    @Test
    public void testMemoryResultObjectType() {
        MemoryResult expected = new MemoryResult();
        expected.add(new MemoryNode(Mets.METS_HDR));

        MemoryResult r = new MemoryResult(new MemoryNode(Mets.METS_HDR));

        assertEquals(expected.size(), r.size());
        assertEquals(expected, r);
    }

    @Test(expected = NoDataException.class)
    public void testNode0Empty() throws LinkedDataException {
        MemoryResult r = new MemoryResult();
        r.node();
    }

    @Test(expected = NoDataException.class)
    public void testNode0LiteralsOnly() throws LinkedDataException {
        MemoryResult r = new MemoryResult();
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));
        r.node();
    }

    @Test
    public void testNode1MemoryNamedNode() throws LinkedDataException {
        MemoryResult r = new MemoryResult();
        MemoryNamedNode expected;
        r.add(expected = new MemoryNamedNode("http://example.org/foo"));
        assertEquals(expected, r.node());
    }

    @Test
    public void testNode1Node() throws LinkedDataException {
        MemoryResult r = new MemoryResult();
        Node expected;
        r.add(expected = new MemoryNode("http://example.org/foo"));
        assertEquals(expected, r.node());
    }

    @Test(expected = AmbiguousDataException.class)
    public void testNode2() throws LinkedDataException {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNamedNode("http://example.org/bar"));
        r.node();
    }

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

        assertEquals(expected, r.nodes());
    }

    @Test
    public void testStrings() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));
        r.add(MemoryLiteral.create("public static void main(String[] args)", null));

        Set<String> expected = new HashSet<>();
        expected.add("Hello world!");
        expected.add("public static void main(String[] args)");

        assertEquals(expected, r.strings());
    }

}
