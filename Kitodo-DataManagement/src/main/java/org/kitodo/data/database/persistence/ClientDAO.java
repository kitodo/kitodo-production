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

package org.kitodo.data.database.persistence;

import java.util.List;

import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.exceptions.DAOException;

public class ClientDAO extends BaseDAO<Client> {

    private static final long serialVersionUID = 4987176626562649317L;

    @Override
    public Client getById(Integer id) throws DAOException {
        Client result = retrieveObject(Client.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Client> getAll() {
        return retrieveAllObjects(Client.class);
    }

    @Override
    public List<Client> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Client ORDER BY id ASC", offset, size);
    }

    @Override
    public Client save(Client client) throws DAOException {
        storeObject(client);
        return retrieveObject(Client.class, client.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Client.class, id);
    }

    /**
     * Refresh Client object after some changes.
     *
     * @param client
     *            object
     */
    public void refresh(Client client) {
        refreshObject(client);
    }
}
