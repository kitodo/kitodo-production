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
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.kitodo.dataaccess.storage.memory.MemoryNodeReference;

/**
 * Tests {@code org.kitodo.dataaccess.Storage}.
 */
public class StorageTest {

    private static final MemoryNodeReference METS_METS = new MemoryNodeReference("http://www.loc.gov/METS/mets");
    private static final MemoryNodeReference MODS_MODS = new MemoryNodeReference("http://www.loc.gov/mods/v3#mods");

    /**
     * Tests {@code createLangString(String, String)}.
     */
    @Test
    public void testCreateLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString a = storage.createLangString("Lorem ipsum dolor sit amet", "la");
            assertThat(a, is(notNullValue()));
        }
    }

    /**
     * Tests {@code createLangString(String, String)}.
     */
    @Test
    public void testCreateLangStringWithEmptyLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createLangString("Lorem ipsum dolor sit amet", "");
                fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
            } catch (IllegalArgumentException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code createLangString(String, String)}.
     */
    @Test
    public void testCreateLangStringWithNullLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createLangString("Lorem ipsum dolor sit amet", null);
                fail(storage.getClass().getSimpleName() + " should throw NullPointerException, but does not.");
            } catch (NullPointerException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code createLangString(String, String)}.
     */
    @Test
    public void testCreateLangStringWithNullValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createLangString(null, "en");
                fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
            } catch (IllegalArgumentException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code createLiteral(String, IdentifiableNode)}.
     */
    @Test
    public void testCreateLiteralStringIdentifiableNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal l = storage.createLiteral("P9M", XMLSchema.DURATION);
            assertThat(l, is(notNullValue()));
        }
    }

    /**
     * Tests {@code createLiteral(String, String)}.
     */
    @Test
    public void testCreateLiteralStringString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal l = storage.createLiteral("P9M", XMLSchema.DURATION.getIdentifier());
            assertThat(l, is(notNullValue()));
        }
    }

    /**
     * Tests {@code createLiteral(String, String)}.
     */
    @Test
    public void testCreateLiteralStringStringWithNonLiteralType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createLiteral("Lorem ipsum dolor sit amet", METS_METS.getIdentifier());
                fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
            } catch (IllegalArgumentException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code createLiteral(String, String)}.
     */
    @Test
    public void testCreateLiteralStringStringWithNullType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal a = storage.createLiteral("1234567", (String) null);
            assertThat(a.getType(), is(equalTo(RDF.PLAIN_LITERAL.getIdentifier())));
        }
    }

    /**
     * Tests {@code createLiteral(String, String)}.
     */
    @Test
    public void testCreateLiteralStringStringWithNullValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createLiteral(null, XMLSchema.DURATION.getIdentifier());
                fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
            } catch (IllegalArgumentException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code createLiteralType(String, String)}.
     */
    @Test
    public void testCreateLiteralTypeWithHttpsUri() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType o = storage.createLeaf("https://w3id.org/i40/aml", "de");
            assertThat(o, is(instanceOf(NodeReference.class)));
        }
    }

    /**
     * Tests {@code createLiteralType(String, String)}.
     */
    @Test
    public void testCreateLiteralTypeWithHttpUriAndEmptyLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType o = storage.createLeaf(
                "http://uri4uri.net/uri/https%3A%2F%2Fwww.kitodo.org%2Fsoftware%2Fkitodoproduction%2F", "");
            assertThat(o, is(instanceOf(NodeReference.class)));
        }
    }

    /**
     * Tests {@code createLiteralType(String, String)}.
     */
    @Test
    public void testCreateLiteralTypeWithHttpUriAndLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType o = storage.createLeaf(
                "http://uri4uri.net/uri/https%3A%2F%2Fwww.kitodo.org%2Fsoftware%2Fkitodoproduction%2F", "de");
            assertThat(o, is(instanceOf(NodeReference.class)));
        }
    }

    /**
     * Tests {@code createLiteralType(String, String)}.
     */
    @Test
    public void testCreateLiteralTypeWithHttpUriAndNullLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType o = storage.createLeaf(
                "http://uri4uri.net/uri/https%3A%2F%2Fwww.kitodo.org%2Fsoftware%2Fkitodoproduction%2F", null);
            assertThat(o, is(instanceOf(NodeReference.class)));
        }
    }

    /**
     * Tests {@code createLiteralType(String, String)}.
     */
    @Test
    public void testCreateLiteralTypeWithStringAndEmptyLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType o = storage.createLeaf("javac.exe", "");
            assertThat(o, is(instanceOf(Literal.class)));
            assertThat(((Literal) o).getType(), is(equalTo(RDF.PLAIN_LITERAL.getIdentifier())));
        }
    }

    /**
     * Tests {@code createLiteralType(String, String)}.
     */
    @Test
    public void testCreateLiteralTypeWithStringAndLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType o = storage.createLeaf("Das Pferd frißt keinen Gurkensalat.", "de");
            assertThat(o, is(instanceOf(Literal.class)));
            assertThat(((Literal) o).getType(), is(equalTo(RDF.LANG_STRING.getIdentifier())));
            assertThat(((LangString) o).getLanguageTag(), is(equalTo("de")));
        }
    }

    /**
     * Tests {@code createLiteralType(String, String)}.
     */
    @Test
    public void testCreateLiteralTypeWithStringAndNullLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType o = storage.createLeaf("javac.exe", null);
            assertThat(o, is(instanceOf(Literal.class)));
            assertThat(((Literal) o).getType(), is(equalTo(RDF.PLAIN_LITERAL.getIdentifier())));
        }
    }

    /**
     * Tests {@code createNamedNode(String)}.
     */
    @Test
    public void testCreateNamedNodeWithEmptyID() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
                try {
                    storage.createNamedNode("");
                    fail(storage.getClass().getSimpleName() + " should throw AssertionError, but does not.");
                } catch (AssertionError e1) {
                    /* expected */
                }
            }
        }
    }

    /**
     * Tests {@code createNamedNode(String)}.
     */
    @Test
    public void testCreateNamedNodeWithHttpName() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node n = storage.createNamedNode("http://localhost/data/namedNode1");
            assertThat(n, is(instanceOf(Node.class)));
            assertThat(n, is(instanceOf(IdentifiableNode.class)));
            assertThat(((IdentifiableNode) n).getIdentifier(), is(equalTo("http://localhost/data/namedNode1")));
        }
    }

    /**
     * Tests {@code createNamedNode(String)}.
     */
    @Test
    public void testCreateNamedNodeWithHttpsName() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node n = storage.createNamedNode("https://localhost/data/namedNode1");
            assertThat(n, is(instanceOf(Node.class)));
            assertThat(n, is(instanceOf(IdentifiableNode.class)));
            assertThat(((IdentifiableNode) n).getIdentifier(), is(equalTo("https://localhost/data/namedNode1")));
        }
    }

    /**
     * Tests {@code createNamedNode(String)}.
     */
    @Test
    public void testCreateNamedNodeWithInvalidID() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                assert (false);
                fail("Assertions disabled. Run JVM with -ea option.");
            } catch (AssertionError e) {
                try {
                    storage.createNamedNode("Das Pferd frißt keinen Gurkensalat.");
                    fail(storage.getClass().getSimpleName() + " should throw AssertionError, but does not.");
                } catch (AssertionError e1) {
                    /* expected */
                }
            }
        }
    }

    /**
     * Tests {@code createNamedNode(String)}.
     */
    @Test
    public void testCreateNamedNodeWithNullID() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createNamedNode(null);
                fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
            } catch (IllegalArgumentException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code createNamedNode(String)}.
     */
    @Test
    public void testCreateNamedNodeWithUrnName() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node n = storage.createNamedNode("urn:nbn:de:1234567-76543210");
            assertThat(n, is(instanceOf(Node.class)));
            assertThat(n, is(instanceOf(IdentifiableNode.class)));
            assertThat(((IdentifiableNode) n).getIdentifier(), is(equalTo("urn:nbn:de:1234567-76543210")));
        }
    }

    /**
     * Tests {@code createNodeReference(String)}.
     */
    @Test
    public void testCreateNodeReferenceWithEmptyString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createNodeReference("");
                fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
            } catch (IllegalArgumentException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code createNodeReference(String)}.
     */
    @Test
    public void testCreateNodeReferenceWithHttpsUrl() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeReference r = storage.createNodeReference("urn:nbn:de:1234567-76543210");
            assertThat(r.getIdentifier(), is(equalTo("urn:nbn:de:1234567-76543210")));
        }
    }

    /**
     * Tests {@code createNodeReference(String)}.
     */
    @Test
    public void testCreateNodeReferenceWithHttpUrl() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeReference r = storage.createNodeReference("http://www.kitodo.org/");
            assertThat(r.getIdentifier(), is(equalTo("http://www.kitodo.org/")));
        }
    }

    /**
     * Tests {@code createNodeReference(String)}.
     */
    @Test
    public void testCreateNodeReferenceWithNonsense() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
                try {
                    storage.createNodeReference("Oh Tannenbaum…");
                    fail(storage.getClass().getSimpleName() + " should throw AssertionError, but does not.");
                } catch (AssertionError e1) {
                    /* expected */
                }
            }
        }
    }

    /**
     * Tests {@code createNodeReference(String)}.
     */
    @Test
    public void testCreateNodeReferenceWithNullReference() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createNodeReference(null);
                fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
            } catch (IllegalArgumentException e) {
                /* expected */
            }
        }
    }

    /**
     * Tests {@code createNodeReference(String)}.
     */
    @Test
    public void testCreateNodeReferenceWithUrn() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeReference r = storage.createNodeReference("https://www.kitodo.org/");
            assertThat(r.getIdentifier(), is(equalTo("https://www.kitodo.org/")));
        }
    }

    /**
     * Tests {@code createNode()}.
     */
    @Test
    public void testNodeCanBeCreatedEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node n = storage.createNode();
            assertThat(n, is(notNullValue()));
        }
    }

    /**
     * Tests {@code createNode(NodeReference)}.
     */
    @Test
    public void testNodeCanBeCreatedWithNodeReferenceAsType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node mets = storage.createNode(METS_METS);

            assertThat(mets.getType(), is(equalTo(METS_METS.getIdentifier())));
        }
    }

    /**
     * Tests {@code createNode(String)}.
     */
    @Test
    public void testNodeCanBeCreatedWithStringAsType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            final String MODS = MODS_MODS.getIdentifier();
            Node mods = storage.createNode(MODS);

            assertThat(mods.getType(), is(equalTo(MODS)));
        }
    }

    /**
     * Tests {@code createNode(String)} with empty string.
     */
    @Test
    public void testNodeCreatedWithEmptyStringAsTypeIsEmpty() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
                try {
                    storage.createNode("");
                    fail(storage.getClass().getSimpleName() + " should throw AssertionError, but does not.");
                } catch (AssertionError e1) {
                    /* expected */
                }
            }
        }
    }

    /**
     * Tests {@code createNode(NodeReference)} with null node reference. The
     * created node must not contain a rdf:type relation.
     */
    @Test
    public void testNodeCreatedWithUnitializedNodeReferenceIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node a = storage.createNode((NodeReference) null);
            assertThat(a.isEmpty(), is(true));
        }
    }

    /**
     * Tests {@code createNode(String)} with null string. The created node must
     * not contain a rdf:type relation.
     */
    @Test
    public void testNodeCreatedWithUnitializedStringIsEmpty() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Node a = storage.createNode((String) null);
            assertThat(a.isEmpty(), is(true));
        }
    }
}
