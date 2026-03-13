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

package org.kitodo.production.controller;

import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.User;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.PrimeFaces;

/**
 * Controller for getting and setting the client of users current session.
 */
@Named("SessionClientController")
@RequestScoped
public class SessionClientController {

    private Client selectedClient;

    /**
     * Gets the name of the current session client. In case that no session client
     * has been set, an empty string is returned and a dialog to select a client is
     * shown.
     *
     * @return The current session clients name or empty string case that no session
     *         client has been set.
     */
    public String getCurrentSessionClientName() {
        if (Objects.nonNull(getCurrentSessionClient())) {
            return getCurrentSessionClient().getName();
        } else {
            if (userHasOnlyOneClient()) {
                Client client = getFirstClientOfCurrentUser();
                setSessionClient(client);
                return client.getName();
            }
            return null;
        }
    }

    private Client getFirstClientOfCurrentUser() {
        return getAvailableClientsOfCurrentUser().getFirst();
    }

    private boolean userHasOnlyOneClient() {
        return getAvailableClientsOfCurrentUser().size() == 1;
    }

    /**
     * The conditions when user need to select a session client is configured in
     * this method. Change is not happening if user has only one client
     * assigned.
     *
     * @return True if the session client select dialog should by displayed to the
     *         current user
     */
    public boolean shouldUserChangeSessionClient() {
        return !userHasOnlyOneClient();
    }

    /**
     * Display client selection dialog if user is logged in and has multiple clients.
     */
    public void showClientSelectDialog() {
        User currentUser = ServiceManager.getUserService().getCurrentUser();
        Client defaultClient = currentUser.getDefaultClient();
        if (Objects.nonNull(defaultClient)) {
            setSessionClient(defaultClient);
        } else if (userHasOnlyOneClient()) {
            setSessionClient(getFirstClientOfCurrentUser());
        } else if (Objects.isNull(getCurrentSessionClient()) && !userHasOnlyOneClient()) {
            PrimeFaces.current().executeScript("PF('selectClientDialog').show();");
        }
    }

    /**
     * Get current session client.
     *
     * @return current session client
     */
    public Client getCurrentSessionClient() {
        return ServiceManager.getUserService().getSessionClientOfAuthenticatedUser();
    }

    /**
     * Sets the current selected client as session client.
     */
    public void setSelectedClientAsSessionClient() {
        setSessionClient(selectedClient);
    }

    /**
     * Checks if clients are available for current user.
     *
     * @return true if if clients are available for current user.
     */
    public boolean areClientsAvailableForUser() {
        return !getAvailableClientsOfCurrentUser().isEmpty();
    }

    /**
     * Gets selectedClient.
     *
     * @return The selectedClient.
     */
    public Client getSelectedClient() {
        return selectedClient;
    }

    /**
     * Sets selectedClient.
     *
     * @param selectedClient
     *            The selectedClient.
     */
    public void setSelectedClient(Client selectedClient) {
        this.selectedClient = selectedClient;
    }

    /**
     * Sets the given client object as new session client.
     *
     * @param sessionClient
     *            The client object that is to be the new session client.
     */
    public String setSessionClient(Client sessionClient) {
        ServiceManager.getUserService().getAuthenticatedUser().setSessionClient(sessionClient);
        if (Objects.nonNull(FacesContext.getCurrentInstance())) {
            return FacesContext.getCurrentInstance().getViewRoot().getViewId();
        }
        return null;
    }

    /**
     * Gets all clients to which the user directly assigned and also those from user
     * assigned projects.
     *
     * @return The list of clients.
     */
    public List<Client> getAvailableClientsOfCurrentUser() {
        return ServiceManager.getUserService().getAvailableClientsOfCurrentUser();
    }

    /**
     * Get default client of current user.
     *
     * @return default client of current user
     */
    public Client getDefaultClientOfCurrentUser() {
        return ServiceManager.getUserService().getCurrentUser().getDefaultClient();
    }

    /**
     * Get list of available clients of current user sorted by name.
     * @return list of available clients of current user sorted by name
     */
    public List<Client> getAvailableClientsOfCurrentUserSortedByName() {
        return ServiceManager.getUserService().getAvailableClientsOfCurrentUserSortedByName();
    }

    /**
     * Get amount of time that warning message is displayed to inform user that he will be logged
     * out of the system automatically due to inactivity. Value returned in seconds.
     * If the session HTTP session timeout configured in the 'web.xml' file is 60 seconds or less,
     * the message will be shown 30 seconds before logout. If the timeout is between 1 and 5 minutes,
     * the message will appear 60 seconds before logout. For any session timeout larger than 5 Minutes,
     * it will be shown 300 seconds in advance.
     * @return number of seconds the warning message is displayed to the user before automatic logout
     */
    public int getAutomaticLogoutWarningSeconds() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (Objects.nonNull(facesContext)) {
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
            int maxInactiveInterval = session.getMaxInactiveInterval();
            if (maxInactiveInterval <= 60) {
                return 30;
            } else if (maxInactiveInterval < 300) {
                return 60;
            } else {
                return 300;
            }
        }
        return 60;
    }
}
