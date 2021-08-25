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

package org.kitodo.production.security.password;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.User;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordEncoderSwitch implements PasswordEncoder {

    @Override
    public String encode(CharSequence rawPassword) {
        SecurityPasswordEncoder legacyPasswordEncoder = new SecurityPasswordEncoder();
        legacyPasswordEncoder.setUser(user);
        return legacyPasswordEncoder.encrypt(rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        SecurityPasswordEncoder legacyPasswordEncoder = new SecurityPasswordEncoder();
        legacyPasswordEncoder.setUser(user);
        return legacyPasswordEncoder.encrypt(rawPassword.toString()).equals(encodedPassword);
    }

    private User user;

    private static final Logger logger = LogManager.getLogger(PasswordEncoderSwitch.class);

    /**
     * Sets the user.
     *
     * @param user
     *            user object
     */
    public void setUser(User user) {
        this.user = user;
    }
}
