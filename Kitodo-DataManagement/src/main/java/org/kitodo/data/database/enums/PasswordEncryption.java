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
    SHA(0, "SHA", "{SSHA}"),
    MD5(1, "MD5", "{SMD5}"),
    SHA_256(2, "SHA-256", "{SSHA-256}"),
    BCRYPT(3, "BCRYPT", "{BCRYPT}"),
    SCRYPT(4, "SCRYPT", "{SCRYPT}"),
    PBKDF2(5, "PBKDF2", "{PBKDF2}");

    private final int value;
    private final String title;
    private final String ldapPrefix;

    /**
     * Private constructor, initializes integer value, title and LDAP prefix.
     */
    PasswordEncryption(int value, String title, String ldapPrefix) {
        this.value = value;
        this.title = title;
        this.ldapPrefix = ldapPrefix;
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
     * Get LDAP prefix for salted password hash, per RFC 2307.
     *
     * @return LDAP prefix e.g. "{SSHA}", "{SMD5}", "{SSHA-256}"
     */
    public String getLdapPrefix() {
        return this.ldapPrefix;
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
