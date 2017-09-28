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

public class NodeReferenceTest {

    @Test
    public void testEqualsIsFalseForDifferentObjects() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeReference one = storage.createNodeReference("http://names.example/foo");
            NodeReference other = storage.createNodeReference("http://names.example/bar");

            assertFalse(one.equals(other));
        }
    }

    @Test
    public void testEqualsObject() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeReference one = storage.createNodeReference("http://names.example/foo");
            NodeReference other = storage.createNodeReference("http://names.example/foo");

            assertTrue(one.equals(other));
        }
    }

    @Test
    public void testGetIdentifier() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals("http://names.example/foo",
                    storage.createNodeReference("http://names.example/foo").getIdentifier());
        }
    }

    @Test
    public void testHashCodeDiffersForTwoDifferentInstances() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeReference one = storage.createNodeReference("http://names.example/foo");
            NodeReference other = storage.createNodeReference("http://names.example/bar");

            assertFalse(one.hashCode() == other.hashCode());
        }
    }

    @Test
    public void testHashCodeIsEqualForTwoEqualInstances() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeReference one = storage.createNodeReference("http://names.example/foo");
            NodeReference other = storage.createNodeReference("http://names.example/foo");

            assertTrue(one.hashCode() == other.hashCode());
        }
    }

    @Test
    public void testNodeReferenceCanBeCreatedFromHTTP_URL() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNodeReference("http://names.example/foo#bar");
        }
    }

    @Test
    public void testNodeReferenceCanBeCreatedFromNonHTTP_URL() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            storage.createNodeReference("urn:example:kitodo-123456789-xyz");
        }
    }

    @Test
    public void testNodeReferenceCannotBeCreatedFromEmptyString() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            try {
                storage.createNodeReference("");
                fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
            } catch (IllegalArgumentException e) {
                /* expected */
            }
        }
    }

    @Test
    public void testNodeReferenceCannotBeCreatedFromGarbage() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
                try {
                    storage.createNodeReference("This isn’t an URI.");
                    fail(storage.getClass().getSimpleName() + " should throw AssertionError, but does not.");
                } catch (AssertionError e1) {
                    /* expected */
                }
            }
        }
    }

    @Test
    public void testNodeReferenceCannotBeCreatedFromNullReference() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
                try {
                    storage.createNodeReference(null);
                    fail(storage.getClass().getSimpleName() + " should throw IllegalArgumentException, but does not.");
                } catch (IllegalArgumentException e1) {
                    /* expected */
                }
            }
        }
    }
}
