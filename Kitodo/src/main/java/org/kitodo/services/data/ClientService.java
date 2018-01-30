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

package org.kitodo.services.data;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ClientDAO;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchDatabaseService;

public class ClientService extends SearchDatabaseService<Client, ClientDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(ClientService.class);
    private static ClientService instance = null;

    /**
     * Saves client to database.
     *
     * @param client
     *            The ldap server.
     */
    public void save(Client client) throws DAOException {
        dao.save(client);
    }

    /**
     * Removes client from database.
     *
     * @param client
     *            The ldap server.
     */
    public void remove(Client client) throws DAOException {
        dao.remove(client);
    }

    /**
     * Removes client from database by id.
     *
     * @param id
     *            The ldap server id.
     */
    public void remove(Integer id) throws DAOException {
        dao.remove(id);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM Client");
    }

    /**
     * Return singleton variable of type AuthorityService.
     *
     * @return unique instance of AuthorityService
     */
    public static ClientService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (ClientService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new ClientService();
                }
            }
        }
        return instance;
    }

    private ClientService() {
        super(new ClientDAO());
    }

}
