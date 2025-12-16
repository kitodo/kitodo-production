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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.forms.LoginForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.UserService;

@Named("UserEditView")
@ViewScoped
public class UserEditView extends BaseForm {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "userEdit");

    /**
     * The user object that is being edited (variable "user" references to the user currently logged in, see BaseForm).
     */
    private User userObject;

    private static final Logger logger = LogManager.getLogger(UserEditView.class);
    private final transient UserService userService = ServiceManager.getUserService();

    @Inject
    private LoginForm loginForm;

    @Inject
    private UserEditViewDetailsTab detailsTab;

    @Inject
    private UserEditViewMetadataTab metadataTab;

    @Inject
    private UserEditViewRolesTab rolesTab;

    @Inject
    private UserEditViewProjectsTab projectsTab;

    @Inject
    private UserEditViewClientsTab clientsTab;

    /**
     * Initialize UserEditView.
     */
    @PostConstruct
    public void init() {
        this.userObject = new User();
        List<Client> clients = new ArrayList<>();
        clients.add(ServiceManager.getUserService().getSessionClientOfAuthenticatedUser());
        this.userObject.setClients(clients);
        this.userObject.setName("");
        this.userObject.setSurname("");
        this.userObject.setLogin("");
        this.userObject.setLdapLogin("");
        this.userObject.setPassword("");
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
     * Method being used as viewAction for user edit form.
     *
     * @param id
     *            ID of the user to load
     */
    public void load(int id) {
        // reset when user is loaded
        try {
            if (!Objects.equals(id, 0)) {
                this.userObject = userService.getById(id);
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.USER.getTranslationSingular(), id }, logger, e);
        }

        detailsTab.load(this.userObject);
        metadataTab.load(this.userObject);
        rolesTab.load(this.userObject);
        projectsTab.load(this.userObject);
        clientsTab.load(this.userObject);
    }

    /**
     * Save user if there is no other user with the same login.
     *
     * @return page or empty String
     */
    public String save() {
        if (!detailsTab.save() || !metadataTab.save() || !rolesTab.save() || !projectsTab.save() || !clientsTab.save()) {
            return this.stayOnCurrentPage;
        }

        try {
            userService.save(this.userObject);

            // check if currently logged in user is updating own user details
            if (userService.getAuthenticatedUser().getId().equals(this.userObject.getId())) {
                loginForm.setLoggedUser(this.userObject);
                ServiceManager.getSecurityAccessService().updateAuthentication(this.userObject);
            }            
        } catch (DAOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.USER.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
        
        return UserListView.VIEW_PATH + "&firstRow=" + getReferrerFirstRow();
    }

}
