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

package org.kitodo.dataaccess;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.kitodo.dataaccess.storage.memory.MemoryNodeReference;
import org.kitodo.dataaccess.storage.memory.MemoryResult;

public class ObjectTypeTest {

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
    public void testLangStringIsAnObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType ot = storage.createLangString("Hello world!", "en");
            assertThat(ot, is(notNullValue()));
        }
    }

    @Test
    public void testLiteralIsAnObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType ot = storage.createLiteral("abcdefghijklmnopqrstuvwxyz", RDF.PLAIN_LITERAL);
            assertThat(ot, is(notNullValue()));
        }
    }

    @Test
    public void testNamedNodeIsAnObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType ot = storage.createNamedNode("http://test.example/foo");
            assertThat(ot, is(notNullValue()));
        }
    }

    @Test
    public void testNodeIsAnObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType ot = storage.createNode();
            assertThat(ot, is(notNullValue()));
        }
    }

    @Test
    public void testNodeReferenceIsAnObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType ot = storage.createNodeReference("http://test.example/foo");
            assertThat(ot, is(notNullValue()));
        }
    }

    /**
     * Tests {@code ObjectType.toRDFNode(Model, Boolean)}.
     */
    @Test
    public void testToModel() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node modsSection = storage.createNode(MODS_MODS)
                    .add(storage.createNode(MODS_CLASSIFICATION)
                            .put(MODS_AUTHORITY, storage.createLiteral("GDZ", RDF.PLAIN_LITERAL))
                            .add(storage.createLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(MODS_RECORD_INFO)
                            .add(storage.createNode(MODS_RECORD_IDENTIFIER)
                                    .put(MODS_SOURCE, storage.createLiteral("gbv-ppn", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("PPN313539384", RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(MODS_IDENTIFIER)
                            .put(MODS_TYPE, storage.createLiteral("PPNanalog", RDF.PLAIN_LITERAL))
                            .add(storage.createLiteral("PPN313539383", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(MODS_TITLE_INFO).add(
                        storage.createNode(MODS_TITLE).add(storage.createLiteral(
                            "Sever. Pinaeus de virginitatis notis, graviditate et partu. Ludov. Bonaciolus de conformatione foetus."
                                    .concat(" Accedeunt alia"),
                            RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(MODS_LANGUAGE)
                            .add(storage.createNode(MODS_LANGUAGE_TERM)
                                    .put(MODS_AUTHORITY, storage.createLiteral("iso639-2b", RDF.PLAIN_LITERAL))
                                    .put(MODS_TYPE, storage.createLiteral("code", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("la", RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(MODS_PLACE)
                            .add(storage.createNode(MODS_PLACE_TERM)
                                    .put(MODS_TYPE, storage.createLiteral("text", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("Lugduni Batavorum", RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(MODS_DATE_ISSUED)
                            .put(MODS_ENCODING, storage.createLiteral("w3cdtf", RDF.PLAIN_LITERAL))
                            .add(storage.createLiteral("1641", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(MODS_PUBLISHER).add(storage.createLiteral("Heger", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(MODS_NAME)
                            .put(MODS_TYPE, storage.createLiteral("personal", RDF.PLAIN_LITERAL))
                            .add(storage.createNode(MODS_ROLE).add(storage.createNode(MODS_ROLE_TERM)
                                    .put(MODS_AUTHORITY, storage.createLiteral("marcrelator", RDF.PLAIN_LITERAL))
                                    .put(MODS_TYPE, storage.createLiteral("code", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("aut", RDF.PLAIN_LITERAL)))
                                    .add(storage.createNode(MODS_NAME_PART)
                                            .put(MODS_TYPE, storage.createLiteral("family", RDF.PLAIN_LITERAL))
                                            .add(storage.createLiteral("Pineau", RDF.PLAIN_LITERAL)))
                                    .add(storage.createNode(MODS_NAME_PART)
                                            .put(MODS_TYPE, storage.createLiteral("given", RDF.PLAIN_LITERAL))
                                            .add(storage.createLiteral("Severin", RDF.PLAIN_LITERAL)))
                                    .add(storage.createNode(MODS_DISPLAY_FORM)
                                            .add(storage.createLiteral("Pineau, Severin", RDF.PLAIN_LITERAL)))))
                    .add(storage.createNode(MODS_PHYSICAL_DESCRIPTION).add(storage.createNode(MODS_EXTENT)
                            .add(storage.createLiteral("getr. Zählung", RDF.PLAIN_LITERAL))));

            Model m = ModelFactory.createDefaultModel();
            modsSection.toRDFNode(m, true);
            assertThat(new MemoryResult(m, false).node(), is(equalTo(modsSection)));
        }
    }

}
