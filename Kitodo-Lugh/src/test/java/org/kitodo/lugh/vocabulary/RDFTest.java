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

package org.kitodo.lugh.vocabulary;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.kitodo.lugh.Node;
import org.kitodo.lugh.vocabulary.RDF;

public class RDFTest {

    @Test
    public void testSequenceNumberOfBelowBelowLongMinValue() {
        assertNull(RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_-9223372036854775809"));
    }

    @Test
    public void testSequenceNumberOfBelowMin() {
        assertNull(RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_0"));
    }

    @Test
    public void testSequenceNumberOfInTheMiddle() {
        assertEquals(4_500_000_000_000_000_000L,
                (long) RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_4500000000000000000"));
    }

    @Test
    public void testSequenceNumberOfMax() {
        assertEquals(Long.MAX_VALUE,
                (long) RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_9223372036854775807"));
    }

    @Test
    public void testSequenceNumberOfMin() {
        assertEquals(Node.FIRST_INDEX, (long) RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1"));
    }

    @Test
    public void testSequenceNumberOfNegative() {
        assertNull(RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_-1"));
    }

    @Test(expected = ArithmeticException.class)
    public void testSequenceNumberOfOneAboveMax() {
        RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_9223372036854775808");
    }

    @Test
    public void testSequenceNumberOfOneAboveMin() {
        assertEquals(Node.FIRST_INDEX + 1,
                (long) RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_2"));
    }

    @Test
    public void testSequenceNumberOfOneBelowMax() {
        assertEquals(Long.MAX_VALUE - 1,
                (long) RDF.sequenceNumberOf("http://www.w3.org/1999/02/22-rdf-syntax-ns#_9223372036854775806"));
    }

    @Test
    public void testSequenceNumberOfWithGarbage() {
        assertNull(RDF.sequenceNumberOf("http://www.example.org/this-is-no-numeric-RDF-reference"));
    }

    @Test
    public void testToURLInTheMiddle() {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#_4500000000000000000",
                RDF.toURL(4_500_000_000_000_000_000L));
    }

    @Test
    public void testToURLMax() {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#_9223372036854775807", RDF.toURL(Long.MAX_VALUE));
    }

    @Test
    public void testToURLMin() {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#_1", RDF.toURL(Node.FIRST_INDEX));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToURLOneAboveMax() {
        RDF.toURL(Long.MAX_VALUE + 1);
    }

    @Test
    public void testToURLOneAboveMin() {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#_2", RDF.toURL(Node.FIRST_INDEX + 1));
    }

    @Test
    public void testToURLOneBelowMax() {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#_9223372036854775806", RDF.toURL(Long.MAX_VALUE - 1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testToURLOneBelowMin() {
        RDF.toURL(Node.FIRST_INDEX - 1);
    }
}
