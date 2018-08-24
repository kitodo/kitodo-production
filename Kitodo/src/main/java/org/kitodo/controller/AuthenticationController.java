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

package org.kitodo.controller;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.kitodo.security.DynamicAuthenticationProvider;

@Named("AuthenticationController")
@RequestScoped
public class AuthenticationController {
    private DynamicAuthenticationProvider dynamicAuthenticationProvider = DynamicAuthenticationProvider.getInstance();

    /**
     * Check if ldap authentication is active.
     * 
     * @return true if ldap authentication is active.
     */
    public boolean isLdapAuthentication() {
        return dynamicAuthenticationProvider.isLdapAuthentication();
    }

    /**
     * Sets ldap authentication.
     * 
     * @param ldapAuthentication
     *            the ldapAuthentication as boolean value
     */
    public void setLdapAuthentication(boolean ldapAuthentication) {
        dynamicAuthenticationProvider.setLdapAuthentication(ldapAuthentication);
    }
}
