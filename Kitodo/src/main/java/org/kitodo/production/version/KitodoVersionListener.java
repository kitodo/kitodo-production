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

package org.kitodo.production.version;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * Listener to set up Kitodo versioning information from Manifest on application
 * startup.
 */
@WebListener
public class KitodoVersionListener implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Retrieve Manifest file as Stream
        ServletContext context = sce.getServletContext();
        InputStream rs = context.getResourceAsStream("/META-INF/MANIFEST.MF");
        // Use Manifest to setup version information
        if (Objects.nonNull(rs)) {
            try {
                Manifest m = new Manifest(rs);
                KitodoVersion.setupFromManifest(m);
            } catch (IOException e) {
                context.log(e.getMessage());
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing is done here
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // nothing is done here
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        Object securityContextObject = se.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
        if (securityContextObject instanceof SecurityContextImpl) {
            SecurityContextImpl securityContext = (SecurityContextImpl) securityContextObject;
            Object principal = securityContext.getAuthentication().getPrincipal();
            if (principal instanceof SecurityUserDetails) {
                ServiceManager.getSessionService().expireSessionsOfUser((SecurityUserDetails) principal);
            }
        }
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent sbe) {
        // nothing is done here
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent sbe) {
        // nothing is done here
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent sbe) {
        // nothing is done here
    }

}
