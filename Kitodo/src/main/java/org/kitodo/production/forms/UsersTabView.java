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

package org.kitodo.production.forms;

import static java.util.Map.entry;

import java.util.Map;
import java.util.Objects;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.kitodo.production.forms.user.UserListView;

@Named("UsersTabView")
@ViewScoped
public class UsersTabView extends BaseForm {

    @Inject
    private UserListView userListView;

    @Inject
    private RoleListView roleListView;
    
    @Inject
    private ClientListView clientListView;

    @Inject
    private AuthorityListView authorityListView;

    @Inject
    private LdapGroupListView ldapGroupListView;

    @Inject
    private LdapServerListView ldapServerListView;

    /**
     * Apply view parameter "firstRow" to currently active list view depending on tab index.
     * 
     * @param firstRow the row index of the first row to be displayed in the active list view
     */
    @Override
    public void setFirstRowFromTemplate(String firstRow) {
        BaseForm activeListView = Map.ofEntries(
            entry(0, userListView), 
            entry(1, roleListView), 
            entry(2, clientListView), 
            entry(3, authorityListView), 
            entry(4, ldapGroupListView), 
            entry(5, ldapServerListView)
        ).get(getActiveTabIndex());

        if (Objects.nonNull(activeListView)) {
            activeListView.setFirstRowFromTemplate(firstRow);
        }
    }

}
