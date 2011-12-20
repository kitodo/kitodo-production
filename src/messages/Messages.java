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

public class Messages {
	private static final String BUNDLE_NAME = "messages.intmessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	 static  ResourceBundle localBundle;

	private Messages() {
	}

	public static String getString(String key) {
		File file = new File(ConfigMain.getParameter("localMessages"));
		if (file.exists()) {
			// Load local message bundle from file system only if file exists; if value not exists in bundle, use default bundle from classpath

			try {
				URL resourceURL = file.toURI().toURL();
				URLClassLoader urlLoader = new URLClassLoader(new URL[] { resourceURL });
				localBundle = ResourceBundle.getBundle("messages", FacesContext.getCurrentInstance().getViewRoot().getLocale(), urlLoader);
			} catch (Exception e) {
			}
			
		}

		// Load local message bundle from classpath
		try {
			if (localBundle != null) {
				if (localBundle.containsKey(key)) {
					String trans = localBundle.getString(key);
					return trans;
				}
				if (localBundle.containsKey(key.toLowerCase())) {
					return localBundle.getString(key.toLowerCase());
				}
			}
		} catch (RuntimeException e) {
		}
		try {
			String msg = RESOURCE_BUNDLE.getString(key);
			return msg;
		} catch (RuntimeException e) {
			return key;
		}
	}
}
