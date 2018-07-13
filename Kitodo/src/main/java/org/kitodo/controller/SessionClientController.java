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

package org.kitodo.controller;

import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.kitodo.data.database.beans.Client;
import org.kitodo.services.ServiceManager;
import org.primefaces.context.RequestContext;

@Named("SessionClientController")
@RequestScoped
public class SessionClientController {

    private transient ServiceManager serviceManager = new ServiceManager();

    private Client selectedClient;

    /**
     * Gets the name of the current session client. In case that no session client
     * has been set, an empty string is returned and a dialog to select a client is
     * shown.
     * 
     * @return The current session clients name or emtpy string case that no session
     *         client has been set.
     */
    public String getCurrentSessionClientName() {
        if (Objects.nonNull(getCurrentSessionClient())) {
            return getCurrentSessionClient().getName();
        } else {
            showClientSelectDialog();
            return "";
        }
    }

    private void showClientSelectDialog() {
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('selectClientDialog').show();");
    }

    private Client getCurrentSessionClient() {
        return serviceManager.getUserService().getSessionClientOfAuthenticatedUser();
    }

    public void setSelectedClientAsSessionClient() {
        setSessionClient(selectedClient);
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
    public void setSessionClient(Client sessionClient) {
        serviceManager.getUserService().getAuthenticatedUser().setSessionClient(sessionClient);
    }
}
