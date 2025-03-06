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

package org.kitodo.config.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ParameterCoreTest {

    @Test
    public void shouldGetParameterWithoutDefaultValueTest() {
        assertNull(ParameterCore.DIR_USERS.getParameter().getDefaultValue(), "Default value for param exists!");
    }

    @Test
    public void shouldOverrideToStringWithParameterKeyTests() {
        ParameterCore parameterCore = ParameterCore.DIR_USERS;
        assertEquals(parameterCore.getParameter().getKey(), String.valueOf(parameterCore), "Methods toString() was not overridden!");
    }
}
