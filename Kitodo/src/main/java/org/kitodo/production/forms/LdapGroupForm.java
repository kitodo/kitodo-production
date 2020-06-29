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

package org.kitodo.production.forms;

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
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("LdapGroupForm")
@SessionScoped
public class LdapGroupForm extends BaseForm {
    private LdapGroup myLdapGruppe = new LdapGroup();
    private static final Logger logger = LogManager.getLogger(LdapGroupForm.class);
    private static final String LDAP_GROUP = "ldapGroup";
    private final String ldapGroupEditPath = MessageFormat.format(REDIRECT_PATH, "ldapgroupEdit");

    protected static final String ERROR_DELETING_LDAP_GROUPE = "ldapGroupInUse";

    /**
     * Create new LDAP group.
     *
     * @return page
     */
    public String newLdapGroup() {
        this.myLdapGruppe = new LdapGroup();
        return ldapGroupEditPath;
    }

    /**
     * Gets all ldap groups.
     *
     * @return list of LdapGroup objects.
     */
    public List<LdapGroup> getLdapGroups() {
        try {
            return ServiceManager.getLdapGroupService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {Helper.getTranslation("ldapGroups") }, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Save LDAP Group.
     *
     * @return page or null
     */
    public String save() {
        try {
            ServiceManager.getLdapGroupService().saveToDatabase(this.myLdapGruppe);
            return usersPage;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {Helper.getTranslation(LDAP_GROUP) }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Duplicate the selected LDAP group.
     *
     * @param itemId
     *            ID of the LDAP group to duplicate
     * @return page address; either redirect to the edit LDAP group page or return
     *         'null' if the LDAP group could not be retrieved, which will prompt
     *         JSF to remain on the same page and reuse the bean.
     */
    public String duplicateLdapGroup(Integer itemId) {
        try {
            LdapGroup baseLdapGroup = ServiceManager.getLdapGroupService().getById(itemId);
            this.myLdapGruppe = ServiceManager.getLdapGroupService().duplicateLdapGroup(baseLdapGroup);
            return ldapGroupEditPath;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DUPLICATE, new Object[] {Helper.getTranslation(LDAP_GROUP) }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Remove LDAP Group.
     *
     * @return page or null
     */
    public String delete() {
        if (!this.myLdapGruppe.getUsers().isEmpty()) {
            Helper.setErrorMessage(ERROR_DELETING_LDAP_GROUPE, new Object[]{Helper.getTranslation(LDAP_GROUP)});
            return this.stayOnCurrentPage;
        }
        try {
            ServiceManager.getLdapGroupService().removeFromDatabase(this.myLdapGruppe);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {Helper.getTranslation(LDAP_GROUP) }, logger, e);
            return this.stayOnCurrentPage;
        }
        return usersPage;
    }

    /**
     * Method being used as viewAction for ldap group edit form.
     *
     * @param id
     *            ID of the ldap group to load
     */
    public void load(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setMyLdapGruppe(ServiceManager.getLdapGroupService().getById(id));
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {Helper.getTranslation(LDAP_GROUP), id }, logger,
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
     * @param ldapGroupID
     *          ID of LDAP group to set.
     */
    public void setLdapGroupById(int ldapGroupID) {
        try {
            setMyLdapGruppe(ServiceManager.getLdapGroupService().getById(ldapGroupID));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {Helper.getTranslation(LDAP_GROUP), ldapGroupID }, logger, e);
        }
    }
}
