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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.kitodo.dataaccess.NodeType;
import org.kitodo.dataaccess.Storage;

public class NodeTypeTest {

    @Test
    public void testNamedNodeIsANodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeType nt = storage.createNamedNode("http://test.example/foo");
            assertThat(nt, is(notNullValue()));
        }
    }

    @Test
    public void testNodeIsANodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeType nt = storage.createNode();
            assertThat(nt, is(notNullValue()));
        }
    }

    @Test
    public void testNodeReferenceIsANodeType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            NodeType nt = storage.createNodeReference("http://test.example/foo");
            assertThat(nt, is(notNullValue()));
        }
    }

}
