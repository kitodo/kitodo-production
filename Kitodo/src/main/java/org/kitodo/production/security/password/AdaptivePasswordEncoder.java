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

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class AdaptivePasswordEncoder {

    private static final BCryptPasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder(16);

    public String hash(String rawPassword) {
        return BCRYPT_ENCODER.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return BCRYPT_ENCODER.matches(rawPassword, encodedPassword);
    }
}
