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
import static org.junit.Assert.fail;

import java.nio.BufferOverflowException;
import java.util.*;

import org.apache.jena.rdf.model.*;
import org.junit.Test;
import org.kitodo.lugh.vocabulary.*;

public class NodeTest {

    @Test
    public void testAdd() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.createNode(Mets.METS);
            mets.add(storage.createNode(Mets.METS_HDR));

            assertEquals(1, mets.get(RDF.toURL(Node.FIRST_INDEX)).size());
        }
    }

    @Test
    public void testEqualsForTwoDifferentNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(Mets.METS);
            Node other = storage.createNode(Mods.DISPLAY_FORM);

            assertFalse(one.equals(other));
        }
    }

    @Test
    public void testEqualsForTwoEqualNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(Mets.METS);
            Node other = storage.createNode(Mets.METS);

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
            Node classification = storage.createNode(Mods.CLASSIFICATION)
                    .put(Mods.AUTHORITY, storage.createLiteral("GDZ", RDF.PLAIN_LITERAL))
                    .add(storage.createLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL));

            assertEquals(storage.createResult(storage.createLiteral("GDZ", RDF.PLAIN_LITERAL)),
                    classification.get(Mods.AUTHORITY));

            NodeReference refToFirstIndex = storage.createNodeReference(RDF.toURL(Node.FIRST_INDEX));
            assertEquals(storage.createResult(storage.createLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)),
                    classification.get(refToFirstIndex));
        }
    }

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
                    storage.createResult((Node) storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.createNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                    mets.get(relation, condition));
        }
    }

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
                    storage.createResult((Node) storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.createNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                    mets.get(ANY_RELATION, condition));
        }
    }

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
                    storage.createResult((Node) storage.createNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                            .add(storage.createNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                    mets.get(relation, ANY_NON_EMPTY_RESULT));
        }
    }

    @Test
    public void testGetString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node node = storage.createNode();
            node.put(RDF.TYPE, "http://names.example/petAnimal");
            node.put(RDF.TYPE, "http://names.example/mammal");

            Result expected = storage.createResult();
            expected.add(storage.createObjectType("http://names.example/petAnimal", null));
            expected.add(storage.createObjectType("http://names.example/mammal", null));

            String relation = RDF.TYPE.getIdentifier();

            assertEquals(expected, node.get(relation));
        }
    }

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

    @Test
    public void testGetType1() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(Mods.NAME.getIdentifier(), storage.createNode(Mods.NAME).getType());
        }
    }

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

    @Test
    public void testHashCodeIsDifferentForTwoDifferentNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(Mets.METS);
            Node other = storage.createNode(Mods.MODS);

            assertFalse(one.hashCode() == other.hashCode());
        }
    }

    @Test
    public void testHashCodeIsEqualForTwoEqualNodes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node one = storage.createNode(Mets.METS);
            Node other = storage.createNode(Mets.METS);

            assertTrue(one.hashCode() == other.hashCode());
        }
    }

    @Test
    public void testIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertTrue(storage.createNode().isEmpty());
        }
    }

    @Test
    public void testIsNotEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertFalse(storage.createNode(Mets.METS).isEmpty());
        }
    }

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

    @Test
    public void testNodeCanBeCreatedEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode();
        }
    }

    @Test
    public void testNodeCanBeCreatedWithNodeReferenceAsType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.createNode(Mets.METS);

            assertEquals(Mets.METS.getIdentifier(), mets.getType());
        }
    }

    @Test
    public void testNodeCanBeCreatedWithStringAsType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            final String MODS = Mods.MODS.getIdentifier();
            Node mods = storage.createNode(MODS);

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
                    storage.createNode("");
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
            Node a = storage.createNode((NodeReference) null);
            assertTrue(a.isEmpty());
        }
    }

    @Test
    public void testNodeCreatedWithUnitializedStringIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node a = storage.createNode((String) null);
            assertTrue(a.isEmpty());
        }
    }

    @Test
    public void testPutNodeReferenceObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put(Mods.NAME, storage.createObjectType("Wilhelm Busch", "de"));
        }
    }

    @Test
    public void testPutNodeReferenceString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put(Mods.NAME, "Wilhelm Busch");
        }
    }

    @Test
    public void testPutStringObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put("http://www.loc.gov/mods/v3#name",
                    storage.createObjectType("Wilhelm Busch", "de"));
        }
    }

    @Test
    public void testPutStringString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNode().put("http://www.loc.gov/mods/v3#name", "Wilhelm Busch");
        }
    }

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
            assertEquals(modsSection, storage.createResult(m, false).node());
        }
    }
}
