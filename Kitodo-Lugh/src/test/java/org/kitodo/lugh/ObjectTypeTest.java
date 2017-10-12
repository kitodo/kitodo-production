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
import static org.junit.Assert.assertNotNull;

import org.apache.jena.rdf.model.*;
import org.junit.Test;

public class ObjectTypeTest {

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

    /** Tests {@code ObjectType.toRDFNode(Model, Boolean)}. */
    @Test
    public void testToModel() throws LinkedDataException {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node modsSection = storage.createNode(Mods.MODS)
                    .add(storage.createNode(Mods.CLASSIFICATION)
                            .put(Mods.AUTHORITY, storage.createLiteral("GDZ", RDF.PLAIN_LITERAL))
                            .add(storage.createLiteral("Zeutschel Digital", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(Mods.RECORD_INFO)
                            .add(storage.createNode(Mods.RECORD_IDENTIFIER)
                                    .put(Mods.SOURCE, storage.createLiteral("gbv-ppn", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("PPN313539384", RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(Mods.IDENTIFIER)
                            .put(Mods.TYPE, storage.createLiteral("PPNanalog", RDF.PLAIN_LITERAL))
                            .add(storage.createLiteral("PPN313539383", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(Mods.TITLE_INFO)
                            .add(storage.createNode(Mods.TITLE).add(storage.createLiteral(
                                    "Sever. Pinaeus de virginitatis notis, graviditate et partu. Ludov. Bonaciolus de conformatione foetus. Accedeunt alia",
                                    RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(Mods.LANGUAGE)
                            .add(storage.createNode(Mods.LANGUAGE_TERM)
                                    .put(Mods.AUTHORITY, storage.createLiteral("iso639-2b", RDF.PLAIN_LITERAL))
                                    .put(Mods.TYPE, storage.createLiteral("code", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("la", RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(Mods.PLACE)
                            .add(storage.createNode(Mods.PLACE_TERM)
                                    .put(Mods.TYPE, storage.createLiteral("text", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("Lugduni Batavorum", RDF.PLAIN_LITERAL))))
                    .add(storage.createNode(Mods.DATE_ISSUED)
                            .put(Mods.ENCODING, storage.createLiteral("w3cdtf", RDF.PLAIN_LITERAL))
                            .add(storage.createLiteral("1641", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(Mods.PUBLISHER).add(storage.createLiteral("Heger", RDF.PLAIN_LITERAL)))
                    .add(storage.createNode(Mods.NAME)
                            .put(Mods.TYPE, storage.createLiteral("personal", RDF.PLAIN_LITERAL))
                            .add(storage.createNode(Mods.ROLE).add(storage.createNode(Mods.ROLE_TERM)
                                    .put(Mods.AUTHORITY, storage.createLiteral("marcrelator", RDF.PLAIN_LITERAL))
                                    .put(Mods.TYPE, storage.createLiteral("code", RDF.PLAIN_LITERAL))
                                    .add(storage.createLiteral("aut", RDF.PLAIN_LITERAL)))
                                    .add(storage.createNode(Mods.NAME_PART)
                                            .put(Mods.TYPE, storage.createLiteral("family", RDF.PLAIN_LITERAL))
                                            .add(storage.createLiteral("Pineau", RDF.PLAIN_LITERAL)))
                                    .add(storage.createNode(Mods.NAME_PART)
                                            .put(Mods.TYPE, storage.createLiteral("given", RDF.PLAIN_LITERAL))
                                            .add(storage.createLiteral("Severin", RDF.PLAIN_LITERAL)))
                                    .add(storage.createNode(Mods.DISPLAY_FORM)
                                            .add(storage.createLiteral("Pineau, Severin", RDF.PLAIN_LITERAL)))))
                    .add(storage.createNode(Mods.PHYSICAL_DESCRIPTION).add(storage.createNode(Mods.EXTENT)
                            .add(storage.createLiteral("getr. Zählung", RDF.PLAIN_LITERAL))));

            Model m = ModelFactory.createDefaultModel();
            modsSection.toRDFNode(m, true);
            assertThat(new MemoryResult(m, false).node(), is(equalTo(modsSection)));
        }
    }

}
