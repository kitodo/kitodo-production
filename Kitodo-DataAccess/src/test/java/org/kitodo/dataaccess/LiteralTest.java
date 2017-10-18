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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.kitodo.dataaccess.Literal;
import org.kitodo.dataaccess.Node;
import org.kitodo.dataaccess.NodeReference;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.Storage;
import org.kitodo.dataaccess.XMLSchema;

public class LiteralTest {

    @Test
    public void testCreateCreatesLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLangString("Hoc est corpus meum.", "la"),
                storage.createLeaf("Hoc est corpus meum.", "la"));
        }
    }

    @Test
    public void testCreateCreatesNodeReferenceWithEmptyLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createNodeReference("http://www.kitodo.org/"),
                storage.createLeaf("http://www.kitodo.org/", ""));
        }
    }

    @Test
    public void testCreateCreatesNodeReferenceWithNullLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createNodeReference("http://www.kitodo.org/"),
                storage.createLeaf("http://www.kitodo.org/", null));
        }
    }

    @Test
    public void testCreateCreatesPlainLiteralWithEmptyLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLeaf("public static void main(String[] args)", ""));
        }
    }

    @Test
    public void testCreateCreatesPlainLiteralWithNullLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                storage.createLeaf("public static void main(String[] args)", null));
        }
    }

    @Test
    public void testCreateLiteralCreatesLangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLangString("Hoc est corpus meum.", "la"),
                storage.createLeaf("Hoc est corpus meum.", "la"));
        }
    }

    @Test
    public void testCreateLiteralCreatesPlainLiteralWithEmptyLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLiteral("http://www.kitodo.org/", RDF.PLAIN_LITERAL),
                storage.createLiteral("http://www.kitodo.org/", ""));
        }
    }

    @Test
    public void testCreateLiteralCreatesPlainLiteralWithNullLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals(storage.createLiteral("http://www.kitodo.org/", RDF.PLAIN_LITERAL),
                storage.createLiteral("http://www.kitodo.org/", (NodeReference) null));
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

            assertThat(one, is(not(equalTo(other))));
        }
    }

    @Test
    public void testEqualsObjectForDifferentTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#string");
            Literal other = storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#integer");

            assertThat(one, is(not(equalTo(other))));
        }
    }

    @Test
    public void testEqualsObjectForTwoEqualLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal another = storage.createLiteral("Lorem ipsum dolor sit amet", "");

            assertThat(one, is(equalTo(another)));
        }
    }

    @Test
    public void testEqualsObjectForTwoEqualLiteralsWithAndWithoutStringDeclared() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal another = storage.createLiteral("Lorem ipsum dolor sit amet", XMLSchema.STRING);

            assertThat(one, is(equalTo(another)));
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

            assertThat(one.hashCode() == other.hashCode(), is(false));
        }
    }

    @Test
    public void testHashCodeIsDifferentForDifferentTypes() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#string");
            Literal other = storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#integer");

            assertThat(one.hashCode() == other.hashCode(), is(false));
        }
    }

    @Test
    public void testHashCodeIsEqualForTwoEqualLiterals() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal one = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal other = storage.createLiteral("Lorem ipsum dolor sit amet", "");

            assertThat(one.hashCode() == other.hashCode(), is(true));
        }
    }

    @Test
    public void testLiteralCanBeCreatedAsRdfHtml() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertNotNull(storage.createLiteral("<html><body><h1>It works!</h1></body></html>",
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML"));
        }
    }

    @Test
    public void testLiteralCanBeCreatedAsRDFPlainLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertThat(storage.createLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                is(notNullValue()));
        }
    }

    @Test
    public void testLiteralCanBeCreatedAsRdfXmlLiteral() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertNotNull(storage.createLiteral(
                "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>",
                RDF.XML_LITERAL));
        }
    }

    @Test
    public void testLiteralCanBeCreatedAsXSDType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertThat(storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#integer"), is(notNullValue()));
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
            assertThat(tested.getType(), is(equalTo(RDF.PLAIN_LITERAL.getIdentifier())));
        }
    }

    @Test
    public void testMatchesNotOnANodeIfThatIsOfADifferentTypeWithEqualValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Node condition = storage.createNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "javac.exe");

            assertThat(object.matches(condition), is(false));
        }
    }

    @Test
    public void testMatchesNotOnANodeIfThatIsOfTheSameTypeWithDifferentValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Node condition = storage.createNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "javac.exe");

            assertThat(object.matches(condition), is(false));
        }
    }

    @Test
    public void testMatchesOnANodeIfThatIsOfTheSameType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Node condition = storage.createNode(RDF.PLAIN_LITERAL);

            assertThat(object.matches(condition), is(true));
        }
    }

    @Test
    public void testMatchesOnANodeIfThatIsOfTheSameTypeAndValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Node condition = storage.createNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "Lorem ipsum dolor sit amet");

            assertThat(object.matches(condition), is(true));
        }
    }

    @Test
    public void testMatchesOnANodeIfThatIsOfTheSameValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Node condition = storage.createNode().put(RDF.VALUE, "Lorem ipsum dolor sit amet");

            assertThat(object.matches(condition), is(true));
        }
    }

    @Test
    public void testMatchesOnAnotherLiteralIfBothAreEqual() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal condition = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);

            assertThat(object.matches(condition), is(true));
        }
    }

    @Test
    public void testMatchesOnAnotherLiteralIfThatOnlyHasATypeAndTheTypeIsEqual() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal condition = storage.createLiteral("", RDF.PLAIN_LITERAL);

            assertThat(object.matches(condition), is(true));
        }
    }

    @Test
    public void testMatchesOnAnotherLiteralIfThatOnlyHasAValueAndTheValueIsEqual() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            Literal object = storage.createLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
            Literal condition = storage.createLiteral("Lorem ipsum dolor sit amet", (NodeReference) null);

            assertThat(object.matches(condition), is(true));
        }
    }

    @Test
    public void testToStringForInteger() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertThat(storage.createLiteral("42", "http://www.w3.org/2001/XMLSchema#integer").toString(),
                is(equalTo("42")));
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
