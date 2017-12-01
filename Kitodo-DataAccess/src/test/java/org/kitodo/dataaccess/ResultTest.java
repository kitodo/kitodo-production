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

package org.kitodo.dataaccess;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

import java.nio.BufferOverflowException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import org.junit.Test;

/**
 * Tests {@code org.kitodo.dataaccess.Result}.
 */
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

    /**
     * Tests {@code accessibleObjectExpectable()} with one AccessibleObject and
     * a NodeReference.
     */
    @Test
    public void testAccessibleObjectExpectableWithAnAccessibleObjectAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            try {
                r.accessibleObjectExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code accessibleObjectExpectable()} with an empty result.
     */
    @Test
    public void testAccessibleObjectExpectableWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.accessibleObjectExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code accessibleObjectExpectable()} with a NodeReference.
     */
    @Test
    public void testAccessibleObjectExpectableWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            try {
                r.accessibleObjectExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code accessibleObjectExpectable()} with one AccessibleObject.
     */
    @Test
    public void testAccessibleObjectExpectableWithOneAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createNode("http://test.example/alice"));

            assertThat(r.accessibleObjectExpectable(),
                is(equalTo((AccessibleObject) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code accessibleObjectExpectable()} with two AccessibleObjects.
     */
    @Test
    public void testAccessibleObjectExpectableWithTwoAccessibleObjects() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.accessibleObjectExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code accessibleObjectExpectable()} with two AccessibleObjects and
     * a NodeReference.
     */
    @Test
    public void testAccessibleObjectExpectableWithTwoAccessibleObjectsAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));
            try {
                r.accessibleObjectExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code accessibleObjectOrElseGet(Supplier<AccessibleObject>)} with
     * one AccessibleObject and a NodeReference.
     */
    @Test
    public void testAccessibleObjectOrElseGetWithAnAccessibleObjectAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            AccessibleObject result = r
                    .accessibleObjectOrElseGet(() -> storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElseGet(Supplier<AccessibleObject>)} with
     * an empty result.
     */
    @Test
    public void testAccessibleObjectOrElseGetWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            AccessibleObject result = r
                    .accessibleObjectOrElseGet(() -> storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElseGet(Supplier<AccessibleObject>)} with
     * a NodeReference.
     */
    @Test
    public void testAccessibleObjectOrElseGetWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            AccessibleObject result = r
                    .accessibleObjectOrElseGet(() -> storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElseGet(Supplier<AccessibleObject>)} with
     * one AccessibleObject.
     */
    @Test
    public void testAccessibleObjectOrElseGetWithOneAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createNode("http://test.example/alice"));
            AccessibleObject result = r.accessibleObjectOrElseGet(() -> storage.createNode("http://test.example/bob"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElseGet(Supplier<AccessibleObject>)} with
     * two AccessibleObjects.
     */
    @Test
    public void testAccessibleObjectOrElseGetWithTwoAccessibleObjects() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            AccessibleObject result = r
                    .accessibleObjectOrElseGet(() -> storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElseGet(Supplier<AccessibleObject>)} with
     * two AccessibleObjects and a NodeReference.
     */
    @Test
    public void testAccessibleObjectOrElseGetWithTwoAccessibleObjectsAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));
            AccessibleObject result = r.accessibleObjectOrElseGet(() -> storage.createNode("http://test.example/dave"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElse(AccessibleObject)} with one
     * AccessibleObject and a NodeReference.
     */
    @Test
    public void testAccessibleObjectOrElseWithAnAccessibleObjectAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            AccessibleObject result = r.accessibleObjectOrElse(storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElse(AccessibleObject)} with an empty
     * result.
     */
    @Test
    public void testAccessibleObjectOrElseWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            AccessibleObject result = r.accessibleObjectOrElse(storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElse(AccessibleObject)} with a
     * NodeReference.
     */
    @Test
    public void testAccessibleObjectOrElseWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            AccessibleObject result = r.accessibleObjectOrElse(storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElse(AccessibleObject)} with one
     * AccessibleObject.
     */
    @Test
    public void testAccessibleObjectOrElseWithOneAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createNode("http://test.example/alice"));
            AccessibleObject result = r.accessibleObjectOrElse(storage.createNode("http://test.example/bob"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElse(AccessibleObject)} with two
     * AccessibleObjects.
     */
    @Test
    public void testAccessibleObjectOrElseWithTwoAccessibleObjects() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            AccessibleObject result = r.accessibleObjectOrElse(storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code accessibleObjectOrElse(AccessibleObject)} with two
     * AccessibleObjects and a NodeReference.
     */
    @Test
    public void testAccessibleObjectOrElseWithTwoAccessibleObjectsAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));
            AccessibleObject result = r.accessibleObjectOrElse(storage.createNode("http://test.example/dave"));

            assertThat(result, is(equalTo((AccessibleObject) storage.createNode("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code accessibleObjects()} with one AccessibleObject and a
     * NodeReference.
     */
    @Test
    public void testAccessibleObjectsWithAnAccessibleObjectAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.accessibleObjects(), hasSize(1));
        }
    }

    /**
     * Tests {@code accessibleObjects()} with an empty result.
     */
    @Test
    public void testAccessibleObjectsWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.accessibleObjects(), hasSize(0));
        }
    }

    /**
     * Tests {@code accessibleObjects()} with a NodeReference.
     */
    @Test
    public void testAccessibleObjectsWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.accessibleObjects(), hasSize(0));
        }
    }

    /**
     * Tests {@code accessibleObjects()} with one AccessibleObject.
     */
    @Test
    public void testAccessibleObjectsWithOneAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createNode("http://test.example/alice"));

            assertThat(r.accessibleObjects(), hasSize(1));
        }
    }

    /**
     * Tests {@code accessibleObjects()} with two AccessibleObjects.
     */
    @Test
    public void testAccessibleObjectsWithTwoAccessibleObjects() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.accessibleObjects(), hasSize(2));
        }
    }

    /**
     * Tests {@code accessibleObjects()} with two AccessibleObjects and a
     * NodeReference.
     */
    @Test
    public void testAccessibleObjectsWithTwoAccessibleObjectsAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));

            assertThat(r.accessibleObjects(), hasSize(2));
        }
    }

    /**
     * Tests {@code accessibleObject()} with one AccessibleObject and a
     * NodeReference.
     */
    @Test
    public void testAccessibleObjectWithAnAccessibleObjectAndANodeReference() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            try {
                r.accessibleObject();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code accessibleObject()} with an empty result.
     */
    @Test
    public void testAccessibleObjectWithAnEmptyResult() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.accessibleObject();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code accessibleObject()} with a NodeReference.
     */
    @Test
    public void testAccessibleObjectWithANodeReference() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            try {
                r.accessibleObject();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code accessibleObject()} with one AccessibleObject.
     */
    @Test
    public void testAccessibleObjectWithOneAccessibleObject() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createNode("http://test.example/alice"));

            assertThat(r.accessibleObject(),
                is(equalTo((AccessibleObject) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code accessibleObject()} with two AccessibleObjects.
     */
    @Test
    public void testAccessibleObjectWithTwoAccessibleObjects() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.accessibleObject();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code accessibleObject()} with two AccessibleObjects and a
     * NodeReference.
     */
    @Test
    public void testAccessibleObjectWithTwoAccessibleObjectsAndANodeReference() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));
            try {
                r.accessibleObject();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code expectable()} with an empty result.
     */
    @Test
    public void testExpectableWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.expectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code expectable()} with one ObjectType.
     */
    @Test
    public void testExpectableWithOneObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (ObjectType) storage.createNode("http://test.example/alice"));

            assertThat(r.expectable(), is(equalTo((ObjectType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code expectable()} with two ObjectTypes.
     */
    @Test
    public void testExpectableWithTwoObjectTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.expectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code forEachAccessibleObject(Consumer<? super AccessibleObject>)}
     * with one AccessibleObject and a NodeReference.
     */
    @Test
    public void testForEachAccessibleObjectWithAnAccessibleObjectAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachAccessibleObject(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachAccessibleObject(Consumer<? super AccessibleObject>)}
     * with an empty result.
     */
    @Test
    public void testForEachAccessibleObjectWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachAccessibleObject(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachAccessibleObject(Consumer<? super AccessibleObject>)}
     * with a NodeReference.
     */
    @Test
    public void testForEachAccessibleObjectWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachAccessibleObject(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachAccessibleObject(Consumer<? super AccessibleObject>)}
     * with one AccessibleObject.
     */
    @Test
    public void testForEachAccessibleObjectWithOneAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createNode("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachAccessibleObject(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachAccessibleObject(Consumer<? super AccessibleObject>)}
     * with two AccessibleObjects.
     */
    @Test
    public void testForEachAccessibleObjectWithTwoAccessibleObjects() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachAccessibleObject(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachAccessibleObject(Consumer<? super AccessibleObject>)}
     * with two AccessibleObjects and a NodeReference.
     */
    @Test
    public void testForEachAccessibleObjectWithTwoAccessibleObjectsAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachAccessibleObject(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachIdentifiableNode(Consumer<? super IdentifiableNode>)}
     * with a LangString.
     */
    @Test
    public void testForEachIdentifiableNodeWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachIdentifiableNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachIdentifiableNode(Consumer<? super IdentifiableNode>)}
     * with an empty result.
     */
    @Test
    public void testForEachIdentifiableNodeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachIdentifiableNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachIdentifiableNode(Consumer<? super IdentifiableNode>)}
     * with one IdentifiableNode and a LangString.
     */
    @Test
    public void testForEachIdentifiableNodeWithAnIdentifiableNodeAndALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createLangString("In aqua bacillus est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachIdentifiableNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachIdentifiableNode(Consumer<? super IdentifiableNode>)}
     * with one IdentifiableNode.
     */
    @Test
    public void testForEachIdentifiableNodeWithOneIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                (IdentifiableNode) storage.createNodeReference("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachIdentifiableNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachIdentifiableNode(Consumer<? super IdentifiableNode>)}
     * with two IdentifiableNodes.
     */
    @Test
    public void testForEachIdentifiableNodeWithTwoIdentifiableNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachIdentifiableNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachIdentifiableNode(Consumer<? super IdentifiableNode>)}
     * with two IdentifiableNodes and a Node.
     */
    @Test
    public void testForEachIdentifiableNodeWithTwoIdentifiableNodesAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"),
                storage.createNode("http://test.example/charlie"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachIdentifiableNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachLangString(Consumer<? super LangString>)} with one
     * LangString and a Literal.
     */
    @Test
    public void testForEachLangStringWithALangStringAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLangString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachLangString(Consumer<? super LangString>)} with an
     * empty result.
     */
    @Test
    public void testForEachLangStringWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLangString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachLangString(Consumer<? super LangString>)} with a
     * Node.
     */
    @Test
    public void testForEachLangStringWithANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLangString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachLangString(Consumer<? super LangString>)} with one
     * LangString.
     */
    @Test
    public void testForEachLangStringWithOneLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLangString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachLangString(Consumer<? super LangString>)} with two
     * LangStrings.
     */
    @Test
    public void testForEachLangStringWithTwoLangStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLangString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachLangString(Consumer<? super LangString>)} with two
     * LangStrings and a Node.
     */
    @Test
    public void testForEachLangStringWithTwoLangStringsAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"),
                storage.createNode("http://test.example/charlie"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLangString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachLeaf(Consumer<? super String>)} with an empty result.
     */
    @Test
    public void testForEachLeafWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLeaf(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachLeaf(Consumer<? super String>)} with a Node.
     */
    @Test
    public void testForEachLeafWithANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLeaf(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachLeaf(Consumer<? super String>)} with one String and a
     * Leaf.
     */
    @Test
    public void testForEachLeafWithAStringAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLeaf("In aqua bacillus est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLeaf(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachLeaf(Consumer<? super String>)} with one String.
     */
    @Test
    public void testForEachLeafWithOneString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLeaf(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachLeaf(Consumer<? super String>)} with two Strings.
     */
    @Test
    public void testForEachLeafWithTwoStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLeaf(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachLeaf(Consumer<? super String>)} with two Strings and
     * a Node.
     */
    @Test
    public void testForEachLeafWithTwoStringsAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"),
                storage.createNode("http://test.example/charlie"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLeaf(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachLiteral(Consumer<? super Literal>)} with one Literal
     * and a NodeReference.
     */
    @Test
    public void testForEachLiteralWithALiteralAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createNodeReference("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLiteral(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachLiteral(Consumer<? super Literal>)} with an empty
     * result.
     */
    @Test
    public void testForEachLiteralWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLiteral(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachLiteral(Consumer<? super Literal>)} with a
     * NodeReference.
     */
    @Test
    public void testForEachLiteralWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLiteral(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachLiteral(Consumer<? super Literal>)} with one Literal.
     */
    @Test
    public void testForEachLiteralWithOneLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLiteral(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachLiteral(Consumer<? super Literal>)} with two
     * Literals.
     */
    @Test
    public void testForEachLiteralWithTwoLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLiteral(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachLiteral(Consumer<? super Literal>)} with two Literals
     * and a Leaf.
     */
    @Test
    public void testForEachLiteralWithTwoLiteralsAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL),
                storage.createLeaf("http://test.example/alice", ""));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachLiteral(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachNamedNode(Consumer<? super NamedNode>)} with a Leaf.
     */
    @Test
    public void testForEachNamedNodeWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("In vino veritas est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNamedNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachNamedNode(Consumer<? super NamedNode>)} with one
     * NamedNode and a NodeReference.
     */
    @Test
    public void testForEachNamedNodeWithANamedNodeAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNamedNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachNamedNode(Consumer<? super NamedNode>)} with an empty
     * result.
     */
    @Test
    public void testForEachNamedNodeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNamedNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachNamedNode(Consumer<? super NamedNode>)} with one
     * NamedNode.
     */
    @Test
    public void testForEachNamedNodeWithOneNamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNamedNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachNamedNode(Consumer<? super NamedNode>)} with two
     * NamedNodes.
     */
    @Test
    public void testForEachNamedNodeWithTwoNamedNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNamedNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachNamedNode(Consumer<? super NamedNode>)} with two
     * NamedNodes and a NodeReference.
     */
    @Test
    public void testForEachNamedNodeWithTwoNamedNodesAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNamedNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachNodeReference(Consumer<? super NodeReference>)} with
     * a LangString.
     */
    @Test
    public void testForEachNodeReferenceWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeReference(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachNodeReference(Consumer<? super NodeReference>)} with
     * an empty result.
     */
    @Test
    public void testForEachNodeReferenceWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeReference(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachNodeReference(Consumer<? super NodeReference>)} with
     * one NodeReference and a Leaf.
     */
    @Test
    public void testForEachNodeReferenceWithANodeReferenceAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createLeaf("In aqua bacillus est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeReference(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachNodeReference(Consumer<? super NodeReference>)} with
     * one NodeReference.
     */
    @Test
    public void testForEachNodeReferenceWithOneNodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeReference(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachNodeReference(Consumer<? super NodeReference>)} with
     * two NodeReferences.
     */
    @Test
    public void testForEachNodeReferenceWithTwoNodeReferences() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeReference(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachNodeReference(Consumer<? super NodeReference>)} with
     * two NodeReferences and a Literal.
     */
    @Test
    public void testForEachNodeReferenceWithTwoNodeReferencesAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"), storage.createLiteral("42", RDF.PLAIN_LITERAL));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeReference(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachNodeType(Consumer<? super NodeType>)} with an
     * AccessibleObject.
     */
    @Test
    public void testForEachNodeTypeWithAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createLeaf("Lorem ipsum dolor sit amet", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeType(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachNodeType(Consumer<? super NodeType>)} with an empty
     * result.
     */
    @Test
    public void testForEachNodeTypeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeType(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachNodeType(Consumer<? super NodeType>)} with one
     * NodeType and an AccessibleObject.
     */
    @Test
    public void testForEachNodeTypeWithANodeTypeAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createLeaf("Lorem ipsum dolor sit amet", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeType(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachNodeType(Consumer<? super NodeType>)} with one
     * NodeType.
     */
    @Test
    public void testForEachNodeTypeWithOneNodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (NodeType) storage.createNode("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeType(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachNodeType(Consumer<? super NodeType>)} with two
     * NodeTypes.
     */
    @Test
    public void testForEachNodeTypeWithTwoNodeTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeType(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachNodeType(Consumer<? super NodeType>)} with two
     * NodeTypes and an AccessibleObject.
     */
    @Test
    public void testForEachNodeTypeWithTwoNodeTypesAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"), storage.createLeaf("Lorem ipsum dolor sit amet", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNodeType(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachNode(Consumer<? super Node>)} with a Leaf.
     */
    @Test
    public void testForEachNodeWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("In vino veritas est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachNode(Consumer<? super Node>)} with an empty result.
     */
    @Test
    public void testForEachNodeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachNode(Consumer<? super Node>)} with one Node and a
     * Literal.
     */
    @Test
    public void testForEachNodeWithANodeAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachNode(Consumer<? super Node>)} with one Node.
     */
    @Test
    public void testForEachNodeWithOneNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachNode(Consumer<? super Node>)} with two Nodes.
     */
    @Test
    public void testForEachNodeWithTwoNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachNode(Consumer<? super Node>)} with two Nodes and a
     * LangString.
     */
    @Test
    public void testForEachNodeWithTwoNodesAndALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachNode(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachString(Consumer<? super String>)} with a NamedNode.
     */
    @Test
    public void testForEachStringWithANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachString(Consumer<? super String>)} with an empty
     * result.
     */
    @Test
    public void testForEachStringWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEachString(Consumer<? super String>)} with one String and
     * a NodeReference.
     */
    @Test
    public void testForEachStringWithAStringAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createNodeReference("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachString(Consumer<? super String>)} with one String.
     */
    @Test
    public void testForEachStringWithOneString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEachString(Consumer<? super String>)} with two Strings.
     */
    @Test
    public void testForEachStringWithTwoStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEachString(Consumer<? super String>)} with two Strings
     * and a Leaf.
     */
    @Test
    public void testForEachStringWithTwoStringsAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"),
                storage.createLeaf("http://test.example/alice", "en"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEachString(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code forEach(Consumer<? super ObjectType>)} with an empty result.
     */
    @Test
    public void testForEachWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEach(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(0));
        }
    }

    /**
     * Tests {@code forEach(Consumer<? super ObjectType>)} with one ObjectType.
     */
    @Test
    public void testForEachWithOneObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (ObjectType) storage.createNode("http://test.example/alice"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEach(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(1));
        }
    }

    /**
     * Tests {@code forEach(Consumer<? super ObjectType>)} with two ObjectTypes.
     */
    @Test
    public void testForEachWithTwoObjectTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            KeySetView<String, Boolean> results = ConcurrentHashMap.<String>newKeySet();

            r.forEach(entry -> results.add(entry.toString()));

            assertThat(results, hasSize(2));
        }
    }

    /**
     * Tests {@code identifiableNodeExpectable()} with a Literal.
     */
    @Test
    public void testIdentifiableNodeExpectableWithALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));
            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code identifiableNodeExpectable()} with an empty result.
     */
    @Test
    public void testIdentifiableNodeExpectableWithAnEmptyResult() {
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

    /**
     * Tests {@code identifiableNodeExpectable()} with one IdentifiableNode and
     * a Literal.
     */
    @Test
    public void testIdentifiableNodeExpectableWithAnIdentifiableNodeAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code identifiableNodeExpectable()} with one IdentifiableNode.
     */
    @Test
    public void testIdentifiableNodeExpectableWithOneIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                (IdentifiableNode) storage.createNodeReference("http://test.example/alice"));

            assertThat(r.identifiableNodeExpectable(),
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code identifiableNodeExpectable()} with two IdentifiableNodes.
     */
    @Test
    public void testIdentifiableNodeExpectableWithTwoIdentifiableNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code identifiableNodeExpectable()} with two IdentifiableNodes and
     * a Literal.
     */
    @Test
    public void testIdentifiableNodeExpectableWithTwoIdentifiableNodesAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"), storage.createLiteral("42", RDF.PLAIN_LITERAL));
            try {
                r.identifiableNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code identifiableNodeOrElseGet(Supplier<IdentifiableNode>)} with
     * a LangString.
     */
    @Test
    public void testIdentifiableNodeOrElseGetWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            IdentifiableNode result = r
                    .identifiableNodeOrElseGet(() -> storage.createNodeReference("http://test.example/alice"));

            assertThat(result,
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElseGet(Supplier<IdentifiableNode>)} with
     * an empty result.
     */
    @Test
    public void testIdentifiableNodeOrElseGetWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            IdentifiableNode result = r
                    .identifiableNodeOrElseGet(() -> storage.createNodeReference("http://test.example/alice"));

            assertThat(result,
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElseGet(Supplier<IdentifiableNode>)} with
     * one IdentifiableNode and a Leaf.
     */
    @Test
    public void testIdentifiableNodeOrElseGetWithAnIdentifiableNodeAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createLeaf("In aqua bacillus est", "la"));
            IdentifiableNode result = r
                    .identifiableNodeOrElseGet(() -> storage.createNodeReference("http://test.example/charlie"));

            assertThat(result,
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElseGet(Supplier<IdentifiableNode>)} with
     * one IdentifiableNode.
     */
    @Test
    public void testIdentifiableNodeOrElseGetWithOneIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                (IdentifiableNode) storage.createNodeReference("http://test.example/alice"));
            IdentifiableNode result = r
                    .identifiableNodeOrElseGet(() -> storage.createNodeReference("http://test.example/bob"));

            assertThat(result,
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElseGet(Supplier<IdentifiableNode>)} with
     * two IdentifiableNodes.
     */
    @Test
    public void testIdentifiableNodeOrElseGetWithTwoIdentifiableNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            IdentifiableNode result = r
                    .identifiableNodeOrElseGet(() -> storage.createNodeReference("http://test.example/charlie"));

            assertThat(result,
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElseGet(Supplier<IdentifiableNode>)} with
     * two IdentifiableNodes and a LangString.
     */
    @Test
    public void testIdentifiableNodeOrElseGetWithTwoIdentifiableNodesAndALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"),
                storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));
            IdentifiableNode result = r
                    .identifiableNodeOrElseGet(() -> storage.createNodeReference("http://test.example/dave"));

            assertThat(result, is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElse(IdentifiableNode)} with a Leaf.
     */
    @Test
    public void testIdentifiableNodeOrElseWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("In vino veritas est", "la"));
            IdentifiableNode result = r
                    .identifiableNodeOrElse(storage.createNodeReference("http://test.example/alice"));

            assertThat(result,
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElse(IdentifiableNode)} with an empty
     * result.
     */
    @Test
    public void testIdentifiableNodeOrElseWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            IdentifiableNode result = r
                    .identifiableNodeOrElse(storage.createNodeReference("http://test.example/alice"));

            assertThat(result,
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElse(IdentifiableNode)} with one
     * IdentifiableNode and a Node.
     */
    @Test
    public void testIdentifiableNodeOrElseWithAnIdentifiableNodeAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            IdentifiableNode result = r
                    .identifiableNodeOrElse(storage.createNodeReference("http://test.example/charlie"));

            assertThat(result,
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElse(IdentifiableNode)} with one
     * IdentifiableNode.
     */
    @Test
    public void testIdentifiableNodeOrElseWithOneIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                (IdentifiableNode) storage.createNodeReference("http://test.example/alice"));
            IdentifiableNode result = r.identifiableNodeOrElse(storage.createNodeReference("http://test.example/bob"));

            assertThat(result,
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElse(IdentifiableNode)} with two
     * IdentifiableNodes.
     */
    @Test
    public void testIdentifiableNodeOrElseWithTwoIdentifiableNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            IdentifiableNode result = r
                    .identifiableNodeOrElse(storage.createNodeReference("http://test.example/charlie"));

            assertThat(result,
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code identifiableNodeOrElse(IdentifiableNode)} with two
     * IdentifiableNodes and a Node.
     */
    @Test
    public void testIdentifiableNodeOrElseWithTwoIdentifiableNodesAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"),
                storage.createNode("http://test.example/charlie"));
            IdentifiableNode result = r.identifiableNodeOrElse(storage.createNodeReference("http://test.example/dave"));

            assertThat(result, is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code identifiableNodes()} with a Literal.
     */
    @Test
    public void testIdentifiableNodesWithALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));

            assertThat(r.identifiableNodes(), hasSize(0));
        }
    }

    /**
     * Tests {@code identifiableNodes()} with an empty result.
     */
    @Test
    public void testIdentifiableNodesWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.identifiableNodes(), hasSize(0));
        }
    }

    /**
     * Tests {@code identifiableNodes()} with one IdentifiableNode and a Leaf.
     */
    @Test
    public void testIdentifiableNodesWithAnIdentifiableNodeAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createLeaf("In aqua bacillus est", "la"));

            assertThat(r.identifiableNodes(), hasSize(1));
        }
    }

    /**
     * Tests {@code identifiableNodes()} with one IdentifiableNode.
     */
    @Test
    public void testIdentifiableNodesWithOneIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                (IdentifiableNode) storage.createNodeReference("http://test.example/alice"));

            assertThat(r.identifiableNodes(), hasSize(1));
        }
    }

    /**
     * Tests {@code identifiableNodes()} with two IdentifiableNodes.
     */
    @Test
    public void testIdentifiableNodesWithTwoIdentifiableNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.identifiableNodes(), hasSize(2));
        }
    }

    /**
     * Tests {@code identifiableNodes()} with two IdentifiableNodes and a
     * Literal.
     */
    @Test
    public void testIdentifiableNodesWithTwoIdentifiableNodesAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"), storage.createLiteral("42", RDF.PLAIN_LITERAL));

            assertThat(r.identifiableNodes(), hasSize(2));
        }
    }

    /**
     * Tests {@code identifiableNode()} with an empty result.
     */
    @Test
    public void testIdentifiableNodeWithAnEmptyResult() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.identifiableNode();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code identifiableNode()} with one IdentifiableNode and a Node.
     */
    @Test
    public void testIdentifiableNodeWithAnIdentifiableNodeAndANode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.identifiableNode();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code identifiableNode()} with a Node.
     */
    @Test
    public void testIdentifiableNodeWithANode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            try {
                r.identifiableNode();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code identifiableNode()} with one IdentifiableNode.
     */
    @Test
    public void testIdentifiableNodeWithOneIdentifiableNode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                (IdentifiableNode) storage.createNodeReference("http://test.example/alice"));

            assertThat(r.identifiableNode(),
                is(equalTo((IdentifiableNode) storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code identifiableNode()} with two IdentifiableNodes.
     */
    @Test
    public void testIdentifiableNodeWithTwoIdentifiableNodes() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            try {
                r.identifiableNode();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code identifiableNode()} with two IdentifiableNodes and a Leaf.
     */
    @Test
    public void testIdentifiableNodeWithTwoIdentifiableNodesAndALeaf() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"),
                storage.createLeaf("Das Pferd frißt keinen Gurkensalat.", "de"));
            try {
                r.identifiableNode();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code isAnyAccessibleObject()} with one AccessibleObject and a
     * NodeReference.
     */
    @Test
    public void testIsAnyAccessibleObjectWithAnAccessibleObjectAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.isAnyAccessibleObject(), is(true));
        }
    }

    /**
     * Tests {@code isAnyAccessibleObject()} with an empty result.
     */
    @Test
    public void testIsAnyAccessibleObjectWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isAnyAccessibleObject(), is(false));
        }
    }

    /**
     * Tests {@code isAnyAccessibleObject()} with a NodeReference.
     */
    @Test
    public void testIsAnyAccessibleObjectWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.isAnyAccessibleObject(), is(false));
        }
    }

    /**
     * Tests {@code isAnyAccessibleObject()} with one AccessibleObject.
     */
    @Test
    public void testIsAnyAccessibleObjectWithOneAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createNode("http://test.example/alice"));

            assertThat(r.isAnyAccessibleObject(), is(true));
        }
    }

    /**
     * Tests {@code isAnyAccessibleObject()} with two AccessibleObjects.
     */
    @Test
    public void testIsAnyAccessibleObjectWithTwoAccessibleObjects() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isAnyAccessibleObject(), is(true));
        }
    }

    /**
     * Tests {@code isAnyAccessibleObject()} with two AccessibleObjects and a
     * NodeReference.
     */
    @Test
    public void testIsAnyAccessibleObjectWithTwoAccessibleObjectsAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));

            assertThat(r.isAnyAccessibleObject(), is(true));
        }
    }

    /**
     * Tests {@code isAnyIdentifiableNode()} with a LangString.
     */
    @Test
    public void testIsAnyIdentifiableNodeWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));

            assertThat(r.isAnyIdentifiableNode(), is(false));
        }
    }

    /**
     * Tests {@code isAnyIdentifiableNode()} with an empty result.
     */
    @Test
    public void testIsAnyIdentifiableNodeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isAnyIdentifiableNode(), is(false));
        }
    }

    /**
     * Tests {@code isAnyIdentifiableNode()} with one IdentifiableNode and a
     * Node.
     */
    @Test
    public void testIsAnyIdentifiableNodeWithAnIdentifiableNodeAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isAnyIdentifiableNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyIdentifiableNode()} with one IdentifiableNode.
     */
    @Test
    public void testIsAnyIdentifiableNodeWithOneIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                (IdentifiableNode) storage.createNodeReference("http://test.example/alice"));

            assertThat(r.isAnyIdentifiableNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyIdentifiableNode()} with two IdentifiableNodes.
     */
    @Test
    public void testIsAnyIdentifiableNodeWithTwoIdentifiableNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.isAnyIdentifiableNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyIdentifiableNode()} with two IdentifiableNodes and a
     * Node.
     */
    @Test
    public void testIsAnyIdentifiableNodeWithTwoIdentifiableNodesAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"),
                storage.createNode("http://test.example/charlie"));

            assertThat(r.isAnyIdentifiableNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyLangString()} with one LangString and a NamedNode.
     */
    @Test
    public void testIsAnyLangStringWithALangStringAndANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createNamedNode("http://test.example/bob"));

            assertThat(r.isAnyLangString(), is(true));
        }
    }

    /**
     * Tests {@code isAnyLangString()} with a Leaf.
     */
    @Test
    public void testIsAnyLangStringWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("http://test.example/charlie", ""));

            assertThat(r.isAnyLangString(), is(false));
        }
    }

    /**
     * Tests {@code isAnyLangString()} with an empty result.
     */
    @Test
    public void testIsAnyLangStringWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isAnyLangString(), is(false));
        }
    }

    /**
     * Tests {@code isAnyLangString()} with one LangString.
     */
    @Test
    public void testIsAnyLangStringWithOneLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));

            assertThat(r.isAnyLangString(), is(true));
        }
    }

    /**
     * Tests {@code isAnyLangString()} with two LangStrings.
     */
    @Test
    public void testIsAnyLangStringWithTwoLangStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"));

            assertThat(r.isAnyLangString(), is(true));
        }
    }

    /**
     * Tests {@code isAnyLangString()} with two LangStrings and a NamedNode.
     */
    @Test
    public void testIsAnyLangStringWithTwoLangStringsAndANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"),
                storage.createNamedNode("http://test.example/charlie"));

            assertThat(r.isAnyLangString(), is(true));
        }
    }

    /**
     * Tests {@code isAnyLiteral()} with one Literal and a Leaf.
     */
    @Test
    public void testIsAnyLiteralWithALiteralAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLeaf("In aqua bacillus est", "la"));

            assertThat(r.isAnyLiteral(), is(true));
        }
    }

    /**
     * Tests {@code isAnyLiteral()} with a NamedNode.
     */
    @Test
    public void testIsAnyLiteralWithANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));

            assertThat(r.isAnyLiteral(), is(false));
        }
    }

    /**
     * Tests {@code isAnyLiteral()} with an empty result.
     */
    @Test
    public void testIsAnyLiteralWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isAnyLiteral(), is(false));
        }
    }

    /**
     * Tests {@code isAnyLiteral()} with one Literal.
     */
    @Test
    public void testIsAnyLiteralWithOneLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));

            assertThat(r.isAnyLiteral(), is(true));
        }
    }

    /**
     * Tests {@code isAnyLiteral()} with two Literals.
     */
    @Test
    public void testIsAnyLiteralWithTwoLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));

            assertThat(r.isAnyLiteral(), is(true));
        }
    }

    /**
     * Tests {@code isAnyLiteral()} with two Literals and a NamedNode.
     */
    @Test
    public void testIsAnyLiteralWithTwoLiteralsAndANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL),
                storage.createNamedNode("http://test.example/charlie"));

            assertThat(r.isAnyLiteral(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNamedNode()} with a Leaf.
     */
    @Test
    public void testIsAnyNamedNodeWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("In vino veritas est", "la"));

            assertThat(r.isAnyNamedNode(), is(false));
        }
    }

    /**
     * Tests {@code isAnyNamedNode()} with one NamedNode and a Leaf.
     */
    @Test
    public void testIsAnyNamedNodeWithANamedNodeAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createLeaf("In aqua bacillus est", "la"));

            assertThat(r.isAnyNamedNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNamedNode()} with an empty result.
     */
    @Test
    public void testIsAnyNamedNodeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isAnyNamedNode(), is(false));
        }
    }

    /**
     * Tests {@code isAnyNamedNode()} with one NamedNode.
     */
    @Test
    public void testIsAnyNamedNodeWithOneNamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));

            assertThat(r.isAnyNamedNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNamedNode()} with two NamedNodes.
     */
    @Test
    public void testIsAnyNamedNodeWithTwoNamedNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"));

            assertThat(r.isAnyNamedNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNamedNode()} with two NamedNodes and a Leaf.
     */
    @Test
    public void testIsAnyNamedNodeWithTwoNamedNodesAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"),
                storage.createLeaf("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(r.isAnyNamedNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNodeReference()} with a LangString.
     */
    @Test
    public void testIsAnyNodeReferenceWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));

            assertThat(r.isAnyNodeReference(), is(false));
        }
    }

    /**
     * Tests {@code isAnyNodeReference()} with an empty result.
     */
    @Test
    public void testIsAnyNodeReferenceWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isAnyNodeReference(), is(false));
        }
    }

    /**
     * Tests {@code isAnyNodeReference()} with one NodeReference and a
     * LangString.
     */
    @Test
    public void testIsAnyNodeReferenceWithANodeReferenceAndALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createLangString("In aqua bacillus est", "la"));

            assertThat(r.isAnyNodeReference(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNodeReference()} with one NodeReference.
     */
    @Test
    public void testIsAnyNodeReferenceWithOneNodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.isAnyNodeReference(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNodeReference()} with two NodeReferences.
     */
    @Test
    public void testIsAnyNodeReferenceWithTwoNodeReferences() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.isAnyNodeReference(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNodeReference()} with two NodeReferences and a
     * LangString.
     */
    @Test
    public void testIsAnyNodeReferenceWithTwoNodeReferencesAndALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"),
                storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(r.isAnyNodeReference(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNodeType()} with an AccessibleObject.
     */
    @Test
    public void testIsAnyNodeTypeWithAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createLeaf("Lorem ipsum dolor sit amet", "la"));

            assertThat(r.isAnyNodeType(), is(false));
        }
    }

    /**
     * Tests {@code isAnyNodeType()} with an empty result.
     */
    @Test
    public void testIsAnyNodeTypeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isAnyNodeType(), is(false));
        }
    }

    /**
     * Tests {@code isAnyNodeType()} with one NodeType and an AccessibleObject.
     */
    @Test
    public void testIsAnyNodeTypeWithANodeTypeAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isAnyNodeType(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNodeType()} with one NodeType.
     */
    @Test
    public void testIsAnyNodeTypeWithOneNodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (NodeType) storage.createNode("http://test.example/alice"));

            assertThat(r.isAnyNodeType(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNodeType()} with two NodeTypes.
     */
    @Test
    public void testIsAnyNodeTypeWithTwoNodeTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isAnyNodeType(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNodeType()} with two NodeTypes and an AccessibleObject.
     */
    @Test
    public void testIsAnyNodeTypeWithTwoNodeTypesAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"), storage.createNode("http://test.example/charlie"));

            assertThat(r.isAnyNodeType(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNode()} with a LangString.
     */
    @Test
    public void testIsAnyNodeWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));

            assertThat(r.isAnyNode(), is(false));
        }
    }

    /**
     * Tests {@code isAnyNode()} with an empty result.
     */
    @Test
    public void testIsAnyNodeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isAnyNode(), is(false));
        }
    }

    /**
     * Tests {@code isAnyNode()} with one Node and a Leaf.
     */
    @Test
    public void testIsAnyNodeWithANodeAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createLeaf("In aqua bacillus est", "la"));

            assertThat(r.isAnyNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNode()} with one Node.
     */
    @Test
    public void testIsAnyNodeWithOneNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));

            assertThat(r.isAnyNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNode()} with two Nodes.
     */
    @Test
    public void testIsAnyNodeWithTwoNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isAnyNode(), is(true));
        }
    }

    /**
     * Tests {@code isAnyNode()} with two Nodes and a Leaf.
     */
    @Test
    public void testIsAnyNodeWithTwoNodesAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createLeaf("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(r.isAnyNode(), is(true));
        }
    }

    /**
     * Tests {@code isAny()} with an empty result.
     */
    @Test
    public void testIsAnyWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isAny(), is(false));
        }
    }

    /**
     * Tests {@code isAny()} with one ObjectType.
     */
    @Test
    public void testIsAnyWithOneObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (ObjectType) storage.createNode("http://test.example/alice"));

            assertThat(r.isAny(), is(true));
        }
    }

    /**
     * Tests {@code isAny()} with two ObjectTypes.
     */
    @Test
    public void testIsAnyWithTwoObjectTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isAny(), is(true));
        }
    }

    /**
     * Tests {@code isUniqueAccessibleObject()} with one AccessibleObject and a
     * NodeReference.
     */
    @Test
    public void testIsUniqueAccessibleObjectWithAnAccessibleObjectAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.isUniqueAccessibleObject(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueAccessibleObject()} with an empty result.
     */
    @Test
    public void testIsUniqueAccessibleObjectWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isUniqueAccessibleObject(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueAccessibleObject()} with a NodeReference.
     */
    @Test
    public void testIsUniqueAccessibleObjectWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.isUniqueAccessibleObject(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueAccessibleObject()} with one AccessibleObject.
     */
    @Test
    public void testIsUniqueAccessibleObjectWithOneAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createNode("http://test.example/alice"));

            assertThat(r.isUniqueAccessibleObject(), is(true));
        }
    }

    /**
     * Tests {@code isUniqueAccessibleObject()} with two AccessibleObjects.
     */
    @Test
    public void testIsUniqueAccessibleObjectWithTwoAccessibleObjects() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isUniqueAccessibleObject(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueAccessibleObject()} with two AccessibleObjects and a
     * NodeReference.
     */
    @Test
    public void testIsUniqueAccessibleObjectWithTwoAccessibleObjectsAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));

            assertThat(r.isUniqueAccessibleObject(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueIdentifiableNode()} with a Leaf.
     */
    @Test
    public void testIsUniqueIdentifiableNodeWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("In vino veritas est", "la"));

            assertThat(r.isUniqueIdentifiableNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueIdentifiableNode()} with an empty result.
     */
    @Test
    public void testIsUniqueIdentifiableNodeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isUniqueIdentifiableNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueIdentifiableNode()} with one IdentifiableNode and a
     * Literal.
     */
    @Test
    public void testIsUniqueIdentifiableNodeWithAnIdentifiableNodeAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));

            assertThat(r.isUniqueIdentifiableNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueIdentifiableNode()} with one IdentifiableNode.
     */
    @Test
    public void testIsUniqueIdentifiableNodeWithOneIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                (IdentifiableNode) storage.createNodeReference("http://test.example/alice"));

            assertThat(r.isUniqueIdentifiableNode(), is(true));
        }
    }

    /**
     * Tests {@code isUniqueIdentifiableNode()} with two IdentifiableNodes.
     */
    @Test
    public void testIsUniqueIdentifiableNodeWithTwoIdentifiableNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.isUniqueIdentifiableNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueIdentifiableNode()} with two IdentifiableNodes and a
     * Node.
     */
    @Test
    public void testIsUniqueIdentifiableNodeWithTwoIdentifiableNodesAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"),
                storage.createNode("http://test.example/charlie"));

            assertThat(r.isUniqueIdentifiableNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueLangString()} with one LangString and a Leaf.
     */
    @Test
    public void testIsUniqueLangStringWithALangStringAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLeaf("In aqua bacillus est", "la"));

            assertThat(r.isUniqueLangString(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueLangString()} with a Leaf.
     */
    @Test
    public void testIsUniqueLangStringWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("http://test.example/charlie", "la"));

            assertThat(r.isUniqueLangString(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueLangString()} with an empty result.
     */
    @Test
    public void testIsUniqueLangStringWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isUniqueLangString(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueLangString()} with one LangString.
     */
    @Test
    public void testIsUniqueLangStringWithOneLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));

            assertThat(r.isUniqueLangString(), is(true));
        }
    }

    /**
     * Tests {@code isUniqueLangString()} with two LangStrings.
     */
    @Test
    public void testIsUniqueLangStringWithTwoLangStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"));

            assertThat(r.isUniqueLangString(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueLangString()} with two LangStrings and a NamedNode.
     */
    @Test
    public void testIsUniqueLangStringWithTwoLangStringsAndANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"),
                storage.createNamedNode("http://test.example/charlie"));

            assertThat(r.isUniqueLangString(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueLiteral()} with one Literal and a NamedNode.
     */
    @Test
    public void testIsUniqueLiteralWithALiteralAndANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createNamedNode("http://test.example/bob"));

            assertThat(r.isUniqueLiteral(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueLiteral()} with an empty result.
     */
    @Test
    public void testIsUniqueLiteralWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isUniqueLiteral(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueLiteral()} with a Node.
     */
    @Test
    public void testIsUniqueLiteralWithANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));

            assertThat(r.isUniqueLiteral(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueLiteral()} with one Literal.
     */
    @Test
    public void testIsUniqueLiteralWithOneLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));

            assertThat(r.isUniqueLiteral(), is(true));
        }
    }

    /**
     * Tests {@code isUniqueLiteral()} with two Literals.
     */
    @Test
    public void testIsUniqueLiteralWithTwoLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));

            assertThat(r.isUniqueLiteral(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueLiteral()} with two Literals and a Leaf.
     */
    @Test
    public void testIsUniqueLiteralWithTwoLiteralsAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL),
                storage.createLeaf("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(r.isUniqueLiteral(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNamedNode()} with one NamedNode and a Literal.
     */
    @Test
    public void testIsUniqueNamedNodeWithANamedNodeAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));

            assertThat(r.isUniqueNamedNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNamedNode()} with an empty result.
     */
    @Test
    public void testIsUniqueNamedNodeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isUniqueNamedNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNamedNode()} with a NodeReference.
     */
    @Test
    public void testIsUniqueNamedNodeWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.isUniqueNamedNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNamedNode()} with one NamedNode.
     */
    @Test
    public void testIsUniqueNamedNodeWithOneNamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));

            assertThat(r.isUniqueNamedNode(), is(true));
        }
    }

    /**
     * Tests {@code isUniqueNamedNode()} with two NamedNodes.
     */
    @Test
    public void testIsUniqueNamedNodeWithTwoNamedNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"));

            assertThat(r.isUniqueNamedNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNamedNode()} with two NamedNodes and a Leaf.
     */
    @Test
    public void testIsUniqueNamedNodeWithTwoNamedNodesAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"),
                storage.createLeaf("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(r.isUniqueNamedNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNodeReference()} with an empty result.
     */
    @Test
    public void testIsUniqueNodeReferenceWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isUniqueNodeReference(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNodeReference()} with a Node.
     */
    @Test
    public void testIsUniqueNodeReferenceWithANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));

            assertThat(r.isUniqueNodeReference(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNodeReference()} with one NodeReference and a Node.
     */
    @Test
    public void testIsUniqueNodeReferenceWithANodeReferenceAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isUniqueNodeReference(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNodeReference()} with one NodeReference.
     */
    @Test
    public void testIsUniqueNodeReferenceWithOneNodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.isUniqueNodeReference(), is(true));
        }
    }

    /**
     * Tests {@code isUniqueNodeReference()} with two NodeReferences.
     */
    @Test
    public void testIsUniqueNodeReferenceWithTwoNodeReferences() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.isUniqueNodeReference(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNodeReference()} with two NodeReferences and a
     * NamedNode.
     */
    @Test
    public void testIsUniqueNodeReferenceWithTwoNodeReferencesAndANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"),
                storage.createNamedNode("http://test.example/charlie"));

            assertThat(r.isUniqueNodeReference(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNodeType()} with an AccessibleObject.
     */
    @Test
    public void testIsUniqueNodeTypeWithAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createLeaf("Lorem ipsum dolor sit amet", "la"));

            assertThat(r.isUniqueNodeType(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNodeType()} with an empty result.
     */
    @Test
    public void testIsUniqueNodeTypeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isUniqueNodeType(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNodeType()} with one NodeType and an
     * AccessibleObject.
     */
    @Test
    public void testIsUniqueNodeTypeWithANodeTypeAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isUniqueNodeType(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNodeType()} with one NodeType.
     */
    @Test
    public void testIsUniqueNodeTypeWithOneNodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (NodeType) storage.createNode("http://test.example/alice"));

            assertThat(r.isUniqueNodeType(), is(true));
        }
    }

    /**
     * Tests {@code isUniqueNodeType()} with two NodeTypes.
     */
    @Test
    public void testIsUniqueNodeTypeWithTwoNodeTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isUniqueNodeType(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNodeType()} with two NodeTypes and an
     * AccessibleObject.
     */
    @Test
    public void testIsUniqueNodeTypeWithTwoNodeTypesAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"), storage.createNode("http://test.example/charlie"));

            assertThat(r.isUniqueNodeType(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNode()} with a LangString.
     */
    @Test
    public void testIsUniqueNodeWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));

            assertThat(r.isUniqueNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNode()} with an empty result.
     */
    @Test
    public void testIsUniqueNodeWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isUniqueNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNode()} with one Node and a NodeReference.
     */
    @Test
    public void testIsUniqueNodeWithANodeAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.isUniqueNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNode()} with one Node.
     */
    @Test
    public void testIsUniqueNodeWithOneNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));

            assertThat(r.isUniqueNode(), is(true));
        }
    }

    /**
     * Tests {@code isUniqueNode()} with two Nodes.
     */
    @Test
    public void testIsUniqueNodeWithTwoNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isUniqueNode(), is(false));
        }
    }

    /**
     * Tests {@code isUniqueNode()} with two Nodes and a Leaf.
     */
    @Test
    public void testIsUniqueNodeWithTwoNodesAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createLeaf("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(r.isUniqueNode(), is(false));
        }
    }

    /**
     * Tests {@code isUnique()} with an empty result.
     */
    @Test
    public void testIsUniqueWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.isUnique(), is(false));
        }
    }

    /**
     * Tests {@code isUnique()} with one ObjectType.
     */
    @Test
    public void testIsUniqueWithOneObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (ObjectType) storage.createNode("http://test.example/alice"));

            assertThat(r.isUnique(), is(true));
        }
    }

    /**
     * Tests {@code isUnique()} with two ObjectTypes.
     */
    @Test
    public void testIsUniqueWithTwoObjectTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.isUnique(), is(false));
        }
    }

    /**
     * Tests {@code langStringExpectable()} with one LangString and a
     * NodeReference.
     */
    @Test
    public void testLangStringExpectableWithALangStringAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createNodeReference("http://test.example/bob"));
            try {
                r.langStringExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code langStringExpectable()} with a Leaf.
     */
    @Test
    public void testLangStringExpectableWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("http://test.example/bob", ""));
            try {
                r.langStringExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code langStringExpectable()} with an empty result.
     */
    @Test
    public void testLangStringExpectableWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.langStringExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code langStringExpectable()} with one LangString.
     */
    @Test
    public void testLangStringExpectableWithOneLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));

            assertThat(r.langStringExpectable(), is(equalTo(storage.createLangString("In vino veritas est", "la"))));
        }
    }

    /**
     * Tests {@code langStringExpectable()} with two LangStrings.
     */
    @Test
    public void testLangStringExpectableWithTwoLangStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"));
            try {
                r.langStringExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code langStringExpectable()} with two LangStrings and a Node.
     */
    @Test
    public void testLangStringExpectableWithTwoLangStringsAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"),
                storage.createNode("http://test.example/charlie"));
            try {
                r.langStringExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code langStringOrElseGet(Supplier<LangString>)} with one
     * LangString and a Node.
     */
    @Test
    public void testLangStringOrElseGetWithALangStringAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createNode("http://test.example/bob"));
            LangString result = r
                    .langStringOrElseGet(() -> storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(result, is(equalTo(storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"))));
        }
    }

    /**
     * Tests {@code langStringOrElseGet(Supplier<LangString>)} with a NamedNode.
     */
    @Test
    public void testLangStringOrElseGetWithANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));
            LangString result = r.langStringOrElseGet(() -> storage.createLangString("In vino veritas est", "la"));

            assertThat(result, is(equalTo(storage.createLangString("In vino veritas est", "la"))));
        }
    }

    /**
     * Tests {@code langStringOrElseGet(Supplier<LangString>)} with an empty
     * result.
     */
    @Test
    public void testLangStringOrElseGetWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            LangString result = r.langStringOrElseGet(() -> storage.createLangString("In vino veritas est", "la"));

            assertThat(result, is(equalTo(storage.createLangString("In vino veritas est", "la"))));
        }
    }

    /**
     * Tests {@code langStringOrElseGet(Supplier<LangString>)} with one
     * LangString.
     */
    @Test
    public void testLangStringOrElseGetWithOneLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            LangString result = r.langStringOrElseGet(() -> storage.createLangString("In aqua bacillus est", "la"));

            assertThat(result, is(equalTo(storage.createLangString("In vino veritas est", "la"))));
        }
    }

    /**
     * Tests {@code langStringOrElseGet(Supplier<LangString>)} with two
     * LangStrings.
     */
    @Test
    public void testLangStringOrElseGetWithTwoLangStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"));
            LangString result = r
                    .langStringOrElseGet(() -> storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(result, is(equalTo(storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"))));
        }
    }

    /**
     * Tests {@code langStringOrElseGet(Supplier<LangString>)} with two
     * LangStrings and a NamedNode.
     */
    @Test
    public void testLangStringOrElseGetWithTwoLangStringsAndANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"),
                storage.createNamedNode("http://test.example/charlie"));
            LangString result = r.langStringOrElseGet(() -> storage.createLangString("Hoc est corpus meum.", "la"));

            assertThat(result, is(equalTo(storage.createLangString("Hoc est corpus meum.", "la"))));
        }
    }

    /**
     * Tests {@code langStringOrElse(LangString)} with one LangString and a
     * Literal.
     */
    @Test
    public void testLangStringOrElseWithALangStringAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            LangString result = r
                    .langStringOrElse(storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(result, is(equalTo(storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"))));
        }
    }

    /**
     * Tests {@code langStringOrElse(LangString)} with an empty result.
     */
    @Test
    public void testLangStringOrElseWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            LangString result = r.langStringOrElse(storage.createLangString("In vino veritas est", "la"));

            assertThat(result, is(equalTo(storage.createLangString("In vino veritas est", "la"))));
        }
    }

    /**
     * Tests {@code langStringOrElse(LangString)} with a Node.
     */
    @Test
    public void testLangStringOrElseWithANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            LangString result = r.langStringOrElse(storage.createLangString("In vino veritas est", "la"));

            assertThat(result, is(equalTo(storage.createLangString("In vino veritas est", "la"))));
        }
    }

    /**
     * Tests {@code langStringOrElse(LangString)} with one LangString.
     */
    @Test
    public void testLangStringOrElseWithOneLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            LangString result = r.langStringOrElse(storage.createLangString("In aqua bacillus est", "la"));

            assertThat(result, is(equalTo(storage.createLangString("In vino veritas est", "la"))));
        }
    }

    /**
     * Tests {@code langStringOrElse(LangString)} with two LangStrings.
     */
    @Test
    public void testLangStringOrElseWithTwoLangStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"));
            LangString result = r
                    .langStringOrElse(storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(result, is(equalTo(storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"))));
        }
    }

    /**
     * Tests {@code langStringOrElse(LangString)} with two LangStrings and a
     * Node.
     */
    @Test
    public void testLangStringOrElseWithTwoLangStringsAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"),
                storage.createNode("http://test.example/charlie"));
            LangString result = r.langStringOrElse(storage.createLangString("Hoc est corpus meum.", "la"));

            assertThat(result, is(equalTo(storage.createLangString("Hoc est corpus meum.", "la"))));
        }
    }

    /**
     * Tests {@code langStrings()} with one LangString and a Node.
     */
    @Test
    public void testLangStringsWithALangStringAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.langStrings(), hasSize(1));
        }
    }

    /**
     * Tests {@code langStrings()} with an empty result.
     */
    @Test
    public void testLangStringsWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.langStrings(), hasSize(0));
        }
    }

    /**
     * Tests {@code langStrings()} with a NodeReference.
     */
    @Test
    public void testLangStringsWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.langStrings(), hasSize(0));
        }
    }

    /**
     * Tests {@code langStrings()} with one LangString.
     */
    @Test
    public void testLangStringsWithOneLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));

            assertThat(r.langStrings(), hasSize(1));
        }
    }

    /**
     * Tests {@code langStrings()} with two LangStrings.
     */
    @Test
    public void testLangStringsWithTwoLangStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"));

            assertThat(r.langStrings(), hasSize(2));
        }
    }

    /**
     * Tests {@code langStrings()} with two LangStrings and a NodeReference.
     */
    @Test
    public void testLangStringsWithTwoLangStringsAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"),
                storage.createNodeReference("http://test.example/charlie"));

            assertThat(r.langStrings(), hasSize(2));
        }
    }

    /**
     * Tests {@code langString()} with one LangString and a Literal.
     */
    @Test
    public void testLangStringWithALangStringAndALiteral() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            try {
                r.langString();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code langString()} with an empty result.
     */
    @Test
    public void testLangStringWithAnEmptyResult() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.langString();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code langString()} with a NodeReference.
     */
    @Test
    public void testLangStringWithANodeReference() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            try {
                r.langString();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code langString()} with one LangString.
     */
    @Test
    public void testLangStringWithOneLangString() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));

            assertThat(r.langString(), is(equalTo(storage.createLangString("In vino veritas est", "la"))));
        }
    }

    /**
     * Tests {@code langString()} with two LangStrings.
     */
    @Test
    public void testLangStringWithTwoLangStrings() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"));
            try {
                r.langString();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code langString()} with two LangStrings and a Literal.
     */
    @Test
    public void testLangStringWithTwoLangStringsAndALiteral() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"),
                storage.createLangString("In aqua bacillus est", "la"), storage.createLiteral("42", RDF.PLAIN_LITERAL));
            try {
                r.langString();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code leaves()}.
     */
    @Test
    public void testLeaves() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                storage.createNodeReference("http://example.org/bar"), storage.createNode("http://example.org/baz"),
                storage.createLeaf("Hello world!", "en"));

            Set<String> expected = new HashSet<>();
            expected.add("http://example.org/bar");
            expected.add("Hello world!");

            assertThat(r.leaves(), is(equalTo(expected)));
        }
    }

    /**
     * Tests {@code leaves(String)}.
     */
    @Test
    public void testLeavesString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                storage.createNodeReference("http://example.org/bar"), storage.createNode("http://example.org/baz"),
                storage.createLeaf("Hello world!", "en"));

            Set<String> expectedOneOf = new HashSet<>();
            expectedOneOf.add("http://example.org/bar ; Hello world!");
            expectedOneOf.add("Hello world! ; http://example.org/bar");

            assertThat(expectedOneOf.contains(r.leaves(" ; ")), is(true));
        }
    }

    /**
     * Tests {@code literalExpectable()} with one Literal and a Node.
     */
    @Test
    public void testLiteralExpectableWithALiteralAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createNode("http://test.example/bob"));
            try {
                r.literalExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code literalExpectable()} with an empty result.
     */
    @Test
    public void testLiteralExpectableWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.literalExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code literalExpectable()} with a Node.
     */
    @Test
    public void testLiteralExpectableWithANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            try {
                r.literalExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code literalExpectable()} with one Literal.
     */
    @Test
    public void testLiteralExpectableWithOneLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));

            assertThat(r.literalExpectable(),
                is(equalTo(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalExpectable()} with two Literals.
     */
    @Test
    public void testLiteralExpectableWithTwoLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            try {
                r.literalExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code literalExpectable()} with two Literals and a Node.
     */
    @Test
    public void testLiteralExpectableWithTwoLiteralsAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL),
                storage.createNode("http://test.example/charlie"));
            try {
                r.literalExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code literalOrElseGet(Supplier<Literal>)} with a Leaf.
     */
    @Test
    public void testLiteralOrElseGetWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("http://test.example/charlie", null));
            Literal result = r.literalOrElseGet(
                () -> storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));

            assertThat(result,
                is(equalTo(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElseGet(Supplier<Literal>)} with one Literal and a
     * NamedNode.
     */
    @Test
    public void testLiteralOrElseGetWithALiteralAndANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createNamedNode("http://test.example/bob"));
            Literal result = r.literalOrElseGet(() -> storage.createLiteral("42", RDF.PLAIN_LITERAL));

            assertThat(result, is(equalTo(storage.createLiteral("42", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElseGet(Supplier<Literal>)} with an empty result.
     */
    @Test
    public void testLiteralOrElseGetWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            Literal result = r.literalOrElseGet(
                () -> storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));

            assertThat(result,
                is(equalTo(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElseGet(Supplier<Literal>)} with one Literal.
     */
    @Test
    public void testLiteralOrElseGetWithOneLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));
            Literal result = r.literalOrElseGet(() -> storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));

            assertThat(result,
                is(equalTo(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElseGet(Supplier<Literal>)} with two Literals.
     */
    @Test
    public void testLiteralOrElseGetWithTwoLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            Literal result = r.literalOrElseGet(() -> storage.createLiteral("42", RDF.PLAIN_LITERAL));

            assertThat(result, is(equalTo(storage.createLiteral("42", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElseGet(Supplier<Literal>)} with two Literals and a
     * Node.
     */
    @Test
    public void testLiteralOrElseGetWithTwoLiteralsAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL),
                storage.createNode("http://test.example/charlie"));
            Literal result = r.literalOrElseGet(() -> storage.createLiteral(":(){:|:&};:", RDF.PLAIN_LITERAL));

            assertThat(result, is(equalTo(storage.createLiteral(":(){:|:&};:", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElse(Literal)} with one Literal and a Node.
     */
    @Test
    public void testLiteralOrElseWithALiteralAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createNode("http://test.example/bob"));
            Literal result = r.literalOrElse(storage.createLiteral("42", RDF.PLAIN_LITERAL));

            assertThat(result, is(equalTo(storage.createLiteral("42", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElse(Literal)} with an empty result.
     */
    @Test
    public void testLiteralOrElseWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            Literal result = r
                    .literalOrElse(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));

            assertThat(result,
                is(equalTo(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElse(Literal)} with a Node.
     */
    @Test
    public void testLiteralOrElseWithANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            Literal result = r
                    .literalOrElse(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));

            assertThat(result,
                is(equalTo(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElse(Literal)} with one Literal.
     */
    @Test
    public void testLiteralOrElseWithOneLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));
            Literal result = r.literalOrElse(storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));

            assertThat(result,
                is(equalTo(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElse(Literal)} with two Literals.
     */
    @Test
    public void testLiteralOrElseWithTwoLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            Literal result = r.literalOrElse(storage.createLiteral("42", RDF.PLAIN_LITERAL));

            assertThat(result, is(equalTo(storage.createLiteral("42", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literalOrElse(Literal)} with two Literals and a NamedNode.
     */
    @Test
    public void testLiteralOrElseWithTwoLiteralsAndANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL),
                storage.createNamedNode("http://test.example/charlie"));
            Literal result = r.literalOrElse(storage.createLiteral(":(){:|:&};:", RDF.PLAIN_LITERAL));

            assertThat(result, is(equalTo(storage.createLiteral(":(){:|:&};:", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literals()} with a Leaf.
     */
    @Test
    public void testLiteralsWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("http://test.example/alice", ""));

            assertThat(r.literals(), hasSize(0));
        }
    }

    /**
     * Tests {@code literals()} with one Literal and a NamedNode.
     */
    @Test
    public void testLiteralsWithALiteralAndANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createNamedNode("http://test.example/bob"));

            assertThat(r.literals(), hasSize(1));
        }
    }

    /**
     * Tests {@code literals()} with an empty result.
     */
    @Test
    public void testLiteralsWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.literals(), hasSize(0));
        }
    }

    /**
     * Tests {@code literals()} with one Literal.
     */
    @Test
    public void testLiteralsWithOneLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));

            assertThat(r.literals(), hasSize(1));
        }
    }

    /**
     * Tests {@code literals()} with two Literals.
     */
    @Test
    public void testLiteralsWithTwoLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));

            assertThat(r.literals(), hasSize(2));
        }
    }

    /**
     * Tests {@code literals()} with two Literals and a NodeReference.
     */
    @Test
    public void testLiteralsWithTwoLiteralsAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL),
                storage.createNodeReference("http://test.example/charlie"));

            assertThat(r.literals(), hasSize(2));
        }
    }

    /**
     * Tests {@code literal()} with a Leaf.
     */
    @Test
    public void testLiteralWithALeaf() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("http://test.example/charlie", "la"));
            try {
                r.literal();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code literal()} with one Literal and a Node.
     */
    @Test
    public void testLiteralWithALiteralAndANode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createNode("http://test.example/bob"));
            try {
                r.literal();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code literal()} with an empty result.
     */
    @Test
    public void testLiteralWithAnEmptyResult() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.literal();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code literal()} with one Literal.
     */
    @Test
    public void testLiteralWithOneLiteral() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));

            assertThat(r.literal(),
                is(equalTo(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL))));
        }
    }

    /**
     * Tests {@code literal()} with two Literals.
     */
    @Test
    public void testLiteralWithTwoLiterals() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            try {
                r.literal();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code literal()} with two Literals and a Leaf.
     */
    @Test
    public void testLiteralWithTwoLiteralsAndALeaf() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL),
                storage.createLeaf("Das Pferd frißt keinen Gurkensalat.", "de"));
            try {
                r.literal();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code namedNodeExpectable()} with a Literal.
     */
    @Test
    public void testNamedNodeExpectableWithALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage,
                storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));
            try {
                r.namedNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code namedNodeExpectable()} with one NamedNode and a
     * NodeReference.
     */
    @Test
    public void testNamedNodeExpectableWithANamedNodeAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            try {
                r.namedNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code namedNodeExpectable()} with an empty result.
     */
    @Test
    public void testNamedNodeExpectableWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.namedNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code namedNodeExpectable()} with one NamedNode.
     */
    @Test
    public void testNamedNodeExpectableWithOneNamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));

            assertThat(r.namedNodeExpectable(), is(equalTo(storage.createNamedNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code namedNodeExpectable()} with two NamedNodes.
     */
    @Test
    public void testNamedNodeExpectableWithTwoNamedNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"));
            try {
                r.namedNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code namedNodeExpectable()} with two NamedNodes and a Leaf.
     */
    @Test
    public void testNamedNodeExpectableWithTwoNamedNodesAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"),
                storage.createLeaf("Das Pferd frißt keinen Gurkensalat.", "de"));
            try {
                r.namedNodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests
     * {@code namedNodeOrElseGet(Supplier<T extends Node & IdentifiableNode>)}
     * with one NamedNode and a LangString.
     */
    @Test
    public void testNamedNodeOrElseGetWithANamedNodeAndALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createLangString("In aqua bacillus est", "la"));
            Node result = r.namedNodeOrElseGet(() -> storage.createNamedNode("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests
     * {@code namedNodeOrElseGet(Supplier<T extends Node & IdentifiableNode>)}
     * with an empty result.
     */
    @Test
    public void testNamedNodeOrElseGetWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            Node result = r.namedNodeOrElseGet(() -> storage.createNamedNode("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests
     * {@code namedNodeOrElseGet(Supplier<T extends Node & IdentifiableNode>)}
     * with a Node.
     */
    @Test
    public void testNamedNodeOrElseGetWithANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            Node result = r.namedNodeOrElseGet(() -> storage.createNamedNode("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests
     * {@code namedNodeOrElseGet(Supplier<T extends Node & IdentifiableNode>)}
     * with one NamedNode.
     */
    @Test
    public void testNamedNodeOrElseGetWithOneNamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));
            Node result = r.namedNodeOrElseGet(() -> storage.createNamedNode("http://test.example/bob"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests
     * {@code namedNodeOrElseGet(Supplier<T extends Node & IdentifiableNode>)}
     * with two NamedNodes.
     */
    @Test
    public void testNamedNodeOrElseGetWithTwoNamedNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"));
            Node result = r.namedNodeOrElseGet(() -> storage.createNamedNode("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests
     * {@code namedNodeOrElseGet(Supplier<T extends Node & IdentifiableNode>)}
     * with two NamedNodes and a Leaf.
     */
    @Test
    public void testNamedNodeOrElseGetWithTwoNamedNodesAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"),
                storage.createLeaf("Das Pferd frißt keinen Gurkensalat.", "de"));
            Node result = r.namedNodeOrElseGet(() -> storage.createNamedNode("http://test.example/dave"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code namedNodeOrElse(NamedNode)} with a LangString.
     */
    @Test
    public void testNamedNodeOrElseWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            Node result = r.namedNodeOrElse(storage.createNamedNode("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code namedNodeOrElse(NamedNode)} with one NamedNode and a
     * Literal.
     */
    @Test
    public void testNamedNodeOrElseWithANamedNodeAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            Node result = r.namedNodeOrElse(storage.createNamedNode("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code namedNodeOrElse(NamedNode)} with an empty result.
     */
    @Test
    public void testNamedNodeOrElseWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            Node result = r.namedNodeOrElse(storage.createNamedNode("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code namedNodeOrElse(NamedNode)} with one NamedNode.
     */
    @Test
    public void testNamedNodeOrElseWithOneNamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));
            Node result = r.namedNodeOrElse(storage.createNamedNode("http://test.example/bob"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code namedNodeOrElse(NamedNode)} with two NamedNodes.
     */
    @Test
    public void testNamedNodeOrElseWithTwoNamedNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"));
            Node result = r.namedNodeOrElse(storage.createNamedNode("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code namedNodeOrElse(NamedNode)} with two NamedNodes and a
     * NodeReference.
     */
    @Test
    public void testNamedNodeOrElseWithTwoNamedNodesAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));
            Node result = r.namedNodeOrElse(storage.createNamedNode("http://test.example/dave"));

            assertThat(result, is(equalTo(storage.createNamedNode("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code namedNodes()} with one NamedNode and a NodeReference.
     */
    @Test
    public void testNamedNodesWithANamedNodeAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.namedNodes(), hasSize(1));
        }
    }

    /**
     * Tests {@code namedNodes()} with an empty result.
     */
    @Test
    public void testNamedNodesWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.namedNodes(), hasSize(0));
        }
    }

    /**
     * Tests {@code namedNodes()} with a NodeReference.
     */
    @Test
    public void testNamedNodesWithANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.namedNodes(), hasSize(0));
        }
    }

    /**
     * Tests {@code namedNodes()} with one NamedNode.
     */
    @Test
    public void testNamedNodesWithOneNamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));

            assertThat(r.namedNodes(), hasSize(1));
        }
    }

    /**
     * Tests {@code namedNodes()} with two NamedNodes.
     */
    @Test
    public void testNamedNodesWithTwoNamedNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"));

            assertThat(r.namedNodes(), hasSize(2));
        }
    }

    /**
     * Tests {@code namedNodes()} with two NamedNodes and a LangString.
     */
    @Test
    public void testNamedNodesWithTwoNamedNodesAndALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"),
                storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));

            assertThat(r.namedNodes(), hasSize(2));
        }
    }

    /**
     * Tests {@code namedNode()} with one NamedNode and a LangString.
     */
    @Test
    public void testNamedNodeWithANamedNodeAndALangString() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createLangString("In aqua bacillus est", "la"));
            try {
                r.namedNode();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code namedNode()} with an empty result.
     */
    @Test
    public void testNamedNodeWithAnEmptyResult() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.namedNode();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code namedNode()} with a NodeReference.
     */
    @Test
    public void testNamedNodeWithANodeReference() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            try {
                r.namedNode();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code namedNode()} with one NamedNode.
     */
    @Test
    public void testNamedNodeWithOneNamedNode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));

            assertThat(r.namedNode(), is(equalTo(storage.createNamedNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code namedNode()} with two NamedNodes.
     */
    @Test
    public void testNamedNodeWithTwoNamedNodes() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"));
            try {
                r.namedNode();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code namedNode()} with two NamedNodes and a LangString.
     */
    @Test
    public void testNamedNodeWithTwoNamedNodesAndALangString() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"),
                storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));
            try {
                r.namedNode();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeExpectable()} with a LangString.
     */
    @Test
    public void testNodeExpectableWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            try {
                r.nodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeExpectable()} with an empty result.
     */
    @Test
    public void testNodeExpectableWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.nodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeExpectable()} with one Node and a Leaf.
     */
    @Test
    public void testNodeExpectableWithANodeAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createLeaf("In aqua bacillus est", "la"));
            try {
                r.nodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeExpectable()} with one Node.
     */
    @Test
    public void testNodeExpectableWithOneNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));

            assertThat(r.nodeExpectable(), is(equalTo(storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeExpectable()} with two Nodes.
     */
    @Test
    public void testNodeExpectableWithTwoNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.nodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeExpectable()} with two Nodes and a NodeReference.
     */
    @Test
    public void testNodeExpectableWithTwoNodesAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));
            try {
                r.nodeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeOrElseGet(Supplier<Node>)} with a Leaf.
     */
    @Test
    public void testNodeOrElseGetWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("In vino veritas est", "la"));
            Node result = r.nodeOrElseGet(() -> storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeOrElseGet(Supplier<Node>)} with an empty result.
     */
    @Test
    public void testNodeOrElseGetWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            Node result = r.nodeOrElseGet(() -> storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeOrElseGet(Supplier<Node>)} with one Node and a
     * NodeReference.
     */
    @Test
    public void testNodeOrElseGetWithANodeAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            Node result = r.nodeOrElseGet(() -> storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeOrElseGet(Supplier<Node>)} with one Node.
     */
    @Test
    public void testNodeOrElseGetWithOneNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            Node result = r.nodeOrElseGet(() -> storage.createNode("http://test.example/bob"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeOrElseGet(Supplier<Node>)} with two Nodes.
     */
    @Test
    public void testNodeOrElseGetWithTwoNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            Node result = r.nodeOrElseGet(() -> storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeOrElseGet(Supplier<Node>)} with two Nodes and a
     * LangString.
     */
    @Test
    public void testNodeOrElseGetWithTwoNodesAndALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));
            Node result = r.nodeOrElseGet(() -> storage.createNode("http://test.example/dave"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code nodeOrElse(Node)} with a Leaf.
     */
    @Test
    public void testNodeOrElseWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("In vino veritas est", "la"));
            Node result = r.nodeOrElse(storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeOrElse(Node)} with an empty result.
     */
    @Test
    public void testNodeOrElseWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            Node result = r.nodeOrElse(storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeOrElse(Node)} with one Node and a Literal.
     */
    @Test
    public void testNodeOrElseWithANodeAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            Node result = r.nodeOrElse(storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeOrElse(Node)} with one Node.
     */
    @Test
    public void testNodeOrElseWithOneNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            Node result = r.nodeOrElse(storage.createNode("http://test.example/bob"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeOrElse(Node)} with two Nodes.
     */
    @Test
    public void testNodeOrElseWithTwoNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            Node result = r.nodeOrElse(storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeOrElse(Node)} with two Nodes and a Literal.
     */
    @Test
    public void testNodeOrElseWithTwoNodesAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"), storage.createLiteral("42", RDF.PLAIN_LITERAL));
            Node result = r.nodeOrElse(storage.createNode("http://test.example/dave"));

            assertThat(result, is(equalTo(storage.createNode("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code nodeReferenceExpectable()} with a LangString.
     */
    @Test
    public void testNodeReferenceExpectableWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));
            try {
                r.nodeReferenceExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeReferenceExpectable()} with an empty result.
     */
    @Test
    public void testNodeReferenceExpectableWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.nodeReferenceExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeReferenceExpectable()} with one NodeReference and a
     * LangString.
     */
    @Test
    public void testNodeReferenceExpectableWithANodeReferenceAndALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createLangString("In aqua bacillus est", "la"));
            try {
                r.nodeReferenceExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeReferenceExpectable()} with one NodeReference.
     */
    @Test
    public void testNodeReferenceExpectableWithOneNodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.nodeReferenceExpectable(),
                is(equalTo(storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeReferenceExpectable()} with two NodeReferences.
     */
    @Test
    public void testNodeReferenceExpectableWithTwoNodeReferences() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            try {
                r.nodeReferenceExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeReferenceExpectable()} with two NodeReferences and a
     * Node.
     */
    @Test
    public void testNodeReferenceExpectableWithTwoNodeReferencesAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"),
                storage.createNode("http://test.example/charlie"));
            try {
                r.nodeReferenceExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeReferenceOrElseGet(Supplier<NodeReference>)} with an
     * empty result.
     */
    @Test
    public void testNodeReferenceOrElseGetWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            NodeReference result = r
                    .nodeReferenceOrElseGet(() -> storage.createNodeReference("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElseGet(Supplier<NodeReference>)} with a
     * Node.
     */
    @Test
    public void testNodeReferenceOrElseGetWithANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            NodeReference result = r
                    .nodeReferenceOrElseGet(() -> storage.createNodeReference("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElseGet(Supplier<NodeReference>)} with one
     * NodeReference and a Node.
     */
    @Test
    public void testNodeReferenceOrElseGetWithANodeReferenceAndANode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            NodeReference result = r
                    .nodeReferenceOrElseGet(() -> storage.createNodeReference("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElseGet(Supplier<NodeReference>)} with one
     * NodeReference.
     */
    @Test
    public void testNodeReferenceOrElseGetWithOneNodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            NodeReference result = r
                    .nodeReferenceOrElseGet(() -> storage.createNodeReference("http://test.example/bob"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElseGet(Supplier<NodeReference>)} with two
     * NodeReferences.
     */
    @Test
    public void testNodeReferenceOrElseGetWithTwoNodeReferences() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            NodeReference result = r
                    .nodeReferenceOrElseGet(() -> storage.createNodeReference("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElseGet(Supplier<NodeReference>)} with two
     * NodeReferences and a LangString.
     */
    @Test
    public void testNodeReferenceOrElseGetWithTwoNodeReferencesAndALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"),
                storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));
            NodeReference result = r
                    .nodeReferenceOrElseGet(() -> storage.createNodeReference("http://test.example/dave"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElse(NodeReference)} with a NamedNode.
     */
    @Test
    public void testNodeReferenceOrElseWithANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://test.example/alice"));
            NodeReference result = r.nodeReferenceOrElse(storage.createNodeReference("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElse(NodeReference)} with an empty result.
     */
    @Test
    public void testNodeReferenceOrElseWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            NodeReference result = r.nodeReferenceOrElse(storage.createNodeReference("http://test.example/alice"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElse(NodeReference)} with one NodeReference
     * and a Leaf.
     */
    @Test
    public void testNodeReferenceOrElseWithANodeReferenceAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createLeaf("In aqua bacillus est", "la"));
            NodeReference result = r.nodeReferenceOrElse(storage.createNodeReference("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElse(NodeReference)} with one NodeReference.
     */
    @Test
    public void testNodeReferenceOrElseWithOneNodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            NodeReference result = r.nodeReferenceOrElse(storage.createNodeReference("http://test.example/bob"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElse(NodeReference)} with two NodeReferences.
     */
    @Test
    public void testNodeReferenceOrElseWithTwoNodeReferences() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            NodeReference result = r.nodeReferenceOrElse(storage.createNodeReference("http://test.example/charlie"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeReferenceOrElse(NodeReference)} with two NodeReferences
     * and a Literal.
     */
    @Test
    public void testNodeReferenceOrElseWithTwoNodeReferencesAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"), storage.createLiteral("42", RDF.PLAIN_LITERAL));
            NodeReference result = r.nodeReferenceOrElse(storage.createNodeReference("http://test.example/dave"));

            assertThat(result, is(equalTo(storage.createNodeReference("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code nodeReferences()} with a LangString.
     */
    @Test
    public void testNodeReferencesWithALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLangString("In vino veritas est", "la"));

            assertThat(r.nodeReferences(), hasSize(0));
        }
    }

    /**
     * Tests {@code nodeReferences()} with an empty result.
     */
    @Test
    public void testNodeReferencesWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.nodeReferences(), hasSize(0));
        }
    }

    /**
     * Tests {@code nodeReferences()} with one NodeReference and a Leaf.
     */
    @Test
    public void testNodeReferencesWithANodeReferenceAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createLeaf("In aqua bacillus est", "la"));

            assertThat(r.nodeReferences(), hasSize(1));
        }
    }

    /**
     * Tests {@code nodeReferences()} with one NodeReference.
     */
    @Test
    public void testNodeReferencesWithOneNodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.nodeReferences(), hasSize(1));
        }
    }

    /**
     * Tests {@code nodeReferences()} with two NodeReferences.
     */
    @Test
    public void testNodeReferencesWithTwoNodeReferences() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));

            assertThat(r.nodeReferences(), hasSize(2));
        }
    }

    /**
     * Tests {@code nodeReferences()} with two NodeReferences and a Literal.
     */
    @Test
    public void testNodeReferencesWithTwoNodeReferencesAndALiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"), storage.createLiteral("42", RDF.PLAIN_LITERAL));

            assertThat(r.nodeReferences(), hasSize(2));
        }
    }

    /**
     * Tests {@code nodeReference()} with an empty result.
     */
    @Test
    public void testNodeReferenceWithAnEmptyResult() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.nodeReference();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeReference()} with a Node.
     */
    @Test
    public void testNodeReferenceWithANode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));
            try {
                r.nodeReference();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeReference()} with one NodeReference and a NamedNode.
     */
    @Test
    public void testNodeReferenceWithANodeReferenceAndANamedNode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNamedNode("http://test.example/bob"));
            try {
                r.nodeReference();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeReference()} with one NodeReference.
     */
    @Test
    public void testNodeReferenceWithOneNodeReference() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));

            assertThat(r.nodeReference(), is(equalTo(storage.createNodeReference("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeReference()} with two NodeReferences.
     */
    @Test
    public void testNodeReferenceWithTwoNodeReferences() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"));
            try {
                r.nodeReference();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeReference()} with two NodeReferences and a Literal.
     */
    @Test
    public void testNodeReferenceWithTwoNodeReferencesAndALiteral() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"),
                storage.createNodeReference("http://test.example/bob"), storage.createLiteral("42", RDF.PLAIN_LITERAL));
            try {
                r.nodeReference();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodes()} with a Leaf.
     */
    @Test
    public void testNodesWithALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createLeaf("In vino veritas est", "la"));

            assertThat(r.nodes(), hasSize(0));
        }
    }

    /**
     * Tests {@code nodes()} with an empty result.
     */
    @Test
    public void testNodesWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.nodes(), hasSize(0));
        }
    }

    /**
     * Tests {@code nodes()} with one Node and a Leaf.
     */
    @Test
    public void testNodesWithANodeAndALeaf() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createLeaf("In aqua bacillus est", "la"));

            assertThat(r.nodes(), hasSize(1));
        }
    }

    /**
     * Tests {@code nodes()} with one Node.
     */
    @Test
    public void testNodesWithOneNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));

            assertThat(r.nodes(), hasSize(1));
        }
    }

    /**
     * Tests {@code nodes()} with two Nodes.
     */
    @Test
    public void testNodesWithTwoNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.nodes(), hasSize(2));
        }
    }

    /**
     * Tests {@code nodes()} with two Nodes and a NodeReference.
     */
    @Test
    public void testNodesWithTwoNodesAndANodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createNodeReference("http://test.example/charlie"));

            assertThat(r.nodes(), hasSize(2));
        }
    }

    /**
     * Tests {@code nodeTypeExpectable()} with an AccessibleObject.
     */
    @Test
    public void testNodeTypeExpectableWithAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createLeaf("Lorem ipsum dolor sit amet", "la"));
            try {
                r.nodeTypeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeTypeExpectable()} with an empty result.
     */
    @Test
    public void testNodeTypeExpectableWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.nodeTypeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeTypeExpectable()} with one NodeType and an
     * AccessibleObject.
     */
    @Test
    public void testNodeTypeExpectableWithANodeTypeAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.nodeTypeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeTypeExpectable()} with one NodeType.
     */
    @Test
    public void testNodeTypeExpectableWithOneNodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (NodeType) storage.createNode("http://test.example/alice"));

            assertThat(r.nodeTypeExpectable(), is(equalTo((NodeType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeTypeExpectable()} with two NodeTypes.
     */
    @Test
    public void testNodeTypeExpectableWithTwoNodeTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.nodeTypeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeTypeExpectable()} with two NodeTypes and an
     * AccessibleObject.
     */
    @Test
    public void testNodeTypeExpectableWithTwoNodeTypesAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"), storage.createNode("http://test.example/charlie"));
            try {
                r.nodeTypeExpectable();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeTypeOrElseGet(Supplier<NodeType>)} with an
     * AccessibleObject.
     */
    @Test
    public void testNodeTypeOrElseGetWithAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createNode("http://test.example/alice"));
            NodeType result = r.nodeTypeOrElseGet(() -> storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElseGet(Supplier<NodeType>)} with an empty result.
     */
    @Test
    public void testNodeTypeOrElseGetWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            NodeType result = r.nodeTypeOrElseGet(() -> storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElseGet(Supplier<NodeType>)} with one NodeType and
     * an AccessibleObject.
     */
    @Test
    public void testNodeTypeOrElseGetWithANodeTypeAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            NodeType result = r.nodeTypeOrElseGet(() -> storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElseGet(Supplier<NodeType>)} with one NodeType.
     */
    @Test
    public void testNodeTypeOrElseGetWithOneNodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (NodeType) storage.createNode("http://test.example/alice"));
            NodeType result = r.nodeTypeOrElseGet(() -> storage.createNode("http://test.example/bob"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElseGet(Supplier<NodeType>)} with two NodeTypes.
     */
    @Test
    public void testNodeTypeOrElseGetWithTwoNodeTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            NodeType result = r.nodeTypeOrElseGet(() -> storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElseGet(Supplier<NodeType>)} with two NodeTypes
     * and an AccessibleObject.
     */
    @Test
    public void testNodeTypeOrElseGetWithTwoNodeTypesAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"), storage.createNode("http://test.example/charlie"));
            NodeType result = r.nodeTypeOrElseGet(() -> storage.createNode("http://test.example/dave"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElse(NodeType)} with an AccessibleObject.
     */
    @Test
    public void testNodeTypeOrElseWithAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createNode("http://test.example/alice"));
            NodeType result = r.nodeTypeOrElse(storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElse(NodeType)} with an empty result.
     */
    @Test
    public void testNodeTypeOrElseWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            NodeType result = r.nodeTypeOrElse(storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElse(NodeType)} with one NodeType and an
     * AccessibleObject.
     */
    @Test
    public void testNodeTypeOrElseWithANodeTypeAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            NodeType result = r.nodeTypeOrElse(storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElse(NodeType)} with one NodeType.
     */
    @Test
    public void testNodeTypeOrElseWithOneNodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (NodeType) storage.createNode("http://test.example/alice"));
            NodeType result = r.nodeTypeOrElse(storage.createNode("http://test.example/bob"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElse(NodeType)} with two NodeTypes.
     */
    @Test
    public void testNodeTypeOrElseWithTwoNodeTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            NodeType result = r.nodeTypeOrElse(storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code nodeTypeOrElse(NodeType)} with two NodeTypes and an
     * AccessibleObject.
     */
    @Test
    public void testNodeTypeOrElseWithTwoNodeTypesAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"), storage.createNode("http://test.example/charlie"));
            NodeType result = r.nodeTypeOrElse(storage.createNode("http://test.example/dave"));

            assertThat(result, is(equalTo((NodeType) storage.createNode("http://test.example/dave"))));
        }
    }

    /**
     * Tests {@code nodeTypes()} with an AccessibleObject.
     */
    @Test
    public void testNodeTypesWithAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createLeaf("Lorem ipsum dolor sit amet", "la"));

            assertThat(r.nodeTypes(), hasSize(0));
        }
    }

    /**
     * Tests {@code nodeTypes()} with an empty result.
     */
    @Test
    public void testNodeTypesWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);

            assertThat(r.nodeTypes(), hasSize(0));
        }
    }

    /**
     * Tests {@code nodeTypes()} with one NodeType and an AccessibleObject.
     */
    @Test
    public void testNodeTypesWithANodeTypeAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createLeaf("Lorem ipsum dolor sit amet", "la"));

            assertThat(r.nodeTypes(), hasSize(1));
        }
    }

    /**
     * Tests {@code nodeTypes()} with one NodeType.
     */
    @Test
    public void testNodeTypesWithOneNodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (NodeType) storage.createNode("http://test.example/alice"));

            assertThat(r.nodeTypes(), hasSize(1));
        }
    }

    /**
     * Tests {@code nodeTypes()} with two NodeTypes.
     */
    @Test
    public void testNodeTypesWithTwoNodeTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));

            assertThat(r.nodeTypes(), hasSize(2));
        }
    }

    /**
     * Tests {@code nodeTypes()} with two NodeTypes and an AccessibleObject.
     */
    @Test
    public void testNodeTypesWithTwoNodeTypesAndAnAccessibleObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"), storage.createLeaf("Lorem ipsum dolor sit amet", "la"));

            assertThat(r.nodeTypes(), hasSize(2));
        }
    }

    /**
     * Tests {@code nodeType()} with an AccessibleObject.
     */
    @Test
    public void testNodeTypeWithAnAccessibleObject() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (AccessibleObject) storage.createLeaf("Lorem ipsum dolor sit amet", "la"));
            try {
                r.nodeType();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeType()} with an empty result.
     */
    @Test
    public void testNodeTypeWithAnEmptyResult() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.nodeType();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeType()} with one NodeType and an AccessibleObject.
     */
    @Test
    public void testNodeTypeWithANodeTypeAndAnAccessibleObject() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.nodeType();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeType()} with one NodeType.
     */
    @Test
    public void testNodeTypeWithOneNodeType() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (NodeType) storage.createNode("http://test.example/alice"));

            assertThat(r.nodeType(), is(equalTo((NodeType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code nodeType()} with two NodeTypes.
     */
    @Test
    public void testNodeTypeWithTwoNodeTypes() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.nodeType();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code nodeType()} with two NodeTypes and an AccessibleObject.
     */
    @Test
    public void testNodeTypeWithTwoNodeTypesAndAnAccessibleObject() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"), storage.createNode("http://test.example/charlie"));
            try {
                r.nodeType();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code node()} with an empty result.
     */
    @Test
    public void testNodeWithAnEmptyResult() throws LinkedDataException {
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

    /**
     * Tests {@code node()} with one Node and a Literal.
     */
    @Test
    public void testNodeWithANodeAndALiteral() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL));
            try {
                r.node();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code node()} with a NodeReference.
     */
    @Test
    public void testNodeWithANodeReference() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNodeReference("http://test.example/alice"));
            try {
                r.node();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code node()} with one Node.
     */
    @Test
    public void testNodeWithOneNode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"));

            assertThat(r.node(), is(equalTo(storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code node()} with two Nodes.
     */
    @Test
    public void testNodeWithTwoNodes() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.node();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code node()} with two Nodes and a LangString.
     */
    @Test
    public void testNodeWithTwoNodesAndALangString() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"),
                storage.createLangString("Das Pferd frißt keinen Gurkensalat.", "de"));
            try {
                r.node();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code orElseGet(Supplier<ObjectType>)} with an empty result.
     */
    @Test
    public void testOrElseGetWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            ObjectType result = r.orElseGet(() -> storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo((ObjectType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code orElseGet(Supplier<ObjectType>)} with one ObjectType.
     */
    @Test
    public void testOrElseGetWithOneObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (ObjectType) storage.createNode("http://test.example/alice"));
            ObjectType result = r.orElseGet(() -> storage.createNode("http://test.example/bob"));

            assertThat(result, is(equalTo((ObjectType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code orElseGet(Supplier<ObjectType>)} with two ObjectTypes.
     */
    @Test
    public void testOrElseGetWithTwoObjectTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            ObjectType result = r.orElseGet(() -> storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo((ObjectType) storage.createNode("http://test.example/charlie"))));
        }
    }

    /**
     * Tests {@code orElse(ObjectType)} with an empty result.
     */
    @Test
    public void testOrElseWithAnEmptyResult() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            ObjectType result = r.orElse(storage.createNode("http://test.example/alice"));

            assertThat(result, is(equalTo((ObjectType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code orElse(ObjectType)} with one ObjectType.
     */
    @Test
    public void testOrElseWithOneObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (ObjectType) storage.createNode("http://test.example/alice"));
            ObjectType result = r.orElse(storage.createNode("http://test.example/bob"));

            assertThat(result, is(equalTo((ObjectType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code orElse(ObjectType)} with two ObjectTypes.
     */
    @Test
    public void testOrElseWithTwoObjectTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            ObjectType result = r.orElse(storage.createNode("http://test.example/charlie"));

            assertThat(result, is(equalTo((ObjectType) storage.createNode("http://test.example/charlie"))));
        }
    }

    @Test
    public void testStrings() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                storage.createNodeReference("http://example.org/bar"), storage.createNode("http://example.org/baz"),
                storage.createLeaf("Hello world!", "en"),
                storage.createLeaf("public static void main(String[] args)", null));

            Set<String> expected = new HashSet<>();
            expected.add("Hello world!");
            expected.add("public static void main(String[] args)");

            assertThat(r.strings(), is(equalTo(expected)));
        }
    }

    /**
     * Tests {@code strings(String)}.
     */
    @Test
    public void testStringsString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNamedNode("http://example.org/foo"),
                storage.createNodeReference("http://example.org/bar"), storage.createNode("http://example.org/baz"),
                storage.createLeaf("Hello world!", "en"),
                storage.createLeaf("public static void main(String[] args)", null));

            assertThat(r.strings(" xxxfxxx "),
                is(anyOf(equalTo("public static void main(String[] args) xxxfxxx Hello world!"),
                    equalTo("Hello world! xxxfxxx public static void main(String[] args)"))));
        }
    }

    /**
     * Tests {@code value()} with an empty result.
     */
    @Test
    public void testValueWithAnEmptyResult() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage);
            try {
                r.value();
                fail(storage.getClass().getSimpleName() + " should throw NoDataException, but does not.");
            } catch (NoDataException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code value()} with one ObjectType.
     */
    @Test
    public void testValueWithOneObjectType() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, (ObjectType) storage.createNode("http://test.example/alice"));

            assertThat(r.value(), is(equalTo((ObjectType) storage.createNode("http://test.example/alice"))));
        }
    }

    /**
     * Tests {@code value()} with two ObjectTypes.
     */
    @Test
    public void testValueWithTwoObjectTypes() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Result r = createResult(storage, storage.createNode("http://test.example/alice"),
                storage.createNode("http://test.example/bob"));
            try {
                r.value();
                fail(storage.getClass().getSimpleName() + " should throw AmbiguousDataException, but does not.");
            } catch (AmbiguousDataException e) {
                /* expected */
            }
        }
    }

}
