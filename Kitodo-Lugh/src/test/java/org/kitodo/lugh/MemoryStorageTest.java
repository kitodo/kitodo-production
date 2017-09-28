package org.kitodo.lugh;

import static org.junit.Assert.assertEquals;

import org.apache.jena.rdf.model.*;
import org.junit.Test;
import org.kitodo.lugh.vocabulary.*;

public class MemoryStorageTest {
    @Test
    public void testToModel() throws LinkedDataException {
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
        assertEquals(modsSection, MemoryStorage.INSTANCE.createResult(m, false).node());
    }
}
