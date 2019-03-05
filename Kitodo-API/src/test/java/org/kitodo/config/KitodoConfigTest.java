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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.kitodo.config.enums.ParameterAPI;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
public class KitodoConfigTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void shouldGetStringParameterWithoutDefault() {
        String param = KitodoConfig.getParameter(ParameterAPI.DIR_XML_CONFIG);
        assertEquals("Incorrect param!", "String", param);
    }

    @Test
    public void shouldGetStringParameterWithDefault() {
        String param = KitodoConfig.getParameter(ParameterAPI.DIR_XML_CONFIG, "test");
        assertEquals("Incorrect param!", "String", param);
    }

    @Test
    public void shouldGetStringParameterForNonexistentWithoutDefault() {
        expectedEx.expect(NoSuchElementException.class);
        expectedEx.expectMessage("No configuration found in kitodo_config.properties for key directory.metadata!");
        KitodoConfig.getParameter(ParameterAPI.DIR_PROCESSES);
    }

    @Test
    public void shouldGetStringParameterForNonexistentWithDefault() {
        String param = KitodoConfig.getParameter(ParameterAPI.DIR_PROCESSES, "Default");
        assertEquals("Incorrect param!", "Default", param);
    }

    @Test
    public void shouldGetBooleanParameter() {
        assertTrue("Incorrect param!", KitodoConfig.getBooleanParameter(ParameterAPI.DIR_MODULES));
    }

    @Test
    public void shouldGetBooleanParameterForNonexistentWithoutDefault() {
        assertFalse("Incorrect param!", KitodoConfig.getBooleanParameter(ParameterAPI.DIR_PROCESSES));
    }

    @Test
    public void shouldGetBooleanParameterForNonexistentWithDefault() {
        assertTrue("Incorrect param!", KitodoConfig.getBooleanParameter(ParameterAPI.DIR_PROCESSES, true));
    }

    @Test
    @PrepareForTest(ParameterAPI.class)
    public void shouldGetIntParameterWithoutDefault() {
        ParameterAPI NONE = PowerMockito.mock(ParameterAPI.class);
        Whitebox.setInternalState(NONE, "name", "NONE");
        Whitebox.setInternalState(NONE, "ordinal", 3);

        PowerMockito.mockStatic(ParameterAPI.class);
        PowerMockito.when(ParameterAPI.values())
                .thenReturn(new ParameterAPI[] {ParameterAPI.DIR_MODULES, ParameterAPI.DIR_PROCESSES,
                                                ParameterAPI.DIR_XML_CONFIG, NONE });

        int param = KitodoConfig.getIntParameter(NONE);
        assertEquals("Incorrect param for non existing enum without default value!", 0, param);
    }

    @Test
    @PrepareForTest(ParameterAPI.class)
    public void shouldGetIntParameterWithDefault() {
        ParameterAPI NONE = PowerMockito.mock(ParameterAPI.class);
        Whitebox.setInternalState(NONE, "name", "NONE");
        Whitebox.setInternalState(NONE, "ordinal", 3);

        PowerMockito.mockStatic(ParameterAPI.class);
        PowerMockito.when(ParameterAPI.values())
                .thenReturn(new ParameterAPI[] {ParameterAPI.DIR_MODULES, ParameterAPI.DIR_PROCESSES,
                        ParameterAPI.DIR_XML_CONFIG, NONE });

        int param = KitodoConfig.getIntParameter(NONE, 3);
        assertEquals("Incorrect param for non existing enum with default value!", 3, param);
    }

    @Test
    public void shouldGetIntParameterForNonexistentWithoutDefault() {
        int param = KitodoConfig.getIntParameter(ParameterAPI.DIR_PROCESSES);
        assertEquals("Incorrect param!", 0, param);
    }

    @Test
    public void shouldGetIntParameterForNonexistentWithDefault() {
        int param = KitodoConfig.getIntParameter(ParameterAPI.DIR_PROCESSES, 3);
        assertEquals("Incorrect param!", 3, param);
    }
}
