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

import static org.junit.Assert.*;

import org.junit.Test;

public class MemoryLangStringTest {

    private static final String XML_LANG = "http://www.w3.org/XML/1998/namespace#lang";

    @Test
    public void testEqualsObject() {
        MemoryLangString one = new MemoryLangString("Hoc est corpus meum.", "la");
        MemoryLangString other = new MemoryLangString("Hoc est corpus meum.", "la");

        assertTrue(one.equals(other));
    }

    @Test
    public void testHashCode() {
        MemoryLangString one = new MemoryLangString("Hoc est corpus meum.", "la");
        MemoryLangString other = new MemoryLangString("Hoc est corpus meum.", "la");

        assertEquals(one.hashCode(), other.hashCode());
    }

    @Test
    public void testMatchesNotOnAMemoryNodeThatRequestsADifferentLanguage() {
        MemoryLangString object = new MemoryLangString("Hoc est corpus meum.", "la");
        MemoryNode condition = new MemoryNode().put(XML_LANG, "fr");

        assertFalse(object.matches(condition));
    }

    @Test
    public void testMatchesNotOnAMemoryNodeThatRequestsADifferentType() {
        MemoryLangString object = new MemoryLangString("Hoc est corpus meum.", "la");
        MemoryNode condition = new MemoryNode(Mets.FILE_SEC);

        assertFalse(object.matches(condition));
    }

    @Test
    public void testMatchesNotOnAMemoryNodeThatRequestsADifferentValue() {
        MemoryLangString object = new MemoryLangString("In vino veritas est.", "la");
        MemoryNode condition = new MemoryNode().put(RDF.VALUE, "In aqua bacillus est.");

        assertFalse(object.matches(condition));
    }

    @Test
    public void testMatchesOnAMemoryNodeThatRequestsAMemoryLangString() {
        MemoryLangString object = new MemoryLangString("Hoc est corpus meum.", "la");
        MemoryNode condition = new MemoryNode(RDF.LANG_STRING);

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnAMemoryNodeThatRequestsTheCorrectValue() {
        MemoryLangString object = new MemoryLangString("Hoc est corpus meum.", "la");
        MemoryNode condition = new MemoryNode().put(RDF.VALUE, "Hoc est corpus meum.");

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnAMemoryNodeThatRequestsTheLanguage() {
        MemoryLangString object = new MemoryLangString("Hoc est corpus meum.", "la");
        MemoryNode condition = new MemoryNode().put(XML_LANG, "la");

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMatchesOnAnotherMemoryLangStringIfTheyAreEqual() {
        MemoryLangString object = new MemoryLangString("Hoc est corpus meum.", "la");
        MemoryLangString condition = new MemoryLangString("Hoc est corpus meum.", "la");

        assertTrue(object.matches(condition));
    }

    @Test
    public void testMemoryLangStringCanBeCreatedWithBothTextAndLanguageGiven() {
        new MemoryLangString("In vino veritas est.", "la");
    }

    @Test(expected = NullPointerException.class)
    public void testMemoryLangStringCannotBeCreatedWithoutLanguage1() {
        new MemoryLangString("In aqua bacillus est.", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMemoryLangStringCannotBeCreatedWithoutLanguage2() {
        new MemoryLangString("In aqua bacillus est.", "");
    }

    @Test
    public void testToString() {
        MemoryLangString MemoryLangString = new MemoryLangString(
                "Control characters like \r and \n should be visible in the toString() result.", "en");

        assertEquals("\"Control characters like \\I and \\F should be visible in the toString() result.\"@en",
                MemoryLangString.toString());
    }
}
