/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *    - http://gdz.sub.uni-goettingen.de
 *    - http://www.goobi.org
 *    - http://launchpad.net/goobi-production
 *
 * Copyright 2011, Center for Retrospective Digitization, Göttingen (GDZ),
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

/**
 * Listener to set up Goobi versioning information from Manifest on application startup.
 */
public class GoobiVersionListener implements ServletContextListener,
        HttpSessionListener, HttpSessionAttributeListener {

    public GoobiVersionListener() {
    }

    public void contextInitialized(ServletContextEvent sce) {

        // Retrieve Manifest file as Stream
        ServletContext context = sce.getServletContext();
        InputStream rs = context.getResourceAsStream("/META-INF/MANIFEST.MF");

        // Use Manifest to setup version information
        try {
            Manifest m = new Manifest(rs);
            GoobiVersion.setupFromManifest(m);
        } catch (IOException e) {
            context.log(e.getMessage());
        }

    }

    public void contextDestroyed(ServletContextEvent sce) {
    }

    public void sessionCreated(HttpSessionEvent se) {
    }

    public void sessionDestroyed(HttpSessionEvent se) {
    }

    public void attributeAdded(HttpSessionBindingEvent sbe) {
    }

    public void attributeRemoved(HttpSessionBindingEvent sbe) {
    }

    public void attributeReplaced(HttpSessionBindingEvent sbe) {
    }

}
