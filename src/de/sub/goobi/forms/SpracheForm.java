package de.sub.goobi.forms;

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
	   while (FacesContext.getCurrentInstance().getApplication().getSupportedLocales().hasNext()){
		   locale = (Locale) FacesContext.getCurrentInstance().getApplication().getSupportedLocales().next();
		   break;
	   }
//      locale = Locale.GERMANY;
      FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
      Helper.loadLanguageBundle();
   }

   

   /**
    * ermitteln auf welche Sprache umgestellt werden soll
    * 
    * @return Navigationsanweisung "null" als String - daher ein Reload der gleichen Seite mit neuer Sprache
    */
   public String SpracheUmschalten() {
//      Helper help = new Helper();
      String aktuelleSprache = Helper.getRequestParameter("locale");

      if ("en".equals(aktuelleSprache))
         locale = Locale.UK;

      if ("de".equals(aktuelleSprache))
         locale = Locale.GERMANY;

      if ("ru".equals(aktuelleSprache))
         locale = new Locale("ru", "RU");

		if ("es".equals(aktuelleSprache)) {
			locale = new Locale("es");
		}
		
      FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
      Helper.loadLanguageBundle();
      return Helper.getRequestParameter("ziel");
   }

   

   public Locale getLocale() {
      return locale;
   }
}