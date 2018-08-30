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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

@Named("LdapGruppenForm")
@SessionScoped
public class LdapGruppenForm extends BaseForm {
    private static final long serialVersionUID = -5644561256582235244L;
    private LdapGroup myLdapGruppe = new LdapGroup();
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(LdapGruppenForm.class);
    private static final String LDAP_GROUP = "ldapgruppe";
    private String ldapgrouopListPath = MessageFormat.format(REDIRECT_PATH, "users");
    private String ldapgrouopEditPath = MessageFormat.format(REDIRECT_PATH, "ldapgroupEdit");

    /**
     * Create new LDAP group.
     *
     * @return page
     */
    public String newLdapGroup() {
        this.myLdapGruppe = new LdapGroup();
        return ldapgrouopEditPath;
    }

    /**
     * Gets all ldap groups.
     * 
     * @return list of LdapGroup objects.
     */
    public List<LdapGroup> getLdapGroups() {
        try {
            return serviceManager.getLdapGroupService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingMany", new Object[] {Helper.getTranslation("ldapGroups") }, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Save LDAP Group.
     *
     * @return page or null
     */
    public String saveLdapGroup() {
        try {
            this.serviceManager.getLdapGroupService().saveToDatabase(this.myLdapGruppe);
            return ldapgrouopListPath;
        } catch (DAOException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation(LDAP_GROUP) }, logger, e);
            return null;
        }
    }

    /**
     * Remove LDAP Group.
     *
     * @return page or null
     */
    public String deleteLdapGroup() {
        try {
            this.serviceManager.getLdapGroupService().removeFromDatabase(this.myLdapGruppe);
        } catch (DAOException e) {
            Helper.setErrorMessage("errorDeleting", new Object[] {Helper.getTranslation(LDAP_GROUP) }, logger, e);
            return null;
        }
        return ldapgrouopListPath;
    }

    /**
     * Method being used as viewAction for ldap group edit form.
     *
     * @param id
     *            ID of the ldap group to load
     */
    public void loadLdapGroup(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setMyLdapGruppe(this.serviceManager.getLdapGroupService().getById(id));
            }
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation(LDAP_GROUP), id }, logger,
                e);
        }
        setSaveDisabled(true);
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

    /**
     * Set LDAP group by ID.
     *
     * @param ldapgroupID
     *          ID of LDAP group to set.
     */
    public void setLdapGroupById(int ldapgroupID) {
        try {
            setMyLdapGruppe(this.serviceManager.getLdapGroupService().getById(ldapgroupID));
        } catch (DAOException e) {
            Helper.setErrorMessage("Unable to find ldap group with ID " + ldapgroupID, logger, e);
        }
    }
}
