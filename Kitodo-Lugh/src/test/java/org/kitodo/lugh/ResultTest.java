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

import java.nio.BufferOverflowException;
import java.util.*;

import org.junit.Test;

/** Tests {@code org.kitodo.lugh.Result}. */
public class ResultTest {

    /**
     * Creates a result with elements in a storage-agnostic manner.
     *
     * @param elements
     *            elements to add
     * @return the created result
     */
    private static Result createResult(Storage storage, ObjectType... elements) {
        Node node = storage.createNode();
        Arrays.asList(elements).forEach(element -> node.put(RDF.VALUE, element));
        return node.get(RDF.VALUE);
    }

    /** Tests {@code accessibleObject()}. */
    @Test
    public void testAccessibleObject() {
        fail("Not yet implemented.");
    }

    /** Tests {@code accessibleObjectExpectable()}. */
    @Test
    public void testAccessibleObjectExpectable() {
        fail("Not yet implemented.");
    }

    /** Tests {@code accessibleObjectOrElse(AccessibleObject)}. */
    @Test
    public void testAccessibleObjectOrElse() {
        fail("Not yet implemented.");
    }

    /** Tests {@code accessibleObjectOrElseGet(Supplier<AccessibleObject>)}. */
    @Test
    public void testAccessibleObjectOrElseGet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code accessibleObjects()}. */
    @Test
    public void testAccessibleObjects() {
        fail("Not yet implemented.");
    }

    /** Tests {@code count(Class<? extends ObjectType>, int)}. */
    @Test
    public void testCount() {
        fail("Not yet implemented.");
    }

    /** Tests {@code expectable()}. */
    @Test
    public void testExpectable() {
        fail("Not yet implemented.");
    }

    /** Tests {@code expectableSingleton(Class<T>)}. */
    @Test
    public void testExpectableSingleton() {
        fail("Not yet implemented.");
    }

    /** Tests {@code forEach(Consumer<? super ObjectType>)}. */
    @Test
    public void testForEach() {
        fail("Not yet implemented.");
    }

    /**
     * Tests
     * {@code forEachAccessibleObject(Consumer<? super AccessibleObject>)}.
     */
    @Test
    public void testForEachAccessibleObject() {
        fail("Not yet implemented.");
    }

    /**
     * Tests
     * {@code forEachIdentifiableNode(Consumer<? super IdentifiableNode>)}.
     */
    @Test
    public void testForEachIdentifiableNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code forEachLangString(Consumer<? super LangString>)}. */
    @Test
    public void testForEachLangString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code forEachLeaf(Consumer<? super String>)}. */
    @Test
    public void testForEachLeaf() {
        fail("Not yet implemented.");
    }

    /** Tests {@code forEachLiteral(Consumer<? super Literal>)}. */
    @Test
    public void testForEachLiteral() {
        fail("Not yet implemented.");
    }

    /** Tests {@code forEachNamedNode(Consumer<? super NamedNode>)}. */
    @Test
    public void testForEachNamedNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code forEachNode(Consumer<? super Node>)}. */
    @Test
    public void testForEachNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code forEachNodeReference(Consumer<? super NodeReference>)}. */
    @Test
    public void testForEachNodeReference() {
        fail("Not yet implemented.");
    }

    /** Tests {@code forEachNodeType(Consumer<? super NodeType>)}. */
    @Test
    public void testForEachNodeType() {
        fail("Not yet implemented.");
    }

    /** Tests {@code forEachString(Consumer<? super String>)}. */
    @Test
    public void testForEachString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code identifiableNode()}. */
    @Test
    public void testIdentifiableNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code identifiableNodeExpectable()}. */
    @Test
    public void testIdentifiableNodeExpectable0() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /** Tests {@code identifiableNodeExpectable()}. */
    @Test
    public void testIdentifiableNodeExpectable0ButLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLiteralType("Hello world!", "en"));

            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /** Tests {@code identifiableNodeExpectable()}. */
    @Test
    public void testIdentifiableNodeExpectable1NamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            IdentifiableNode expected;
            Result r = createResult(storage, expected = storage.createNamedNode("http://example.org/fooBar"));

            assertEquals(expected, r.identifiableNodeExpectable());
        }
    }

    /** Tests {@code identifiableNodeExpectable()}. */
    @Test
    public void testIdentifiableNodeExpectable1NodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            IdentifiableNode expected;
            Result r = createResult(storage, expected = storage.createNodeReference("http://example.org/fooBar"));

            assertEquals(expected, r.identifiableNodeExpectable());
        }
    }

    /** Tests {@code identifiableNodeExpectable()}. */
    @Test
    public void testIdentifiableNodeExpectable2() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createNamedNode("http://example.org/bar"));

            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /** Tests {@code identifiableNodeExpectable()}. */
    @Test
    public void testIdentifiableNodeExpectable2WhereOneIsALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createLiteralType("Hello world!", "en"));

            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /** Tests {@code identifiableNodeExpectable()}. */
    @Test
    public void testIdentifiableNodeExpectable2WhereOneIsIdentifiable() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createNode("http://example.org/bar"));

            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /** Tests {@code identifiableNodeOrElse(IdentifiableNode)}. */
    @Test
    public void testIdentifiableNodeOrElse() {
        fail("Not yet implemented.");
    }

    /** Tests {@code identifiableNodeOrElseGet(Supplier<IdentifiableNode>)}. */
    @Test
    public void testIdentifiableNodeOrElseGet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code identifiableNodes()}. */
    @Test
    public void testIdentifiableNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createNodeReference("http://example.org/bar"), storage.createNode("http://example.org/baz"),
                    storage.createLiteralType("Hello world!", "en"));

            Set<IdentifiableNode> expected = new HashSet<>();
            expected.add(storage.createNamedNode("http://example.org/foo"));
            expected.add(storage.createNodeReference("http://example.org/bar"));

            assertEquals(expected, r.identifiableNodes());
        }
    }

    /** Tests {@code isAny()}. */
    @Test
    public void testIsAny() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isAnyAccessibleObject()}. */
    @Test
    public void testIsAnyAccessibleObject() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isAnyIdentifiableNode()}. */
    @Test
    public void testIsAnyIdentifiableNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isAnyLangString()}. */
    @Test
    public void testIsAnyLangString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isAnyLiteral()}. */
    @Test
    public void testIsAnyLiteral() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isAnyNamedNode()}. */
    @Test
    public void testIsAnyNamedNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isAnyNode()}. */
    @Test
    public void testIsAnyNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isAnyNodeReference()}. */
    @Test
    public void testIsAnyNodeReference() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isAnyNodeType()}. */
    @Test
    public void testIsAnyNodeType() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isSingleton(Class<? extends ObjectType>)}. */
    @Test
    public void testIsSingleton() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isUnique()}. */
    @Test
    public void testIsUnique() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isUniqueAccessibleObject()}. */
    @Test
    public void testIsUniqueAccessibleObject() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isUniqueIdentifiableNode()}. */
    @Test
    public void testIsUniqueIdentifiableNode0() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertFalse(r.isUniqueIdentifiableNode());
        }
    }

    /** Tests {@code isUniqueIdentifiableNode()}. */
    @Test
    public void testIsUniqueIdentifiableNode0ButLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLiteralType("Hello world!", "en"));

            assertFalse(r.isUniqueIdentifiableNode());
        }
    }

    /** Tests {@code isUniqueIdentifiableNode()}. */
    @Test
    public void testIsUniqueIdentifiableNode1NamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (ObjectType) storage.createNamedNode("http://example.org/fooBar"));

            assertTrue(r.isUniqueIdentifiableNode());
        }
    }

    /** Tests {@code isUniqueIdentifiableNode()}. */
    @Test
    public void testIsUniqueIdentifiableNode1NodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://example.org/fooBar"));

            assertTrue(r.isUniqueIdentifiableNode());
        }
    }

    /** Tests {@code isUniqueIdentifiableNode()}. */
    @Test
    public void testIsUniqueIdentifiableNode2() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createNamedNode("http://example.org/bar"));

            assertFalse(r.isUniqueIdentifiableNode());
        }
    }

    /** Tests {@code isUniqueIdentifiableNode()}. */
    @Test
    public void testIsUniqueIdentifiableNode2WhereOneIsALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createLiteralType("Hello world!", "en"));

            assertFalse(r.isUniqueIdentifiableNode());
        }
    }

    /** Tests {@code isUniqueIdentifiableNode()}. */
    @Test
    public void testIsUniqueIdentifiableNode2WhereOneIsIdentifiable() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createNode("http://example.org/bar"));

            assertFalse(r.isUniqueIdentifiableNode());
        }
    }

    /** Tests {@code isUniqueIdentifiableNode()}. */
    /** Tests {@code isUniqueLangString()}. */
    @Test
    public void testIsUniqueLangString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isUniqueLiteral()}. */
    @Test
    public void testIsUniqueLiteral() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isUniqueNamedNode()}. */
    @Test
    public void testIsUniqueNamedNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isUniqueNode()}. */
    @Test
    public void testIsUniqueNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isUniqueNodeReference()}. */
    @Test
    public void testIsUniqueNodeReference() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isUniqueNodeType()}. */
    @Test
    public void testIsUniqueNodeType() {
        fail("Not yet implemented.");
    }

    /** Tests {@code langString()}. */
    @Test
    public void testLangString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code langStringExpectable()}. */
    @Test
    public void testLangStringExpectable() {
        fail("Not yet implemented.");
    }

    /** Tests {@code langStringOrElse(LangString)}. */
    @Test
    public void testLangStringOrElse() {
        fail("Not yet implemented.");
    }

    /** Tests {@code langStringOrElseGet(Supplier<LangString>)}. */
    @Test
    public void testLangStringOrElseGet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code langStrings()}. */
    @Test
    public void testLangStrings() {
        fail("Not yet implemented.");
    }

    /** Tests {@code leaves()}. */
    @Test
    public void testLeaves() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createNodeReference("http://example.org/bar"), storage.createNode("http://example.org/baz"),
                    storage.createLiteralType("Hello world!", "en"));

            Set<String> expected = new HashSet<>();
            expected.add("http://example.org/bar");
            expected.add("Hello world!");

            assertEquals(expected, r.leaves());
        }
    }

    /** Tests {@code leaves(String)}. */
    @Test
    public void testLeavesString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createNodeReference("http://example.org/bar"), storage.createNode("http://example.org/baz"),
                    storage.createLiteralType("Hello world!", "en"));

            Set<String> expectedOneOf = new HashSet<>();
            expectedOneOf.add("http://example.org/bar ; Hello world!");
            expectedOneOf.add("Hello world! ; http://example.org/bar");

            assertTrue(expectedOneOf.contains(r.leaves(" ; ")));
        }
    }

    /** Tests {@code literal()}. */
    @Test
    public void testLiteral() {
        fail("Not yet implemented.");
    }

    /** Tests {@code literalExpectable()}. */
    @Test
    public void testLiteralExpectable() {
        fail("Not yet implemented.");
    }

    /** Tests {@code literalOrElse(Literal)}. */
    @Test
    public void testLiteralOrElse() {
        fail("Not yet implemented.");
    }

    /** Tests {@code literalOrElseGet(Supplier<Literal>)}. */
    @Test
    public void testLiteralOrElseGet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code literals()}. */
    @Test
    public void testLiterals() {
        fail("Not yet implemented.");
    }

    /** Tests {@code namedNode()}. */
    @Test
    public void testNamedNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code namedNodeExpectable()}. */
    @Test
    public void testNamedNodeExpectable() {
        fail("Not yet implemented.");
    }

    /** Tests {@code namedNodeOrElse(NamedNode)}. */
    @Test
    public void testNamedNodeOrElse() {
        fail("Not yet implemented.");
    }

    /** Tests {@code namedNodeOrElseGet(Supplier<NamedNode>)}. */
    @Test
    public void testNamedNodeOrElseGet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code namedNodes()}. */
    @Test
    public void testNamedNodes() {
        fail("Not yet implemented.");
    }

    /** Tests {@code node()}. */
    @Test
    public void testNode0Empty() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.node();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /** Tests {@code node()}. */
    @Test
    public void testNode0LiteralsOnly() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLiteralType("Hello world!", "en"));
            try {
                r.node();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /** Tests {@code node()}. */
    @Test
    public void testNode1NamedNode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node expected = storage.createNamedNode("http://example.org/foo");
            Result r = createResult(storage, expected);
            assertEquals(expected, r.node());
        }
    }

    /** Tests {@code node()}. */
    @Test
    public void testNode1Node() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node expected = storage.createNode("http://example.org/foo");
            Result r = createResult(storage);
            assertEquals(expected, r.node());
        }
    }

    /** Tests {@code node()}. */
    @Test
    public void testNode2() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createNamedNode("http://example.org/bar"));
            try {
                r.node();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /** Tests {@code nodeExpectable()}. */
    @Test
    public void testNodeExpectable() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeOrElse(Node)}. */
    @Test
    public void testNodeOrElse() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeOrElseGet(Supplier<Node>)}. */
    @Test
    public void testNodeOrElseGet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeReference()}. */
    @Test
    public void testNodeReference() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeReferenceExpectable()}. */
    @Test
    public void testNodeReferenceExpectable() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeReferenceOrElse(NodeReference)}. */
    @Test
    public void testNodeReferenceOrElse() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeReferenceOrElseGet(Supplier<NodeReference>)}. */
    @Test
    public void testNodeReferenceOrElseGet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeReferences()}. */
    @Test
    public void testNodeReferences() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodes()}. */
    @Test
    public void testNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createNodeReference("http://example.org/bar"), storage.createNode("http://example.org/baz"),
                    storage.createLiteralType("Hello world!", "en"));

            Set<Node> expected = new HashSet<>();
            expected.add(storage.createNamedNode("http://example.org/foo"));
            expected.add(storage.createNode("http://example.org/baz"));

            assertEquals(expected, r.nodes());
        }
    }

    /** Tests {@code nodeType()}. */
    @Test
    public void testNodeType() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeTypeExpectable()}. */
    @Test
    public void testNodeTypeExpectable() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeTypeOrElse(NodeType)}. */
    @Test
    public void testNodeTypeOrElse() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeTypeOrElseGet(Supplier<NodeType>)}. */
    @Test
    public void testNodeTypeOrElseGet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code nodeTypes()}. */
    @Test
    public void testNodeTypes() {
        fail("Not yet implemented.");
    }

    /** Tests {@code orElse(ObjectType)}. */
    @Test
    public void testOrElse() {
        fail("Not yet implemented.");
    }

    /** Tests {@code orElseGet(Supplier<ObjectType>)}. */
    @Test
    public void testOrElseGet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code singleton(Class<T>)}. */
    @Test
    public void testSingleton() {
        fail("Not yet implemented.");
    }

    /** Tests {@code strings()}. */

    @Test
    public void testStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                    storage.createNodeReference("http://example.org/bar"), storage.createNode("http://example.org/baz"),
                    storage.createLiteralType("Hello world!", "en"),
                    storage.createLiteralType("public static void main(String[] args)", null));

            Set<String> expected = new HashSet<>();
            expected.add("Hello world!");
            expected.add("public static void main(String[] args)");

            assertEquals(expected, r.strings());
        }
    }

    /** Tests {@code strings(String)}. */
    @Test
    public void testStringsString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code subset(Class<T>)}. */
    @Test
    public void testSubset() {
        fail("Not yet implemented.");
    }

    /** Tests {@code value()}. */
    @Test
    public void testValue() {
        fail("Not yet implemented.");
    }
}
