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

import java.io.Serializable;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.kitodo.data.database.beans.User;
import org.kitodo.services.ServiceManager;

@Named("LoginForm")
@SessionScoped
public class LoginForm implements Serializable {
    private static final long serialVersionUID = 7732045664713555233L;
    private User loggedUser;
    private boolean alreadyLoggedIn = false;
    private transient ServiceManager serviceManager = new ServiceManager();
    private boolean firstVisit = true;

    /**
     * Gets current authenticated User.
     *
     * @return The user object or null if no user is authenticated.
     */
    public User getLoggedUser() {
        if (loggedUser != null) {
            return this.loggedUser;
        } else {
            this.loggedUser = serviceManager.getUserService().getAuthenticatedUser();
            return this.loggedUser;
        }
    }

    public void setLoggedUser(User myClass) {
        this.loggedUser = myClass;
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

    /**
     * Redirect to desktop if user is already logged in.
     *
     * @return path to desktop
     */
    public String redirectToDesktop() {
        if (Objects.nonNull(this.loggedUser)) {
            return "desktop";
        } else {
            return "login";
        }

    }
}
