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

import java.util.HashSet;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import org.kitodo.data.database.beans.Benutzer;
import org.kitodo.data.database.beans.Benutzergruppe;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.BenutzergruppenDAO;
import org.kitodo.data.database.persistence.SimpleDAO;

public class BenutzergruppenForm extends BasisForm {
	private static final long serialVersionUID = 8051160917458068675L;
	private Benutzergruppe myBenutzergruppe = new Benutzergruppe();
	private BenutzergruppenDAO dao = new BenutzergruppenDAO();

	public String Neu() {
		this.myBenutzergruppe = new Benutzergruppe();
		return "BenutzergruppenBearbeiten";
	}

	public String Speichern() {
		try {
			this.dao.save(this.myBenutzergruppe);
			return "BenutzergruppenAlle";
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error, could not save", e.getMessage());
			return "";
		}
	}

	public String Loeschen() {
		try {
			new SimpleDAO().refreshObject(this.myBenutzergruppe);
			if (this.myBenutzergruppe.getBenutzer().size() > 0) {
				for (Benutzer b : this.myBenutzergruppe.getBenutzer()) {
					b.getBenutzergruppen().remove(this.myBenutzergruppe);
				}
				this.myBenutzergruppe.setBenutzer(new HashSet<Benutzer>());
				this.dao.save(this.myBenutzergruppe);
			}
			if (this.myBenutzergruppe.getSchritte().size() > 0) {
				Helper.setFehlerMeldung("userGroupAssignedError");
				return "";
			}
			this.dao.remove(this.myBenutzergruppe);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Error, could not delete", e.getMessage());
			return "";
		}
		return "BenutzergruppenAlle";
	}

	public String FilterKein() {
		try {
			Session session = Helper.getHibernateSession();
			session.clear();
			Criteria crit = session.createCriteria(Benutzergruppe.class);
			crit.addOrder(Order.asc("titel"));
			this.page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("Error, could not read", he.getMessage());
			return "";
		}
		return "BenutzergruppenAlle";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return this.zurueck;
	}

	/*
 	 * Getter und Setter 
	 */

	public Benutzergruppe getMyBenutzergruppe() {
		return this.myBenutzergruppe;
	}

	public void setMyBenutzergruppe(Benutzergruppe myBenutzergruppe) {
		Helper.getHibernateSession().clear();
		this.myBenutzergruppe = myBenutzergruppe;
	}

}
