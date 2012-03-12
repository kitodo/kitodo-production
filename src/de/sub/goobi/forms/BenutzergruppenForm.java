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

import dubious.sub.goobi.helper.Page;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import de.sub.goobi.beans.Benutzergruppe;
import de.sub.goobi.persistence.BenutzergruppenDAO;
import de.sub.goobi.helper.Helper;
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
			dao.remove(myBenutzergruppe);
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

	/*#####################################################
	 #####################################################
	 ##                                                                                              
	 ##                                                Getter und Setter                         
	 ##                                                                                                    
	 #####################################################
	 ####################################################*/

	public Benutzergruppe getMyBenutzergruppe() {
		return myBenutzergruppe;
	}

	public void setMyBenutzergruppe(Benutzergruppe myBenutzergruppe) {
		Helper.getHibernateSession().clear();
		this.myBenutzergruppe = myBenutzergruppe;
	}

}
