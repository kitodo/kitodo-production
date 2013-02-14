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

import de.sub.goobi.beans.LdapGruppe;
import de.sub.goobi.persistence.LdapGruppenDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;

public class LdapGruppenForm extends BasisForm {
	private static final long serialVersionUID = -5644561256582235244L;
	private LdapGruppe myLdapGruppe = new LdapGruppe();
	private LdapGruppenDAO dao = new LdapGruppenDAO();

	public String Neu() {
		myLdapGruppe = new LdapGruppe();
		return "LdapGruppenBearbeiten";
	}

	public String Speichern() {
		try {
			dao.save(myLdapGruppe);
			return "LdapGruppenAlle";
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Could not save", e.getMessage());
			return "";
		}
	}

	public String Loeschen() {
		try {
			dao.remove(myLdapGruppe);
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Could not delete from database", e.getMessage());
			return "";
		}
		return "LdapGruppenAlle";
	}

	public String FilterKein() {
		try {
			Session session = Helper.getHibernateSession();
			session.clear();
			Criteria crit = session.createCriteria(LdapGruppe.class);
			crit.addOrder(Order.asc("titel"));
			page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("Error on reading database", he.getMessage());
			return "";
		}
		return "LdapGruppenAlle";
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

	public LdapGruppe getMyLdapGruppe() {
		return myLdapGruppe;
	}

	public void setMyLdapGruppe(LdapGruppe myLdapGruppe) {
		this.myLdapGruppe = myLdapGruppe;
	}

}
