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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.Test;
import org.kitodo.dataaccess.storage.memory.GraphPathTest;
import org.kitodo.dataaccess.storage.memory.MemoryNodeReference;

/**
 * Tests {@code org.kitodo.dataaccess.Node}.
 */
public class NodeTest {

    private static final Set<ObjectType> ANY_NON_EMPTY_RESULT = Collections.emptySet();
    private static final Set<String> ANY_RELATION = Collections.emptySet();
    private static final MemoryNodeReference METS_DIV = new MemoryNodeReference("http://www.loc.gov/METS/div");
    private static final MemoryNodeReference METS_MD_WRAP = new MemoryNodeReference("http://www.loc.gov/METS/mdWrap");
    private static final MemoryNodeReference METS_METS = new MemoryNodeReference("http://www.loc.gov/METS/mets");
    private static final MemoryNodeReference METS_METS_HDR = new MemoryNodeReference("http://www.loc.gov/METS/metsHdr");
    private static final MemoryNodeReference METS_ORDERLABEL = new MemoryNodeReference(
            "http://www.loc.gov/METS/ORDERLABEL");
    private static final MemoryNodeReference METS_STRUCT_MAP = new MemoryNodeReference(
            "http://www.loc.gov/METS/structMap");
    private static final MemoryNodeReference METS_TYPE = new MemoryNodeReference("http://www.loc.gov/METS/TYPE");
    private static final MemoryNodeReference METS_USE = new MemoryNodeReference("http://www.loc.gov/METS/USE");
    private static final MemoryNodeReference MODS_AUTHORITY = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#authority");
    private static final MemoryNodeReference MODS_CLASSIFICATION = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#classification");
    private static final MemoryNodeReference MODS_DISPLAY_FORM = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#displayForm");
    private static final MemoryNodeReference MODS_MODS = new MemoryNodeReference("http://www.loc.gov/mods/v3#mods");
    private static final MemoryNodeReference MODS_NAME = new MemoryNodeReference("http://www.loc.gov/mods/v3#name");

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
     * Tests {@code add()}.
     */

    @Test
    public void testAdd() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.createNode(METS_METS);
            mets.add(storage.createNode(METS_METS_HDR));

            assertThat(mets.get(RDF.toURL(Node.FIRST_INDEX)).size(), is(equalTo(1)));
        }
    }

    /**
     * Tests {@code addAll(Collection<? extends ObjectType>)}.
     */
    @Test
    public void testAddAllCollection() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode();

            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNode(MODS_MODS);
            ArrayList<ObjectType> toAdd = new ArrayList<>();
            toAdd.add(javac);
            toAdd.add(mods);

            node.addAll(toAdd);

            assertThat(node.size(), is(equalTo(2)));
            assertThat(node.first().orElse(null), is(equalTo((long) Node.FIRST_INDEX)));
            assertThat(node.last().orElse(null), is(equalTo((long) 2)));
            assertThat(node.getFirst(), contains(javac));
            assertThat(node.getLast(), contains(mods));
        }
    }

    /**
     * Tests {@code addFirst(ObjectType)}.
     */
    @Test
    public void testAddFirst() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode();

            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNode(MODS_MODS);

            node.add(mods);
            node.addFirst(javac);

            assertThat(node.size(), is(equalTo(2)));
            assertThat(node.first().orElse(null), is(equalTo((long) Node.FIRST_INDEX)));
            assertThat(node.last().orElse(null), is(equalTo((long) 2)));
            assertThat(node.getFirst(), contains(javac));
            assertThat(node.getLast(), contains(mods));
        }
    }

    /**
     * Tests {@code asUnordered(boolean)}.
     */
    @Test
    public void testAsUnordered() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node testNode = storage.createNode().add(storage.createNode(MODS_NAME).setValue("Max"))
                    .add(storage.createNode(MODS_NAME).setValue("Moritz")).put(METS_USE, "foobar");

            ObjectType outcome = testNode.asUnordered(false);

            assertThat(outcome, is(instanceOf(Node.class)));

            Node node = (Node) outcome;

            assertThat(node.first().isPresent(), is(equalTo(false)));
            assertThat(node.last().isPresent(), is(equalTo(false)));
            assertThat(node.get(MODS_NAME), is(iterableWithSize(2)));
            assertThat(node.get(METS_USE), is(iterableWithSize(1)));
        }
    }

    /**
     * Tests {@code containsKey(IdentifiableNode)}.
     */
    @Test
    public void testContainsKeyIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNode(MODS_MODS);
            NodeReference kitodo = storage.createNodeReference("https://www.kitodo.org/");

            Node node = storage.createNode().add(javac).add(kitodo).put(METS_TYPE, mods);

            assertThat(node.containsKey(METS_TYPE), is(equalTo(true)));
        }
    }

    /**
     * Tests {@code containsKey(String)}.
     */
    @Test
    public void testContainsKeyString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNode(MODS_MODS);
            NodeReference kitodo = storage.createNodeReference("https://www.kitodo.org/");

            Node node = storage.createNode().add(javac).add(kitodo).put(METS_TYPE, mods);

            assertThat(node.containsKey(METS_TYPE.getIdentifier()), is(equalTo(true)));
        }
    }

    /**
     * Tests {@code contains(Object)}.
     */
    @Test
    public void testContainsObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNode(MODS_MODS);
            NodeReference kitodo = storage.createNodeReference("https://www.kitodo.org/");

            Node node = storage.createNode().add(javac).add(kitodo).put(METS_TYPE, mods);

            assertThat(node.contains(mods), is(equalTo(true)));
            assertThat(node.contains(javac), is(equalTo(true)));
            assertThat(node.contains(kitodo), is(equalTo(true)));
        }
    }

    /**
     * Tests {@code equals(Object)} for two different nodes.
     */
    @Test
    public void testEqualsForTwoDifferentNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(METS_METS);
            Node other = storage.createNode(MODS_DISPLAY_FORM);

            assertThat(one, is(not(equalTo(other))));
        }
    }

    /**
     * Tests {@code equals(Object)} for two equal nodes.
     */
    @Test
    public void testEqualsForTwoEqualNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(METS_METS);
            Node another = storage.createNode(METS_METS);

            assertThat(one, is(equalTo(another)));
        }
    }

    /**
     * Tests {@code find(Node)}.
     */
    @Test
    public void testFind() {
        new GraphPathTest().testApplyingAGraphPathToANode();
    }

    /**
     * Tests {@code first()}.
     */
    @Test
    public void testFirst() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNode(MODS_MODS);
            NodeReference kitodo = storage.createNodeReference("https://www.kitodo.org/");

            Node node = storage.createNode().add(javac).add(kitodo).put(METS_TYPE, mods);
            node.removeAll(RDF.toURL(Node.FIRST_INDEX));

            assertThat(node.first().orElse(null), is(equalTo((long) 2)));
        }
    }

    /**
     * Tests {@code getByIdentifier(String)}.
     */
    @Test
    public void testGetByIdentifierString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNamedNode("http://localhost/test/nodeID", MODS_MODS);
            NodeReference kitodo = storage.createNodeReference("https://www.kitodo.org/");

            Node node = storage.createNode().add(javac).add(kitodo).put(METS_TYPE, mods);

            assertThat(node.getByIdentifier("http://localhost/test/nodeID"), is(equalTo(mods)));
            assertThat(node.getByIdentifier("https://www.kitodo.org/"), is(equalTo(kitodo)));
        }
    }

    /**
     * Tests {@code getByType(IdentifiableNode)}.
     */
    @Test
    public void testGetByTypeIdentifiableNode() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNamedNode("http://localhost/test/nodeID", MODS_MODS);
            NodeReference kitodo = storage.createNodeReference("https://www.kitodo.org/");

            Node node = storage.createNode().add(javac).add(kitodo).put(METS_TYPE, mods);

            assertThat(node.getByType(MODS_MODS).node(), is(equalTo(mods)));
        }
    }

    /**
     * Tests {@code getByType(IdentifiableNode, IdentifiableNode, Literal)}.
     */
    @Test
    public void testGetByTypeIdentifiableNodeIdentifiableNodeLiteral() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node physical = storage.createNode(METS_DIV).put(METS_TYPE, "PHYSICAL");
            Node logical = storage.createNode(METS_DIV).put(METS_TYPE, "LOGICAL");

            Node node = storage.createNode().add(logical).add(physical);

            assertThat(node.getByType(METS_DIV, METS_TYPE, storage.createLiteral("LOGICAL", RDF.PLAIN_LITERAL)).node(),
                is(equalTo(logical)));
            assertThat(node.getByType(METS_DIV, METS_TYPE, storage.createLiteral("PHYSICAL", RDF.PLAIN_LITERAL)).node(),
                is(equalTo(physical)));
        }
    }

    /**
     * Tests {@code getByType(IdentifiableNode, IdentifiableNode, String)}.
     */
    @Test
    public void testGetByTypeIdentifiableNodeIdentifiableNodeString() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node physical = storage.createNode(METS_DIV).put(METS_TYPE, "PHYSICAL");
            Node logical = storage.createNode(METS_DIV).put(METS_TYPE, "LOGICAL");

            Node node = storage.createNode().add(logical).add(physical);

            assertThat(node.getByType(METS_DIV, METS_TYPE, "LOGICAL").node(), is(equalTo(logical)));
            assertThat(node.getByType(METS_DIV, METS_TYPE, "PHYSICAL").node(), is(equalTo(physical)));
        }
    }

    /**
     * Tests {@code getByType(String)}.
     */
    @Test
    public void testGetByTypeString() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNamedNode("http://localhost/test/nodeID", MODS_MODS);
            NodeReference kitodo = storage.createNodeReference("https://www.kitodo.org/");

            Node node = storage.createNode().add(javac).add(kitodo).put(METS_TYPE, mods);

            assertThat(node.getByType(MODS_MODS.getIdentifier()).node(), is(equalTo(mods)));
        }
    }

    /**
     * Tests {@code getByType(String, String, String)}.
     */
    @Test
    public void testGetByTypeStringStringString() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node physical = storage.createNode(METS_DIV).put(METS_TYPE, "PHYSICAL");
            Node logical = storage.createNode(METS_DIV).put(METS_TYPE, "LOGICAL");

            Node node = storage.createNode().add(logical).add(physical);

            assertThat(node.getByType(METS_DIV.getIdentifier(), METS_TYPE.getIdentifier(), "LOGICAL").node(),
                is(equalTo(logical)));
            assertThat(node.getByType(METS_DIV.getIdentifier(), METS_TYPE.getIdentifier(), "PHYSICAL").node(),
                is(equalTo(physical)));
        }
    }

    /**
     * Tests {@code getEnumerated()}.
     */
    @Test
    public void testGetEnumerated() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {

            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNamedNode("http://localhost/test/nodeID", MODS_MODS);
            NodeReference kitodo = storage.createNodeReference("https://www.kitodo.org/");

            Node node = storage.createNode().add(javac).add(kitodo).put(METS_TYPE, mods);

            List<Result> enumerated = node.getEnumerated();
            assertThat(enumerated, hasSize(2));
            for (Result enumeratedResult : enumerated) {
                assertThat(enumeratedResult.value(), is(anyOf(equalTo(javac), equalTo(kitodo))));

            }
        }
    }

    /**
     * Tests {@code getFirst()}.
     */
    @Test
    public void testGetFirst() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            Node mods = storage.createNode(MODS_MODS);

            Node node = storage.createNode();
            node.add(mods);
            node.add(javac);

            assertThat(node.getFirst(), contains(mods));
        }
    }

    /**
     * Tests {@code get(IdentifiableNode, IdentifiableNode)}.
     */
    @Test
    public void testGetIdentifiableNodeIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mods = storage.createNode(MODS_MODS);
            Node node = storage.createNode().put(METS_DIV, mods);

            assertThat(node.get(METS_DIV, MODS_MODS), contains(mods));
        }
    }

    /**
     * Tests {@code get(IdentifiableNode, IdentifiableNode, String)}.
     */
    @Test
    public void testGetIdentifiableNodeIdentifiableNodeString() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node physical = storage.createNode(METS_DIV).put(METS_TYPE, "PHYSICAL");
            Node logical = storage.createNode(METS_DIV).put(METS_TYPE, "LOGICAL");

            Node node = storage.createNode().add(logical).add(physical);

            assertThat(node.getByType(METS_DIV, METS_TYPE, "LOGICAL").node(), is(equalTo(logical)));
            assertThat(node.getByType(METS_DIV, METS_TYPE, "PHYSICAL").node(), is(equalTo(physical)));
        }
    }

    /**
     * Tests {@code get(IdentifiableNode, String)}.
     */
    @Test
    public void testGetIdentifiableNodeString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mods = storage.createNode(MODS_MODS);
            Node node = storage.createNode().put(METS_DIV, mods);

            assertThat(node.get(METS_DIV, MODS_MODS.getIdentifier()), contains(mods));
        }
    }

    /**
     * Tests {@code getLast()}.
     */
    @Test
    public void testGetLast() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            final Literal javac = storage.createLiteral("javac.exe", RDF.PLAIN_LITERAL);
            final Node mods = storage.createNode(MODS_MODS);

            Node node = storage.createNode();
            node.add(mods);
            node.add(javac);

            assertThat(node.getLast(), contains(javac));
        }
    }

    /**
     * Tests {@code get(long)}.
     */
    @Test
    public void testGetLong() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            node.add(storage.createNode("http://names.example/alice"));
            node.add(storage.createNode("http://names.example/bob"));
            node.add(storage.createNode("http://names.example/charlie"));

            assertThat(node.get(2), contains(storage.createNode("http://names.example/bob")));
        }
    }

    /**
     * Tests {@code get(IdentifiableNode)}.
     */
    @Test
    public void testGetNodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node classification = storage.createNode(MODS_CLASSIFICATION)
                    .put(MODS_AUTHORITY, storage.createLiteral("GDZ", RDF.PLAIN_LITERAL))
                    .add(storage.createLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL));

            assertEquals(createResult(storage, storage.createLiteral("GDZ", RDF.PLAIN_LITERAL)),
                classification.get(MODS_AUTHORITY));

            NodeReference refToFirstIndex = storage.createNodeReference(RDF.toURL(Node.FIRST_INDEX));
            assertEquals(createResult(storage, storage.createLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)),
                classification.get(refToFirstIndex));
        }
    }

    /**
     * Tests {@code getRelations()}.
     */
    @Test
    public void testGetRelations() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            node.add(storage.createNode("http://names.example/alice"));
            node.add(storage.createNode("http://names.example/bob"));
            node.add(storage.createNode("http://names.example/charlie"));

            Set<String> expected = new HashSet<>();
            expected.add(RDF.TYPE.getIdentifier());
            expected.add(RDF.toURL(1));
            expected.add(RDF.toURL(2));
            expected.add(RDF.toURL(3));

            assertThat(node.getRelations(), is(equalTo(expected)));
        }
    }

    /**
     * Tests {@code get(Collection<String>, Collection<ObjectType>)} with both
     * relation and objects condition given.
     */
    @Test
    public void testGetSetOfStringSetOfObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.createNode(METS_METS)
                    .add(storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                            .add(storage.createNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph")))
                    .add(storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "PHYSICAL")
                            .add(storage.createNode(METS_DIV).put(METS_TYPE, "physSequence")));

            Set<String> relation = new HashSet<>();
            relation.add(RDF.toURL(Node.FIRST_INDEX));
            Set<ObjectType> condition = new HashSet<>();
            condition.add(storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL"));

            assertEquals(
                createResult(storage,
                    storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                            .add(storage.createNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph"))),

                mets.get(relation, condition));
        }
    }

    /**
     * Tests {@code get(Collection<String>, Collection<ObjectType>)} with
     * unspecified relation, but objects condition given.
     */
    @Test
    public void testGetSetOfStringSetOfObjectTypeWithAnyRelation() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.createNode(METS_METS)
                    .add(storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                            .add(storage.createNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph")))
                    .add(storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "PHYSICAL")
                            .add(storage.createNode(METS_DIV).put(METS_TYPE, "physSequence")));

            Set<ObjectType> condition = new HashSet<>();
            condition.add(storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL"));

            assertEquals(
                createResult(storage,
                    storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                            .add(storage.createNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph"))),

                mets.get(ANY_RELATION, condition));
        }
    }

    /**
     * Tests {@code get(Collection<String>, Collection<ObjectType>)} with
     * relation given, but no objects condition.
     */
    @Test
    public void testGetSetOfStringSetOfObjectTypeWithEmptyConditions() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.createNode(METS_METS)
                    .add(storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                            .add(storage.createNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph")))
                    .add(storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "PHYSICAL")
                            .add(storage.createNode(METS_DIV).put(METS_TYPE, "physSequence")));

            Set<String> relation = new HashSet<>();
            relation.add(RDF.toURL(Node.FIRST_INDEX));

            assertEquals(
                createResult(storage,
                    storage.createNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                            .add(storage.createNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph"))),

                mets.get(relation, ANY_NON_EMPTY_RESULT));
        }
    }

    /**
     * Tests {@code get(String)}.
     */
    @Test
    public void testGetString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode();
            node.put(RDF.TYPE, "http://names.example/petAnimal");
            node.put(RDF.TYPE, "http://names.example/mammal");

            String relation = RDF.TYPE.getIdentifier();

            assertThat(node.get(relation),
                containsInAnyOrder(storage.createLeaf("http://names.example/petAnimal", null),
                    storage.createLeaf("http://names.example/mammal", null)));
        }
    }

    /**
     * Tests {@code get(String, IdentifiableNode)}.
     */
    @Test
    public void testGetStringIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mods = storage.createNode(MODS_MODS);
            Node node = storage.createNode().put(METS_DIV, mods);

            assertThat(node.get(METS_DIV.getIdentifier(), MODS_MODS), contains(mods));
        }
    }

    /**
     * Tests {@code get(String, IdentifiableNode, ObjectType)}.
     */
    @Test
    public void testGetStringIdentifiableNodeObjectType() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node physical = storage.createNode().put(METS_TYPE, "PHYSICAL");
            Node logical = storage.createNode().put(METS_TYPE, "LOGICAL");

            Node node = storage.createNode().put(METS_DIV, logical).put(METS_DIV, physical);

            assertThat(node.get(METS_DIV.getIdentifier(), METS_TYPE, storage.createLiteral("LOGICAL", "")).node(),
                is(equalTo(logical)));
            assertThat(node.get(METS_DIV.getIdentifier(), METS_TYPE, storage.createLiteral("PHYSICAL", "")).node(),
                is(equalTo(physical)));
        }
    }

    /**
     * Tests {@code get(String, String)}.
     */
    @Test
    public void testGetStringString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mods = storage.createNode(MODS_MODS);
            Node node = storage.createNode().put(METS_DIV, mods);

            assertThat(node.get(METS_DIV.getIdentifier(), MODS_MODS.getIdentifier()), contains(mods));
        }
    }

    /**
     * Tests {@code getType()} on a node without a type.
     */
    @Test
    public void testGetTypeOnEmptyNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createNode().getType();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code getType()} on a node with exactly one type.
     */
    @Test
    public void testGetTypeOnNodeWithOneType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertThat(storage.createNode(MODS_NAME).getType(), is(equalTo(MODS_NAME.getIdentifier())));
        }
    }

    /**
     * Tests {@code getType()} on a node with several types.
     */
    @Test
    public void testGetTypeOnNodeWithTwoTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode();
            node.put(RDF.TYPE, "http://names.example/petAnimal");
            node.put(RDF.TYPE, "http://names.example/mammal");

            try {
                node.getType();
                fail(storage.getClass().getSimpleName() + " should throw BufferOverflowException, but does not.");
            } catch (BufferOverflowException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code hashCode()} for two non-equal nodes.
     */
    @Test
    public void testHashCodeIsDifferentForTwoDifferentNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(METS_METS);
            Node other = storage.createNode(MODS_MODS);

            assertThat(one.hashCode() == other.hashCode(), is(false));
        }
    }

    /**
     * Tests {@code hashCode()} for two equal nodes.
     */
    @Test
    public void testHashCodeIsEqualForTwoEqualNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(METS_METS);
            Node other = storage.createNode(METS_METS);

            assertThat(one.hashCode() == other.hashCode(), is(true));
        }
    }

    /**
     * Tests {@code hasType(IdentifiableNode)}.
     */
    @Test
    public void testHasTypeIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_MODS).put(RDF.TYPE, METS_MD_WRAP);

            assertThat(node.hasType(MODS_MODS), is(true));
            assertThat(node.hasType(METS_MD_WRAP), is(true));
        }
    }

    /**
     * Tests {@code hasType(String)}.
     */
    @Test
    public void testHasTypeString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_MODS).put(RDF.TYPE, METS_MD_WRAP);

            assertThat(node.hasType(MODS_MODS.getIdentifier()), is(true));
            assertThat(node.hasType(METS_MD_WRAP.getIdentifier()), is(true));
        }
    }

    /**
     * Tests {@code isEmpty()} for an empty node.
     */
    @Test
    public void testIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertThat(storage.createNode().isEmpty(), is(true));
        }
    }

    /**
     * Tests {@code isEmpty()} for a non-empty node.
     */
    @Test
    public void testIsNotEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertThat(storage.createNode(METS_METS).isEmpty(), is(false));
        }
    }

    /**
     * Tests {@code iterator()}.
     */
    @Test
    public void testIterator() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            node.add(storage.createNode("http://names.example/alice"));
            node.add(storage.createNamedNode("http://names.example/bob"));
            node.add(storage.createNodeReference("http://names.example/charlie"));

            Set<ObjectType> expected = new HashSet<>();
            expected.add(storage.createNode("http://names.example/alice"));
            expected.add(storage.createNamedNode("http://names.example/bob"));
            expected.add(storage.createNodeReference("http://names.example/charlie"));

            Iterator<ObjectType> i = node.iterator();
            while (i.hasNext()) {
                expected.remove(i.next());
            }
            assertThat(expected.isEmpty(), is(true));
        }
    }

    /**
     * Tests {@code last()}.
     */
    @Test
    public void testLast() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            node.add(storage.createNode("http://names.example/alice"));
            node.add(storage.createNode("http://names.example/bob"));
            node.add(storage.createNode("http://names.example/charlie"));

            assertEquals((long) 3, (long) node.last().orElse(null));
        }
    }

    /**
     * Tests {@code matches(ObjectType)} for a matching candidate.
     */
    @Test
    public void testMatches() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            node.add(storage.createNode("http://names.example/alice"));
            node.add(storage.createNode("http://names.example/bob"));
            node.add(storage.createNode("http://names.example/charlie"));

            Node condition = storage.createNode().put(RDF.toURL(2), storage.createNode("http://names.example/bob"));

            assertThat(node.matches(condition), is(true));
        }
    }

    /**
     * Tests {@code matches(ObjectType)} for a non-matching candidate.
     */
    @Test
    public void testMatchesNot() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            node.add(storage.createNode("http://names.example/alice"));
            node.add(storage.createNode("http://names.example/bob"));
            node.add(storage.createNode("http://names.example/charlie"));

            Node condition = storage.createNode(MODS_NAME).put(RDF.toURL(9),
                storage.createNode("http://names.example/alice"));

            assertThat(node.matches(condition), is(false));
        }
    }

    /**
     * Tests {@code putAll(IdentifiableNode, Collection<? extends ObjectType>)}.
     */
    @Test
    public void testPutAllIdentifiableNodeSet() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            List<ObjectType> nodes = new ArrayList<>();
            nodes.add(storage.createNodeReference("http://names.example/alice"));
            nodes.add(storage.createNodeReference("http://names.example/bob"));
            nodes.add(storage.createNodeReference("http://names.example/charlie"));

            Node node = storage.createNode();
            node.putAll(MODS_NAME, nodes);

            assertThat(node.get(MODS_NAME).countUntil(Long.MAX_VALUE), is((long) 3));
        }
    }

    /**
     * Tests {@code putAll(String, Collection<? extends ObjectType>)}.
     */
    @Test
    public void testPutAllStringCollection() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            List<ObjectType> nodes = new ArrayList<>();
            nodes.add(storage.createNodeReference("http://names.example/alice"));
            nodes.add(storage.createNodeReference("http://names.example/bob"));
            nodes.add(storage.createNodeReference("http://names.example/charlie"));

            Node node = storage.createNode();
            node.putAll(MODS_NAME.getIdentifier(), nodes);

            assertThat(node.get(MODS_NAME).countUntil(Long.MAX_VALUE), is((long) 3));
        }
    }

    /**
     * Tests {@code put(IdentifiableNode, ObjectType)}.
     */
    @Test
    public void testPutNodeReferenceObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put(MODS_NAME, storage.createLeaf("Wilhelm Busch", "de"));
        }
    }

    /**
     * Tests {@code put(IdentifiableNode, String)}.
     */
    @Test
    public void testPutNodeReferenceString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put(MODS_NAME, "Wilhelm Busch");
        }
    }

    /**
     * Tests {@code put(String, ObjectType)}.
     */
    @Test
    public void testPutStringObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put("http://www.loc.gov/mods/v3#name", storage.createLeaf("Wilhelm Busch", "de"));
        }
    }

    /**
     * Tests {@code put(String, String)}.
     */
    @Test
    public void testPutStringString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put("http://www.loc.gov/mods/v3#name", "Wilhelm Busch");
        }
    }

    /**
     * Tests {@code removeAll(IdentifiableNode)}.
     */
    @Test
    public void testRemoveAllIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            node.add(storage.createNode("http://names.example/alice"));
            Node bob = storage.createNode("http://names.example/bob");
            node.add(bob);
            node.add(storage.createNode("http://names.example/charlie"));

            assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(3));
            IdentifiableNode two = storage.createNodeReference(RDF.toURL(2));
            assertThat(node.removeAll(two), contains(bob));
            assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(2));
            assertThat(node.removeAll(two), is(nullValue()));
            assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(2));
        }
    }

    /**
     * Tests {@code removeAll(String)}.
     */
    @Test
    public void testRemoveAllString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            node.add(storage.createNode("http://names.example/alice"));
            Node bob = storage.createNode("http://names.example/bob");
            node.add(bob);
            node.add(storage.createNode("http://names.example/charlie"));

            assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(3));
            assertThat(node.removeAll(RDF.toURL(2)), contains(bob));
            assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(2));
            assertThat(node.removeAll(RDF.toURL(2)), is(nullValue()));
            assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(2));
        }
    }

    /**
     * Tests {@code removeFirst()}.
     */
    @Test
    public void testRemoveFirst() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            Node alice = storage.createNode("http://names.example/alice");
            node.add(alice);
            Node bob = storage.createNode("http://names.example/bob");
            node.add(bob);
            Node charlie = storage.createNode("http://names.example/charlie");
            node.add(charlie);

            node.removeFirst();

            assertThat(node.get(1), contains(bob));
            assertThat(node.get(2), contains(charlie));

            node.removeFirst();

            assertThat(node.get(1), contains(charlie));
        }
    }

    /**
     * Tests {@code removeFirstOccurrence(Object)}.
     */
    @Test
    public void testRemoveFirstOccurrenceWithOneElementInRelation() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            Node alice = storage.createNode("http://names.example/alice");
            node.add(alice);
            Node bob = storage.createNode("http://names.example/bob");
            node.add(bob);
            Node charlie = storage.createNode("http://names.example/charlie");
            node.add(charlie);

            node.removeFirstOccurrence(bob);

            assertThat(node.get(1), contains(alice));
            assertThat(node.get(2), contains(charlie));
        }
    }

    /**
     * Tests {@code removeFirstOccurrence(Object)}.
     */
    @Test
    public void testRemoveFirstOccurrenceWithTwoElementsInRelation() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            Node alice = storage.createNode("http://names.example/alice");
            node.add(alice);
            Node bob = storage.createNode("http://names.example/bob");
            Node james = storage.createNode("http://names.example/james");
            node.add(bob).put(RDF.toURL(2), james);
            Node charlie = storage.createNode("http://names.example/charlie");
            node.add(charlie);

            node.removeFirstOccurrence(bob);

            assertThat(node.get(1), contains(alice));
            assertThat(node.get(2), not(contains(bob)));
            assertThat(node.get(2), contains(james));
            assertThat(node.get(3), contains(charlie));
        }
    }

    /**
     * Tests {@code removeLast()}.
     */
    @Test
    public void testRemoveLast() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            Node alice = storage.createNode("http://names.example/alice");
            node.add(alice);
            Node bob = storage.createNode("http://names.example/bob");
            node.add(bob);
            Node charlie = storage.createNode("http://names.example/charlie");
            node.add(charlie);

            node.removeLast();

            assertThat(node.getEnumerated(), hasSize(2));
            assertThat(node.getLast(), contains(bob));
        }
    }

    /**
     * Tests {@code removeLastOccurrence(Object)}.
     */
    @Test
    public void testRemoveLastOccurrenceObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            Node alice = storage.createNode("http://names.example/alice");
            node.add(alice);
            Node bob = storage.createNode("http://names.example/bob");
            node.add(bob);
            Node charlie = storage.createNode("http://names.example/charlie");
            node.add(charlie);

            assertThat(node.removeLastOccurrence(bob), is(true));
            assertThat(node.last().orElse(null), is((long) 2));
            assertThat(node.get(2), contains(charlie));
            assertThat(node.removeLastOccurrence(bob), is(false));
            assertThat(node.last().orElse(null), is((long) 2));
            assertThat(node.get(2), contains(charlie));
        }
    }

    /**
     * Tests {@code remove(Object)}.
     */
    @Test
    public void testRemoveObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME);
            node.add(storage.createNode("http://names.example/alice"));
            Node bob = storage.createNode("http://names.example/bob");
            node.add(bob);
            node.add(storage.createNode("http://names.example/charlie"));

            assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(3));
            assertThat(node.remove(bob), is(true));
            assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(2));
            assertThat(node.remove(bob), is(false));
            assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(2));
        }
    }

    /**
     * Tests {@code replace(String, Set<ObjectType>)}.
     */
    @Test
    public void testReplaceStringSet() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node alice = storage.createNode("http://names.example/alice");
            Node bob = storage.createNode("http://names.example/bob");
            Node charlie = storage.createNode("http://names.example/charlie");
            Node james = storage.createNode("http://names.example/james");

            Node node = storage.createNode(MODS_NAME).add(alice).add(bob).add(charlie);

            node.replace(RDF.toURL(2), new HashSet<>(Arrays.asList(new ObjectType[] {bob, james })));

            assertThat(node.get(2), containsInAnyOrder(james, bob));
        }
    }

    /**
     * Tests {@code set(long, ObjectType)}.
     */
    @Test
    public void testSetLongObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node alice = storage.createNode("http://names.example/alice");

            Node node = storage.createNode();

            node.set(42, alice);

            assertThat(node.get(42), contains(alice));
        }
    }

    /**
     * Tests {@code size()}.
     */
    @Test
    public void testSize() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(MODS_NAME); // 1× rdf:type
            Node alice = storage.createNode("http://names.example/alice"); // 1×
            // rdf:_1
            node.add(alice);
            Node bob = storage.createNode("http://names.example/bob");
            Node james = storage.createNode("http://names.example/james");
            node.add(bob).put(RDF.toURL(2), james); // 2× rdf:_2
            Node charlie = storage.createNode("http://names.example/charlie");
            node.add(charlie); // 1× rdf:_3

            assertThat(node.size(), is(5));
        }
    }

}
