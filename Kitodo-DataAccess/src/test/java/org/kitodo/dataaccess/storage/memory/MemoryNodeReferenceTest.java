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

package org.kitodo.dataaccess.storage.memory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.kitodo.dataaccess.storage.memory.MemoryNodeReference;

public class MemoryNodeReferenceTest {

    @Test
    public void testEqualsIsFalseForDifferentObjects() {
        MemoryNodeReference one = new MemoryNodeReference("http://names.example/foo");
        MemoryNodeReference other = new MemoryNodeReference("http://names.example/bar");

        assertThat(one, is(not(equalTo(other))));
    }

    @Test
    public void testEqualsObject() {
        MemoryNodeReference one = new MemoryNodeReference("http://names.example/foo");
        MemoryNodeReference another = new MemoryNodeReference("http://names.example/foo");

        assertThat(one, is(equalTo(another)));
    }

    @Test
    public void testGetIdentifier() {
        assertThat(new MemoryNodeReference("http://names.example/foo").getIdentifier(),
            is(equalTo("http://names.example/foo")));
    }

    @Test
    public void testHashCodeDiffersForTwoDifferentInstances() {
        MemoryNodeReference one = new MemoryNodeReference("http://names.example/foo");
        MemoryNodeReference other = new MemoryNodeReference("http://names.example/bar");

        assertThat(one.hashCode() == other.hashCode(), is(false));
    }

    @Test
    public void testHashCodeIsEqualForTwoEqualInstances() {
        MemoryNodeReference one = new MemoryNodeReference("http://names.example/foo");
        MemoryNodeReference other = new MemoryNodeReference("http://names.example/foo");

        assertThat(one.hashCode() == other.hashCode(), is(true));
    }

    @Test
    public void testMemoryNodeReferenceCanBeCreatedFromHttpUrl() {
        new MemoryNodeReference("http://names.example/foo#bar");
    }

    @Test
    public void testMemoryNodeReferenceCanBeCreatedFromNonHttpUrl() {
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
