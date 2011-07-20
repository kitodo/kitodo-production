package de.sub.goobi.config;
//TODO: Move this into the GetOPAC Package
public class ConfigOpacCatalogueBeautifierElement {
   private String tag = "";
   private String subtag = "";
   private String value = "";
   
   public ConfigOpacCatalogueBeautifierElement(String inTag, String inSubTag, String inValue) {
      tag = inTag;
      subtag = inSubTag;
      value = inValue;
   }
   
   public String getTag() {
      return tag;
   }
   public void setTag(String tag) {
      this.tag = tag;
   }
   public String getSubtag() {
      return subtag;
   }
   public void setSubtag(String subtag) {
      this.subtag = subtag;
   }
   public String getValue() {
      return value;
   }
   public void setValue(String value) {
      this.value = value;
   }
   
   @Override
   public String toString() {
    return tag + " - " + subtag + " : " + value;
   }
}