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

import de.sub.goobi.helper.Helper;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.security.DynamicAuthenticationProvider;
import org.kitodo.services.ServiceManager;

@Named("LoginForm")
@SessionScoped
public class LoginForm implements Serializable {
    private static final long serialVersionUID = 7732045664713555233L;
    private String login;
    private String password;
    private User myBenutzer;
    private boolean alreadyLoggedIn = false;
    private String passwordChanged;
    private String passwordChangedRepeat;
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(LoginForm.class);
    private boolean firstVisit = true;

    /**
     * Save changed password at database and in case Ldap authentication is active
     * also on ldap server.
     *
     */
    public void saveChangedPassword() {
        if (!this.passwordChanged.equals(this.passwordChangedRepeat)) {
            Helper.setErrorMessage("passwordsDontMatch");
        } else {
            try {
                if (DynamicAuthenticationProvider.getInstance().isLdapAuthentication()) {
                    serviceManager.getLdapServerService().changeUserPassword(this.myBenutzer, this.passwordChanged);
                }
                serviceManager.getUserService().changeUserPassword(this.myBenutzer, this.passwordChanged);
                Helper.setMessage("passwordChanged");
            } catch (DataException e) {
                Helper.setErrorMessage("errorSaving", new Object[] {"user" }, logger, e);
            } catch (NoSuchAlgorithmException e) {
                Helper.setErrorMessage("ldap error", logger, e);
            }
        }
    }

    /**
     * Save user configuration.
     *
     */
    public void saveUser() {
        try {
            serviceManager.getUserService().save(this.myBenutzer);
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("user") }, logger, e);
        }
    }

    /*
     * Getter und Setter
     */

    public String getLogin() {
        return this.login;
    }

    /**
     * Set login.
     *
     * @param login
     *            String
     */
    public void setLogin(String login) {
        if (this.login != null && !this.login.equals(login)) {
            this.alreadyLoggedIn = false;
        }
        this.login = login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets current authenticated User.
     *
     * @return The user object or null if no user is authenticated.
     */
    public User getMyBenutzer() {
        if (myBenutzer != null) {
            return this.myBenutzer;
        } else {
            this.myBenutzer = serviceManager.getUserService().getAuthenticatedUser();
            return this.myBenutzer;
        }
    }

    public void setMyBenutzer(User myClass) {
        this.myBenutzer = myClass;
    }

    /**
     * Get changed password.
     * 
     * @return changed password
     */
    public String getPasswordChanged() {
        return this.passwordChanged;
    }

    /**
     * Set changed password.
     * 
     * @param passwordChanged
     *            as String
     */
    public void setPasswordChanged(String passwordChanged) {
        this.passwordChanged = passwordChanged;
    }

    /**
     * Get repeated changed password.
     * 
     * @return repeated changed password
     */
    public String getPasswordChangedRepeat() {
        return this.passwordChangedRepeat;
    }

    /**
     * Set repeated changed password.
     * 
     * @param passwordChangedRepeat
     *            as String
     */
    public void setPasswordChangedRepeat(String passwordChangedRepeat) {
        this.passwordChangedRepeat = passwordChangedRepeat;
    }

    /**
     * Check if user is already logged im.
     * 
     * @return true or false
     */
    public boolean isAlreadyLoggedIn() {
        return this.alreadyLoggedIn;
    }

    /**
     * Checks and returns whether this is the first time the user visits a Kitodo
     * page in the current session or not. Makes use of the fact that "LoginForm" is
     * a SessionScoped bean.
     *
     * @return whether this is the users first visit to Kitodo during the current
     *         session or not
     */
    public boolean isFirstVisit() {
        boolean visit = firstVisit;
        if (firstVisit) {
            firstVisit = false;
        }
        return visit;
    }
}
