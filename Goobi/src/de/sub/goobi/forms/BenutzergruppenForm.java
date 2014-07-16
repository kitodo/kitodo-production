package de.sub.goobi.forms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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
import java.util.HashSet;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import de.sub.goobi.beans.Benutzer;
import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.persistence.BenutzergruppenDAO;
import de.sub.goobi.persistence.SimpleDAO;

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
