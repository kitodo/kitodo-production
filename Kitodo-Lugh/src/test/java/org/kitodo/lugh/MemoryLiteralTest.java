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
import org.kitodo.lugh.vocabulary.RDF;

public class MemoryLiteralTest {

    @Test
    public void testCreateCreatesLangString() {
        assertEquals(new MemoryLangString("Hoc est corpus meum.", "la"),
                MemoryLiteral.create("Hoc est corpus meum.", "la"));
    }

    @Test
    public void testCreateCreatesMemoryNodeReference1() {
        assertEquals(new MemoryNodeReference("http://www.kitodo.org/"),
                MemoryLiteral.create("http://www.kitodo.org/", null));
    }

    @Test
    public void testCreateCreatesMemoryNodeReference2() {
        assertEquals(new MemoryNodeReference("http://www.kitodo.org/"),
                MemoryLiteral.create("http://www.kitodo.org/", ""));
    }

    @Test
    public void testCreateCreatesPlainMemoryLiteral1() {
        assertEquals(new MemoryLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                MemoryLiteral.create("public static void main(String[] args)", null));
    }

    @Test
    public void testCreateCreatesPlainMemoryLiteral2() {
        assertEquals(new MemoryLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL),
                MemoryLiteral.create("public static void main(String[] args)", ""));
    }

    @Test
    public void testCreateMemoryLiteralCreatesLangString() {
        assertEquals(new MemoryLangString("Hoc est corpus meum.", "la"),
                MemoryLiteral.createLiteral("Hoc est corpus meum.", "la"));
    }

    @Test
    public void testCreateMemoryLiteralCreatesPlainMemoryLiteral1() {
        assertEquals(new MemoryLiteral("http://www.kitodo.org/", RDF.PLAIN_LITERAL),
                MemoryLiteral.createLiteral("http://www.kitodo.org/", null));
    }

    @Test
    public void testCreateMemoryLiteralCreatesPlainMemoryLiteral2() {
        assertEquals(new MemoryLiteral("http://www.kitodo.org/", RDF.PLAIN_LITERAL),
                MemoryLiteral.createLiteral("http://www.kitodo.org/", ""));
    }

    @Test
    public void testEqualsObjectForDifferentContent() {
        MemoryLiteral one = new MemoryLiteral(
                "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>",
                RDF.XML_LITERAL);
        MemoryLiteral other = new MemoryLiteral(
                "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">edt</mods:roleTerm></mods:role>",
                RDF.XML_LITERAL);

        assertFalse(one.equals(other));
    }

    @Test
    public void testEqualsObjectForDifferentTypes() {
        MemoryLiteral one = new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#string");
        MemoryLiteral other = new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#integer");

        assertFalse(one.equals(other));
    }

    @Test
    public void testEqualsObjectForTwoEqualMemoryLiterals() {
        MemoryLiteral one = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryLiteral other = MemoryLiteral.createLiteral("Lorem ipsum dolor sit amet", "");

        assertTrue(one.equals(other));
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

        assertFalse(one.hashCode() == other.hashCode());
    }

    @Test
    public void testHashCodeIsDifferentForDifferentTypes() {
        MemoryLiteral one = new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#string");
        MemoryLiteral other = new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#integer");

        assertFalse(one.hashCode() == other.hashCode());
    }

    @Test
    public void testHashCodeIsEqualForTwoEqualMemoryLiterals() {
        MemoryLiteral one = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryLiteral other = MemoryLiteral.createLiteral("Lorem ipsum dolor sit amet", "");

        assertTrue(one.hashCode() == other.hashCode());
    }

    @Test
    public void testMatchesNotOnAMemoryNodeIfThatIsOfADifferentTypeWithEqualValue() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryNode condition = new MemoryNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "javac.exe");

        assertFalse(object.matches(condition));
    }

    @Test
    public void testMatchesNotOnAMemoryNodeIfThatIsOfTheSameTypeWithDifferentValue() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryNode condition = new MemoryNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "javac.exe");

        assertFalse(object.matches(condition));
    }

    @Test
    public void testMatchesOnAMemoryNodeIfThatIsOfTheSameType() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryNode condition = new MemoryNode(RDF.PLAIN_LITERAL);

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnAMemoryNodeIfThatIsOfTheSameTypeAndValue() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryNode condition = new MemoryNode(RDF.PLAIN_LITERAL).put(RDF.VALUE, "Lorem ipsum dolor sit amet");

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnAMemoryNodeIfThatIsOfTheSameValue() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryNode condition = new MemoryNode().put(RDF.VALUE, "Lorem ipsum dolor sit amet");

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnAnotherMemoryLiteralIfBothAreEqual() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryLiteral condition = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnAnotherMemoryLiteralIfThatOnlyHasATypeAndTheTypeIsEqual() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryLiteral condition = new MemoryLiteral("", RDF.PLAIN_LITERAL);

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnAnotherMemoryLiteralIfThatOnlyHasAValueAndTheValueIsEqual() {
        MemoryLiteral object = new MemoryLiteral("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        MemoryLiteral condition = new MemoryLiteral("Lorem ipsum dolor sit amet", (MemoryNodeReference) null);

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMemoryLiteralCanBeCreatedAsRDF_HTML() {
        assertNotNull(new MemoryLiteral("<html><body><h1>It works!</h1></body></html>",
                "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML"));
    }

    @Test
    public void testMemoryLiteralCanBeCreatedAsRDF_XMLMemoryLiteral() {
        assertNotNull(new MemoryLiteral(
                "<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>",
                RDF.XML_LITERAL));
    }

    @Test
    public void testMemoryLiteralCanBeCreatedAsRDFPlainMemoryLiteral() {
        assertNotNull(new MemoryLiteral("public static void main(String[] args)", RDF.PLAIN_LITERAL));
    }

    @Test
    public void testMemoryLiteralCanBeCreatedAsXSDType() {
        assertNotNull(new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#integer"));
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
        assertEquals(RDF.PLAIN_LITERAL.getIdentifier(), tested.getType());
    }

    @Test
    public void testMemoryLiteralStringString() {
        assertNotNull(new MemoryLiteral("In vino veritas est.", RDF.LANG_STRING.getIdentifier()));
    }

    @Test
    public void testToStringForInteger() {
        assertEquals("42", new MemoryLiteral("42", "http://www.w3.org/2001/XMLSchema#integer").toString());
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
