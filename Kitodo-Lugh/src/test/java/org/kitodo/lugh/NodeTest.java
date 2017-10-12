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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.nio.BufferOverflowException;
import java.util.*;

import org.apache.jena.rdf.model.*;
import org.junit.Test;

public class NodeTest {

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

    /** Tests {@code add()}. */

    @Test
    public void testAdd() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.createNode(Mets.METS);
            mets.add(storage.createNode(Mets.METS_HDR));

            assertEquals(1, mets.get(RDF.toURL(Node.FIRST_INDEX)).size());
        }
    }

    /** Tests {@code addAll(Collection<? extends ObjectType>)}. */
    @Test
    public void testAddAllCollection() {
        fail("Not yet implemented.");
    }

    /** Tests {@code addFirst(ObjectType)}. */
    @Test
    public void testAddFirst() {
        fail("Not yet implemented.");
    }

    /** Tests {@code asUnordered(boolean)}. */
    @Test
    public void testAsUnordered() {
        fail("Not yet implemented.");
    }

    /** Tests {@code containsKey(IdentifiableNode)}. */
    @Test
    public void testContainsKeyIdentifiableNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code containsKey(String)}. */
    @Test
    public void testContainsKeyString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code contains(Object)}. */
    @Test
    public void testContainsObject() {
        fail("Not yet implemented.");
    }

    /** Tests {@code equals(Object)} for two different nodes. */
    @Test
    public void testEqualsForTwoDifferentNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(Mets.METS);
            Node other = storage.createNode(Mods.DISPLAY_FORM);

            assertFalse(one.equals(other));
        }
    }

    /** Tests {@code equals(Object)} for two equal nodes. */
    @Test
    public void testEqualsForTwoEqualNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(Mets.METS);
            Node other = storage.createNode(Mets.METS);

            assertTrue(one.equals(other));
        }
    }

    /** Tests {@code find(Node)}. */
    @Test
    public void testFind() {
        new GraphPathTest().testApplyingAGraphPathToANode();
    }

    /** Tests {@code first()}. */
    @Test
    public void testFirst() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getByIdentifier(String)}. */
    @Test
    public void testGetByIdentifierString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getByType(IdentifiableNode)}. */
    @Test
    public void testGetByTypeIdentifiableNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getByType(IdentifiableNode, IdentifiableNode, Literal)}. */
    @Test
    public void testGetByTypeIdentifiableNodeIdentifiableNodeLiteral() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getByType(IdentifiableNode, IdentifiableNode, String)}. */
    @Test
    public void testGetByTypeIdentifiableNodeIdentifiableNodeString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getByType(String)}. */
    @Test
    public void testGetByTypeString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getByType(String, String, String)}. */
    @Test
    public void testGetByTypeStringStringString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getEnumerated()}. */
    @Test
    public void testGetEnumerated() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getFirst()}. */
    @Test
    public void testGetFirst() {
        fail("Not yet implemented.");
    }

    /** Tests {@code get(IdentifiableNode, IdentifiableNode)}. */
    @Test
    public void testGetIdentifiableNodeIdentifiableNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code get(IdentifiableNode, IdentifiableNode, String)}. */
    @Test
    public void testGetIdentifiableNodeIdentifiableNodeString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code get(IdentifiableNode, String)}. */
    @Test
    public void testGetIdentifiableNodeString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getLast()}. */
    @Test
    public void testGetLast() {
        fail("Not yet implemented.");
    }

    /** Tests {@code get(long)}. */
    @Test
    public void testGetLong() {
        fail("Not yet implemented.");
    }

    /** Tests {@code get(IdentifiableNode)}. */
    @Test
    public void testGetNodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node classification = storage.createNode(Mods.CLASSIFICATION)
                    .put(Mods.AUTHORITY, storage.createLiteral("GDZ", RDF.PLAIN_LITERAL))
                    .add(storage.createLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL));

            assertEquals(createResult(storage, storage.createLiteral("GDZ", RDF.PLAIN_LITERAL)),
                    classification.get(Mods.AUTHORITY));

            NodeReference refToFirstIndex = storage.createNodeReference(RDF.toURL(Node.FIRST_INDEX));
            assertEquals(createResult(storage, storage.createLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)),
                    classification.get(refToFirstIndex));
        }
    }

    /** Tests {@code getRelations()}. */
    @Test
    public void testGetRelations() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(Mods.NAME);
            node.add(storage.createNode("http://names.example/alice"));
            node.add(storage.createNode("http://names.example/bob"));
            node.add(storage.createNode("http://names.example/charlie"));

            Set<String> expected = new HashSet<>();
            expected.add(RDF.TYPE.getIdentifier());
            expected.add(RDF.toURL(1));
            expected.add(RDF.toURL(2));
            expected.add(RDF.toURL(3));

            assertEquals(expected, node.getRelations());
        }
    }

    /**
     * Tests {@code get(Collection<String>, Collection<ObjectType>)} with both
     * relation and objects condition given.
     */
    @Test
    public void testGetSetOfStringSetOfObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.createNode(Mets.METS)
                    .add(storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.createNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                    .add(storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                            .add(storage.createNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

            Set<String> relation = new HashSet<>();
            relation.add(RDF.toURL(Node.FIRST_INDEX));
            Set<ObjectType> condition = new HashSet<>();
            condition.add(storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL"));

            assertEquals(
                    createResult(storage,
                            (Node) storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                                    .add(storage.createNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE,
                                            "Monograph"))),

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
            Node mets = storage.createNode(Mets.METS)
                    .add(storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.createNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                    .add(storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                            .add(storage.createNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

            final Set<String> ANY_RELATION = Collections.emptySet();
            Set<ObjectType> condition = new HashSet<>();
            condition.add(storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL"));

            assertEquals(
                    createResult(storage,
                            (Node) storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                                    .add(storage.createNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE,
                                            "Monograph"))),

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
            Node mets = storage.createNode(Mets.METS)
                    .add(storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.createNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                    .add(storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                            .add(storage.createNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

            Set<String> relation = new HashSet<>();
            relation.add(RDF.toURL(Node.FIRST_INDEX));
            final Set<ObjectType> ANY_NON_EMPTY_RESULT = Collections.emptySet();

            assertEquals(
                    createResult(storage,
                            (Node) storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                                    .add(storage.createNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE,
                                            "Monograph"))),

                    mets.get(relation, ANY_NON_EMPTY_RESULT));
        }
    }

    /** Tests {@code get(String)}. */
    @Test
    public void testGetString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode();
            node.put(RDF.TYPE, "http://names.example/petAnimal");
            node.put(RDF.TYPE, "http://names.example/mammal");

            String relation = RDF.TYPE.getIdentifier();

            assertThat(node.get(relation),
                    containsInAnyOrder(storage.createLiteralType("http://names.example/petAnimal", null),
                            storage.createLiteralType("http://names.example/mammal", null)));
        }
    }

    /** Tests {@code get(String, IdentifiableNode)}. */
    @Test
    public void testGetStringIdentifiableNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code get(String, IdentifiableNode, ObjectType)}. */
    @Test
    public void testGetStringIdentifiableNodeObjectType() {
        fail("Not yet implemented.");
    }

    /** Tests {@code get(String, String)}. */
    @Test
    public void testGetStringString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getType()} on a node without a type. */
    @Test
    public void testGetType0() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createNode().getType();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    /** Tests {@code getType()} on a node with exactly one type. */
    @Test
    public void testGetType1() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(Mods.NAME.getIdentifier(), storage.createNode(Mods.NAME).getType());
        }
    }

    /** Tests {@code getType()} on a node with several types. */
    @Test
    public void testGetType2() {
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

    /** Tests {@code hashCode()} for two non-equal nodes. */
    @Test
    public void testHashCodeIsDifferentForTwoDifferentNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(Mets.METS);
            Node other = storage.createNode(Mods.MODS);

            assertFalse(one.hashCode() == other.hashCode());
        }
    }

    /** Tests {@code hashCode()} for two equal nodes. */
    @Test
    public void testHashCodeIsEqualForTwoEqualNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(Mets.METS);
            Node other = storage.createNode(Mets.METS);

            assertTrue(one.hashCode() == other.hashCode());
        }
    }

    /** Tests {@code hasType(IdentifiableNode)}. */
    @Test
    public void testHasTypeIdentifiableNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code hasType(String)}. */
    @Test
    public void testHasTypeString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code isEmpty()} for an empty node. */
    @Test
    public void testIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertTrue(storage.createNode().isEmpty());
        }
    }

    /** Tests {@code isEmpty()} for a non-empty node. */
    @Test
    public void testIsNotEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertFalse(storage.createNode(Mets.METS).isEmpty());
        }
    }

    /** Tests {@code iterator()}. */
    @Test
    public void testIterator() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(Mods.NAME);
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
            assertTrue(expected.isEmpty());
        }
    }

    /** Tests {@code keySet()}. */
    @Test
    public void testKeySet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code last()}. */
    @Test
    public void testLast() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(Mods.NAME);
            node.add(storage.createNode("http://names.example/alice"));
            node.add(storage.createNode("http://names.example/bob"));
            node.add(storage.createNode("http://names.example/charlie"));

            assertEquals((long) 3, (long) node.last().orElse(null));
        }
    }

    /** Tests {@code matches(ObjectType)} for a matching candidate. */
    @Test
    public void testMatches() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(Mods.NAME);
            node.add(storage.createNode("http://names.example/alice"));
            node.add(storage.createNode("http://names.example/bob"));
            node.add(storage.createNode("http://names.example/charlie"));

            Node condition = storage.createNode().put(RDF.toURL(2), storage.createNode("http://names.example/bob"));

            assertTrue(node.matches(condition));
        }
    }

    /** Tests {@code matches(ObjectType)} for a non-matching candidate. */
    @Test
    public void testMatchesNot() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode(Mods.NAME);
            node.add(storage.createNode("http://names.example/alice"));
            node.add(storage.createNode("http://names.example/bob"));
            node.add(storage.createNode("http://names.example/charlie"));

            Node condition = storage.createNode(Mods.NAME).put(RDF.toURL(9),
                    storage.createNode("http://names.example/alice"));

            assertFalse(node.matches(condition));
        }
    }

    /** Tests {@code putAll(IdentifiableNode, Set<? extends ObjectType>)}. */
    @Test
    public void testPutAllIdentifiableNodeSet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code putAll(String, Collection<? extends ObjectType>)}. */
    @Test
    public void testPutAllStringCollection() {
        fail("Not yet implemented.");
    }

    /** Tests {@code put(IdentifiableNode, ObjectType)}. */
    @Test
    public void testPutNodeReferenceObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put(Mods.NAME, storage.createLiteralType("Wilhelm Busch", "de"));
        }
    }

    /** Tests {@code put(IdentifiableNode, String)}. */
    @Test
    public void testPutNodeReferenceString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put(Mods.NAME, "Wilhelm Busch");
        }
    }

    /** Tests {@code put(String, ObjectType)}. */
    @Test
    public void testPutStringObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put("http://www.loc.gov/mods/v3#name",
                    storage.createLiteralType("Wilhelm Busch", "de"));
        }
    }

    /** Tests {@code put(String, String)}. */
    @Test
    public void testPutStringString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put("http://www.loc.gov/mods/v3#name", "Wilhelm Busch");
        }
    }

    /** Tests {@code removeAll(IdentifiableNode)}. */
    @Test
    public void testRemoveAllIdentifiableNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code removeAll(String)}. */
    @Test
    public void testRemoveAllString() {
        fail("Not yet implemented.");
    }

    /** Tests {@code removeFirst()}. */
    @Test
    public void testRemoveFirst() {
        fail("Not yet implemented.");
    }

    /** Tests {@code removeFirstOccurrence(Object)}. */
    @Test
    public void testRemoveFirstOccurrenceObject() {
        fail("Not yet implemented.");
    }

    /** Tests {@code removeLast()}. */
    @Test
    public void testRemoveLast() {
        fail("Not yet implemented.");
    }

    /** Tests {@code removeLastOccurrence(Object)}. */
    @Test
    public void testRemoveLastOccurrenceObject() {
        fail("Not yet implemented.");
    }

    /** Tests {@code remove(Object)}. */
    @Test
    public void testRemoveObject() {
        fail("Not yet implemented.");
    }

    /** Tests {@code replace(String, Set<ObjectType>)}. */
    @Test
    public void testReplaceStringSet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code set(int, ObjectType)}. */
    @Test
    public void testSetIntObjectType() {
        fail("Not yet implemented.");
    }

    /** Tests {@code size()}. */
    @Test
    public void testSize() {
        fail("Not yet implemented.");
    }

    /** Tests {@code ObjectType.toRDFNode(Model, Boolean)}. */
    @Test
    public void testToModel() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node modsSection = storage.createNode(Mods.MODS)
                    .add(storage.createNode(Mods.CLASSIFICATION)
                            .put(Mods.AUTHORITY, storage.createLiteral("GDZ", RDF.PLAIN_LITERAL))
                            .add(storage.createLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(Mods.RECORD_INFO)
                            .add(storage.createNode(Mods.RECORD_IDENTIFIER)
                                    .put(Mods.SOURCE, storage.createLiteral("gbv-ppn", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("PPN313539384", RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(Mods.IDENTIFIER)
                            .put(Mods.TYPE, storage.createLiteral("PPNanalog", RDF.PLAIN_LITERAL))
                            .add(storage.createLiteral("PPN313539383", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(Mods.TITLE_INFO)
                            .add(storage.createNode(Mods.TITLE).add(storage.createLiteral(
                                    "Sever. Pinaeus de virginitatis notis, graviditate et partu. Ludov. Bonaciolus de conformatione foetus. Accedeunt alia",
                                    RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(Mods.LANGUAGE)
                            .add(storage.createNode(Mods.LANGUAGE_TERM)
                                    .put(Mods.AUTHORITY, storage.createLiteral("iso639-2b", RDF.PLAIN_LITERAL))
                                    .put(Mods.TYPE, storage.createLiteral("code", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("la", RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(Mods.PLACE)
                            .add(storage.createNode(Mods.PLACE_TERM)
                                    .put(Mods.TYPE, storage.createLiteral("text", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("Lugduni Batavorum", RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(Mods.DATE_ISSUED)
                            .put(Mods.ENCODING, storage.createLiteral("w3cdtf", RDF.PLAIN_LITERAL))
                            .add(storage.createLiteral("1641", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(Mods.PUBLISHER).add(storage.createLiteral("Heger", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(Mods.NAME)
                            .put(Mods.TYPE, storage.createLiteral("personal", RDF.PLAIN_LITERAL))
                            .add(storage.createNode(Mods.ROLE).add(storage.createNode(Mods.ROLE_TERM)
                                    .put(Mods.AUTHORITY, storage.createLiteral("marcrelator", RDF.PLAIN_LITERAL))
                                    .put(Mods.TYPE, storage.createLiteral("code", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("aut", RDF.PLAIN_LITERAL)))
                                    .add(storage.createNode(Mods.NAME_PART)
                                            .put(Mods.TYPE, storage.createLiteral("family", RDF.PLAIN_LITERAL))
                                            .add(storage.createLiteral("Pineau", RDF.PLAIN_LITERAL)))
                                    .add(storage.createNode(Mods.NAME_PART)
                                            .put(Mods.TYPE, storage.createLiteral("given", RDF.PLAIN_LITERAL))
                                            .add(storage.createLiteral("Severin", RDF.PLAIN_LITERAL)))
                                    .add(storage.createNode(Mods.DISPLAY_FORM)
                                            .add(storage.createLiteral("Pineau, Severin", RDF.PLAIN_LITERAL)))))
                    .add(storage.createNode(Mods.PHYSICAL_DESCRIPTION).add(storage.createNode(Mods.EXTENT)
                            .add(storage.createLiteral("getr. Zählung", RDF.PLAIN_LITERAL))));

            Model m = ModelFactory.createDefaultModel();
            modsSection.toRDFNode(m, true);
            assertThat(new MemoryResult(m, false).node(), is(equalTo(modsSection)));
        }
    }

}
