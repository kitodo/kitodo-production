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
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyRoleModel;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.DualListModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("RoleForm")
@SessionScoped
public class RoleForm extends BaseForm {
    private static final Logger logger = LogManager.getLogger(RoleForm.class);
    private Role role = new Role();

    private final String roleEditPath = MessageFormat.format(REDIRECT_PATH, "roleEdit");

    /**
     * Default constructor that also sets the LazyBeanModel instance of this
     * bean.
     */
    public RoleForm() {
        super();
        super.setLazyBeanModel(new LazyRoleModel(ServiceManager.getRoleService()));
        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
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
            return usersPage;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.ROLE.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Remove role.
     */
    public void delete() {
        try {
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
        } catch (DAOException e) {
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
        if (!Objects.equals(id, 0)) {
            setRoleById(id);
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
     * Set role by id.
     *
     * @param id
     *            of role to set
     */
    public void setRoleById(int id) {
        try {
            setRole(ServiceManager.getRoleService().getById(id));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.ROLE.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Get all available clients.
     *
     * @return list of Client objects
     */
    public List<Client> getClients() {
        try {
            return ServiceManager.getClientService().getAll();
        } catch (DAOException e) {
            return new ArrayList<>();
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
        return new DualListModel<>(sortAuthorityListByTitle(availableAuthorities), sortAuthorityListByTitle(assignedAuthorities));
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'role' using a PrimeFaces PickList object.
     *
     * @param globalAuthoritiesModel
     *            list of authority assigned to 'role'
     */
    public void setGlobalAssignableAuthorities(DualListModel<Authority> globalAuthoritiesModel) {
        setAssignableAuthorities(globalAuthoritiesModel);
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
        List<Authority> availableAuthorities = new ArrayList<>();
        try {
            availableAuthorities = ServiceManager.getAuthorityService().getAllAssignableToClients();
            availableAuthorities.removeAll(assignedAuthorities);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return new DualListModel<>(sortAuthorityListByTitle(availableAuthorities), sortAuthorityListByTitle(assignedAuthorities));
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'role' using a PrimeFaces PickList object.
     *
     * @param clientAuthoritiesModel
     *            list of authority assigned to 'role'
     */
    public void setClientAssignableAuthorities(DualListModel<Authority> clientAuthoritiesModel) {
        setAssignableAuthorities(clientAuthoritiesModel);
    }

    /**
     * Get value of property 'showRolesOfAllAvailableClients' of this forms 'LazyRoleModel' instance.
     *
     * @return value 'showRolesOfAllAvailableClients' as boolean
     */
    public boolean isShowRolesOfAllAvailableClients() {
        return ((LazyRoleModel)this.lazyBeanModel).isShowRolesOfAllAvailableClients();
    }

    /**
     * Returns whether current user has the authority to see roles of all clients.
     *
     * @return whether current user has the authority to see roles of all clients.
     */
    public boolean isHasPermissionToSeeAllClientsRoles() {
        return ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewRoleList();
    }

    /**
     * Set value of property 'showRolesOfAllAvailableClients' in this forms 'LazyRoleModel' instance.
     *
     * @param showRolesOfAllAvailableClients value of 'showRolesOfAllAvailableClients' as boolean
     */
    public void setShowRolesOfAllAvailableClients(boolean showRolesOfAllAvailableClients) {
        ((LazyRoleModel)this.lazyBeanModel).setShowRolesOfAllAvailableClients(showRolesOfAllAvailableClients);
    }

    private void setAssignableAuthorities(DualListModel<Authority> authoritiesModel) {
        for (Authority authority : authoritiesModel.getSource()) {
            this.role.getAuthorities().remove(authority);
        }
        for (Authority authority : authoritiesModel.getTarget()) {
            if (!this.role.getAuthorities().contains(authority)) {
                this.role.getAuthorities().add(authority);
            }
        }
    }

    private List<Authority> sortAuthorityListByTitle(List<Authority> authorityListToSort) {
        return authorityListToSort.stream()
                .sorted(Comparator.comparing(authority -> Helper.getTranslation(authority.getTitleWithoutSuffix())))
                .collect(Collectors.toList());
    }
}
