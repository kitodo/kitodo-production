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

import org.kitodo.data.database.beans.ProcessProperty;
import org.kitodo.data.database.exceptions.DAOException;

public class ProcessPropertyDAO extends BaseDAO {

    private static final long serialVersionUID = 834210840673022251L;

    /**
     * Find process property object by id.
     *
     * @param id of searched object
     * @return result
     * @throws DAOException an exception that can be thrown from the underlying find() procedure failure.
     */
    public ProcessProperty find(Integer id) throws DAOException {
        ProcessProperty result = (ProcessProperty) retrieveObject(ProcessProperty.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all processes' properties from the database.
     *
     * @return all persisted processes' properties
     */
    @SuppressWarnings("unchecked")
    public List<ProcessProperty> findAll() {
        return retrieveAllObjects(ProcessProperty.class);
    }

    public ProcessProperty save(ProcessProperty processProperty) throws DAOException {
        storeObject(processProperty);
        return (ProcessProperty) retrieveObject(ProcessProperty.class, processProperty.getId());
    }

    /**
     * The function remove() removes a process property
     *
     * @param processProperty to be removed
     * @throws DAOException an exception that can be thrown from the underlying save() procedure upon database
     * 				failure.
     */
    public void remove(ProcessProperty processProperty) throws DAOException {
        removeObject(processProperty);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}
