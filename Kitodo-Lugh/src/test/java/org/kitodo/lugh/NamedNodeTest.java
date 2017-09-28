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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class NamedNodeTest {

    @Test
    public void testEqualsIfTheNameURIIsEqual() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NamedNode one = storage.createNamedNode("http://names.kitodo.org/test#Object1");
            NamedNode another = storage.createNamedNode("http://names.kitodo.org/test#Object1");

            assertTrue(one.equals(another));
        }
    }

    @Test
    public void testEqualsIfTheNameURIIsEqualButTheContentIsDifferent() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
                NamedNode one = storage.createNamedNode("http://names.kitodo.org/test#Object1");
                NamedNode another = storage.createNamedNode("http://names.kitodo.org/test#Object1");

                one.add(storage.createNode("http://names.kitodo.org/test#Class1"));
                another.add(storage.createNodeReference("http://names.kitodo.org/test#Object2"));

                try {
                    one.equals(another);
                    fail(storage.getClass().getSimpleName() + " should throw AssertionError, but does not.");
                } catch (AssertionError e1) {
                    /* expected */
                }
            }
        }
    }

    @Test
    public void testEqualsNotIfTheNameURIIsDifferent() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NamedNode one = storage.createNamedNode("http://names.kitodo.org/test#Object1");
            NamedNode another = storage.createNamedNode("http://names.kitodo.org/test#Object2");

            assertFalse(one.equals(another));
        }
    }

    @Test
    public void testGetIdentifier() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals("http://names.kitodo.org/test#Object1",
                    storage.createNamedNode("http://names.kitodo.org/test#Object1").getIdentifier());
        }
    }

    @Test
    public void testHashCodeIsEqualForTheSameURI() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NamedNode one = storage.createNamedNode("http://names.kitodo.org/test#Object1");
            NamedNode another = storage.createNamedNode("http://names.kitodo.org/test#Object1");

            assertTrue(one.hashCode() == another.hashCode());
        }
    }

    @Test
    public void testHashCodeIsInequalForADifferentURI() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NamedNode one = storage.createNamedNode("http://names.kitodo.org/test#Object1");
            NamedNode another = storage.createNamedNode("http://names.kitodo.org/test#Object2");

            assertFalse(one.hashCode() == another.hashCode());
        }
    }

    @Test
    public void testNamedNodeCanBeCreatedWithA_HTTP_URI() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNamedNode("http://names.kitodo.org/test#Object1");
        }
    }

    @Test
    public void testNamedNodeCanBeCreatedWithANonHTTP_URI() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNamedNode("urn:example:kitodo:test-1234567-xyz");
        }
    }

    @Test
    public void testNamedNodeCannotBeCreatedWithGarbage() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
                try {
                    storage.createNamedNode("This isn’t a valid URI.");
                    fail(storage.getClass().getSimpleName() + " should throw AssertionError, but does not.");
                } catch (AssertionError e1) {
                    /* expected */
                }
            }
        }

    }
}
