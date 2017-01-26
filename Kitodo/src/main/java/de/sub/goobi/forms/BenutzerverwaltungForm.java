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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.ldap.Ldap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.LdapGroupService;
import org.kitodo.services.ProjectService;
import org.kitodo.services.UserService;
import org.kitodo.services.UserGroupService;

public class BenutzerverwaltungForm extends BasisForm {
	private static final long serialVersionUID = -3635859455444639614L;
	private User myClass = new User();
	private LdapGroupService ldapGroupService = new LdapGroupService();
	private ProjectService projectService = new ProjectService();
	private UserService userService = new UserService();
	private UserGroupService userGroupService = new UserGroupService();
	private boolean hideInactiveUsers = true;
	private static final Logger logger = Logger.getLogger(BenutzerverwaltungForm.class);

	public String Neu() {
		this.myClass = new User();
		this.myClass.setName("");
		this.myClass.setSurname("");
		this.myClass.setLogin("");
		this.myClass.setLdapLogin("");
		this.myClass.setPasswordDecrypted("Passwort");
		return "BenutzerBearbeiten";
	}

	public String FilterKein() {
		this.filter = null;
		try {
			Session session = Helper.getHibernateSession();
			session.clear();
			Criteria crit = session.createCriteria(User.class);
			crit.add(Restrictions.isNull("isVisible"));
			if (this.hideInactiveUsers) {
				crit.add(Restrictions.eq("isActive", true));
			}
			crit.addOrder(Order.asc("surname"));
			crit.addOrder(Order.asc("name"));
			this.page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("Error, could not read", he.getMessage());
			return "";
		}
		return "BenutzerAlle";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return this.zurueck;
	}

	/**
	 * Anzeige der gefilterten Nutzer.
	 */
	public String FilterAlleStart() {
		try {
			Session session = Helper.getHibernateSession();
			session.clear();
			Criteria crit = session.createCriteria(User.class);
			crit.add(Restrictions.isNull("isVisible"));
			if (this.hideInactiveUsers) {
				crit.add(Restrictions.eq("isActive", true));
			}

			if (this.filter != null && this.filter.length() != 0) {
				Disjunction ex = Restrictions.disjunction();
				ex.add(Restrictions.like("name", "%" + this.filter + "%"));
				ex.add(Restrictions.like("surname", "%" + this.filter + "%"));
				//crit.createCriteria("projekte", "proj");
				//ex.add(Restrictions.like("proj.titel", "%" + this.filter + "%"));

				//crit.createCriteria("benutzergruppen", "group");
				//ex.add(Restrictions.like("group.titel", "%" + this.filter + "%"));
				crit.add(ex);
			}
			crit.addOrder(Order.asc("surname"));
			crit.addOrder(Order.asc("name"));
			this.page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("Error, could not read", he.getMessage());
			return "";
		}
		return "BenutzerAlle";
	}

	public String Speichern() {
		Session session = Helper.getHibernateSession();
		session.evict(this.myClass);
		String bla = this.myClass.getLogin();

		if (!LoginValide(bla)) {
			return "";
		}

		Integer blub = this.myClass.getId();
		try {
			/* prüfen, ob schon ein anderer Benutzer mit gleichem Login existiert */
			if (this.userService.count("from User where login='" + bla + "'AND id<>" + blub) == 0) {
				this.userService.save(this.myClass);
				return "BenutzerAlle";
			} else {
				Helper.setFehlerMeldung("", Helper.getTranslation("loginBereitsVergeben"));
				return "";
			}
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error, could not save", e.getMessage());
			logger.error(e);
			return "";
		}
	}

	private boolean LoginValide(String inLogin) {
		boolean valide = true;
		String patternStr = "[A-Za-z0-9@_\\-.]*";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(inLogin);
		valide = matcher.matches();
		if (!valide) {
			Helper.setFehlerMeldung("", Helper.getTranslation("loginNotValid"));
		}

		/* Pfad zur Datei ermitteln */
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		String filename = session.getServletContext().getRealPath("/WEB-INF") + File.separator + "classes" + File.separator
				+ "goobi_loginBlacklist.txt";
		/* Datei zeilenweise durchlaufen und die auf ungültige Zeichen vergleichen */
		try (
			FileInputStream fis = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
			BufferedReader in = new BufferedReader(isr);
		) {
			String str;
			while ((str = in.readLine()) != null) {
				if (str.length() > 0 && inLogin.equalsIgnoreCase(str)) {
					valide = false;
					Helper.setFehlerMeldung("", "Login " + str
							+ Helper.getTranslation("loginNotValid"));
				}
			}
		} catch (IOException e) {
		}
		return valide;
	}

	/**
	 * The function Loeschen() deletes a user account.
	 *
	 * Please note that deleting a user in goobi.production will not delete the user from a connected LDAP service.
	 *
	 * @return a string indicating the screen showing up after the command has been performed.
	 */
	public String Loeschen() {
		try {
			userService.remove(myClass);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error, could not save", e.getMessage());
			logger.error(e);
			return "";
		}
		return "BenutzerAlle";
	}

	public String AusGruppeLoeschen() {
		int gruppenID = Integer.parseInt(Helper.getRequestParameter("id"));

		List<UserGroup> neu = new ArrayList<>();
		for (Iterator<UserGroup> iter = this.myClass.getUserGroups().iterator(); iter.hasNext();) {
			UserGroup element = iter.next();
			if (element.getId() != gruppenID) {
				neu.add(element);
			}
		}
		this.myClass.setUserGroups(neu);
		return "";
	}

	public String ZuGruppeHinzufuegen() {
		Integer gruppenID = Integer.valueOf(Helper.getRequestParameter("id"));
		try {
			UserGroup usergroup = userGroupService.find(gruppenID);
			for (UserGroup b : this.myClass.getUserGroups()) {
				if (b.equals(usergroup)) {
					return "";
				}
			}
			this.myClass.getUserGroups().add(usergroup);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error on reading database", e.getMessage());
			return null;
		}
		return "";
	}

	public String AusProjektLoeschen() {
		int projektID = Integer.parseInt(Helper.getRequestParameter("id"));
		List<Project> neu = new ArrayList<>();
		for (Iterator<Project> iter = this.myClass.getProjects().iterator(); iter.hasNext();) {
			Project element = iter.next();
			if (element.getId() != projektID) {
				neu.add(element);
			}
		}
		this.myClass.setProjects(neu);
		return "";
	}

	public String ZuProjektHinzufuegen() {
		Integer projektID = Integer.valueOf(Helper.getRequestParameter("id"));
		try {
			Project project = projectService.find(projektID);
			for (Project p : this.myClass.getProjects()) {
				if (p.equals(project)) {
					return "";
				}
			}
			this.myClass.getProjects().add(project);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error on reading database", e.getMessage());
			return null;
		}
		return "";
	}

	/*
	 * Getter und Setter
	 */

	public User getMyClass() {
		return this.myClass;
	}

	public void setMyClass(User inMyClass) {
		Helper.getHibernateSession().flush();
		Helper.getHibernateSession().clear();
		try {
			this.myClass = userService.find(inMyClass.getId());
		} catch (DAOException e) {
			this.myClass = inMyClass;
		}
	}

	/*
	 * Ldap-Konfiguration
	 */

	public Integer getLdapGruppeAuswahl() {
		if (this.myClass.getLdapGroup() != null) {
			return this.myClass.getLdapGroup().getId();
		} else {
			return Integer.valueOf(0);
		}
	}

	public void setLdapGruppeAuswahl(Integer inAuswahl) {
		if (inAuswahl.intValue() != 0) {
			try {
				this.myClass.setLdapGroup(ldapGroupService.find(inAuswahl));
			} catch (DAOException e) {
				Helper.setFehlerMeldung("Error on writing to database", "");
				logger.error(e);
			}
		}
	}

	public List<SelectItem> getLdapGruppeAuswahlListe() throws DAOException {
		List<SelectItem> myLdapGruppen = new ArrayList<SelectItem>();
		List<LdapGroup> temp = ldapGroupService.search("from LdapGroup ORDER BY title");
		for (LdapGroup gru : temp) {
			myLdapGruppen.add(new SelectItem(gru.getId(), gru.getTitle(), null));
		}
		return myLdapGruppen;
	}

	/**
	 * Ldap-Konfiguration für den Benutzer schreiben
	 */
	public String LdapKonfigurationSchreiben() {
		Ldap myLdap = new Ldap();
		try {
			myLdap.createNewUser(this.myClass, this.myClass.getPasswordDecrypted());
		} catch (Exception e) {
			if (logger.isEnabledFor(Level.WARN)) {
				logger.warn("Could not generate ldap entry: " + e.getMessage());
			}
			Helper.setFehlerMeldung(e.getMessage());
		}
		return "";
	}

	public boolean isHideInactiveUsers() {
		return this.hideInactiveUsers;
	}

	public void setHideInactiveUsers(boolean hideInactiveUsers) {
		this.hideInactiveUsers = hideInactiveUsers;
	}

	public boolean getLdapUsage() {
		return ConfigMain.getBooleanParameter("ldap_use");
	}

}
