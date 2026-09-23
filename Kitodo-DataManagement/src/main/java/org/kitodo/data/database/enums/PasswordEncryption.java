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

package org.kitodo.data.database.enums;

/**
 * Enum for type of password encryption, each one with integer value for
 * database.
 */
public enum PasswordEncryption {
    SHA(0, "SHA-1"),
    MD5(1, "MD5"),

    // Salted SHA-1, see https://man7.org/linux/man-pages/man5/slapd.conf.5.html
    SSHA(2, "SHA-1 with Salt"),

    // Salted SHA-256 via Crypt, see https://man7.org/linux/man-pages/man3/crypt.3.html
    CRYPT_5(3, "SHA-256 with Salt (Linux only, libcrypt required)"),

    // Salted SHA-512 via Crypt, see https://man7.org/linux/man-pages/man3/crypt.3.html
    CRYPT_6(4, "SHA-512 with Salt (Linux only, libcrypt required)");

    private final int value;
    private final String title;

    /**
     * Private constructor, initializes integer value.
     */
    PasswordEncryption(int value, String title) {
        this.value = value;
        this.title = title;
    }

    /**
     * Return integer value for database savings.
     *
     * @return value as integer
     */
    public Integer getValue() {
        return this.value;
    }

    /**
     * Get title from password encryption.
     *
     * @return title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Retrieve password encryption by integer value, necessary for database
     * handlings, where only integer is saved but not type safe.
     *
     * @param value
     *            as integer value
     * @return {@link PasswordEncryption} for given integer
     */
    public static PasswordEncryption getEncryptionFromValue(Integer value) {
        if (value != null) {
            for (PasswordEncryption passwordEncryption : values()) {
                if (passwordEncryption.getValue() == value.intValue()) {
                    return passwordEncryption;
                }
            }
        }
        return SHA;
    }

}
