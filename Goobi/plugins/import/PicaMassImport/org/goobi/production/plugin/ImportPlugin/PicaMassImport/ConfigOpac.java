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
package org.goobi.production.plugin.ImportPlugin.PicaMassImport;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.commons.io.FilenameUtils;

import de.intranda.goobi.plugins.PicaMassImport;

public class ConfigOpac {
	private static XMLConfiguration config;

	private static XMLConfiguration getConfig() {
		if (config != null)
			return config;
		String configPfad = FilenameUtils.concat(PicaMassImport.getGoobiConfigDirectory(), "goobi_opac.xml");
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

	/**
	 * find Catalogue in Opac-Configurationlist
	 * ================================================================
	 */
	public static ConfigOpacCatalogue getCatalogueByName(String inTitle) {
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
							getConfig().getString(tempJ + "[@value]"));

					// Elemente, die bestimmte Werte haben müssen, als Prüfung, ob das zu ändernde Element geändert werden soll

					ArrayList<ConfigOpacCatalogueBeautifierElement> proofElements = new ArrayList<ConfigOpacCatalogueBeautifierElement>();
					for (int k = 0; k <= getConfig().getMaxIndex(tempJ + ".condition"); k++) {
						String tempK = tempJ + ".condition(" + k + ")";
						ConfigOpacCatalogueBeautifierElement oteProof = new ConfigOpacCatalogueBeautifierElement(
								getConfig().getString(tempK + "[@tag]"), getConfig().getString(tempK + "[@subtag]"),
								getConfig().getString(tempK + "[@value]"));
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

}
