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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.Objects;

@ManagedBean
@ViewScoped
public class LdapGruppenForm extends BasisForm {
    private static final long serialVersionUID = -5644561256582235244L;
    private LdapGroup myLdapGruppe = new LdapGroup();
    private int itemId;
    private final ServiceManager serviceManager = new ServiceManager();

    public String Neu() {
        this.myLdapGruppe = new LdapGroup();
        this.itemId = 0;
        return "/newpages/LdapGruppenBearbeiten?faces-redirect=true";
    }

    /**
     * Save.
     *
     * @return page or empty String
     */
    public String Speichern() {
        try {
            this.serviceManager.getLdapGroupService().save(this.myLdapGruppe);
            return "/newpages/LdapGruppenAlle?faces-redirect=true";
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Could not save", e.getMessage());
            return null;
        }
    }

    /**
     * Remove.
     *
     * @return page or empty String
     */
    public String Loeschen() {
        try {
            this.serviceManager.getLdapGroupService().remove(this.myLdapGruppe);
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Could not delete from database", e.getMessage());
            return null;
        }
        return "/newpages/LdapGruppenAlle";
    }

    /**
     * No filter.
     *
     * @return page or empty String
     */
    public String filterKein() {
        try {
            Session session = Helper.getHibernateSession();
            session.clear();
            Criteria crit = session.createCriteria(LdapGroup.class);
            crit.addOrder(Order.asc("title"));
            this.page = new Page(crit, 0);
        } catch (HibernateException he) {
            Helper.setFehlerMeldung("Error on reading database", he.getMessage());
            return null;
        }
        return "/newpages/LdapGruppenAlle";
    }

    /**
     * Method being used as viewAction for ldap group edit form.
     * If 'itemId' is '0', the form for creating a new ldap group will be displayed.
     */
    public void loadLdapGroup() {
        try {
            if (!Objects.equals(this.itemId, 0)) {
                setMyLdapGruppe(this.serviceManager.getLdapGroupService().find(this.itemId));
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error retrieving Ldap group with ID '" + this.itemId + "'; ", e.getMessage());
        }
    }

    /**
     * This method initializes the ldap group list without filters.
     */
    @PostConstruct
    public void initializeLdapGroupList() {
        filterKein();
    }

    public String FilterKeinMitZurueck() {
        filterKein();
        return this.zurueck;
    }

    /*
     * Getter und Setter
     */

    public LdapGroup getMyLdapGruppe() {
        return this.myLdapGruppe;
    }

    public void setMyLdapGruppe(LdapGroup myLdapGruppe) {
        this.myLdapGruppe = myLdapGruppe;
    }

    public void setItemId(int id) { this.itemId = id; }

    public int getItemId() { return this.itemId; }
}
