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
import java.util.Objects;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("LdapGroupEditView")
@ViewScoped
public class LdapGroupEditView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(LdapGroupEditView.class);

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "ldapgroupEdit");

    private LdapGroup ldapGroup;

    @PostConstruct
    public void init() {
        ldapGroup = new LdapGroup();
        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
    }

    /**
     * Save LDAP Group.
     *
     * @return page or null
     */
    public String save() {
        try {
            ServiceManager.getLdapGroupService().save(ldapGroup);
            return LdapGroupListView.VIEW_PATH;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.LDAP_GROUP.getTranslationSingular()}, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * View action removing current LDAP group.
     *
     * @return page or null
     */
    public String delete() {
        if (LdapGroupListView.deleteLdapGroup(ldapGroup)) {
            return LdapGroupListView.VIEW_PATH;
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Method being used as viewAction for ldap group edit form.
     *
     * @param id
     *            ID of the ldap group to load
     * @param duplicate
     *            whether the ldap group should be duplicated
     */
    public void load(int id, Boolean duplicate) {
        if (Objects.nonNull(duplicate) && duplicate) {
            loadAsDuplicate(id);
        } else {
            loadById(id);
        }
        setSaveDisabled(true);
    }

    /**
     * Load an existing ldap group as duplicate such that it can be saved as a new ldap group.
     * 
     * @param id the id of the ldap group that should be duplicated
     */
    private void loadAsDuplicate(int id) {
        try {
            LdapGroup baseLdapGroup = ServiceManager.getLdapGroupService().getById(id);
            this.ldapGroup = ServiceManager.getLdapGroupService().duplicateLdapGroup(baseLdapGroup);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DUPLICATE, new Object[] {ObjectType.LDAP_GROUP.getTranslationSingular()}, logger, e);
        }
    }

    /**
     * Load an existing ldap group for editing.
     * 
     * @param id the id of the ldap group that will be edited
     */
    private void loadById(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                this.ldapGroup = ServiceManager.getLdapGroupService().getById(id);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.LDAP_GROUP.getTranslationSingular()}, logger, e);
        }
    }

    /**
     * Return current ldap group.
     * 
     * @return the current ldap group
     */
    public LdapGroup getLdapGroup() {
        return this.ldapGroup;
    }

}
