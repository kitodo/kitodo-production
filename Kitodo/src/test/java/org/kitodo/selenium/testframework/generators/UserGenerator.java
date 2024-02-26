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

package org.kitodo.selenium.testframework.generators;

import org.kitodo.data.database.beans.User;
import org.kitodo.production.helper.Helper;

public class UserGenerator {

    /**
     * Create user with random name, surname, login and password.
     *
     * @return Created user.
     */
    public static User generateUser() {
        String suffix = Helper.generateRandomString(5, true, true);

        User user = new User();
        // sometimes generated password doesn't contain number so it is added here explicitly
        user.setPassword("P1_" + Helper.generateRandomString(10, true, true));
        user.setName("Name" + suffix);
        user.setSurname("Surname" + suffix);
        user.setLogin("UserLogin" + suffix);
        user.setLocation("MockLocation");

        return user;
    }
}
