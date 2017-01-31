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

public class NodeReferenceTest {

    @Test
    public void testHashCodeIsEqualForTwoEqualInstances() {
        NodeReference one = new NodeReference("http://names.example/foo");
        NodeReference other = new NodeReference("http://names.example/foo");

        assertTrue(one.hashCode() == other.hashCode());
    }

    @Test
    public void testHashCodeDiffersForTwoDifferentInstances() {
        NodeReference one = new NodeReference("http://names.example/foo");
        NodeReference other = new NodeReference("http://names.example/bar");

        assertFalse(one.hashCode() == other.hashCode());
    }

    @Test
    public void testNodeReferenceCanBeCreatedFromHTTP_URL() {
        new NodeReference("http://names.example/foo#bar");
    }

    @Test
    public void testNodeReferenceCanBeCreatedFromNonHTTP_URL() {
        new NodeReference("urn:example:kitodo-123456789-xyz");
    }

    @Test
    public void testNodeReferenceCannotBeCreatedFromGarbage() {
        new NodeReference("This isn’t an URI.");
    }

    @Test
    public void testNodeReferenceCannotBeCreatedFromEmptyString() {
        new NodeReference("");
    }

    @Test
    public void testNodeReferenceCannotBeCreatedFromNullReference() {
        new NodeReference(null);
    }

    @Test
    public void testEqualsObject() {
        NodeReference one = new NodeReference("http://names.example/foo");
        NodeReference other = new NodeReference("http://names.example/foo");

        assertTrue(one.equals(other));
    }

    @Test
    public void testEqualsIsFalseForDifferentObjects() {
        NodeReference one = new NodeReference("http://names.example/foo");
        NodeReference other = new NodeReference("http://names.example/bar");

        assertFalse(one.equals(other));
    }

    @Test
    public void testGetIdentifier() {
        assertEquals("http://names.example/foo", new NodeReference("http://names.example/foo").getIdentifier());
    }
}
