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

import org.kitodo.dataaccess.Storage;
import org.kitodo.dataaccess.storage.memory.MemoryStorage;

/**
 * Configure the test package here.
 */
public class TestConfig {

    /**
     * Storage instances to run the tests against.
     */
    static final Storage[] STORAGES_TO_TEST_AGAINST = {MemoryStorage.INSTANCE };
}
