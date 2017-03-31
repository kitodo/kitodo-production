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

package de.sub.kitodo.helper.encryption;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

public class MD5Test {
    static HashMap<String, String> testData;

    static {
        testData = new HashMap<String, String>();
        testData.put("Test", "0cbc6611f5540bd0809a388dc95a615b");
        testData.put("Password", "dc647eb65e6711e155375218212b3964");
        testData.put("12345678", "25d55ad283aa400af464c76d713c07ad");
        testData.put("GoobiPassword1234*./", "8480dc5afeaadcfc3114ea22e38e3412");
        testData.put("AreallyreallyreallylongPassword", "84b23a5fc3b6f0a275d32c28dbb28478");
        testData.put("$%!--_-_/*-äöüä", "57fe2c6b74dcedd667234f1955aea362");
    }

    @Test
    public void encryptTest() {
        MD5 md5 = new MD5("");
        for (String clearText : testData.keySet()) {
            String encrypted = md5.getMD5(clearText);
            assertTrue("Encrypted password doesn't match the precomputed one!",
                    encrypted != null && encrypted.equals(testData.get(clearText)));
        }
    }
}
