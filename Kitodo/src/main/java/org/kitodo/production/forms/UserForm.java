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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.security.DynamicAuthenticationProvider;
import org.kitodo.production.security.SecuritySession;
import org.kitodo.production.security.password.KitodoPassword;
import org.kitodo.production.security.password.SecurityPasswordEncoder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.UserService;

@Named("UserForm")
@SessionScoped
public class UserForm extends BaseForm {
    private User userObject = new User();
    private boolean hideInactiveUsers = true;
    private static final Logger logger = LogManager.getLogger(UserForm.class);
    private transient SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();
    private transient UserService userService = ServiceManager.getUserService();

    private String passwordToEncrypt;

    private String oldPassword;

    @Named("LoginForm")
    private LoginForm loginForm;

    private final String userListPath = MessageFormat.format(REDIRECT_PATH, "users");
    private final String userEditPath = MessageFormat.format(REDIRECT_PATH, "userEdit");

    /**
     * Default constructor with inject login form that also sets the LazyDTOModel
     * instance of this bean.
     *
     * @param loginForm
     *            is used for update logged user in case updated user is currently
     *            logged user
     */
    @Inject
    public UserForm(LoginForm loginForm) {
        super();
        super.setLazyDTOModel(new LazyDTOModel(userService));
        this.loginForm = loginForm;
    }

    /**
     * Initialize the list of displayed list columns.
     */
    @PostConstruct
    public void init() {
        columns = new ArrayList<>();
        try {
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("user"));
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("role"));
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("client"));
            columns.add(ServiceManager.getListColumnService().getListColumnsForListAsSelectItemGroup("ldapgroup"));
        } catch (DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        selectedColumns = new ArrayList<>();
        selectedColumns.addAll(ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("user"));
        selectedColumns.addAll(ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("role"));
        selectedColumns.addAll(ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("client"));
        selectedColumns
                .addAll(ServiceManager.getListColumnService().getSelectedListColumnsForListAndClient("ldapgroup"));
    }

    /**
     * New user.
     *
     * @return page
     */
    public String newUser() {
        this.userObject = new User();
        List<Client> clients = new ArrayList<>();
        clients.add(ServiceManager.getUserService().getSessionClientOfAuthenticatedUser());
        this.userObject.setClients(clients);
        this.userObject.setName("");
        this.userObject.setSurname("");
        this.userObject.setLogin("");
        this.userObject.setLdapLogin("");
        this.userObject.setPassword("");
        return userEditPath;
    }

    /**
     * Save user if there is not other user with the same login.
     *
     * @return page or empty String
     */
    public String save() {
        Set<ConstraintViolation<KitodoPassword>> passwordViolations = getPasswordViolations();
        if(passwordViolations.isEmpty()) {
            for (ConstraintViolation<KitodoPassword> passwordViolation : passwordViolations) {
                Helper.setErrorMessage(passwordViolation.getMessage());
            }
            return this.stayOnCurrentPage;
        }

        String login = this.userObject.getLogin();
        if (!isUserExistingOrLoginValid(login)) {
            Helper.setErrorMessage("loginNotValid", new Object[] {login });
            return this.stayOnCurrentPage;
        }

        if (isMissingClient()) {
            Helper.setErrorMessage("errorMissingClient");
            return this.stayOnCurrentPage;
        }

        try {
            if (userService.getAmountOfUsersWithExactlyTheSameLogin(this.userObject.getId(), login) == 0) {
                if (Objects.nonNull(this.passwordToEncrypt)) {
                    this.userObject.setPassword(passwordEncoder.encrypt(this.passwordToEncrypt));
                }
                userService.saveToDatabase(this.userObject);

                if (userService.getAuthenticatedUser().getId().equals(this.userObject.getId())) {
                    loginForm.setLoggedUser(this.userObject);
                }

                return userListPath;
            } else {
                Helper.setErrorMessage("loginInUse");
                return this.stayOnCurrentPage;
            }
        } catch (DAOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.USER.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    private boolean isUserExistingOrLoginValid(String login) {
        return Objects.nonNull(userObject.getId()) || userService.isLoginValid(login);
    }

    private Set<ConstraintViolation<KitodoPassword>> getPasswordViolations() {
        KitodoPassword validPassword = new KitodoPassword(passwordToEncrypt);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator.validate(validPassword);
    }

    private boolean isMissingClient() {
        return this.userObject.getClients().isEmpty();
    }

    /**
     * Deletes a user account.
     *
     * <p>
     * Please note that deleting a user in Production will not delete the
     * user from a connected LDAP service.
     */
    public void delete() {
        try {
            userService.removeFromDatabase(userObject);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.USER.getTranslationSingular() }, logger, e);
        }
    }

    /**
     * Remove from role.
     *
     * @return empty String
     */
    public String deleteFromRole() {
        try {
            int roleId = Integer.parseInt(Helper.getRequestParameter("ID"));
            for (Role role : this.userObject.getRoles()) {
                if (role.getId().equals(roleId)) {
                    this.userObject.getRoles().remove(role);
                    break;
                }
            }
        } catch (NumberFormatException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Add to role.
     *
     * @return stay on the same page
     */
    public String addToRole() {
        int roleId = 0;
        try {
            roleId = Integer.parseInt(Helper.getRequestParameter("ID"));
            Role role = ServiceManager.getRoleService().getById(roleId);

            if (!this.userObject.getRoles().contains(role)) {
                this.userObject.getRoles().add(role);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DATABASE_READING,
                new Object[] {ObjectType.ROLE.getTranslationSingular(), roleId }, logger, e);
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
                    break;
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
            Client client = ServiceManager.getClientService().getById(clientId);

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
                    break;
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
            Project project = ServiceManager.getProjectService().getById(projectId);

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
            this.userObject = userService.getById(userObject.getId());
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
            setUserObject(userService.getById(userID));
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
            ServiceManager.getLdapServerService().createNewUser(this.userObject,
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
                setUserObject(userService.getById(id));
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.USER.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Get map of supported metadata languages.
     *
     * @return map of supported metadata languages
     */
    public Map<String, String> getMetadataLanguages() {
        Map<String, String> metadataLanguages = new HashMap<>();
        String[] availableMetadataLanguages = ConfigCore.getStringArrayParameter(ParameterCore.METADATA_LANGUAGE_LIST);
        for (String availableLanguage : availableMetadataLanguages) {
            String[] language = availableLanguage.split("-");
            metadataLanguages.put(language[0], language[1]);
        }
        return metadataLanguages;
    }

    /**
     * Return list of projects available for assignment to the user.
     *
     * @return list of projects available for assignment to the user
     */
    public List<ProjectDTO> getProjects() {
        try {
            return ServiceManager.getProjectService().findAllAvailableForAssignToUser(this.userObject);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.PROJECT.getTranslationPlural() },
                logger, e);
            return new LinkedList<>();
        }
    }

    /**
     * Return list of roles available for assignment to the user.
     *
     * @return list of roles available for assignment to the user
     */
    public List<Role> getRoles() {
        try {
            return ServiceManager.getRoleService().getAllAvailableForAssignToUser(this.userObject);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.ROLE.getTranslationPlural() }, logger,
                e);
            return new LinkedList<>();
        }
    }

    /**
     * Return list of clients available for assignment to the user.
     *
     * @return list of clients available for assignment to the user
     */
    public List<Client> getClients() {
        try {
            return ServiceManager.getClientService().getAllAvailableForAssignToUser(this.userObject);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.CLIENT.getTranslationPlural() }, logger,
                    e);
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
    public boolean checkUserLoggedIn(User user) {
        for (SecuritySession securitySession : ServiceManager.getSessionService().getActiveSessions()) {
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
        if (isOldPasswordInvalid()) {
            Helper.setErrorMessage("passwordsDontMatchOld");
        }
        try {
            if (DynamicAuthenticationProvider.getInstance().isLdapAuthentication()) {
                ServiceManager.getLdapServerService().changeUserPassword(userObject, this.passwordToEncrypt);
            }
            userService.changeUserPassword(userObject, this.passwordToEncrypt);
            Helper.setMessage("passwordChanged");
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.USER.getTranslationSingular() }, logger, e);
        } catch (NoSuchAlgorithmException e) {
            Helper.setErrorMessage("ldap error", logger, e);
        }
    }

    private boolean isOldPasswordInvalid() {
        if (!ServiceManager.getSecurityAccessService().hasAuthorityToEditUser()) {
            return !Objects.equals(this.oldPassword, passwordEncoder.decrypt(this.userObject.getPassword()));
        }
        return false;
    }

    /**
     * Get old password.
     *
     * @return value of oldPassword
     */
    public String getOldPassword() {
        return oldPassword;
    }

    /**
     * Set old password.
     *
     * @param oldPassword
     *            as java.lang.String
     */
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
