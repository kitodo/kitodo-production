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

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Project;
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
        return getAvailableClientsOfCurrentUser().get(0);
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
        if (Objects.isNull(getCurrentSessionClient()) && !userHasOnlyOneClient()) {
            PrimeFaces.current().executeScript("PF('selectClientDialog').show();");
        } else if (userHasOnlyOneClient()) {
            setSessionClient(getFirstClientOfCurrentUser());
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
    public List<Client> getAvailableClientsOfCurrentUser()  {
        User currentUser = ServiceManager.getUserService().getCurrentUser();
        List<Client> clients = currentUser.getClients();
        for (Project project : currentUser.getProjects()) {
            if (!clients.contains(project.getClient())) {
                clients.add(project.getClient());
            }
        }
        return clients;
    }
}
