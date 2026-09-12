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

package org.kitodo.production.forms.user;

import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.logintask.LoginTaskStatus;
import org.kitodo.api.logintask.LoginTaskType;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.LoginTask;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseTabEditView;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.security.DynamicAuthenticationProvider;
import org.kitodo.production.security.password.KitodoPassword;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.LoginTaskService;
import org.kitodo.production.services.data.UserService;
import org.primefaces.PrimeFaces;

@Named("UserEditViewDetailsTab")
@ViewScoped
public class UserEditViewDetailsTab extends BaseTabEditView<User> {
    
    /**
     * The user object that is being edited (variable "user" references to the user currently logged in, see BaseForm).
     */
    private User userObject;
    
    private static final Logger logger = LogManager.getLogger(UserEditViewDetailsTab.class);

    private final transient UserService userService = ServiceManager.getUserService();
    private final transient LoginTaskService loginTaskService = ServiceManager.getLoginTaskService();

    private String passwordToEncrypt;
    private String oldPassword;
    private List<Client> clientsOfUser;

    private Optional<LoginTask> lastLdapLoginTask;

    /**
     * Return user object currently being edited.
     * 
     * @return the user currently being edited
     */
    public User getUserObject() {
        return this.userObject;
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

    /**
     * Retrieves the list of clients associated with the current user, sorted
     * based on predefined criteria.
     *
     * @return a list of sorted {@code Client} objects associated with the user
     */
    public List<Client> getUserClientsSorted() {
        return clientsOfUser;
    }

    /**
     * Return empty string. Returning the actual password is never required, but GUI needs a getter for form fields.
     *
     * @return Empty string.
     */
    public String getPasswordToEncrypt() {
        return "";
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
     * Method that is called from viewAction of user edit form.
     *
     * @param userObject
     *            the user currently being edited
     */
    @Override
    public void load(User userObject) {
        this.userObject = userObject;
        this.clientsOfUser = UserService.getClientsOfUserSorted(userObject);
        passwordToEncrypt = "";
        loadLastLdapLoginTask();
    }

    /**
     * Save detail information of a user if there is no other user with the same login.
     *
     * @return true if user information can be saved, else false
     */
    @Override
    public boolean save() {
        if (Objects.isNull(userObject.getId())) {
            Set<ConstraintViolation<KitodoPassword>> passwordViolations = getPasswordViolations();
            if (!passwordViolations.isEmpty()) {
                for (ConstraintViolation<KitodoPassword> passwordViolation : passwordViolations) {
                    Helper.setErrorMessage(passwordViolation.getMessage());
                }
                return false;
            }
        }

        String login = this.userObject.getLogin();
        if (!isUserExistingOrLoginValid(login)) {
            Helper.setErrorMessage("loginNotValid", new Object[] {login });
            return false;
        }

        if (userObject.getClients().isEmpty()) {
            Helper.setErrorMessage("errorMissingClient");
            return false;
        }

        try {
            if (userService.getAmountOfUsersWithExactlyTheSameLogin(this.userObject.getId(), login) > 0) {
                Helper.setErrorMessage("loginInUse");
                return false;
            }
        } catch (DAOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.USER.getTranslationSingular() }, logger, e);
            return false;
        }

        // save the password only when user is created else changePasswordForCurrentUser is used
        if (Objects.isNull(userObject.getId()) && Objects.nonNull(passwordToEncrypt)) {
            userObject.setPassword(userService.getPasswordEncoder().encode(passwordToEncrypt));
        }

        return true;
    }

    /**
     * Check and return whether LDAP group and LDAP server are configured for current user
     * and if LDAP server is read only.
     *
     * @return whether LDAP server is configured and read only
     */
    public boolean isLdapServerReadOnly() {
        if (Objects.nonNull(this.userObject)
                && Objects.nonNull(this.userObject.getLdapGroup())
                && Objects.nonNull(this.userObject.getLdapGroup().getLdapServer())) {
            return this.userObject.getLdapGroup().getLdapServer().isReadOnly();
        } else {
            return false;
        }
    }

    /**
     * Return label for ldap login task button.
     * 
     * @return the label
     */
    public String getLdapLoginTaskButtonLabel() {
        String label = Helper.getTranslation("ldapWriteConfiguration");
        if (Objects.isNull(lastLdapLoginTask) || lastLdapLoginTask.isEmpty()) {
            return label;
        }

        boolean lastSuccessful = LoginTaskStatus.COMPLETED.equals(lastLdapLoginTask.get().getStatus());
        boolean lastFailed = LoginTaskStatus.FAILED.equals(lastLdapLoginTask.get().getStatus()); 

        if (lastSuccessful) {
            Date lastExecutionDate = lastLdapLoginTask.get().getExecutedAt();
            label = Helper.getTranslation("ldapWriteConfigurationSuccessful", Helper.getDateAsFormattedString(lastExecutionDate));
        } else if (lastFailed) {
            String lastError = lastLdapLoginTask.get().getError();
            label = Helper.getTranslation("ldapWriteConfigurationFailed", lastError);
        }

        return label;
    }

    /**
     * Return true if the user has a pending ldap login task.
     * 
     * <p>Will disable the button to add a new ldap login task</p>
     */
    public boolean hasPendingLdapLoginTask() {
        return Objects.nonNull(lastLdapLoginTask) 
            && lastLdapLoginTask.isPresent() 
            && LoginTaskStatus.PENDING.equals(lastLdapLoginTask.get().getStatus());
    }

    /**
     * User requests to add a new ldap login task to write user credentials to the ldap server.
     */
    public void addLdapLoginTask() {
        loginTaskService.addLoginTask(this.userObject, LoginTaskType.SAVE_USER_TO_LDAP);
        loadLastLdapLoginTask();
    }

    /**
     * User cancels the the last pending ldap login task.
     */
    public void cancelLdapLoginTask() {
        if (Objects.nonNull(this.lastLdapLoginTask) && this.lastLdapLoginTask.isPresent()) {
            loginTaskService.cancelLoginTask(this.lastLdapLoginTask.get());
        }
        loadLastLdapLoginTask();
    }

    /**
     * Retrieve whether the current user already has a pending login task to save user credentials to the ldap server.
     * 
     * @return true if pending login task exists
     */
    private void loadLastLdapLoginTask() {
        this.lastLdapLoginTask = loginTaskService.getLastLoginTaskForUserAndType(this.userObject, LoginTaskType.SAVE_USER_TO_LDAP);
    }

    /**
     * Changes the password for current user in database and in case Ldap
     * authentication is active also on ldap server.
     */
    public void changePasswordForCurrentUser() {
        if (!isOldPasswordValid()) {
            Helper.setErrorMessage("passwordsDontMatchOld");
        } else {
            try {
                Set<ConstraintViolation<KitodoPassword>> passwordViolations = getPasswordViolations();
                if (passwordViolations.isEmpty()) {
                    if (DynamicAuthenticationProvider.getInstance().isLdapAuthentication()
                            && Objects.nonNull(userObject.getLdapGroup())) {
                        ServiceManager.getLdapServerService().changeUserPassword(userObject, passwordToEncrypt);
                    }
                    // NOTE: password has to be changed in database in any case because of a bug in LdapServerService
                    userService.changeUserPassword(userObject, this.passwordToEncrypt);
                    Helper.setMessage("passwordChanged");
                    PrimeFaces.current().executeScript("PF('resetPasswordDialog').hide();");
                } else {
                    for (ConstraintViolation<KitodoPassword> passwordViolation : passwordViolations) {
                        Helper.setErrorMessage(passwordViolation.getMessage());
                    }
                }
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[]{ObjectType.USER.getTranslationSingular()}, logger, e);
            } catch (NoSuchAlgorithmException e) {
                Helper.setErrorMessage("ldap error", logger, e);
            }
        }
    }

    /**
     * Returns true if the currently edited user is an existing user or the username (login) is valid to be saved as a new user.
     * 
     * @param login the username (login) of the currently edited user object
     * @return true if user already exists or username id valid and follows rules
     */
    private boolean isUserExistingOrLoginValid(String login) {
        return Objects.nonNull(userObject.getId()) || userService.isLoginValid(login);
    }

    /**
     * Validate the entered password.
     * 
     * @return the set of validation violations
     */
    private Set<ConstraintViolation<KitodoPassword>> getPasswordViolations() {
        if (isLdapServerReadOnly()) {
            return Collections.emptySet();
        }
        KitodoPassword validPassword = new KitodoPassword(passwordToEncrypt);
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            return validator.validate(validPassword);
        }
    }

    /**
     * Check whether the old password matches the password stored in the database.
     * 
     * @return true if the old password does match the password stored in the database
     */
    private boolean isOldPasswordValid() {
        if (ServiceManager.getSecurityAccessService().hasAuthorityToEditUser()) {
            // user has admin rights and old password doesn't matter
            return true;
        }
        return userService.getPasswordEncoder().matches(this.oldPassword, this.userObject.getPassword());
    }

}
