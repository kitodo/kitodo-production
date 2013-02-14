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

package de.sub.goobi.beans;

import java.io.Serializable;

import org.apache.log4j.Logger;

import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;
import de.sub.goobi.config.ConfigMain;

public class Regelsatz implements Serializable {
	private static final long serialVersionUID = -6663371963274685060L;
	private Integer id;
	private String titel;
	private String datei;
	private Prefs mypreferences;
	private Boolean orderMetadataByRuleset = false;
	private static final Logger logger = Logger.getLogger(Regelsatz.class);

	/*#####################################################
	 #####################################################
	 ##																															 
	 ##																Getter und Setter									
	 ##                                                   															    
	 #####################################################
	 ####################################################*/

	public String getDatei() {
		return datei;
	}

	public void setDatei(String datei) {
		this.datei = datei;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitel() {
		return titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public Prefs getPreferences() {
		mypreferences = new Prefs();
		try {
			mypreferences.loadPrefs(ConfigMain.getParameter("RegelsaetzeVerzeichnis")
					+ datei);
		} catch (PreferencesException e) {
			logger.error(e);
		}
		return mypreferences;
	}

	public boolean isOrderMetadataByRuleset() {
		return isOrderMetadataByRulesetHibernate();
	}

	public void setOrderMetadataByRuleset(boolean orderMetadataByRuleset) {
		this.orderMetadataByRuleset = orderMetadataByRuleset;
	}

	public Boolean isOrderMetadataByRulesetHibernate() {
		if (orderMetadataByRuleset == null)
			orderMetadataByRuleset = false;
		return orderMetadataByRuleset;
	}

	public void setOrderMetadataByRulesetHibernate(
			Boolean orderMetadataByRuleset) {
		this.orderMetadataByRuleset = orderMetadataByRuleset;
	}
}
