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
import de.sub.goobi.helper.Messages;

/**
 * The SpracheForm class serves to switch the displayed language for the current
 * user in the running application
 */
public class SpracheForm {
	private Locale locale;

	/**
	 * The constructor of this class sets the locale to the first available
	 * value and loads the required MessageBundle
	 */
	public SpracheForm() {
		while (FacesContext.getCurrentInstance().getApplication()
				.getSupportedLocales().hasNext()) {
			locale = (Locale) FacesContext.getCurrentInstance()
					.getApplication().getSupportedLocales().next();
			break;
		}
		FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
		Messages.loadLanguageBundle();
	}

	/**
	 * The procedure switchLanguage is used to alter the application’s interface
	 * language.
	 * 
	 * @param langCodeCombined
	 *            This parameter can be either of form “‹language›” or of form
	 *            “‹language›_‹country›”, e.g. “en” or “en_GB” are valid values.
	 */
	public void switchLanguage(String langCodeCombined) {
		String[] languageCode = langCodeCombined.split("_");
		if (languageCode.length == 2) {
			locale = new Locale(languageCode[0], languageCode[1]);
		} else {
			locale = new Locale(languageCode[0]);
		}
		FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
		Messages.loadLanguageBundle();
	}

	/**
	 * The procedure SpracheUmschalten is called from /pages/Metadaten2oben.jsp
	 * to switch the language.
	 * 
	 * @return the empty String to point to the JSF framework to remain on the
	 *         current page
	 */
	public String SpracheUmschalten() {
		String languageCodeCombined = Helper.getRequestParameter("locale");
		switchLanguage(languageCodeCombined);
		return Helper.getRequestParameter("ziel");
	}

	public Locale getLocale() {
		return locale;
	}
}
