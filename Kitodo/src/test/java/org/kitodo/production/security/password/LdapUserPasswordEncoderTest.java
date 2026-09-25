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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.enums.PasswordEncryption;

public class LdapUserPasswordEncoderTest {

    /**
     * Check that a fixed password is correctly hashed with MD5.
     */
    @Test 
    public void testEncodeWithMdFive() throws Exception {
        assertEquals("{MD5}siuNQnIxR+3lEENyqa3jng==", LdapUserPasswordEncoder.encodeMdFive("Test$234"));
    }

    /**
     * Check that a fixed password is correctly hashed with SHA-1.
     */
    @Test 
    public void testEncodeWithShaOne() throws Exception {
        assertEquals("{SHA}24fsAgg0LcpebG97xV+WoPojnnM=", LdapUserPasswordEncoder.encodeShaOne("Test$234"));
    }

    /**
     * Check that salted methods do return different password encodings for the same password.
     */
    @Test
    public void testEncodeWithSaltedMethods() throws Exception {
        List<PasswordEncryption> saltedMethods = List.of(PasswordEncryption.SSHA, PasswordEncryption.CRYPT_5, PasswordEncryption.CRYPT_6);

        for (PasswordEncryption method : saltedMethods) {
            String hashOne = LdapUserPasswordEncoder.encode(method, "Test$234");
            String hashTwo = LdapUserPasswordEncoder.encode(method,"Test$234");
            assertNotEquals(hashOne, hashTwo);
        }   
    }

}
