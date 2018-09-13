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

package org.kitodo.forms;

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
import org.kitodo.helper.Helper;

@Named("LdapGroupForm")
@SessionScoped
public class LdapGroupForm extends BaseForm {
    private static final long serialVersionUID = -5644561256582235244L;
    private LdapGroup myLdapGruppe = new LdapGroup();
    private static final Logger logger = LogManager.getLogger(LdapGroupForm.class);
    private static final String LDAP_GROUP = "ldapGroup";
    private String ldapGroupListPath = MessageFormat.format(REDIRECT_PATH, "users");
    private String ldapGroupEditPath = MessageFormat.format(REDIRECT_PATH, "ldapgroupEdit");

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
            return serviceManager.getLdapGroupService().getAll();
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
    public String saveLdapGroup() {
        try {
            this.serviceManager.getLdapGroupService().saveToDatabase(this.myLdapGruppe);
            return ldapGroupListPath;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {Helper.getTranslation(LDAP_GROUP) }, logger, e);
            return null;
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
            LdapGroup baseLdapGroup = serviceManager.getLdapGroupService().getById(itemId);
            this.myLdapGruppe = serviceManager.getLdapGroupService().duplicateLdapGroup(baseLdapGroup);
            return ldapGroupEditPath;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DUPLICATE, new Object[] {Helper.getTranslation(LDAP_GROUP) }, logger, e);
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
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {Helper.getTranslation(LDAP_GROUP) }, logger, e);
            return null;
        }
        return ldapGroupListPath;
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
            setMyLdapGruppe(this.serviceManager.getLdapGroupService().getById(ldapGroupID));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {Helper.getTranslation(LDAP_GROUP), ldapGroupID }, logger, e);
        }
    }
}
