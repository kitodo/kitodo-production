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

package de.sub.goobi.config;


import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.goobi.production.constants.FileNames;
import org.joda.time.Duration;

public class ConfigMain {
	private static final Logger myLogger = Logger.getLogger(ConfigMain.class);
	private static volatile PropertiesConfiguration config;
	private static String imagesPath = null;

	private static PropertiesConfiguration getConfig() {
		if (config == null) {
			synchronized (ConfigMain.class) {
				PropertiesConfiguration initialized = config;
				if (initialized == null) {
					PropertiesConfiguration.setDefaultListDelimiter('&');
					try {
						initialized = new PropertiesConfiguration(FileNames.CONFIG_FILE);
					} catch (ConfigurationException e) {
						if (myLogger.isEnabledFor(Level.WARN)) {
							myLogger.warn("Loading of " + FileNames.CONFIG_FILE
									+ " failed. Trying to start with empty configuration.", e);
						}
						initialized = new PropertiesConfiguration();
					}
					initialized.setListDelimiter('&');
					initialized.setReloadingStrategy(new FileChangedReloadingStrategy());
					config = initialized;
				}
			}
		}
		return config;
	}

	/**
	 * @return den Pfad für die temporären Images zur Darstellung zurückgeben
	 */
	public static String getTempImagesPath() {
		return "/pages/imagesTemp/";
	}

	/**
	 * @return den absoluten Pfad für die temporären Images zurückgeben
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
