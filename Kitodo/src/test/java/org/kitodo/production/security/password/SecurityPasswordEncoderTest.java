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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class SecurityPasswordEncoderTest {
    private static Map<String, String> testData;

    static {
        testData = new HashMap<>();
        testData.put("Password", "6lPEb6Gic+/7BNRdMmL1qQ==");
        testData.put("12345678", "wkQy7f152Zl422PTPOPAMQ==");
        testData.put("GoobiPassword1234*./", "nDI2cSug5Nj/kkEvKQPBOsHjdTofLmaJ");
        testData.put("AreallyreallyreallylongPassword", "89DIASbZ9PNGN132djaFlfVXNo7V3DgBeFCEZG2WmSM=");
        testData.put("$%!--_-_/*-äöüä", "/wFe+pyc/QQTmhxAZcjSt9mkwrv03udL");
    }

    @Test
    public void encryptTest() {
        for (String clearText : testData.keySet()) {
            String encrypted = new SecurityPasswordEncoder().encrypt(clearText);
            assertEquals("Encrypted Password doesn't match the precomputed one!", testData.get(clearText),
                    encrypted);
        }
    }

    @Test
    public void decryptTest() {
        for (String clearText : testData.keySet()) {
            String decrypted = new SecurityPasswordEncoder().decrypt(testData.get(clearText));
            assertEquals("Decrypted Password doesn't match the given plain text", clearText, decrypted);
        }
    }

}
