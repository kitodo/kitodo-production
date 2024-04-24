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

package org.kitodo.production;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.services.ServiceManager;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * Kitodo.Production is the workflow management module of the Kitodo suite. It
 * supports the digitization process of various types of materials.
 */
@WebListener
public class KitodoProduction implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {
    private static final Logger logger = LogManager.getLogger(KitodoProduction.class);
    private static CompletableFuture<KitodoProduction> instance = new CompletableFuture<>();
    private ServletContext context;
    private Optional<Manifest> manifest = Optional.empty();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Retrieve Manifest file as Stream
        context = sce.getServletContext();
        try (InputStream rs = context.getResourceAsStream("/META-INF/MANIFEST.MF")){
            // Use Manifest to setup version information
            if (Objects.nonNull(rs)) {
                Manifest m = new Manifest(rs);
                manifest = Optional.of(m);
                KitodoVersion.setupFromManifest(m);
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            context.log(e.getMessage());
        }
    }

    /**
     * Returns the application’s main class.
     * 
     * @return the main class
     * @throws UndeclaredThrowableException
     *             if the servlet container is shut down before the web
     *             application is started
     */
    public static KitodoProduction getInstance() {
        try {
            return instance.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.fatal(e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Returns the servlet context.
     * 
     * @return the servlet context
     */
    public ServletContext getServletContext() {
        return context;
    }

    /**
     * Returns the manifest. If there is one. Otherwise, it can also be empty.
     * 
     * @return the manifest
     */
    public Optional<Manifest> getManifest() {
        return manifest;
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
