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
import org.kitodo.lugh.mem.GraphPathTest;
import org.kitodo.lugh.vocabulary.*;

public class NodeTest {

    @Test
    public void testAdd() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.newNode(Mets.METS);
            mets.add(storage.newNode(Mets.METS_HDR));

            assertEquals(1, mets.get(RDF.toURL(Node.FIRST_INDEX)).size());
        }
    }

    @Test
    public void testEqualsForTwoDifferentNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.newNode(Mets.METS);
            Node other = storage.newNode(Mods.DISPLAY_FORM);

            assertFalse(one.equals(other));
        }
    }

    @Test
    public void testEqualsForTwoEqualNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.newNode(Mets.METS);
            Node other = storage.newNode(Mets.METS);

            assertTrue(one.equals(other));
        }
    }

    @Test
    public void testFind() {
        new GraphPathTest().testApplyingAGraphPathToANode();
    }

    @Test
    public void testGetNodeReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node classification = storage.newNode(Mods.CLASSIFICATION)
                    .put(Mods.AUTHORITY, storage.newLiteral("GDZ", RDF.PLAIN_LITERAL))
                    .add(storage.newLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL));

            assertEquals(storage.newResult(storage.newLiteral("GDZ", RDF.PLAIN_LITERAL)),
                    classification.get(Mods.AUTHORITY));

            NodeReference refToFirstIndex = storage.newNodeReference(RDF.toURL(Node.FIRST_INDEX));
            assertEquals(storage.newResult(storage.newLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)),
                    classification.get(refToFirstIndex));
        }
    }

    @Test
    public void testGetRelations() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.newNode(Mods.NAME);
            node.add(storage.newNode("http://names.example/alice"));
            node.add(storage.newNode("http://names.example/bob"));
            node.add(storage.newNode("http://names.example/charlie"));

            Set<String> expected = new HashSet<>();
            expected.add(RDF.TYPE.getIdentifier());
            expected.add(RDF.toURL(1));
            expected.add(RDF.toURL(2));
            expected.add(RDF.toURL(3));

            assertEquals(expected, node.getRelations());
        }
    }

    @Test
    public void testGetSetOfStringSetOfObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.newNode(Mets.METS)
                    .add(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.newNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                    .add(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                            .add(storage.newNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

            Set<String> relation = new HashSet<>();
            relation.add(RDF.toURL(Node.FIRST_INDEX));
            Set<ObjectType> condition = new HashSet<>();
            condition.add(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL"));

            assertEquals(
                    storage.newResult(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.newNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                    mets.get(relation, condition));
        }
    }

    @Test
    public void testGetSetOfStringSetOfObjectTypeWithAnyRelation() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.newNode(Mets.METS)
                    .add(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.newNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                    .add(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                            .add(storage.newNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

            final Set<String> ANY_RELATION = Collections.emptySet();
            Set<ObjectType> condition = new HashSet<>();
            condition.add(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL"));

            assertEquals(
                    storage.newResult(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.newNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                    mets.get(ANY_RELATION, condition));
        }
    }

    @Test
    public void testGetSetOfStringSetOfObjectTypeWithEmptyConditions() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.newNode(Mets.METS)
                    .add(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.newNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                    .add(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                            .add(storage.newNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

            Set<String> relation = new HashSet<>();
            relation.add(RDF.toURL(Node.FIRST_INDEX));
            final Set<ObjectType> ANY_NON_EMPTY_RESULT = Collections.emptySet();

            assertEquals(
                    storage.newResult(storage.newNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.newNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                    mets.get(relation, ANY_NON_EMPTY_RESULT));
        }
    }

    @Test
    public void testGetString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.newNode();
            node.put(RDF.TYPE, "http://names.example/petAnimal");
            node.put(RDF.TYPE, "http://names.example/mammal");

            Result expected = storage.newResult();
            expected.add(storage.newObjectType("http://names.example/petAnimal", null));
            expected.add(storage.newObjectType("http://names.example/mammal", null));

            String relation = RDF.TYPE.getIdentifier();

            assertEquals(expected, node.get(relation));
        }
    }

    @Test
    public void testGetType0() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.newNode().getType();
                fail(storage.getClass().getSimpleName() + " should throw NoSuchElementException, but does not.");
            } catch (NoSuchElementException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testGetType1() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(Mods.NAME.getIdentifier(), storage.newNode(Mods.NAME).getType());
        }
    }

    @Test
    public void testGetType2() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.newNode();
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

    @Test
    public void testHashCodeIsDifferentForTwoDifferentNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.newNode(Mets.METS);
            Node other = storage.newNode(Mods.MODS);

            assertFalse(one.hashCode() == other.hashCode());
        }
    }

    @Test
    public void testHashCodeIsEqualForTwoEqualNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.newNode(Mets.METS);
            Node other = storage.newNode(Mets.METS);

            assertTrue(one.hashCode() == other.hashCode());
        }
    }

    @Test
    public void testIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertTrue(storage.newNode().isEmpty());
        }
    }

    @Test
    public void testIsNotEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertFalse(storage.newNode(Mets.METS).isEmpty());
        }
    }

    @Test
    public void testIterator() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.newNode(Mods.NAME);
            node.add(storage.newNode("http://names.example/alice"));
            node.add(storage.newNamedNode("http://names.example/bob"));
            node.add(storage.newNodeReference("http://names.example/charlie"));

            Set<ObjectType> expected = new HashSet<>();
            expected.add(storage.newNode("http://names.example/alice"));
            expected.add(storage.newNamedNode("http://names.example/bob"));
            expected.add(storage.newNodeReference("http://names.example/charlie"));

            Iterator<ObjectType> i = node.iterator();
            while (i.hasNext()) {
                expected.remove(i.next());
            }
            assertTrue(expected.isEmpty());
        }
    }

    @Test
    public void testLast() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.newNode(Mods.NAME);
            node.add(storage.newNode("http://names.example/alice"));
            node.add(storage.newNode("http://names.example/bob"));
            node.add(storage.newNode("http://names.example/charlie"));

            assertEquals((long) 3, (long) node.last());
        }
    }

    @Test
    public void testMatches() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.newNode(Mods.NAME);
            node.add(storage.newNode("http://names.example/alice"));
            node.add(storage.newNode("http://names.example/bob"));
            node.add(storage.newNode("http://names.example/charlie"));

            Node condition = storage.newNode().put(RDF.toURL(2), storage.newNode("http://names.example/bob"));

            assertTrue(node.matches(condition));
        }
    }

    @Test
    public void testMatchesNot() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.newNode(Mods.NAME);
            node.add(storage.newNode("http://names.example/alice"));
            node.add(storage.newNode("http://names.example/bob"));
            node.add(storage.newNode("http://names.example/charlie"));

            Node condition = storage.newNode(Mods.NAME).put(RDF.toURL(9),
                    storage.newNode("http://names.example/alice"));

            assertFalse(node.matches(condition));
        }
    }

    @Test
    public void testNodeCanBeCreatedEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.newNode();
        }
    }

    @Test
    public void testNodeCanBeCreatedWithNodeReferenceAsType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.newNode(Mets.METS);

            assertEquals(Mets.METS.getIdentifier(), mets.getType());
        }
    }

    @Test
    public void testNodeCanBeCreatedWithStringAsType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            final String MODS = Mods.MODS.getIdentifier();
            Node mods = storage.newNode(MODS);

            assertEquals(MODS, mods.getType());
        }
    }

    @Test
    public void testNodeCreatedWithEmptyStringAsTypeIsEmpty() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
                try {
                    storage.newNode("");
                    fail(storage.getClass().getSimpleName() + " should throw AssertionError, but does not.");
                } catch (AssertionError e1) {
                    /* expected */
                }
            }
        }
    }

    @Test
    public void testNodeCreatedWithUnitializedNodeReferenceIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node a = storage.newNode((NodeReference) null);
            assertTrue(a.isEmpty());
        }
    }

    @Test
    public void testNodeCreatedWithUnitializedStringIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node a = storage.newNode((String) null);
            assertTrue(a.isEmpty());
        }
    }

    @Test
    public void testPutNodeReferenceObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.newNode().put(Mods.NAME, storage.newObjectType("Wilhelm Busch", "de"));
        }
    }

    @Test
    public void testPutNodeReferenceString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.newNode().put(Mods.NAME, "Wilhelm Busch");
        }
    }

    @Test
    public void testPutStringObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.newNode().put("http://www.loc.gov/mods/v3#name", storage.newObjectType("Wilhelm Busch", "de"));
        }
    }

    @Test
    public void testPutStringString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.newNode().put("http://www.loc.gov/mods/v3#name", "Wilhelm Busch");
        }
    }

    @Test
    public void testToModel() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node modsSection = storage.newNode(Mods.MODS)
                    .add(storage.newNode(Mods.CLASSIFICATION)
                            .put(Mods.AUTHORITY, storage.newLiteral("GDZ", RDF.PLAIN_LITERAL))
                            .add(storage.newLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)))
                    .add(storage.newNode(Mods.RECORD_INFO)
                            .add(storage.newNode(Mods.RECORD_IDENTIFIER)
                                    .put(Mods.SOURCE, storage.newLiteral("gbv-ppn", RDF.PLAIN_LITERAL))
                                    .add(storage.newLiteral("PPN313539384", RDF.PLAIN_LITERAL))))
                    .add(storage.newNode(Mods.IDENTIFIER)
                            .put(Mods.TYPE, storage.newLiteral("PPNanalog", RDF.PLAIN_LITERAL))
                            .add(storage.newLiteral("PPN313539383", RDF.PLAIN_LITERAL)))
                    .add(storage.newNode(Mods.TITLE_INFO).add(storage.newNode(Mods.TITLE).add(storage.newLiteral(
                            "Sever. Pinaeus de virginitatis notis, graviditate et partu. Ludov. Bonaciolus de conformatione foetus. Accedeunt alia",
                            RDF.PLAIN_LITERAL))))
                    .add(storage.newNode(Mods.LANGUAGE)
                            .add(storage.newNode(Mods.LANGUAGE_TERM)
                                    .put(Mods.AUTHORITY, storage.newLiteral("iso639-2b", RDF.PLAIN_LITERAL))
                                    .put(Mods.TYPE, storage.newLiteral("code", RDF.PLAIN_LITERAL))
                                    .add(storage.newLiteral("la", RDF.PLAIN_LITERAL))))
                    .add(storage.newNode(Mods.PLACE)
                            .add(storage.newNode(Mods.PLACE_TERM)
                                    .put(Mods.TYPE, storage.newLiteral("text", RDF.PLAIN_LITERAL))
                                    .add(storage.newLiteral("Lugduni Batavorum", RDF.PLAIN_LITERAL))))
                    .add(storage.newNode(Mods.DATE_ISSUED)
                            .put(Mods.ENCODING, storage.newLiteral("w3cdtf", RDF.PLAIN_LITERAL))
                            .add(storage.newLiteral("1641", RDF.PLAIN_LITERAL)))
                    .add(storage.newNode(Mods.PUBLISHER).add(storage.newLiteral("Heger", RDF.PLAIN_LITERAL)))
                    .add(storage.newNode(Mods.NAME).put(Mods.TYPE, storage.newLiteral("personal", RDF.PLAIN_LITERAL))
                            .add(storage.newNode(Mods.ROLE)
                                    .add(storage.newNode(Mods.ROLE_TERM)
                                            .put(Mods.AUTHORITY, storage.newLiteral("marcrelator", RDF.PLAIN_LITERAL))
                                            .put(Mods.TYPE, storage.newLiteral("code", RDF.PLAIN_LITERAL))
                                            .add(storage.newLiteral("aut", RDF.PLAIN_LITERAL)))
                                    .add(storage.newNode(Mods.NAME_PART)
                                            .put(Mods.TYPE, storage.newLiteral("family", RDF.PLAIN_LITERAL))
                                            .add(storage.newLiteral("Pineau", RDF.PLAIN_LITERAL)))
                                    .add(storage.newNode(Mods.NAME_PART)
                                            .put(Mods.TYPE, storage.newLiteral("given", RDF.PLAIN_LITERAL))
                                            .add(storage.newLiteral("Severin", RDF.PLAIN_LITERAL)))
                                    .add(storage.newNode(Mods.DISPLAY_FORM)
                                            .add(storage.newLiteral("Pineau, Severin", RDF.PLAIN_LITERAL)))))
                    .add(storage.newNode(Mods.PHYSICAL_DESCRIPTION).add(
                            storage.newNode(Mods.EXTENT).add(storage.newLiteral("getr. Zählung", RDF.PLAIN_LITERAL))));

            assertEquals(modsSection, storage.createResultFrom(modsSection.toModel(), false).node());
        }
    }
}
