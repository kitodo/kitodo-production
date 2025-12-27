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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("LdapGroupListView")
@ViewScoped
public class LdapGroupListView extends BaseListView {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "users") + "&tabIndex=4";

    private static final Logger logger = LogManager.getLogger(LdapGroupListView.class);  

    protected static final String ERROR_DELETING_LDAP_GROUP = "ldapGroupInUse";

    private List<LdapGroup> ldapGroups;

    /**
     * Initialize new ldap group list view by retrieving all available ldap groups from the database.
     */
    @PostConstruct
    public void init() {
        ldapGroups = retrieveLdapGroups();
        
        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("ldapgroup"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        selectedColumns = ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("ldapgroup");

        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
    }

    /**
     * Forward to new LDAP group edit view.
     *
     * @return page
     */
    public String newLdapGroup() {
        return LdapGroupEditView.VIEW_PATH;
    }

    /**
     * Return all ldap groups.
     *
     * @return list of LdapGroup objects.
     */
    public List<LdapGroup> getLdapGroups() {
        return ldapGroups;
    }

    /**
     * View action that removes a specific ldap group.
     * 
     * @param ldapGroup the ldap group to be deleted
     * @return page
     */
    public String delete(LdapGroup ldapGroup) {
        if (deleteLdapGroup(ldapGroup)) {
            return VIEW_PATH;
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Remove LDAP Group.
     *
     * @return true if group was removed
     */
    public static boolean deleteLdapGroup(LdapGroup group) {
        if (Objects.nonNull(group) && !group.getUsers().isEmpty()) {
            Helper.setErrorMessage(ERROR_DELETING_LDAP_GROUP, new Object[] {ObjectType.LDAP_GROUP.getTranslationSingular()}, logger);
            return false;
        }
        try {
            ServiceManager.getLdapGroupService().remove(group);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.LDAP_GROUP.getTranslationSingular()}, logger, e);
            return false;
        }
        return true;
    }

    /**
     * Declare the allowed sort fields for sanitizing the query parameter "sortField".
     */
    @Override
    protected Set<String> getAllowedSortFields() {
        return Set.of("title", "homeDirectory", "gidNumber");
    }

    /**
     * Retrieve ldap groups from database.
     *
     * @return list of LdapGroup objects.
     */
    private static List<LdapGroup> retrieveLdapGroups() {
        try {
            return ServiceManager.getLdapGroupService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.LDAP_GROUP.getTranslationPlural()}, logger, e);
            return new ArrayList<>();
        }
    }

}
