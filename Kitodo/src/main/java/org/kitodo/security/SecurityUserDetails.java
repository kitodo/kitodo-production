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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.services.ServiceManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SecurityUserDetails extends User implements UserDetails {

    private static final long serialVersionUID = 2950419497162715796L;

    /**
     * The client which was selected by user after login.
     */
    private Client sessionClient;

    private ServiceManager serviceManager = new ServiceManager();

    public SecurityUserDetails(final User user) {
        super(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<UserGroup> userGroups = super.getUserGroups();
        List<Client> clients = super.getClients();
        List<Project> projects = super.getProjects();
        List<SimpleGrantedAuthority> userAuthorities = new ArrayList<>();

        for (UserGroup userGroup : userGroups) {
            List<Authority> authorities = userGroup.getAuthorities();
            for (Authority authority : authorities) {
                if (authority.getTitle().contains(serviceManager.getAuthorityService().getGlobalAuthoritySuffix())) {
                    insertGlobalAuthorities(userAuthorities, authority);
                }
                if (authority.getTitle().contains(serviceManager.getAuthorityService().getClientAuthoritySuffix())) {
                    insertClientAuthorities(userAuthorities, authority, clients);
                }
                if (authority.getTitle().contains(serviceManager.getAuthorityService().getProjectAuthoritySuffix())) {
                    insertProjectAuthorities(userAuthorities, authority, projects);
                }
            }
        }
        return userAuthorities;
    }

    private void insertGlobalAuthorities(List<SimpleGrantedAuthority> userAuthorities, Authority authority) {
        String authorityTitle = authority.getTitle()
            .replace(serviceManager.getAuthorityService().getGlobalAuthoritySuffix(), "");
        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authorityTitle + "_GLOBAL");
        if (!userAuthorities.contains(simpleGrantedAuthority)) {
            userAuthorities.add(simpleGrantedAuthority);
        }
    }

    private void insertClientAuthorities(List<SimpleGrantedAuthority> userAuthorities, Authority authority,
            List<Client> clients) {
        for (Client client : clients) {
            String authorityTitle = authority.getTitle()
                    .replace(serviceManager.getAuthorityService().getClientAuthoritySuffix(), "");

            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authorityTitle + "_CLIENT_ANY");
            if (!userAuthorities.contains(simpleGrantedAuthority)) {
                userAuthorities.add(simpleGrantedAuthority);
            }
            SimpleGrantedAuthority simpleGrantedAuthorityWithId = new SimpleGrantedAuthority(
                    authorityTitle + "_CLIENT_" + client.getId());
            if (!userAuthorities.contains(simpleGrantedAuthorityWithId)) {
                userAuthorities.add(simpleGrantedAuthorityWithId);
            }
        }
    }

    private void insertProjectAuthorities(List<SimpleGrantedAuthority> userAuthorities,
                                          Authority authority, List<Project> projects) {
        for (Project project : projects) {
            String authorityTitle = authority.getTitle()
                    .replace(serviceManager.getAuthorityService().getProjectAuthoritySuffix(), "");

            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authorityTitle + "_PROJECT_ANY");
            if (!userAuthorities.contains(simpleGrantedAuthority)) {
                userAuthorities.add(simpleGrantedAuthority);
            }
            SimpleGrantedAuthority simpleGrantedAuthorityWithId = new SimpleGrantedAuthority(
                    authorityTitle + "_PROJECT_" + project.getId());
            if (!userAuthorities.contains(simpleGrantedAuthorityWithId)) {
                userAuthorities.add(simpleGrantedAuthorityWithId);
            }
        }
    }

    /**
     * Gets sessionClient.
     *
     * @return The sessionClient.
     */
    public Client getSessionClient() {
        return sessionClient;
    }

    /**
     * Sets sessionClient.
     *
     * @param sessionClient
     *            The sessionClient.
     */
    public void setSessionClient(Client sessionClient) {
        this.sessionClient = sessionClient;
    }

    @Override
    public String getUsername() {
        return super.getLogin();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !super.isDeleted();
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return super.isActive();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
