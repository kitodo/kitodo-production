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
        BaseForm activeListView = getActiveListView();

        if (Objects.nonNull(activeListView)) {
            activeListView.setFirstRowFromTemplate(firstRow);
        }
    }

    /**
     * Sets the sort by query parameters for the currently active list view.
     * 
     * @param field the sort by field
     * @param order the sort by order
     */
    public void setSortByFromTemplate(String field, String order) {
        BaseListView activeListView = getActiveListView();

        if (Objects.nonNull(activeListView)) {
            activeListView.setSortByFromTemplate(field, order);
        }
    }

    /**
     * Sets the filter from a URL query parameter.
     * 
     * @param encodedFilter the filter value provided as encoded URL query parameter
     */
    public void setFilterFromTemplate(String encodedFilter) {
        if (getActiveTabIndex() == 0) {
            // user list view
            userListView.setFilterFromTemplate(encodedFilter);
        }
    }

    /**
     * Return the currently active list view.
     * 
     * @return the currently active list view
     */
    private BaseListView getActiveListView() {
        return Map.ofEntries(
            entry(0, userListView), 
            entry(1, roleListView), 
            entry(2, clientListView), 
            entry(3, authorityListView), 
            entry(4, ldapGroupListView), 
            entry(5, ldapServerListView)
        ).get(getActiveTabIndex());
    }
}
