/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
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

import javax.imageio.ImageIO;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Listener to set up static ImageIO library with web application class loader.
 * <p/>
 * This listener works as a workaround for problems occurring when bootstrapping web applications
 * using ImageIO library within Tomcat when using JreMemoryLeakPreventionListener.
 *
 * @see https://bugs.launchpad.net/goobi-production/+bug/788160
 */
public class ImageIOInitializer implements ServletContextListener,
		HttpSessionListener, HttpSessionAttributeListener {

	static {
		// makes sure, plugins get loaded via web application class loader
		// and are available for later calls from the application
		ImageIO.scanForPlugins();
	}

	public void contextInitialized(ServletContextEvent sce) {
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
