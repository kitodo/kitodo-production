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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.kitodo.dataaccess.RDF;
import org.kitodo.dataaccess.storage.memory.MemoryLangString;
import org.kitodo.dataaccess.storage.memory.MemoryLiteral;
import org.kitodo.dataaccess.storage.memory.MemoryNode;
import org.kitodo.dataaccess.storage.memory.MemoryNodeReference;

public class MemoryLiteralTest {

    @Test
    public void testCreateCreatesLangString() {
        assertEquals(new MemoryLangString("Hoc est corpus meum.", "la"),
            MemoryLiteral.createLeaf("Hoc est corpus meum.", "la"));
    }

    @Test
    public void testCreateCreatesMemoryNodeReferenceWithEmptyLanguage() {
        assertEquals(new MemoryNodeReference("http://www.kitodo.org/"),
            MemoryLiteral.createLeaf("http://www.kitodo.org/", ""));
    }

    @Test
    public void testCreateCreatesMemoryNodeReferenceWithNullLanguage() {
        assertEquals(new MemoryNodeReference("http://www.kitodo.org/"),
            MemoryLiteral.createLeaf("http://www.kitodo.org/", null));
    }

    @Test
    public void testCreateCreatesPlainMemoryLiteralWithEmptyLanguage() {
        assertEquals(new MemoryLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
            MemoryLiteral.createLeaf("public static void main(String[] args)", ""));
    }

    @Test
    public void testCreateCreatesPlainMemoryLiteralWithNullLanguage() {
        assertEquals(new MemoryLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
            MemoryLiteral.createLeaf("public static void main(String[] args)", null));
    }

    @Test
    public void testCreateMemoryLiteralCreatesLangString() {
        assertEquals(new MemoryLangString("Hoc est corpus meum.", "la"),
            MemoryLiteral.createLiteral("Hoc est corpus meum.", "la"));
    }

    @Test
    public void testCreateMemoryLiteralCreatesPlainMemoryLiteralWithEmptyLanguage() {
        assertEquals(new MemoryLiteral("http://www.kitodo.org/", RDF.PLAIN_LITERAL),
            MemoryLiteral.createLiteral("http://www.kitodo.org/", ""));
    }

    @Test
    public void testCreateMemoryLiteralCreatesPlainMemoryLiteralWithNullLanguage() {
        assertEquals(new MemoryLiteral("http://www.kitodo.org/", RDF.PLAIN_LITERAL),
            MemoryLiteral.createLiteral("http://www.kitodo.org/", null));
    }

    @Test
    public void testEqualsObjectForDifferentContent() {
        MemoryLiteral one = new MemoryLiteral(
                "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>",
                RDF.XML_LITERAL);
        MemoryLiteral other = new MemoryLiteral(
                "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">edt</mods:roleTerm></mods:role>",
                RDF.XML_LITERAL);

        assertThat(one, is(not(equalTo(other))));
    }

    @Test
    public void testEqualsObjectForDifferentTypes() {
        MemoryLiteral one = new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#string");
        MemoryLiteral other = new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#integer");

        assertThat(one, is(not(equalTo(other))));
    }

    @Test
    public void testEqualsObjectForTwoEqualMemoryLiterals() {
        MemoryLiteral one = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryLiteral another = MemoryLiteral.createLiteral("Lorem ipsum dolor sit amet", "");

        assertThat(one, is(equalTo(another)));
    }

    @Test
    public void testGetType() {
        assertEquals(RDF.PLAIN_LITERAL.getIdentifier(),
            new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL).getType());
    }

    @Test
    public void testGetValue() {
        assertEquals("Lorem ipsum dolor sit amet",
            new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL).getValue());
    }

    @Test
    public void testHashCodeIsDifferentForDifferentContent() {
        MemoryLiteral one = new MemoryLiteral(
                "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>",
                RDF.XML_LITERAL);
        MemoryLiteral other = new MemoryLiteral(
                "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">edt</mods:roleTerm></mods:role>",
                RDF.XML_LITERAL);

        assertThat(one.hashCode() == other.hashCode(), is(false));
    }

    @Test
    public void testHashCodeIsDifferentForDifferentTypes() {
        MemoryLiteral one = new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#string");
        MemoryLiteral other = new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#integer");

        assertThat(one.hashCode() == other.hashCode(), is(false));
    }

    @Test
    public void testHashCodeIsEqualForTwoEqualMemoryLiterals() {
        MemoryLiteral one = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryLiteral other = MemoryLiteral.createLiteral("Lorem ipsum dolor sit amet", "");

        assertThat(one.hashCode() == other.hashCode(), is(true));
    }

    @Test
    public void testMatchesNotOnAMemoryNodeIfThatIsOfADifferentTypeWithEqualValue() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryNode condition = new MemoryNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "javac.exe");

        assertThat(object.matches(condition), is(false));
    }

    @Test
    public void testMatchesNotOnAMemoryNodeIfThatIsOfTheSameTypeWithDifferentValue() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryNode condition = new MemoryNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "javac.exe");

        assertThat(object.matches(condition), is(false));
    }

    @Test
    public void testMatchesOnAMemoryNodeIfThatIsOfTheSameType() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryNode condition = new MemoryNode(RDF.PLAIN_LITERAL);

        assertThat(object.matches(condition), is(true));
    }

    @Test
    public void testMatchesOnAMemoryNodeIfThatIsOfTheSameTypeAndValue() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryNode condition = new MemoryNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "Lorem ipsum dolor sit amet");

        assertThat(object.matches(condition), is(true));
    }

    @Test
    public void testMatchesOnAMemoryNodeIfThatIsOfTheSameValue() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryNode condition = new MemoryNode().put(RDF.VALUE, "Lorem ipsum dolor sit amet");

        assertThat(object.matches(condition), is(true));
    }

    @Test
    public void testMatchesOnAnotherMemoryLiteralIfBothAreEqual() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryLiteral condition = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);

        assertThat(object.matches(condition), is(true));
    }

    @Test
    public void testMatchesOnAnotherMemoryLiteralIfThatOnlyHasATypeAndTheTypeIsEqual() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryLiteral condition = new MemoryLiteral("", RDF.PLAIN_LITERAL);

        assertThat(object.matches(condition), is(true));
    }

    @Test
    public void testMatchesOnAnotherMemoryLiteralIfThatOnlyHasAValueAndTheValueIsEqual() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryLiteral condition = new MemoryLiteral("Lorem ipsum dolor sit amet", (MemoryNodeReference) null);

        assertThat(object.matches(condition), is(true));
    }

    @Test
    public void testMemoryLiteralCanBeCreatedAsRdfHtml() {
        assertNotNull(new MemoryLiteral("<html><body><h1>It works!</h1></body></html>",
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML"));
    }

    @Test
    public void testMemoryLiteralCanBeCreatedAsRDFPlainMemoryLiteral() {
        assertThat(new MemoryLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL), is(notNullValue()));
    }

    @Test
    public void testMemoryLiteralCanBeCreatedAsRdfXmlMemoryLiteral() {
        assertNotNull(new MemoryLiteral(
                "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>",
                RDF.XML_LITERAL));
    }

    @Test
    public void testMemoryLiteralCanBeCreatedAsXSDType() {
        assertThat(new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#integer"), is(notNullValue()));
    }

    @Test(expected = AssertionError.class)
    public void testMemoryLiteralCannotBeCreatedAsRDFLangString() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            new MemoryLiteral("In vino veritas est.", "la");
        }
    }

    @Test
    public void testMemoryLiteralCreatedWithoutTypeIsAPlainMemoryLiteral() {
        MemoryLiteral tested = new MemoryLiteral("Lorem ipsum dolor sit amet", (MemoryNodeReference) null);
        assertThat(tested.getType(), is(equalTo(RDF.PLAIN_LITERAL.getIdentifier())));
    }

    @Test
    public void testMemoryLiteralStringString() {
        assertThat(new MemoryLiteral("In vino veritas est.", RDF.LANG_STRING.getIdentifier()), is(notNullValue()));
    }

    @Test
    public void testToStringForInteger() {
        assertThat(new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#integer").toString(), is(equalTo("42")));
    }

    @Test
    public void testToStringForPlainMemoryLiteral() {
        assertEquals("\"Lorem ipsum dolor sit amet\"",
            new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL).toString());
    }

    @Test
    public void testToStringForTypedMemoryLiteral() {
        assertEquals("\"<html><body><h1>It works!</h1></body></html>\"^^rdf:HTML",
            new MemoryLiteral("<html><body><h1>It works!</h1></body></html>",
                    "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML").toString());
    }

}
