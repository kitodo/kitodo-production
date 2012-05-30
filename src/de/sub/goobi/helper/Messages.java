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

package de.sub.goobi.helper;

import de.sub.goobi.config.ConfigMain;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import org.apache.log4j.Logger;

public class Messages {
	private static final Logger logger = Logger.getLogger(Messages.class);

	private static ResourceBundle bundle;
	private static ResourceBundle localBundle;

	public static void loadLanguageBundle() {
		bundle = ResourceBundle.getBundle("messages", FacesContext
				.getCurrentInstance().getViewRoot().getLocale());
		localBundle = loadLocalMessageBundleIfAvailable();
	}

	/**
	 * A parameter “localMessages” can be configured in GoobiConfig.properties.
	 * If this points to a valid path where the messages_*.properties files can
	 * be found, these are used instead. Load local message bundle from file
	 * system only if file exists.
	 * 
	 * @return Resource bundle for local messages. Returns NULL if no local
	 *         message bundle could be found.
	 */
	private static ResourceBundle loadLocalMessageBundleIfAvailable() {
		String localMessages = ConfigMain.getParameter("localMessages");
		if (localMessages != null) {
			File path = new File(localMessages);
			if (path.exists()) {
				try {
					URL pathURL = path.toURI().toURL();
					URLClassLoader urlLoader = new URLClassLoader(
							new URL[] { pathURL });
					return ResourceBundle.getBundle("messages", FacesContext
							.getCurrentInstance().getViewRoot().getLocale(),
							urlLoader);
				} catch (java.net.MalformedURLException e) {
					logger.error("Error reading local message bundle", e);
				}
			}
		}
		return null;
	}

	/**
	 * The function getString() returns the translated key in the given language.
	 * 
	 * @param key
	 *            A key to to be looked up in the messages.
	 * @return The verbalisation for the given key in the chosen application
	 *         frontend’s language
	 */
	public static String getString(String key) {
		// running instance of ResourceBundle doesn't respond on user language
		// changes, workaround by instantiating it every time

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
			String msg = bundle.getString(key);
			return msg;
		} catch (RuntimeException e) {
			return key;
		}
	}
}
