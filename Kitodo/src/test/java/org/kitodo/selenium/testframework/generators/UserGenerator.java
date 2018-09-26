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

import org.apache.commons.lang3.RandomStringUtils;
import org.kitodo.data.database.beans.User;

public class UserGenerator {

    public static User generateUser() {
        String suffix = generateRandomString(5);

        User user = new User();
        // sometimes generated password doesn't contain number so it is added here explicitly
        user.setPassword("P1_" + generateRandomString(10));
        user.setName("Name" + suffix);
        user.setSurname("Surname" + suffix);
        user.setLogin("UserLogin" + suffix);
        user.setLocation("MockLocation");
        user.setMetadataLanguage("de");

        return user;
    }

    private static String generateRandomString(int length) {
        return RandomStringUtils.random(length, true, true);
    }
}
