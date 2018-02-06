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

package de.sub.goobi.forms;

import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.beans.UserGroupClientAuthorityRelation;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;
import org.primefaces.model.DualListModel;

@Named("BenutzergruppenForm")
@SessionScoped
public class BenutzergruppenForm extends BasisForm {
    private static final long serialVersionUID = 8051160917458068675L;
    private static final Logger logger = LogManager.getLogger(BenutzergruppenForm.class);
    private UserGroup userGroup = new UserGroup();
    private transient ServiceManager serviceManager = new ServiceManager();
    private int userGroupId;

    @Inject
    @Named("BenutzerverwaltungForm")
    private BenutzerverwaltungForm userForm;

    private Client selectedClient;
    private boolean clientsAvailable = false;

    private boolean clientAuthoritiesChanged = false;
    private List<UserGroupClientAuthorityRelation> userGroupClientAuthorityRelationsToDelete = new ArrayList<>();

    private void initializeSelectedClient() {
        if (selectedClient == null) {
            try {
                Long databaseRows = serviceManager.getClientService().countDatabaseRows();
                if (databaseRows > 0L) {
                    this.selectedClient = serviceManager.getClientService().getById(1);
                    this.clientsAvailable = true;
                } else {
                    this.clientsAvailable = false;
                }
            } catch (DAOException e) {
                Helper.setFehlerMeldung(e);
                clientsAvailable = false;
            }
        }
    }

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of this
     * bean.
     */
    public BenutzergruppenForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getUserGroupService()));
    }

    /**
     * Create new user group.
     *
     * @return page address
     */
    public String newUserGroup() {
        this.userGroup = new UserGroup();
        this.userGroupId = 0;
        return redirectToEdit();
    }

    /**
     * Save user group.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            this.serviceManager.getUserGroupService().save(this.userGroup);

            if (clientAuthoritiesChanged) {
                for (UserGroupClientAuthorityRelation relation : userGroupClientAuthorityRelationsToDelete) {
                    relation.setAuthority(null);
                    relation.setClient(null);
                    relation.setUserGroup(null);
                    this.serviceManager.getUserGroupClientAuthorityRelationService().remove(relation);
                }
                this.userGroupClientAuthorityRelationsToDelete.clear();
                this.clientAuthoritiesChanged = false;
            }

            return redirectToList();
        } catch (DataException | DAOException e) {
            logger.error(e);
            Helper.setFehlerMeldung("Error, could not save user group", e.getMessage());
            return null;
        }
    }

    /**
     * Remove user group.
     *
     * @return page or empty String
     */
    public String delete() {
        try {
            this.serviceManager.getUserGroupService().refresh(this.userGroup);
            if (this.userGroup.getUsers().size() > 0) {
                for (User b : this.userGroup.getUsers()) {
                    b.getUserGroups().remove(this.userGroup);
                }
                this.userGroup.setUsers(new ArrayList<>());
                this.serviceManager.getUserGroupService().save(this.userGroup);
            }
            if (this.userGroup.getTasks().size() > 0) {
                Helper.setFehlerMeldung("userGroupAssignedError");
                return null;
            }
            if (this.userGroup.getUserGroupClientAuthorityRelations().size() > 0) {
                for (UserGroupClientAuthorityRelation relation : userGroup.getUserGroupClientAuthorityRelations()) {
                    relation.setAuthority(null);
                    relation.setClient(null);
                    relation.setUserGroup(null);
                    this.serviceManager.getUserGroupClientAuthorityRelationService().remove(relation);
                }
            }
            if (this.userGroup.getAuthorities().size() > 0) {
                this.userGroup.setAuthorities(new ArrayList<>());
                this.serviceManager.getUserGroupService().save(this.userGroup);
            }
            this.serviceManager.getUserGroupService().remove(this.userGroup);
        } catch (DataException | DAOException e) {
            Helper.setFehlerMeldung("Error, could not delete", e.getMessage());
            return null;
        }
        return redirectToList();
    }

    /**
     * Method being used as viewAction for user group edit form. If 'userGroupId' is
     * '0', the form for creating a new user group will be displayed.
     */
    public void loadUserGroup() {
        try {
            if (!Objects.equals(this.userGroupId, 0)) {
                setUserGroup(this.serviceManager.getUserGroupService().getById(this.userGroupId));
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error retrieving user group with ID '" + this.userGroupId + "'; ", e.getMessage());
        }

        initializeSelectedClient();
        userGroupClientAuthorityRelationsToDelete.clear();
    }

    /*
     * Getter und Setter
     */

    public UserGroup getUserGroup() {
        return this.userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        Helper.getHibernateSession().clear();
        this.userGroup = userGroup;
    }

    public void setUserGroupId(int id) {
        this.userGroupId = id;
    }

    public int getUserGroupId() {
        return this.userGroupId;
    }

    /**
     * Return the list of available authorization levels and the list of authority
     * levels currently assigned to 'userGroup' as a combined 'DualListModel' that
     * is used by the frontend for authority management of user groups utilizing a
     * PrimeFaces PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getAuthorities() {
        List<Authority> assignedAuthorities = this.userGroup.getAuthorities();
        List<Authority> availableAuthorities = serviceManager.getAuthorityService().getAll();
        availableAuthorities.removeAll(assignedAuthorities);
        return new DualListModel<>(availableAuthorities, assignedAuthorities);
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'userGroup' using a PrimeFaces PickList object.
     *
     * @param authorities
     *            list of authority assigned to 'userGroup'
     */
    public void setAuthorities(DualListModel<Authority> authorities) {
        // authorities.getTarget().removeIf(Objects::isNull);
        this.userGroup.setAuthorities(authorities.getTarget());
    }

    /**
     * Return the list of available authorization levels and the list of authority
     * levels currently assigned to 'userGroup' as a combined 'DualListModel' that
     * is used by the frontend for authority management of user groups utilizing a
     * PrimeFaces PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getAuthoritiesByCurrentClient() {
        List<Authority> assignedAuthorities = serviceManager.getUserGroupClientAuthorityRelationService()
                .getAuthoritiesFromListByClientAndUserGroup(this.userGroup, this.selectedClient,
                    this.userGroup.getUserGroupClientAuthorityRelations());
        List<Authority> availableAuthorities = serviceManager.getAuthorityService().getAll();
        availableAuthorities.removeAll(assignedAuthorities);
        return new DualListModel<>(availableAuthorities, assignedAuthorities);
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'userGroup' using a PrimeFaces PickList object.
     *
     * @param clientAuthorities
     *            list of authority assigned to 'userGroup'
     */
    public void setAuthoritiesByCurrentClient(DualListModel<Authority> clientAuthorities) {

        // clientAuthorities.getTarget().removeIf(Objects::isNull);
        List<Authority> targetAuthorities = clientAuthorities.getTarget();
        List<Authority> sourceAuthorities = clientAuthorities.getSource();

        List<UserGroupClientAuthorityRelation> userGroupClientAuthorityRelations = this.userGroup
                .getUserGroupClientAuthorityRelations();

        userGroupClientAuthorityRelations = modifyClientRelationsByPickedAuthorities(userGroupClientAuthorityRelations,
            targetAuthorities);
        userGroupClientAuthorityRelations = modifyClientRelationsByNotPickedAuthorities(
            userGroupClientAuthorityRelations, sourceAuthorities);

        this.userGroup.setUserGroupClientAuthorityRelations(userGroupClientAuthorityRelations);
    }

    private UserGroupClientAuthorityRelation getRelationCopyWithId(
            List<UserGroupClientAuthorityRelation> relationsWithId, UserGroupClientAuthorityRelation relation) {
        for (UserGroupClientAuthorityRelation relationItem : relationsWithId) {
            if (relation.equals(relationItem)) {
                if (relationItem.getId() != null) {
                    return relationItem;
                } else {
                    return relation;
                }
            }
        }
        return new UserGroupClientAuthorityRelation();
    }

    private List<UserGroupClientAuthorityRelation> modifyClientRelationsByPickedAuthorities(
            List<UserGroupClientAuthorityRelation> userGroupClientAuthorityRelations,
            List<Authority> authoritiesToCheck) {
        for (Authority authority : authoritiesToCheck) {
            UserGroupClientAuthorityRelation userGroupClientAuthorityRelation = new UserGroupClientAuthorityRelation(
                    this.userGroup, this.selectedClient, authority);

            if (this.userGroupClientAuthorityRelationsToDelete.contains(userGroupClientAuthorityRelation)) {

                userGroupClientAuthorityRelations.add(
                    getRelationCopyWithId(userGroupClientAuthorityRelationsToDelete, userGroupClientAuthorityRelation));
                this.clientAuthoritiesChanged = true;
                this.userGroupClientAuthorityRelationsToDelete.remove(userGroupClientAuthorityRelation);
            }
            if (!userGroupClientAuthorityRelations.contains(userGroupClientAuthorityRelation)) {
                userGroupClientAuthorityRelations.add(userGroupClientAuthorityRelation);
            }
        }
        return userGroupClientAuthorityRelations;
    }

    private List<UserGroupClientAuthorityRelation> modifyClientRelationsByNotPickedAuthorities(
            List<UserGroupClientAuthorityRelation> userGroupClientAuthorityRelations,
            List<Authority> authoritiesToCheck) {

        for (Authority authority : authoritiesToCheck) {
            UserGroupClientAuthorityRelation userGroupClientAuthorityRelation = new UserGroupClientAuthorityRelation(
                    this.userGroup, this.selectedClient, authority);

            if (userGroupClientAuthorityRelations.contains(userGroupClientAuthorityRelation)) {
                this.clientAuthoritiesChanged = true;

                userGroupClientAuthorityRelation = getRelationCopyWithId(userGroupClientAuthorityRelations,
                    userGroupClientAuthorityRelation);

                userGroupClientAuthorityRelations.remove(userGroupClientAuthorityRelation);
                this.userGroupClientAuthorityRelationsToDelete.add(userGroupClientAuthorityRelation);
            }
        }
        return userGroupClientAuthorityRelations;
    }

    /**
     * Gets selectedClient.
     *
     * @return The selectedClient.
     */
    public Client getSelectedClient() {
        return selectedClient;
    }

    /**
     * Sets selectedClient.
     *
     * @param selectedClient
     *            The selectedClient.
     */
    public void setSelectedClient(Client selectedClient) {
        this.selectedClient = selectedClient;
    }

    public List<Client> getClients() {
        return serviceManager.getClientService().getAll();
    }

    // TODO:
    // replace calls to this function with "/pages/usergroupEdit" once we have
    // completely switched to the new frontend pages
    private String redirectToEdit() {
        try {
            String referrer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap()
                    .get("referer");
            String callerViewId = referrer.substring(referrer.lastIndexOf("/") + 1);
            if (!callerViewId.isEmpty() && callerViewId.contains("users.jsf")) {
                return "/pages/usergroupEdit?" + REDIRECT_PARAMETER;
            } else {
                return "/pages/BenutzergruppenBearbeiten?" + REDIRECT_PARAMETER;
            }
        } catch (NullPointerException e) {
            // This NPE gets thrown - and therefore must be caught - when
            // "BenutzergruppenForm" is
            // used from it's integration test
            // class "BenutzergruppenFormIT", where no "FacesContext" is available!
            return "/pages/BenutzergruppenBearbeiten?" + REDIRECT_PARAMETER;
        }
    }

    // TODO:
    // replace calls to this function with "/pages/users" once we have completely
    // switched to the new frontend pages
    private String redirectToList() {
        try {
            String referrer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap()
                    .get("referer");
            String callerViewId = referrer.substring(referrer.lastIndexOf("/") + 1);
            if (!callerViewId.isEmpty() && callerViewId.contains("usergroupEdit.jsf")) {
                return "/pages/users.jsf?id=" + userForm.getActiveTabIndex() + "&" + REDIRECT_PARAMETER;
            } else {
                return "/pages/BenutzergruppenAlle?" + REDIRECT_PARAMETER;
            }
        } catch (NullPointerException e) {
            // This NPE gets thrown - and therefore must be caught - when
            // "BenutzergruppenForm" is
            // used from it's integration test
            // class "BenutzergruppenFormIT", where no "FacesContext" is available!
            return "/pages/BenutzergruppenAlle?" + REDIRECT_PARAMETER;
        }
    }

    /**
     * Gets clientsAvailable.
     *
     * @return The clientsAvailable.
     */
    public boolean isClientsAvailable() {
        return clientsAvailable;
    }
}
