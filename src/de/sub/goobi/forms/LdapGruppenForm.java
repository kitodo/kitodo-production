package de.sub.goobi.forms;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import de.sub.goobi.beans.LdapGruppe;
import de.sub.goobi.Persistence.LdapGruppenDAO;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;
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
			//	  HibernateUtil.clearSession();
			Session session = Helper.getHibernateSession();
			//	session.flush();
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
