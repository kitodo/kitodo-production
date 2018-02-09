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
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.beans.UserGroupClientAuthorityRelation;
import org.kitodo.data.database.beans.UserGroupProjectAuthorityRelation;
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

    private Project selectedProject;
    private boolean projectsAvailable = false;
    private boolean projectAuthoritiesChanged = false;
    private List<UserGroupProjectAuthorityRelation> userGroupProjectAuthorityRelationsToDelete = new ArrayList<>();

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

    private void initializeSelectedProject() {
        if (selectedProject == null) {
            try {
                Long databaseRows = serviceManager.getProcessService().countDatabaseRows();
                if (databaseRows > 0L) {
                    this.selectedProject = serviceManager.getProjectService().getById(1);
                    this.projectsAvailable = true;
                } else {
                    this.projectsAvailable = false;
                }
            } catch (DAOException e) {
                Helper.setFehlerMeldung(e);
                projectsAvailable = false;
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

            if (projectAuthoritiesChanged) {
                for (UserGroupProjectAuthorityRelation relation : userGroupProjectAuthorityRelationsToDelete) {
                    relation.setAuthority(null);
                    relation.setProject(null);
                    relation.setUserGroup(null);
                    this.serviceManager.getUserGroupProjectAuthorityRelationService().remove(relation);
                }
                this.userGroupProjectAuthorityRelationsToDelete.clear();
                this.projectAuthoritiesChanged = false;
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
            if (this.userGroup.getGlobalAuthorities().size() > 0) {
                this.userGroup.setGlobalAuthorities(new ArrayList<>());
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
     * '0', the form for creating a new user group will be displayed. Selectable
     * clients and projects are initialized as well.
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
        initializeSelectedProject();
        userGroupClientAuthorityRelationsToDelete.clear();
    }

    /**
     * Gets the user group.
     * 
     * @return The user group.
     */
    public UserGroup getUserGroup() {
        return this.userGroup;
    }

    /**
     * Sets the user group.
     * 
     * @param userGroup
     *            The user group.
     */
    public void setUserGroup(UserGroup userGroup) {
        Helper.getHibernateSession().clear();
        this.userGroup = userGroup;
    }

    /**
     * Sets the user group id.
     * 
     * @param id
     *            The user group id.
     */
    public void setUserGroupId(int id) {
        this.userGroupId = id;
    }

    /**
     * Gets the user group id.
     * 
     * @return The user group id.
     */
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
        List<Authority> assignedAuthorities = this.userGroup.getGlobalAuthorities();
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
        this.userGroup.setGlobalAuthorities(authorities.getTarget());
    }

    /**
     * Return the list of available authorization levels and the list of authority
     * levels currently assigned to 'userGroup' relation to selected client as a
     * combined 'DualListModel' that is used by the frontend for authority
     * management of user groups utilizing a PrimeFaces PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getAuthoritiesByCurrentClient() {
        List<Authority> assignedAuthorities = this.userGroup.getAuthoritiesByClient(this.selectedClient);
        List<Authority> availableAuthorities = serviceManager.getAuthorityService().getAll();
        availableAuthorities.removeAll(assignedAuthorities);
        return new DualListModel<>(availableAuthorities, assignedAuthorities);
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'userGroup' in relation to selected client using a PrimeFaces PickList
     * object.
     *
     * @param clientAuthorities
     *            list of authority assigned to 'userGroup'
     */
    public void setAuthoritiesByCurrentClient(DualListModel<Authority> clientAuthorities) {

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

    private UserGroupClientAuthorityRelation getClientRelationCopyWithId(
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

                userGroupClientAuthorityRelations.add(getClientRelationCopyWithId(
                    userGroupClientAuthorityRelationsToDelete, userGroupClientAuthorityRelation));
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

                userGroupClientAuthorityRelation = getClientRelationCopyWithId(userGroupClientAuthorityRelations,
                    userGroupClientAuthorityRelation);

                userGroupClientAuthorityRelations.remove(userGroupClientAuthorityRelation);
                this.userGroupClientAuthorityRelationsToDelete.add(userGroupClientAuthorityRelation);
            }
        }
        return userGroupClientAuthorityRelations;
    }

    /**
     * Return the list of available authority levels and the list of authority
     * levels currently assigned to 'userGroup' in relation to selected project as a
     * combined 'DualListModel' that is used by the frontend for authority
     * management of user groups utilizing a PrimeFaces PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getAuthoritiesByCurrentProject() {
        List<Authority> assignedAuthorities = this.userGroup.getAuthoritiesByProject(this.selectedProject);
        List<Authority> availableAuthorities = serviceManager.getAuthorityService().getAll();
        availableAuthorities.removeAll(assignedAuthorities);
        return new DualListModel<>(availableAuthorities, assignedAuthorities);
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'userGroup' in relation to selected project using a PrimeFaces PickList
     * object.
     *
     * @param projectAuthorities
     *            list of authority assigned to 'userGroup'
     */
    public void setAuthoritiesByCurrentProject(DualListModel<Authority> projectAuthorities) {

        List<Authority> targetAuthorities = projectAuthorities.getTarget();
        List<Authority> sourceAuthorities = projectAuthorities.getSource();

        List<UserGroupProjectAuthorityRelation> userGroupProjectAuthorityRelations = this.userGroup
                .getUserGroupProjectAuthorityRelations();

        userGroupProjectAuthorityRelations = modifyProjectRelationsByPickedAuthorities(
            userGroupProjectAuthorityRelations, targetAuthorities);
        userGroupProjectAuthorityRelations = modifyProjectRelationsByNotPickedAuthorities(
            userGroupProjectAuthorityRelations, sourceAuthorities);

        this.userGroup.setUserGroupProjectAuthorityRelations(userGroupProjectAuthorityRelations);
    }

    private UserGroupProjectAuthorityRelation getProjectRelationCopyWithId(
            List<UserGroupProjectAuthorityRelation> relationsWithId, UserGroupProjectAuthorityRelation relation) {
        for (UserGroupProjectAuthorityRelation relationItem : relationsWithId) {
            if (relation.equals(relationItem)) {
                if (relationItem.getId() != null) {
                    return relationItem;
                } else {
                    return relation;
                }
            }
        }
        return new UserGroupProjectAuthorityRelation();
    }

    private List<UserGroupProjectAuthorityRelation> modifyProjectRelationsByPickedAuthorities(
            List<UserGroupProjectAuthorityRelation> userGroupProjectAuthorityRelations,
            List<Authority> authoritiesToCheck) {
        for (Authority authority : authoritiesToCheck) {
            UserGroupProjectAuthorityRelation userGroupProjectAuthorityRelation = new UserGroupProjectAuthorityRelation(
                    this.userGroup, this.selectedProject, authority);

            if (this.userGroupProjectAuthorityRelationsToDelete.contains(userGroupProjectAuthorityRelation)) {

                userGroupProjectAuthorityRelations.add(getProjectRelationCopyWithId(
                    this.userGroupProjectAuthorityRelationsToDelete, userGroupProjectAuthorityRelation));
                this.projectAuthoritiesChanged = true;
                this.userGroupProjectAuthorityRelationsToDelete.remove(userGroupProjectAuthorityRelation);
            }
            if (!userGroupProjectAuthorityRelations.contains(userGroupProjectAuthorityRelation)) {
                userGroupProjectAuthorityRelations.add(userGroupProjectAuthorityRelation);
            }
        }
        return userGroupProjectAuthorityRelations;
    }

    private List<UserGroupProjectAuthorityRelation> modifyProjectRelationsByNotPickedAuthorities(
            List<UserGroupProjectAuthorityRelation> userGroupProjectAuthorityRelations,
            List<Authority> authoritiesToCheck) {

        for (Authority authority : authoritiesToCheck) {
            UserGroupProjectAuthorityRelation userGroupProjectAuthorityRelation = new UserGroupProjectAuthorityRelation(
                    this.userGroup, this.selectedProject, authority);

            if (userGroupProjectAuthorityRelations.contains(userGroupProjectAuthorityRelation)) {
                this.projectAuthoritiesChanged = true;

                userGroupProjectAuthorityRelation = getProjectRelationCopyWithId(userGroupProjectAuthorityRelations,
                    userGroupProjectAuthorityRelation);

                userGroupProjectAuthorityRelations.remove(userGroupProjectAuthorityRelation);
                this.userGroupProjectAuthorityRelationsToDelete.add(userGroupProjectAuthorityRelation);
            }
        }
        return userGroupProjectAuthorityRelations;
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

    /**
     * Gets all available clients.
     * 
     * @return The list of clients.
     */
    public List<Client> getClients() {
        return serviceManager.getClientService().getAll();
    }

    /**
     * Gets selectedProject.
     *
     * @return The selectedProject.
     */
    public Project getSelectedProject() {
        return selectedProject;
    }

    /**
     * Sets selectedProject.
     *
     * @param selectedProject
     *            The selectedProject.
     */
    public void setSelectedProject(Project selectedProject) {
        this.selectedProject = selectedProject;
    }

    /**
     * Gets all available Projects.
     * 
     * @return The list of projects.
     */
    public List<Project> getProjects() {
        return serviceManager.getProjectService().getAll();
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

    /**
     * Gets clientsAvailable.
     *
     * @return The clientsAvailable.
     */
    public boolean isProjectsAvailable() {
        return projectsAvailable;
    }
}
