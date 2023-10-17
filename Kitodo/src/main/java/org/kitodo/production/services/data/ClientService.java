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

package org.kitodo.production.services.data;

import static org.kitodo.constants.StringConstants.COMMA_DELIMITER;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ClientDAO;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;

public class ClientService extends SearchDatabaseService<Client, ClientDAO> {

    private static volatile ClientService instance = null;

    /**
     * Return singleton variable of type ClientService.
     *
     * @return unique instance of ClientService
     */
    public static ClientService getInstance() {
        ClientService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ClientService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new ClientService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Constructor.
     */
    private ClientService() {
        super(new ClientDAO());
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM Client");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        return countDatabaseRows();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Client> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        return dao.getByQuery("FROM Client" + getSort(sortField, sortOrder), filters, first, pageSize);
    }

    /**
     * Refresh project object after update.
     *
     * @param client
     *            object
     */
    @Override
    public void refresh(Client client) {
        dao.refresh(client);
    }

    /**
     * Add standard list columns to client.
     * @param client Client to add standard list columns to.
     * @return updated client
     */
    public Client addStandardListColumns(Client client) {
        client.setListColumns(ServiceManager.getListColumnService().getAllStandardListColumns());
        return client;
    }

    /**
     * Find all clients available to assign to the edited user. It will be
     * displayed in the addclientsPopup.
     *
     * @param user
     *            user which is going to be edited
     * @return list of all matching clients
     */
    public List<Client> getAllAvailableForAssignToUser(User user) throws DAOException {
        List<Client> clients = getAll();
        clients.removeAll(user.getClients());
        return clients;
    }

    /**
     * Create and return String containing the names of all given clients joined by a ", ".
     * @param clients list of roles
     * @return String containing client names
     */
    public static String getClientNames(List<Client> clients) {
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewClientList()) {
            return clients.stream().map(Client::getName).collect(Collectors.joining(COMMA_DELIMITER));
        } else {
            return clients.stream().filter(client -> ServiceManager.getUserService().getAuthenticatedUser().getClients()
                    .contains(client)).map(Client::getName).collect(Collectors.joining(COMMA_DELIMITER));
        }
    }
}
