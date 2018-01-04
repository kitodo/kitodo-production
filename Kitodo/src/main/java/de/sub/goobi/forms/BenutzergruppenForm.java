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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Authorization;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.model.LazyDTOModel;
import org.kitodo.services.ServiceManager;
import org.primefaces.model.DualListModel;

@Named("BenutzergruppenForm")
@SessionScoped
public class BenutzergruppenForm extends BasisForm {
    private static final long serialVersionUID = 8051160917458068675L;
    private static final Logger logger = LogManager.getLogger(BenutzergruppenForm.class);
    private UserGroup myBenutzergruppe = new UserGroup();
    private transient ServiceManager serviceManager = new ServiceManager();
    private int userGroupId;

    /**
     * Empty default constructor that also sets the LazyDTOModel instance of this
     * bean.
     */
    public BenutzergruppenForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getUserGroupService()));
    }

    /**
     * Create new user group.
     *
     * @return page address
     */
    public String newUserGroup() {
        this.myBenutzergruppe = new UserGroup();
        this.userGroupId = 0;
        return redirectToEdit("?faces-redirect=true");
    }

    /**
     * Save user group.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            this.serviceManager.getUserGroupService().save(this.myBenutzergruppe);
            return redirectToList("?faces-redirect=true");
        } catch (DataException e) {
            Helper.setFehlerMeldung("Error, could not save", e.getMessage());
            return null;
        }
    }

    /**
     * Remove user group.
     *
     * @return page or empty String
     */
    public String delete() {
        try {
            this.serviceManager.getUserGroupService().refresh(this.myBenutzergruppe);
            if (this.myBenutzergruppe.getUsers().size() > 0) {
                for (User b : this.myBenutzergruppe.getUsers()) {
                    b.getUserGroups().remove(this.myBenutzergruppe);
                }
                this.myBenutzergruppe.setUsers(new ArrayList<>());
                this.serviceManager.getUserGroupService().save(this.myBenutzergruppe);
            }
            if (this.myBenutzergruppe.getTasks().size() > 0) {
                Helper.setFehlerMeldung("userGroupAssignedError");
                return null;
            }
            this.serviceManager.getUserGroupService().remove(this.myBenutzergruppe);
        } catch (DataException e) {
            Helper.setFehlerMeldung("Error, could not delete", e.getMessage());
            return null;
        }
        return redirectToList("?faces-redirect=true");
    }

    /**
     * Method being used as viewAction for user group edit form. If
     * 'userGroupId' is '0', the form for creating a new user group will be
     * displayed.
     */
    public void loadUserGroup() {
        try {
            if (!Objects.equals(this.userGroupId, 0)) {
                setMyBenutzergruppe(this.serviceManager.getUserGroupService().getById(this.userGroupId));
            }
        } catch (DAOException e) {
            Helper.setFehlerMeldung("Error retrieving project with ID '" + this.userGroupId + "'; ", e.getMessage());
        }
    }

    /*
     * Getter und Setter
     */

    public UserGroup getMyBenutzergruppe() {
        return this.myBenutzergruppe;
    }

    public void setMyBenutzergruppe(UserGroup myBenutzergruppe) {
        Helper.getHibernateSession().clear();
        this.myBenutzergruppe = myBenutzergruppe;
    }

    public void setUserGroupId(int id) {
        this.userGroupId = id;
    }

    public int getUserGroupId() {
        return this.userGroupId;
    }

    /**
     * Return the list of available authorization levels and the list of
     * authorization levels currently assigned to 'myBenutzergruppe' as a combined
     * 'DualListModel' that is used by the frontend for authorization management of
     * user groups utilizing a PrimeFaces PickList object.
     *
     * @return DualListModel of available and assigned authorization levels
     */
    public DualListModel<Authorization> getAuthorizations() {
        List<Authorization> assignedAuthorizations = this.myBenutzergruppe.getAuthorizations();
        List<Authorization> availableAuthorizations = serviceManager.getAuthorizationService().getAll();
        availableAuthorizations.removeAll(assignedAuthorizations);
        return new DualListModel<>(availableAuthorizations, assignedAuthorizations);
    }

    /**
     * Assign the target property of given DualListModel of authorizations to
     * 'myBenutzergruppe' using a PrimeFaces PickList object.
     *
     * @param authorizations
     *            list of authorizations assigned to 'myBenutzergruppe'
     */
    public void setAuthorizations(DualListModel<Authorization> authorizations) {
        try {
            List<Authorization> assignedAuthorizations = new ArrayList<>();
            for (Object object : authorizations.getTarget()) {
                assignedAuthorizations
                        .add(serviceManager.getAuthorizationService().getById(Integer.parseInt((String) object)));
            }
            this.myBenutzergruppe.setAuthorizations(assignedAuthorizations);
        } catch (DAOException e) {
            logger.error(e.getMessage());
        }
    }

    // TODO:
    // replace calls to this function with "/pages/usergroupEdit" once we have
    // completely switched to the new frontend pages
    private String redirectToEdit(String urlSuffix) {
        String referrer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap()
                .get("referer");
        String callerViewId = referrer.substring(referrer.lastIndexOf("/") + 1);
        if (!callerViewId.isEmpty() && callerViewId.contains("users.jsf")) {
            return "/pages/usergroupEdit" + urlSuffix;
        } else {
            return "/pages/BenutzergruppenBearbeiten" + urlSuffix;
        }
    }

    // TODO:
    // replace calls to this function with "/pages/users" once we have completely
    // switched to the new frontend pages
    private String redirectToList(String urlSuffix) {
        String referrer = FacesContext.getCurrentInstance().getExternalContext().getRequestHeaderMap()
                .get("referer");
        String callerViewId = referrer.substring(referrer.lastIndexOf("/") + 1);
        if (!callerViewId.isEmpty() && callerViewId.contains("usergroupEdit.jsf")) {
            return "/pages/users" + urlSuffix;
        } else {
            return "/pages/BenutzergruppenAlle" + urlSuffix;
        }
    }

}
