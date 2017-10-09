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

import org.junit.Test;
import org.kitodo.lugh.vocabulary.*;

public class MemoryNodeTest {

    /** Tests {@code hasType(String)}. */
    @Test
    public void test() {
        fail("Not yet implemented.");
    }

    /** Tests {@code addAll(Collection<? extends ObjectType>)}. */
    @Test
    public void testAddAll() {
        fail("Not yet implemented.");
    }

    /** Tests {@code addFirst(ObjectType)}. */
    @Test
    public void testAddFirst() {
        fail("Not yet implemented.");
    }

    /** Tests {@code add(int, ObjectType)}. */
    @Test
    public void testAddIntrObjectType() {
        fail("Not yet implemented.");
    }

    /** Tests {@code add(ObjectType)}. */
    @Test
    public void testAddObjectType() {
        MemoryNode mets = new MemoryNode(Mets.METS);
        mets.add(new MemoryNode(Mets.METS_HDR));

        assertEquals(1, mets.get(RDF.toURL(Node.FIRST_INDEX)).size());
    }

    /** Tests {@code asUnordered(boolean)}. */
    @Test
    public void testAsUnordered() {
        fail("Not yet implemented.");
    }

    /** Tests {@code contains(Object)}. */
    @Test
    public void testContains() {
        fail("Not yet implemented.");
    }

    /** Tests {@code containsKey(String)}. */
    @Test
    public void testContainsKey() {
        fail("Not yet implemented.");
    }

    /** Tests {@code createRDFSubject(Model)}. */
    @Test
    public void testCreateRDFSubject() {
        fail("Not yet implemented.");
    }

    /** Tests {@code entrySet()}. */
    @Test
    public void testEntrySet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code equals(Object)} for two different MemoryNodes. */
    @Test
    public void testEqualsForTwoDifferentMemoryNodes() {
        MemoryNode one = new MemoryNode(Mets.METS);
        MemoryNode other = new MemoryNode(Mods.DISPLAY_FORM);

        assertFalse(one.equals(other));
    }

    /** Tests {@code equals(Object)} for twor equal MemoryNodes. */

    @Test
    public void testEqualsForTwoEqualMemoryNodes() {
        MemoryNode one = new MemoryNode(Mets.METS);
        MemoryNode other = new MemoryNode(Mets.METS);

        assertTrue(one.equals(other));
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

    /** Tests {@code getLast()}. */
    @Test
    public void testGetLast() {
        fail("Not yet implemented.");
    }

    /** Tests {@code getRelations()}. */
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

    /**
     * Tests {@code get(Collection<String>, Collection<ObjectType>)} with both
     * relation and objects condition given.
     */
    @Test
    public void testGetSetOfStringSetOfObjectType() {
        MemoryNode mets = new MemoryNode(Mets.METS)
                .add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                .add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

        Set<String> relation = new HashSet<>();
        relation.add(RDF.toURL(Node.FIRST_INDEX));
        Set<ObjectType> condition = new HashSet<>();
        condition.add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL"));

        assertEquals(
                new MemoryResult(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL").<MemoryNode>add(
                        new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                mets.get(relation, condition));
    }

    /**
     * Tests {@code get(Collection<String>, Collection<ObjectType>)} with
     * unspecified relation, but objects condition given.
     */
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
                new MemoryResult(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL").<MemoryNode>add(
                        new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                mets.get(ANY_RELATION, condition));
    }

    /**
     * Tests {@code get(Collection<String>, Collection<ObjectType>)} with
     * relation given, but no objects condition.
     */
    @Test
    public void testGetSetOfStringSetOfObjectTypeWithEmptyConditions() {
        MemoryNode mets = new MemoryNode(Mets.METS)
                .add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph")))
                .add(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "PHYSICAL")
                        .add(new MemoryNode(Mets.DIV).put(Mets.TYPE, "physSequence")));

        Set<String> relation = new HashSet<>();
        relation.add(RDF.toURL(Node.FIRST_INDEX));
        final Set<ObjectType> ANY_NON_EMPTY_RESULT = Collections.emptySet();

        assertEquals(
                new MemoryResult(new MemoryNode(Mets.STRUCT_MAP).put(Mets.TYPE, "LOGICAL").<MemoryNode>add(
                        new MemoryNode(Mets.DIV).put(Mets.ORDERLABEL, " - ").put(Mets.TYPE, "Monograph"))),

                mets.get(relation, ANY_NON_EMPTY_RESULT));
    }

    /** Tests {@code get(String)}. */
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
    @Test(expected = NoSuchElementException.class)
    public void testGetType0() {
        new MemoryNode().getType();
    }

    /** Tests {@code getType()} on a node with exactly one type. */
    @Test
    public void testGetType1() {
        assertEquals(Mods.NAME.getIdentifier(), new MemoryNode(Mods.NAME).getType());
    }

    /** Tests {@code getType()} on a node with several types. */
    @Test(expected = BufferOverflowException.class)
    public void testGetType2() {
        MemoryNode node = new MemoryNode();
        node.put(RDF.TYPE, "http://names.example/petAnimal");
        node.put(RDF.TYPE, "http://names.example/mammal");

        node.getType();
    }

    /** Tests {@code hashCode()} for two non-equal nodes. */
    @Test
    public void testHashCodeIsDifferentForTwoDifferentMemoryNodes() {
        MemoryNode one = new MemoryNode(Mets.METS);
        MemoryNode other = new MemoryNode(Mods.MODS);

        assertFalse(one.hashCode() == other.hashCode());
    }

    /** Tests {@code hashCode()} for two equal nodes. */
    @Test
    public void testHashCodeIsEqualForTwoEqualMemoryNodes() {
        MemoryNode one = new MemoryNode(Mets.METS);
        MemoryNode other = new MemoryNode(Mets.METS);

        assertTrue(one.hashCode() == other.hashCode());
    }

    /** Tests {@code isEmpty()} for an empty node. */
    @Test
    public void testIsEmpty() {
        assertTrue(new MemoryNode().isEmpty());
    }

    /** Tests {@code isEmpty()} for a non-empty node. */
    @Test
    public void testIsNotEmpty() {
        assertFalse(new MemoryNode(Mets.METS).isEmpty());
    }

    /** Tests {@code iterator()}. */

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

    /** Tests {@code last()}. */

    @Test
    public void testLast() {
        MemoryNode node = new MemoryNode(Mods.NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        assertEquals((long) 3, (long) node.last().orElse(null));
    }

    /** Tests {@code get(long)}. */
    @Test
    public void testLong() {
        fail("Not yet implemented.");
    }

    /** Tests {@code matches(ObjectType)} for a matching candidate. */
    @Test
    public void testMatches() {
        MemoryNode node = new MemoryNode(Mods.NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        MemoryNode condition = new MemoryNode().put(RDF.toURL(2), new MemoryNode("http://names.example/bob"));

        assertTrue(node.matches(condition));
    }

    /** Tests {@code matches(ObjectType)} for a non-matching candidate. */
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

    /** Tests {@code MemoryNode()} constructor. */
    @Test
    public void testMemoryNodeCanBeCreatedEmpty() {
        new MemoryNode();
    }

    /** Tests {@code MemoryNode(IdentifiableNode)} constructor. */
    @Test
    public void testMemoryNodeCanBeCreatedWithMemoryNodeReferenceAsType() {
        MemoryNode mets = new MemoryNode(Mets.METS);

        assertEquals(Mets.METS.getIdentifier(), mets.getType());
    }

    /** Tests {@code MemoryNode(String)} constructor. */
    @Test
    public void testMemoryNodeCanBeCreatedWithStringAsType() {
        final String MODS = Mods.MODS.getIdentifier();
        MemoryNode mods = new MemoryNode(MODS);

        assertEquals(MODS, mods.getType());
    }

    /** Tests {@code MemoryNode(String)} constructor. */
    @Test(expected = AssertionError.class)
    public void testMemoryNodeCreatedWithEmptyStringAsTypeIsEmpty() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            new MemoryNode("");
        }
    }

    /** Tests {@code MemoryNode(IdentifiableNode)} constructor. */
    @Test
    public void testMemoryNodeCreatedWithUnitializedMemoryNodeReferenceIsEmpty() {
        MemoryNode a = new MemoryNode((MemoryNodeReference) null);
        assertTrue(a.isEmpty());
    }

    /** Tests {@code MemoryNode(String)} constructor. */
    @Test
    public void testMemoryNodeCreatedWithUnitializedStringIsEmpty() {
        MemoryNode a = new MemoryNode((String) null);
        assertTrue(a.isEmpty());
    }

    /** Tests {@code putAll(String, Collection<? extends ObjectType>)}. */
    @Test
    public void testPutAll() {
        fail("Not yet implemented.");
    }

    // @Test
    // public void testPutMemoryNodeReferenceString() {
    // new MemoryNode().put(Mods.NAME, "Wilhelm Busch");
    // }
    /** Tests {@code put(String, ObjectType)}. */

    @Test
    public void testPutStringObjectType() {
        new MemoryNode().put("http://www.loc.gov/mods/v3#name", MemoryLiteral.create("Wilhelm Busch", "de"));
    }

    /** Tests {@code put(String, String)}. */
    @Test
    public void testPutStringString() {
        new MemoryNode().put("http://www.loc.gov/mods/v3#name", "Wilhelm Busch");
    }

    /** Tests {@code remove(Object)}. */
    @Test
    public void testRemove() {
        fail("Not yet implemented.");
    }

    /** Tests {@code removeAll(String)}. */
    @Test
    public void testRemoveAll() {
        fail("Not yet implemented.");
    }

    /** Tests {@code removeFirst()}. */
    @Test
    public void testRemoveFirst() {
        fail("Not yet implemented.");
    }

    /** Tests {@code removeFirstOccurrence(Object)}. */
    @Test
    public void testRemoveFirstOccurrence() {
        fail("Not yet implemented.");
    }

    /** Tests {@code removeLast()}. */
    @Test
    public void testRemoveLast() {
        fail("Not yet implemented.");
    }

    /** Tests {@code removeLastOccurrence(Object)}. */
    @Test
    public void testRemoveLastOccurrence() {
        fail("Not yet implemented.");
    }

    /** Tests {@code replace(String, Set<ObjectType>)}. */
    @Test
    public void testReplace() {
        fail("Not yet implemented.");
    }

    /**
     * Tests {@code replaceAllNamedNodesWithNoDataByNodeReferences(boolean)}.
     */
    @Test
    public void testReplaceAllNamedMemoryNodesWithNoDataByMemoryNodeReferences() {
        MemoryNode a = new MemoryNode().put(RDF.TYPE, new MemoryNamedNode("http://www.loc.gov/mods/v3#name"));
        a.replaceAllNamedNodesWithNoDataByNodeReferences(true);

        assertEquals(new MemoryNode().put(RDF.TYPE, new MemoryNodeReference("http://www.loc.gov/mods/v3#name")), a);
    }

    /** Tests {@code set(int, ObjectType)}. */
    @Test
    public void testSet() {
        fail("Not yet implemented.");
    }

    /** Tests {@code setValue(String)}. */
    @Test
    public void testSetValue() {
        fail("Not yet implemented.");
    }

    // @Test
    // public void testPutMemoryNodeReferenceObjectType() {
    // new MemoryNode().put(Mods.NAME, MemoryLiteral.create("Wilhelm Busch",
    // "de"));
    // }

    /** Tests {@code size()}. */
    @Test
    public void testSize() {
        fail("Not yet implemented.");
    }

    /** Tests {@code toRDFNode(Model, Boolean)}. */
    @Test
    public void testToRDFNode() {
        fail("Not yet implemented.");
    }

    /** Tests {@code toString()}. */
    @Test
    public void testToString() {
        fail("Not yet implemented.");
    }
}
