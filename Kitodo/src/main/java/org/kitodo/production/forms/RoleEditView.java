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

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.DualListModel;

@Named("RoleEditView")
@ViewScoped
public class RoleEditView extends BaseForm {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "roleEdit");

    private static final Logger logger = LogManager.getLogger(RoleEditView.class);

    private Role role;
    private List<Client> clients;
    private DualListModel<Authority> globalAuthoritiesSelection;
    private DualListModel<Authority> clientAuthoritiesSelection;

    /**
     * Initialize role edit view.
     */
    @PostConstruct
    public void init() {
        role = new Role();
        globalAuthoritiesSelection = retrieveGlobalAssignableAuthorities(role);
        clientAuthoritiesSelection = retrieveClientAssignableAuthorities(role);
        try {
            clients = ServiceManager.getClientService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.CLIENT.getTranslationPlural() }, logger, e);
        }
    }

    /**
     * Save role.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            updateAssignedAuthorities(globalAuthoritiesSelection, role);
            updateAssignedAuthorities(clientAuthoritiesSelection, role);
            ServiceManager.getRoleService().save(role);
            return RoleListView.VIEW_PATH + "&firstRow=" + getReferrerFirstRow();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.ROLE.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
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
            try {
                role = ServiceManager.getRoleService().getById(id);
                globalAuthoritiesSelection = retrieveGlobalAssignableAuthorities(role);
                clientAuthoritiesSelection = retrieveClientAssignableAuthorities(role);
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.ROLE.getTranslationSingular(), id }, logger, e);
            }
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
     * Get all available clients.
     *
     * @return list of Client objects
     */
    public List<Client> getClients() {
        return clients;
    }

    /**
     * Return the current selection of global assignable authorities.
     * 
     * @return the current selection of global assignable authorities
     */
    public DualListModel<Authority> getGlobalAssignableAuthorities() {
        return globalAuthoritiesSelection;
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'role' using a PrimeFaces PickList object.
     *
     * @param globalAuthoritiesSelection
     *            list of authority assigned to 'role'
     */
    public void setGlobalAssignableAuthorities(DualListModel<Authority> globalAuthoritiesSelection) {
        this.globalAuthoritiesSelection = globalAuthoritiesSelection;
    }

    /**
     * Return the current selection of client assignable authorities.
     * 
     * @return the current selection of client assignable authorities
     */
    public DualListModel<Authority> getClientAssignableAuthorities() {
        return clientAuthoritiesSelection;
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'role' using a PrimeFaces PickList object.
     *
     * @param clientAuthoritiesSelection
     *            list of authority assigned to 'role'
     */
    public void setClientAssignableAuthorities(DualListModel<Authority> clientAuthoritiesSelection) {
        this.clientAuthoritiesSelection = clientAuthoritiesSelection;
    }

    /**
     * Return the list of available authorization levels and the list of authority
     * levels currently assigned to 'role' as a combined 'DualListModel' that
     * is used by the frontend for authority management of roles utilizing a
     * PrimeFaces PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    private static DualListModel<Authority> retrieveGlobalAssignableAuthorities(Role role) {
        List<Authority> assignedAuthorities = ServiceManager.getAuthorityService()
                .filterAssignableGlobal(role.getAuthorities());
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
     * Return the list of available authorization levels which can be assigned
     * client specific and the list of authority levels currently client specific
     * assigned to 'role' as a combined 'DualListModel' that is used by the
     * frontend for authority management of user groups utilizing a PrimeFaces
     * PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    private static DualListModel<Authority> retrieveClientAssignableAuthorities(Role role) {
        List<Authority> assignedAuthorities = ServiceManager.getAuthorityService()
                .filterAssignableToClients(role.getAuthorities());
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
     * Updates the list of authorities of a role from the PrimeFaces picklist selection.
     * 
     * @param authoritiesModel the PrimeFaces picklist selection
     * @param role the role to be updated
     */
    private static void updateAssignedAuthorities(DualListModel<Authority> authoritiesModel, Role role) {
        for (Authority authority : authoritiesModel.getSource()) {
            role.getAuthorities().remove(authority);
        }
        for (Authority authority : authoritiesModel.getTarget()) {
            if (!role.getAuthorities().contains(authority)) {
                role.getAuthorities().add(authority);
            }
        }
    }

    /**
     * Sorts a list of authorities by name.
     * 
     * @param authorityListToSort the authorities to be sorted
     * @return the sorted list of authorities
     */
    private static List<Authority> sortAuthorityListByTitle(List<Authority> authorityListToSort) {
        return authorityListToSort.stream()
                .sorted(Comparator.comparing(authority -> Helper.getTranslation(authority.getTitleWithoutSuffix())))
                .collect(Collectors.toList());
    }
}
