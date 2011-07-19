package de.sub.goobi.Forms;

import java.util.HashSet;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import de.sub.goobi.Beans.Benutzer;
import de.sub.goobi.Beans.Benutzergruppe;
import de.sub.goobi.Beans.Schritt;
import de.sub.goobi.Persistence.BenutzergruppenDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;

public class BenutzergruppenForm extends BasisForm {
	private static final long serialVersionUID = 8051160917458068675L;
	private Benutzergruppe myBenutzergruppe = new Benutzergruppe();
	private BenutzergruppenDAO dao = new BenutzergruppenDAO();

	public String Neu() {
		myBenutzergruppe = new Benutzergruppe();
		return "BenutzergruppenBearbeiten";
	}

	public String Speichern() {
		try {
			dao.save(myBenutzergruppe);
			return "BenutzergruppenAlle";
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error, could not save", e.getMessage());
			return "";
		}
	}

	public String Loeschen() {
		try {
			if (myBenutzergruppe.getBenutzerList().size() > 0) {
				myBenutzergruppe.setBenutzer(new HashSet<Benutzer>());
				dao.save(myBenutzergruppe);
			}
			if (myBenutzergruppe.getSchritteList().size() > 0) {
				myBenutzergruppe.setSchritte(new HashSet<Schritt>());
				dao.save(myBenutzergruppe);
			}
			dao.remove(myBenutzergruppe);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error, could not delete", e.getMessage());
			return "";
		}
		return "BenutzergruppenAlle";
	}

	public String FilterKein() {
		try {
			// HibernateUtil.clearSession();
			Session session = Helper.getHibernateSession();
			// session.flush();
			session.clear();
			Criteria crit = session.createCriteria(Benutzergruppe.class);
			crit.addOrder(Order.asc("titel"));
			page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("Error, could not read", he.getMessage());
			return "";
		}
		return "BenutzergruppenAlle";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return zurueck;
	}

	/*
	 * #####################################################
	 * ##################################################### ## ## Getter und
	 * Setter ## #####################################################
	 * ####################################################
	 */

	public Benutzergruppe getMyBenutzergruppe() {
		return myBenutzergruppe;
	}

	public void setMyBenutzergruppe(Benutzergruppe myBenutzergruppe) {
		Helper.getHibernateSession().clear();
		this.myBenutzergruppe = myBenutzergruppe;
	}

}
