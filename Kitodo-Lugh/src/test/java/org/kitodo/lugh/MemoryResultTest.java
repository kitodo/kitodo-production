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

import java.util.*;

import org.apache.jena.rdf.model.*;
import org.junit.Test;

/** Tests {@code org.kitodo.lugh.MemoryResult}. */
public class MemoryResultTest {

    /** Tests {@code HashSet.add(ObjectType e)}. */
    @Test
    public void testAddObjectType() {
        MemoryResult r = new MemoryResult();

        assertThat(r.size(), is(equalTo(0)));

        r.add(new MemoryNode(Mets.METS_HDR));

        assertThat(r.size(), is(equalTo(1)));
    }

    /** Tests {@code HashSet.clear()}. */
    @Test
    public void testClear() {
        MemoryResult r = new MemoryResult();
        r.add(Mets.ADMID);
        r.add(Mods.EXTENT);
        r.clear();
        assertThat(r.size(), is(equalTo(0)));
    }

    /** Tests {@code countUntil(Class<? extends ObjectType>, int)}. */
    @Test
    public void testCount() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        assertThat(r.countUntil(2, Node.class), is(greaterThanOrEqualTo((long) 2)));
        assertThat(r.countUntil(2, Literal.class), is(greaterThanOrEqualTo((long) 1)));
        assertThat(r.countUntil(2), is(greaterThanOrEqualTo((long) 2)));
        assertThat(r.countUntil(99), is(equalTo((long) 4)));
    }

    /** Tests {@code subset(Class<T>)}. */
    @Test
    public void testIdentifiableNodes() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        Set<IdentifiableNode> expected = new HashSet<>();
        expected.add(new MemoryNamedNode("http://example.org/foo"));
        expected.add(new MemoryNodeReference("http://example.org/bar"));

        assertThat(r.subset(IdentifiableNode.class), is(equalTo(expected)));
    }

    /** Tests {@code leaves()}. */
    @Test
    public void testLeaves() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        Set<String> expected = new HashSet<>();
        expected.add("http://example.org/bar");
        expected.add("Hello world!");

        assertThat(r.leaves(), is(equalTo(expected)));
    }

    /** Tests {@code leaves(String)}. */
    @Test
    public void testLeavesString() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        Set<String> expectedOneOf = new HashSet<>();
        expectedOneOf.add("http://example.org/bar ; Hello world!");
        expectedOneOf.add("Hello world! ; http://example.org/bar");

        assertThat(expectedOneOf.contains(r.leaves(" ; ")), is(true));
    }

    /** Tests {@code MemoryResult()} constructor. */
    @Test
    public void testMemoryResult() {
        MemoryResult r = new MemoryResult();
        assertThat(r.size(), is(equalTo(0)));
    }

    /**
     * Tests {@code MemoryResult(Collection<? extends ObjectType>)} constructor.
     */
    @Test
    public void testMemoryResultCollectionOfObjectType() {
        MemoryResult another = new MemoryResult();
        another.add(new MemoryNode(Mets.METS_HDR));
        another.add(Mods.IDENTIFIER);

        MemoryResult r = new MemoryResult(another);
        assertThat(r, hasSize(another.size()));
        assertThat(r, is(equalTo(another)));
    }

    /** Tests {@code MemoryResult(int)} constructor. */
    @Test
    public void testMemoryResultInt() {
        MemoryResult r = new MemoryResult(42);
        assertThat(r, hasSize(0));
    }

    /** Tests {@code createResult(Model, boolean)}. */
    @Test
    public void testMemoryResultModelBoolean() throws LinkedDataException {
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

        Model m = ModelFactory.createDefaultModel();
        modsSection.toRDFNode(m, true);
        assertThat(new MemoryResult(m, false).node(), is(equalTo(modsSection)));
    }

    /** Tests {@code MemoryResult(ObjectType)} constructor. */
    @Test
    public void testMemoryResultObjectType() {
        MemoryResult expected = new MemoryResult();
        expected.add(new MemoryNode(Mets.METS_HDR));

        MemoryResult r = new MemoryResult(new MemoryNode(Mets.METS_HDR));

        assertThat(r.size(), is(equalTo(expected.size())));
        assertThat(r, is(equalTo(expected)));
    }

    /** Tests {@code subset(Class<T>)}. */
    @Test
    public void testNodes() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));

        Set<Node> expected = new HashSet<>();
        expected.add(new MemoryNamedNode("http://example.org/foo"));
        expected.add(new MemoryNode("http://example.org/baz"));

        assertThat(r.subset(Node.class), is(equalTo(expected)));
    }

    /** Tests {@code strings()}. */
    @Test
    public void testStrings() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));
        r.add(MemoryLiteral.createLeaf("public static void main(String[] args)", null));

        Set<String> expected = new HashSet<>();
        expected.add("Hello world!");
        expected.add("public static void main(String[] args)");

        assertThat(r.strings(), is(equalTo(expected)));
    }

    /** Tests {@code strings(String)}. */
    @Test
    public void testStringsString() {
        MemoryResult r = new MemoryResult();
        r.add(new MemoryNamedNode("http://example.org/foo"));
        r.add(new MemoryNodeReference("http://example.org/bar"));
        r.add(new MemoryNode("http://example.org/baz"));
        r.add(MemoryLiteral.createLiteral("Hello world!", "en"));
        r.add(MemoryLiteral.createLeaf("public static void main(String[] args)", null));

        assertThat(r.strings(" xxxfxxx "),
                is(anyOf(equalTo("public static void main(String[] args) xxxfxxx Hello world!"),
                        equalTo("Hello world! xxxfxxx public static void main(String[] args)"))));
    }

}
