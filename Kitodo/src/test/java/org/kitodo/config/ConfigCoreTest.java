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

package org.kitodo.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ConfigCoreTest {

    @Test
    public void shouldGetKitodoConfigDirectory() {
        String expected = "src/test/resources/";

        assertEquals(expected, ConfigCore.getKitodoConfigDirectory(), "Directory was queried incorrectly!");
    }

    @Test
    public void shouldGetKitodoDataDirectory() {
        String expected = "src/test/resources/metadata/";

        assertEquals(expected, ConfigCore.getKitodoDataDirectory(), "Directory was queried incorrectly!");
    }
}
