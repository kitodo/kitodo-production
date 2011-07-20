package de.sub.goobi.Forms;

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

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Benutzergruppe;
import de.sub.goobi.Beans.LdapGruppe;
import de.sub.goobi.Beans.Projekt;
import de.sub.goobi.Persistence.BenutzerDAO;
import de.sub.goobi.Persistence.BenutzergruppenDAO;
import de.sub.goobi.Persistence.LdapGruppenDAO;
import de.sub.goobi.Persistence.ProjektDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.ldap.Ldap;

public class BenutzerverwaltungForm extends BasisForm {
	private static final long serialVersionUID = -3635859455444639614L;
	private Benutzer myClass = new Benutzer();
	private BenutzerDAO dao = new BenutzerDAO();
	private boolean hideInactiveUsers = false;
	private static final Logger logger = Logger.getLogger(BenutzerverwaltungForm.class);
	
	public String Neu() {
		myClass = new Benutzer();
		myClass.setVorname("- Vorname -");
		myClass.setNachname("- Nachname -");
		myClass.setLogin("- Login -");
		myClass.setPasswortCrypt("Passwort");
		return "BenutzerBearbeiten";
	}

	public String FilterKein() {
		filter = null;
		try {
			//	HibernateUtil.clearSession();
			Session session = Helper.getHibernateSession();
			//	session.flush();
				session.clear();
			Criteria crit = session.createCriteria(Benutzer.class);
			crit.add(Restrictions.isNull("isVisible"));
			if (hideInactiveUsers)
				crit.add(Restrictions.eq("istAktiv", true));
			crit.addOrder(Order.asc("nachname"));
			crit.addOrder(Order.asc("vorname"));
			page = new Page(crit, 0);
		} catch (HibernateException he) {
			new Helper().setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
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
			//	HibernateUtil.clearSession();
			Session session = Helper.getHibernateSession();
			//	session.flush();
				session.clear();
			Criteria crit = session.createCriteria(Benutzer.class);
			crit.add(Restrictions.isNull("isVisible"));
			if (hideInactiveUsers)
				crit.add(Restrictions.eq("istAktiv", true));

			if (filter != null || filter.length() != 0) {
				Disjunction ex = Expression.disjunction();
				ex.add(Expression.like("vorname", "%" + filter + "%"));
				ex.add(Expression.like("nachname", "%" + filter + "%"));
				crit.add(ex);
			}
			crit.addOrder(Order.asc("nachname"));
			crit.addOrder(Order.asc("vorname"));
			page = new Page(crit, 0);
			//calcHomeImages();
		} catch (HibernateException he) {
			new Helper().setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
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
				new Helper().setFehlerMeldung("", "loginBereitsVergeben");
				return "";
			}
		} catch (DAOException e) {
			new Helper().setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
			e.printStackTrace();
			return "";
		}
	}

	private boolean LoginValide(String inLogin) {
		boolean valide = true;
		String patternStr = "[A-Za-z0-9@_\\-]*";
		Pattern pattern = Pattern.compile(patternStr);
		Matcher matcher = pattern.matcher(inLogin);
		valide = matcher.matches();
		if (!valide)
			new Helper().setFehlerMeldung("", "Login enth�lt ungültige Zeichen");

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
					new Helper().setFehlerMeldung("", "Login " + str + " entspricht ungültiger Zeichenfolge");
				}
			in.close();
		} catch (IOException e) {
		}
		return valide;
	}

	public String Loeschen() {
		myClass.setBenutzergruppen(new HashSet());
		myClass.setProjekte(new HashSet());
		myClass.setIstAktiv(false);
		myClass.setIsVisible("deleted");
		return "BenutzerAlle";
	}

	public String AusGruppeLoeschen() {
		int gruppenID = Integer.parseInt(new Helper().getRequestParameter("ID"));

		//TODO: Use generics.
		Set neu = new HashSet();
		//TODO: Don't use Iterators
		for (Iterator iter = myClass.getBenutzergruppen().iterator(); iter.hasNext();) {
			Benutzergruppe element = (Benutzergruppe) iter.next();
			if (element.getId().intValue() != gruppenID)
				neu.add(element);
		}
		myClass.setBenutzergruppen(neu);
		return "";
	}

	public String ZuGruppeHinzufuegen() {
		Integer gruppenID = Integer.valueOf(new Helper().getRequestParameter("ID"));
		try {
			myClass.getBenutzergruppen().add(new BenutzergruppenDAO().get(gruppenID));
		} catch (DAOException e) {
			new Helper().setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
			return null;
		}
		return "";
	}

	public String AusProjektLoeschen() {
		int projektID = Integer.parseInt(new Helper().getRequestParameter("ID"));
		//TODO: Use generics.
		Set neu = new HashSet();
		//TODO: Don't use Iterators
		for (Iterator iter = myClass.getProjekte().iterator(); iter.hasNext();) {
			Projekt element = (Projekt) iter.next();
			if (element.getId().intValue() != projektID)
				neu.add(element);
		}
		myClass.setProjekte(neu);
		return "";
	}

	public String ZuProjektHinzufuegen() {
		Integer projektID = Integer.valueOf(new Helper().getRequestParameter("ID"));
		try {
			myClass.getProjekte().add(new ProjektDAO().get(projektID));
		} catch (DAOException e) {
			new Helper().setFehlerMeldung("fehlerBeimEinlesen", e.getMessage());
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
				new Helper().setFehlerMeldung("Ldapgruppe kann nicht zugewiesen werden", "");
				e.printStackTrace();
			}
	}

	public List<SelectItem> getLdapGruppeAuswahlListe() throws DAOException {
		List<SelectItem> myLdapGruppen = new ArrayList();
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
			new Helper().setMeldung(null, "Ldap-Konfiguration erfolgreich geschrieben für: " + myClass.getNachVorname(), "");
		} catch (Exception e) {
			logger.warn("Ldapeintrag konnte nicht erzeugt werden: " + e.getMessage());
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