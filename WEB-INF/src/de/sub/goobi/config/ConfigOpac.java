package de.sub.goobi.config;
//TODO: Move this into the GetOPAC Package
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
//      AbstractConfiguration.setDefaultListDelimiter('&');
      configPfad = new Helper().getGoobiConfigDirectory() + "opac.xml";

      if (!(new File(configPfad)).exists())
         throw new IOException("File not found: " + configPfad);
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
   * ================================================================*/
   public ConfigOpacCatalogue getCatalogueByName(String inTitle) {
      int countCatalogues = config.getMaxIndex("catalogue");
      for (int i = 0; i <= countCatalogues; i++) {
         String title = config.getString("catalogue(" + i + ")[@title]");
         if (title.equals(inTitle)) {
            String description = config.getString("catalogue(" + i + ").config[@description]");
            String address = config.getString("catalogue(" + i + ").config[@address]");
            String database = config.getString("catalogue(" + i + ").config[@database]");
            String iktlist = config.getString("catalogue(" + i + ").config[@iktlist]");
            int port = config.getInt("catalogue(" + i + ").config[@port]");

            String charset = "iso-8859-1";
            if (config.getString("catalogue(" + i + ").config[@charset]") != null) {
            	charset = config.getString("catalogue(" + i + ").config[@charset]");
            }
            
            /* ---------------------
             * Opac-Beautifier einlesen und in Liste zu jedem Catalogue packen
             * -------------------*/
            ArrayList<ConfigOpacCatalogueBeautifier> beautyList = new ArrayList<ConfigOpacCatalogueBeautifier>();
            for (int j = 0; j <= config.getMaxIndex("catalogue(" + i + ").beautify.setvalue"); j++) {
               /* Element, dessen Wert geändert werden soll */
               String tempJ = "catalogue(" + i + ").beautify.setvalue(" + j + ")";
               ConfigOpacCatalogueBeautifierElement oteChange = new ConfigOpacCatalogueBeautifierElement(
                     config.getString(tempJ + "[@tag]"), config.getString(tempJ + "[@subtag]"), config
                           .getString(tempJ + "[@value]"));
               /* Elemente, die bestimmte Werte haben müssen, als Prüfung, ob das zu ändernde Element geändert werden soll */
               ArrayList<ConfigOpacCatalogueBeautifierElement> proofElements = new ArrayList<ConfigOpacCatalogueBeautifierElement>();
               for (int k = 0; k <= config.getMaxIndex(tempJ + ".condition"); k++) {
                  String tempK = tempJ + ".condition(" + k + ")";
                  ConfigOpacCatalogueBeautifierElement oteProof = new ConfigOpacCatalogueBeautifierElement(
                        config.getString(tempK + "[@tag]"), config.getString(tempK + "[@subtag]"), config
                              .getString(tempK + "[@value]"));
                  proofElements.add(oteProof);
               }
               beautyList.add(new ConfigOpacCatalogueBeautifier(oteChange, proofElements));
            }

            ConfigOpacCatalogue coc = new ConfigOpacCatalogue(title, description, address, database, iktlist,
                  port, charset, beautyList);
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
      int countCatalogues = config.getMaxIndex("catalogue");
      for (int i = 0; i <= countCatalogues; i++) {
         String title = config.getString("catalogue(" + i + ")[@title]");
         myList.add(title);
      }
      return myList;
   }

   /**
    * return all configured Doctype-Titles from Configfile
    * ================================================================*/
   public ArrayList<String> getAllDoctypeTitles() {
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
      int countCatalogues = config.getMaxIndex("catalogue");
      for (int i = 0; i <= countCatalogues; i++) {
         String title = config.getString("catalogue(" + i + ")[@title]");
         if (title.equals(inCatalogue)) {
            /* ---------------------
             * alle speziell gemappten DocTypes eines Kataloges einlesen
             * -------------------*/
            HashMap<String, String> labels = new HashMap<String, String>();
            int countLabels = config.getMaxIndex("catalogue(" + i + ").specialmapping");
            for (int j = 0; j <= countLabels; j++) {
               String type = config.getString("catalogue(" + i + ").specialmapping[@type]");
               String value = config.getString("catalogue(" + i + ").specialmapping");
               labels.put(value, type);
            }
            if (labels.containsKey(inMapping))
               return getDoctypeByName(labels.get(inMapping));
         }
      }

      /* ---------------------
       * falls der Katalog kein spezielles Mapping für den Doctype hat, jetzt in den Doctypes suchen
       * -------------------*/
      for (String title : getAllDoctypeTitles()) {
         ConfigOpacDoctype tempType = getDoctypeByName(title);
         if (tempType.getMappings().contains(inMapping))
            return tempType;
      }
      return null;
   }

   /**
    * get doctype from title
   * ================================================================*/
   @SuppressWarnings("unchecked")
   public ConfigOpacDoctype getDoctypeByName(String inTitle) {
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
            ArrayList<String> mappings = (ArrayList<String>) config.getList("doctypes.type(" + i
                  + ").mapping");

            ConfigOpacDoctype cod = new ConfigOpacDoctype(inTitle, inRulesetType, inTifHeaderType,
                  periodical, multiVolume, containedWork, labels, mappings);
            return cod;
         }
      }
      return null;
   }

}
