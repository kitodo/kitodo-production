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

package org.kitodo.production.lugh.ld;

import static org.junit.Assert.*;

import java.nio.BufferOverflowException;
import java.util.*;

import org.junit.Test;
import org.kitodo.production.lugh.*;

public class ResultTest {

    @Test
    public void testClear() {
        Result r = new Result();
        r.add(Mets.ADMID);
        r.add(Mods.EXTENT);
        r.clear();
        assertEquals(0, r.size());
    }

    @Test
    public void testCreateFrom() throws LinkedDataException {
        new NodeTest().testToModel();
    }

    @Test
    public void testResult() {
        Result r = new Result();
        assertEquals(0, r.size());
    }

    @Test
    public void testResultCollectionOfQextendsObjectType() {
        Result other = new Result();
        other.add(new Node(Mets.METS_HDR));
        other.add(Mods.IDENTIFIER);

        Result r = new Result(other);
        assertEquals(other.size(), r.size());
        assertEquals(other, r);
    }

    @Test
    public void testResultInt() {
        Result r = new Result(42);
        assertEquals(0, r.size());
    }

    @Test
    public void testResultObjectType() {
        Result expected = new Result();
        expected.add(new Node(Mets.METS_HDR));

        Result r = new Result(new Node(Mets.METS_HDR));

        assertEquals(expected.size(), r.size());
        assertEquals(expected, r);
    }

    @Test
    public void testAddObjectType() {
        Result r = new Result();

        assertEquals(0, r.size());

        r.add(new Node(Mets.METS_HDR));

        assertEquals(1, r.size());
    }

    @Test(expected = NoSuchElementException.class)
    public void testIdentifiableNodeExpectable0() {
        Result r = new Result();

        r.identifiableNodeExpectable();
    }

    @Test(expected = NoSuchElementException.class)
    public void testIdentifiableNodeExpectable0ButLiteral() {
        Result r = new Result(Literal.createLiteral("Hello world!", "en"));

        r.identifiableNodeExpectable();
    }

    @Test
    public void testIdentifiableNodeExpectable1NamedNode() {
        IdentifiableNode expected;
        Result r = new Result(expected = new NamedNode("http://example.org/fooBar"));

        assertEquals(expected, r.identifiableNodeExpectable());
    }

    @Test
    public void testIdentifiableNodeExpectable1NodeReference() {
        IdentifiableNode expected;
        Result r = new Result(expected = new NodeReference("http://example.org/fooBar"));

        assertEquals(expected, r.identifiableNodeExpectable());
    }

    @Test(expected = BufferOverflowException.class)
    public void testIdentifiableNodeExpectable2() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(new NamedNode("http://example.org/bar"));

        r.identifiableNodeExpectable();
    }

    @Test(expected = BufferOverflowException.class)
    public void testIdentifiableNodeExpectable2WhereOneIsIdentifiable() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(new Node("http://example.org/bar"));

        r.identifiableNodeExpectable();
    }

    @Test(expected = BufferOverflowException.class)
    public void testIdentifiableNodeExpectable2WhereOneIsALiteral() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(Literal.createLiteral("Hello world!", "en"));

        r.identifiableNodeExpectable();
    }

    @Test
    public void testIdentifiableNodes() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(new NodeReference("http://example.org/bar"));
        r.add(new Node("http://example.org/baz"));
        r.add(Literal.createLiteral("Hello world!", "en"));

        Set<IdentifiableNode> expected = new HashSet<IdentifiableNode>();
        expected.add(new NamedNode("http://example.org/foo"));
        expected.add(new NodeReference("http://example.org/bar"));

        assertEquals(expected, r.identifiableNodes());
    }

    @Test
    public void testIsUniqueIdentifiableNode0() {
        Result r = new Result();

        assertFalse(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode0ButLiteral() {
        Result r = new Result(Literal.createLiteral("Hello world!", "en"));

        assertFalse(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode1NamedNode() {
        Result r = new Result(new NamedNode("http://example.org/fooBar"));

        assertTrue(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode1NodeReference() {
        Result r = new Result(new NodeReference("http://example.org/fooBar"));

        assertTrue(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode2() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(new NamedNode("http://example.org/bar"));

        assertFalse(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode2WhereOneIsIdentifiable() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(new Node("http://example.org/bar"));

        assertFalse(r.isUniqueIdentifiableNode());
    }

    @Test
    public void testIsUniqueIdentifiableNode2WhereOneIsALiteral() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(Literal.createLiteral("Hello world!", "en"));

        assertFalse(r.isUniqueIdentifiableNode());
    }

    @Test(expected = NoDataException.class)
    public void testNode0Empty() throws LinkedDataException {
        Result r = new Result();
        r.node();
    }

    @Test(expected = NoDataException.class)
    public void testNode0LiteralsOnly() throws LinkedDataException {
        Result r = new Result();
        r.add(Literal.createLiteral("Hello world!", "en"));
        r.node();
    }

    @Test
    public void testNode1Node() throws LinkedDataException {
        Result r = new Result();
        Node expected;
        r.add(expected = new Node("http://example.org/foo"));
        assertEquals(expected, r.node());
    }

    @Test
    public void testNode1NamedNode() throws LinkedDataException {
        Result r = new Result();
        NamedNode expected;
        r.add(expected = new NamedNode("http://example.org/foo"));
        assertEquals(expected, r.node());
    }

    @Test(expected = AmbiguousDataException.class)
    public void testNode2() throws LinkedDataException {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(new NamedNode("http://example.org/bar"));
        r.node();
    }

    @Test
    public void testNodes() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(new NodeReference("http://example.org/bar"));
        r.add(new Node("http://example.org/baz"));
        r.add(Literal.createLiteral("Hello world!", "en"));

        Set<Node> expected = new HashSet<Node>();
        expected.add(new NamedNode("http://example.org/foo"));
        expected.add(new Node("http://example.org/baz"));

        assertEquals(expected, r.nodes());
    }

    @Test
    public void testStrings() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(new NodeReference("http://example.org/bar"));
        r.add(new Node("http://example.org/baz"));
        r.add(Literal.createLiteral("Hello world!", "en"));
        r.add(Literal.create("public static void main(String[] args)", null));

        Set<String> expected = new HashSet<>();
        expected.add("Hello world!");
        expected.add("public static void main(String[] args)");

        assertEquals(expected, r.strings());
    }

    @Test
    public void testStringsBoolean() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(new NodeReference("http://example.org/bar"));
        r.add(new Node("http://example.org/baz"));
        r.add(Literal.createLiteral("Hello world!", "en"));

        Set<String> expected = new HashSet<>();
        expected.add("http://example.org/bar");
        expected.add("Hello world!");

        assertEquals(expected, r.strings(true));
    }

    @Test
    public void testStringsStringBoolean() {
        Result r = new Result();
        r.add(new NamedNode("http://example.org/foo"));
        r.add(new NodeReference("http://example.org/bar"));
        r.add(new Node("http://example.org/baz"));
        r.add(Literal.createLiteral("Hello world!", "en"));

        Set<String> expectedOneOf = new HashSet<>();
        expectedOneOf.add("http://example.org/bar ; Hello world!");
        expectedOneOf.add("Hello world! ; http://example.org/bar");

        assertTrue(expectedOneOf.contains(r.strings(" ; ", true)));
    }

}
