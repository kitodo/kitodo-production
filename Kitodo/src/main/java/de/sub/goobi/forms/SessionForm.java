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

package de.sub.goobi.forms;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.kitodo.security.SecurityConfig;
import org.kitodo.security.SecuritySession;
import org.kitodo.security.SecurityUserDetails;
import org.kitodo.services.ServiceManager;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

/**
 * Die Klasse SessionForm für den überblick über die aktuell offenen Sessions
 * 
 * @author Steffen Hankiewicz
 * @version 1.00 - 16.01.2005
 */
@Named
@ApplicationScoped
public class SessionForm {

    private static final Logger logger = LogManager.getLogger(SessionForm.class);
    private SessionRegistry sessionRegistry;

    /**
     * Gets all active sessions.
     *
     * @return The active sessions.
     */
    public List<SecuritySession> getActiveSessions() {

        if (sessionRegistry == null) {
            WebApplicationContext context = ContextLoader.getCurrentWebApplicationContext();
            SecurityConfig securityConfig = context.getBean(SecurityConfig.class);
            this.sessionRegistry = securityConfig.getSessionRegistry();
        }

        List<Object> allPrincipals = sessionRegistry.getAllPrincipals();

        List<SecuritySession> activeSessions = new ArrayList<>();

        for (final Object principal : allPrincipals) {
            if (principal instanceof SecurityUserDetails) {

                try {
                    SecurityUserDetails user = (SecurityUserDetails) principal;

                    List<SessionInformation> activeSessionInformations = new ArrayList<>();
                    activeSessionInformations.addAll(sessionRegistry.getAllSessions(principal, false));

                    for (SessionInformation sessionInformation : activeSessionInformations) {
                        SecuritySession securitySession = new SecuritySession();
                        securitySession.setUserName(user.getUsername());
                        securitySession.setSessionId(sessionInformation.getSessionId());
                        securitySession.setLastRequest(new LocalDateTime(sessionInformation.getLastRequest()));

                        activeSessions.add(securitySession);
                    }
                } catch (Exception e) {
                    logger.error("Error at creating list of active sessions",e);
                }
            }
        }
        return activeSessions;
    }
}
