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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.Result;

public class GraphPathTest {

    private static final MemoryNodeReference METS_DMD_SEC = new MemoryNodeReference("http://www.loc.gov/METS/dmdSec");
    private static final MemoryNodeReference METS_MD_WRAP = new MemoryNodeReference("http://www.loc.gov/METS/mdWrap");
    private static final MemoryNodeReference METS_XML_DATA = new MemoryNodeReference("http://www.loc.gov/METS/xmlData");
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

    @Test
    public void testApplyingAGraphPathToANode() {

        String gpath = "* [rdf:type mods:name, mods:type personal, * [rdf:type mods:role, * [rdf:type mods:roleTerm, rdf:_1 aut]]] * [rdf:t"
                .concat("ype mods:displayForm] rdf:_1");

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("mods", "http://www.loc.gov/mods/v3");
        prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");

        Node modsSection = new MemoryNode(MODS_MODS)
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
                .add(new MemoryNode(
                        MODS_PUBLISHER).add(
                            new MemoryLiteral("Heger", RDF.PLAIN_LITERAL)))
                .add(
                    new MemoryNode(MODS_NAME)
                            .put(MODS_TYPE, new MemoryLiteral("personal",
                                    RDF.PLAIN_LITERAL))
                            .add(new MemoryNode(MODS_ROLE).add(new MemoryNode(MODS_ROLE_TERM)
                                    .put(MODS_AUTHORITY, new MemoryLiteral("marcrelator", RDF.PLAIN_LITERAL))
                                    .put(MODS_TYPE, new MemoryLiteral("code", RDF.PLAIN_LITERAL))
                                    .add(new MemoryLiteral("aut", RDF.PLAIN_LITERAL))))
                            .add(new MemoryNode(MODS_NAME_PART)
                                    .put(MODS_TYPE, new MemoryLiteral("family", RDF.PLAIN_LITERAL))
                                    .add(new MemoryLiteral("Pineau", RDF.PLAIN_LITERAL)))
                            .add(new MemoryNode(MODS_NAME_PART)
                                    .put(MODS_TYPE, new MemoryLiteral("given", RDF.PLAIN_LITERAL))
                                    .add(new MemoryLiteral("Severin", RDF.PLAIN_LITERAL)))
                            .add(new MemoryNode(MODS_DISPLAY_FORM)
                                    .add(new MemoryLiteral("Pineau, Severin", RDF.PLAIN_LITERAL))))
                .add(new MemoryNode(MODS_PHYSICAL_DESCRIPTION)
                        .add(new MemoryNode(MODS_EXTENT).add(new MemoryLiteral("getr. Zählung", RDF.PLAIN_LITERAL))));

        Result result = modsSection.find(new GraphPath(gpath, prefixes));
        assertThat(result.leaves(" ; "), is(equalTo("Pineau, Severin")));
    }

    @Test
    public void testGraphPathCanBeCreatedFromAComplexString() {
        String gpath = "* [rdf:type mets:dmdSec] * [rdf:type mets:mdWrap] * [rdf:type mets:xmlData] * [rdf:type mods:mods] * [rdf:type mods"
                + ":name, mods:type personal, * [rdf:type mods:role, * [rdf:type mods:roleTerm, rdf:_1 aut]]] * [rdf:type mods:displayForm]"
                + " rdf:_1";

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("mets", "http://www.loc.gov/METS/");
        prefixes.put("mods", "http://www.loc.gov/mods/v3");
        prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");

        GraphPath graphPath = new GraphPath(gpath, prefixes);

        GraphPath expected = (GraphPath) new GraphPath().put(GraphPath.TO,
            new MemoryNode(GraphPath.LOCATION_STEP).put(RDF.OBJECT, new MemoryNode(METS_DMD_SEC)).put(GraphPath.TO,
                new MemoryNode(GraphPath.LOCATION_STEP).put(RDF.OBJECT, new MemoryNode(METS_MD_WRAP)).put(GraphPath.TO,
                    new MemoryNode(GraphPath.LOCATION_STEP).put(RDF.OBJECT, new MemoryNode(METS_XML_DATA))
                            .put(GraphPath.TO,
                                new MemoryNode(GraphPath.LOCATION_STEP).put(RDF.OBJECT, new MemoryNode(MODS_MODS))
                                        .put(GraphPath.TO, new MemoryNode(GraphPath.LOCATION_STEP)
                                                .put(RDF.OBJECT,
                                                    new MemoryNode(MODS_NAME).put(MODS_TYPE, "personal")
                                                            .put(GraphPath.ANY_PREDICATE,
                                                                new MemoryNode(MODS_ROLE).put(GraphPath.ANY_PREDICATE,
                                                                    new MemoryNode(MODS_ROLE_TERM).add(
                                                                        new MemoryLiteral("aut", RDF.PLAIN_LITERAL)))))
                                                .put(GraphPath.TO, new MemoryNode(GraphPath.LOCATION_STEP)
                                                        .put(RDF.OBJECT, new MemoryNode(MODS_DISPLAY_FORM))
                                                        .put(GraphPath.TO, new MemoryNode(GraphPath.LOCATION_STEP)
                                                                .put(RDF.PREDICATE, RDF.toURL(Node.FIRST_INDEX)))))))));

        assertThat(graphPath, is(equalTo(expected)));

        new MemoryNode(MODS_ROLE).put(GraphPath.ANY_PREDICATE,
            new MemoryNode(MODS_ROLE_TERM).add(new MemoryLiteral("aut", RDF.PLAIN_LITERAL)));
    }

    @Test(expected = NullPointerException.class)
    public void testGraphPathCannotBeCreatedFromNull() {
        new GraphPath(null, null);
    }
}
