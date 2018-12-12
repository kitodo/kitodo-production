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

package org.kitodo.forms;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.enums.ObjectType;
import org.kitodo.helper.Helper;
import org.kitodo.helper.SelectItemList;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;
import org.primefaces.model.DualListModel;

@Named("RoleForm")
@SessionScoped
public class RoleForm extends BaseForm {
    private static final long serialVersionUID = 8051160917458068675L;
    private static final Logger logger = LogManager.getLogger(RoleForm.class);
    private Role role = new Role();

    private String roleListPath = MessageFormat.format(REDIRECT_PATH, "users");
    private String roleEditPath = MessageFormat.format(REDIRECT_PATH, "roleEdit");

    /**
     * Default constructor that also sets the LazyDTOModel instance of this bean.
     */
    @Inject
    public RoleForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getRoleService()));
    }

    /**
     * Create new role.
     *
     * @return page address
     */
    public String newRole() {
        this.role = new Role();

        if (!ServiceManager.getSecurityAccessService().hasAuthorityGlobalToAddOrEditRole()) {
            Client sessionClient = ServiceManager.getUserService().getSessionClientOfAuthenticatedUser();
            if (Objects.nonNull(sessionClient)) {
                this.role.setClient(sessionClient);
            }
        }

        return roleEditPath;
    }

    /**
     * Save role.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            ServiceManager.getRoleService().save(this.role);
            return roleListPath;
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.ROLE.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Remove role.
     */
    public void delete() {
        try {
            ServiceManager.getRoleService().refresh(this.role);
            if (!this.role.getUsers().isEmpty()) {
                for (User user : this.role.getUsers()) {
                    user.getRoles().remove(this.role);
                }
                this.role.setUsers(new ArrayList<>());
                ServiceManager.getRoleService().save(this.role);
            }
            if (!this.role.getTasks().isEmpty()) {
                Helper.setErrorMessage("roleAssignedError");
                return;
            }
            if (!this.role.getAuthorities().isEmpty()) {
                this.role.setAuthorities(new ArrayList<>());
                ServiceManager.getRoleService().save(this.role);
            }
            ServiceManager.getRoleService().remove(this.role);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.ROLE.getTranslationSingular() }, logger, e);
        }
    }

    /**
     * Method being used as viewAction for role edit form. Selectable clients
     * and projects are initialized as well.
     *
     * @param id
     *            ID of the role to load
     */
    public void load(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setRole(ServiceManager.getRoleService().getById(id));
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.ROLE.getTranslationSingular(), id },
                logger, e);
        }
        setSaveDisabled(true);
    }

    /**
     * Get the role.
     *
     * @return the role
     */
    public Role getRole() {
        return this.role;
    }

    /**
     * Set the role.
     *
     * @param role
     *            the role
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Set role by ID.
     *
     * @param roleID
     *            ID of role to set.
     */
    public void setRoleById(int roleID) {
        try {
            setRole(ServiceManager.getRoleService().getById(roleID));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.ROLE.getTranslationSingular(), roleID },
                logger, e);
        }
    }

    /**
     * Get all available clients.
     *
     * @return list of Client objects
     */
    public List<SelectItem> getClients() {
        try {
            return SelectItemList.getClients(ServiceManager.getClientService().getAll());
        } catch (DAOException e) {
            return SelectItemList.getClients(new ArrayList<>());
        }
    }

    /**
     * Return the list of available authorization levels and the list of authority
     * levels currently assigned to 'role' as a combined 'DualListModel' that
     * is used by the frontend for authority management of roles utilizing a
     * PrimeFaces PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getGlobalAssignableAuthorities() {
        List<Authority> assignedAuthorities = ServiceManager.getAuthorityService()
                .filterAssignableGlobal(this.role.getAuthorities());
        List<Authority> availableAuthorities = new ArrayList<>();
        try {
            availableAuthorities = ServiceManager.getAuthorityService().getAllAssignableGlobal();
            availableAuthorities.removeAll(assignedAuthorities);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return new DualListModel<>(availableAuthorities, assignedAuthorities);
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'role' using a PrimeFaces PickList object.
     *
     * @param globalAuthoritiesModel
     *            list of authority assigned to 'role'
     */
    public void setGlobalAssignableAuthorities(DualListModel<Authority> globalAuthoritiesModel) {
        for (Authority authority : globalAuthoritiesModel.getSource()) {
            this.role.getAuthorities().remove(authority);
        }
        for (Authority authority : globalAuthoritiesModel.getTarget()) {
            if (!this.role.getAuthorities().contains(authority)) {
                this.role.getAuthorities().add(authority);
            }
        }
    }

    /**
     * Return the list of available authorization levels which can be assigned
     * client specific and the list of authority levels currently client specific
     * assigned to 'role' as a combined 'DualListModel' that is used by the
     * frontend for authority management of user groups utilizing a PrimeFaces
     * PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getClientAssignableAuthorities() {
        List<Authority> assignedAuthorities = ServiceManager.getAuthorityService()
                .filterAssignableToClients(this.role.getAuthorities());
        List<Authority> availableAuthorities = null;
        try {
            availableAuthorities = ServiceManager.getAuthorityService().getAllAssignableToClients();
            availableAuthorities.removeAll(assignedAuthorities);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return new DualListModel<>(availableAuthorities, assignedAuthorities);
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'role' using a PrimeFaces PickList object.
     *
     * @param clientAuthoritiesModel
     *            list of authority assigned to 'role'
     */
    public void setClientAssignableAuthorities(DualListModel<Authority> clientAuthoritiesModel) {
        for (Authority authority : clientAuthoritiesModel.getSource()) {
            this.role.getAuthorities().remove(authority);
        }
        for (Authority authority : clientAuthoritiesModel.getTarget()) {
            if (!this.role.getAuthorities().contains(authority)) {
                this.role.getAuthorities().add(authority);
            }
        }
    }
}
