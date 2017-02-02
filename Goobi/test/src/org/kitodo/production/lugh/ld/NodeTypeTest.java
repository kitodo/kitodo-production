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

public class NodeTypeTest {

    @Test
    public void testNodeReferenceIsANodeType() {
        NodeType nt = new NodeReference("http://test.example/foo");
        assertNotNull(nt);
    }

    @Test
    public void testNodeIsANodeType() {
        NodeType nt = new Node();
        assertNotNull(nt);
    }

    @Test
    public void testNamedNodeIsANodeType() {
        NodeType nt = new NamedNode("http://test.example/foo");
        assertNotNull(nt);
    }

}
