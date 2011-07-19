package de.sub.goobi.Beans;

import java.io.Serializable;

public class Werkstueckeigenschaft implements Serializable {
   private static final long serialVersionUID = -88407008893258729L;
   private Integer id;
   private String titel;
   private String wert;
   private boolean istObligatorisch;
   private Integer datentyp;
   private String auswahl;
   private Werkstueck werkstueck;

   public Werkstueckeigenschaft() {
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

   public Werkstueck getWerkstueck() {
      return werkstueck;
   }

   public void setWerkstueck(Werkstueck werkstueck) {
      this.werkstueck = werkstueck;
   }
}
