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

package org.kitodo.dataaccess.storage.memory;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.Test;
import org.kitodo.dataaccess.LinkedDataException;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.ObjectType;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.Result;

/**
 * Tests {@code org.kitodo.dataaccess.storage.memory.MemoryNode}.
 */
public class MemoryNodeTest {

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
    private static final MemoryNodeReference MODS_DISPLAY_FORM = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#displayForm");
    private static final MemoryNodeReference MODS_MODS = new MemoryNodeReference("http://www.loc.gov/mods/v3#mods");
    private static final MemoryNodeReference MODS_NAME = new MemoryNodeReference("http://www.loc.gov/mods/v3#name");

    /**
     * Tests {@code addAll(Collection<? extends ObjectType>)}.
     */
    @Test
    public void testAddAll() {
        MemoryNode node = new MemoryNode();

        MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        MemoryNode mods = new MemoryNode(MODS_MODS);
        ArrayList<ObjectType> toAdd = new ArrayList<>();
        toAdd.add(javac);
        toAdd.add(mods);

        node.addAll(toAdd);

        assertThat(node.size(), is(equalTo(2)));
        assertThat(node.first().orElse(null), is(equalTo((long) Node.FIRST_INDEX)));
        assertThat(node.last().orElse(null), is((long) 2));
        assertThat(node.getFirst(), contains(javac));
        assertThat(node.getLast(), contains(mods));
    }

    /**
     * Tests {@code addFirst(ObjectType)}.
     */
    @Test
    public void testAddFirst() {
        MemoryNode node = new MemoryNode();

        MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        MemoryNode mods = new MemoryNode(MODS_MODS);

        node.add(mods);
        node.addFirst(javac);

        assertThat(node.size(), is(equalTo(2)));
        assertThat(node.first().orElse(null), is(equalTo((long) Node.FIRST_INDEX)));
        assertThat(node.last().orElse(null), is((long) 2));
        assertThat(node.getFirst(), contains(javac));
        assertThat(node.getLast(), contains(mods));

    }

    /**
     * Tests {@code add(long, ObjectType)}.
     */
    @Test
    public void testAddLongObjectType() {
        final MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        final MemoryNode mods = new MemoryNode(MODS_MODS);
        final MemoryNodeReference kitodo = new MemoryNodeReference("https://www.kitodo.org/");

        MemoryNode node = new MemoryNode();
        node.add(mods);
        node.add(javac);

        node.add(2, kitodo);

        assertThat(node.size(), is(equalTo(3)));
        assertThat(node.first().orElse(null), is((long) 1));
        assertThat(node.last().orElse(null), is((long) 3));
        assertThat(node.getFirst(), contains(mods));
        assertThat(node.get(2), contains(kitodo));
        assertThat(node.getLast(), contains(javac));
    }

    /**
     * Tests {@code add(ObjectType)}.
     */
    @Test
    public void testAddObjectType() {
        MemoryNode mets = new MemoryNode(METS_METS);
        mets.add(new MemoryNode(METS_METS_HDR));

        assertThat(mets.get(RDF.toURL(Node.FIRST_INDEX)).size(), is(equalTo(1)));
    }

    /**
     * Tests {@code asUnordered(boolean)}.
     */
    @Test
    public void testAsUnordered() {
        MemoryNode testNode = new MemoryNode().add(new MemoryNode(MODS_NAME).setValue("Max"))
                .add(new MemoryNode(MODS_NAME).setValue("Moritz")).put(METS_USE, "foobar");

        ObjectType outcome = testNode.asUnordered(false);

        assertThat(outcome, is(instanceOf(Node.class)));

        Node node = (Node) outcome;

        assertThat(node.first().isPresent(), is(equalTo(false)));
        assertThat(node.last().isPresent(), is(equalTo(false)));
        assertThat(node.get(MODS_NAME), is(iterableWithSize(2)));
        assertThat(node.get(METS_USE), is(iterableWithSize(1)));
    }

    /**
     * Tests {@code contains(Object)}.
     */
    @Test
    public void testContains() {
        MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        MemoryNode mods = new MemoryNode(MODS_MODS);
        MemoryNodeReference kitodo = new MemoryNodeReference("https://www.kitodo.org/");

        MemoryNode node = new MemoryNode().add(javac).add(kitodo).put(METS_TYPE, mods);

        assertThat(node.contains(mods), is(equalTo(true)));
        assertThat(node.contains(javac), is(equalTo(true)));
        assertThat(node.contains(kitodo), is(equalTo(true)));
    }

    /**
     * Tests {@code containsKey(String)}.
     */
    @Test
    public void testContainsKey() {
        MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        MemoryNode mods = new MemoryNode(MODS_MODS);
        MemoryNodeReference kitodo = new MemoryNodeReference("https://www.kitodo.org/");

        MemoryNode node = new MemoryNode().add(javac).add(kitodo).put(METS_TYPE, mods);

        assertThat(node.containsKey(METS_TYPE.getIdentifier()), is(equalTo(true)));
    }

    /**
     * Tests {@code entrySet()}.
     */
    @Test
    public void testEntrySet() {
        MemoryNode node = new MemoryNode().put(METS_TYPE, MODS_MODS);

        Set<Entry<String, Collection<ObjectType>>> entrySet = node.entrySet();
        Iterator<Entry<String, Collection<ObjectType>>> entrySetIterator = entrySet.iterator();
        Entry<String, Collection<ObjectType>> entry = entrySetIterator.next();

        assertThat(entry.getKey(), is(equalTo(METS_TYPE.getIdentifier())));
        assertThat(entry.getValue(), hasSize(1));
        assertThat(entry.getValue(), containsInAnyOrder(MODS_MODS));
        assertThat(entrySetIterator.hasNext(), is(equalTo(false)));
    }

    /**
     * Tests {@code equals(Object)} for two different MemoryNodes.
     */
    @Test
    public void testEqualsForTwoDifferentMemoryNodes() {
        MemoryNode one = new MemoryNode(METS_METS);
        MemoryNode other = new MemoryNode(MODS_DISPLAY_FORM);

        assertThat(one, is(not(equalTo(other))));
    }

    /**
     * Tests {@code equals(Object)} for twor equal MemoryNodes.
     */

    @Test
    public void testEqualsForTwoEqualMemoryNodes() {
        MemoryNode one = new MemoryNode(METS_METS);
        MemoryNode another = new MemoryNode(METS_METS);

        assertThat(one, is(equalTo(another)));
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
        MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        MemoryNode mods = new MemoryNode(MODS_MODS);
        MemoryNodeReference kitodo = new MemoryNodeReference("https://www.kitodo.org/");

        MemoryNode node = new MemoryNode().add(javac).add(kitodo).put(METS_TYPE, mods);
        node.removeAll(RDF.toURL(Node.FIRST_INDEX));

        assertThat(node.first().orElse(null), is((long) 2));
    }

    /**
     * Tests {@code getByIdentifier(String)}.
     */
    @Test
    public void testGetByIdentifierString() {
        MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        MemoryNode mods = new MemoryNamedNode("http://localhost/test/nodeID", MODS_MODS);
        MemoryNodeReference kitodo = new MemoryNodeReference("https://www.kitodo.org/");

        MemoryNode node = new MemoryNode().add(javac).add(kitodo).put(METS_TYPE, mods);

        assertThat(node.getByIdentifier("http://localhost/test/nodeID"), is(equalTo(mods)));
        assertThat(node.getByIdentifier("https://www.kitodo.org/"), is(equalTo(kitodo)));
    }

    /**
     * Tests {@code getByType(String)}.
     */
    @Test
    public void testGetByTypeString() throws LinkedDataException {
        MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        MemoryNode mods = new MemoryNamedNode("http://localhost/test/nodeID", MODS_MODS);
        MemoryNodeReference kitodo = new MemoryNodeReference("https://www.kitodo.org/");

        MemoryNode node = new MemoryNode().add(javac).add(kitodo).put(METS_TYPE, mods);

        assertThat(node.getByType(MODS_MODS.getIdentifier()).node(), is(equalTo(mods)));
    }

    /**
     * Tests {@code getByType(String, String, String)}.
     */
    @Test
    public void testGetByTypeStringStringString() throws LinkedDataException {
        Node physical = new MemoryNode(METS_DIV).put(METS_TYPE, "PHYSICAL");
        Node logical = new MemoryNode(METS_DIV).put(METS_TYPE, "LOGICAL");

        Node node = new MemoryNode().add(logical).add(physical);

        assertThat(node.getByType(METS_DIV.getIdentifier(), METS_TYPE.getIdentifier(), "LOGICAL").node(),
            is(equalTo(logical)));
        assertThat(node.getByType(METS_DIV.getIdentifier(), METS_TYPE.getIdentifier(), "PHYSICAL").node(),
            is(equalTo(physical)));
    }

    /**
     * Tests {@code getEnumerated()}.
     */
    @Test
    public void testGetEnumerated() throws LinkedDataException {
        MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        MemoryNode mods = new MemoryNamedNode("http://localhost/test/nodeID", MODS_MODS);
        MemoryNodeReference kitodo = new MemoryNodeReference("https://www.kitodo.org/");

        MemoryNode node = new MemoryNode().add(javac).add(kitodo).put(METS_TYPE, mods);

        List<Result> enumerated = node.getEnumerated();
        assertThat(enumerated, hasSize(2));
        for (Result enumeratedResult : enumerated) {
            assertThat(enumeratedResult.value(), is(anyOf(equalTo(javac), equalTo(kitodo))));
        }
    }

    /**
     * Tests {@code getFirst()}.
     */
    @Test
    public void testGetFirst() {
        MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        MemoryNode mods = new MemoryNode(MODS_MODS);

        MemoryNode node = new MemoryNode();
        node.add(mods);
        node.add(javac);

        assertThat(node.getFirst(), contains(mods));
    }

    /**
     * Tests {@code getLast()}.
     */
    @Test
    public void testGetLast() {
        final MemoryLiteral javac = new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL);
        final MemoryNode mods = new MemoryNode(MODS_MODS);

        MemoryNode node = new MemoryNode();
        node.add(mods);
        node.add(javac);

        assertThat(node.getLast(), contains(javac));
    }

    /**
     * Tests {@code get(long)}.
     */
    @Test
    public void testGetLong() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        assertThat(node.get(2), contains(new MemoryNode("http://names.example/bob")));
    }

    /**
     * Tests {@code getRelations()}.
     */
    @Test
    public void testGetRelations() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        Set<String> expected = new HashSet<>();
        expected.add(RDF.TYPE.getIdentifier());
        expected.add(RDF.toURL(1));
        expected.add(RDF.toURL(2));
        expected.add(RDF.toURL(3));

        assertThat(node.getRelations(), is(equalTo(expected)));
    }

    /**
     * Tests {@code get(Collection<String>, Collection<ObjectType>)} with both
     * relation and objects condition given.
     */
    @Test
    public void testGetSetOfStringSetOfObjectType() {
        MemoryNode mets = new MemoryNode(METS_METS)
                .add(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                        .add(new MemoryNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph")))
                .add(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "PHYSICAL")
                        .add(new MemoryNode(METS_DIV).put(METS_TYPE, "physSequence")));

        Set<String> relation = new HashSet<>();
        relation.add(RDF.toURL(Node.FIRST_INDEX));
        Set<ObjectType> condition = new HashSet<>();
        condition.add(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL"));

        assertEquals(
            new MemoryResult(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                    .<MemoryNode>add(new MemoryNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph"))),

            mets.get(relation, condition));
    }

    /**
     * Tests {@code get(Collection<String>, Collection<ObjectType>)} with
     * unspecified relation, but objects condition given.
     */
    @Test
    public void testGetSetOfStringSetOfObjectTypeWithAnyRelation() {
        MemoryNode mets = new MemoryNode(METS_METS)
                .add(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                        .add(new MemoryNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph")))
                .add(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "PHYSICAL")
                        .add(new MemoryNode(METS_DIV).put(METS_TYPE, "physSequence")));

        Set<ObjectType> condition = new HashSet<>();
        condition.add(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL"));

        assertEquals(
            new MemoryResult(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                    .<MemoryNode>add(new MemoryNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph"))),

            mets.get(ANY_RELATION, condition));
    }

    /**
     * Tests {@code get(Collection<String>, Collection<ObjectType>)} with
     * relation given, but no objects condition.
     */
    @Test
    public void testGetSetOfStringSetOfObjectTypeWithEmptyConditions() {
        MemoryNode mets = new MemoryNode(METS_METS)
                .add(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                        .add(new MemoryNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph")))
                .add(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "PHYSICAL")
                        .add(new MemoryNode(METS_DIV).put(METS_TYPE, "physSequence")));

        Set<String> relation = new HashSet<>();
        relation.add(RDF.toURL(Node.FIRST_INDEX));

        assertEquals(
            new MemoryResult(new MemoryNode(METS_STRUCT_MAP).put(METS_TYPE, "LOGICAL")
                    .<MemoryNode>add(new MemoryNode(METS_DIV).put(METS_ORDERLABEL, " - ").put(METS_TYPE, "Monograph"))),

            mets.get(relation, ANY_NON_EMPTY_RESULT));
    }

    /**
     * Tests {@code get(String)}.
     */
    @Test
    public void testGetString() {
        MemoryNode node = new MemoryNode();
        node.put(RDF.TYPE, "http://names.example/petAnimal");
        node.put(RDF.TYPE, "http://names.example/mammal");

        MemoryResult expected = new MemoryResult();
        expected.add(MemoryLiteral.createLeaf("http://names.example/petAnimal", null));
        expected.add(MemoryLiteral.createLeaf("http://names.example/mammal", null));

        String relation = RDF.TYPE.getIdentifier();

        assertThat(node.get(relation), is(equalTo(expected)));
    }

    /**
     * Tests {@code get(String, IdentifiableNode, ObjectType)}.
     */
    @Test
    public void testGetStringIdentifiableNodeObjectType() throws LinkedDataException {
        Node physical = new MemoryNode().put(METS_TYPE, "PHYSICAL");
        Node logical = new MemoryNode().put(METS_TYPE, "LOGICAL");

        Node node = new MemoryNode().put(METS_DIV, logical).put(METS_DIV, physical);

        assertThat(node.get(METS_DIV.getIdentifier(), METS_TYPE, MemoryLiteral.createLiteral("LOGICAL", "")).node(),
            is(equalTo(logical)));
        assertThat(node.get(METS_DIV.getIdentifier(), METS_TYPE, MemoryLiteral.createLiteral("PHYSICAL", "")).node(),
            is(equalTo(physical)));
    }

    /**
     * Tests {@code get(String, String)}.
     */
    @Test
    public void testGetStringString() {
        MemoryNode mods = new MemoryNode(MODS_MODS);
        MemoryNode node = new MemoryNode().put(METS_DIV, mods);

        assertThat(node.get(METS_DIV.getIdentifier(), MODS_MODS.getIdentifier()), contains(mods));
    }

    /**
     * Tests {@code getType()} on a node without a type.
     */
    @Test(expected = NoSuchElementException.class)
    public void testGetTypeOnEmptyMemoryNode() {
        new MemoryNode().getType();
    }

    /**
     * Tests {@code getType()} on a node with exactly one type.
     */
    @Test
    public void testGetTypeOnMemoryNodeWithOneType() {
        assertThat(new MemoryNode(MODS_NAME).getType(), is(equalTo(MODS_NAME.getIdentifier())));
    }

    /**
     * Tests {@code getType()} on a node with several types.
     */
    @Test(expected = BufferOverflowException.class)
    public void testGetTypeOnMemoryNodeWithTwoTypes() {
        MemoryNode node = new MemoryNode();
        node.put(RDF.TYPE, "http://names.example/petAnimal");
        node.put(RDF.TYPE, "http://names.example/mammal");

        node.getType();
    }

    /**
     * Tests {@code hashCode()} for two non-equal nodes.
     */
    @Test
    public void testHashCodeIsDifferentForTwoDifferentMemoryNodes() {
        MemoryNode one = new MemoryNode(METS_METS);
        MemoryNode other = new MemoryNode(MODS_MODS);

        assertThat(one.hashCode() == other.hashCode(), is(false));
    }

    /**
     * Tests {@code hashCode()} for two equal nodes.
     */
    @Test
    public void testHashCodeIsEqualForTwoEqualMemoryNodes() {
        MemoryNode one = new MemoryNode(METS_METS);
        MemoryNode other = new MemoryNode(METS_METS);

        assertThat(one.hashCode() == other.hashCode(), is(true));
    }

    /**
     * Tests {@code hasType(String)}.
     */
    @Test
    public void testHasType() {
        MemoryNode node = new MemoryNode(MODS_MODS).put(RDF.TYPE, METS_MD_WRAP);

        assertThat(node.hasType(MODS_MODS.getIdentifier()), is(true));
        assertThat(node.hasType(METS_MD_WRAP.getIdentifier()), is(true));
    }

    /**
     * Tests {@code isEmpty()} for an empty node.
     */
    @Test
    public void testIsEmpty() {
        assertThat(new MemoryNode().isEmpty(), is(true));
    }

    /**
     * Tests {@code isEmpty()} for a non-empty node.
     */
    @Test
    public void testIsNotEmpty() {
        assertThat(new MemoryNode(METS_METS).isEmpty(), is(false));
    }

    /**
     * Tests {@code iterator()}.
     */
    @Test
    public void testIterator() {
        MemoryNode node = new MemoryNode(MODS_NAME);
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
        assertThat(expected.isEmpty(), is(true));
    }

    /**
     * Tests {@code last()}.
     */
    @Test
    public void testLast() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        assertEquals((long) 3, (long) node.last().orElse(null));
    }

    /**
     * Tests {@code matches(ObjectType)} for a matching candidate.
     */
    @Test
    public void testMatches() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        MemoryNode condition = new MemoryNode().put(RDF.toURL(2), new MemoryNode("http://names.example/bob"));

        assertThat(node.matches(condition), is(true));
    }

    /**
     * Tests {@code matches(ObjectType)} for a non-matching candidate.
     */
    @Test
    public void testMatchesNot() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        node.add(new MemoryNode("http://names.example/bob"));
        node.add(new MemoryNode("http://names.example/charlie"));

        MemoryNode condition = new MemoryNode(MODS_NAME).put(RDF.toURL(9),
            new MemoryNode("http://names.example/alice"));

        assertThat(node.matches(condition), is(false));
    }

    /**
     * Tests {@code MemoryNode()} constructor.
     */
    @Test
    public void testMemoryNodeCanBeCreatedEmpty() {
        new MemoryNode();
    }

    /**
     * Tests {@code MemoryNode(IdentifiableNode)} constructor.
     */
    @Test
    public void testMemoryNodeCanBeCreatedWithMemoryNodeReferenceAsType() {
        MemoryNode mets = new MemoryNode(METS_METS);

        assertThat(mets.getType(), is(equalTo(METS_METS.getIdentifier())));
    }

    /**
     * Tests {@code MemoryNode(String)} constructor.
     */
    @Test
    public void testMemoryNodeCanBeCreatedWithStringAsType() {
        final String MODS = MODS_MODS.getIdentifier();
        MemoryNode mods = new MemoryNode(MODS);

        assertThat(mods.getType(), is(equalTo(MODS)));
    }

    /**
     * Tests {@code MemoryNode(String)} constructor.
     */
    @Test(expected = AssertionError.class)
    public void testMemoryNodeCreatedWithEmptyStringAsTypeIsEmpty() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            new MemoryNode("");
        }
    }

    /**
     * Tests {@code MemoryNode(IdentifiableNode)} constructor.
     */
    @Test
    public void testMemoryNodeCreatedWithUnitializedMemoryNodeReferenceIsEmpty() {
        MemoryNode a = new MemoryNode((MemoryNodeReference) null);
        assertThat(a.isEmpty(), is(true));
    }

    /**
     * Tests {@code MemoryNode(String)} constructor.
     */
    @Test
    public void testMemoryNodeCreatedWithUnitializedStringIsEmpty() {
        MemoryNode a = new MemoryNode((String) null);
        assertThat(a.isEmpty(), is(true));
    }

    /**
     * Tests {@code putAll(String, Collection<? extends ObjectType>)}.
     */
    @Test
    public void testPutAll() {
        List<ObjectType> nodes = new ArrayList<>();
        nodes.add(new MemoryNodeReference("http://names.example/alice"));
        nodes.add(new MemoryNodeReference("http://names.example/bob"));
        nodes.add(new MemoryNodeReference("http://names.example/charlie"));

        Node node = new MemoryNode();
        node.putAll(MODS_NAME.getIdentifier(), nodes);

        assertThat(node.get(MODS_NAME).countUntil(Long.MAX_VALUE), is((long) 3));
    }

    /**
     * Tests {@code put(String, ObjectType)}.
     */

    @Test
    public void testPutStringObjectType() {
        new MemoryNode().put("http://www.loc.gov/mods/v3#name", MemoryLiteral.createLeaf("Wilhelm Busch", "de"));
    }

    /**
     * Tests {@code put(String, String)}.
     */
    @Test
    public void testPutStringString() {
        new MemoryNode().put("http://www.loc.gov/mods/v3#name", "Wilhelm Busch");
    }

    /**
     * Tests {@code remove(Object)}.
     */
    @Test
    public void testRemove() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        MemoryNode bob = new MemoryNode("http://names.example/bob");
        node.add(bob);
        node.add(new MemoryNode("http://names.example/charlie"));

        assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(3));
        assertThat(node.remove(bob), is(true));
        assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(2));
        assertThat(node.remove(bob), is(false));
        assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(2));
    }

    /**
     * Tests {@code removeAll(String)}.
     */
    @Test
    public void testRemoveAll() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        node.add(new MemoryNode("http://names.example/alice"));
        MemoryNode bob = new MemoryNode("http://names.example/bob");
        node.add(bob);
        node.add(new MemoryNode("http://names.example/charlie"));

        assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(3));
        assertThat(node.removeAll(RDF.toURL(2)), contains(bob));
        assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(2));
        assertThat(node.removeAll(RDF.toURL(2)), is(nullValue()));
        assertThat(node.getEnumerated().stream().mapToInt(r -> r.isAny() ? 1 : 0).sum(), is(2));
    }

    /**
     * Tests {@code removeFirst()}.
     */
    @Test
    public void testRemoveFirst() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        MemoryNode alice = new MemoryNode("http://names.example/alice");
        node.add(alice);
        MemoryNode bob = new MemoryNode("http://names.example/bob");
        node.add(bob);
        MemoryNode charlie = new MemoryNode("http://names.example/charlie");
        node.add(charlie);

        node.removeFirst();

        assertThat(node.get(1), contains(bob));
        assertThat(node.get(2), contains(charlie));

        node.removeFirst();

        assertThat(node.get(1), contains(charlie));
    }

    /**
     * Tests {@code removeFirstOccurrence(Object)}.
     */
    @Test
    public void testRemoveFirstOccurrenceWithOneElementInRelation() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        MemoryNode alice = new MemoryNode("http://names.example/alice");
        node.add(alice);
        MemoryNode bob = new MemoryNode("http://names.example/bob");
        node.add(bob);
        MemoryNode charlie = new MemoryNode("http://names.example/charlie");
        node.add(charlie);

        node.removeFirstOccurrence(bob);

        assertThat(node.get(1), contains(alice));
        assertThat(node.get(2), contains(charlie));
    }

    /**
     * Tests {@code removeFirstOccurrence(Object)}.
     */
    @Test
    public void testRemoveFirstOccurrenceWithTwoElementsInRelation() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        MemoryNode alice = new MemoryNode("http://names.example/alice");
        node.add(alice);
        MemoryNode bob = new MemoryNode("http://names.example/bob");
        MemoryNode james = new MemoryNode("http://names.example/james");
        node.add(bob).put(RDF.toURL(2), james);
        MemoryNode charlie = new MemoryNode("http://names.example/charlie");
        node.add(charlie);

        node.removeFirstOccurrence(bob);

        assertThat(node.get(1), contains(alice));
        assertThat(node.get(2), not(contains(bob)));
        assertThat(node.get(2), contains(james));
        assertThat(node.get(3), contains(charlie));
    }

    /**
     * Tests {@code removeLast()}.
     */
    @Test
    public void testRemoveLast() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        MemoryNode alice = new MemoryNode("http://names.example/alice");
        node.add(alice);
        MemoryNode bob = new MemoryNode("http://names.example/bob");
        node.add(bob);
        MemoryNode charlie = new MemoryNode("http://names.example/charlie");
        node.add(charlie);

        node.removeLast();

        assertThat(node.getEnumerated(), hasSize(2));
        assertThat(node.getLast(), contains(bob));
    }

    /**
     * Tests {@code removeLastOccurrence(Object)}.
     */
    @Test
    public void testRemoveLastOccurrence() {
        MemoryNode node = new MemoryNode(MODS_NAME);
        MemoryNode alice = new MemoryNode("http://names.example/alice");
        node.add(alice);
        MemoryNode bob = new MemoryNode("http://names.example/bob");
        node.add(bob);
        MemoryNode charlie = new MemoryNode("http://names.example/charlie");
        node.add(charlie);

        assertThat(node.removeLastOccurrence(bob), is(true));
        assertThat(node.last().orElse(null), is((long) 2));
        assertThat(node.get(2), contains(charlie));
        assertThat(node.removeLastOccurrence(bob), is(false));
        assertThat(node.last().orElse(null), is((long) 2));
        assertThat(node.get(2), contains(charlie));
    }

    /**
     * Tests {@code replace(String, Set<ObjectType>)}.
     */
    @Test
    public void testReplace() {
        MemoryNode alice = new MemoryNode("http://names.example/alice");
        MemoryNode bob = new MemoryNode("http://names.example/bob");
        MemoryNode charlie = new MemoryNode("http://names.example/charlie");
        MemoryNode james = new MemoryNode("http://names.example/james");

        MemoryNode node = new MemoryNode(MODS_NAME).add(alice).add(bob).add(charlie);

        node.replace(RDF.toURL(2), new HashSet<>(Arrays.asList(new ObjectType[] {bob, james })));

        assertThat(node.get(2), containsInAnyOrder(james, bob));
    }

    /**
     * Tests {@code replaceAllNamedNodesWithNoDataByNodeReferences(boolean)}.
     */
    @Test
    public void testReplaceAllNamedMemoryNodesWithNoDataByMemoryNodeReferences() {
        MemoryNode a = new MemoryNode().put(RDF.TYPE, new MemoryNamedNode("http://www.loc.gov/mods/v3#name"));
        a.replaceAllMemoryNamedNodesWithNoDataByNodeReferences(true);

        assertEquals(new MemoryNode().put(RDF.TYPE, new MemoryNodeReference("http://www.loc.gov/mods/v3#name")), a);
    }

    /**
     * Tests {@code set(long, ObjectType)}.
     */
    @Test
    public void testSet() {
        MemoryNode alice = new MemoryNode("http://names.example/alice");

        MemoryNode node = new MemoryNode();

        node.set(42, alice);

        assertThat(node.get(42), contains(alice));
    }

    /**
     * Tests {@code setValue(String)}.
     */
    @Test
    public void testSetValue() {
        MemoryNode node = new MemoryNode(MODS_NAME).put(METS_TYPE, "LOGICAL")
                .add(new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL));

        node.setValue("public static void main(String[] args)");

        assertThat(node.getType(), is(MODS_NAME.getIdentifier()));
        assertThat(node.getRelations(), hasSize(3));
        assertThat(node.get(1),
            contains(new MemoryLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL)));
    }

    /**
     * Tests {@code size()}.
     */
    @Test
    public void testSize() {
        MemoryNode node = new MemoryNode(MODS_NAME); // 1× rdf:type
        MemoryNode alice = new MemoryNode("http://names.example/alice");
        node.add(alice); // 1× rdf:_1
        MemoryNode bob = new MemoryNode("http://names.example/bob");
        MemoryNode james = new MemoryNode("http://names.example/james");
        node.add(bob).put(RDF.toURL(2), james); // 2× rdf:_1
        MemoryNode charlie = new MemoryNode("http://names.example/charlie");
        node.add(charlie); // 1× rdf:_3

        assertThat(node.size(), is(5));
    }

    /**
     * Tests {@code toString()}.
     */
    @Test
    public void testToString() {
        MemoryNode node = new MemoryNode(MODS_NAME).put(METS_TYPE, "LOGICAL")
                .add(new MemoryLiteral("javac.exe", RDF.PLAIN_LITERAL));

        assertThat(node.toString(), is(
            "http://www.loc.gov/METS/TYPE = \"LOGICAL\"\nhttp://www.w3.org/1999/02/22-rdf-syntax-ns#type = ↗http://www.loc.gov/mods/v3#name\nhttp://www.w3.org/1999/02/22-rdf-syntax-ns#_1 = \"javac.exe\"\n"));
    }
}
