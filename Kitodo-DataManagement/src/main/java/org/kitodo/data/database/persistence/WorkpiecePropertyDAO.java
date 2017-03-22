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

import org.kitodo.data.database.beans.WorkpieceProperty;
import org.kitodo.data.database.exceptions.DAOException;

public class WorkpiecePropertyDAO extends BaseDAO {

    private static final long serialVersionUID = 824210840673022251L;

    /**
     * Find workpiece property object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public WorkpieceProperty find(Integer id) throws DAOException {
        WorkpieceProperty result = (WorkpieceProperty) retrieveObject(WorkpieceProperty.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all workpieces' properties from the
     * database.
     *
     * @return all persisted workpieces' properties
     */
    @SuppressWarnings("unchecked")
    public List<WorkpieceProperty> findAll() {
        return retrieveAllObjects(WorkpieceProperty.class);
    }

    public WorkpieceProperty save(WorkpieceProperty workpieceProperty) throws DAOException {
        storeObject(workpieceProperty);
        return (WorkpieceProperty) retrieveObject(WorkpieceProperty.class, workpieceProperty.getId());
    }

    /**
     * The function remove() removes a workpiece property
     *
     * @param workpieceProperty
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(WorkpieceProperty workpieceProperty) throws DAOException {
        removeObject(workpieceProperty);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}
