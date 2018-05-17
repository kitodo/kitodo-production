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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConfigTest {

    @Test
    public void shouldGetStringParameter() {
        String param = Config.getParameter("stringParam");
        assertEquals("Incorrect param!", param, "String");
    }

    @Test
    public void shouldGetStringParameterForNonexistentWithoutDefault() {
        String param = Config.getParameter("noStringParam");
        assertEquals("Incorrect param!", param, null);
    }

    @Test
    public void shouldGetStringParameterForNonexistentWithDefault() {
        String param = Config.getParameter("noStringParam", "Default");
        assertEquals("Incorrect param!", param, "Default");
    }

    @Test
    public void shouldGetBooleanParameter() {
        assertTrue("Incorrect param!", Config.getBooleanParameter("booleanParam"));
    }

    @Test
    public void shouldGetBooleanParameterForNonexistentWithoutDefault() {
        assertFalse("Incorrect param!", Config.getBooleanParameter("noBooleanParam"));
    }

    @Test
    public void shouldGetBooleanParameterForNonexistentWithDefault() {
        assertTrue("Incorrect param!", Config.getBooleanParameter("noBooleanParam", true));
    }

    @Test
    public void shouldGetIntParameterWithoutDefault() {
        int param = Config.getIntParameter("intParam");
        assertEquals("Incorrect param!", param, 1);
    }

    @Test
    public void shouldGetIntParameterWithDefault() {
        int param = Config.getIntParameter("intParam", 3);
        assertEquals("Incorrect param!", param, 1);
    }

    @Test
    public void shouldGetIntParameterForNonexistentWithoutDefault() {
        int param = Config.getIntParameter("noIntParam");
        assertEquals("Incorrect param!", param, 0);
    }

    @Test
    public void shouldGetIntParameterForNonexistentWithDefault() {
        int param = Config.getIntParameter("noIntParam", 3);
        assertEquals("Incorrect param!", param, 3);
    }
}
