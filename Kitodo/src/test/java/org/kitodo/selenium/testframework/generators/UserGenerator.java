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
        User user = new User();
        user.setPassword(generatePassword());
        user.setName("MockName");
        user.setSurname("MockSurname");
        user.setLogin("MockUserLogin");
        user.setLocation("MockLocation");
        user.setMetadataLanguage("de");

        return user;
    }

    private static String generatePassword() {
        int length = 10;
        return RandomStringUtils.random(length, true, false);
    }
}
