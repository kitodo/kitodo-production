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

import org.junit.Test;
import org.kitodo.lugh.vocabulary.RDF;

public class IdentifiableNodeTest {
    @Test
    public void testGetIdentifierFromANamedNode() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            assertEquals("http://kitodo.org/",
                    ((IdentifiableNode) storage.createNamedNode("http://kitodo.org/")).getIdentifier());
        }
    }

    @Test
    public void testGetIdentifierFromANodeReference() {
        assertEquals("http://www.w3.org/1999/02/22-rdf-syntax-ns#value",
                ((IdentifiableNode) RDF.VALUE).getIdentifier());
    }
}
