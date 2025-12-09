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

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.security.DynamicAuthenticationProvider;
import org.kitodo.production.security.password.KitodoPassword;
import org.kitodo.production.security.password.SecurityPasswordEncoder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.UserService;
import org.primefaces.PrimeFaces;

@Named("UserEditViewDetailsTab")
@ViewScoped
public class UserEditViewDetailsTab extends BaseForm {
    
    /**
     * The user object that is being edited (variable "user" references to the user currently logged in, see BaseForm).
     */
    private User userObject;
    
    private static final Logger logger = LogManager.getLogger(UserEditViewDetailsTab.class);

    private final transient SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();
    private final transient UserService userService = ServiceManager.getUserService();

    private String passwordToEncrypt;
    private String oldPassword;

    /**
     * Return user object currently being edited.
     * 
     * @return the user currently being edited
     */
    public User getUserObject() {
        return this.userObject;
    }

    /**
     * Method that is called from viewAction of user edit form.
     *
     * @param userObject
     *            the user currently being edited
     */
    public void load(User userObject) {
        this.userObject = userObject;
        passwordToEncrypt = "";
    }

    /**
     * Save details information of a user if there is no other user with the same login.
     *
     * @return true if user information can be saved, else false
     */
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
            userObject.setPassword(passwordEncoder.encrypt(passwordToEncrypt));
        }

        return true;
    }

    private boolean isUserExistingOrLoginValid(String login) {
        return Objects.nonNull(userObject.getId()) || userService.isLoginValid(login);
    }

    private Set<ConstraintViolation<KitodoPassword>> getPasswordViolations() {
        if (isLdapServerReadOnly()) {
            return Collections.emptySet();
        }
        KitodoPassword validPassword = new KitodoPassword(passwordToEncrypt);
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        return validator.validate(validPassword);
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
     * Changes the password for current user in database and in case Ldap
     * authentication is active also on ldap server.
     */
    public void changePasswordForCurrentUser() {
        if (isOldPasswordInvalid()) {
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

    /**
     * Retrieves the list of clients associated with the current user, sorted
     * based on predefined criteria.
     *
     * @return a list of sorted {@code Client} objects associated with the user
     */
    public List<Client> getUserClientsSorted() {
        return UserService.getClientsOfUserSorted(userObject);
    }

}
