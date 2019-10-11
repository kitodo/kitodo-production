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

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.controller.SessionClientController;
import org.kitodo.production.security.CustomLoginSuccessHandler;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

@Named("LoginForm")
@SessionScoped
public class LoginForm implements Serializable {
    private User loggedUser;
    private boolean firstVisit = true;
    private static final String INDEXING_PAGE = "system.jsf?tabIndex=2";
    private static final String DESKTOP_VIEW = "desktop.jsf";

    /**
     * Gets current authenticated User.
     *
     * @return The user object or null if no user is authenticated.
     */
    public User getLoggedUser() {
        if (Objects.nonNull(loggedUser)) {
            return this.loggedUser;
        } else {
            this.loggedUser = ServiceManager.getUserService().getAuthenticatedUser();
            return this.loggedUser;
        }
    }

    public void setLoggedUser(User myClass) {
        this.loggedUser = myClass;
    }

    /**
     * Check if user is already logged im.
     *
     * @return true or false
     */
    public boolean isAlreadyLoggedIn() {
        return false;
    }

    /**
     * Checks and returns whether this is the first time the user visits a Kitodo
     * page in the current session or not. Makes use of the fact that "LoginForm" is
     * a SessionScoped bean.
     *
     * @return whether this is the users first visit to Kitodo during the current
     *         session or not
     */
    public boolean isFirstVisit() {
        boolean visit = firstVisit;
        if (firstVisit) {
            firstVisit = false;
        }
        return visit;
    }

    /**
     * Redirect to desktop if user is already logged in.
     *
     * @return path to desktop
     */
    public String redirectToDesktop() {
        if (Objects.nonNull(this.loggedUser)) {
            return "desktop";
        } else {
            return "login";
        }
    }

    /**
     * Check if index is up to date and if user has multiple clients and display corresponding notification dialogs.
     */
    public void performPostLoginChecks() throws DataException, DAOException, IOException {

        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();

        if (ServiceManager.getIndexingService().isIndexCorrupted()) {
            if (ServiceManager.getSecurityAccessService().hasAuthorityToEditIndex()) {
                // redirect admins to indexing page
                context.redirect(INDEXING_PAGE);
            } else {
                // show dialog with logout button to other users
                PrimeFaces.current().executeScript("PF('indexWarningDialog').show();");
            }
        } else {
            PrimeFaces.current().executeScript("PF('indexWarningDialog').hide();");
            SessionClientController controller = new SessionClientController();
            if (controller.getAvailableClientsOfCurrentUser().size() > 1
                    && Objects.isNull(controller.getCurrentSessionClient())) {
                controller.showClientSelectDialog();
            } else {
                String originalRequest = CustomLoginSuccessHandler.getOriginalRequest(context.getSessionMap()
                        .get(CustomLoginSuccessHandler.getSavedRequestString()));
                if (originalRequest.isEmpty() || originalRequest.contains("login")) {
                    context.redirect(DESKTOP_VIEW);
                } else {
                    context.redirect(context.getRequestContextPath() + originalRequest);
                }
            }
        }
    }
}
