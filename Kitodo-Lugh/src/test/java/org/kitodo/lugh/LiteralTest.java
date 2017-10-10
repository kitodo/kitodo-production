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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.kitodo.lugh.vocabulary.*;

public class LiteralTest {

    @Test
    public void testCreateCreatesLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLangString("Hoc est corpus meum.", "la"),
                    storage.createLiteralType("Hoc est corpus meum.", "la"));
        }
    }

    @Test
    public void testCreateCreatesNodeReference1() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createNodeReference("http://www.kitodo.org/"),
                    storage.createLiteralType("http://www.kitodo.org/", null));
        }
    }

    @Test
    public void testCreateCreatesNodeReference2() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createNodeReference("http://www.kitodo.org/"),
                    storage.createLiteralType("http://www.kitodo.org/", ""));
        }
    }

    @Test
    public void testCreateCreatesPlainLiteral1() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                    storage.createLiteralType("public static void main(String[] args)", null));
        }
    }

    @Test
    public void testCreateCreatesPlainLiteral2() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                    storage.createLiteralType("public static void main(String[] args)", ""));
        }
    }

    @Test
    public void testCreateLiteralCreatesLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLangString("Hoc est corpus meum.", "la"),
                    storage.createLiteralType("Hoc est corpus meum.", "la"));
        }
    }

    @Test
    public void testCreateLiteralCreatesPlainLiteral1() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLiteral("http://www.kitodo.org/", RDF.PLAIN_LITERAL),
                    storage.createLiteral("http://www.kitodo.org/", (NodeReference) null));
        }
    }

    @Test
    public void testCreateLiteralCreatesPlainLiteral2() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLiteral("http://www.kitodo.org/", RDF.PLAIN_LITERAL),
                    storage.createLiteral("http://www.kitodo.org/", ""));
        }
    }

    @Test
    public void testEqualsObjectForDifferentContent() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral(
                    "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>",
                    RDF.XML_LITERAL);
            Literal other = storage.createLiteral(
                    "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">edt</mods:roleTerm></mods:role>",
                    RDF.XML_LITERAL);

            assertFalse(one.equals(other));
        }
    }

    @Test
    public void testEqualsObjectForDifferentTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#string");
            Literal other = storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#integer");

            assertFalse(one.equals(other));
        }
    }

    @Test
    public void testEqualsObjectForTwoEqualLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal other = storage.createLiteral("Lorem ipsum dolor sit amet", "");

            assertTrue(one.equals(other));
        }
    }

    @Test
    public void testEqualsObjectForTwoEqualLiteralsWithAndWithoutStringDeclared() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal other = storage.createLiteral("Lorem ipsum dolor sit amet", XMLSchema.STRING);

            assertTrue(one.equals(other));
        }
    }

    @Test
    public void testGetType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(RDF.PLAIN_LITERAL.getIdentifier(),
                    storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL).getType());
        }
    }

    @Test
    public void testGetValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals("Lorem ipsum dolor sit amet",
                    storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL).getValue());
        }
    }

    @Test
    public void testHashCodeIsDifferentForDifferentContent() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral(
                    "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>",
                    RDF.XML_LITERAL);
            Literal other = storage.createLiteral(
                    "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">edt</mods:roleTerm></mods:role>",
                    RDF.XML_LITERAL);

            assertFalse(one.hashCode() == other.hashCode());
        }
    }

    @Test
    public void testHashCodeIsDifferentForDifferentTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#string");
            Literal other = storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#integer");

            assertFalse(one.hashCode() == other.hashCode());
        }
    }

    @Test
    public void testHashCodeIsEqualForTwoEqualLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal other = storage.createLiteral("Lorem ipsum dolor sit amet", "");

            assertTrue(one.hashCode() == other.hashCode());
        }
    }

    @Test
    public void testLiteralCanBeCreatedAsRDF_HTML() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertNotNull(storage.createLiteral("<html><body><h1>It works!</h1></body></html>",
                    "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML"));
        }
    }

    @Test
    public void testLiteralCanBeCreatedAsRDF_XMLLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertNotNull(storage.createLiteral(
                    "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>",
                    RDF.XML_LITERAL));
        }
    }

    @Test
    public void testLiteralCanBeCreatedAsRDFPlainLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertNotNull(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));
        }
    }

    @Test
    public void testLiteralCanBeCreatedAsXSDType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertNotNull(storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#integer"));
        }
    }

    @Test
    public void testLiteralCannotBeCreatedAsRDFLangString() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
                try {
                    storage.createLiteral("In vino veritas est.", "la");
                    fail(storage.getClass().getSimpleName() + " should throw AssertionError, but does not.");
                } catch (AssertionError e1) {
                    /* expected */
                }
            }
        }
    }

    @Test
    public void testLiteralCreatedWithoutTypeIsAPlainLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal tested = storage.createLiteral("Lorem ipsum dolor sit amet", (NodeReference) null);
            assertEquals(RDF.PLAIN_LITERAL.getIdentifier(), tested.getType());
        }
    }

    @Test
    public void testMatchesNotOnANodeIfThatIsOfADifferentTypeWithEqualValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Node condition = storage.createNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "javac.exe");

            assertFalse(object.matches(condition));
        }
    }

    @Test
    public void testMatchesNotOnANodeIfThatIsOfTheSameTypeWithDifferentValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Node condition = storage.createNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "javac.exe");

            assertFalse(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnANodeIfThatIsOfTheSameType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Node condition = storage.createNode(RDF.PLAIN_LITERAL);

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnANodeIfThatIsOfTheSameTypeAndValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Node condition = storage.createNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "Lorem ipsum dolor sit amet");

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnANodeIfThatIsOfTheSameValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Node condition = storage.createNode().put(RDF.VALUE, "Lorem ipsum dolor sit amet");

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnAnotherLiteralIfBothAreEqual() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal condition = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnAnotherLiteralIfThatOnlyHasATypeAndTheTypeIsEqual() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal condition = storage.createLiteral("", RDF.PLAIN_LITERAL);

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnAnotherLiteralIfThatOnlyHasAValueAndTheValueIsEqual() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal condition = storage.createLiteral("Lorem ipsum dolor sit amet", (NodeReference) null);

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testToStringForInteger() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals("42", storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#integer").toString());
        }
    }

    @Test
    public void testToStringForPlainLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals("\"Lorem ipsum dolor sit amet\"",
                    storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL).toString());
        }
    }

    @Test
    public void testToStringForTypedLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals("\"<html><body><h1>It works!</h1></body></html>\"^^rdf:HTML",
                    storage.createLiteral("<html><body><h1>It works!</h1></body></html>",
                            "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML").toString());
        }
    }

}
