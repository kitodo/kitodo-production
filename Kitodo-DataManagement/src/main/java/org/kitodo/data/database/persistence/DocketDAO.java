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

import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;

public class DocketDAO extends BaseDAO<Docket> {

    private static final long serialVersionUID = 1913256950316879121L;

    @Override
    public Docket getById(Integer id) throws DAOException {
        Docket result = retrieveObject(Docket.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
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
    public void remove(Integer id) throws DAOException {
        removeObject(Docket.class, id);
    }
}
