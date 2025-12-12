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

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("LdapServerListView")
@ViewScoped
public class LdapServerListView extends BaseForm {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "users") + "&tabIndex=5";

    private static final Logger logger = LogManager.getLogger(LdapServerEditView.class);    

    private List<LdapServer> ldapServers;

    /**
     * Initialize LdapServerListView.
     */
    @PostConstruct
    public void init() {
        ldapServers = getLdapServersOrShowErrorMessage();
        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
    }

    /**
     * Load ldap servers from database or show error message in case they cannot be loaded.
     * 
     * @return the list of ldap servers
     */
    private List<LdapServer> getLdapServersOrShowErrorMessage() {
        try {
            return ServiceManager.getLdapServerService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.LDAP_SERVER.getTranslationPlural()}, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Create new LDAP server.
     *
     * @return page
     */
    public String newLdapServer() {
        return LdapServerEditView.VIEW_PATH;
    }

    /**
     * Gets all ldap servers.
     *
     * @return list of LdapServer objects.
     */
    public List<LdapServer> getLdapServers() {
        return ldapServers;
    }

    /**
     * Remove LDAP Server.
     *
     */
    public String deleteById(int id) {
        try {
            ServiceManager.getLdapServerService().remove(id);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.LDAP_SERVER.getTranslationSingular()}, logger, e);
        }
        return VIEW_PATH;
    }

}
