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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
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
    private static final String USER_GROUP = "userGroup";
    private transient ServiceManager serviceManager = new ServiceManager();

    @Named("BenutzerverwaltungForm")
    private BenutzerverwaltungForm userForm;

    private String usergroupListPath = MessageFormat.format(REDIRECT_PATH, "users");
    private String usergroupEditPath = MessageFormat.format(REDIRECT_PATH, "usergroupEdit");

    /**
     * Default constructor with inject user form that also sets the LazyDTOModel
     * instance of this bean.
     * 
     * @param userForm
     *            BenutzerverwaltungForm managed bean
     */
    @Inject
    public BenutzergruppenForm(BenutzerverwaltungForm userForm) {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getUserGroupService()));
        this.userForm = userForm;
    }

    /**
     * Create new user group.
     *
     * @return page address
     */
    public String newUserGroup() {
        this.userGroup = new UserGroup();
        return usergroupEditPath;
    }

    /**
     * Save user group.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            this.serviceManager.getUserGroupService().save(this.userGroup);

//            if (clientAuthoritiesChanged) {
//                for (UserGroupClientAuthorityRelation relation : userGroupClientAuthorityRelationsToDelete) {
//                    relation.setAuthority(null);
//                    relation.setClient(null);
//                    relation.setUserGroup(null);
//                    this.serviceManager.getUserGroupClientAuthorityRelationService().removeFromDatabase(relation);
//                }
//                this.userGroupClientAuthorityRelationsToDelete.clear();
//                this.clientAuthoritiesChanged = false;
//            }
//
//            if (projectAuthoritiesChanged) {
//                for (UserGroupProjectAuthorityRelation relation : userGroupProjectAuthorityRelationsToDelete) {
//                    relation.setAuthority(null);
//                    relation.setProject(null);
//                    relation.setUserGroup(null);
//                    this.serviceManager.getUserGroupProjectAuthorityRelationService().removeFromDatabase(relation);
//                }
//                this.userGroupProjectAuthorityRelationsToDelete.clear();
//                this.projectAuthoritiesChanged = false;
//            }

            return usergroupListPath;
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation(USER_GROUP) }, logger, e);
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
            if (!this.userGroup.getUsers().isEmpty()) {
                for (User b : this.userGroup.getUsers()) {
                    b.getUserGroups().remove(this.userGroup);
                }
                this.userGroup.setUsers(new ArrayList<>());
                this.serviceManager.getUserGroupService().save(this.userGroup);
            }
            if (!this.userGroup.getTasks().isEmpty()) {
                Helper.setErrorMessage("userGroupAssignedError");
                return null;
            }
            if (!this.userGroup.getAuthorities().isEmpty()) {
                this.userGroup.setAuthorities(new ArrayList<>());
                this.serviceManager.getUserGroupService().save(this.userGroup);
            }
            this.serviceManager.getUserGroupService().remove(this.userGroup);
        } catch (DataException e) {
            Helper.setErrorMessage("errorDeleting", new Object[] {Helper.getTranslation(USER_GROUP) }, logger, e);
            return null;
        }
        return usergroupListPath;
    }

    /**
     * Method being used as viewAction for user group edit form. Selectable clients
     * and projects are initialized as well.
     *
     * @param id
     *            ID of the user group to load
     */
    public void loadUserGroup(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setUserGroup(this.serviceManager.getUserGroupService().getById(id));
            }
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation(USER_GROUP), id },
                logger, e);
        }
        setSaveDisabled(true);
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
        this.userGroup = userGroup;
    }

    /**
     * Return the list of available authorization levels and the list of authority
     * levels currently assigned to 'userGroup' as a combined 'DualListModel' that
     * is used by the frontend for authority management of user groups utilizing a
     * PrimeFaces PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getGlobalAuthorities() {
        List<Authority> assignedAuthorities = userGroup.getGlobalAuthorities();
        List<Authority> availableAuthorities = new ArrayList<>();
        try {
            availableAuthorities = serviceManager.getAuthorityService().getAll();
            availableAuthorities.removeAll(assignedAuthorities);
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
        }
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
        this.userGroup.setAuthorities(authorities.getTarget());
    }

    /**
     * Return the list of available authorization levels which can be assigned
     * client specific and the list of authority levels currently client specific
     * assigned to 'userGroup' as a combined 'DualListModel' that is used by the
     * frontend for authority management of user groups utilizing a PrimeFaces
     * PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getClientAssignableAuthorities() {
        List<Authority> assignedAuthorities = this.userGroup.getClientAuthorities();
        List<Authority> availableAuthorities = null;
        try {
            availableAuthorities = serviceManager.getAuthorityService().getAllAssignableToClients();
            availableAuthorities.removeAll(assignedAuthorities);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return new DualListModel<>(availableAuthorities, assignedAuthorities);
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'userGroup' using a PrimeFaces PickList object.
     *
     * @param clientAuthoritiesModel
     *            list of authority assigned to 'userGroup'
     */
    public void setClientAssignableAuthorities(DualListModel<Authority> clientAuthoritiesModel) {
        for (Authority authority : clientAuthoritiesModel.getSource()) {
            userGroup.getAuthorities().remove(authority);
        }
        for (Authority authority : clientAuthoritiesModel.getTarget()) {
            if (!userGroup.getAuthorities().contains(authority)) {
                userGroup.getAuthorities().add(authority);
            }
        }
    }

    /**
     * Return the list of available authority levels and the list of authority
     * levels currently assigned to 'userGroup' as a combined 'DualListModel' that
     * is used by the frontend for authority management of user groups utilizing a
     * PrimeFaces PickList object.
     *
     * @return DualListModel of available and assigned authority levels
     */
    public DualListModel<Authority> getProjectAssignableAuthorities() {
        List<Authority> assignedAuthorities = this.userGroup.getProjectAuthorities();
        List<Authority> availableAuthorities = null;
        try {
            availableAuthorities = serviceManager.getAuthorityService().getAllAssignableToProjects();
            availableAuthorities.removeAll(assignedAuthorities);
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return new DualListModel<>(availableAuthorities, assignedAuthorities);
    }

    /**
     * Assign the target property of given DualListModel of authorities to
     * 'userGroup' in using a PrimeFaces PickList object.
     *
     * @param projectAuthoritiesModel
     *            list of authority assigned to 'userGroup'
     */
    public void setProjectAssignableAuthorities(DualListModel<Authority> projectAuthoritiesModel) {
        for (Authority authority : projectAuthoritiesModel.getSource()) {
            userGroup.getAuthorities().remove(authority);
        }
        for (Authority authority : projectAuthoritiesModel.getTarget()) {
            if (!userGroup.getAuthorities().contains(authority)) {
                userGroup.getAuthorities().add(authority);
            }
        }
    }
}
