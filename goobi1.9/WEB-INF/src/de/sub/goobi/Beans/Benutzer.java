package de.sub.goobi.Beans;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 * 			- http://digiverso.com 
 * 			- http://www.intranda.com
 * 
 * Copyright 2011, intranda GmbH, Göttingen
 * 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.encryption.DesEncrypter;
import de.sub.goobi.helper.ldap.Ldap;

public class Benutzer implements Serializable {
	private static final long serialVersionUID = -7482853955996650586L;
	private Integer id;
	private String vorname;
	private String nachname;
	private String login;
	private String ldaplogin;
	private String passwort;
	private boolean istAktiv = true;
	private String isVisible;
	private String standort;
	private Integer tabellengroesse = Integer.valueOf(10);
	private Integer sessiontimeout = 7200;
	private boolean confVorgangsdatumAnzeigen = false;
	private String metadatenSprache;
	private Set<Benutzergruppe> benutzergruppen;
	private Set<Schritt> schritte;
	private Set<Schritt> bearbeitungsschritte;
	private Set<Projekt> projekte;
	private Set<Benutzereigenschaft> eigenschaften;
	private boolean mitMassendownload = false;
	private LdapGruppe ldapGruppe;
	private String css;

	// private String lastFilter = null;

	public Benutzer() {
		this.benutzergruppen = new HashSet<Benutzergruppe>();
		this.projekte = new HashSet<Projekt>();
		this.schritte = new HashSet<Schritt>();
		this.eigenschaften = new HashSet<Benutzereigenschaft>();
	}

	/*
	 * =======================================================
	 * 
	 * Getter und Setter
	 * 
	 * ========================================================
	 */

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLogin() {
		return this.login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getNachname() {
		return this.nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getPasswort() {
		return this.passwort;
	}

	public void setPasswort(String inpasswort) {
		this.passwort = inpasswort;
	}

	public String getPasswortCrypt() {
		DesEncrypter encrypter = new DesEncrypter();
		String decrypted = encrypter.decrypt(this.passwort);
		return decrypted;
	}

	public void setPasswortCrypt(String inpasswort) {
		DesEncrypter encrypter = new DesEncrypter();
		String encrypted = encrypter.encrypt(inpasswort);
		this.passwort = encrypted;
	}

	public boolean isIstAktiv() {
		return this.istAktiv;
	}

	public void setIstAktiv(boolean istAktiv) {
		this.istAktiv = istAktiv;
	}

	public void setIsVisible(String isVisible) {
		this.isVisible = isVisible;
	}

	public String getIsVisible() {
		return this.isVisible;
	}

	public String getStandort() {
		return this.standort;
	}

	public void setStandort(String instandort) {
		this.standort = instandort;
	}

	public String getVorname() {
		return this.vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public Integer getTabellengroesse() {
		if (this.tabellengroesse == null) {
			return Integer.valueOf(10);
		}
		// else if (this.tabellengroesse > 100) {
		// return Integer.valueOf(100);
		// }

		return this.tabellengroesse;
	}

	public void setTabellengroesse(Integer tabellengroesse) {
		// if (tabellengroesse > 100) {
		// tabellengroesse = Integer.valueOf(100);
		// }
		this.tabellengroesse = tabellengroesse;
	}

	public boolean isMitMassendownload() {
		return this.mitMassendownload;
	}

	public void setMitMassendownload(boolean mitMassendownload) {
		this.mitMassendownload = mitMassendownload;
	}

	public LdapGruppe getLdapGruppe() {
		return this.ldapGruppe;
	}

	public void setLdapGruppe(LdapGruppe ldapGruppe) {
		this.ldapGruppe = ldapGruppe;
	}

	/*---------------------------------------------------------------------------------------------------------
	 Datum: 24.06.2005, 23:34:10
	 Zweck: Set für Benutzergruppen
	 ---------------------------------------------------------------------------------------------------------*/
	public Set<Benutzergruppe> getBenutzergruppen() {
		return this.benutzergruppen;
	}

	public void setBenutzergruppen(Set<Benutzergruppe> benutzergruppen) {
		this.benutzergruppen = benutzergruppen;
	}

	public int getBenutzergruppenSize() {
		if (this.benutzergruppen == null) {
			return 0;
		} else {
			return this.benutzergruppen.size();
		}
	}

	public List<Benutzergruppe> getBenutzergruppenList() {
		try {
			Hibernate.initialize(this.benutzergruppen);
		} catch (HibernateException e) {
		}
		if (this.benutzergruppen == null) {
			return new ArrayList<Benutzergruppe>();
		} else {
			return new ArrayList<Benutzergruppe>(this.benutzergruppen);
		}
	}

	/*---------------------------------------------------------------------------------------------------------
	 Datum: 24.06.2005, 23:34:10
	 Zweck: Set für Schritte
	 ---------------------------------------------------------------------------------------------------------*/

	public Set<Schritt> getSchritte() {
		return this.schritte;
	}

	public void setSchritte(Set<Schritt> schritte) {
		this.schritte = schritte;
	}

	public int getSchritteSize() {
		try {
			Hibernate.initialize(this.schritte);
		} catch (HibernateException e) {
		}
		if (this.schritte == null) {
			return 0;
		} else {
			return this.schritte.size();
		}
	}

	public List<Schritt> getSchritteList() {
		try {
			Hibernate.initialize(this.schritte);
		} catch (HibernateException e) {
		}
		if (this.schritte == null) {
			return new ArrayList<Schritt>();
		} else {
			return new ArrayList<Schritt>(this.schritte);
		}
	}

	/*---------------------------------------------------------------------------------------------------------
	 Datum: 24.06.2005, 23:34:10
	 Zweck: Set für BearbeitungsSchritte
	 ---------------------------------------------------------------------------------------------------------*/
	public Set<Schritt> getBearbeitungsschritte() {
		return this.bearbeitungsschritte;
	}

	public void setBearbeitungsschritte(Set<Schritt> bearbeitungsschritte) {
		this.bearbeitungsschritte = bearbeitungsschritte;
	}

	public int getBearbeitungsschritteSize() {
		try {
			Hibernate.initialize(this.bearbeitungsschritte);
		} catch (HibernateException e) {
		}
		if (this.bearbeitungsschritte == null) {
			return 0;
		} else {
			return this.bearbeitungsschritte.size();
		}
	}

	public List<Schritt> getBearbeitungsschritteList() {
		try {
			Hibernate.initialize(this.bearbeitungsschritte);
		} catch (HibernateException e) {
		}
		if (this.bearbeitungsschritte == null) {
			this.bearbeitungsschritte = new HashSet<Schritt>();
		}
		return new ArrayList<Schritt>(this.bearbeitungsschritte);
	}

	/*---------------------------------------------------------------------------------------------------------
	 Datum: 24.02.2006, 23:34:10
	 Zweck: Set für Projekte
	 ---------------------------------------------------------------------------------------------------------*/

	public Set<Projekt> getProjekte() {
		return this.projekte;
	}

	public void setProjekte(Set<Projekt> projekte) {
		this.projekte = projekte;
	}

	public int getProjekteSize() {
		try {
			Hibernate.initialize(this.projekte);
		} catch (HibernateException e) {
		}
		if (this.projekte == null) {
			return 0;
		} else {
			return this.projekte.size();
		}
	}

	public List<Projekt> getProjekteList() {
		try {
			Hibernate.initialize(this.projekte);
		} catch (HibernateException e) {
		}
		if (this.projekte == null) {
			return new ArrayList<Projekt>();
		} else {
			return new ArrayList<Projekt>(this.projekte);
		}
	}

	public boolean isConfVorgangsdatumAnzeigen() {
		return this.confVorgangsdatumAnzeigen;
	}

	public void setConfVorgangsdatumAnzeigen(boolean confVorgangsdatumAnzeigen) {
		this.confVorgangsdatumAnzeigen = confVorgangsdatumAnzeigen;
	}

	public String getMetadatenSprache() {
		return this.metadatenSprache;
	}

	public void setMetadatenSprache(String metadatenSprache) {
		this.metadatenSprache = metadatenSprache;
	}

	public String getLdaplogin() {
		return this.ldaplogin;
	}
	
	public void setLdaplogin(String ldaplogin) {
		this.ldaplogin = ldaplogin;
	}
	
	/*
	 * ## Helper ##
	 */

	public boolean istPasswortKorrekt(String inPasswort) {
		if (inPasswort == null || inPasswort.length() == 0) {

			return false;
		} else {

			/* Verbindung zum LDAP-Server aufnehmen und Login prüfen, wenn LDAP genutzt wird */

			if (ConfigMain.getBooleanParameter("ldap_use")) {
				Ldap myldap = new Ldap();
				return myldap.isUserPasswordCorrect(this, inPasswort);
			} else {
				// return passwort.equals(inPasswort);
				DesEncrypter encrypter = new DesEncrypter();
				String encoded = encrypter.encrypt(inPasswort);
				return this.passwort.equals(encoded);
			}
		}
	}

	public String getNachVorname() {
		return this.nachname + ", " + this.vorname;
	}

	/**
	 * BenutzerHome ermitteln und zurückgeben (entweder aus dem LDAP oder direkt aus der Konfiguration)
	 * 
	 * @return Path as String
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public String getHomeDir() throws IOException, InterruptedException {
		String rueckgabe = "";
		/* wenn LDAP genutzt wird, HomeDir aus LDAP ermitteln, ansonsten aus der Konfiguration */

		if (ConfigMain.getBooleanParameter("ldap_use")) {
			Ldap myldap = new Ldap();
			rueckgabe = myldap.getUserHomeDirectory(this);
		} else {
			rueckgabe = ConfigMain.getParameter("dir_Users") + this.login;
		}

		if (rueckgabe.equals("")) {
			return "";
		}

		if (!rueckgabe.endsWith(File.separator)) {
			rueckgabe += File.separator;
		}
		/* wenn das Verzeichnis nicht "" ist, aber noch nicht existiert, dann jetzt anlegen */
		File homePath = new File(rueckgabe);
		if (!homePath.exists()) {
			new Helper().createUserDirectory(rueckgabe, this.login);
		}
		return rueckgabe;
	}

	public Integer getSessiontimeout() {
		if (this.sessiontimeout == null) {
			this.sessiontimeout = 7200;
		}
		return this.sessiontimeout;
	}

	public void setSessiontimeout(Integer sessiontimeout) {
		this.sessiontimeout = sessiontimeout;
	}

	public Integer getSessiontimeoutInMinutes() {
		return getSessiontimeout() / 60;
	}

	public void setSessiontimeoutInMinutes(Integer sessiontimeout) {
		if (sessiontimeout.intValue() < 5) {
			this.sessiontimeout = 5 * 60;
		} else {
			this.sessiontimeout = sessiontimeout * 60;
		}
	}

	public String getCss() {
		if (this.css == null || this.css.length() == 0) {
			this.css = "/css/default.css";
		}
		return this.css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	/*
	 * added 05.05.2010 used for user filter
	 */

	/**
	 * @return set of all properties
	 */
	public Set<Benutzereigenschaft> getEigenschaften() {
		return this.eigenschaften;
	}

	/**
	 * 
	 * @param eigenschaften
	 *            set of all properties
	 */

	public void setEigenschaften(Set<Benutzereigenschaft> eigenschaften) {
		this.eigenschaften = eigenschaften;
	}

	/**
	 * 
	 * @return size of properties
	 */

	public int getEigenschaftenSize() {
		try {
			Hibernate.initialize(this.eigenschaften);
		} catch (HibernateException e) {
		}
		if (this.eigenschaften == null) {
			return 0;
		} else {
			return this.eigenschaften.size();
		}
	}

	/**
	 * 
	 * @return List of all properties
	 */
	public List<Benutzereigenschaft> getEigenschaftenList() {
		try {
			Hibernate.initialize(this.eigenschaften);
		} catch (HibernateException e) {
		}
		if (this.eigenschaften == null) {
			return new ArrayList<Benutzereigenschaft>();
		} else {
			return new ArrayList<Benutzereigenschaft>(this.eigenschaften);
		}
	}

	/**
	 * 
	 * @return List of filters as strings
	 */

	public List<String> getFilters() {
		List<String> filters = new ArrayList<String>();
		if (this.getEigenschaften() != null) {
			for (Benutzereigenschaft hgp : this.getEigenschaftenList()) {
				if (hgp.getTitel().equals("_filter")) {
					filters.add(hgp.getWert());
				}
			}
		}
		return filters;
	}

	/**
	 * adds a new filter to list
	 * 
	 * @param inFilter
	 *            the filter to add
	 */

	public void addFilter(String inFilter) {
		try {
			Hibernate.initialize(this.eigenschaften);
		} catch (HibernateException e) {
		}
		if (this.eigenschaften == null) {
			this.eigenschaften = new HashSet<Benutzereigenschaft>();
		}
		// no double entries here
		for (Benutzereigenschaft be : this.eigenschaften) {
			if (be.getTitel().equals("_filter") && be.getWert().equals(inFilter)) {
				return;
			}
		}
		Benutzereigenschaft be = new Benutzereigenschaft();
		be.setBenutzer(this);
		be.setTitel("_filter");
		be.setWert(inFilter);
		this.eigenschaften.add(be);
	}

	/**
	 * removes filter from list
	 * 
	 * @param inFilter
	 *            the filter to remove
	 */
	public void removeFilter(String inFilter) {
		try {
			Hibernate.initialize(this.eigenschaften);
		} catch (HibernateException e) {
		}
		if (this.eigenschaften != null) {
			for (Benutzereigenschaft be : this.eigenschaften) {
				if (be.getTitel().equals("_filter") && be.getWert().equals(inFilter)) {
					this.eigenschaften.remove(be);
					return;
				}
			}
		}
	}
}


