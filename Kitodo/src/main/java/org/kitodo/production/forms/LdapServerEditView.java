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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.enums.PasswordEncryption;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.security.encrypt.EncryptionUtils;
import org.kitodo.production.security.encrypt.KitodoEncryptionException;
import org.kitodo.production.services.ServiceManager;

@Named("LdapServerEditView")
@ViewScoped
public class LdapServerEditView extends BaseEditView {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "ldapserverEdit");
    
    private static final Logger logger = LogManager.getLogger(LdapServerEditView.class);
    
    private LdapServer ldapServer;

    /**
     * Initialize LdapServerEditView.
     */
    @PostConstruct
    public void init() {
        ldapServer = new LdapServer();
    }

    /**
     * Save LDAP Server.
     *
     * @return page or null
     */
    public String save() {
        try {
            ServiceManager.getLdapServerService().save(ldapServer);
            return LdapServerListView.VIEW_PATH + "&" + getReferrerListOptions();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.LDAP_SERVER.getTranslationSingular()}, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Remove current LDAP Server.
     */
    public String delete() {
        try {
            ServiceManager.getLdapServerService().remove(this.ldapServer);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.LDAP_SERVER.getTranslationSingular()}, logger, e);
        }
        return LdapServerListView.VIEW_PATH + "&" + getReferrerListOptions();
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
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.LDAP_SERVER.getTranslationSingular()}, logger, e);
        }
        setSaveDisabled(true);
    }

    /**
     * Gets manager password.
     *
     * @return The manager password
     */
    public String getManagerPassword() {

        String managerPassword = ldapServer.getManagerPassword();
        String securitySecret = ConfigCore.getParameterOrDefaultValue(ParameterCore.SECURITY_SECRET_LDAPMANAGERPASSWORD);
        if (Objects.nonNull(managerPassword) && !Strings.isBlank(securitySecret)) {
            try {
                return EncryptionUtils.decrypt(ldapServer.getManagerPassword(), securitySecret);
            } catch (KitodoEncryptionException e) {
                Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.LDAP_SERVER.getTranslationSingular()}, logger, e);
            }
        }
        return managerPassword;
    }

    /**
     * Sets manager password.
     *
     * @param managerPassword The manager password
     */
    public void setManagerPassword(String managerPassword) {
        try {
            String securitySecret = ConfigCore.getParameterOrDefaultValue(ParameterCore.SECURITY_SECRET_LDAPMANAGERPASSWORD);
            if (StringUtils.isNotBlank(securitySecret)) {
                managerPassword = EncryptionUtils.encrypt(managerPassword, securitySecret);
            }
            ldapServer.setManagerPassword(managerPassword);
        } catch (KitodoEncryptionException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.LDAP_SERVER.getTranslationSingular()}, logger, e);
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
     * Gets passwordEncryption.
     *
     * @return The passwordEncryption.
     */
    public PasswordEncryption[] getPasswordEncryption() {
        return PasswordEncryption.values();
    }

}
