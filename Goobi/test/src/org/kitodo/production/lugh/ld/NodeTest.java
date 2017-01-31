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

public class NodeTest {

    @Test
    public void testHashCodeIsEqualForTwoEqualNodes() {
        Node one = new Node(Mets.METS);
        Node other = new Node(Mets.METS);

        assertTrue(one.hashCode() == other.hashCode());
    }

    @Test
    public void testHashCodeIsDifferentForTwoDifferentNodes() {
        Node one = new Node(Mets.METS);
        Node other = new Node(Mods.MODS);

        assertFalse(one.hashCode() == other.hashCode());
    }

    @Test
    public void testNodeCanBeCreatedEmpty() {
        new Node();
    }

    @Test
    public void testNodeCanBeCreatedWithNodeReferenceAsType() {
        Node mets = new Node(Mets.METS);

        assertEquals(Mets.METS.getIdentifier(), mets.getType());
    }

    @Test
    public void testNodeCanBeCreatedWithStringAsType() {
        final String MODS = Mods.MODS.getIdentifier();
        Node mods = new Node(MODS);

        assertEquals(MODS, mods.getType());
    }

    @Test(expected = AssertionError.class)
    public void testNodeCreatedWithEmptyStringAsTypeIsEmpty() {
        try{
            assert(false);
            fail("Assertions disabled. Run JVM with -ea option.");
        }catch(AssertionError e){
            new Node("");
        }
    }

    @Test
    public void testNodeCreatedWithUnitializedNodeReferenceIsEmpty() {
        Node a = new Node((NodeReference) null);
        assertTrue(a.isEmpty());
    }

    @Test
    public void testNodeCreatedWithUnitializedStringIsEmpty() {
        Node a = new Node((String) null);
        assertTrue(a.isEmpty());
    }

    @Test
    public void testAdd() {
        Node mets = new Node(Mets.METS);
        mets.add(new Node(Mets.METS_HDR));

        assertEquals(1, mets.get(RDF.toURL(Node.FIRST_INDEX)).size());
    }

    @Test
    public void testEqualsForTwoEqualNodes() {
        Node one = new Node(Mets.METS);
        Node other = new Node(Mets.METS);

        assertTrue(one.equals(other));
    }

    @Test
    public void testEqualsForTwoDifferentNodes() {
        Node one = new Node(Mets.METS);
        Node other = new Node(Mods.DISPLAY_FORM);

        assertFalse(one.equals(other));
    }

    @Test
    public void testFind() {
        new GraphPathTest().testApplyingAGraphPathToANode();
    }

    @Test
    public void testGetNodeReference() {
        Node classification = new Node(Mods.CLASSIFICATION)
            .put(Mods.AUTHORITY, new Literal("GDZ", RDF.PLAIN_LITERAL))
            .add(new Literal("Zeutschel Digital", RDF.PLAIN_LITERAL));

        assertEquals(
            new Result(new Literal("GDZ", RDF.PLAIN_LITERAL)),
            classification.get(Mods.AUTHORITY)
        );

        NodeReference refToFirstIndex = new NodeReference(RDF.toURL(Node.FIRST_INDEX));
        assertEquals(
            new Result(new Literal("Zeutschel Digital", RDF.PLAIN_LITERAL)),
            classification.get(refToFirstIndex)
        );
    }

    @Test
    public void testGetSetOfStringSetOfObjectType() {
        Node mets = new Node(Mets.METS)
            .add(new Node(Mets.STRUCT_MAP)
                .put(Mets.TYPE, "LOGICAL")
                .add(new Node(Mets.DIV)
                    .put(Mets.ORDERLABEL, " - ")
                    .put(Mets.TYPE, "Monograph")
                )
            )
            .add(new Node(Mets.STRUCT_MAP)
                .put(Mets.TYPE, "PHYSICAL")
                .add(new Node(Mets.DIV)
                    .put(Mets.TYPE, "physSequence")
                )
            );

        Set<String> relation = new HashSet<>();
        relation.add(RDF.toURL(Node.FIRST_INDEX));
        Set<ObjectType> condition = new HashSet<ObjectType>();
        condition.add(new Node(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL"));

        assertEquals(
            new Result(
                new Node(Mets.STRUCT_MAP)
                    .put(Mets.TYPE, "LOGICAL")
                    .add(new Node(Mets.DIV)
                        .put(Mets.ORDERLABEL, " - ")
                        .put(Mets.TYPE, "Monograph")
                    )
            ),

            mets.get(relation, condition)
        );
    }

    @Test
    public void testGetSetOfStringSetOfObjectTypeWithEmptyConditions() {
        Node mets = new Node(Mets.METS)
            .add(new Node(Mets.STRUCT_MAP)
                .put(Mets.TYPE, "LOGICAL")
                .add(new Node(Mets.DIV)
                    .put(Mets.ORDERLABEL, " - ")
                    .put(Mets.TYPE, "Monograph")
                )
            )
            .add(new Node(Mets.STRUCT_MAP)
                .put(Mets.TYPE, "PHYSICAL")
                .add(new Node(Mets.DIV)
                    .put(Mets.TYPE, "physSequence")
                )
            );

        Set<String> relation = new HashSet<>();
        relation.add(RDF.toURL(Node.FIRST_INDEX));
        final Set<ObjectType> ANY_NON_EMPTY_RESULT = Collections.emptySet();

        assertEquals(
            new Result(
                new Node(Mets.STRUCT_MAP)
                    .put(Mets.TYPE, "LOGICAL")
                    .add(new Node(Mets.DIV)
                        .put(Mets.ORDERLABEL, " - ")
                        .put(Mets.TYPE, "Monograph")
                    )
            ),

            mets.get(relation, ANY_NON_EMPTY_RESULT)
        );
    }

    @Test
    public void testGetSetOfStringSetOfObjectTypeWithAnyRelation() {
        Node mets = new Node(Mets.METS)
            .add(new Node(Mets.STRUCT_MAP)
                .put(Mets.TYPE, "LOGICAL")
                .add(new Node(Mets.DIV)
                    .put(Mets.ORDERLABEL, " - ")
                    .put(Mets.TYPE, "Monograph")
                )
            )
            .add(new Node(Mets.STRUCT_MAP)
                .put(Mets.TYPE, "PHYSICAL")
                .add(new Node(Mets.DIV)
                    .put(Mets.TYPE, "physSequence")
                )
            );

        final Set<String> ANY_RELATION = Collections.emptySet();
        Set<ObjectType> condition = new HashSet<ObjectType>();
        condition.add(new Node(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL"));

        assertEquals(
            new Result(
                new Node(Mets.STRUCT_MAP)
                    .put(Mets.TYPE, "LOGICAL")
                    .add(new Node(Mets.DIV)
                        .put(Mets.ORDERLABEL, " - ")
                        .put(Mets.TYPE, "Monograph")
                    )
            ),

            mets.get(ANY_RELATION, condition)
        );
    }

    @Test
    public void testGetString() {
        Node node = new Node();
        node.put(RDF.TYPE, "http://names.example/petAnimal");
        node.put(RDF.TYPE, "http://names.example/mammal");

        Result expected = new Result();
        expected.add(Literal.create("http://names.example/petAnimal", null));
        expected.add(Literal.create("http://names.example/mammal", null));

        String relation = RDF.TYPE.getIdentifier();

        assertEquals(expected, node.get(relation));
    }

    @Test
    public void testGetRelations() {
        Node node = new Node(Mods.NAME);
        node.add(new Node("http://names.example/alice"));
        node.add(new Node("http://names.example/bob"));
        node.add(new Node("http://names.example/charlie"));

        Set<String> expected = new HashSet<>();
        expected.add(RDF.TYPE.getIdentifier());
        expected.add(RDF.toURL(1));
        expected.add(RDF.toURL(2));
        expected.add(RDF.toURL(3));

        assertEquals(expected, node.getRelations());
    }

    @Test(expected = NoSuchElementException.class)
    public void testGetType0() {
        new Node().getType();
    }

    @Test
    public void testGetType1() {
        assertEquals(Mods.NAME.getIdentifier(), new Node(Mods.NAME).getType());
    }

    @Test(expected = BufferOverflowException.class)
    public void testGetType2() {
        Node node = new Node();
        node.put(RDF.TYPE, "http://names.example/petAnimal");
        node.put(RDF.TYPE, "http://names.example/mammal");

        node.getType();
    }

    @Test
    public void testIsEmpty() {
        assertTrue(new Node().isEmpty());
    }

    @Test
    public void testIsNotEmpty() {
        assertFalse(new Node(Mets.METS).isEmpty());
    }

    @Test
    public void testIterator() {
        Node node = new Node(Mods.NAME);
        node.add(new Node("http://names.example/alice"));
        node.add(new NamedNode("http://names.example/bob"));
        node.add(new NodeReference("http://names.example/charlie"));

        Set<ObjectType> expected = new HashSet<>();
        expected.add(new Node("http://names.example/alice"));
        expected.add(new NamedNode("http://names.example/bob"));
        expected.add(new NodeReference("http://names.example/charlie"));

        Iterator<ObjectType> i = node.iterator();
        while(i.hasNext()) expected.remove(i.next());
        assertTrue(expected.isEmpty());
    }

    @Test
    public void testLast() {
        Node node = new Node(Mods.NAME);
        node.add(new Node("http://names.example/alice"));
        node.add(new Node("http://names.example/bob"));
        node.add(new Node("http://names.example/charlie"));

        assertEquals((long) 3, node.last());
    }

    @Test
    public void testMatches() {
        Node node = new Node(Mods.NAME);
        node.add(new Node("http://names.example/alice"));
        node.add(new Node("http://names.example/bob"));
        node.add(new Node("http://names.example/charlie"));

        Node condition = new Node().put(RDF.toURL(2), new Node("http://names.example/bob"));

        assertTrue(node.matches(condition));
    }

    @Test
    public void testMatchesNot() {
        Node node = new Node(Mods.NAME);
        node.add(new Node("http://names.example/alice"));
        node.add(new Node("http://names.example/bob"));
        node.add(new Node("http://names.example/charlie"));

        Node condition = new Node(Mods.NAME).put(RDF.toURL(9), new Node("http://names.example/alice"));

        assertFalse(node.matches(condition));
    }

    @Test
    public void testPutNodeReferenceObjectType() {
        new Node().put(Mods.NAME, Literal.create("Wilhelm Busch", "de"));
    }

    @Test
    public void testPutNodeReferenceString() {
        new Node().put(Mods.NAME, "Wilhelm Busch");
    }

    @Test
    public void testPutStringObjectType() {
        new Node().put("http://www.loc.gov/mods/v3#name", Literal.create("Wilhelm Busch", "de"));
    }

    @Test
    public void testPutStringString() {
        new Node().put("http://www.loc.gov/mods/v3#name", "Wilhelm Busch");
    }

    @Test
    public void testReplaceAllNamedNodesWithNoDataByNodeReferences() {
        Node a = new Node().put(RDF.TYPE, new NamedNode("http://www.loc.gov/mods/v3#name"));
        a.replaceAllNamedNodesWithNoDataByNodeReferences(true);

        assertEquals(new Node().put(RDF.TYPE, new NodeReference("http://www.loc.gov/mods/v3#name")), a);
    }

    @Test
    public void testToModel() throws LinkedDataException {
        Node modsSection = new Node(Mods.MODS)
            .add(new Node(Mods.CLASSIFICATION)
                .put(Mods.AUTHORITY, new Literal("GDZ", RDF.PLAIN_LITERAL))
                .add(new Literal("Zeutschel Digital", RDF.PLAIN_LITERAL))
            )
            .add(new Node(Mods.RECORD_INFO)
                .add(new Node(Mods.RECORD_IDENTIFIER)
                    .put(Mods.SOURCE, new Literal("gbv-ppn", RDF.PLAIN_LITERAL))
                    .add(new Literal("PPN313539384", RDF.PLAIN_LITERAL))
                )
            )
            .add(new Node(Mods.IDENTIFIER)
                .put(Mods.TYPE, new Literal("PPNanalog", RDF.PLAIN_LITERAL))
                .add(new Literal("PPN313539383", RDF.PLAIN_LITERAL))
            )
            .add(new Node(Mods.TITLE_INFO)
                .add(new Node(Mods.TITLE)
                    .add(new Literal("Sever. Pinaeus de virginitatis notis, graviditate et partu. Ludov. Bonaciolus de conformatione foetus. Accedeunt alia", RDF.PLAIN_LITERAL))
                )
            )
            .add(new Node(Mods.LANGUAGE)
                .add(new Node(Mods.LANGUAGE_TERM)
                    .put(Mods.AUTHORITY, new Literal("iso639-2b", RDF.PLAIN_LITERAL))
                    .put(Mods.TYPE, new Literal("code", RDF.PLAIN_LITERAL))
                    .add(new Literal("la", RDF.PLAIN_LITERAL))
                )
            )
            .add(new Node(Mods.PLACE)
                .add(new Node(Mods.PLACE_TERM)
                    .put(Mods.TYPE, new Literal("text", RDF.PLAIN_LITERAL))
                    .add(new Literal("Lugduni Batavorum", RDF.PLAIN_LITERAL))
                )
            )
            .add(new Node(Mods.DATE_ISSUED)
                .put(Mods.ENCODING, new Literal("w3cdtf", RDF.PLAIN_LITERAL))
                .add(new Literal("1641", RDF.PLAIN_LITERAL))
            )
            .add(new Node(Mods.PUBLISHER)
                .add(new Literal("Heger", RDF.PLAIN_LITERAL))
            )
            .add(new Node(Mods.NAME)
                .put(Mods.TYPE, new Literal("personal", RDF.PLAIN_LITERAL))
                .add(new Node(Mods.ROLE)
                    .add(new Node(Mods.ROLE_TERM)
                        .put(Mods.AUTHORITY, new Literal("marcrelator", RDF.PLAIN_LITERAL))
                        .put(Mods.TYPE, new Literal("code", RDF.PLAIN_LITERAL))
                        .add(new Literal("aut", RDF.PLAIN_LITERAL))
                    )
                    .add(new Node(Mods.NAME_PART)
                        .put(Mods.TYPE, new Literal("family", RDF.PLAIN_LITERAL))
                        .add(new Literal("Pineau", RDF.PLAIN_LITERAL))
                    )
                    .add(new Node(Mods.NAME_PART)
                        .put(Mods.TYPE, new Literal("given", RDF.PLAIN_LITERAL))
                        .add(new Literal("Severin", RDF.PLAIN_LITERAL))
                    )
                    .add(new Node(Mods.DISPLAY_FORM)
                        .add(new Literal("Pineau, Severin", RDF.PLAIN_LITERAL))
                    )
                )
            )
            .add(new Node(Mods.PHYSICAL_DESCRIPTION)
                .add(new Node(Mods.EXTENT)
                    .add(new Literal("getr. Zählung", RDF.PLAIN_LITERAL))
                )
            )
        ;

        assertEquals(modsSection, Result.createFrom(modsSection.toModel(), false).node());
    }
}
