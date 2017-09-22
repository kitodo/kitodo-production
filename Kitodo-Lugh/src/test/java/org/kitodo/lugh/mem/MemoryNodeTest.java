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

package org.kitodo.lugh.mem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.BufferOverflowException;
import java.util.*;

import org.junit.Test;
import org.kitodo.lugh.*;
import org.kitodo.lugh.vocabulary.*;

public class MemoryNodeTest {

    @Test
    public void testAdd() {
        MemoryNode mets = new MemoryNode(Mets.METS);
        mets.add(new MemoryNode(Mets.METS_HDR));

        assertEquals(1, mets.get(RDF.toURL(MemoryNode.FIRST_INDEX)).size());
    }

    @Test
    public void testEqualsForTwoDifferentMemoryNodes() {
        MemoryNode one = new MemoryNode(Mets.METS);
        MemoryNode other = new MemoryNode(Mods.DISPLAY_FORM);

        assertFalse(one.equals(other));
    }

    @Test
    public void testEqualsForTwoEqualMemoryNodes() {
        MemoryNode one = new MemoryNode(Mets.METS);
        MemoryNode other = new MemoryNode(Mets.METS);

        assertTrue(one.equals(other));
    }

    @Test
    public void testFind() {
        new GraphPathTest().testApplyingAGraphPathToANode();
    }

    @Test
    public void testGetMemoryNodeReference() {
        MemoryNode classification = new MemoryNode(Mods.CLASSIFICATION)
                .put(Mods.AUTHORITY, new MemoryLiteral("GDZ", RDF.PLAIN_LITERAL))
                .add(new MemoryLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL));

        assertEquals(new MemoryResult(new MemoryLiteral("GDZ", RDF.PLAIN_LITERAL)), classification.get(Mods.AUTHORITY));

        MemoryNodeReference refToFirstIndex = new MemoryNodeReference(RDF.toURL(MemoryNode.FIRST_INDEX));
        assertEquals(new MemoryResult(new MemoryLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)),
                classification.get(refToFirstIndex));
    }

    @Test
    public void testGetRelations() {
        MemoryNode node = new MemoryNode(Mods.NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        Set<String> expected = new HashSet<>();
        expected.add(RDF.TYPE.getIdentifier());
        expected.add(RDF.toURL(1));
        expected.add(RDF.toURL(2));
        expected.add(RDF.toURL(3));

        assertEquals(expected, node.getRelations());
    }

    @Test
    public void testGetSetOfStringSetOfObjectType() {
        MemoryNode mets = new MemoryNode(Mets.METS)
                .add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                .add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

        Set<String> relation = new HashSet<>();
        relation.add(RDF.toURL(MemoryNode.FIRST_INDEX));
        Set<ObjectType> condition = new HashSet<>();
        condition.add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL"));

        assertEquals(
                new MemoryResult(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                mets.get(relation, condition));
    }

    @Test
    public void testGetSetOfStringSetOfObjectTypeWithAnyRelation() {
        MemoryNode mets = new MemoryNode(Mets.METS)
                .add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                .add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

        final Set<String> ANY_RELATION = Collections.emptySet();
        Set<ObjectType> condition = new HashSet<>();
        condition.add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL"));

        assertEquals(
                new MemoryResult(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                mets.get(ANY_RELATION, condition));
    }

    @Test
    public void testGetSetOfStringSetOfObjectTypeWithEmptyConditions() {
        MemoryNode mets = new MemoryNode(Mets.METS)
                .add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                .add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

        Set<String> relation = new HashSet<>();
        relation.add(RDF.toURL(MemoryNode.FIRST_INDEX));
        final Set<ObjectType> ANY_NON_EMPTY_RESULT = Collections.emptySet();

        assertEquals(
                new MemoryResult(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                mets.get(relation, ANY_NON_EMPTY_RESULT));
    }

    @Test
    public void testGetString() {
        MemoryNode node = new MemoryNode();
        node.put(RDF.TYPE, "http://names.example/petAnimal");
        node.put(RDF.TYPE, "http://names.example/mammal");

        Result expected = new MemoryResult();
        expected.add(MemoryLiteral.create("http://names.example/petAnimal", null));
        expected.add(MemoryLiteral.create("http://names.example/mammal", null));

        String relation = RDF.TYPE.getIdentifier();

        assertEquals(expected, node.get(relation));
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetType0() {
        new MemoryNode().getType();
    }

    @Test
    public void testGetType1() {
        assertEquals(Mods.NAME.getIdentifier(), new MemoryNode(Mods.NAME).getType());
    }

    @Test(expected = BufferOverflowException.class)
    public void testGetType2() {
        MemoryNode node = new MemoryNode();
        node.put(RDF.TYPE, "http://names.example/petAnimal");
        node.put(RDF.TYPE, "http://names.example/mammal");

        node.getType();
    }

    @Test
    public void testHashCodeIsDifferentForTwoDifferentMemoryNodes() {
        MemoryNode one = new MemoryNode(Mets.METS);
        MemoryNode other = new MemoryNode(Mods.MODS);

        assertFalse(one.hashCode() == other.hashCode());
    }

    @Test
    public void testHashCodeIsEqualForTwoEqualMemoryNodes() {
        MemoryNode one = new MemoryNode(Mets.METS);
        MemoryNode other = new MemoryNode(Mets.METS);

        assertTrue(one.hashCode() == other.hashCode());
    }

    @Test
    public void testIsEmpty() {
        assertTrue(new MemoryNode().isEmpty());
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(new MemoryNode(Mets.METS).isEmpty());
    }

    @Test
    public void testIterator() {
        MemoryNode node = new MemoryNode(Mods.NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNamedNode("http://names.example/bob"));
        node.add(new MemoryNodeReference("http://names.example/charlie"));

        Set<ObjectType> expected = new HashSet<>();
        expected.add(new MemoryNode("http://names.example/alice"));
        expected.add(new MemoryNamedNode("http://names.example/bob"));
        expected.add(new MemoryNodeReference("http://names.example/charlie"));

        Iterator<ObjectType> i = node.iterator();
        while (i.hasNext()) {
            expected.remove(i.next());
        }
        assertTrue(expected.isEmpty());
    }

    @Test
    public void testLast() {
        MemoryNode node = new MemoryNode(Mods.NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        assertEquals((long) 3, (long) node.last());
    }

    @Test
    public void testMatches() {
        MemoryNode node = new MemoryNode(Mods.NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        MemoryNode condition = new MemoryNode().put(RDF.toURL(2), new MemoryNode("http://names.example/bob"));

        assertTrue(node.matches(condition));
    }

    @Test
    public void testMatchesNot() {
        MemoryNode node = new MemoryNode(Mods.NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        MemoryNode condition = new MemoryNode(Mods.NAME).put(RDF.toURL(9),
                new MemoryNode("http://names.example/alice"));

        assertFalse(node.matches(condition));
    }

    @Test
    public void testMemoryNodeCanBeCreatedEmpty() {
        new MemoryNode();
    }

    @Test
    public void testMemoryNodeCanBeCreatedWithMemoryNodeReferenceAsType() {
        MemoryNode mets = new MemoryNode(Mets.METS);

        assertEquals(Mets.METS.getIdentifier(), mets.getType());
    }

    @Test
    public void testMemoryNodeCanBeCreatedWithStringAsType() {
        final String MODS = Mods.MODS.getIdentifier();
        MemoryNode mods = new MemoryNode(MODS);

        assertEquals(MODS, mods.getType());
    }

    @Test(expected = AssertionError.class)
    public void testMemoryNodeCreatedWithEmptyStringAsTypeIsEmpty() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            new MemoryNode("");
        }
    }

    @Test
    public void testMemoryNodeCreatedWithUnitializedMemoryNodeReferenceIsEmpty() {
        MemoryNode a = new MemoryNode((MemoryNodeReference) null);
        assertTrue(a.isEmpty());
    }

    @Test
    public void testMemoryNodeCreatedWithUnitializedStringIsEmpty() {
        MemoryNode a = new MemoryNode((String) null);
        assertTrue(a.isEmpty());
    }

    @Test
    public void testPutMemoryNodeReferenceObjectType() {
        new MemoryNode().put(Mods.NAME, MemoryLiteral.create("Wilhelm Busch", "de"));
    }

    @Test
    public void testPutMemoryNodeReferenceString() {
        new MemoryNode().put(Mods.NAME, "Wilhelm Busch");
    }

    @Test
    public void testPutStringObjectType() {
        new MemoryNode().put("http://www.loc.gov/mods/v3#name", MemoryLiteral.create("Wilhelm Busch", "de"));
    }

    @Test
    public void testPutStringString() {
        new MemoryNode().put("http://www.loc.gov/mods/v3#name", "Wilhelm Busch");
    }

    @Test
    public void testReplaceAllNamedMemoryNodesWithNoDataByMemoryNodeReferences() {
        MemoryNode a = new MemoryNode().put(RDF.TYPE, new MemoryNamedNode("http://www.loc.gov/mods/v3#name"));
        a.replaceAllNamedNodesWithNoDataByNodeReferences(true);

        assertEquals(new MemoryNode().put(RDF.TYPE, new MemoryNodeReference("http://www.loc.gov/mods/v3#name")), a);
    }

    @Test
    public void testToModel() throws LinkedDataException {
        MemoryNode modsSection = new MemoryNode(Mods.MODS)
                .add(new MemoryNode(Mods.CLASSIFICATION)
                        .put(Mods.AUTHORITY, new MemoryLiteral("GDZ", RDF.PLAIN_LITERAL))
                        .add(new MemoryLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)))
                .add(new MemoryNode(Mods.RECORD_INFO).add(new MemoryNode(Mods.RECORD_IDENTIFIER)
                        .put(Mods.SOURCE, new MemoryLiteral("gbv-ppn", RDF.PLAIN_LITERAL))
                        .add(new MemoryLiteral("PPN313539384", RDF.PLAIN_LITERAL))))
                .add(new MemoryNode(Mods.IDENTIFIER).put(Mods.TYPE, new MemoryLiteral("PPNanalog", RDF.PLAIN_LITERAL))
                        .add(new MemoryLiteral("PPN313539383", RDF.PLAIN_LITERAL)))
                .add(new MemoryNode(Mods.TITLE_INFO).add(new MemoryNode(Mods.TITLE).add(new MemoryLiteral(
                        "Sever. Pinaeus de virginitatis notis, graviditate et partu. Ludov. Bonaciolus de conformatione foetus. Accedeunt alia",
                        RDF.PLAIN_LITERAL))))
                .add(new MemoryNode(Mods.LANGUAGE).add(new MemoryNode(Mods.LANGUAGE_TERM)
                        .put(Mods.AUTHORITY, new MemoryLiteral("iso639-2b", RDF.PLAIN_LITERAL))
                        .put(Mods.TYPE, new MemoryLiteral("code", RDF.PLAIN_LITERAL))
                        .add(new MemoryLiteral("la", RDF.PLAIN_LITERAL))))
                .add(new MemoryNode(Mods.PLACE).add(
                        new MemoryNode(Mods.PLACE_TERM).put(Mods.TYPE, new MemoryLiteral("text", RDF.PLAIN_LITERAL))
                                .add(new MemoryLiteral("Lugduni Batavorum", RDF.PLAIN_LITERAL))))
                .add(new MemoryNode(Mods.DATE_ISSUED).put(Mods.ENCODING, new MemoryLiteral("w3cdtf", RDF.PLAIN_LITERAL))
                        .add(new MemoryLiteral("1641", RDF.PLAIN_LITERAL)))
                .add(new MemoryNode(Mods.PUBLISHER).add(new MemoryLiteral("Heger", RDF.PLAIN_LITERAL))).add(
                        new MemoryNode(Mods.NAME)
                                .put(Mods.TYPE,
                                        new MemoryLiteral("personal", RDF.PLAIN_LITERAL))
                                .add(new MemoryNode(Mods.ROLE)
                                        .add(new MemoryNode(Mods.ROLE_TERM)
                                                .put(Mods.AUTHORITY,
                                                        new MemoryLiteral("marcrelator", RDF.PLAIN_LITERAL))
                                                .put(Mods.TYPE, new MemoryLiteral("code", RDF.PLAIN_LITERAL))
                                                .add(new MemoryLiteral("aut", RDF.PLAIN_LITERAL)))
                                        .add(new MemoryNode(Mods.NAME_PART)
                                                .put(Mods.TYPE, new MemoryLiteral("family", RDF.PLAIN_LITERAL))
                                                .add(new MemoryLiteral("Pineau", RDF.PLAIN_LITERAL)))
                                        .add(new MemoryNode(Mods.NAME_PART)
                                                .put(Mods.TYPE, new MemoryLiteral("given", RDF.PLAIN_LITERAL))
                                                .add(new MemoryLiteral("Severin", RDF.PLAIN_LITERAL)))
                                        .add(new MemoryNode(Mods.DISPLAY_FORM)
                                                .add(new MemoryLiteral("Pineau, Severin", RDF.PLAIN_LITERAL)))))
                .add(new MemoryNode(Mods.PHYSICAL_DESCRIPTION)
                        .add(new MemoryNode(Mods.EXTENT).add(new MemoryLiteral("getr. Zählung", RDF.PLAIN_LITERAL))));

        assertEquals(modsSection, MemoryResult.createFrom(modsSection.toModel(), false).node());
    }
}
