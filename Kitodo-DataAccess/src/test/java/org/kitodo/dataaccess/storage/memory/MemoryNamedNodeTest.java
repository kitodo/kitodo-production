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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.kitodo.dataaccess.storage.memory.MemoryNamedNode;
import org.kitodo.dataaccess.storage.memory.MemoryNode;
import org.kitodo.dataaccess.storage.memory.MemoryNodeReference;

public class MemoryNamedNodeTest {

    @Test
    public void testEqualsIfTheNameURIIsEqual() {
        MemoryNamedNode one = new MemoryNamedNode("http://names.kitodo.org/test#Object1");
        MemoryNamedNode another = new MemoryNamedNode("http://names.kitodo.org/test#Object1");

        assertThat(one, is(equalTo(another)));
    }

    @Test(expected = AssertionError.class)
    public void testEqualsIfTheNameURIIsEqualButTheContentIsDifferent() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            MemoryNamedNode one = new MemoryNamedNode("http://names.kitodo.org/test#Object1");
            MemoryNamedNode another = new MemoryNamedNode("http://names.kitodo.org/test#Object1");

            one.add(new MemoryNode("http://names.kitodo.org/test#Class1"));
            another.add(new MemoryNodeReference("http://names.kitodo.org/test#Object2"));

            assertThat(one, is(equalTo(another)));
        }
    }

    @Test
    public void testEqualsNotIfTheNameURIIsDifferent() {
        MemoryNamedNode one = new MemoryNamedNode("http://names.kitodo.org/test#Object1");
        MemoryNamedNode another = new MemoryNamedNode("http://names.kitodo.org/test#Object2");

        assertThat(one, is(not(equalTo(another))));
    }

    @Test
    public void testGetIdentifier() {
        assertEquals("http://names.kitodo.org/test#Object1",
            new MemoryNamedNode("http://names.kitodo.org/test#Object1").getIdentifier());
    }

    @Test
    public void testHashCodeIsEqualForTheSameURI() {
        MemoryNamedNode one = new MemoryNamedNode("http://names.kitodo.org/test#Object1");
        MemoryNamedNode another = new MemoryNamedNode("http://names.kitodo.org/test#Object1");

        assertThat(one.hashCode() == another.hashCode(), is(true));
    }

    @Test
    public void testHashCodeIsInequalForADifferentURI() {
        MemoryNamedNode one = new MemoryNamedNode("http://names.kitodo.org/test#Object1");
        MemoryNamedNode another = new MemoryNamedNode("http://names.kitodo.org/test#Object2");

        assertThat(one.hashCode() == another.hashCode(), is(false));
    }

    @Test
    public void testMemoryNamedNodeCanBeCreatedWithAHttpUri() {
        new MemoryNamedNode("http://names.kitodo.org/test#Object1");
    }

    @Test
    public void testMemoryNamedNodeCanBeCreatedWithANonHttpUri() {
        new MemoryNamedNode("urn:example:kitodo:test-1234567-xyz");
    }

    @Test(expected = AssertionError.class)
    public void testMemoryNamedNodeCannotBeCreatedWithGarbage() {
        try {
            assert (false);
            fail("Assertions disabled. Run JVM with -ea option.");
        } catch (AssertionError e) {
            new MemoryNamedNode("This isn’t a valid URI.");
        }

    }
}
