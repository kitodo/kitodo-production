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


package org.kitodo.production.session;

import java.util.Objects;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.springframework.security.core.context.SecurityContextImpl;


@WebListener
public class CustomHttpSessionListener implements HttpSessionListener {

    private static final Logger logger = LogManager.getLogger(CustomHttpSessionListener.class);

    /**
     * Event handler that is triggere when an HTTP session is created.
     *
     * @param sessionEvent the notification event
     */
    @Override
    public void sessionCreated(HttpSessionEvent sessionEvent) {
        logger.debug("Session created: {}", sessionEvent.getSession().getId());
    }

    /**
     * Event handler that is triggered when an HTTP session expires.
     *
     * @param sessionEvent the notification event
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent sessionEvent) {
        Object securityContextObject = sessionEvent.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        if (Objects.nonNull(securityContextObject) && securityContextObject instanceof SecurityContextImpl) {
            SecurityContextImpl securityContext = (SecurityContextImpl) securityContextObject;
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (principal instanceof SecurityUserDetails) {
                logger.debug("Session expired: {}", sessionEvent.getSession().getId());
                ServiceManager.getSessionService().expireSessionsOfUser((SecurityUserDetails) principal);
            } else {
                logger.debug("Cannot expire session: {} is not an instance of SecurityUserDetails",
                        Helper.getObjectDescription(principal));
            }
        } else {
            logger.debug("Cannot expire session: {} is not an instance of SecurityContextImpl",
                    Helper.getObjectDescription(securityContextObject));
        }
    }
}
