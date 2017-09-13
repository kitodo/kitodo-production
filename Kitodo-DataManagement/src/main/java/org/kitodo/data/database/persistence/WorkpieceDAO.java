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

    /**
     * Find workpiece object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public Workpiece find(Integer id) throws DAOException {
        Workpiece result = retrieveObject(Workpiece.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all workpieces from the database.
     *
     * @return all persisted dockets
     */
    public List<Workpiece> findAll() {
        return retrieveAllObjects(Workpiece.class);
    }

    /**
     * Retrieves all workpieces in given range.
     *
     * @param first
     *            result
     * @param max
     *            amount of results
     * @return constrained list of results
     */
    public List<Workpiece> getAll(int first, int max) throws DAOException {
        return retrieveObjects("FROM Workpiece ORDER BY id ASC", first, max);
    }

    public Workpiece save(Workpiece workpiece) throws DAOException {
        storeObject(workpiece);
        return retrieveObject(Workpiece.class, workpiece.getId());
    }

    /**
     * The function remove() removes a workpiece from database.
     *
     * @param workpiece
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Workpiece workpiece) throws DAOException {
        if (workpiece.getId() != null) {
            removeObject(workpiece);
        }
    }

    /**
     * The function remove() removes a workpiece from database.
     *
     * @param id
     *            of the task to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Integer id) throws DAOException {
        removeObject(Workpiece.class, id);
    }

    public List<Workpiece> search(String query) {
        return retrieveObjects(query);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}
