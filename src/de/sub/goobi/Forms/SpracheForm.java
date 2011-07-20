package de.sub.goobi.Forms;

import java.util.Locale;

import javax.faces.context.FacesContext;

import de.sub.goobi.helper.Helper;

/**
 * Klasse SpracheForm für die Umstellung der Sprache aus dem 
 * laufenden Servlet
 */
public class SpracheForm {
   private Locale locale;

   

   public SpracheForm() {
	   //TODO: Change default language to English
      locale = Locale.GERMANY;
      FacesContext.getCurrentInstance().getViewRoot().setLocale(Locale.GERMANY);
      Helper.loadLanguageBundle();
   }

   

   /**
    * ermitteln auf welche Sprache umgestellt werden soll
    * 
    * @return Navigationsanweisung "null" als String - daher ein Reload der gleichen Seite mit neuer Sprache
    */
   public String SpracheUmschalten() {
      Helper help = new Helper();
      String aktuelleSprache = help.getRequestParameter("locale");

      if ("en".equals(aktuelleSprache))
         locale = Locale.UK;

      if ("de".equals(aktuelleSprache))
         locale = Locale.GERMANY;

      if ("ru".equals(aktuelleSprache))
         locale = new Locale("ru", "RU");

      FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
      Helper.loadLanguageBundle();
      return help.getRequestParameter("ziel");
   }

   

   public Locale getLocale() {
      return locale;
   }
}