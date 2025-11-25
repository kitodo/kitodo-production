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

package org.kitodo.production.forms.user;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

@Named("UserEditViewRolesTab")
@ViewScoped
public class UserEditViewRolesTab extends BaseForm {

    /**
     * The user object that is being edited (variable "user" references to the user currently logged in, see BaseForm).
     */
    private User userObject;
    private List<Role> availableRoles;

    private static final Logger logger = LogManager.getLogger(UserEditViewRolesTab.class);
   
    /**
     * Initialize UserEditViewRolesTab.
     */
    @PostConstruct
    public void init() {
        sortBy = SortMeta.builder().field("title").order(SortOrder.ASCENDING).build();
    }

    /**
     * Return user object currently being edited.
     * 
     * @return the user currently being edited
     */
    public User getUserObject() {
        return this.userObject;
    }

    /**
     * Return list of roles available for assignment to the user.
     *
     * @return list of roles available for assignment to the user
     */
    public List<Role> getAvailableRoles() {
        return availableRoles;
    }

    /**
     * Method that is called from viewAction of user edit form.
     *
     * @param userObject
     *            the user currently being edited
     */
    public void load(User userObject) {
        this.userObject = userObject;

        try {
            availableRoles = ServiceManager.getRoleService().getAllAvailableForAssignToUser(this.userObject)
                    .stream().sorted(Comparator.comparing(Role::getTitle)).collect(Collectors.toList());
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.ROLE.getTranslationPlural() }, logger,
                e);
            availableRoles = new LinkedList<>();
        }
    }

    /**
     * Save user if there is no other user with the same login.
     *
     * @return page or empty String
     */
    public boolean save() {
        return true;
    }

    /**
     * Remove from role.
     *
     * @return empty String
     */
    public String deleteFromRole() {
        String idParameter = Helper.getRequestParameter("ID");
        if (Objects.nonNull(idParameter)) {
            try {
                int roleId = Integer.parseInt(idParameter);
                for (Role role : this.userObject.getRoles()) {
                    if (role.getId().equals(roleId)) {
                        this.userObject.getRoles().remove(role);
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

    /**
     * Add to role.
     *
     * @return stay on the same page
     */
    public String addToRole() {
        String idParameter = Helper.getRequestParameter("ID");
        if (Objects.nonNull(idParameter)) {
            int roleId = 0;
            try {
                roleId = Integer.parseInt(idParameter);
                Role role = ServiceManager.getRoleService().getById(roleId);

                if (!this.userObject.getRoles().contains(role)) {
                    this.userObject.getRoles().add(role);
                }
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_DATABASE_READING,
                        new Object[] {ObjectType.ROLE.getTranslationSingular(), roleId }, logger, e);
            } catch (NumberFormatException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(ERROR_PARAMETER_MISSING, new Object[] {ID_PARAMETER});
        }
        return this.stayOnCurrentPage;
    }

}
