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

package org.kitodo.lugh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.BufferOverflowException;
import java.util.*;

import org.junit.Test;
import org.kitodo.lugh.vocabulary.*;

public class ResultTest {

    @Test
    public void testAddObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();

            assertEquals(0, r.size());

            r.add(storage.newNode(Mets.METS_HDR));

            assertEquals(1, r.size());
        }
    }

    @Test
    public void testClear() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(Mets.ADMID);
            r.add(Mods.EXTENT);
            r.clear();
            assertEquals(0, r.size());
        }
    }

    @Test
    public void testCreateFrom() throws LinkedDataException {
        new NodeTest().testToModel();
    }

    @Test
    public void testIdentifiableNodeExpectable0() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();

            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void testIdentifiableNodeExpectable0ButLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult(storage.newLiteral("Hello world!", "en"));

            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testIdentifiableNodeExpectable1NamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            IdentifiableNode expected;
            Result r = storage.newResult(expected = storage.newNamedNode("http://example.org/fooBar"));

            assertEquals(expected, r.identifiableNodeExpectable());
        }
    }

    @Test
    public void testIdentifiableNodeExpectable1NodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            IdentifiableNode expected;
            Result r = storage.newResult(expected = storage.newNodeReference("http://example.org/fooBar"));

            assertEquals(expected, r.identifiableNodeExpectable());
        }
    }

    @Test
    public void testIdentifiableNodeExpectable2() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newNamedNode("http://example.org/bar"));

            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testIdentifiableNodeExpectable2WhereOneIsALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newLiteral("Hello world!", "en"));

            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testIdentifiableNodeExpectable2WhereOneIsIdentifiable() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newNode("http://example.org/bar"));

            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testIdentifiableNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newNodeReference("http://example.org/bar"));
            r.add(storage.newNode("http://example.org/baz"));
            r.add(storage.newLiteral("Hello world!", "en"));

            Set<IdentifiableNode> expected = new HashSet<>();
            expected.add(storage.newNamedNode("http://example.org/foo"));
            expected.add(storage.newNodeReference("http://example.org/bar"));

            assertEquals(expected, r.identifiableNodes());
        }
    }

    @Test
    public void testIsUniqueIdentifiableNode0() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();

            assertFalse(r.isUniqueIdentifiableNode());
        }
    }

    @Test
    public void testIsUniqueIdentifiableNode0ButLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult(storage.newLiteral("Hello world!", "en"));

            assertFalse(r.isUniqueIdentifiableNode());
        }
    }

    @Test
    public void testIsUniqueIdentifiableNode1NamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult(storage.newNamedNode("http://example.org/fooBar"));

            assertTrue(r.isUniqueIdentifiableNode());
        }
    }

    @Test
    public void testIsUniqueIdentifiableNode1NodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult(storage.newNodeReference("http://example.org/fooBar"));

            assertTrue(r.isUniqueIdentifiableNode());
        }
    }

    @Test
    public void testIsUniqueIdentifiableNode2() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newNamedNode("http://example.org/bar"));

            assertFalse(r.isUniqueIdentifiableNode());
        }
    }

    @Test
    public void testIsUniqueIdentifiableNode2WhereOneIsALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newLiteral("Hello world!", "en"));

            assertFalse(r.isUniqueIdentifiableNode());
        }
    }

    @Test
    public void testIsUniqueIdentifiableNode2WhereOneIsIdentifiable() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newNode("http://example.org/bar"));

            assertFalse(r.isUniqueIdentifiableNode());
        }
    }

    @Test
    public void testNode0Empty() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            try {
                r.node();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testNode0LiteralsOnly() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newLiteral("Hello world!", "en"));
            try {
                r.node();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testNode1NamedNode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            NamedNode expected;
            r.add(expected = storage.newNamedNode("http://example.org/foo"));
            assertEquals(expected, r.node());
        }
    }

    @Test
    public void testNode1Node() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            Node expected;
            r.add(expected = storage.newNode("http://example.org/foo"));
            assertEquals(expected, r.node());
        }
    }

    @Test
    public void testNode2() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newNamedNode("http://example.org/bar"));
            try {
                r.node();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newNodeReference("http://example.org/bar"));
            r.add(storage.newNode("http://example.org/baz"));
            r.add(storage.newLiteral("Hello world!", "en"));

            Set<Node> expected = new HashSet<>();
            expected.add(storage.newNamedNode("http://example.org/foo"));
            expected.add(storage.newNode("http://example.org/baz"));

            assertEquals(expected, r.nodes());
        }
    }

    @Test
    public void testResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            assertEquals(0, r.size());
        }
    }

    @Test
    public void testResultCollectionOfQextendsObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result other = storage.newResult();
            other.add(storage.newNode(Mets.METS_HDR));
            other.add(Mods.IDENTIFIER);

            Result r = storage.newResult(other);
            assertEquals(other.size(), r.size());
            assertEquals(other, r);
        }
    }

    @Test
    public void testResultInt() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult(42);
            assertEquals(0, r.size());
        }
    }

    @Test
    public void testResultObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result expected = storage.newResult();
            expected.add(storage.newNode(Mets.METS_HDR));

            Result r = storage.newResult(storage.newNode(Mets.METS_HDR));

            assertEquals(expected.size(), r.size());
            assertEquals(expected, r);
        }
    }

    @Test
    public void testStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newNodeReference("http://example.org/bar"));
            r.add(storage.newNode("http://example.org/baz"));
            r.add(storage.newLiteral("Hello world!", "en"));
            r.add(storage.newObjectType("public static void main(String[] args)", null));

            Set<String> expected = new HashSet<>();
            expected.add("Hello world!");
            expected.add("public static void main(String[] args)");

            assertEquals(expected, r.strings());
        }
    }

    @Test
    public void testStringsBoolean() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newNodeReference("http://example.org/bar"));
            r.add(storage.newNode("http://example.org/baz"));
            r.add(storage.newLiteral("Hello world!", "en"));

            Set<String> expected = new HashSet<>();
            expected.add("http://example.org/bar");
            expected.add("Hello world!");

            assertEquals(expected, r.strings(true));
        }
    }

    @Test
    public void testStringsStringBoolean() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = storage.newResult();
            r.add(storage.newNamedNode("http://example.org/foo"));
            r.add(storage.newNodeReference("http://example.org/bar"));
            r.add(storage.newNode("http://example.org/baz"));
            r.add(storage.newLiteral("Hello world!", "en"));

            Set<String> expectedOneOf = new HashSet<>();
            expectedOneOf.add("http://example.org/bar ; Hello world!");
            expectedOneOf.add("Hello world! ; http://example.org/bar");

            assertTrue(expectedOneOf.contains(r.strings(" ; ", true)));
        }
    }

}
