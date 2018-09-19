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

import java.util.NoSuchElementException;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.config.enums.ParameterAPI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldGetStringParameterWithoutDefault() {
        String param = Config.getParameter(ParameterAPI.DIR_XML_CONFIG);
        assertEquals("Incorrect param!", "String", param);
    }

    @Test
    public void shouldGetStringParameterWithDefault() {
        String param = Config.getParameter(ParameterAPI.DIR_XML_CONFIG, "test");
        assertEquals("Incorrect param!", "String", param);
    }

    @Test
    public void shouldGetStringParameterForNonexistentWithoutDefault() {
        expectedEx.expect(NoSuchElementException.class);
        expectedEx.expectMessage("No configuration found in kitodo_config.properties for key MetadatenVerzeichnis!");
        Config.getParameter(ParameterAPI.DIR_PROCESSES);
    }

    @Test
    public void shouldGetStringParameterForNonexistentWithDefault() {
        String param = Config.getParameter(ParameterAPI.DIR_PROCESSES, "Default");
        assertEquals("Incorrect param!", "Default", param);
    }

    @Test
    public void shouldGetBooleanParameter() {
        assertTrue("Incorrect param!", Config.getBooleanParameter(ParameterAPI.DIR_MODULES));
    }

    @Test
    public void shouldGetBooleanParameterForNonexistentWithoutDefault() {
        assertFalse("Incorrect param!", Config.getBooleanParameter(ParameterAPI.DIR_PROCESSES));
    }

    @Test
    public void shouldGetBooleanParameterForNonexistentWithDefault() {
        assertTrue("Incorrect param!", Config.getBooleanParameter(ParameterAPI.DIR_PROCESSES, true));
    }

    @Ignore("find a way to mock enum or add more enums")
    @Test
    public void shouldGetIntParameterWithoutDefault() {
        //int param = Config.getIntParameter("intParam");
        //assertEquals("Incorrect param!", 1, param);
    }

    @Ignore("find a way to mock enum or add more enums")
    @Test
    public void shouldGetIntParameterWithDefault() {
        //int param = Config.getIntParameter("intParam", 3);
        //assertEquals("Incorrect param!", 1, param);
    }

    @Test
    public void shouldGetIntParameterForNonexistentWithoutDefault() {
        int param = Config.getIntParameter(ParameterAPI.DIR_PROCESSES);
        assertEquals("Incorrect param!", 0, param);
    }

    @Test
    public void shouldGetIntParameterForNonexistentWithDefault() {
        int param = Config.getIntParameter(ParameterAPI.DIR_PROCESSES, 3);
        assertEquals("Incorrect param!", 3, param);
    }
}
