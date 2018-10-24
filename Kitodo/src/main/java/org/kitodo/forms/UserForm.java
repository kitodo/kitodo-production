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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.dto.RoleDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.helper.Helper;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.security.DynamicAuthenticationProvider;
import org.kitodo.security.SecuritySession;
import org.kitodo.security.password.SecurityPasswordEncoder;
import org.kitodo.security.password.ValidPassword;

@Named("UserForm")
@SessionScoped
public class UserForm extends BaseForm {
    private static final long serialVersionUID = -3635859455444639614L;
    private User userObject = new User();
    private boolean hideInactiveUsers = true;
    private static final Logger logger = LogManager.getLogger(UserForm.class);
    private transient SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();

    @ValidPassword
    private String passwordToEncrypt;

    private String userListPath = MessageFormat.format(REDIRECT_PATH, "users");
    private String userEditPath = MessageFormat.format(REDIRECT_PATH, "userEdit");

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of
     * this bean.
     */
    public UserForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getUserService()));
    }

    /**
     * New user.
     *
     * @return page
     */
    public String newUser() {
        this.userObject = new User();
        this.userObject.setName("");
        this.userObject.setSurname("");
        this.userObject.setLogin("");
        this.userObject.setLdapLogin("");
        this.userObject.setPassword("");
        setPasswordToEncrypt("");
        return userEditPath;
    }

    /**
     * Save user if there is not other user with the same login.
     *
     * @return page or empty String
     */
    public String save() {
        String login = this.userObject.getLogin();

        if (!isLoginValid(login)) {
            return this.stayOnCurrentPage;
        }

        if (isMissingClient()) {
            Helper.setErrorMessage("errorMissingClient");
            return this.stayOnCurrentPage;
        }

        try {
            if (this.serviceManager.getUserService().getAmountOfUsersWithExactlyTheSameLogin(getUserId(), login) == 0) {
                if (Objects.nonNull(this.passwordToEncrypt)) {
                    this.userObject.setPassword(passwordEncoder.encrypt(this.passwordToEncrypt));
                }
                this.serviceManager.getUserService().save(this.userObject);
                return userListPath;
            } else {
                Helper.setErrorMessage("loginInUse");
                return this.stayOnCurrentPage;
            }
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.USER.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    private boolean isLoginValid(String inLogin) {
        return serviceManager.getUserService().isLoginValid(inLogin);
    }

    private boolean isMissingClient() {
        return this.userObject.getClients().isEmpty();
    }

    private String getUserId() {
        if (this.userObject.getId() != null) {
            return this.userObject.getId().toString();
        }
        return null;
    }

    /**
     * The function delete() deletes a user account.
     *
     * <p>
     * Please note that deleting a user in goobi.production will not delete the
     * user from a connected LDAP service.
     * </p>
     */
    public void delete() {
        try {
            serviceManager.getUserService().remove(userObject);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.USER.getTranslationSingular() }, logger, e);
        }
    }

    /**
     * Remove from user group.
     *
     * @return empty String
     */
    public String deleteFromUserGroup() {
        try {
            int userGroupId = Integer.parseInt(Helper.getRequestParameter("ID"));
            for (Role userGroup : this.userObject.getRoles()) {
                if (userGroup.getId().equals(userGroupId)) {
                    this.userObject.getRoles().remove(userGroup);
                }
            }
        } catch (NumberFormatException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Add to user group.
     *
     * @return empty String or null
     */
    public String addToUserGroup() {
        int userGroupId = 0;
        try {
            userGroupId = Integer.parseInt(Helper.getRequestParameter("ID"));
            Role userGroup = serviceManager.getUserGroupService().getById(userGroupId);

            if (!this.userObject.getRoles().contains(userGroup)) {
                this.userObject.getRoles().add(userGroup);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DATABASE_READING,
                new Object[] {ObjectType.USER_GROUP.getTranslationSingular(), userGroupId }, logger, e);
        } catch (NumberFormatException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Remove user from client.
     *
     * @return empty String
     */
    public String deleteFromClient() {
        try {
            int clientId = Integer.parseInt(Helper.getRequestParameter("ID"));
            for (Client client : this.userObject.getClients()) {
                if (client.getId().equals(clientId)) {
                    this.userObject.getClients().remove(client);
                }
            }
        } catch (NumberFormatException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Add client to user.
     *
     * @return null
     */
    public String addToClient() {
        int clientId = 0;
        try {
            clientId = Integer.parseInt(Helper.getRequestParameter("ID"));
            Client client = serviceManager.getClientService().getById(clientId);

            if (!this.userObject.getClients().contains(client)) {
                this.userObject.getClients().add(client);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DATABASE_READING,
                new Object[] {ObjectType.CLIENT.getTranslationSingular(), clientId }, logger, e);
        } catch (NumberFormatException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Remove user from project.
     *
     * @return empty String
     */
    public String deleteFromProject() {
        try {
            int projectId = Integer.parseInt(Helper.getRequestParameter("ID"));
            for (Project project : this.userObject.getProjects()) {
                if (project.getId().equals(projectId)) {
                    this.userObject.getProjects().remove(project);
                }
            }
        } catch (NumberFormatException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Add user to project.
     *
     * @return empty String or null
     */
    public String addToProject() {
        int projectId = 0;
        try {
            projectId = Integer.parseInt(Helper.getRequestParameter("ID"));
            Project project = serviceManager.getProjectService().getById(projectId);

            if (!this.userObject.getProjects().contains(project)) {
                this.userObject.getProjects().add(project);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DATABASE_READING,
                new Object[] {ObjectType.PROJECT.getTranslationSingular(), projectId }, logger, e);
        } catch (NumberFormatException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return this.stayOnCurrentPage;
    }

    /*
     * Getter und Setter
     */

    public User getUserObject() {
        return this.userObject;
    }

    /**
     * Set class.
     *
     * @param userObject
     *            user object
     */
    public void setUserObject(User userObject) {
        try {
            this.userObject = serviceManager.getUserService().getById(userObject.getId());
        } catch (DAOException e) {
            this.userObject = userObject;
        }
    }

    /**
     * Set user by ID.
     *
     * @param userID
     *            ID of user to set.
     */
    public void setUserById(int userID) {
        try {
            setUserObject(serviceManager.getUserService().getById(userID));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.USER.getTranslationSingular(), userID },
                logger, e);
        }
    }

    /**
     * Writes the user at ldap server.
     */
    public String writeUserAtLdapServer() {
        try {
            serviceManager.getLdapServerService().createNewUser(this.userObject,
                passwordEncoder.decrypt(this.userObject.getPassword()));
        } catch (NameAlreadyBoundException e) {
            Helper.setErrorMessage("Ldap entry already exists", logger, e);
        } catch (NoSuchAlgorithmException | NamingException | IOException | RuntimeException e) {
            Helper.setErrorMessage("Could not generate ldap entry", logger, e);
        }
        return null;
    }

    public boolean isHideInactiveUsers() {
        return this.hideInactiveUsers;
    }

    public void setHideInactiveUsers(boolean hideInactiveUsers) {
        this.hideInactiveUsers = hideInactiveUsers;
    }

    /**
     * Method being used as viewAction for user edit form.
     *
     * @param id
     *            ID of the user to load
     */
    public void load(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setUserObject(this.serviceManager.getUserService().getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.USER.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Return list of projects.
     *
     * @return list of projects
     */
    public List<ProjectDTO> getProjects() {
        try {
            return serviceManager.getProjectService().findAllAvailableForAssignToUser(this.userObject.getId());
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.PROJECT.getTranslationPlural() },
                logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Return list of user groups.
     *
     * @return list of user groups
     */
    public List<RoleDTO> getUserGroups() {
        try {
            return serviceManager.getUserGroupService().findAll();
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.USER_GROUP.getTranslationPlural() },
                logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Gets password.
     *
     * @return The password.
     */
    public String getPasswordToEncrypt() {
        return passwordToEncrypt;
    }

    /**
     * Sets password.
     *
     * @param passwordToEncrypt
     *            The password.
     */
    public void setPasswordToEncrypt(String passwordToEncrypt) {
        this.passwordToEncrypt = passwordToEncrypt;
    }

    /**
     * Check and return whether given UserDTO 'user' is logged in.
     * 
     * @param user
     *            UserDTO to check
     * @return whether given UserDTO is checked in
     */
    public boolean checkUserLoggedIn(UserDTO user) {
        for (SecuritySession securitySession : serviceManager.getSessionService().getActiveSessions()) {
            if (securitySession.getUserName().equals(user.getLogin())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Changes the password for current user in database and in case Ldap
     * authentication is active also on ldap server.
     */
    public void changePasswordForCurrentUser() {
        try {
            if (DynamicAuthenticationProvider.getInstance().isLdapAuthentication()) {
                serviceManager.getLdapServerService().changeUserPassword(userObject, this.passwordToEncrypt);
            }
            serviceManager.getUserService().changeUserPassword(userObject, this.passwordToEncrypt);
            Helper.setMessage("passwordChanged");
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.USER.getTranslationSingular() }, logger, e);
        } catch (NoSuchAlgorithmException e) {
            Helper.setErrorMessage("ldap error", logger, e);
        }
    }
}
