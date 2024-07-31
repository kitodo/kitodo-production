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

package org.kitodo.production.services.data;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.kitodo.production.services.ServiceManager;

public class UserServiceTest {

    private static final UserService userService = ServiceManager.getUserService();

    @Test
    public void shouldCheckIfIsLoginValid() {
        assertTrue(userService.isLoginValid("validLogin"), "Login is invalid!");

        assertFalse(userService.isLoginValid("root"), "Login is valid!");
    }
}
