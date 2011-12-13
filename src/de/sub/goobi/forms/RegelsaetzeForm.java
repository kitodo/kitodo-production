package de.sub.goobi.forms;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import de.sub.goobi.beans.Regelsatz;
import de.sub.goobi.Persistence.RegelsatzDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;

public class RegelsaetzeForm extends BasisForm {
	private static final long serialVersionUID = -445707928042517243L;
	private Regelsatz myRegelsatz = new Regelsatz();
	private RegelsatzDAO dao = new RegelsatzDAO();
	private static final Logger logger = Logger.getLogger(RegelsaetzeForm.class);

	public String Neu() {
		myRegelsatz = new Regelsatz();
		return "RegelsaetzeBearbeiten";
	}

	public String Speichern() {
		try {
			dao.save(myRegelsatz);
			return "RegelsaetzeAlle";
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtSpeicherbar", e.getMessage());
			logger.error(e);
			return "";
		}
	}

	public String Loeschen() {
		try {
			dao.remove(myRegelsatz);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("fehlerNichtLoeschbar", e.getMessage());
			return "";
		}
		return "RegelsaetzeAlle";
	}

	public String FilterKein() {
		try {
			//	  HibernateUtil.clearSession();
			Session session = Helper.getHibernateSession();
			//	session.flush();
				session.clear();
			Criteria crit = session.createCriteria(Regelsatz.class);
			crit.addOrder(Order.asc("titel"));
			page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("fehlerBeimEinlesen", he.getMessage());
			return "";
		}
		return "RegelsaetzeAlle";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return zurueck;
	}

	/*#####################################################
	 #####################################################
	 ##                                                                                              
	 ##                                                Getter und Setter                         
	 ##                                                                                                    
	 #####################################################
	 ####################################################*/

	public Regelsatz getMyRegelsatz() {
		return myRegelsatz;
	}

	public void setMyRegelsatz(Regelsatz inPreference) {
		Helper.getHibernateSession().clear();
		this.myRegelsatz = inPreference;
	}
}
