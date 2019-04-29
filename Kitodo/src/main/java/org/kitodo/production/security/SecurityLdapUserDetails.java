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

package org.kitodo.production.security;

import org.kitodo.data.database.beans.User;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

/**
 * This Class is an extension for the SecurityUserDetails class and is used when user authentication is made against ldap.
 */
public class SecurityLdapUserDetails extends SecurityUserDetails implements LdapUserDetails {

    private String dn;

    SecurityLdapUserDetails(final User user) {
        super(user);
    }

    @Override
    public String getDn() {
        return dn;
    }

    /**
     * Sets dn.
     *
     * @param dn The dn.
     */
    public void setDn(String dn) {
        this.dn = dn;
    }

    @Override
    public void eraseCredentials() {
        super.setPassword("*PROTECTED*");
    }
}
