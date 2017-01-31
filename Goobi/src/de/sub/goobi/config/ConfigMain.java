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

import java.io.File;
import java.util.*;
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

import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;

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

	/**
	 * Request Map<String,String>-parameter from Configuration
	 * 
	 * @param inParameter
	 *            the key prefix in the configuration, excluding the tailing dot
	 * @return Parameter as {@code Map<String,String>}
	 */
	public static Map<String, String> getParameterMap(String inParameter) {
		return getParameterMap(inParameter, false, true);
	}

	/**
	 * Request Map<String,String>-parameter from Configuration
	 * 
	 * @param inParameter
	 *            the key prefix in the configuration, excluding the tailing dot
	 * @param fullKey
	 *            If true, returns the full key, consisting of
	 *            {@code inParameter}, a dot and the variable part of the key.
	 *            If false, returns the variable part of the key only.
	 * @return Parameter as {@code Map<String,String>}
	 */
	public static Map<String, String> getParameterMap(String inParameter, boolean fullKey) {
		return getParameterMap(inParameter, fullKey, true);
	}

	/**
	 * Request Map<String,String>-parameter from Configuration
	 * 
	 * @param inParameter
	 *            the key prefix in the configuration, excluding the tailing dot
	 * @param fullKey
	 *            If true, returns the full key, consisting of
	 *            {@code inParameter}, a dot and the variable part of the key.
	 *            If false, returns the variable part of the key only.
	 * @param ahead
	 *            if true, return ahead mapping (key-to-value), else reverse
	 *            (value-to-key) mapping
	 * @return Parameter as {@code Map<String,String>}
	 */
	public static Map<String, String> getParameterMap(String inParameter, boolean fullKey, boolean ahead) {
		Map<String, String> result = new HashMap<String, String>();
		Iterator<?> keyIterator = getConfig().getKeys(inParameter);
		int begin = inParameter.length() + 1;
		while (keyIterator.hasNext()) {
			Object nextKey = keyIterator.next();
			if (nextKey instanceof String) {
				String key = (String) nextKey;
				String resultKey = fullKey ? key : key.substring(begin);
				String value = config.getString(key);
				if (ahead) {
					result.put(resultKey, value);
				} else {
					result.put(value, resultKey);
				}
			}
		}
		return result;
	}
}
