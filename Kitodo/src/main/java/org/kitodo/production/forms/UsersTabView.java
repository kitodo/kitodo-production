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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.kitodo.production.forms.user.UserListView;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.security.SecurityAccessService;

@Named("UsersTabView")
@ViewScoped
public class UsersTabView extends BaseTabView {

    private static final SecurityAccessService securityAccessService = ServiceManager.getSecurityAccessService();

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
     * Initialize users tab view.
     */
    @PostConstruct
    public void init() {
        setActiveTabId("usersTab");
    }

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
        if (getActiveTabId().equals("usersTab")) {
            // user list view
            userListView.setFilterFromTemplate(encodedFilter);
        }
    }

    /** 
     * Overwrite allowed tab ids for sanitization of URL parameter.
     */
    @Override
    protected List<String> getAllowedTabIds() {
        return Stream.of(
            securityAccessService.hasAuthorityToViewUserList() ? "usersTab" : null,
            securityAccessService.hasAuthorityToViewRoleList() ? "rolesTab" : null,
            securityAccessService.hasAuthorityToViewClientList() ? "clientsTab" : null,
            securityAccessService.hasAuthorityToViewAuthorityList() ? "authoritiesTab" : null,
            securityAccessService.hasAuthorityToViewLdapGroupList() ? "ldapGroupsTab" : null,
            securityAccessService.hasAuthorityToViewLdapServerList() ? "ldapServersTab" : null
        ).filter(Objects::nonNull).toList();
    }

    /**
     * Return the currently active list view.
     * 
     * @return the currently active list view
     */
    private BaseListView getActiveListView() {
        return Map.ofEntries(
            entry("usersTab", userListView), 
            entry("rolesTab", roleListView), 
            entry("clientsTab", clientListView), 
            entry("authoritiesTab", authorityListView), 
            entry("ldapGroupsTab", ldapGroupListView), 
            entry("ldapServersTab", ldapServerListView)
        ).get(getActiveTabId());
    }

    
}
