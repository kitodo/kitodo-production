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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

public class ConfigTest {

    /**
     * Simple test for getting the PropertiesConfiguration for an existing property file.
     */
    @Test
    public void getPropertiesConfiguration() {
        PropertiesConfiguration config = Config.getConfig("kitodo_config.properties");
        assertNotNull(config, "PropertyConfiguration should not be null");
    }

    /**
     * Test for comparing two different property files to get two different PropertyConfiguration objects.
     */
    @Test
    public void propertiesConfigurationAreNotEqual() {
        PropertiesConfiguration configOne = Config.getConfig("kitodo_config.properties");
        PropertiesConfiguration configTwo = Config.getConfig("kitodo_config_test.properties");

        assertNotEquals(configOne, configTwo, "Different property files should have different PropertiesConfiguration objects");
    }

    /**
     * Test for proving that the same property file is connected to same object even with a loading a different property file between.
     */
    @Test
    public void sameConfigurationFileShouldHaveSamePropertiesConfigurationObject() {
        PropertiesConfiguration configOne = Config.getConfig("kitodo_config.properties");
        PropertiesConfiguration configTwo = Config.getConfig("kitodo_config_test.properties");
        PropertiesConfiguration configThree = Config.getConfig("kitodo_config.properties");

        assertEquals(configOne, configThree, "Same property file should have same PropertiesConfiguration object");
        assertNotEquals(configTwo, configThree, "Different property files should have different PropertiesConfiguration objects");
    }
}
