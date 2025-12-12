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
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("AuthorityEditView")
@ViewScoped
public class AuthorityEditView extends BaseForm {
    
    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "authorityEdit");
    
    private static final Logger logger = LogManager.getLogger(AuthorityEditView.class);

    private Authority authority;
    private String title;
    private String type;

    /**
     * Initialize AuthorityEditView.
     */
    @PostConstruct
    public void init() {
        authority = new Authority();
    }

    /**
     * Get title.
     *
     * @return value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title
     *            as java.lang.String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get type.
     *
     * @return value of type
     */
    public String getType() {
        return type;
    }

    /**
     * Set type.
     *
     * @param type
     *            as java.lang.String
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Save authority.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            this.authority.setTitle(this.title + "_" + this.type);
            ServiceManager.getAuthorityService().save(this.authority);
            return AuthorityListView.VIEW_PATH;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.AUTHORITY.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Method being used as viewAction for authority edit form.
     *
     * @param id
     *            ID of the authority to load
     */
    public void load(int id) {
        if (!Objects.equals(id, 0)) {
            try {
                authority = ServiceManager.getAuthorityService().getById(id);
                title = authority.getTitleWithoutSuffix();
                type = authority.getType();
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.ROLE.getTranslationSingular(), id }, logger, e);
            }
        }
        setSaveDisabled(true);
    }

    /**
     * Get authority.
     *
     * @return value of authority
     */
    public Authority getAuthority() {
        return authority;
    }
}
