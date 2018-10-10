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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ParameterCoreTest {

    @Test
    public void shouldGetParameterWithoutDefaultValueTest() {
        assertNull("Default value for param exists!",
            ParameterCore.DIR_USERS.getParameter().getDefaultValue());
    }

    @Test
    public void shouldGetParameterWithDefaultValueTest() {
        assertEquals("Default value for param doesn't exist!", 1800000L,
            ParameterCore.METS_EDITOR_LOCKING_TIME.getParameter().getDefaultValue());
    }

    @Test
    public void shouldOverrideToStringWithParameterKeyTests() {
        ParameterCore parameterCore = ParameterCore.DIR_USERS;
        assertEquals("Methods toString() was not overridden!", parameterCore.getParameter().getKey(),
                String.valueOf(parameterCore));
    }
}
