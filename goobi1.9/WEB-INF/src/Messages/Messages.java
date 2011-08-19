package Messages;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.util.ResourceBundle;

import java.security.PrivilegedAction;
import javax.faces.context.FacesContext;

import de.sub.goobi.config.ConfigMain;

public class Messages {
	private static final String BUNDLE_NAME = "Messages.intmessages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	 static  ResourceBundle localBundle;

	private Messages() {
	}

	public static String getString(String key) {
		File file = new File(ConfigMain.getParameter("localMessages"));
		if (file.exists()) {
			// Load local message bundle from file system only if file exists; if value not exists in bundle, use default bundle from classpath

			try {
				final URL resourceURL = file.toURI().toURL();
				URLClassLoader urlLoader = AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
					public URLClassLoader run() {
						return new URLClassLoader(new URL[] { resourceURL });
					}
				});
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
