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

import java.util.UUID;

/**
 * Kitodo-style UUID. As UUIDs are used as XML IDs in METS, they must not start
 * with a digit, what they typically do. Therefore, the UUID is converted to a
 * sequence of letters A-Z.
 */
public class KitodoUUID {

    private static final String UUID_PREFIX = "uuid-";

    /**
     * Returns a UUID code that represents a hash of the given bytes.
     *
     * @return a hash code
     */
    public static String nameUUIDFromBytes(byte[] bytes) {
        return UUID_PREFIX.concat(UUID.nameUUIDFromBytes(bytes).toString());
    }

    /**
     * Returns a random UUID code.
     *
     * @return a random code
     */
    public static String randomUUID() {
        return UUID_PREFIX.concat(UUID.randomUUID().toString());
    }
}
