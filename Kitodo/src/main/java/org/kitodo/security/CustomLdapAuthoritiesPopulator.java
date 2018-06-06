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

import org.kitodo.services.ServiceManager;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;

public class CustomLdapAuthoritiesPopulator implements LdapAuthoritiesPopulator {

    private ServiceManager serviceManager = new ServiceManager();

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData,
            String username) {

        UserDetails user = serviceManager.getUserService().loadUserByUsername(username);
        return user.getAuthorities();
    }
}
