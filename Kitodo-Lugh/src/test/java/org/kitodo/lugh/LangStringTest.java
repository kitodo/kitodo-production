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

public class LangStringTest {

    private static final String XML_LANG = "http://www.w3.org/XML/1998/namespace#lang";

    @Test
    public void testEqualsObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString one = storage.createLangString("Hoc est corpus meum.", "la");
            LangString other = storage.createLangString("Hoc est corpus meum.", "la");

            assertTrue(one.equals(other));
        }
    }

    @Test
    public void testHashCode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString one = storage.createLangString("Hoc est corpus meum.", "la");
            LangString other = storage.createLangString("Hoc est corpus meum.", "la");

            assertEquals(one.hashCode(), other.hashCode());
        }
    }

    @Test
    public void testLangStringCanBeCreatedWithBothTextAndLanguageGiven() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createLangString("In vino veritas est.", "la");
        }
    }

    @Test
    public void testLangStringCannotBeCreatedWithoutLanguage1() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createLangString("In aqua bacillus est.", null);
                fail(storage.getClass().getSimpleName() + " should throw NullPointerException, but does not.");
            } catch (NullPointerException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testLangStringCannotBeCreatedWithoutLanguage2() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createLangString("In aqua bacillus est.", "");
                fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
            } catch (IllegalArgumentException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            Node condition = storage.createNode().put(XML_LANG, "fr");

            assertFalse(object.matches(condition));
        }
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            Node condition = storage.createNode(Mets.FILE_SEC);

            assertFalse(object.matches(condition));
        }
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("In vino veritas est.", "la");
            Node condition = storage.createNode().put(RDF.VALUE, "In aqua bacillus est.");

            assertFalse(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnANodeThatRequestsALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            Node condition = storage.createNode(RDF.LANG_STRING);

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnANodeThatRequestsTheCorrectValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            Node condition = storage.createNode().put(RDF.VALUE, "Hoc est corpus meum.");

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnANodeThatRequestsTheLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            Node condition = storage.createNode().put(XML_LANG, "la");

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnAnotherLangStringIfTheyAreEqual() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            LangString condition = storage.createLangString("Hoc est corpus meum.", "la");

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testToString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString LangString = storage.createLangString(
                    "Control characters like \r and \n should be visible in the toString() result.", "en");

            assertEquals("\"Control characters like \\I and \\F should be visible in the toString() result.\"@en",
                    LangString.toString());
        }
    }
}
