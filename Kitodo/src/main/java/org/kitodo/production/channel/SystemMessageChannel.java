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

package org.kitodo.production.channel;

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

@Named("SystemMessageChannel")
@ApplicationScoped
public class SystemMessageChannel {

    @Inject
    @Push
    PushContext messageChannel;

    private static final String SHOW_MESSAGE = "showMessage";
    private static final String UPDATE_USER_TABLE = "updateUserTable";

    private String userName = "";
    private String message = "";
    private HashMap<String, Boolean> currentUsers;

    private String staticSystemMessage = "";
    private Boolean showSystemMessage = false;

    /**
     * Inform connected websocket clients to show message.
     */
    public void showMessage() {
        messageChannel.send(SHOW_MESSAGE);
    }

    /**
     * Inform connected websocket clients to update user table.
     */
    public void updateUserTable() {
        messageChannel.send(UPDATE_USER_TABLE);
    }

    /**
     * Set message String.
     *
     * @param messageText message String
     */
    public void setMessage(String messageText) {
        this.message = messageText;
    }

    /**
     * Get message String.
     *
     * @return message String
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Acknowledge system message.
     *
     * @param user User that acknowledges system message
     */
    public void acknowledge(User user) {
        this.currentUsers.put(user.getLogin(), true);
        this.updateUserTable();
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
    public void resetCurrentUsersStatus() {
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
     * Return whether User with user name 'userName' acknowledged system message or not.
     *
     * @param userName user name of user to check
     * @return whether user acknowledged system message or not
     */
    public Boolean acknowledged(String userName) {
        return this.currentUsers.get(userName);
    }

    /**
     * Get user name of broadcaster.
     *
     * @return user name
     */
    public String getUserName() {
        return this.userName;
    }

    /**
     * Set user name of broadcaster.
     *
     * @param name user name
     */
    public void setUserName(String name) {
        this.userName = name;
    }

    /**
     * Send message String to connected clients.
     */
    public void sendMessage() {
        this.resetCurrentUsersStatus();
        this.showMessage();
    }

    /**
     * Get staticSystemMessage.
     *
     * @return value of staticSystemMessage
     */
    public String getStaticSystemMessage() {
        return staticSystemMessage;
    }

    /**
     * Set staticSystemMessage.
     *
     * @param staticSystemMessage as java.lang.String
     */
    public void setStaticSystemMessage(String staticSystemMessage) {
        this.staticSystemMessage = staticSystemMessage;
    }

    /**
     * Get showSystemMessage.
     *
     * @return value of showSystemMessage
     */
    public boolean isShowSystemMessage() {
        return showSystemMessage;
    }

    /**
     * Set showSystemMessage.
     *
     * @param showSystemMessage as boolean
     */
    public void setShowSystemMessage(boolean showSystemMessage) {
        this.showSystemMessage = showSystemMessage;
    }

}
