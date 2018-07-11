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

    public String getCurrentSessionClientName() {
        if (Objects.nonNull(getCurrentSessionClient())) {
            return getCurrentSessionClient().getName();
        } else {
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('selectClientDialog').show();");
            return "";
        }
    }

    public Client getCurrentSessionClient() {
        return serviceManager.getUserService().getSessionClientOfAuthenticatedUser();
    }

    public void setSelectedClientAsSessionClient() {
        serviceManager.getUserService().getAuthenticatedUser().setSessionClient(selectedClient);
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
     * @param selectedClient The selectedClient.
     */
    public void setSelectedClient(Client selectedClient) {
        this.selectedClient = selectedClient;
    }
}
