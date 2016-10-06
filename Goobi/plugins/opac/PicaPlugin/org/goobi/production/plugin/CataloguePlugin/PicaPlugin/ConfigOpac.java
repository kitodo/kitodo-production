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
package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;

class ConfigOpac {
	private static XMLConfiguration config;

	private static XMLConfiguration getConfig() {
		if (config != null) {
			return config;
		}
		String configPfad = FilenameUtils.concat(PicaPlugin.getConfigDir(), PicaPlugin.OPAC_CONFIGURATION_FILE);
		if (!new File(configPfad).exists()) {
			String message = "File not found: ".concat(configPfad);
			throw new RuntimeException(message, new FileNotFoundException(message));
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

	static List<String> getAllCatalogues(){
		List<String> catalogueTitles = new ArrayList<String>();
		XMLConfiguration conf = getConfig();
		for(int i = 0; i <= conf.getMaxIndex("catalogue"); i++){
			catalogueTitles.add(conf.getString("catalogue(" + i + ")[@title]"));
		}
		return catalogueTitles;
	}
	
	
	/**
	 * find Catalogue in Opac-Configurationlist
	 * ================================================================
	 */
	static ConfigOpacCatalogue getCatalogueByName(String inTitle) {
		int countCatalogues = getConfig().getMaxIndex("catalogue");
		for (int i = 0; i <= countCatalogues; i++) {
			String title = getConfig().getString("catalogue(" + i + ")[@title]");
			if (title.equals(inTitle)) {
				String description = getConfig().getString("catalogue(" + i + ").config[@description]");
				String address = getConfig().getString("catalogue(" + i + ").config[@address]");
				String database = getConfig().getString("catalogue(" + i + ").config[@database]");
				String iktlist = getConfig().getString("catalogue(" + i + ").config[@iktlist]");
				String cbs = getConfig().getString("catalogue(" + i + ").config[@ucnf]", "");
				if (!cbs.equals("")) {
					cbs = "&" + cbs;
				}
				int port = getConfig().getInt("catalogue(" + i + ").config[@port]");
				String charset = "iso-8859-1";
				if (getConfig().getString("catalogue(" + i + ").config[@charset]") != null) {
					charset = getConfig().getString("catalogue(" + i + ").config[@charset]");
				}
				String opacType = getConfig().getString("catalogue(" + i + ").config[@opacType]", "PICA");

				// Opac-Beautifier einlesen und in Liste zu jedem Catalogue packen

				ArrayList<ConfigOpacCatalogueBeautifier> beautyList = new ArrayList<ConfigOpacCatalogueBeautifier>();
				for (int j = 0; j <= getConfig().getMaxIndex("catalogue(" + i + ").beautify.setvalue"); j++) {
					/* Element, dessen Wert geändert werden soll */
					String tempJ = "catalogue(" + i + ").beautify.setvalue(" + j + ")";
					ConfigOpacCatalogueBeautifierElement oteChange = new ConfigOpacCatalogueBeautifierElement(
							getConfig().getString(tempJ + "[@tag]"), getConfig().getString(tempJ + "[@subtag]"),
							getConfig().getString(tempJ + "[@value]").replaceAll("\u2423", " "), getConfig().getString(
									tempJ + "[@mode]", "replace"));

					// Elemente, die bestimmte Werte haben müssen, als Prüfung, ob das zu ändernde Element geändert werden soll

					ArrayList<ConfigOpacCatalogueBeautifierElement> proofElements = new ArrayList<ConfigOpacCatalogueBeautifierElement>();
					for (int k = 0; k <= getConfig().getMaxIndex(tempJ + ".condition"); k++) {
						String tempK = tempJ + ".condition(" + k + ")";
						ConfigOpacCatalogueBeautifierElement oteProof = new ConfigOpacCatalogueBeautifierElement(
								getConfig().getString(tempK + "[@tag]"), getConfig().getString(tempK + "[@subtag]"),
								getConfig().getString(tempK + "[@value]").replaceAll("\u2423", " "), getConfig()
										.getString(tempK + "[@mode]", "matches"));
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
	 * return all configured Doctype-Titles from Configfile
	 * ================================================================
	 */
	private static ArrayList<String> getAllDoctypeTitles() {
		ArrayList<String> myList = new ArrayList<String>();
		int countTypes = getConfig().getMaxIndex("doctypes.type");
		for (int i = 0; i <= countTypes; i++) {
			String title = getConfig().getString("doctypes.type(" + i + ")[@title]");
			myList.add(title);
		}
		return myList;
	}

	/**
	 * return all configured Doctype-Titles from Configfile
	 * ================================================================
	 */
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
		int countCatalogues = getConfig().getMaxIndex("catalogue");
		for (int i = 0; i <= countCatalogues; i++) {
			String title = getConfig().getString("catalogue(" + i + ")[@title]");
			if (title.equals(inCatalogue)) {

				// alle speziell gemappten DocTypes eines Kataloges einlesen 

				HashMap<String, String> labels = new HashMap<String, String>();
				int countLabels = getConfig().getMaxIndex("catalogue(" + i + ").specialmapping");
				for (int j = 0; j <= countLabels; j++) {
					String type = getConfig().getString("catalogue(" + i + ").specialmapping[@type]");
					String value = getConfig().getString("catalogue(" + i + ").specialmapping");
					labels.put(value, type);
				}
				if (labels.containsKey(inMapping)) {
					return getDoctypeByName(labels.get(inMapping));
				}
			}
		}

		// falls der Katalog kein spezielles Mapping für den Doctype hat, jetzt in den Doctypes suchen

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
		int countCatalogues = getConfig().getMaxIndex("doctypes.type");
		for (int i = 0; i <= countCatalogues; i++) {
			String title = getConfig().getString("doctypes.type(" + i + ")[@title]");
			if (title.equals(inTitle)) {
				boolean periodical = getConfig().getBoolean("doctypes.type(" + i + ")[@isPeriodical]");
				boolean multiVolume = getConfig().getBoolean("doctypes.type(" + i + ")[@isMultiVolume]");
				boolean containedWork = getConfig().getBoolean("doctypes.type(" + i + ")[@isContainedWork]");
				ArrayList<String> mappings = (ArrayList<String>) getConfig()
						.getList("doctypes.type(" + i + ").mapping");

				ConfigOpacDoctype cod = new ConfigOpacDoctype(title, periodical, multiVolume, containedWork, mappings);
				return cod;
			}
		}
		return null;
	}
}
