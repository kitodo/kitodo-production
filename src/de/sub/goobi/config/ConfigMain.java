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

package de.sub.goobi.config;

import java.io.File;
import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Messages;

public class ConfigMain implements Serializable{
	private static final long serialVersionUID = -7167854300981799440L;

	private static final Logger myLogger = Logger.getLogger(ConfigMain.class);

	static ConfigMain configMain = new ConfigMain();
	private static PropertiesConfiguration config;
	private static String configPfad;
	private static String imagesPath = null;
	
	

	/**
	 * @throws ConfigurationException
	 */
	private ConfigMain() {
		PropertiesConfiguration.setDefaultListDelimiter('&');
		if (configPfad == null)
			configPfad = "GoobiConfig.properties";
		try {
			config = new PropertiesConfiguration(configPfad);
		} catch (ConfigurationException e) {
			//TODO: Remove this, it was only used for compatibility between 1.49 and 1.5
			configPfad = "Konfiguration.properties";
			try {
				config = new PropertiesConfiguration(configPfad);
			} catch (ConfigurationException e1) {
				myLogger.error(e1);
				config = new PropertiesConfiguration();
			}
		}
		//config.setDelimiterParsingDisabled(true);
		config.setListDelimiter('|');

		config.setReloadingStrategy(new FileChangedReloadingStrategy());
	}


	
	/**
	 * den Pfad für die temporären Images zur Darstellung zurückgeben
	 * ================================================================
	 */
	public static String getTempImagesPath() {
		//TODO: Create a const
		return "/pages/imagesTemp/";
	}

	/**
	 * den absoluten Pfad für die temporären Images zurückgeben
	 * ================================================================
	 */
	public static String getTempImagesPathAsCompleteDirectory() {
		FacesContext context = FacesContext.getCurrentInstance();
		String filename;
		if (imagesPath != null) {
			filename = imagesPath;
		}
		else 
		{	
			HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
			filename = session.getServletContext().getRealPath("/pages/imagesTemp") + File.separator;
	
			/* den Ordner neu anlegen, wenn er nicht existiert */
			Helper help = new Helper();
			try {
				if (!new File(filename).exists()) {
					help.createMetaDirectory(filename);
				}
			} catch (Exception ioe) {
				myLogger.error("IO error: " + ioe);
				Helper.setFehlerMeldung(Messages.getString("couldNotCreateImageFolder"), ioe.getMessage());
			}
		}
		return filename;
	}

	
	public static void setImagesPath (String path) {
		imagesPath = path;
	}
	
	

	/**
	 * Ermitteln eines bestimmten Paramters der Konfiguration
	 * 
	 * @return Paramter als String
	 */
	public static String getParameter(String inParameter) {
		try {
			return config.getString(inParameter);
		} catch (RuntimeException e) {
			myLogger.error(e);
			return "- keine Konfiguration gefunden -";
		}
	}

	
	//TODO: Remove this methods, they are provided by Commons Configuration
	/**
	 * Ermitteln eines bestimmten Paramters der Konfiguration mit Angabe eines
	 * Default-Wertes
	 * 
	 * @return Paramter als String
	 */
	public static String getParameter(String inParameter, String inDefaultIfNull) {
		try {
			return config.getString(inParameter, inDefaultIfNull);
			// return config.getProperty(inParameter).toString();
		} catch (RuntimeException e) {
			return inDefaultIfNull;
		}
	}

	

	/**
	 * Ermitteln eines boolean-Paramters der Konfiguration, default if missing: false
	 * 
	 * @return Paramter als String
	 */
	public static boolean getBooleanParameter(String inParameter) {
		return getBooleanParameter(inParameter, false);
	}

	/**
	 * Ermitteln eines boolean-Paramters der Konfiguration
	 * 
	 * @return Paramter als String
	 */
	public static boolean getBooleanParameter(String inParameter, boolean inDefault) {
		return config.getBoolean(inParameter, inDefault);
	}

	

	/**
	 * Ermitteln eines long-Paramters der Konfiguration
	 * 
	 * @return Paramter als Long
	 */
	public static long getLongParameter(String inParameter, int inDefault) {
		try {
			return config.getLong(inParameter, inDefault);
		} catch (RuntimeException e) {
			myLogger.error(e);
			return 0;
		}
	}

	

	/**
	 * Request int-parameter from Configuration
	 * 
	 * @return Paramter as Int
	 */
	public static int getIntParameter(String inParameter) {
		return getIntParameter(inParameter, 0);
	}

	/**
	 * Request int-parameter from Configuration with default-value
	 * 
	 * @return Paramter as Int
	 */
	public static int getIntParameter(String inParameter, int inDefault) {
		try {
			return config.getInt(inParameter, inDefault);
		} catch (Exception e) {
			return 0;
		}
	}
}
