/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package de.sub.goobi.beans;

import de.sub.goobi.config.ConfigMain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.log4j.Logger;

import ugh.dl.Prefs;
import ugh.exceptions.PreferencesException;

@Entity
@Table(name = "ruleset")
public class Regelsatz implements Serializable {
	private static final long serialVersionUID = -6663371963274685060L;

	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;

	@Column(name = "title")
	private String titel;

	@Column(name = "file")
	private String datei;

	@Column(name = "orderMetadataByRuleset")
	private Boolean orderMetadataByRuleset = false;

	private static final Logger logger = Logger.getLogger(Regelsatz.class);

	/*##########################################################################################################
	 ##
	 ##	Getter und Setter
	 ##
	 #########################################################################################################*/

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

	/**
	 *
	 * @return add description
	 */
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

	/**
	 *
	 * @return add description
	 */
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
