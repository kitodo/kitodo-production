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

import java.util.*;

import org.junit.Test;
import org.kitodo.production.lugh.*;

public class GraphPathTest {

    @Test(expected = NullPointerException.class)
    public void testGraphPathCannotBeCreatedFromNull() {
        new GraphPath(null, null);
    }

    @Test
    public void testGraphPathCanBeCreatedFromAComplexString() {
        String gpath = "* [rdf:type mets:dmdSec] * [rdf:type mets:mdWrap] * [rdf:type mets:xmlData] * [rdf:type mods:mods] * [rdf:type mods:name, mods:type personal, * [rdf:type mods:role, * [rdf:type mods:roleTerm, rdf:_1 aut]]] * [rdf:type mods:displayForm] rdf:_1";

        Map<String,String>prefixes = new HashMap<>();
        prefixes.put("mets","http://www.loc.gov/METS/");
        prefixes.put("mods","http://www.loc.gov/mods/v3");
        prefixes.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns");

        GraphPath expected = (GraphPath) new GraphPath()
            .put(GraphPath.TO, new Node(GraphPath.LOCATION_STEP)
                .put(RDF.OBJECT, new Node(Mets.DMD_SEC))
                .put(GraphPath.TO, new Node(GraphPath.LOCATION_STEP)
                    .put(RDF.OBJECT, new Node(Mets.MD_WRAP))
                    .put(GraphPath.TO, new Node(GraphPath.LOCATION_STEP)
                        .put(RDF.OBJECT, new Node(Mets.XML_DATA))
                        .put(GraphPath.TO, new Node(GraphPath.LOCATION_STEP)
                            .put(RDF.OBJECT, new Node(Mods.MODS))
                            .put(GraphPath.TO, new Node(GraphPath.LOCATION_STEP)
                                .put(RDF.OBJECT, new Node(Mods.NAME)
                                    .put(Mods.TYPE, "personal")
                                    .put(GraphPath.ANY_PREDICATE, new Node(Mods.ROLE)
                                        .put(GraphPath.ANY_PREDICATE, new Node(Mods.ROLE_TERM)
                                            .add(new Literal("aut", RDF.PLAIN_LITERAL))
                                        )
                                    )
                                )
                                .put(GraphPath.TO, new Node(GraphPath.LOCATION_STEP)
                                    .put(RDF.OBJECT, new Node(Mods.DISPLAY_FORM))
                                    .put(GraphPath.TO, new Node(GraphPath.LOCATION_STEP)
                                        .put(RDF.PREDICATE, RDF.toURL(Node.FIRST_INDEX))
                                    )
                                )
                            )
                        )
                    )
                )
            )
        ;

        assertTrue(expected.equals(new GraphPath(gpath, prefixes)));
    }

    @Test
    public void testApplyingAGraphPathToANode() {

        String gpath = "* [rdf:type mods:name, mods:type personal, * [rdf:type mods:role, * [rdf:type mods:roleTerm, rdf:_1 aut]]] * [rdf:type mods:displayForm] rdf:_1";

        Map<String,String>prefixes = new HashMap<>();
        prefixes.put("mods","http://www.loc.gov/mods/v3");
        prefixes.put("rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns");

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
            .add(new Node(Mods.PHYSICAL_DESCRIPTION)
                .add(new Node(Mods.EXTENT)
                    .add(new Literal("getr. Zählung", RDF.PLAIN_LITERAL))
                )
            )
        ;

        Result result = modsSection.find(new GraphPath(gpath, prefixes));
        assertEquals("Pineau, Severin", result.strings(" ; ", true));
    }
}
