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

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

@Named("LdapGruppenForm")
@SessionScoped
public class LdapGruppenForm extends BasisForm {
    private static final long serialVersionUID = -5644561256582235244L;
    private LdapGroup myLdapGruppe = new LdapGroup();
    private transient ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(LdapGruppenForm.class);

    private String ldapgrouopListPath = MessageFormat.format(REDIRECT_PATH, "users");
    private String ldapgrouopEditPath = MessageFormat.format(REDIRECT_PATH, "ldapgroupEdit");

    @Inject
    @Named("BenutzerverwaltungForm")
    private BenutzerverwaltungForm userForm;

    /**
     * Create new LDAP group.
     *
     * @return page
     */
    public String Neu() {
        this.myLdapGruppe = new LdapGroup();
        return ldapgrouopEditPath;
    }

    /**
     * Save.
     *
     * @return page or empty String
     */
    public String Speichern() {
        try {
            this.serviceManager.getLdapGroupService().save(this.myLdapGruppe);
            return ldapgrouopListPath;
        } catch (DAOException e) {
            Helper.setErrorMessage("errorSaving", new Object[] {Helper.getTranslation("ldapgruppe") }, logger, e);
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
            Helper.setErrorMessage("errorDeleting", new Object[] {Helper.getTranslation("ldapgruppe") }, logger, e);
            return null;
        }
        return ldapgrouopListPath;
    }

    /**
     * No filter.
     *
     * @return page or empty String
     */
    public String filterKein() {
        List<LdapGroup> ldapGroups = serviceManager.getLdapGroupService().getAll();
        this.page = new Page(0, ldapGroups);
        return "/pages/LdapGruppenAlle";
    }

    /**
     * Method being used as viewAction for ldapgroup edit form.
     *
     * @param id
     *            ID of the ldapgroup to load
     */
    public void loadLdapGroup(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setMyLdapGruppe(this.serviceManager.getLdapGroupService().getById(id));
            }
        } catch (DAOException e) {
            Helper.setErrorMessage("errorLoadingOne", new Object[] {Helper.getTranslation("ldapgruppe"), id }, logger,
                e);
        }
        setSaveDisabled(true);
    }

    /**
     * This method initializes the ldap group list without filters.
     */
    @PostConstruct
    public void initializeLdapGroupList() {
        filterKein();
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
}
