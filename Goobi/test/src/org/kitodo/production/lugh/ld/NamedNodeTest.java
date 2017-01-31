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

package org.kitodo.production.lugh.ld;

import static org.junit.Assert.*;

import org.junit.Test;

public class NamedNodeTest {

    @Test
    public void testHashCodeIsEqualForTheSameURI() {
        NamedNode one = new NamedNode("http://names.kitodo.org/test#Object1");
        NamedNode another = new NamedNode("http://names.kitodo.org/test#Object1");

        assertTrue(one.hashCode() == another.hashCode());
    }

    @Test
    public void testHashCodeIsInequalForADifferentURI() {
        NamedNode one = new NamedNode("http://names.kitodo.org/test#Object1");
        NamedNode another = new NamedNode("http://names.kitodo.org/test#Object2");

        assertFalse(one.hashCode() == another.hashCode());
    }

    @Test
    public void testEqualsIfTheNameURIIsEqual() {
        NamedNode one = new NamedNode("http://names.kitodo.org/test#Object1");
        NamedNode another = new NamedNode("http://names.kitodo.org/test#Object1");

        assertTrue(one.equals(another));
    }

    @Test
    public void testEqualsNotIfTheNameURIIsDifferent() {
        NamedNode one = new NamedNode("http://names.kitodo.org/test#Object1");
        NamedNode another = new NamedNode("http://names.kitodo.org/test#Object2");

        assertFalse(one.equals(another));
    }

    @Test(expected = AssertionError.class)
    public void testEqualsIfTheNameURIIsEqualButTheContentIsDifferent() {
        try{
            assert(false);
            fail("Assertions disabled. Run JVM with -ea option.");
        }catch(AssertionError e){
            NamedNode one = new NamedNode("http://names.kitodo.org/test#Object1");
            NamedNode another = new NamedNode("http://names.kitodo.org/test#Object1");

            one.add(new Node("http://names.kitodo.org/test#Class1"));
            another.add(new NodeReference("http://names.kitodo.org/test#Object2"));

            one.equals(another);
        }
    }

    @Test
    public void testNamedNodeCanBeCreatedWithA_HTTP_URI() {
        new NamedNode("http://names.kitodo.org/test#Object1");
    }

    @Test
    public void testNamedNodeCanBeCreatedWithANonHTTP_URI() {
        new NamedNode("urn:example:kitodo:test-1234567-xyz");
    }

    @Test(expected = AssertionError.class)
    public void testNamedNodeCannotBeCreatedWithGarbage() {
        try{
            assert(false);
            fail("Assertions disabled. Run JVM with -ea option.");
        }catch(AssertionError e){
            new NamedNode("This isn’t a valid URI.");
        }

    }

    @Test
    public void testGetIdentifier() {
        assertEquals(
            "http://names.kitodo.org/test#Object1",
            new NamedNode("http://names.kitodo.org/test#Object1").getIdentifier()
        );
    }
}
