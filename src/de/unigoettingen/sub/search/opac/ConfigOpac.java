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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

import de.sub.goobi.helper.Helper;

public class ConfigOpac {
   private XMLConfiguration config;
   private static String configPfad;

   public ConfigOpac() throws IOException {
	   configPfad = new Helper().getGoobiConfigDirectory() + "goobi_opac.xml";

      if (!(new File(configPfad)).exists()) {
		throw new IOException("File not found: " + configPfad);
	}
      try {
         this.config = new XMLConfiguration(configPfad);
      } catch (ConfigurationException e) {
         e.printStackTrace();
         this.config = new XMLConfiguration();
      }
      this.config.setListDelimiter('&');
      this.config.setReloadingStrategy(new FileChangedReloadingStrategy());
   }

   /**
    * find Catalogue in Opac-Configurationlist
   * ================================================================*/
   public ConfigOpacCatalogue getCatalogueByName(String inTitle) {
      int countCatalogues = this.config.getMaxIndex("catalogue");
      for (int i = 0; i <= countCatalogues; i++) {
         String title = this.config.getString("catalogue(" + i + ")[@title]");
         if (title.equals(inTitle)) {
            String description = this.config.getString("catalogue(" + i + ").config[@description]");
            String address = this.config.getString("catalogue(" + i + ").config[@address]");
            String database = this.config.getString("catalogue(" + i + ").config[@database]");
            String iktlist = this.config.getString("catalogue(" + i + ").config[@iktlist]");
            String cbs = this.config.getString("catalogue(" + i + ").config[@ucnf]", "");
            if (!cbs.equals("")) {
            	cbs = "&" + cbs;
            }
            int port = this.config.getInt("catalogue(" + i + ").config[@port]");
            String charset = "iso-8859-1";
            if (this.config.getString("catalogue(" + i + ").config[@charset]") != null) {
            	charset = this.config.getString("catalogue(" + i + ").config[@charset]");
            }
            
            /* ---------------------
             * Opac-Beautifier einlesen und in Liste zu jedem Catalogue packen
             * -------------------*/
            ArrayList<ConfigOpacCatalogueBeautifier> beautyList = new ArrayList<ConfigOpacCatalogueBeautifier>();
            for (int j = 0; j <= this.config.getMaxIndex("catalogue(" + i + ").beautify.setvalue"); j++) {
               /* Element, dessen Wert geändert werden soll */
               String tempJ = "catalogue(" + i + ").beautify.setvalue(" + j + ")";
               ConfigOpacCatalogueBeautifierElement oteChange = new ConfigOpacCatalogueBeautifierElement(
                     this.config.getString(tempJ + "[@tag]"), this.config.getString(tempJ + "[@subtag]"), this.config
                           .getString(tempJ + "[@value]"));
               /* Elemente, die bestimmte Werte haben müssen, als Prüfung, ob das zu ändernde Element geändert werden soll */
               ArrayList<ConfigOpacCatalogueBeautifierElement> proofElements = new ArrayList<ConfigOpacCatalogueBeautifierElement>();
               for (int k = 0; k <= this.config.getMaxIndex(tempJ + ".condition"); k++) {
                  String tempK = tempJ + ".condition(" + k + ")";
                  ConfigOpacCatalogueBeautifierElement oteProof = new ConfigOpacCatalogueBeautifierElement(
                        this.config.getString(tempK + "[@tag]"), this.config.getString(tempK + "[@subtag]"), this.config
                              .getString(tempK + "[@value]"));
                  proofElements.add(oteProof);
               }
               beautyList.add(new ConfigOpacCatalogueBeautifier(oteChange, proofElements));
            }

            ConfigOpacCatalogue coc = new ConfigOpacCatalogue(title, description, address, database, iktlist,
                  port, charset, cbs, beautyList);
            return coc;
         }
      }
      return null;
   }

   /**
    * return all configured Catalogue-Titles from Configfile
    * ================================================================*/
   public ArrayList<String> getAllCatalogueTitles() {
      ArrayList<String> myList = new ArrayList<String>();
      int countCatalogues = this.config.getMaxIndex("catalogue");
      for (int i = 0; i <= countCatalogues; i++) {
         String title = this.config.getString("catalogue(" + i + ")[@title]");
         myList.add(title);
      }
      return myList;
   }

   /**
    * return all configured Doctype-Titles from Configfile
    * ================================================================*/
   public ArrayList<String> getAllDoctypeTitles() {
      ArrayList<String> myList = new ArrayList<String>();
      int countTypes = this.config.getMaxIndex("doctypes.type");
      for (int i = 0; i <= countTypes; i++) {
         String title = this.config.getString("doctypes.type(" + i + ")[@title]");
         myList.add(title);
      }
      return myList;
   }

   /**
    * return all configured Doctype-Titles from Configfile
    * ================================================================*/
   public ArrayList<ConfigOpacDoctype> getAllDoctypes() {
      ArrayList<ConfigOpacDoctype> myList = new ArrayList<ConfigOpacDoctype>();
      for (String title : getAllDoctypeTitles()) {
         myList.add(getDoctypeByName(title));
      }
      return myList;
   }

   /**
    * get doctype from mapping of opac response
    * first check if there is a special mapping for this
   * ================================================================*/
   public ConfigOpacDoctype getDoctypeByMapping(String inMapping, String inCatalogue) {
      int countCatalogues = this.config.getMaxIndex("catalogue");
      for (int i = 0; i <= countCatalogues; i++) {
         String title = this.config.getString("catalogue(" + i + ")[@title]");
         if (title.equals(inCatalogue)) {
            /* ---------------------
             * alle speziell gemappten DocTypes eines Kataloges einlesen
             * -------------------*/
            HashMap<String, String> labels = new HashMap<String, String>();
            int countLabels = this.config.getMaxIndex("catalogue(" + i + ").specialmapping");
            for (int j = 0; j <= countLabels; j++) {
               String type = this.config.getString("catalogue(" + i + ").specialmapping[@type]");
               String value = this.config.getString("catalogue(" + i + ").specialmapping");
               labels.put(value, type);
            }
            if (labels.containsKey(inMapping)) {
				return getDoctypeByName(labels.get(inMapping));
			}
         }
      }

      /* ---------------------
       * falls der Katalog kein spezielles Mapping für den Doctype hat, jetzt in den Doctypes suchen
       * -------------------*/
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
   * ================================================================*/
   @SuppressWarnings("unchecked")
   public ConfigOpacDoctype getDoctypeByName(String inTitle) {
      int countCatalogues = this.config.getMaxIndex("doctypes.type");
      for (int i = 0; i <= countCatalogues; i++) {
         String title = this.config.getString("doctypes.type(" + i + ")[@title]");
         if (title.equals(inTitle)) {
            /* Sprachen erfassen */
            HashMap<String, String> labels = new HashMap<String, String>();
            int countLabels = this.config.getMaxIndex("doctypes.type(" + i + ").label");
            for (int j = 0; j <= countLabels; j++) {
               String language = this.config.getString("doctypes.type(" + i + ").label(" + j + ")[@language]");
               String value = this.config.getString("doctypes.type(" + i + ").label(" + j + ")");
               labels.put(language, value);
            }
            String inRulesetType = this.config.getString("doctypes.type(" + i + ")[@rulesetType]");
            String inTifHeaderType = this.config.getString("doctypes.type(" + i + ")[@tifHeaderType]");
            boolean periodical = this.config.getBoolean("doctypes.type(" + i + ")[@isPeriodical]");
            boolean multiVolume = this.config.getBoolean("doctypes.type(" + i + ")[@isMultiVolume]");
            boolean containedWork = this.config.getBoolean("doctypes.type(" + i + ")[@isContainedWork]");
            ArrayList<String> mappings = (ArrayList<String>) this.config.getList("doctypes.type(" + i
                  + ").mapping");

            ConfigOpacDoctype cod = new ConfigOpacDoctype(inTitle, inRulesetType, inTifHeaderType,
                  periodical, multiVolume, containedWork, labels, mappings);
            return cod;
         }
      }
      return null;
   }

}
