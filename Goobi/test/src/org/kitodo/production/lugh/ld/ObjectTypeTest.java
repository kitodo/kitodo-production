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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ObjectTypeTest {

    @Test
    public void testNodeReferenceIsAnObjectType() {
        ObjectType ot = new NodeReference("http://test.example/foo");
        assertNotNull(ot);
    }

    @Test
    public void testNodeIsAnObjectType() {
        ObjectType ot = new Node();
        assertNotNull(ot);
    }

    @Test
    public void testNamedNodeIsAnObjectType() {
        ObjectType ot = new NamedNode("http://test.example/foo");
        assertNotNull(ot);
    }

    @Test
    public void testLiteralIsAnObjectType() {
        ObjectType ot = new Literal("abcdefghijklmnopqrstuvwxyz", RDF.PLAIN_LITERAL);
        assertNotNull(ot);
    }

    @Test
    public void testLangStringIsAnObjectType() {
        ObjectType ot = new LangString("Hello world!", "en");
        assertNotNull(ot);
    }

}
