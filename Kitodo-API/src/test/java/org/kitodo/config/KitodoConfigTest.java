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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.config.enums.ParameterAPI;

public class KitodoConfigTest {

    private static ParameterAPI NONE;

    /**
     * Init once before tests.
     *
     * @throws Exception the exceptions thrown by method
     */
    @BeforeAll
    public static void init() {
        NONE = mock(ParameterAPI.class);
        doReturn(3).when(NONE).ordinal();

        mockStatic(ParameterAPI.class);
        when(ParameterAPI.values())
                .thenReturn(new ParameterAPI[] {ParameterAPI.DIR_MODULES, ParameterAPI.DIR_PROCESSES,
                                                ParameterAPI.DIR_XML_CONFIG, NONE });
    }

    @Test
    public void shouldGetStringParameterWithoutDefault() {
        String param = KitodoConfig.getParameter(ParameterAPI.DIR_XML_CONFIG);
        assertEquals("String", param, "Incorrect param!");
    }

    @Test
    public void shouldGetStringParameterWithDefault() {
        String param = KitodoConfig.getParameter(ParameterAPI.DIR_XML_CONFIG, "test");
        assertEquals("String", param, "Incorrect param!");
    }

    @Test
    public void shouldGetStringParameterForNonexistentWithoutDefault() {
        NoSuchElementException thrown = assertThrows(NoSuchElementException.class,
            () -> KitodoConfig.getParameter(ParameterAPI.DIR_PROCESSES),
            "Expected NoSuchElementException to be thrown, but it didn't");

        assertEquals("No configuration found in kitodo_config.properties for key directory.metadata!", thrown.getMessage());
    }

    @Test
    public void shouldGetStringParameterForNonexistentWithDefault() {
        String param = KitodoConfig.getParameter(ParameterAPI.DIR_PROCESSES, "Default");
        assertEquals("Default", param, "Incorrect param!");
    }

    @Test
    public void shouldGetBooleanParameter() {
        assertTrue(KitodoConfig.getBooleanParameter(ParameterAPI.DIR_MODULES), "Incorrect param!");
    }

    @Test
    public void shouldGetBooleanParameterForNonexistentWithoutDefault() {
        assertFalse(KitodoConfig.getBooleanParameter(ParameterAPI.DIR_PROCESSES), "Incorrect param!");
    }

    @Test
    public void shouldGetBooleanParameterForNonexistentWithDefault() {
        assertTrue(KitodoConfig.getBooleanParameter(ParameterAPI.DIR_PROCESSES, true), "Incorrect param!");
    }

    @Test
    public void shouldGetIntParameterWithoutDefault() {
        int param = KitodoConfig.getIntParameter(NONE);
        assertEquals(0, param, "Incorrect param for non existing enum without default value!");
    }

    @Test
    public void shouldGetIntParameterWithDefault() {
        int param = KitodoConfig.getIntParameter(NONE, 3);
        assertEquals(3, param, "Incorrect param for non existing enum with default value!");
    }

    @Test
    public void shouldGetIntParameterForNonexistentWithoutDefault() {
        int param = KitodoConfig.getIntParameter(ParameterAPI.DIR_PROCESSES);
        assertEquals(0, param, "Incorrect param!");
    }

    @Test
    public void shouldGetIntParameterForNonexistentWithDefault() {
        int param = KitodoConfig.getIntParameter(ParameterAPI.DIR_PROCESSES, 3);
        assertEquals(3, param, "Incorrect param!");
    }
}
