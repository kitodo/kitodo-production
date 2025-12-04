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

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.model.LazyBeanModel;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("AuthorityForm")
@SessionScoped
public class AuthorityForm extends BaseForm {
    private static final Logger logger = LogManager.getLogger(AuthorityForm.class);
    private Authority authority = new Authority();
    private String title;
    private String type;
    private final String authorityEditPath = MessageFormat.format(REDIRECT_PATH, "authorityEdit");

    /**
     * Default constructor that also sets the LazyBeanModel instance of this
     * bean.
     */
    public AuthorityForm() {
        super();
        super.setLazyBeanModel(new LazyBeanModel(ServiceManager.getAuthorityService()));
        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
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
            this.authority.setTitle(this.title + "_" + this.type);
            ServiceManager.getAuthorityService().save(this.authority);
            return usersPage;
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
            if (!this.authority.getRoles().isEmpty()) {
                Helper.setErrorMessage("authorityAssignedError");
                return;
            }
            ServiceManager.getAuthorityService().remove(this.authority);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.AUTHORITY.getTranslationSingular() },
                logger, e);
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
            setAuthorityById(id);
        }
        setSaveDisabled(true);
    }

    /**
     * Set authority by id.
     *
     * @param id
     *            ID of authority to set
     */
    public void setAuthorityById(int id) {
        try {
            setAuthority(ServiceManager.getAuthorityService().getById(id));
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.ROLE.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Set authority.
     *
     * @param authority
     *            as org.kitodo.data.database.beans.Authority
     */
    public void setAuthority(Authority authority) {
        this.authority = authority;
        this.title = this.authority.getTitleWithoutSuffix();
        this.type = this.authority.getType();
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
