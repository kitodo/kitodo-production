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
package org.goobi.production.constants;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.Test;

public class ParametersTest {

    @Test
    public void testGetAuthorityDataURLTailWithPrefixConfigured() {
        Map<String, String> config = new HashMap<>();
        config.put("authority.gnd.dataUrlTail", "/about/lds.rdf");
        config.put("namespace.gnd", "http://d-nb.info/gnd/");
        config.put("other", "will be ignored");
        assertEquals("/about/lds.rdf", new Parameters(config).getAuthorityDataURLTail("http://d-nb.info/gnd/"));
    }

    @Test
    public void testGetAuthorityDataURLTailWithNoPrefixConfigured() {
        Map<String, String> config = new HashMap<>();
        config.put("authority.http://d-nb.info/gnd/.dataUrlTail", "/about/lds.rdf");
        config.put("other", "will be ignored");
        assertEquals("/about/lds.rdf", new Parameters(config).getAuthorityDataURLTail("http://d-nb.info/gnd/"));
    }

    @Test
    public void testGetAuthorityMapping() {
        Map<String, String> config = new HashMap<>();
        config.put("authorityMapping.ctrFirstName", "gndo:preferredNameEntityForThePerson gndo:forename");
        config.put("authorityMapping.ctrLastName", "gndo:preferredNameEntityForThePerson gndo:surname");
        config.put("authorityMapping.ctrDisplayForm", "gndo:preferredNameForThePerson");
        config.put("other", "will be ignored");

        Map<String, String> expected = new HashMap<>();
        expected.put("ctrFirstName", "gndo:preferredNameEntityForThePerson gndo:forename");
        expected.put("ctrLastName", "gndo:preferredNameEntityForThePerson gndo:surname");
        expected.put("ctrDisplayForm", "gndo:preferredNameForThePerson");

        assertEquals(expected, new Parameters(config).getAuthorityMapping());
    }

    @Test
    public void testGetNamespacePrefixesResolvingWithHash() {
        Map<String, String> config = new HashMap<>();
        config.put("namespace.dcterms", "http://purl.org/dc/terms/");
        config.put("namespace.foaf", "http://xmlns.com/foaf/0.1/");
        config.put("namespace.mets", "http://www.loc.gov/METS/");
        config.put("namespace.mods", "http://www.loc.gov/mods/v3#");
        config.put("namespace.rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        config.put("namespace.rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        config.put("namespace.xsd", "http://www.w3.org/2001/XMLSchema");
        config.put("other", "will be ignored");

        Map<String, String> expected = new HashMap<>();
        expected.put("dcterms", "http://purl.org/dc/terms/");
        expected.put("foaf", "http://xmlns.com/foaf/0.1/");
        expected.put("mets", "http://www.loc.gov/METS/");
        expected.put("mods", "http://www.loc.gov/mods/v3#");
        expected.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        expected.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        expected.put("xsd", "http://www.w3.org/2001/XMLSchema#");

        assertEquals(expected, new Parameters(config).getNamespacePrefixes(true, true));
    }

    @Test
    public void testGetNamespacePrefixesResolvingWithoutHash() {
        Map<String, String> config = new HashMap<>();
        config.put("namespace.dcterms", "http://purl.org/dc/terms/");
        config.put("namespace.foaf", "http://xmlns.com/foaf/0.1/");
        config.put("namespace.mets", "http://www.loc.gov/METS/");
        config.put("namespace.mods", "http://www.loc.gov/mods/v3#");
        config.put("namespace.rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        config.put("namespace.rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        config.put("namespace.xsd", "http://www.w3.org/2001/XMLSchema");
        config.put("other", "will be ignored");

        Map<String, String> expected = new HashMap<>();
        expected.put("dcterms", "http://purl.org/dc/terms/");
        expected.put("foaf", "http://xmlns.com/foaf/0.1/");
        expected.put("mets", "http://www.loc.gov/METS/");
        expected.put("mods", "http://www.loc.gov/mods/v3");
        expected.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        expected.put("rdfs", "http://www.w3.org/2000/01/rdf-schema");
        expected.put("xsd", "http://www.w3.org/2001/XMLSchema");

        assertEquals(expected, new Parameters(config).getNamespacePrefixes(true, false));
    }

    @Test
    public void testGetNamespacePrefixesForLookUpWithHash() {
        Map<String, String> config = new HashMap<>();
        config.put("namespace.dcterms", "http://purl.org/dc/terms/");
        config.put("namespace.foaf", "http://xmlns.com/foaf/0.1/");
        config.put("namespace.mets", "http://www.loc.gov/METS/");
        config.put("namespace.mods", "http://www.loc.gov/mods/v3#");
        config.put("namespace.rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        config.put("namespace.rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        config.put("namespace.xsd", "http://www.w3.org/2001/XMLSchema");
        config.put("other", "will be ignored");

        Map<String, String> expected = new HashMap<>();
        expected.put("http://purl.org/dc/terms/", "dcterms");
        expected.put("http://xmlns.com/foaf/0.1/", "foaf");
        expected.put("http://www.loc.gov/METS/", "mets");
        expected.put("http://www.loc.gov/mods/v3#", "mods");
        expected.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
        expected.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
        expected.put("http://www.w3.org/2001/XMLSchema#", "xsd");

        assertEquals(expected, new Parameters(config).getNamespacePrefixes(false, true));
    }

    @Test
    public void testGetNamespacePrefixesForLookUpWithoutHash() {
        Map<String, String> config = new HashMap<>();
        config.put("namespace.dcterms", "http://purl.org/dc/terms/");
        config.put("namespace.foaf", "http://xmlns.com/foaf/0.1/");
        config.put("namespace.mets", "http://www.loc.gov/METS/");
        config.put("namespace.mods", "http://www.loc.gov/mods/v3#");
        config.put("namespace.rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns");
        config.put("namespace.rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        config.put("namespace.xsd", "http://www.w3.org/2001/XMLSchema");
        config.put("other", "will be ignored");

        Map<String, String> expected = new HashMap<>();
        expected.put("http://purl.org/dc/terms/", "dcterms");
        expected.put("http://xmlns.com/foaf/0.1/", "foaf");
        expected.put("http://www.loc.gov/METS/", "mets");
        expected.put("http://www.loc.gov/mods/v3", "mods");
        expected.put("http://www.w3.org/1999/02/22-rdf-syntax-ns", "rdf");
        expected.put("http://www.w3.org/2000/01/rdf-schema", "rdfs");
        expected.put("http://www.w3.org/2001/XMLSchema", "xsd");

        assertEquals(expected, new Parameters(config).getNamespacePrefixes(false, false));
    }

}
