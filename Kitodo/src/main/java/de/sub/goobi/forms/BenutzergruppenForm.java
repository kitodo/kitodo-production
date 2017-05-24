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

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.Page;

import java.io.IOException;
import java.util.ArrayList;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.SimpleDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.services.ServiceManager;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean
@ViewScoped
public class BenutzergruppenForm extends BasisForm {
    private static final long serialVersionUID = 8051160917458068675L;
    private UserGroup myBenutzergruppe = new UserGroup();
    private final ServiceManager serviceManager = new ServiceManager();

    public String Neu() {
        this.myBenutzergruppe = new UserGroup();
        return "/newpages/BenutzergruppenBearbeiten";
    }

    /**
     * Save user group.
     *
     * @return page or empty String
     */
    public String Speichern() {
        try {
            this.serviceManager.getUserGroupService().save(this.myBenutzergruppe);
            return "/newpages/BenutzergruppenAlle";
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error, could not save", e.getMessage());
            return null;
        } catch (IOException e) {
            Helper.setFehlerMeldung("Error, could not insert to index", e.getMessage());
            return null;
        } catch (CustomResponseException e) {
            Helper.setFehlerMeldung("Error, ElasticSearch incorrect server response", e.getMessage());
            return null;
        }
    }

    /**
     * Remove user group.
     *
     * @return page or empty String
     */
    public String Loeschen() {
        try {
            new SimpleDAO().refreshObject(this.myBenutzergruppe);
            if (this.myBenutzergruppe.getUsers().size() > 0) {
                for (User b : this.myBenutzergruppe.getUsers()) {
                    b.getUserGroups().remove(this.myBenutzergruppe);
                }
                this.myBenutzergruppe.setUsers(new ArrayList<User>());
                this.serviceManager.getUserGroupService().save(this.myBenutzergruppe);
            }
            if (this.myBenutzergruppe.getTasks().size() > 0) {
                Helper.setFehlerMeldung("userGroupAssignedError");
                return null;
            }
            this.serviceManager.getUserGroupService().remove(this.myBenutzergruppe);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error, could not delete", e.getMessage());
            return null;
        } catch (IOException e) {
            Helper.setFehlerMeldung("Error, could not delete from index", e.getMessage());
            return null;
        } catch (CustomResponseException e) {
            Helper.setFehlerMeldung("Error, ElasticSearch incorrect server response", e.getMessage());
            return null;
        }
        return "/newpages/BenutzergruppenAlle";
    }

    /**
     * Display all user groups with any filtering.
     *
     * @return page or empty String
     */
    public String filterKein() {
        try {
            Session session = Helper.getHibernateSession();
            session.clear();
            Criteria crit = session.createCriteria(UserGroup.class);
            crit.addOrder(Order.asc("title"));
            this.page = new Page(crit, 0);
        } catch (HibernateException he) {
            Helper.setFehlerMeldung("Error, could not read", he.getMessage());
            return null;
        }
        return "/newpages/BenutzergruppenAlle";
    }

    public String FilterKeinMitZurueck() {
        filterKein();
        return this.zurueck;
    }

    /*
     * Getter und Setter
     */

    public UserGroup getMyBenutzergruppe() {
        return this.myBenutzergruppe;
    }

    public void setMyBenutzergruppe(UserGroup myBenutzergruppe) {
        Helper.getHibernateSession().clear();
        this.myBenutzergruppe = myBenutzergruppe;
    }

}
