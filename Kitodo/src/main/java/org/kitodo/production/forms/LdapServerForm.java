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
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.enums.PasswordEncryption;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("LdapServerForm")
@SessionScoped
public class LdapServerForm extends BaseForm {

    private static final long serialVersionUID = 2390900243176826176L;
    private static final Logger logger = LogManager.getLogger(LdapServerForm.class);
    private static final String LDAP_SERVER = Helper.getTranslation("ldapServer");
    private String ldapServerListPath = MessageFormat.format(REDIRECT_PATH, "users");
    private String ldapServerEditPath = MessageFormat.format(REDIRECT_PATH, "ldapserverEdit");

    private LdapServer ldapServer;
    private PasswordEncryption passwordEncryption;

    /**
     * Create new LDAP server.
     *
     * @return page
     */
    public String newLdapServer() {
        this.ldapServer = new LdapServer();
        return ldapServerEditPath;
    }

    /**
     * Gets all ldap servers.
     *
     * @return list of LdapServer objects.
     */
    public List<LdapServer> getLdapServers() {
        try {
            return ServiceManager.getLdapServerService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {Helper.getTranslation("ldapServers") }, logger, e);
            return new ArrayList<>();
        }
    }

    /**
     * Save LDAP Server.
     *
     * @return page or null
     */
    public String save() {
        try {
            ServiceManager.getLdapServerService().saveToDatabase(this.ldapServer);
            return ldapServerListPath;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {LDAP_SERVER }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Remove LDAP Server.
     *
     */
    public void delete() {
        try {
            ServiceManager.getLdapServerService().removeFromDatabase(this.ldapServer);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {LDAP_SERVER }, logger, e);
        }
    }

    /**
     * Method being used as viewAction for ldap server edit form.
     *
     * @param id
     *            ID of the ldap server to load
     */
    public void load(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                this.ldapServer = ServiceManager.getLdapServerService().getById(id);
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {LDAP_SERVER, id }, logger,
                e);
        }
        setSaveDisabled(true);
    }

    /**
     * Set LDAP group by ID.
     *
     * @param ldapServerID
     *          ID of LDAP server to set.
     */
    public void setLdapServerById(int ldapServerID) {
        try {
            this.ldapServer = ServiceManager.getLdapServerService().getById(ldapServerID);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {LDAP_SERVER, ldapServerID }, logger, e);
        }
    }

    /**
     * Gets ldapServer.
     *
     * @return The ldapServer.
     */
    public LdapServer getLdapServer() {
        return ldapServer;
    }

    /**
     * Sets ldapServer.
     *
     * @param ldapServer The ldapServer.
     */
    public void setLdapServer(LdapServer ldapServer) {
        this.ldapServer = ldapServer;
    }

    /**
     * Gets passwordEncryption.
     *
     * @return The passwordEncryption.
     */
    public PasswordEncryption[] getPasswordEncryption() {
        return PasswordEncryption.values();
    }

    /**
     * Sets passwordEncryption.
     *
     * @param passwordEncryption The passwordEncryption.
     */
    public void setPasswordEncryption(PasswordEncryption passwordEncryption) {
        this.passwordEncryption = passwordEncryption;
    }
}
