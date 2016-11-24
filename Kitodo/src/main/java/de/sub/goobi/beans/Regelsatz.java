package de.sub.goobi.beans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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
		return this.datei;
	}

	public void setDatei(String datei) {
		this.datei = datei;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitel() {
		return this.titel;
	}

	public void setTitel(String titel) {
		this.titel = titel;
	}

	public Prefs getPreferences() {
		Prefs mypreferences = new Prefs();
		try {
			mypreferences.loadPrefs(ConfigMain.getParameter("RegelsaetzeVerzeichnis") + getDatei());
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
		if (this.orderMetadataByRuleset == null) {
			this.orderMetadataByRuleset = false;
		}
		return this.orderMetadataByRuleset;
	}

	public void setOrderMetadataByRulesetHibernate(Boolean orderMetadataByRuleset) {
		this.orderMetadataByRuleset = orderMetadataByRuleset;
	}
}
