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

package org.kitodo.lugh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.kitodo.lugh.vocabulary.*;

public class LangStringTest {

    private static final String XML_LANG = "http://www.w3.org/XML/1998/namespace#lang";

    @Test
    public void testEqualsObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString one = storage.newLangString("Hoc est corpus meum.", "la");
            LangString other = storage.newLangString("Hoc est corpus meum.", "la");

            assertTrue(one.equals(other));
        }
    }

    @Test
    public void testHashCode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString one = storage.newLangString("Hoc est corpus meum.", "la");
            LangString other = storage.newLangString("Hoc est corpus meum.", "la");

            assertEquals(one.hashCode(), other.hashCode());
        }
    }

    @Test
    public void testLangStringCanBeCreatedWithBothTextAndLanguageGiven() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.newLangString("In vino veritas est.", "la");
        }
    }

    @Test
    public void testLangStringCannotBeCreatedWithoutLanguage1() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.newLangString("In aqua bacillus est.", null);
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
                storage.newLangString("In aqua bacillus est.", "");
                fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
            } catch (IllegalArgumentException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.newLangString("Hoc est corpus meum.", "la");
            Node condition = storage.newNode().put(XML_LANG, "fr");

            assertFalse(object.matches(condition));
        }
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.newLangString("Hoc est corpus meum.", "la");
            Node condition = storage.newNode(Mets.FILE_SEC);

            assertFalse(object.matches(condition));
        }
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.newLangString("In vino veritas est.", "la");
            Node condition = storage.newNode().put(RDF.VALUE, "In aqua bacillus est.");

            assertFalse(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnANodeThatRequestsALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.newLangString("Hoc est corpus meum.", "la");
            Node condition = storage.newNode(RDF.LANG_STRING);

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnANodeThatRequestsTheCorrectValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.newLangString("Hoc est corpus meum.", "la");
            Node condition = storage.newNode().put(RDF.VALUE, "Hoc est corpus meum.");

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnANodeThatRequestsTheLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.newLangString("Hoc est corpus meum.", "la");
            Node condition = storage.newNode().put(XML_LANG, "la");

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testMatchesOnAnotherLangStringIfTheyAreEqual() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.newLangString("Hoc est corpus meum.", "la");
            LangString condition = storage.newLangString("Hoc est corpus meum.", "la");

            assertTrue(object.matches(condition));
        }
    }

    @Test
    public void testToString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString LangString = storage.newLangString(
                    "Control characters like \r and \n should be visible in the toString() result.", "en");

            assertEquals("\"Control characters like \\I and \\F should be visible in the toString() result.\"@en",
                    LangString.toString());
        }
    }
}
