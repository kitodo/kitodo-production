package de.sub.goobi.config;
//TODO: Move this into the GetOPAC Package
import java.util.ArrayList;
import java.util.HashMap;

public class ConfigOpacDoctype {
   private String title = "";
   private String rulesetType = "";
   private String tifHeaderType = "";
   private boolean periodical = false;
   private boolean multiVolume = false;
   private boolean containedWork = false;
   private HashMap<String, String> labels;
   private ArrayList<String> mappings;

   public ConfigOpacDoctype(String inTitle, String inRulesetType, String inTifHeaderType, boolean inPeriodical, boolean inMultiVolume,
         boolean inContainedWork, HashMap<String, String> inLabels, ArrayList<String> inMappings) {
      title = inTitle;
      rulesetType = inRulesetType;
      tifHeaderType = inTifHeaderType;
      periodical = inPeriodical;
      multiVolume = inMultiVolume;
      containedWork = inContainedWork;
      labels = inLabels;
      mappings = inMappings;
   }

   public String getTitle() {
      return title;
   }

   public String getRulesetType() {
      return rulesetType;
   }
   
   public String getTifHeaderType() {
      return tifHeaderType;
   }
   
   public boolean isPeriodical() {
      return periodical;
   }

   public boolean isMultiVolume() {
      return multiVolume;
   }

   public boolean isContainedWork() {
      return containedWork;
   }

   public HashMap<String, String> getLabels() {
      return labels;
   }

   public ArrayList<String> getMappings() {
      return mappings;
   }

   public void setMappings(ArrayList<String> mappings) {
      this.mappings = mappings;
   }

}
