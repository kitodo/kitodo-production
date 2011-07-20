package de.sub.goobi.Beans;

import java.io.Serializable;

public class Vorlageeigenschaft implements Serializable {
   private static final long serialVersionUID = -5981263038302791497L;
   private Integer id;
   private String titel;
   private String wert;
   private boolean istObligatorisch;
   private Integer datentyp;
   private String auswahl;
   private Vorlage vorlage;

   public Vorlageeigenschaft() {
   }

   /*#####################################################
    #####################################################
    ##                                                                                                                          
    ##                                                             Getter und Setter                                   
    ##                                                                                                                 
    #####################################################
    ####################################################*/

   public String getAuswahl() {
      return auswahl;
   }

   public void setAuswahl(String auswahl) {
      this.auswahl = auswahl;
   }

   public Integer getDatentyp() {
      return datentyp;
   }

   public void setDatentyp(Integer datentyp) {
      this.datentyp = datentyp;
   }

   public Integer getId() {
      return id;
   }

   public void setId(Integer id) {
      this.id = id;
   }

   public boolean isIstObligatorisch() {
      return istObligatorisch;
   }

   public void setIstObligatorisch(boolean istObligatorisch) {
      this.istObligatorisch = istObligatorisch;
   }

   public String getTitel() {
      return titel;
   }

   public void setTitel(String titel) {
      this.titel = titel;
   }

   public String getWert() {
      return wert;
   }

   public void setWert(String wert) {
      this.wert = wert;
   }

   public void setVorlage(Vorlage vorlage) {
      this.vorlage = vorlage;
   }
   
   public Vorlage getVorlage() {
      return vorlage;
   }
}
