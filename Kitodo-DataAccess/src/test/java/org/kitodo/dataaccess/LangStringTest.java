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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.kitodo.dataaccess.storage.memory.MemoryNodeReference;

public class LangStringTest {

    private static final MemoryNodeReference METS_FILE_SEC = new MemoryNodeReference("http://www.loc.gov/METS/fileSec");
    private static final String XML_LANG = "http://www.w3.org/XML/1998/namespace#lang";

    @Test
    public void testEqualsObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString one = storage.createLangString("Hoc est corpus meum.", "la");
            LangString another = storage.createLangString("Hoc est corpus meum.", "la");

            assertThat(one, is(equalTo(another)));
        }
    }

    @Test
    public void testHashCode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString one = storage.createLangString("Hoc est corpus meum.", "la");
            LangString other = storage.createLangString("Hoc est corpus meum.", "la");

            assertThat(other.hashCode(), is(equalTo(one.hashCode())));
        }
    }

    @Test
    public void testLangStringCanBeCreatedWithBothTextAndLanguageGiven() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createLangString("In vino veritas est.", "la");
        }
    }

    @Test
    public void testLangStringCannotBeCreatedWithEmptyLanguage() {
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
    public void testLangStringCannotBeCreatedWithNullLanguage() {
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
    public void testMatchesNotOnANodeThatRequestsADifferentLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            Node condition = storage.createNode().put(XML_LANG, "fr");

            assertThat(object.matches(condition), is(false));
        }
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            Node condition = storage.createNode(METS_FILE_SEC);

            assertThat(object.matches(condition), is(false));
        }
    }

    @Test
    public void testMatchesNotOnANodeThatRequestsADifferentValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("In vino veritas est.", "la");
            Node condition = storage.createNode().put(RDF.VALUE, "In aqua bacillus est.");

            assertThat(object.matches(condition), is(false));
        }
    }

    @Test
    public void testMatchesOnANodeThatRequestsALangString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            Node condition = storage.createNode(RDF.LANG_STRING);

            assertThat(object.matches(condition), is(true));
        }
    }

    @Test
    public void testMatchesOnANodeThatRequestsTheCorrectValue() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            Node condition = storage.createNode().put(RDF.VALUE, "Hoc est corpus meum.");

            assertThat(object.matches(condition), is(true));
        }
    }

    @Test
    public void testMatchesOnANodeThatRequestsTheLanguage() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            Node condition = storage.createNode().put(XML_LANG, "la");

            assertThat(object.matches(condition), is(true));
        }
    }

    @Test
    public void testMatchesOnAnotherLangStringIfTheyAreEqual() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString object = storage.createLangString("Hoc est corpus meum.", "la");
            LangString condition = storage.createLangString("Hoc est corpus meum.", "la");

            assertThat(object.matches(condition), is(true));
        }
    }

    @Test
    public void testToString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            LangString langString = storage.createLangString(
                "Control characters like \r and \n should be visible in the toString() result.", "en");

            assertEquals("\"Control characters like \\I and \\F should be visible in the toString() result.\"@en",
                langString.toString());
        }
    }
}
