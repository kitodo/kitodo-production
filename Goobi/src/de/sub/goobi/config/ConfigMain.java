package de.sub.goobi.config;

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

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;
import org.goobi.production.constants.FileNames;
import org.joda.time.Duration;


import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;

public class ConfigMain {
	private static final Logger myLogger = Logger.getLogger(ConfigMain.class);
	private static PropertiesConfiguration config;
	private static String imagesPath = null;

	private static PropertiesConfiguration getConfig() {
		if (config != null)
			return config;
		PropertiesConfiguration.setDefaultListDelimiter('&');
		try {
			config = new PropertiesConfiguration(FileNames.CONFIG_FILE);
		} catch (ConfigurationException e) {
			myLogger.warn("Loading of " + FileNames.CONFIG_FILE + " failed. Trying to start with empty configuration.", e);
			config = new PropertiesConfiguration();
		}
		config.setListDelimiter('|');
		config.setReloadingStrategy(new FileChangedReloadingStrategy());
		return config;
	}

	/**
	 * den Pfad für die temporären Images zur Darstellung zurückgeben ================================================================
	 */
	public static String getTempImagesPath() {
		return "/pages/imagesTemp/";
	}

	/**
	 * den absoluten Pfad für die temporären Images zurückgeben ================================================================
	 */
	public static String getTempImagesPathAsCompleteDirectory() {
		FacesContext context = FacesContext.getCurrentInstance();
		String filename;
		if (imagesPath != null) {
			filename = imagesPath;
		} else {
			HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
			filename = session.getServletContext().getRealPath("/pages/imagesTemp") + File.separator;

			/* den Ordner neu anlegen, wenn er nicht existiert */
			try {
				FilesystemHelper.createDirectory(filename);
			} catch (Exception ioe) {
				myLogger.error("IO error: " + ioe);
				Helper.setFehlerMeldung(Helper.getTranslation("couldNotCreateImageFolder"), ioe.getMessage());
			}
		}
		return filename;
	}

	public static void setImagesPath(String path) {
		imagesPath = path;
	}

	/**
	 * Request selected parameter from configuration
	 * 
	 * @return Parameter as String
	 */
	public static String getParameter(String inParameter) {
		try {
			return getConfig().getString(inParameter);
		} catch (RuntimeException e) {
			myLogger.error(e);
			return "- keine Konfiguration gefunden -";
		}
	}

	/**
	 * Request selected parameter with given default value from configuration
	 * 
	 * @return Parameter as String
	 */
	public static String getParameter(String inParameter, String inDefaultIfNull) {
		try {
			return getConfig().getString(inParameter, inDefaultIfNull);
			// return config.getProperty(inParameter).toString();
		} catch (RuntimeException e) {
			return inDefaultIfNull;
		}
	}

	/**
	 * Request boolean parameter from configuration, default if missing: false
	 * 
	 * @return Parameter as String
	 */
	public static boolean getBooleanParameter(String inParameter) {
		return getBooleanParameter(inParameter, false);
	}

	/**
	 * Request boolean parameter from configuration
	 * 
	 * @return Parameter as String
	 */
	public static boolean getBooleanParameter(String inParameter, boolean inDefault) {
		return getConfig().getBoolean(inParameter, inDefault);
	}

	/**
	 * Request long parameter from configuration
	 * 
	 * @return Parameter as Long
	 */
	public static long getLongParameter(String inParameter, long inDefault) {
		return getConfig().getLong(inParameter, inDefault);
	}

	/**
	 * Request Duration parameter from configuration
	 * 
	 * @return Parameter as Duration
	 */
	public static Duration getDurationParameter(String inParameter, TimeUnit timeUnit, long inDefault) {
		long duration = getLongParameter(inParameter, inDefault);
		return new Duration(TimeUnit.MILLISECONDS.convert(duration, timeUnit));
	}

	/**
	 * Request int-parameter from Configuration
	 * 
	 * @return Parameter as Int
	 */
	public static int getIntParameter(String inParameter) {
		return getIntParameter(inParameter, 0);
	}
	
	/**
	 * Request int-parameter from Configuration with default-value
	 * 
	 * @return Parameter as Int
	 */
	public static int getIntParameter(String inParameter, int inDefault) {
		try {
			return getConfig().getInt(inParameter, inDefault);
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * Request String[]-parameter from Configuration
	 * 
	 * @return Parameter as String[] 
	 */
	public static String[] getStringArrayParameter(String inParameter) {
		
		return getConfig().getStringArray(inParameter);
	}
}
