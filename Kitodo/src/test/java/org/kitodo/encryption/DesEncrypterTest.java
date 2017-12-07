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

package org.kitodo.encryption;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.kitodo.security.SecurityPasswordEncoder;

public class DesEncrypterTest {
    static Map<String, String> testData;

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
            assertTrue("Encrypted Password doesn't match the precomputed one!",
                    encrypted.equals(testData.get(clearText)));
        }
    }

    @Test
    public void decryptTest() {
        for (String clearText : testData.keySet()) {
            String decrypted = new SecurityPasswordEncoder().decrypt(testData.get(clearText));
            assertTrue("Decrypted Password doesn't match the given plain text", decrypted.equals(clearText));
        }
    }

}
