package de.unigoettingen.sub.search.opac;

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
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;
import org.goobi.production.constants.FileNames;
import org.goobi.production.constants.Parameters;

import de.sub.goobi.config.ConfigMain;

@XmlRootElement(name = "catalogueConfiguration")
public class ConfigOpac {
	private static XMLConfiguration config;
	static {
		String configPfad = FilenameUtils.concat(ConfigMain.getParameter(Parameters.CONFIG_DIR),
				FileNames.OPAC_CONFIGURATION_FILE);

		if (!new File(configPfad).exists()) {
			throw new RuntimeException("File not found: " + configPfad, new FileNotFoundException("File not found: "
					+ configPfad));
		}
		try {
			config = new XMLConfiguration(configPfad);
		} catch (ConfigurationException e) {
			e.printStackTrace();
			config = new XMLConfiguration();
		}
		config.setListDelimiter('&');
		config.setReloadingStrategy(new FileChangedReloadingStrategy());
	}

	/**
	 * find Catalogue in Opac-Configurationlist
	 * ================================================================
	 */
	public static String getOpacType(String inTitle) {
		int countCatalogues = config.getMaxIndex("catalogue");
		for (int i = 0; i <= countCatalogues; i++) {
			String title = config.getString("catalogue(" + i + ")[@title]");
			if (title.equals(inTitle)) {
				return config.getString("catalogue(" + i + ").config[@opacType]", "PICA");
			}
		}
		return null;
	}

	/**
	 * return all configured Catalogue-Titles from Configfile
	 * ================================================================
	 */
	@XmlElement(name = "interface")
	public static ArrayList<String> getAllCatalogueTitles() {
		ArrayList<String> myList = new ArrayList<String>();
		int countCatalogues = config.getMaxIndex("catalogue");
		for (int i = 0; i <= countCatalogues; i++) {
			String title = config.getString("catalogue(" + i + ")[@title]");
			myList.add(title);
		}
		return myList;
	}

	/**
	 * return all configured Doctype-Titles from Configfile
	 * ================================================================
	 */
	private static ArrayList<String> getAllDoctypeTitles() {
		ArrayList<String> myList = new ArrayList<String>();
		int countTypes = config.getMaxIndex("doctypes.type");
		for (int i = 0; i <= countTypes; i++) {
			String title = config.getString("doctypes.type(" + i + ")[@title]");
			myList.add(title);
		}
		return myList;
	}

	/**
	 * return all configured Doctype-Titles from Configfile
	 * ================================================================
	 */
	@XmlElement(name = "mediaType")
	public static ArrayList<ConfigOpacDoctype> getAllDoctypes() {
		ArrayList<ConfigOpacDoctype> myList = new ArrayList<ConfigOpacDoctype>();
		for (String title : getAllDoctypeTitles()) {
			myList.add(getDoctypeByName(title));
		}
		return myList;
	}

	/**
	 * get doctype from title
	 * ================================================================
	 */
	@SuppressWarnings("unchecked")
	public static ConfigOpacDoctype getDoctypeByName(String inTitle) {
		int countCatalogues = config.getMaxIndex("doctypes.type");
		for (int i = 0; i <= countCatalogues; i++) {
			String title = config.getString("doctypes.type(" + i + ")[@title]");
			if (title.equals(inTitle)) {
				/* Sprachen erfassen */
				HashMap<String, String> labels = new HashMap<String, String>();
				int countLabels = config.getMaxIndex("doctypes.type(" + i + ").label");
				for (int j = 0; j <= countLabels; j++) {
					String language = config.getString("doctypes.type(" + i + ").label(" + j + ")[@language]");
					String value = config.getString("doctypes.type(" + i + ").label(" + j + ")");
					labels.put(language, value);
				}
				String inRulesetType = config.getString("doctypes.type(" + i + ")[@rulesetType]");
				String inTifHeaderType = config.getString("doctypes.type(" + i + ")[@tifHeaderType]");
				boolean periodical = config.getBoolean("doctypes.type(" + i + ")[@isPeriodical]");
				boolean multiVolume = config.getBoolean("doctypes.type(" + i + ")[@isMultiVolume]");
				boolean containedWork = config.getBoolean("doctypes.type(" + i + ")[@isContainedWork]");
				boolean newspaper;
				try {
					newspaper = config.getBoolean("doctypes.type(" + i + ")[@isNewspaper]");
				} catch (NoSuchElementException noParameterIsNewspaper) {
					newspaper = false;
				}
				ArrayList<String> mappings = (ArrayList<String>) config.getList("doctypes.type(" + i + ").mapping");

				ConfigOpacDoctype cod = new ConfigOpacDoctype(inTitle, inRulesetType, inTifHeaderType, periodical,
						multiVolume, containedWork, newspaper, labels, mappings);
				return cod;
			}
		}
		return null;
	}



}
