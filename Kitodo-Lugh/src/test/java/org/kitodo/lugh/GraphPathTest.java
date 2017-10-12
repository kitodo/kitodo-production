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

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

public class GraphPathTest {

    @Test
    public void testApplyingAGraphPathToANode() {

        String gpath = "* [rdf:type mods:name, mods:type personal, * [rdf:type mods:role, * [rdf:type mods:roleTerm, rdf:_1 aut]]] * [rdf:type mods:displayForm] rdf:_1";

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("mods", "http://www.loc.gov/mods/v3");
        prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");

        Node modsSection = new MemoryNode(Mods.MODS)
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
                .add(new MemoryNode(Mods.PUBLISHER)
                        .add(new MemoryLiteral("Heger", RDF.PLAIN_LITERAL)))
                .add(new MemoryNode(Mods.NAME)
                        .put(Mods.TYPE,
                                new MemoryLiteral("personal", RDF.PLAIN_LITERAL))
                        .add(new MemoryNode(Mods.ROLE).add(new MemoryNode(Mods.ROLE_TERM)
                                .put(Mods.AUTHORITY, new MemoryLiteral("marcrelator", RDF.PLAIN_LITERAL))
                                .put(Mods.TYPE, new MemoryLiteral("code", RDF.PLAIN_LITERAL))
                                .add(new MemoryLiteral("aut", RDF.PLAIN_LITERAL))))
                        .add(new MemoryNode(Mods.NAME_PART)
                                .put(Mods.TYPE, new MemoryLiteral("family", RDF.PLAIN_LITERAL))
                                .add(new MemoryLiteral("Pineau", RDF.PLAIN_LITERAL)))
                        .add(new MemoryNode(Mods.NAME_PART)
                                .put(Mods.TYPE, new MemoryLiteral("given", RDF.PLAIN_LITERAL))
                                .add(new MemoryLiteral("Severin", RDF.PLAIN_LITERAL)))
                        .add(new MemoryNode(Mods.DISPLAY_FORM)
                                .add(new MemoryLiteral("Pineau, Severin", RDF.PLAIN_LITERAL))))
                .add(new MemoryNode(Mods.PHYSICAL_DESCRIPTION)
                        .add(new MemoryNode(Mods.EXTENT).add(new MemoryLiteral("getr. Zählung", RDF.PLAIN_LITERAL))));

        Result result = modsSection.find(new GraphPath(gpath, prefixes));
        assertEquals("Pineau, Severin", result.leaves(" ; "));
    }

    @Test
    public void testGraphPathCanBeCreatedFromAComplexString() {
        String gpath = "* [rdf:type mets:dmdSec] * [rdf:type mets:mdWrap] * [rdf:type mets:xmlData] * [rdf:type mods:mods] * [rdf:type mods:name, mods:type personal, * [rdf:type mods:role, * [rdf:type mods:roleTerm, rdf:_1 aut]]] * [rdf:type mods:displayForm] rdf:_1";

        Map<String, String> prefixes = new HashMap<>();
        prefixes.put("mets", "http://www.loc.gov/METS/");
        prefixes.put("mods", "http://www.loc.gov/mods/v3");
        prefixes.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");

        GraphPath expected = (GraphPath) new GraphPath().put(GraphPath.TO, new MemoryNode(GraphPath.LOCATION_STEP)
                .put(RDF.OBJECT, new MemoryNode(Mets.DMD_SEC))
                .put(GraphPath.TO, new MemoryNode(GraphPath.LOCATION_STEP).put(RDF.OBJECT, new MemoryNode(Mets.MD_WRAP))
                        .put(GraphPath.TO, new MemoryNode(GraphPath.LOCATION_STEP)
                                .put(RDF.OBJECT, new MemoryNode(Mets.XML_DATA))
                                .put(GraphPath.TO, new MemoryNode(GraphPath.LOCATION_STEP)
                                        .put(RDF.OBJECT, new MemoryNode(Mods.MODS))
                                        .put(GraphPath.TO, new MemoryNode(GraphPath.LOCATION_STEP)
                                                .put(RDF.OBJECT, new MemoryNode(Mods.NAME).put(Mods.TYPE, "personal")
                                                        .put(GraphPath.ANY_PREDICATE, new MemoryNode(Mods.ROLE).put(
                                                                GraphPath.ANY_PREDICATE,
                                                                new MemoryNode(Mods.ROLE_TERM).add(
                                                                        new MemoryLiteral("aut", RDF.PLAIN_LITERAL)))))
                                                .put(GraphPath.TO, new MemoryNode(GraphPath.LOCATION_STEP)
                                                        .put(RDF.OBJECT, new MemoryNode(Mods.DISPLAY_FORM))
                                                        .put(GraphPath.TO, new MemoryNode(GraphPath.LOCATION_STEP)
                                                                .put(RDF.PREDICATE, RDF.toURL(Node.FIRST_INDEX)))))))));

        assertTrue(expected.equals(new GraphPath(gpath, prefixes)));

        new MemoryNode(Mods.ROLE).put(GraphPath.ANY_PREDICATE,
                new MemoryNode(Mods.ROLE_TERM).add(new MemoryLiteral("aut", RDF.PLAIN_LITERAL)));
    }

    @Test(expected = NullPointerException.class)
    public void testGraphPathCannotBeCreatedFromNull() {
        new GraphPath(null, null);
    }
}
