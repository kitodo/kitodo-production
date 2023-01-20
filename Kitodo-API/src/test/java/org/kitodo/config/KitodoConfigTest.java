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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.config.enums.ParameterAPI;

public class KitodoConfigTest {

    private static ParameterAPI NONE;

    /**
     * Init once before tests.
     *
     * @throws Exception the exceptions thrown by method
     */
    @BeforeClass
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
        assertEquals("Incorrect param!", "String", param);
    }

    @Test
    public void shouldGetStringParameterWithDefault() {
        String param = KitodoConfig.getParameter(ParameterAPI.DIR_XML_CONFIG, "test");
        assertEquals("Incorrect param!", "String", param);
    }

    @Test
    public void shouldGetStringParameterForNonexistentWithoutDefault() {
        NoSuchElementException thrown = assertThrows(NoSuchElementException.class,
            () -> KitodoConfig.getParameter(ParameterAPI.DIR_PROCESSES),
            "Expected NoSuchElementException to be thrown, but it didn't");

        assertEquals("No configuration found in kitodo_config.properties for key directory.metadata!",
            thrown.getMessage());
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
    public void shouldGetIntParameterWithoutDefault() {
        int param = KitodoConfig.getIntParameter(NONE);
        assertEquals("Incorrect param for non existing enum without default value!", 0, param);
    }

    @Test
    public void shouldGetIntParameterWithDefault() {
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
