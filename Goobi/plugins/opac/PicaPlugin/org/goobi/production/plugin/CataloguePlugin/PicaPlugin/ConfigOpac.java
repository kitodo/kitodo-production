/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;

class ConfigOpac {
	private static XMLConfiguration config;
	static {
		String configPfad = FilenameUtils.concat(PicaPlugin.getConfigDir(), PicaPlugin.OPAC_CONFIGURATION_FILE);

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
	static ConfigOpacCatalogue getCatalogueByName(String inTitle) {
		int countCatalogues = config.getMaxIndex("catalogue");
		for (int i = 0; i <= countCatalogues; i++) {
			String title = config.getString("catalogue(" + i + ")[@title]");
			if (title.equals(inTitle)) {
				String description = config.getString("catalogue(" + i + ").config[@description]");
				String address = config.getString("catalogue(" + i + ").config[@address]");
				String database = config.getString("catalogue(" + i + ").config[@database]");
				String iktlist = config.getString("catalogue(" + i + ").config[@iktlist]");
				String cbs = config.getString("catalogue(" + i + ").config[@ucnf]", "");
				if (!cbs.equals("")) {
					cbs = "&" + cbs;
				}
				int port = config.getInt("catalogue(" + i + ").config[@port]");
				String charset = "iso-8859-1";
				if (config.getString("catalogue(" + i + ").config[@charset]") != null) {
					charset = config.getString("catalogue(" + i + ").config[@charset]");
				}
				String opacType = config.getString("catalogue(" + i + ").config[@opacType]", "PICA");
				/*
				 * --------------------- Opac-Beautifier einlesen und in Liste
				 * zu jedem Catalogue packen -------------------
				 */
				ArrayList<ConfigOpacCatalogueBeautifier> beautyList = new ArrayList<ConfigOpacCatalogueBeautifier>();
				for (int j = 0; j <= config.getMaxIndex("catalogue(" + i + ").beautify.setvalue"); j++) {
					/* Element, dessen Wert geändert werden soll */
					String tempJ = "catalogue(" + i + ").beautify.setvalue(" + j + ")";
					ConfigOpacCatalogueBeautifierElement oteChange = new ConfigOpacCatalogueBeautifierElement(
							config.getString(tempJ + "[@tag]"), config.getString(tempJ + "[@subtag]"),
							config.getString(tempJ + "[@value]"));
					/*
					 * Elemente, die bestimmte Werte haben müssen, als Prüfung,
					 * ob das zu ändernde Element geändert werden soll
					 */
					ArrayList<ConfigOpacCatalogueBeautifierElement> proofElements = new ArrayList<ConfigOpacCatalogueBeautifierElement>();
					for (int k = 0; k <= config.getMaxIndex(tempJ + ".condition"); k++) {
						String tempK = tempJ + ".condition(" + k + ")";
						ConfigOpacCatalogueBeautifierElement oteProof = new ConfigOpacCatalogueBeautifierElement(
								config.getString(tempK + "[@tag]"), config.getString(tempK + "[@subtag]"),
								config.getString(tempK + "[@value]"));
						proofElements.add(oteProof);
					}
					beautyList.add(new ConfigOpacCatalogueBeautifier(oteChange, proofElements));
				}

				ConfigOpacCatalogue coc = new ConfigOpacCatalogue(title, description, address, database, iktlist, port,
						charset, cbs, beautyList, opacType);
				return coc;
			}
		}
		return null;
	}

	/**
	 * return all configured Catalogue-Titles from Configfile
	 * ================================================================
	 */
	@XmlElement(name = "interface")
	private static ArrayList<String> getAllCatalogueTitles() {
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
	static ArrayList<ConfigOpacDoctype> getAllDoctypes() {
		ArrayList<ConfigOpacDoctype> myList = new ArrayList<ConfigOpacDoctype>();
		for (String title : getAllDoctypeTitles()) {
			myList.add(getDoctypeByName(title));
		}
		return myList;
	}

	/**
	 * get doctype from mapping of opac response first check if there is a
	 * special mapping for this
	 * ================================================================
	 */
	static ConfigOpacDoctype getDoctypeByMapping(String inMapping, String inCatalogue) {
		int countCatalogues = config.getMaxIndex("catalogue");
		for (int i = 0; i <= countCatalogues; i++) {
			String title = config.getString("catalogue(" + i + ")[@title]");
			if (title.equals(inCatalogue)) {
				/*
				 * --------------------- alle speziell gemappten DocTypes eines
				 * Kataloges einlesen -------------------
				 */
				HashMap<String, String> labels = new HashMap<String, String>();
				int countLabels = config.getMaxIndex("catalogue(" + i + ").specialmapping");
				for (int j = 0; j <= countLabels; j++) {
					String type = config.getString("catalogue(" + i + ").specialmapping[@type]");
					String value = config.getString("catalogue(" + i + ").specialmapping");
					labels.put(value, type);
				}
				if (labels.containsKey(inMapping)) {
					return getDoctypeByName(labels.get(inMapping));
				}
			}
		}

		/*
		 * --------------------- falls der Katalog kein spezielles Mapping für
		 * den Doctype hat, jetzt in den Doctypes suchen -------------------
		 */
		for (String title : getAllDoctypeTitles()) {
			ConfigOpacDoctype tempType = getDoctypeByName(title);
			if (tempType.getMappings().contains(inMapping)) {
				return tempType;
			}
		}
		return null;
	}

	/**
	 * get doctype from title
	 * ================================================================
	 */
	@SuppressWarnings("unchecked")
	private static ConfigOpacDoctype getDoctypeByName(String inTitle) {
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
				boolean periodical = config.getBoolean("doctypes.type(" + i + ")[@isPeriodical]");
				boolean multiVolume = config.getBoolean("doctypes.type(" + i + ")[@isMultiVolume]");
				boolean containedWork = config.getBoolean("doctypes.type(" + i + ")[@isContainedWork]");
				ArrayList<String> mappings = (ArrayList<String>) config.getList("doctypes.type(" + i + ").mapping");

				ConfigOpacDoctype cod = new ConfigOpacDoctype(periodical, multiVolume, containedWork, mappings);
				return cod;
			}
		}
		return null;
	}
}
