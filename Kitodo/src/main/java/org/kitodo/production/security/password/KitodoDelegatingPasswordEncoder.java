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

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Implementation of a spring security delegating password encoder, which is able to encode and check 
 * passwords that were previously encoded via legacy formats. Currently, there are two formats:
 * 
 * <ul>
 *   <li>null - the legacy format that was not stored with this delegating password encoder and has no algorithm prefix "{something}"</li>
 *   <li>argon2 - the current password encryption algorithm stored with the prefix "{argon2}"</li>
 * </ul>
 * 
 * <p>In the future, if passwords need to be migrated to a more secure encryption algorithm, additional formats can be added.</p>
 * 
 * <p>In contrast to the legacy encoder, this password encoder doesn't support recovering plaintext passwords. Instead, any 
 * tasks that require access to the plaintext password need to be either scheduled as tasks that are performed at the next login, 
 * or interactively ask the user to enter the password again.</p>
 */
public class KitodoDelegatingPasswordEncoder extends DelegatingPasswordEncoder {

    private static final String LEGACY_ENCODER_ID = null;
    private static final String ARGON2_ENCODER_ID = "argon2";


    /**
     * Initialize a new delegating password encoder with encoding strategies supported by Kitodo.Production.
     */
    public KitodoDelegatingPasswordEncoder() {
        // register default encoder (argon2), which all legacy passwords are upgraded too
        // and all current and legacy encoder implementations
        super(ARGON2_ENCODER_ID, getIdToEncoderMap());
    }

    /**
     * Return the map of supported password encoders and their ids (used as prefixes).
     * 
     * @return the map of password encoders and their ids
     */
    private static Map<String, PasswordEncoder> getIdToEncoderMap() {
        Map<String, PasswordEncoder> map = new HashMap<>();
        map.put(LEGACY_ENCODER_ID, new KitodoLegacyPasswordEncoder());
        map.put(ARGON2_ENCODER_ID, Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        return map;
    }
    
}
