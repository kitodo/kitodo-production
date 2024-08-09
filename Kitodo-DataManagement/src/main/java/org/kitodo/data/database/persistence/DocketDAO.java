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

import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;

public class DocketDAO extends BaseDAO<Docket> {

    @Override
    public Docket getById(Integer id) throws DAOException {
        Docket docket = retrieveObject(Docket.class, id);
        if (docket == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return docket;
    }

    @Override
    public List<Docket> getAll() throws DAOException {
        return retrieveAllObjects(Docket.class);
    }

    @Override
    public List<Docket> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Docket ORDER BY id", offset, size);
    }

    @Override
    public List<Docket> getAllNotIndexed(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Docket WHERE indexAction = 'INDEX' OR indexAction IS NULL ORDER BY id ASC", offset,
                size);
    }

    @Override
    public void remove(Integer docketId) throws DAOException {
        removeObject(Docket.class, docketId);
    }
}
