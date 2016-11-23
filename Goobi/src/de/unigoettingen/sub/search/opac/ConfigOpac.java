package de.unigoettingen.sub.search.opac;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
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
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.goobi.production.constants.FileNames;
import org.goobi.production.constants.Parameters;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;

@XmlRootElement(name = "catalogueConfiguration")
public class ConfigOpac {
	private static final Logger myLogger = Logger.getLogger(ConfigOpac.class);

	private static XMLConfiguration config;

	private static XMLConfiguration getConfig() throws FileNotFoundException {
		if (config != null) {
			return config;
		}
		String configPfad = FilenameUtils.concat(ConfigMain.getParameter(Parameters.CONFIG_DIR),
				FileNames.OPAC_CONFIGURATION_FILE);

		if (!new File(configPfad).exists()) {
			throw new FileNotFoundException("File not found: " + configPfad);
		}
		try {
			config = new XMLConfiguration(configPfad);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			config = new XMLConfiguration();
		}
		config.setListDelimiter('&');
		config.setReloadingStrategy(new FileChangedReloadingStrategy());
		return config;
	}

	/**
	 * Sets the XMLConfiguration of the ConfigOpac
	 *
	 * @param conf the XMLConfiguration to set
	 */
	public static void setConfiguration (XMLConfiguration conf) {
		config = conf;
	}

	/**
	 * Returns the XMLConfiguration of the ConfigOpac
	 *
	 * @return config the XMLConfiguration of the ConfigOpac
	 */
	public static XMLConfiguration getConfiguration () {
		return config;
	}

	/**
	 * Returns all configured catalogue titles from the config file.
	 *
	 * @return all catalogue titles
	 */
	public static ArrayList<String> getAllCatalogueTitles() {
		ArrayList<String> myList = new ArrayList<String>();
		try {
		int countCatalogues = getConfig().getMaxIndex("catalogue");
		for (int i = 0; i <= countCatalogues; i++) {
			String title = getConfig().getString("catalogue(" + i + ")[@title]");
			myList.add(title);
		}
		} catch (Throwable t) {
			myLogger.error("Error while reading von opac-config", t);
			Helper.setFehlerMeldung("Error while reading von opac-config", t.getMessage());
		}
		return myList;
	}

	/**
	 * return all configured Doctype-Titles from Configfile
	 * ================================================================
	 */
	private static ArrayList<String> getAllDoctypeTitles() {
		ArrayList<String> myList = new ArrayList<String>();
		try {
		int countTypes = getConfig().getMaxIndex("doctypes.type");
		for (int i = 0; i <= countTypes; i++) {
			String title = getConfig().getString("doctypes.type(" + i + ")[@title]");
			myList.add(title);
		}
		} catch (Throwable t) {
			myLogger.error("Error while reading von opac-config", t);
			Helper.setFehlerMeldung("Error while reading von opac-config", t.getMessage());
		}
		return myList;
	}

	/**
	 * Returns all configured media types from the config file.
	 *
	 * @return all media types
	 */
	public static ArrayList<ConfigOpacDoctype> getAllDoctypes() {
		ArrayList<ConfigOpacDoctype> myList = new ArrayList<ConfigOpacDoctype>();
		try {
		for (String title : getAllDoctypeTitles()) {
			myList.add(getDoctypeByName(title));
		}
		} catch (Throwable t) {
			myLogger.error("Error while reading von opac-config", t);
			Helper.setFehlerMeldung("Error while reading von opac-config", t.getMessage());
		}
		return myList;
	}

	/**
	 * get doctype from title
	 * ================================================================
	 */
	@SuppressWarnings("unchecked")
	public static ConfigOpacDoctype getDoctypeByName(String inTitle) throws FileNotFoundException {
		int countCatalogues = getConfig().getMaxIndex("doctypes.type");
		for (int i = 0; i <= countCatalogues; i++) {
			String title = getConfig().getString("doctypes.type(" + i + ")[@title]");
			if (title.equals(inTitle)) {
				/* Sprachen erfassen */
				HashMap<String, String> labels = new HashMap<String, String>();
				int countLabels = getConfig().getMaxIndex("doctypes.type(" + i + ").label");
				for (int j = 0; j <= countLabels; j++) {
					String language = getConfig().getString("doctypes.type(" + i + ").label(" + j + ")[@language]");
					String value = getConfig().getString("doctypes.type(" + i + ").label(" + j + ")");
					labels.put(language, value);
				}
				String inRulesetType = getConfig().getString("doctypes.type(" + i + ")[@rulesetType]");

				boolean newspaper;
				String inTifHeaderType;
				boolean periodical;
				boolean multiVolume;
				boolean containedWork;

				try {
					inTifHeaderType = getConfig().getString("doctypes.type(" + i + ")[@tifHeaderType]");
				} catch (NoSuchElementException noParameterIsNewspaper) {
					inTifHeaderType = "";
				}

				try {
					periodical = getConfig().getBoolean("doctypes.type(" + i + ")[@isPeriodical]");
				} catch (NoSuchElementException noParameterIsNewspaper) {
					periodical = false;
				}

				try {
					multiVolume = getConfig().getBoolean("doctypes.type(" + i + ")[@isMultiVolume]");
				} catch (NoSuchElementException noParameterIsNewspaper) {
					multiVolume = false;
				}

				try {
					containedWork = getConfig().getBoolean("doctypes.type(" + i + ")[@isContainedWork]");
				} catch (NoSuchElementException noParameterIsNewspaper) {
					containedWork = false;
				}

				try {
					newspaper = getConfig().getBoolean("doctypes.type(" + i + ")[@isNewspaper]");
				} catch (NoSuchElementException noParameterIsNewspaper) {
					newspaper = false;
				}
				ArrayList<String> mappings = (ArrayList<String>) getConfig()
						.getList("doctypes.type(" + i + ").mapping");

				ConfigOpacDoctype cod = new ConfigOpacDoctype(inTitle, inRulesetType, inTifHeaderType, periodical,
						multiVolume, containedWork, newspaper, labels, mappings);
				return cod;
			}
		}
		return null;
	}

	/**
	 * The function getRestrictionsForCatalogue() returns the content of all
	 * <kbd>&lt;restriction&gt;</kbd> elements from the
	 * <kbd>&lt;catalogue&gt;</kbd> entry with the given <kbd>title</kbd> from
	 * <kbd>goobi_opac.xml</kbd>.
	 *
	 * The function will return an empty list if there are no such entries for
	 * the given catalogue.
	 *
	 * @param title
	 *            Title parameter of the <kbd>&lt;catalogue&gt;</kbd> entry to
	 *            examine
	 * @return List
	 * @throws FileNotFoundException
	 */
	public static List<String> getRestrictionsForCatalogue(String title) throws FileNotFoundException {
		List<String> result = new LinkedList<String>();
		@SuppressWarnings("unchecked")
		List<HierarchicalConfiguration> catalogues = getConfig().configurationsAt("catalogue");
		for (HierarchicalConfiguration catalogue : catalogues) {
			if (title.equals(catalogue.getString("[@title]"))) {
				for (String restriction : catalogue.getStringArray("restriction")) {
					result.add(restriction);
				}
				break;
			}
		}
		return result;
	}

	/**
	 * Returns all configured catalogue titles from the config file. The Jersey
	 * API cannot invoke static methods, so we need this wrapper method.
	 *
	 * @return all catalogue titles
	 */
	@XmlElement(name = "interface")
	public ArrayList<String> getInterface() {
		return getAllCatalogueTitles();
	}

	/**
	 * Returns all configured media types from the config file. The Jersey API
	 * cannot invoke static methods, so we need this wrapper method.
	 *
	 * @return all media types
	 */
	@XmlElement(name = "mediaType")
	public ArrayList<ConfigOpacDoctype> getMediaType() {
		return getAllDoctypes();
	}
}
