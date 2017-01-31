/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General private License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.lugh.ld;

import static org.junit.Assert.*;

import org.junit.Test;

public class LiteralTest {

    @Test
    public void testHashCodeIsEqualForTwoEqualLiterals() {
        Literal one = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        Literal other = Literal.createLiteral("Lorem ipsum dolor sit amet", "");

        assertTrue(one.hashCode() == other.hashCode());
    }

    @Test
    public void testHashCodeIsDifferentForDifferentContent() {
        Literal one = new Literal("<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>", RDF.XML_LITERAL);
        Literal other = new Literal("<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">edt</mods:roleTerm></mods:role>", RDF.XML_LITERAL);

        assertFalse(one.hashCode() == other.hashCode());
    }

    @Test
    public void testHashCodeIsDifferentForDifferentTypes() {
        Literal one = new Literal("42", "http://www.w3.org/2001/XMLSchema#string");
        Literal other = new Literal("42", "http://www.w3.org/2001/XMLSchema#integer");

        assertFalse(one.hashCode() == other.hashCode());
    }

    @Test
    public void testCreateCreatesNodeReference1() {
        assertEquals(new NodeReference("http://www.kitodo.org/"), Literal.create("http://www.kitodo.org/", null));
    }

    @Test
    public void testCreateCreatesNodeReference2() {
        assertEquals(new NodeReference("http://www.kitodo.org/"), Literal.create("http://www.kitodo.org/", ""));
    }

    @Test
    public void testCreateCreatesPlainLiteral1() {
        assertEquals(
            new Literal("public static void main(String[] args)", RDF.PLAIN_LITERAL),
            Literal.create("public static void main(String[] args)", null)
        );
    }

    @Test
    public void testCreateCreatesPlainLiteral2() {
        assertEquals(
            new Literal("public static void main(String[] args)", RDF.PLAIN_LITERAL),
            Literal.create("public static void main(String[] args)", "")
        );
    }

    @Test
    public void testCreateCreatesLangString() {
        assertEquals(
            new LangString("Hoc est corpus meum.", "la"),
            Literal.create("Hoc est corpus meum.", "la")
        );
    }

    @Test
    public void testCreateLiteralCreatesPlainLiteral1() {
        assertEquals(
            new Literal("http://www.kitodo.org/", RDF.PLAIN_LITERAL),
            Literal.createLiteral("http://www.kitodo.org/", null)
        );
    }

    @Test
    public void testCreateLiteralCreatesPlainLiteral2() {
        assertEquals(
            new Literal("http://www.kitodo.org/", RDF.PLAIN_LITERAL),
            Literal.createLiteral("http://www.kitodo.org/", "")
        );
    }

    @Test
    public void testCreateLiteralCreatesLangString() {
        assertEquals(
            new LangString("Hoc est corpus meum.", "la"),
            Literal.createLiteral("Hoc est corpus meum.", "la")
        );
    }

    @Test
    public void testLiteralCanBeCreatedAsRDF_HTML() {
        assertNotNull(
            new Literal("<html><body><h1>It works!</h1></body></html>", "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML")
        );
    }

    @Test
    public void testLiteralCanBeCreatedAsRDFPlainLiteral() {
        assertNotNull(new Literal("public static void main(String[] args)", RDF.PLAIN_LITERAL));
    }

    @Test
    public void testLiteralCanBeCreatedAsRDF_XMLLiteral() {
        assertNotNull(new Literal("<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>", RDF.XML_LITERAL));
    }

    @Test
    public void testLiteralCanBeCreatedAsXSDType() {
        assertNotNull(new Literal("42", "http://www.w3.org/2001/XMLSchema#integer"));
    }

    @Test(expected = AssertionError.class)
    public void testLiteralCannotBeCreatedAsRDFLangString() {
        try{
            assert(false);
            fail("Assertions disabled. Run JVM with -ea option.");
        }catch(AssertionError e){
            new Literal("In vino veritas est.", "la");
        }
    }

    @Test
    public void testLiteralCreatedWithoutTypeIsAPlainLiteral() {
        Literal tested = new Literal("Lorem ipsum dolor sit amet", (NodeReference) null);
        assertEquals(RDF.PLAIN_LITERAL.getIdentifier(), tested.getType());
    }

    @Test
    public void testLiteralStringString() {
        assertNotNull(new Literal("In vino veritas est.", RDF.LANG_STRING.getIdentifier()));
    }

    @Test
    public void testEqualsObjectForTwoEqualLiterals() {
        Literal one = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        Literal other = Literal.createLiteral("Lorem ipsum dolor sit amet", "");

        assertTrue(one.equals(other));
    }

    @Test
    public void testEqualsObjectForDifferentContent() {
        Literal one = new Literal("<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">aut</mods:roleTerm></mods:role>", RDF.XML_LITERAL);
        Literal other = new Literal("<mods:role><mods:roleTerm authority=\"marcrelator\" type=\"code\">edt</mods:roleTerm></mods:role>", RDF.XML_LITERAL);

        assertFalse(one.equals(other));
    }

    @Test
    public void testEqualsObjectForDifferentTypes() {
        Literal one = new Literal("42", "http://www.w3.org/2001/XMLSchema#string");
        Literal other = new Literal("42", "http://www.w3.org/2001/XMLSchema#integer");

        assertFalse(one.equals(other));
    }

    @Test
    public void testGetType() {
        assertEquals(RDF.PLAIN_LITERAL.getIdentifier(), new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL).getType());
    }

    @Test
    public void testGetValue() {
        assertEquals("Lorem ipsum dolor sit amet", new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL).getValue());
    }

    @Test
    public void testMatchesOnAnotherLiteralIfBothAreEqual() {
        Literal object = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        Literal condition = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnAnotherLiteralIfThatOnlyHasATypeAndTheTypeIsEqual() {
        Literal object = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        Literal condition = new Literal("", RDF.PLAIN_LITERAL);

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnAnotherLiteralIfThatOnlyHasAValueAndTheValueIsEqual() {
        Literal object = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        Literal condition = new Literal("Lorem ipsum dolor sit amet", (NodeReference) null);

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnANodeIfThatIsOfTheSameType() {
        Literal object = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        Node condition = new Node(RDF.PLAIN_LITERAL);

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnANodeIfThatIsOfTheSameValue() {
        Literal object = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        Node condition = new Node().put(RDF.VALUE, "Lorem ipsum dolor sit amet");

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnANodeIfThatIsOfTheSameTypeAndValue() {
        Literal object = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        Node condition = new Node(RDF.PLAIN_LITERAL).put(RDF.VALUE, "Lorem ipsum dolor sit amet");

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesNotOnANodeIfThatIsOfTheSameTypeWithDifferentValue() {
        Literal object = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        Node condition = new Node(RDF.PLAIN_LITERAL).put(RDF.VALUE, "javac.exe");

        assertFalse(object.matches(condition));
    }

    @Test
    public void testMatchesNotOnANodeIfThatIsOfADifferentTypeWithEqualValue() {
        Literal object = new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL);
        Node condition = new Node(RDF.PLAIN_LITERAL).put(RDF.VALUE, "javac.exe");

        assertFalse(object.matches(condition));
    }

    @Test
    public void testToStringForPlainLiteral() {
        assertEquals(
            "\"Lorem ipsum dolor sit amet\"",
            new Literal("Lorem ipsum dolor sit amet", RDF.PLAIN_LITERAL).toString()
        );
    }

    @Test
    public void testToStringForInteger() {
        assertEquals("42", new Literal("42", "http://www.w3.org/2001/XMLSchema#integer").toString());
    }

    @Test
    public void testToStringForTypedLiteral() {
        assertEquals(
            "\"<html><body><h1>It works!</h1></body></html>\"^^rdf:HTML",
            new Literal("<html><body><h1>It works!</h1></body></html>", "http://www.w3.org/1999/02/22-rdf-syntax-ns#HTML").toString()
        );
    }

}
