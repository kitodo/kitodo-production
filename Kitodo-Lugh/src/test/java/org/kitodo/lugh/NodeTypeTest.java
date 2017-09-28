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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class NodeTypeTest {

    @Test
    public void testNamedNodeIsANodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeType nt = storage.createNamedNode("http://test.example/foo");
            assertNotNull(nt);
        }
    }

    @Test
    public void testNodeIsANodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeType nt = storage.createNode();
            assertNotNull(nt);
        }
    }

    @Test
    public void testNodeReferenceIsANodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeType nt = storage.createNodeReference("http://test.example/foo");
            assertNotNull(nt);
        }
    }

}
