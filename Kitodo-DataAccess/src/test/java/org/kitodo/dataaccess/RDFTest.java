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

import java.util.Optional;
import org.junit.Test;

public class RDFTest {

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSequenceNumberOfBelowBelowLongMinValue() {
        RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_-9223372036854775809");
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSequenceNumberOfBelowMin() {
        RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_0");
    }

    @Test
    public void testSequenceNumberOfInTheMiddle() {
        assertEquals(4_500_000_000_000_000_000L,
            (long) RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_4500000000000000000").get());
    }

    @Test
    public void testSequenceNumberOfMax() {
        assertEquals(Long.MAX_VALUE,
            (long) RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_9223372036854775807").get());
    }

    @Test
    public void testSequenceNumberOfMin() {
        assertThat((long) RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1").get(),
            is((long) Node.FIRST_INDEX));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSequenceNumberOfNegative() {
        RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_-1");
    }

    @Test(expected = ArithmeticException.class)
    public void testSequenceNumberOfOneAboveMax() {
        RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_9223372036854775808");
    }

    @Test
    public void testSequenceNumberOfOneAboveMin() {
        assertEquals(Node.FIRST_INDEX + 1,
            (long) RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_2").get());
    }

    @Test
    public void testSequenceNumberOfOneBelowMax() {
        assertEquals(Long.MAX_VALUE - 1,
            (long) RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_9223372036854775806").get());
    }

    @Test
    public void testSequenceNumberOfWithGarbage() {
        assertThat(RDF.sequenceNumberOf("http://www.example.org/this-is-no-numeric-RDF-reference"),
            is(Optional.empty()));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testSequenceNumberOfZero() {
        RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_0");
    }

    @Test
    public void testToURLInTheMiddle() {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#_4500000000000000000",
            RDF.toURL(4_500_000_000_000_000_000L));
    }

    @Test
    public void testToURLMax() {
        assertThat(RDF.toURL(Long.MAX_VALUE),
            is(equalTo("http://www.w3.org/1999/02/22-rdf-syntax-ns#_9223372036854775807")));
    }

    @Test
    public void testToURLMin() {
        assertThat(RDF.toURL(Node.FIRST_INDEX), is(equalTo("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1")));
    }

    @Test
    public void testToURLOneAboveMin() {
        assertThat(RDF.toURL(Node.FIRST_INDEX + 1), is(equalTo("http://www.w3.org/1999/02/22-rdf-syntax-ns#_2")));
    }

    @Test
    public void testToURLOneBelowMax() {
        assertThat(RDF.toURL(Long.MAX_VALUE - 1),
            is(equalTo("http://www.w3.org/1999/02/22-rdf-syntax-ns#_9223372036854775806")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToURLOneBelowMin() {
        RDF.toURL(Node.FIRST_INDEX - 1);
    }
}
