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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;

@Named("ClientEditView")
@ViewScoped
public class ClientEditView extends BaseForm {

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "clientEdit");

    private static final Logger logger = LogManager.getLogger(ClientEditView.class);
    
    private Client client;
    private Client clientToCopyRoles;
    private List<Role> rolesForClient;
    private List<Client> allClients;

    /**
     * Initialize ClientEditView by loading all existing clients from the database.
     */
    @PostConstruct
    public void init() {
        client = new Client();
        rolesForClient = new ArrayList<>();
        try {
            allClients = ServiceManager.getClientService().getAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.CLIENT.getTranslationPlural() },
                logger, e);
        }
    }

    /**
     * Save client.
     *
     * @return page or empty String
     */
    public String save() {
        try {
            ServiceManager.getClientService().save(this.client);
            for (Role role : rolesForClient) {
                ServiceManager.getRoleService().save(role);
            }
            return usersPage;
        } catch (DAOException | RuntimeException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.CLIENT.getTranslationSingular() }, logger, e);
            return this.stayOnCurrentPage;
        }
    }

    /**
     * Method being used as viewAction for client edit form. If 'clientId' is '0',
     * the form for creating a new client will be displayed.
     */
    public void load(int id) {
        try {
            if (!Objects.equals(id, 0)) {
                client = ServiceManager.getClientService().getById(id);
                rolesForClient = ServiceManager.getRoleService().getAllRolesByClientId(client.getId());
            }
            setSaveDisabled(true);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.CLIENT.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Gets client.
     *
     * @return The client.
     */
    public Client getClient() {
        return client;
    }

    /**
     * Gets all roles for a Client.
     *
     * @return a list of roles
     */
    public List<Role> getRolesForClient() {
        return rolesForClient;
    }

    /**
     * Get clientToCopyRoles.
     *
     * @return value of clientToCopyRoles
     */
    public Client getClientToCopyRoles() {
        return clientToCopyRoles;
    }

    /**
     * Set clientToCopyRoles.
     *
     * @param clientToCopyRoles
     *            as org.kitodo.data.database.beans.Client
     */
    public void setClientToCopyRoles(Client clientToCopyRoles) {
        this.clientToCopyRoles = clientToCopyRoles;
    }

    /**
     * Copies all roles from a chosen client to the current client.
     */
    public void copyRolesToClient() {
        List<Role> allRolesToCopy = ServiceManager.getRoleService().getAllRolesByClientId(clientToCopyRoles.getId());
        for (Role role : allRolesToCopy) {
            Role newRole = new Role();
            newRole.setTitle(role.getTitle());
            newRole.setClient(client);
            newRole.setAuthorities(new ArrayList<>(role.getAuthorities()));
            rolesForClient.add(newRole);
        }
    }

    /**
     * Removes a givon role from a client.
     *
     * @param roleToRemove
     *            role to remove.
     */
    public void deleteRoleFromClient(Role roleToRemove) {
        rolesForClient.remove(roleToRemove);
    }

    /**
     * Get all clients where roles can be copied from.
     * 
     * @return a list of possible clients.
     */
    public List<Client> getPossibleClientsForCopying() throws DAOException {
        return allClients.stream().filter(c -> !c.equals(client)).collect(Collectors.toList());
    }
}
