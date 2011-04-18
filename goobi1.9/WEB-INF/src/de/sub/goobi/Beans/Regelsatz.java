package de.sub.goobi.Beans;

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
