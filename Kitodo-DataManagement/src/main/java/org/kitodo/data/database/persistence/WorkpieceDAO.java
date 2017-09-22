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

import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;

public class WorkpieceDAO extends BaseDAO<Workpiece> {
    private static final long serialVersionUID = 123266825187246791L;

    @Override
    public Workpiece getById(Integer id) throws DAOException {
        Workpiece result = retrieveObject(Workpiece.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Workpiece> getAll() {
        return retrieveAllObjects(Workpiece.class);
    }

    @Override
    public List<Workpiece> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Workpiece ORDER BY id ASC", offset, size);
    }

    @Override
    public Workpiece save(Workpiece workpiece) throws DAOException {
        storeObject(workpiece);
        return retrieveObject(Workpiece.class, workpiece.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Workpiece.class, id);
    }
}
