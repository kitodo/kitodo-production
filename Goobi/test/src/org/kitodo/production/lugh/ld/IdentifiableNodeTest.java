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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IdentifiableNodeTest {

    @Test
    public void testGetIdentifierFromANodeReference() {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#value",
                ((IdentifiableNode) RDF.VALUE).getIdentifier());
    }

    @Test
    public void testGetIdentifierFromANamedNode() {
        assertEquals("http://kitodo.org/", ((IdentifiableNode) new NamedNode("http://kitodo.org/")).getIdentifier());
    }

}
