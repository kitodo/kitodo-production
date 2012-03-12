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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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

import dubious.sub.goobi.helper.Page;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.beans.LdapGruppe;
import de.sub.goobi.beans.Projekt;
import de.sub.goobi.persistence.BenutzerDAO;
import de.sub.goobi.persistence.BenutzergruppenDAO;
import de.sub.goobi.persistence.LdapGruppenDAO;
import de.sub.goobi.persistence.ProjektDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.Ldap;

public class BenutzerverwaltungForm extends BasisForm {
	private static final long serialVersionUID = -3635859455444639614L;
	private Benutzer myClass = new Benutzer();
	private BenutzerDAO dao = new BenutzerDAO();
	private boolean hideInactiveUsers = true;
	private static final Logger logger = Logger.getLogger(BenutzerverwaltungForm.class);
	
	public String Neu() {
		myClass = new Benutzer();
		myClass.setVorname("");
		myClass.setNachname("");
		myClass.setLogin("");
		myClass.setPasswortCrypt("Passwort");
		return "BenutzerBearbeiten";
	}

	public String FilterKein() {
		filter = null;
		try {
			Session session = Helper.getHibernateSession();
			session.clear();
			Criteria crit = session.createCriteria(Benutzer.class);
			crit.add(Restrictions.isNull("isVisible"));
			if (hideInactiveUsers)
				crit.add(Restrictions.eq("istAktiv", true));
			crit.addOrder(Order.asc("nachname"));
			crit.addOrder(Order.asc("vorname"));
			page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("Error, could not read", he.getMessage());
			return "";
		}
		return "BenutzerAlle";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return zurueck;
	}

	/**
	 * Anzeige der gefilterten Nutzer
	 */
	public String FilterAlleStart() {
		try {
			Session session = Helper.getHibernateSession();
			session.clear();
			Criteria crit = session.createCriteria(Benutzer.class);
			crit.add(Restrictions.isNull("isVisible"));
			if (hideInactiveUsers)
				crit.add(Restrictions.eq("istAktiv", true));

			if (filter != null || filter.length() != 0) {
				Disjunction ex = Restrictions.disjunction();
				ex.add(Restrictions.like("vorname", "%" + filter + "%"));
				ex.add(Restrictions.like("nachname", "%" + filter + "%"));
				crit.add(ex);
			}
			crit.addOrder(Order.asc("nachname"));
			crit.addOrder(Order.asc("vorname"));
			page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("Error, could not read", he.getMessage());
			return "";
		}
		return "BenutzerAlle";
	}

	public String Speichern() {
		Session session = Helper.getHibernateSession();
		session.evict(myClass);
		String bla = myClass.getLogin();

		if (!LoginValide(bla))
			return "";

		Integer blub = myClass.getId();
		try {
			/* prüfen, ob schon ein anderer Benutzer mit gleichem Login existiert */
			if (dao.count("from Benutzer where login='" + bla + "'AND BenutzerID<>" + blub) == 0) {
				dao.save(myClass);
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
		if (!valide)
			Helper.setFehlerMeldung("", Helper.getTranslation("loginNotValid"));

		/* Pfad zur Datei ermitteln */
		FacesContext context = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) context.getExternalContext().getSession(false);
		String filename = session.getServletContext().getRealPath("/WEB-INF") + File.separator + "classes" + File.separator + "loginblacklist.txt";
		/* Datei zeilenweise durchlaufen und die auf ungültige Zeichen vergleichen */
		try {
			FileInputStream fis = new FileInputStream(filename);
			InputStreamReader isr = new InputStreamReader(fis, "UTF8");
			BufferedReader in = new BufferedReader(isr);
			String str;
			while ((str = in.readLine()) != null)
				if (str.length() > 0 && inLogin.equalsIgnoreCase(str)) {
					valide = false;
					Helper.setFehlerMeldung("", "Login " + str + Helper.getTranslation("loginNotValid"));
				}
			in.close();
		} catch (IOException e) {
		}
		return valide;
	}

	public String Loeschen() {
		myClass.setBenutzergruppen(new HashSet<Benutzergruppe>());
		myClass.setProjekte(new HashSet<Projekt>());
		myClass.setIstAktiv(false);
		myClass.setIsVisible("deleted");
		return "BenutzerAlle";
	}

	public String AusGruppeLoeschen() {
		int gruppenID = Integer.parseInt(Helper.getRequestParameter("ID"));

		//TODO: Use generics.
		Set<Benutzergruppe> neu = new HashSet<Benutzergruppe>();
		//TODO: Don't use Iterators
		for (Iterator<Benutzergruppe> iter = myClass.getBenutzergruppen().iterator(); iter.hasNext();) {
			Benutzergruppe element = (Benutzergruppe) iter.next();
			if (element.getId().intValue() != gruppenID)
				neu.add(element);
		}
		myClass.setBenutzergruppen(neu);
		return "";
	}

	public String ZuGruppeHinzufuegen() {
		Integer gruppenID = Integer.valueOf(Helper.getRequestParameter("ID"));
		try {
			myClass.getBenutzergruppen().add(new BenutzergruppenDAO().get(gruppenID));
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error on reading database", e.getMessage());
			return null;
		}
		return "";
	}

	public String AusProjektLoeschen() {
		int projektID = Integer.parseInt(Helper.getRequestParameter("ID"));
		//TODO: Use generics.
		Set<Projekt> neu = new HashSet<Projekt>();
		//TODO: Don't use Iterators
		for (Iterator<Projekt> iter = myClass.getProjekte().iterator(); iter.hasNext();) {
			Projekt element = (Projekt) iter.next();
			if (element.getId().intValue() != projektID)
				neu.add(element);
		}
		myClass.setProjekte(neu);
		return "";
	}

	public String ZuProjektHinzufuegen() {
		Integer projektID = Integer.valueOf(Helper.getRequestParameter("ID"));
		try {
			myClass.getProjekte().add(new ProjektDAO().get(projektID));
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error on reading database", e.getMessage());
			return null;
		}
		return "";
	}

	/*#####################################################
	 #####################################################
	 ##                                                                                              
	 ##                                                Getter und Setter                         
	 ##                                                                                                    
	 #####################################################
	 ####################################################*/

	public Benutzer getMyClass() {
		return myClass;
	}

	public void setMyClass(Benutzer inMyClass) {
		Helper.getHibernateSession().flush();
		Helper.getHibernateSession().clear();
		try {
			myClass = new BenutzerDAO().get(inMyClass.getId());
		} catch (DAOException e) {
			this.myClass = inMyClass;
		}
	}

	/*#####################################################
	 #####################################################
	 ##																															 
	 ##												Ldap-Konfiguration									
	 ##                                                   															    
	 #####################################################
	 ####################################################*/

	public Integer getLdapGruppeAuswahl() {
		if (myClass.getLdapGruppe() != null)
			return myClass.getLdapGruppe().getId();
		else
			return Integer.valueOf(0);
	}

	public void setLdapGruppeAuswahl(Integer inAuswahl) {
		if (inAuswahl.intValue() != 0)
			try {
				myClass.setLdapGruppe(new LdapGruppenDAO().get(inAuswahl));
			} catch (DAOException e) {
				Helper.setFehlerMeldung("Error on writing to database", "");
				logger.error(e);
			}
	}

	public List<SelectItem> getLdapGruppeAuswahlListe() throws DAOException {
		List<SelectItem> myLdapGruppen = new ArrayList<SelectItem>();
		List<LdapGruppe> temp = new LdapGruppenDAO().search("from LdapGruppe ORDER BY titel");
		for (LdapGruppe gru : temp) {
			myLdapGruppen.add(new SelectItem(gru.getId(), gru.getTitel(), null));
		}
		return myLdapGruppen;
	}

	/**
	 * Ldap-Konfiguration für den Benutzer schreiben
	 * 
	 * @return
	 */
	public String LdapKonfigurationSchreiben() {
		Ldap myLdap = new Ldap();
		try {
			myLdap.createNewUser(myClass, myClass.getPasswortCrypt());
			Helper.setMeldung(null, Helper.getTranslation("ldapWritten") + myClass.getNachVorname(), "");
		} catch (Exception e) {
			logger.warn("Could not generate ldap entry: " + e.getMessage());
		}
		return "";
	}

	public boolean isHideInactiveUsers() {
		return hideInactiveUsers;
	}

	public void setHideInactiveUsers(boolean hideInactiveUsers) {
		this.hideInactiveUsers = hideInactiveUsers;
	}

}
