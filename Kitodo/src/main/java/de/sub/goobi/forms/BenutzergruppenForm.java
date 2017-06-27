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

import java.util.ArrayList;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;

@Named("BenutzergruppenForm")
@SessionScoped
public class BenutzergruppenForm extends BasisForm {
    private static final long serialVersionUID = 8051160917458068675L;
    private UserGroup myBenutzergruppe = new UserGroup();
    private transient ServiceManager serviceManager = new ServiceManager();
    private int userGroupId;

    public String Neu() {
        this.myBenutzergruppe = new UserGroup();
        this.userGroupId = 0;
        return "/newpages/BenutzergruppenBearbeiten?faces-redirect=true";
    }

    /**
     * Save user group.
     *
     * @return page or empty String
     */
    public String Speichern() {
        try {
            this.serviceManager.getUserGroupService().save(this.myBenutzergruppe);
            return "/newpages/BenutzergruppenAlle?faces-redirect=true";
        } catch (DataException e) {
            Helper.setFehlerMeldung("Error, could not save", e.getMessage());
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
            this.serviceManager.getUserGroupService().refresh(this.myBenutzergruppe);
            if (this.myBenutzergruppe.getUsers().size() > 0) {
                for (User b : this.myBenutzergruppe.getUsers()) {
                    b.getUserGroups().remove(this.myBenutzergruppe);
                }
                this.myBenutzergruppe.setUsers(new ArrayList<>());
                this.serviceManager.getUserGroupService().save(this.myBenutzergruppe);
            }
            if (this.myBenutzergruppe.getTasks().size() > 0) {
                Helper.setFehlerMeldung("userGroupAssignedError");
                return null;
            }
            this.serviceManager.getUserGroupService().remove(this.myBenutzergruppe);
        } catch (DataException e) {
            Helper.setFehlerMeldung("Error, could not delete", e.getMessage());
            return null;
        }
        return "/newpages/BenutzergruppenAlle?faces-redirect=true";
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

    /**
     * This method initializes the user group list without applying any filters
     * whenever the bean is constructed.
     */
    @PostConstruct
    public void initializeUserGroupList() {
        filterKein();
    }

    public String FilterKeinMitZurueck() {
        filterKein();
        return this.zurueck;
    }

    /**
     * Method being used as viewAction for user group edit form. If
     * 'userGroupId' is '0', the form for creating a new user group will be
     * displayed.
     */
    public void loadUserGroup() {
        try {
            if (!Objects.equals(this.userGroupId, 0)) {
                setMyBenutzergruppe(this.serviceManager.getUserGroupService().find(this.userGroupId));
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error retrieving project with ID '" + this.userGroupId + "'; ", e.getMessage());
        }
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

    public void setUserGroupId(int id) {
        this.userGroupId = id;
    }

    public int getUserGroupId() {
        return this.userGroupId;
    }

}
