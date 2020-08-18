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

package org.kitodo.production.security;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class for storing information of user sessions.
 */
public class SecuritySession {
    private String sessionId;
    private String userName;
    private LocalDateTime lastRequest;

    /**
     * Gets sessionId.
     *
     * @return The sessionId.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets sessionId.
     *
     * @param sessionId The sessionId.
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Gets userName.
     *
     * @return The userName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets userName.
     *
     * @param userName The userName.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Gets lastRequest.
     *
     * @return The lastRequest.
     */
    public LocalDateTime getLastRequest() {
        return lastRequest;
    }

    /**
     * Gets lastRequest.
     *
     * @return The lastRequest.
     */
    public String getLastRequestAsString() {
        return lastRequest.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Sets lastRequest.
     *
     * @param lastRequest The lastRequest.
     */
    public void setLastRequest(LocalDateTime lastRequest) {
        this.lastRequest = lastRequest;
    }
}
