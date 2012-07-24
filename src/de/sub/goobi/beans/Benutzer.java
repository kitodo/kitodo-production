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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.ldap.Ldap;
import dubious.sub.goobi.helper.encryption.DesEncrypter;

public class Benutzer implements Serializable {
	private static final long serialVersionUID = -7482853955996650586L;
	private Integer id;
	private String vorname;
	private String nachname;
	private String login;
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
	private boolean mitMassendownload = false;
	private LdapGruppe ldapGruppe;
	private String css;
	private Set<Benutzereigenschaft> eigenschaften;

	public Benutzer() {
		benutzergruppen = new HashSet<Benutzergruppe>();
		projekte = new HashSet<Projekt>();
		schritte = new HashSet<Schritt>();
		eigenschaften = new HashSet<Benutzereigenschaft>();
	}

	/*
	 * =======================================================
	 * 
	 * Getter und Setter
	 * 
	 * ========================================================
	 */

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getNachname() {
		return nachname;
	}

	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getPasswort() {
		return passwort;
	}

	public void setPasswort(String inpasswort) {
		this.passwort = inpasswort;
	}

	public String getPasswortCrypt() {
		DesEncrypter encrypter = new DesEncrypter();
		String decrypted = encrypter.decrypt(passwort);
		return decrypted;
	}

	public void setPasswortCrypt(String inpasswort) {
		DesEncrypter encrypter = new DesEncrypter();
		String encrypted = encrypter.encrypt(inpasswort);
		this.passwort = encrypted;
	}

	public boolean isIstAktiv() {
		return istAktiv;
	}

	public void setIstAktiv(boolean istAktiv) {
		this.istAktiv = istAktiv;
	}

	public void setIsVisible(String isVisible) {
		this.isVisible = isVisible;
	}

	public String getIsVisible() {
		return isVisible;
	}

	public String getStandort() {
		return standort;
	}

	public void setStandort(String instandort) {
		standort = instandort;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public Integer getTabellengroesse() {
		if (this.tabellengroesse == null) {
			return Integer.valueOf(10);
		} else if (this.tabellengroesse > 100) {
			return Integer.valueOf(100);
		}

		return tabellengroesse;
	}

	public void setTabellengroesse(Integer tabellengroesse) {
		if (tabellengroesse > 100) {
			tabellengroesse = Integer.valueOf(100);
		}
		this.tabellengroesse = tabellengroesse;
	}

	public boolean isMitMassendownload() {
		return mitMassendownload;
	}

	public void setMitMassendownload(boolean mitMassendownload) {
		this.mitMassendownload = mitMassendownload;
	}

	public LdapGruppe getLdapGruppe() {
		return ldapGruppe;
	}

	public void setLdapGruppe(LdapGruppe ldapGruppe) {
		this.ldapGruppe = ldapGruppe;
	}

	/*---------------------------------------------------------------------------------------------------------
	 Datum: 24.06.2005, 23:34:10
	 Zweck: Set für Benutzergruppen
	 ---------------------------------------------------------------------------------------------------------*/
	public Set<Benutzergruppe> getBenutzergruppen() {
		return benutzergruppen;
	}

	public void setBenutzergruppen(Set<Benutzergruppe> benutzergruppen) {
		this.benutzergruppen = benutzergruppen;
	}

	public int getBenutzergruppenSize() {
		if (benutzergruppen == null)
			return 0;
		else
			return benutzergruppen.size();
	}

	public List<Benutzergruppe> getBenutzergruppenList() {
		if (benutzergruppen == null)
			return new ArrayList<Benutzergruppe>();
		else
			return new ArrayList<Benutzergruppe>(benutzergruppen);
	}

	/*---------------------------------------------------------------------------------------------------------
	 Datum: 24.06.2005, 23:34:10
	 Zweck: Set für Schritte
	 ---------------------------------------------------------------------------------------------------------*/

	public Set<Schritt> getSchritte() {
		return schritte;
	}

	public void setSchritte(Set<Schritt> schritte) {
		this.schritte = schritte;
	}

	public int getSchritteSize() {
		if (schritte == null)
			return 0;
		else
			return schritte.size();
	}

	public List<Schritt> getSchritteList() {
		if (schritte == null)
			return new ArrayList<Schritt>();
		else
			return new ArrayList<Schritt>(schritte);
	}

	/*---------------------------------------------------------------------------------------------------------
	 Datum: 24.06.2005, 23:34:10
	 Zweck: Set für BearbeitungsSchritte
	 ---------------------------------------------------------------------------------------------------------*/
	public Set<Schritt> getBearbeitungsschritte() {
		return bearbeitungsschritte;
	}

	public void setBearbeitungsschritte(Set<Schritt> bearbeitungsschritte) {
		this.bearbeitungsschritte = bearbeitungsschritte;
	}

	public int getBearbeitungsschritteSize() {
		if (bearbeitungsschritte == null)
			return 0;
		else
			return bearbeitungsschritte.size();
	}

	public List<Schritt> getBearbeitungsschritteList() {
		if (bearbeitungsschritte == null)
			bearbeitungsschritte = new HashSet<Schritt>();
		return new ArrayList<Schritt>(bearbeitungsschritte);
	}

	/*---------------------------------------------------------------------------------------------------------
	 Datum: 24.02.2006, 23:34:10
	 Zweck: Set für Projekte
	 ---------------------------------------------------------------------------------------------------------*/

	public Set<Projekt> getProjekte() {
		return projekte;
	}

	public void setProjekte(Set<Projekt> projekte) {
		this.projekte = projekte;
	}

	public int getProjekteSize() {
		if (projekte == null)
			return 0;
		else
			return projekte.size();
	}

	public List<Projekt> getProjekteList() {
		if (projekte == null)
			return new ArrayList<Projekt>();
		else {
			return new ArrayList<Projekt>(projekte);
		}
	}

	public boolean isConfVorgangsdatumAnzeigen() {
		return confVorgangsdatumAnzeigen;
	}

	public void setConfVorgangsdatumAnzeigen(boolean confVorgangsdatumAnzeigen) {
		this.confVorgangsdatumAnzeigen = confVorgangsdatumAnzeigen;
	}

	public String getMetadatenSprache() {
		return metadatenSprache;
	}

	public void setMetadatenSprache(String metadatenSprache) {
		this.metadatenSprache = metadatenSprache;
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
				DesEncrypter encrypter = new DesEncrypter();
				String encoded = encrypter.encrypt(inPasswort);
				return passwort.equals(encoded);
			}
		}
	}

	public String getNachVorname() {
		return nachname + ", " + vorname;
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
			rueckgabe = ConfigMain.getParameter("dir_Users") + login;
		}

		if (rueckgabe.equals(""))
			return "";

		if (!rueckgabe.endsWith(File.separator))
			rueckgabe += File.separator;
		/* wenn das Verzeichnis nicht "" ist, aber noch nicht existiert, dann jetzt anlegen */
		FilesystemHelper.createDirectoryForUser(rueckgabe, login);
		return rueckgabe;
	}

	public Integer getSessiontimeout() {
		if (sessiontimeout == null)
			sessiontimeout = 7200;
		return sessiontimeout;
	}

	public void setSessiontimeout(Integer sessiontimeout) {
		this.sessiontimeout = sessiontimeout;
	}

	public Integer getSessiontimeoutInMinutes() {
		return getSessiontimeout() / 60;
	}

	public void setSessiontimeoutInMinutes(Integer sessiontimeout) {
		if (sessiontimeout.intValue() < 5)
			this.sessiontimeout = 5 * 60;
		else
			this.sessiontimeout = sessiontimeout * 60;
	}

	public String getCss() {
		if (css == null || css.length() == 0)
			css = "/css/default.css";
		return css;
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
		return eigenschaften;
	}

	/**
	 * 
	 * @param eigenschaften set of all properties
	 */

	public void setEigenschaften(Set<Benutzereigenschaft> eigenschaften) {
		this.eigenschaften = eigenschaften;
	}

	/**
	 * 
	 * @return size of properties
	 */
	
	public int getEigenschaftenSize() {
		if (eigenschaften == null)
			return 0;
		else
			return eigenschaften.size();
	}

	/**
	 * 
	 * @return List of all properties
	 */
	public List<Benutzereigenschaft> getEigenschaftenList() {
		if (eigenschaften == null)
			return new ArrayList<Benutzereigenschaft>();
		else
			return new ArrayList<Benutzereigenschaft>(eigenschaften);
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
	 * @param inFilter the filter to add
	 */
	
	public void addFilter(String inFilter) {
		if (eigenschaften == null) {
			eigenschaften = new HashSet<Benutzereigenschaft>();
		}
		// no double entries here
		for (Benutzereigenschaft be : eigenschaften) {
			if (be.getTitel().equals("_filter") && be.getWert().equals(inFilter)) {
				return;
			}
		}
		Benutzereigenschaft be = new Benutzereigenschaft();
		be.setBenutzer(this);
		be.setTitel("_filter");
		be.setWert(inFilter);
		eigenschaften.add(be);
	}

	
	/**
	 * removes filter from list
	 * @param inFilter the filter to remove
	 */
	public void removeFilter(String inFilter) {
		if (eigenschaften != null) {
			for (Benutzereigenschaft be : eigenschaften) {
				if (be.getTitel().equals("_filter") && be.getWert().equals(inFilter)) {
					eigenschaften.remove(be);
					return;
				}
			}
		}
	}

	/**
	 * The function selfDestruct() removes a user from the environment. Since
	 * the user ID may still be referenced somewhere, the user is not hard
	 * deleted from the database, instead the account is set inactive and
	 * invisible.
	 * 
	 * To allow recreation of an account with the same login the login is
	 * cleaned - otherwise it would be blocked eternally by the login existence
	 * test performed in the BenutzerverwaltungForm.Speichern() function. In
	 * addition, all personally identifiable information is removed from the
	 * database as well.
	 */
	public Benutzer selfDestruct() {
		this.isVisible = "deleted";
		this.login = null;
		this.istAktiv = false;
		this.vorname = null;
		this.nachname = null;
		this.standort = null;
		this.benutzergruppen = new HashSet<Benutzergruppe>();
		this.projekte = new HashSet<Projekt>();
		return this;
	}
}
