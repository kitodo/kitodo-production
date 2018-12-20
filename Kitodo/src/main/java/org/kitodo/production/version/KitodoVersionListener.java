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
import java.util.jar.Manifest;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

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
        if (rs != null) {
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
        // nothing is done here
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
