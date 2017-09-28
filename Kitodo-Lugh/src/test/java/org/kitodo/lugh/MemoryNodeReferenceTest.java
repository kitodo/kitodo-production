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

public class MemoryNodeReferenceTest {

    @Test
    public void testEqualsIsFalseForDifferentObjects() {
        MemoryNodeReference one = new MemoryNodeReference("http://names.example/foo");
        MemoryNodeReference other = new MemoryNodeReference("http://names.example/bar");

        assertFalse(one.equals(other));
    }

    @Test
    public void testEqualsObject() {
        MemoryNodeReference one = new MemoryNodeReference("http://names.example/foo");
        MemoryNodeReference other = new MemoryNodeReference("http://names.example/foo");

        assertTrue(one.equals(other));
    }

    @Test
    public void testGetIdentifier() {
        assertEquals("http://names.example/foo", new MemoryNodeReference("http://names.example/foo").getIdentifier());
    }

    @Test
    public void testHashCodeDiffersForTwoDifferentInstances() {
        MemoryNodeReference one = new MemoryNodeReference("http://names.example/foo");
        MemoryNodeReference other = new MemoryNodeReference("http://names.example/bar");

        assertFalse(one.hashCode() == other.hashCode());
    }

    @Test
    public void testHashCodeIsEqualForTwoEqualInstances() {
        MemoryNodeReference one = new MemoryNodeReference("http://names.example/foo");
        MemoryNodeReference other = new MemoryNodeReference("http://names.example/foo");

        assertTrue(one.hashCode() == other.hashCode());
    }

    @Test
    public void testMemoryNodeReferenceCanBeCreatedFromHTTP_URL() {
        new MemoryNodeReference("http://names.example/foo#bar");
    }

    @Test
    public void testMemoryNodeReferenceCanBeCreatedFromNonHTTP_URL() {
        new MemoryNodeReference("urn:example:kitodo-123456789-xyz");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMemoryNodeReferenceCannotBeCreatedFromEmptyString() {
        new MemoryNodeReference("");
    }

    @Test(expected = AssertionError.class)
    public void testMemoryNodeReferenceCannotBeCreatedFromGarbage() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            new MemoryNodeReference("This isn’t an URI.");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testMemoryNodeReferenceCannotBeCreatedFromNullReference() {
        new MemoryNodeReference(null);
    }
}
