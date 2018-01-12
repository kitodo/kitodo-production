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

import java.util.List;
import java.util.Objects;

import org.kitodo.security.SecurityConfig;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

public class SessionService {

    private static SessionService instance = null;

    /**
     * Expires all active sessions of a spring security UserDetails object.
     *
     * @param user
     *      The UserDetails Object.
     */
    public void expireSessionsOfUser(UserDetails user) {
        WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
        SecurityConfig securityConfig = context.getBean(SecurityConfig.class);
        SessionRegistry sessionRegistry = securityConfig.getSessionRegistry();

        List<SessionInformation> activeUserSessions = sessionRegistry.getAllSessions(user,false);
        for (SessionInformation sessionInformation : activeUserSessions) {
            sessionInformation.expireNow();
        }
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
