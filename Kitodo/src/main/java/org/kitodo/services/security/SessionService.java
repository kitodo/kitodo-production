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

package org.kitodo.services.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.joda.time.LocalDateTime;
import org.kitodo.security.SecurityConfig;
import org.kitodo.security.SecuritySession;
import org.kitodo.security.SecurityUserDetails;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;

public class SessionService {

    private static SessionService instance = null;
    private SessionRegistry sessionRegistry;

    /**
     * Private constructor which is getting the SessionRegistry from the main SecurityConfig class.
     */
    private SessionService() {
        SecurityConfig securityConfig = SecurityConfig.getInstance();
        this.sessionRegistry = securityConfig.getSessionRegistry();
    }

    /**
     * Expires all active sessions of a spring security UserDetails object.
     *
     * @param user
     *            The UserDetails Object.
     */
    public void expireSessionsOfUser(UserDetails user) {
        List<SessionInformation> activeUserSessions = sessionRegistry.getAllSessions(user, false);
        for (SessionInformation sessionInformation : activeUserSessions) {
            sessionInformation.expireNow();
        }
    }

    /**
     * Gets all active sessions.
     *
     * @return The active sessions.
     */
    public List<SecuritySession> getActiveSessions() {
        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();
        List<SecuritySession> activeSessions = new ArrayList<>();
        UserDetails user = null;

        for (final Object principal : allPrincipals) {
            if (principal instanceof SecurityUserDetails) {
                user = (SecurityUserDetails) principal;
            }
            if (user != null) {
                List<SessionInformation> activeSessionInformation = new ArrayList<>(sessionRegistry.getAllSessions(principal, false));

                for (SessionInformation sessionInformation : activeSessionInformation) {
                    SecuritySession securitySession = new SecuritySession();
                    securitySession.setUserName(user.getUsername());
                    securitySession.setSessionId(sessionInformation.getSessionId());
                    securitySession.setLastRequest(new LocalDateTime(sessionInformation.getLastRequest()));

                    activeSessions.add(securitySession);
                }
            }
        }
        return activeSessions;
    }

    /**
     * Return singleton variable of type SessionService.
     *
     * @return unique instance of SessionService
     */
    public static SessionService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (SessionService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new SessionService();
                }
            }
        }
        return instance;
    }
}
