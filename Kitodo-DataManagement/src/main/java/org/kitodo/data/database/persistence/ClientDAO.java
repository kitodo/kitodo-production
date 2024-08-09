/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.data.database.persistence;

import java.util.List;

import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.exceptions.DAOException;

public class ClientDAO extends BaseDAO<Client> {

    @Override
    public Client getById(Integer clientId) throws DAOException {
        Client client = retrieveObject(Client.class, clientId);
        if (client == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return client;
    }

    @Override
    public List<Client> getAll() throws DAOException {
        return retrieveAllObjects(Client.class);
    }

    @Override
    public List<Client> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Client ORDER BY id ASC", offset, size);
    }

    @Override
    public List<Client> getAllNotIndexed(int offset, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Integer clientId) throws DAOException {
        removeObject(Client.class, clientId);
    }
}
