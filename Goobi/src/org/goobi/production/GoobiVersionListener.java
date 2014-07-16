/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
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
public class GoobiVersionListener implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {

	public GoobiVersionListener() {
	}

	public void contextInitialized(ServletContextEvent sce) {

		// Retrieve Manifest file as Stream
		ServletContext context = sce.getServletContext();
		InputStream rs = context.getResourceAsStream("/META-INF/MANIFEST.MF");
		// Use Manifest to setup version information
		if (rs != null) {
			try {
				Manifest m = new Manifest(rs);
				GoobiVersion.setupFromManifest(m);
			} catch (IOException e) {
				context.log(e.getMessage());
			}
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
