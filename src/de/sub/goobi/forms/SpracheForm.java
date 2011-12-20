/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

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