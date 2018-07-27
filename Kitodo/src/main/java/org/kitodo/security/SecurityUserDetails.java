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
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SecurityUserDetails extends User implements UserDetails {

    /**
     * The client which was selected by user after login.
     */
    private Client sessionClient;

    public SecurityUserDetails(final User user) {
        super(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<UserGroup> userGroups = super.getUserGroups();
        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();

        for (UserGroup userGroup : userGroups) {
            List<Authority> authorities = userGroup.getGlobalAuthorities();
            for (Authority authority : authorities) {
                grantedAuthorities.add(new SimpleGrantedAuthority(authority.getTitle() + "_GLOBAL"));
            }

            insertClientAuthoritiesFromUserGroup(grantedAuthorities, userGroup);
            insertProjectAuthoritiesFromUserGroup(grantedAuthorities, userGroup);
        }
        return grantedAuthorities;
    }

    private void insertClientAuthoritiesFromUserGroup(
            List<SimpleGrantedAuthority> simpleGrantedAuthorities, UserGroup userGroup) {
//        List<UserGroupClientAuthorityRelation> userGroupClientAuthorityRelations = userGroup
//                .getUserGroupClientAuthorityRelations();
//
//        for (UserGroupClientAuthorityRelation relation : userGroupClientAuthorityRelations) {
//
//            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(
//                        relation.getAuthority().getTitle() + "_CLIENT_ANY");
//
//            if (!simpleGrantedAuthorities.contains(simpleGrantedAuthority)) {
//                simpleGrantedAuthorities.add(simpleGrantedAuthority);
//            }
//
//            SimpleGrantedAuthority simpleGrantedAuthorityWithId = new SimpleGrantedAuthority(
//                        relation.getAuthority().getTitle() + "_CLIENT_" + relation.getClient().getId());
//
//            if (!simpleGrantedAuthorities.contains(simpleGrantedAuthorityWithId)) {
//                simpleGrantedAuthorities.add(simpleGrantedAuthorityWithId);
//            }
//        }
    }

    private void insertProjectAuthoritiesFromUserGroup(
            List<SimpleGrantedAuthority> simpleGrantedAuthorities, UserGroup userGroup) {
//        List<UserGroupProjectAuthorityRelation> userGroupProjectAuthorityRelations = userGroup
//                .getUserGroupProjectAuthorityRelations();
//
//        for (UserGroupProjectAuthorityRelation relation : userGroupProjectAuthorityRelations) {
//
//            SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(
//                    relation.getAuthority().getTitle() + "_PROJECT_ANY");
//
//            if (!simpleGrantedAuthorities.contains(simpleGrantedAuthority)) {
//                simpleGrantedAuthorities.add(simpleGrantedAuthority);
//            }
//
//            SimpleGrantedAuthority simpleGrantedAuthorityWithId = new SimpleGrantedAuthority(
//                    relation.getAuthority().getTitle() + "_PROJECT_" + relation.getProject().getId());
//
//            if (!simpleGrantedAuthorities.contains(simpleGrantedAuthorityWithId)) {
//                simpleGrantedAuthorities.add(simpleGrantedAuthorityWithId);
//            }
//        }
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
     * @param sessionClient The sessionClient.
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
