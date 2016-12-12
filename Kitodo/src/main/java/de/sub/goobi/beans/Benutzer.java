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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;

import org.kitodo.encryption.DesEncrypter;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.ldap.Ldap;
import de.sub.goobi.persistence.apache.UserManager;

@Entity
@Table(name = "user")
public class Benutzer implements Serializable {
	private static final long serialVersionUID = -7482853955996650586L;

	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;

	@Column(name = "name")
	private String vorname;

	@Column(name = "surname")
	private String nachname;

	@Column(name = "login")
	private String login;

	@Column(name = "ldapLogin")
	private String ldaplogin;

	@Column(name = "password")
	private String passwort;

	@Column(name = "isActive")
	private boolean istAktiv = true;

	@Column(name = "isVisible")
	private String isVisible;

	@Column(name = "location")
	private String standort;

	@Column(name = "tableSize")
	private Integer tabellengroesse = Integer.valueOf(10);

	@Column(name = "sessionTimeout")
	private Integer sessiontimeout = 7200;

	@Column(name = "configProductionDateShow")
	private boolean confVorgangsdatumAnzeigen = false;

	@Column(name = "metadataLanguage")
	private String metadatenSprache;

	@Column(name = "withMassDownload")
	private boolean mitMassendownload = false;

	@Column(name = "css")
	private String css;

	@ManyToOne
	@JoinColumn(name = "ldapGroup_id", foreignKey = @ForeignKey(name = "FK_user_ldapGroup_id"))
	private LdapGruppe ldapGruppe;

	@ManyToMany
	@JoinTable(name = "user_x_userGroup",
			joinColumns = {
					@JoinColumn(
							name = "user_id",
							foreignKey = @ForeignKey(name = "FK_user_x_userGroup_user_id")
					) },
			inverseJoinColumns = {
					@JoinColumn(
							name = "userGroup_id",
							foreignKey = @ForeignKey(name = "FK_user_x_userGroup_userGroup_id")
					) })
	private Set<Benutzergruppe> benutzergruppen;

	@ManyToMany(mappedBy = "steps")
	private Set<Schritt> schritte;

	@OneToMany(mappedBy = "processingSteps", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Schritt> bearbeitungsschritte;

	@ManyToMany(mappedBy = "projects")
	private Set<Projekt> projekte;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("title ASC")
	private Set<Benutzereigenschaft> eigenschaften;

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
		return this.tabellengroesse;
	}

	public void setTabellengroesse(Integer tabellengroesse) {
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
			List<Benutzergruppe> answer = new ArrayList<Benutzergruppe>(this.benutzergruppen);
			Collections.sort(answer);
			return answer;

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
			List<Projekt> answer = new ArrayList<Projekt>(this.projekte);
			Collections.sort(answer);
			return answer;

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
        FilesystemHelper.createDirectoryForUser(rueckgabe, login);
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
		return UserManager.getFilters(this.id);
	}

	/**
	 * adds a new filter to list
	 * 
	 * @param inFilter
	 *            the filter to add
	 */

	public void addFilter(String inFilter) {
		UserManager.addFilter(this.id, inFilter);
	}

	/**
	 * removes filter from list
	 * 
	 * @param inFilter
	 *            the filter to remove
	 */
	public void removeFilter(String inFilter) {
		UserManager.removeFilter(this.id, inFilter);
	}

	/**
	 * The function selfDestruct() removes a user from the environment. Since the user ID may still be referenced somewhere, the user is not hard
	 * deleted from the database, instead the account is set inactive and invisible.
	 * 
	 * To allow recreation of an account with the same login the login is cleaned - otherwise it would be blocked eternally by the login existence
	 * test performed in the BenutzerverwaltungForm.Speichern() function. In addition, all personally identifiable information is removed from the
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
