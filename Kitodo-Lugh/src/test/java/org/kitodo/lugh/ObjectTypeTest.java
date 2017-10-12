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

public class ObjectTypeTest {

    @Test
    public void testLangStringIsAnObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType ot = storage.createLangString("Hello world!", "en");
            assertNotNull(ot);
        }
    }

    @Test
    public void testLiteralIsAnObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType ot = storage.createLiteral("abcdefghijklmnopqrstuvwxyz", RDF.PLAIN_LITERAL);
            assertNotNull(ot);
        }
    }

    @Test
    public void testNamedNodeIsAnObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType ot = storage.createNamedNode("http://test.example/foo");
            assertNotNull(ot);
        }
    }

    @Test
    public void testNodeIsAnObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType ot = storage.createNode();
            assertNotNull(ot);
        }
    }

    @Test
    public void testNodeReferenceIsAnObjectType() {
        for (Storage storage : TestConfig.STORAGES_TO_TEST_AGAINST) {
            ObjectType ot = storage.createNodeReference("http://test.example/foo");
            assertNotNull(ot);
        }
    }

}
