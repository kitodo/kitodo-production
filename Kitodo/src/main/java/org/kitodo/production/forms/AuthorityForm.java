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

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.services.ServiceManager;

@Named("AuthorityForm")
@SessionScoped
public class AuthorityForm extends BaseForm {
    private static final long serialVersionUID = 3541160917458068675L;
    private static final Logger logger = LogManager.getLogger(RoleForm.class);
    private Authority authority = new Authority();
    private final String authorityListPath = MessageFormat.format(REDIRECT_PATH, "users");
    private final String authorityEditPath = MessageFormat.format(REDIRECT_PATH, "authorityEdit");

    /**
     * Default constructor that also sets the LazyDTOModel instance of this bean.
     */
    public AuthorityForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getAuthorityService()));
    }

    /**
     * Create new authority.
     *
     * @return page address
     */
    public String newAuthority() {
        this.authority = new Authority();
        return authorityEditPath;
    }

    /**
     * Save authority.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            ServiceManager.getAuthorityService().saveToDatabase(this.authority);
            return authorityListPath;
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.AUTHORITY.getTranslationSingular() }, logger,
                e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Remove authority.
     */
    public void delete() {
        try {

            ServiceManager.getAuthorityService().removeFromDatabase(this.authority);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.AUTHORITY.getTranslationSingular() },
                logger, e);
        }
    }

    /**
     * Method being used as viewAction for role edit form. Selectable clients and
     * projects are initialized as well.
     *
     * @param id
     *            ID of the role to load
     */
    public void load(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                setAuthority(ServiceManager.getAuthorityService().getById(id));
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.AUTHORITY.getTranslationSingular(), id },
                logger, e);
        }
        setSaveDisabled(true);
    }

    /**
     * Set authority.
     *
     * @param authority
     *            as org.kitodo.data.database.beans.Authority
     */
    public void setAuthority(Authority authority) {
        this.authority = authority;
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
