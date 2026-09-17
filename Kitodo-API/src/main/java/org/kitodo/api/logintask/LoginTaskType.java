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

package org.kitodo.api.logintask;

public enum LoginTaskType {

    /**
     * Saves user credentials to the configured LDAP server.
     */
    SAVE_USER_TO_LDAP,

    // in the future, add more login task types, e.g. to force a password reset, setup 2fa, accept terms and conditions, etc.

}
