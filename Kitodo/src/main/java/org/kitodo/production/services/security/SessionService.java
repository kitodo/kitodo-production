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

package org.kitodo.production.services.security;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.kitodo.production.metadata.MetadataLock;
import org.kitodo.production.security.SecurityConfig;
import org.kitodo.production.security.SecuritySession;
import org.kitodo.production.security.SecurityUserDetails;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;

public class SessionService {

    private static volatile SessionService instance = null;
    private final SessionRegistry sessionRegistry;

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
        MetadataLock.setAllUserLocksFree(user.getUsername());
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
            if (Objects.nonNull(user)) {
                List<SessionInformation> activeSessionInformation = new ArrayList<>(sessionRegistry.getAllSessions(principal, false));

                for (SessionInformation sessionInformation : activeSessionInformation) {
                    SecuritySession securitySession = new SecuritySession();
                    securitySession.setUserName(user.getUsername());
                    securitySession.setSessionId(sessionInformation.getSessionId());
                    securitySession.setLastRequest(sessionInformation.getLastRequest().toInstant()
                            .atZone(ZoneId.systemDefault()).toLocalDateTime());

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
        SessionService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (SessionService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new SessionService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }
}
