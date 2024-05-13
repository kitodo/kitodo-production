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

package org.kitodo.production.helper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class HelperTest {

    @Test
    public void generateRandomStringOnlyLetters() {
        String randomString = Helper.generateRandomString(10);
        assertTrue(StringUtils.isAlpha(randomString), "Generated string contains not only letters");
    }

    @Test
    public void generateRandomStringOnlyNumbers() {
        String randomString = Helper.generateRandomString(10, false, true);
        assertTrue(StringUtils.isNumeric(randomString), "Generated string contains not only numbers");
    }

    @Test
    public void generateRandomStringLettersAndNumbers() {
        String randomString = Helper.generateRandomString(10, true, true);
        assertTrue(StringUtils.isAlphanumeric(randomString), "Generated string contains not only letters and numbers");
    }
}
