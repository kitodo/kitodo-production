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

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyRoleModel;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("RoleListView")
@ViewScoped
public class RoleListView extends BaseForm {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "users") + "#usersTabView:rolesTab";

    private static final Logger logger = LogManager.getLogger(RoleListView.class);
    
    /**
     * Initialize RoleListView.
     */
    @PostConstruct
    public void init() {
        setLazyBeanModel(new LazyRoleModel(ServiceManager.getRoleService()));

        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("role"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        selectedColumns = ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("role");

        if (!ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewRoleList()) {
            deselectRoleClientColumn();
        }

        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
    }

    /**
     * Create new role.
     *
     * @return page address
     */
    public String newRole() {
        return RoleEditView.VIEW_PATH;
    }

    /**
     * View action to delete a specific role.
     * 
     * @param role the role to be deleted
     * 
     * @return next page
     */
    public String delete(Role role) {
        if (deleteRole(role)) {
            return VIEW_PATH;
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Remove a role from the database.
     * 
     * @param role the role to be removed
     * @return true if role was removed
     */
    public static boolean deleteRole(Role role) {
        try {
            if (!role.getUsers().isEmpty()) {
                for (User user : role.getUsers()) {
                    user.getRoles().remove(role);
                }
                role.setUsers(new ArrayList<>());
                ServiceManager.getRoleService().save(role);
            }
            if (!role.getTasks().isEmpty()) {
                Helper.setErrorMessage("roleAssignedError");
                return false;
            }
            if (!role.getAuthorities().isEmpty()) {
                role.setAuthorities(new ArrayList<>());
                ServiceManager.getRoleService().save(role);
            }
            ServiceManager.getRoleService().remove(role);
            return true;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.ROLE.getTranslationSingular() }, logger, e);
        }
        return false;
    }

    /**
     * Returns whether current user has the authority to see roles of all clients.
     *
     * @return whether current user has the authority to see roles of all clients.
     */
    public boolean isHasPermissionToSeeAllClientRoles() {
        return ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewRoleList();
    }

    /**
     * Get value of property 'showRolesOfAllAvailableClients' in this forms 'LazyRoleModel' instance.
     *
     * @return value of 'showRolesOfAllAvailableClients' as boolean
     */
    public boolean isShowRolesOfAllAvailableClients() {
        return ((LazyRoleModel)this.lazyBeanModel).isShowRolesOfAllAvailableClients();
    }

    /**
     * Set value of property 'showRolesOfAllAvailableClients' in this forms 'LazyRoleModel' instance.
     *
     * @param showRolesOfAllAvailableClients value of 'showRolesOfAllAvailableClients' as boolean
     */
    public void setShowRolesOfAllAvailableClients(boolean showRolesOfAllAvailableClients) {
        ((LazyRoleModel)this.lazyBeanModel).setShowRolesOfAllAvailableClients(showRolesOfAllAvailableClients);
    }

    private void deselectRoleClientColumn() {
        selectedColumns = ServiceManager.getListColumnService().removeColumnByTitle(selectedColumns, "role.client");
    }

}
