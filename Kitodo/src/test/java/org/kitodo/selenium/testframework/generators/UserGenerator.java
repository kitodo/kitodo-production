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

import java.security.SecureRandom;

import org.apache.commons.lang3.RandomStringUtils;
import org.kitodo.data.database.beans.User;

public class UserGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Create user with random name, surname, login and password.
     *
     * @return Created user.
     */
    public static User generateUser() {
        String suffix = generateRandomString(5);

        User user = new User();
        // sometimes generated password doesn't contain number so it is added here explicitly
        user.setPassword("P1_" + generateRandomString(10));
        user.setName("Name" + suffix);
        user.setSurname("Surname" + suffix);
        user.setLogin("UserLogin" + suffix);
        user.setLocation("MockLocation");

        return user;
    }

    /**
     * Create a random string with a defined length.
     *
     * @param length How long the to be created string should be
     * @return Created string with random values.
     */
    private static String generateRandomString(int length) {
        // RandomStringUtils is using a non-secure random generator by default
        // call random method with all parameters to set a secure random generator
        return RandomStringUtils.random(length, 0, 0, true, true, null, secureRandom);
    }
}
