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

package org.kitodo.security;

import java.util.Collection;

import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

public class LdapUserDetailsContextMapper extends LdapUserDetailsMapper implements UserDetailsContextMapper {

    private ServiceManager serviceManager = new ServiceManager();

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        try {
            User user = serviceManager.getUserService().getByLdapLoginWithFallback(username);
            SecurityLdapUserDetails securityLdapUserDetails = new SecurityLdapUserDetails(user);
            securityLdapUserDetails.setDn(ctx.getDn().toString());
            return securityLdapUserDetails;
        } catch (DAOException e) {
            throw new UsernameNotFoundException("Error on reading database while mapping user information from context");
        }
    }
}
