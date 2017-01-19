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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import org.kitodo.data.database.beans.LdapGruppe;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LdapGruppenDAO;

public class LdapGruppenForm extends BasisForm {
	private static final long serialVersionUID = -5644561256582235244L;
	private LdapGruppe myLdapGruppe = new LdapGruppe();
	private LdapGruppenDAO dao = new LdapGruppenDAO();

	public String Neu() {
		this.myLdapGruppe = new LdapGruppe();
		return "LdapGruppenBearbeiten";
	}

	public String Speichern() {
		try {
			this.dao.save(this.myLdapGruppe);
			return "LdapGruppenAlle";
		} catch (DAOException e) {
			Helper.setFehlerMeldung("Could not save", e.getMessage());
			return "";
		}
	}

	public String Loeschen() {
		try {
			this.dao.remove(this.myLdapGruppe);
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
			this.page = new Page(crit, 0);
		} catch (HibernateException he) {
			Helper.setFehlerMeldung("Error on reading database", he.getMessage());
			return "";
		}
		return "LdapGruppenAlle";
	}

	public String FilterKeinMitZurueck() {
		FilterKein();
		return this.zurueck;
	}

	/*
	 * Getter und Setter     
	 */

	public LdapGruppe getMyLdapGruppe() {
		return this.myLdapGruppe;
	}

	public void setMyLdapGruppe(LdapGruppe myLdapGruppe) {
		this.myLdapGruppe = myLdapGruppe;
	}

}
