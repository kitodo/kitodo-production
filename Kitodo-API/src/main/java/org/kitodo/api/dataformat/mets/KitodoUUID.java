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

package org.kitodo.api.dataformat.mets;

import java.math.BigInteger;
import java.util.UUID;

/**
 * Kitodo-style UUID. As UUIDs are used as XML IDs in METS, they must not start
 * with a digit, what they typically do. Therefore, the UUID is converted to a
 * sequence of letters A-Z.
 */
public class KitodoUUID {
    /**
     * Returns a UUID code that represents a hash of the given bytes.
     *
     * @return a hash code
     */
    public static String nameUUIDFromBytes(byte[] bytes) {
        return bigIntegerToLetters(uuidToBigInteger(UUID.nameUUIDFromBytes(bytes)));
    }

    /**
     * Returns a random UUID code.
     *
     * @return a random code
     */
    public static String randomUUID() {
        return bigIntegerToLetters(uuidToBigInteger(UUID.randomUUID()));
    }

    /**
     * Converts a hypen-spaced UUID to a {@code BigInteger}.
     *
     * @param uuid
     *            UUID to convert
     * @return big integer with the value of the UUID
     */
    private static BigInteger uuidToBigInteger(UUID uuid) {
        String glyphs = uuid.toString();
        StringBuilder buffer = new StringBuilder(32);
        buffer.append(glyphs, 0, 8);
        buffer.append(glyphs, 9, 13);
        buffer.append(glyphs, 14, 18);
        buffer.append(glyphs, 19, 23);
        buffer.append(glyphs, 24, 36);
        return new BigInteger(buffer.toString(), 16);
    }

    /**
     * Converts a big integer to a sequence of letters A-Z.
     *
     * @param toConvert
     *            big intereg to convert
     * @return letters for the integer value
     */
    private static String bigIntegerToLetters(BigInteger toConvert) {
        String sequence = toConvert.toString(26);
        StringBuilder buffer = new StringBuilder(sequence.length());
        final int length = sequence.length();
        for (int offset = 0; offset < length;) {
            int codePoint = sequence.codePointAt(offset);
            buffer.appendCodePoint(codePoint < 77 ? codePoint + 17 : codePoint - 22);
            offset += Character.charCount(codePoint);
        }
        return buffer.toString();
    }
}
