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
import org.kitodo.production.lugh.Mets;

public class LangStringTest {

    private static final String XML_LANG = "http://www.w3.org/XML/1998/namespace#lang";

    @Test
    public void testHashCode() {
        LangString one = new LangString("Hoc est corpus meum.", "la");
        LangString other = new LangString("Hoc est corpus meum.", "la");

        assertEquals(one.hashCode(), other.hashCode());
    }

    @Test
    public void testEqualsObject() {
        LangString one = new LangString("Hoc est corpus meum.", "la");
        LangString other = new LangString("Hoc est corpus meum.", "la");

        assertTrue(one.equals(other));
    }

    @Test
    public void testMatchesOnAnotherLangStringIfTheyAreEqual() {
        LangString object = new LangString("Hoc est corpus meum.", "la");
        LangString condition = new LangString("Hoc est corpus meum.", "la");

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnANodeThatRequestsTheLanguage() {
        LangString object = new LangString("Hoc est corpus meum.", "la");
        Node condition = new Node().put(XML_LANG, "la");

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentLanguage() {
        LangString object = new LangString("Hoc est corpus meum.", "la");
        Node condition = new Node().put(XML_LANG, "fr");

        assertFalse(object.matches(condition));
    }

    @Test
    public void testMatchesOnANodeThatRequestsALangString() {
        LangString object = new LangString("Hoc est corpus meum.", "la");
        Node condition = new Node(RDF.LANG_STRING);

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentType() {
        LangString object = new LangString("Hoc est corpus meum.", "la");
        Node condition = new Node(Mets.FILE_SEC);

        assertFalse(object.matches(condition));
    }

    @Test
    public void testMatchesOnANodeThatRequestsTheCorrectValue() {
        LangString object = new LangString("Hoc est corpus meum.", "la");
        Node condition = new Node().put(RDF.VALUE, "Hoc est corpus meum.");

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentValue() {
        LangString object = new LangString("In vino veritas est.", "la");
        Node condition = new Node().put(RDF.VALUE, "In aqua bacillus est.");

        assertFalse(object.matches(condition));
    }

    @Test
    public void testToString() {
        LangString langString = new LangString("Control characters like \r and \n should be visible in the toString() result.", "en");

        assertEquals("\"Control characters like \\I and \\F should be visible in the toString() result.\"@en", langString.toString());
    }

    @Test
    public void testLangStringCanBeCreatedWithBothTextAndLanguageGiven() {
        new LangString("In vino veritas est.", "la");
    }

    @Test(expected = NullPointerException.class)
    public void testLangStringCannotBeCreatedWithoutLanguage1() {
        new LangString("In aqua bacillus est.", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLangStringCannotBeCreatedWithoutLanguage2() {
        new LangString("In aqua bacillus est.", "");
    }
}
