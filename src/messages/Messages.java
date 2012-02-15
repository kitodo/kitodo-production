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

package messages;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import de.sub.goobi.config.ConfigMain;

import org.apache.log4j.Logger;

public class Messages {
	private static final String BUNDLE_NAME = "messages.intmessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	static  ResourceBundle localBundle;

	private static final Logger myLogger = Logger.getLogger(Messages.class);

	private Messages() {
	}

	public static String getString(String key) {
		// load local message bundle if not yet loaded
		if (localBundle == null) {
			localBundle = loadLocalMessageBundleIfAvailable();
		}
		
		// do local translation
		if (localBundle != null) {
			if (localBundle.containsKey(key)) {
				String trans = localBundle.getString(key);
				return trans;
			}
			if (localBundle.containsKey(key.toLowerCase())) {
				return localBundle.getString(key.toLowerCase());
			}
		}

		// fallback to default resource bundle
		if (RESOURCE_BUNDLE.containsKey(key)) {
			String msg = RESOURCE_BUNDLE.getString(key);
			return msg;
		} else {
			return key;
		}
	}

	/**
	  * Load local message bundle from file system only if file exists.
	  *
	  * @return Resource bundle for local messages. Returns NULL if no local message bundle could be found.
	  */
	private static ResourceBundle loadLocalMessageBundleIfAvailable() {
		String localMessages = ConfigMain.getParameter("localMessages");
		if (localMessages != null) {
			File file = new File(localMessages);
			if (file.exists()) {
				myLogger.info("Local message bundle found: " + localMessages);
				try {
					URL resourceURL = file.toURI().toURL();
					URLClassLoader urlLoader = new URLClassLoader(new URL[] { resourceURL });
					return ResourceBundle.getBundle("messages", FacesContext.getCurrentInstance().getViewRoot().getLocale(), urlLoader);
				} catch (java.net.MalformedURLException muex) {
					myLogger.error("Error reading local message bundle", muex);
				}
			}
		}
		return null;
	}

}
