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

import java.util.HashMap;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.push.Push;
import javax.faces.push.PushContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.kitodo.data.database.beans.User;
import org.kitodo.production.security.SecuritySession;
import org.kitodo.production.services.ServiceManager;

@Named("SystemWarningForm")
@ApplicationScoped
public class SystemWarningForm {

    @Inject
    @Push
    PushContext warningChannel;

    private static final String SHOW_WARNING = "showWarning";
    private static final String UPDATE_USER_TABLE = "updateUserTable";

    private String warning = "";
    private String userName = "";
    private HashMap<String, Boolean> currentUsers;

    public void setWarning(String warningText) {
        this.warning = warningText;
    }

    /**
     * Get warning String.
     *
     * @return warning String
     */
    public String getWarning() {
        return this.warning;
    }

    /**
     * Send warning String through warningChannel to connected clients.
     */
    public void sendWarning() {
        resetCurrentUsersStatus();
        warningChannel.send(SHOW_WARNING);
    }

    /**
     * Get user name.
     *
     * @return user name
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Set user name
     *
     * @param name user name
     */
    public void setUserName(String name) {
        this.userName = name;
    }

    /**
     * Load list of current users.
     */
    public void loadCurrentUsers() {
        this.currentUsers = new HashMap<>();
        for (SecuritySession session : ServiceManager.getSessionService().getActiveSessions()) {
            this.currentUsers.put(session.getUserName(), null);
        }
    }

    /**
     * Reset status of current users.
     */
    private void resetCurrentUsersStatus() {
        this.currentUsers.replaceAll((n, v) -> false);
    }

    /**
     * Get list of current users.
     *
     * @return list of current users
     */
    public Set<String> getCurrentUsers() {
        return this.currentUsers.keySet();
    }

    /**
     * Acknowledge system message.
     *
     * @param user User that acknowledges system message
     */
    public void acknowledge(User user) {
        this.currentUsers.put(user.getLogin(), true);
        warningChannel.send(UPDATE_USER_TABLE);
    }

    /**
     * Return whether User with user name 'userName' acknowledged system message or not.
     *
     * @param userName user name of user to check
     * @return whether user acknowledged system message or not
     */
    public Boolean acknowledged(String userName) {
        return this.currentUsers.get(userName);
    }
}
