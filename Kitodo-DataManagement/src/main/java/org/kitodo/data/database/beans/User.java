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

package org.kitodo.data.database.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

import org.kitodo.data.encryption.DesEncrypter;

@Entity
@Table(name = "user")
public class User implements Serializable {
	private static final long serialVersionUID = -7482853955996650586L;

	@Id
	@Column(name = "id")
	@GeneratedValue
	private Integer id;

	@Column(name = "name")
	private String name;

	@Column(name = "surname")
	private String surname;

	@Column(name = "login")
	private String login;

	@Column(name = "ldapLogin")
	private String ldapLogin;

	@Column(name = "password")
	private String password;

	@Column(name = "isActive")
	private boolean isActive = true;

	@Column(name = "isVisible")
	private String isVisible;

	@Column(name = "location")
	private String location;

	@Column(name = "tableSize")
	private Integer tableSize = Integer.valueOf(10);

	@Column(name = "sessionTimeout")
	private Integer sessionTimeout = 7200;

	@Column(name = "configProductionDateShow")
	private boolean configProductionDateShow = false;

	@Column(name = "metadataLanguage")
	private String metadataLanguage;

	@Column(name = "withMassDownload")
	private boolean withMassDownload = false;

	@Column(name = "css")
	private String css;

	@ManyToOne
	@JoinColumn(name = "ldapGroup_id", foreignKey = @ForeignKey(name = "FK_user_ldapGroup_id"))
	private LdapGruppe ldapGroup;

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
	private List<Benutzergruppe> userGroups;

	@ManyToMany(mappedBy = "benutzer")
	private List<Schritt> steps;

	@OneToMany(mappedBy = "bearbeitungsbenutzer", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Schritt> processingSteps;

	@ManyToMany(mappedBy = "benutzer")
	private List<Projekt> projects;

	@OneToMany(mappedBy = "benutzer", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("title ASC")
	private List<Benutzereigenschaft> properties;

	/**
	 * Constructor for User Entity.
	 */
	public User() {
		this.userGroups = new ArrayList<>();
		this.projects = new ArrayList<>();
		this.steps = new ArrayList<>();
		this.properties = new ArrayList<>();
	}

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

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return this.surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(String inputPassword) {
		this.password = inputPassword;
	}

	public String getPasswordDecrypted() {
		DesEncrypter encrypter = new DesEncrypter();
		return encrypter.decrypt(this.password);
	}

	public void setPasswordDecrypted(String inputPassword) {
		DesEncrypter encrypter = new DesEncrypter();
		this.password = encrypter.encrypt(inputPassword);
	}

	public boolean isActive() {
		return this.isActive;
	}

	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String isVisible() {
		return this.isVisible;
	}

	public void setIsVisible(String isVisible) {
		this.isVisible = isVisible;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getTableSize() {
		return this.tableSize;
	}

	public void setTableSize(Integer tableSize) {
		this.tableSize = tableSize;
	}

	public boolean isWithMassDownload() {
		return this.withMassDownload;
	}

	public void setWithMassDownload(boolean withMassDownload) {
		this.withMassDownload = withMassDownload;
	}

	public LdapGruppe getLdapGroup() {
		return this.ldapGroup;
	}

	public void setLdapGroup(LdapGruppe ldapGroup) {
		this.ldapGroup = ldapGroup;
	}

	public List<Benutzergruppe> getUserGroups() {
		return this.userGroups;
	}

	public void setUserGroups(List<Benutzergruppe> userGroups) {
		this.userGroups = userGroups;
	}

	public List<Schritt> getSteps() {
		return this.steps;
	}

	public void setSteps(List<Schritt> steps) {
		this.steps = steps;
	}

	public List<Schritt> getProcessingSteps() {
		return this.processingSteps;
	}

	public void setProcessingSteps(List<Schritt> processingSteps) {
		this.processingSteps = processingSteps;
	}

	public List<Projekt> getProjects() {
		return this.projects;
	}

	public void setProjects(List<Projekt> projects) {
		this.projects = projects;
	}

	public boolean isConfigProductionDateShow() {
		return this.configProductionDateShow;
	}

	public void setConfigProductionDateShow(boolean configProductionDateShow) {
		this.configProductionDateShow = configProductionDateShow;
	}

	public String getMetadataLanguage() {
		return this.metadataLanguage;
	}

	public void setMetadataLanguage(String metadataLanguage) {
		this.metadataLanguage = metadataLanguage;
	}

	public String getLdapLogin() {
		return this.ldapLogin;
	}

	public void setLdapLogin(String ldapLogin) {
		this.ldapLogin = ldapLogin;
	}

	public Integer getSessionTimeout() {
		return this.sessionTimeout;
	}

	public void setSessionTimeout(Integer sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}

	public String getCss() {
		return this.css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public List<Benutzereigenschaft> getProperties() {
		return this.properties;
	}

	public void setProperties(List<Benutzereigenschaft> properties) {
		this.properties = properties;
	}

	/**
	 * The function selfDestruct() removes a user from the environment. Since the user ID may still be referenced
	 * somewhere, the user is not hard deleted from the database, instead the account is set inactive and invisible.
	 *
	 * <p>To allow recreation of an account with the same login the login is cleaned - otherwise it would be blocked
	 * eternally by the login existence test performed in the BenutzerverwaltungForm.Speichern() function.
	 * In addition,  all personally identifiable information is removed from the database as well.</p>
	 */

	public User selfDestruct() {
		this.isVisible = "deleted";
		this.login = null;
		this.isActive = false;
		this.name = null;
		this.surname = null;
		this.location = null;
		this.userGroups = new ArrayList<>();
		this.projects = new ArrayList<>();
		return this;
	}
}
