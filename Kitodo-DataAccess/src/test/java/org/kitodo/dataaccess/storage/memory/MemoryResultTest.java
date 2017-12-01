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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.HashSet;
import java.util.Set;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.kitodo.dataaccess.IdentifiableNode;
import org.kitodo.dataaccess.LinkedDataException;
import org.kitodo.dataaccess.Literal;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.RDF;

/**
 * Tests {@code org.kitodo.dataaccess.storage.memory.MemoryResult}.
 */
public class MemoryResultTest {

    private static final MemoryNodeReference METS_ADMID = new MemoryNodeReference("http://www.loc.gov/METS/ADMID");
    private static final MemoryNodeReference METS_METS_HDR = new MemoryNodeReference("http://www.loc.gov/METS/metsHdr");
    private static final MemoryNodeReference MODS_AUTHORITY = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#authority");
    private static final MemoryNodeReference MODS_CLASSIFICATION = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#classification");
    private static final MemoryNodeReference MODS_DATE_ISSUED = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#dateIssued");
    private static final MemoryNodeReference MODS_DISPLAY_FORM = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#displayForm");
    private static final MemoryNodeReference MODS_ENCODING = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#encoding");
    private static final MemoryNodeReference MODS_EXTENT = new MemoryNodeReference("http://www.loc.gov/mods/v3#extent");
    private static final MemoryNodeReference MODS_IDENTIFIER = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#identifier");
    private static final MemoryNodeReference MODS_LANGUAGE = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#language");
    private static final MemoryNodeReference MODS_LANGUAGE_TERM = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#languageTerm");
    private static final MemoryNodeReference MODS_MODS = new MemoryNodeReference("http://www.loc.gov/mods/v3#mods");
    private static final MemoryNodeReference MODS_NAME = new MemoryNodeReference("http://www.loc.gov/mods/v3#name");
    private static final MemoryNodeReference MODS_NAME_PART = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#namePart");
    private static final MemoryNodeReference MODS_PHYSICAL_DESCRIPTION = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#physicalDescription");
    private static final MemoryNodeReference MODS_PLACE = new MemoryNodeReference("http://www.loc.gov/mods/v3#place");
    private static final MemoryNodeReference MODS_PLACE_TERM = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#placeTerm");
    private static final MemoryNodeReference MODS_PUBLISHER = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#publisher");
    private static final MemoryNodeReference MODS_RECORD_IDENTIFIER = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#recordIdentifier");
    private static final MemoryNodeReference MODS_RECORD_INFO = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#recordInfo");
    private static final MemoryNodeReference MODS_ROLE = new MemoryNodeReference("http://www.loc.gov/mods/v3#role");
    private static final MemoryNodeReference MODS_ROLE_TERM = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#roleTerm");
    private static final MemoryNodeReference MODS_SOURCE = new MemoryNodeReference("http://www.loc.gov/mods/v3#source");
    private static final MemoryNodeReference MODS_TITLE = new MemoryNodeReference("http://www.loc.gov/mods/v3#title");
    private static final MemoryNodeReference MODS_TITLE_INFO = new MemoryNodeReference(
            "http://www.loc.gov/mods/v3#titleInfo");
    private static final MemoryNodeReference MODS_TYPE = new MemoryNodeReference("http://www.loc.gov/mods/v3#type");

    /**
     * Tests {@code HashSet.add(ObjectType e)}.
     */
    @Test
    public void testAddObjectType() {
        MemoryResult r = new MemoryResult();

        assertThat(r.size(), is(equalTo(0)));

        r.add(new MemoryNode(METS_METS_HDR));

        assertThat(r.size(), is(equalTo(1)));
    }

    /**
     * Tests {@code HashSet.clear()}.
     */
    @Test
    public void testClear() {
        MemoryResult r = new MemoryResult();
        r.add(METS_ADMID);
        r.add(MODS_EXTENT);
        r.clear();
        assertThat(r.size(), is(equalTo(0)));
    }

    /**
     * Tests {@code countUntil(Class<? extends ObjectType>, int)}.
     */
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

    /**
     * Tests {@code subset(Class<T>)}.
     */
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

    /**
     * Tests {@code leaves()}.
     */
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

    /**
     * Tests {@code leaves(String)}.
     */
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

    /**
     * Tests {@code MemoryResult()} constructor.
     */
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
        another.add(new MemoryNode(METS_METS_HDR));
        another.add(MODS_IDENTIFIER);

        MemoryResult r = new MemoryResult(another);
        assertThat(r, hasSize(another.size()));
        assertThat(r, is(equalTo(another)));
    }

    /**
     * Tests {@code MemoryResult(int)} constructor.
     */
    @Test
    public void testMemoryResultInt() {
        MemoryResult r = new MemoryResult(42);
        assertThat(r, hasSize(0));
    }

    /**
     * Tests {@code createResult(Model, boolean)}.
     */
    @Test
    public void testMemoryResultModelBoolean() throws LinkedDataException {
        MemoryNode modsSection = new MemoryNode(MODS_MODS)
                .add(
                    new MemoryNode(MODS_CLASSIFICATION).put(MODS_AUTHORITY, new MemoryLiteral("GDZ", RDF.PLAIN_LITERAL))
                            .add(new MemoryLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)))
                .add(new MemoryNode(MODS_RECORD_INFO).add(new MemoryNode(MODS_RECORD_IDENTIFIER)
                        .put(MODS_SOURCE, new MemoryLiteral("gbv-ppn", RDF.PLAIN_LITERAL))
                        .add(new MemoryLiteral("PPN313539384", RDF.PLAIN_LITERAL))))
                .add(new MemoryNode(MODS_IDENTIFIER).put(MODS_TYPE, new MemoryLiteral("PPNanalog", RDF.PLAIN_LITERAL))
                        .add(new MemoryLiteral("PPN313539383", RDF.PLAIN_LITERAL)))
                .add(new MemoryNode(MODS_TITLE_INFO).add(new MemoryNode(MODS_TITLE).add(new MemoryLiteral(
                        "Sever. Pinaeus de virginitatis notis, graviditate et partu. Ludov. Bonaciolus de conformatione foetus. Accedeunt a"
                                .concat("lia"),
                        RDF.PLAIN_LITERAL))))
                .add(new MemoryNode(MODS_LANGUAGE).add(new MemoryNode(MODS_LANGUAGE_TERM)
                        .put(MODS_AUTHORITY, new MemoryLiteral("iso639-2b", RDF.PLAIN_LITERAL))
                        .put(MODS_TYPE, new MemoryLiteral("code", RDF.PLAIN_LITERAL))
                        .add(new MemoryLiteral("la", RDF.PLAIN_LITERAL))))
                .add(new MemoryNode(MODS_PLACE).add(
                    new MemoryNode(MODS_PLACE_TERM).put(MODS_TYPE, new MemoryLiteral("text", RDF.PLAIN_LITERAL))
                            .add(new MemoryLiteral("Lugduni Batavorum", RDF.PLAIN_LITERAL))))
                .add(new MemoryNode(MODS_DATE_ISSUED).put(MODS_ENCODING, new MemoryLiteral("w3cdtf", RDF.PLAIN_LITERAL))
                        .add(new MemoryLiteral("1641", RDF.PLAIN_LITERAL)))
                .add(new MemoryNode(MODS_PUBLISHER)
                        .add(new MemoryLiteral("Heger", RDF.PLAIN_LITERAL)))
                .add(
                    new MemoryNode(
                            MODS_NAME)
                                    .put(MODS_TYPE, new MemoryLiteral("personal",
                                            RDF.PLAIN_LITERAL))
                                    .add(new MemoryNode(MODS_ROLE)
                                            .add(new MemoryNode(MODS_ROLE_TERM)
                                                    .put(MODS_AUTHORITY,
                                                        new MemoryLiteral("marcrelator", RDF.PLAIN_LITERAL))
                                                    .put(MODS_TYPE, new MemoryLiteral("code", RDF.PLAIN_LITERAL))
                                                    .add(new MemoryLiteral("aut", RDF.PLAIN_LITERAL)))
                                            .add(new MemoryNode(MODS_NAME_PART)
                                                    .put(MODS_TYPE, new MemoryLiteral("family", RDF.PLAIN_LITERAL))
                                                    .add(new MemoryLiteral("Pineau", RDF.PLAIN_LITERAL)))
                                            .add(new MemoryNode(MODS_NAME_PART)
                                                    .put(MODS_TYPE, new MemoryLiteral("given", RDF.PLAIN_LITERAL))
                                                    .add(new MemoryLiteral("Severin", RDF.PLAIN_LITERAL)))
                                            .add(new MemoryNode(MODS_DISPLAY_FORM)
                                                    .add(new MemoryLiteral("Pineau, Severin", RDF.PLAIN_LITERAL)))))
                .add(new MemoryNode(MODS_PHYSICAL_DESCRIPTION)
                        .add(new MemoryNode(MODS_EXTENT).add(new MemoryLiteral("getr. Zählung", RDF.PLAIN_LITERAL))));

        Model m = ModelFactory.createDefaultModel();
        modsSection.toRDFNode(m, true);
        assertThat(new MemoryResult(m, false).node(), is(equalTo(modsSection)));
    }

    /**
     * Tests {@code MemoryResult(ObjectType)} constructor.
     */
    @Test
    public void testMemoryResultObjectType() {
        MemoryResult expected = new MemoryResult();
        expected.add(new MemoryNode(METS_METS_HDR));

        MemoryResult r = new MemoryResult(new MemoryNode(METS_METS_HDR));

        assertThat(r.size(), is(equalTo(expected.size())));
        assertThat(r, is(equalTo(expected)));
    }

    /**
     * Tests {@code subset(Class<T>)}.
     */
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

    /**
     * Tests {@code strings()}.
     */
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

    /**
     * Tests {@code strings(String)}.
     */
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
