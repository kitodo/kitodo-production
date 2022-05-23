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

import java.util.Collection;

import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

/**
 * This class is used for mapping a UserDetails object from an ldap authentication context.
 */
public class LdapUserDetailsContextMapper extends LdapUserDetailsMapper implements UserDetailsContextMapper {

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        User user = ServiceManager.getUserService().getByLdapLoginOrLogin(username);
        SecurityLdapUserDetails securityLdapUserDetails = new SecurityLdapUserDetails(user);
        securityLdapUserDetails.setDn(ctx.getDn().toString());
        return securityLdapUserDetails;
    }
}
