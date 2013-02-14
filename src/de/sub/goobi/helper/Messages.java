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
import org.apache.commons.collections.iterators.ArrayIterator;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import static java.util.Locale.getAvailableLocales;
import static java.util.ResourceBundle.getBundle;

public class Messages {
	private static final Logger logger = Logger.getLogger(Messages.class);

	protected static Map<Locale, ResourceBundle> commonMessages = new HashMap<Locale, ResourceBundle>();
	protected static Map<Locale, ResourceBundle> localMessages = new HashMap<Locale, ResourceBundle>();

	static{
		Iterator polyglot = getSupportedLocalesIterator();
		while (polyglot.hasNext()) {
			Locale language = (Locale) polyglot.next();

			ResourceBundle commonMessageBundle = localCommonMessageBundleIfAvailable(language);
			if (commonMessageBundle != null) {
				commonMessages.put(language, commonMessageBundle);
			}

			ResourceBundle localMessageBundle = loadLocalMessageBundleIfAvailable(language);
			if (localMessageBundle != null) {
				localMessages.put(language, localMessageBundle);
			}
		}
	}

	private static ResourceBundle localCommonMessageBundleIfAvailable(Locale language) {
		try {
			return getBundle("messages", language);
		} catch (NullPointerException npe) {
			logger.error("Attempt to load message bundle, but no locale information given.");
		} catch (MissingResourceException mre) {
			logger.error("Cannot load common message bundle for language " + language.toString());
		}
		return null;
	}

	private static Iterator getSupportedLocalesIterator() {
		if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getApplication() != null) {
			return FacesContext.getCurrentInstance().getApplication().getSupportedLocales();
		}
		return new ArrayIterator(getAvailableLocales());
	}

	/**
	 * A parameter “localMessages” can be configured in GoobiConfig.properties.
	 * If this points to a valid path where the messages_*.properties files can
	 * be found, these are used instead. Load local message commonMessages from
	 * file system only if file exists.
	 * 
	 * @param variant
	 *            The language
	 * 
	 * @return Resource commonMessages for local messages. Returns NULL if no
	 *         local message commonMessages could be found.
	 */
	private static ResourceBundle loadLocalMessageBundleIfAvailable(Locale variant) {
		String localMessages = ConfigMain.getParameter("localMessages");
		if (localMessages != null) {
			File path = new File(localMessages);
			if (path.exists()) {
				try {
					URL pathURL = path.toURI().toURL();
					URLClassLoader urlLoader = new URLClassLoader(new URL[] { pathURL });
					return getBundle("messages", variant, urlLoader);
				} catch (java.net.MalformedURLException e) {
					logger.error("Error reading local message commonMessages", e);
				}
			}
		}
		return null;
	}

	private static Locale getCurrentUsedLanguage() {
		Locale result;

		if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getViewRoot() != null) {
			result = FacesContext.getCurrentInstance().getViewRoot().getLocale();
		} else {
			result = Locale.getDefault();
		}

		return result;
	}

	/**
	 * The function getString() returns the translated key in the language
	 * currently configured in the front end.
	 * 
	 * @param key
	 *            A key to to be looked up in the messages.
	 * 
	 * @return The verbalisation for the given key in the language chosen
	 */
	public static String getString(String key) {
		Locale desiredLanguage;

		desiredLanguage = getCurrentUsedLanguage();

		if (desiredLanguage != null)
			return getString(desiredLanguage, key);
		else
			return getString(Locale.ENGLISH, key).concat(" [Could not retrieve desired language, switching to English.]");
	}

	/**
	 * The function getString() returns the translated key in the given
	 * language.
	 * 
	 * @param language
	 *            The locale wanted.
	 * @param key
	 *            A key to to be looked up in the messages.
	 * 
	 * @return The verbalisation for the given key in the requested language
	 */
	public static String getString(Locale language, String key) {
		if (localMessages.containsKey(language)) {
			ResourceBundle languageLocal = localMessages.get(language);
			if (languageLocal.containsKey(key))
				return languageLocal.getString(key);
			String lowKey = key.toLowerCase();
			if (languageLocal.containsKey(lowKey))
				return languageLocal.getString(lowKey);
		}
		try {
			return commonMessages.get(language).getString(key);
		} catch (RuntimeException irrelevant) {
			return key;
		}
	}
}
