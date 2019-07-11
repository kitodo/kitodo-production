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

import org.kitodo.data.database.beans.WorkflowCondition;
import org.kitodo.data.database.exceptions.DAOException;

public class WorkflowConditionDAO extends BaseDAO<WorkflowCondition> {

    @Override
    public WorkflowCondition getById(Integer id) throws DAOException {
        WorkflowCondition workflowCondition = retrieveObject(WorkflowCondition.class, id);
        if (workflowCondition == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return workflowCondition;
    }

    @Override
    public List<WorkflowCondition> getAll() throws DAOException {
        return retrieveAllObjects(WorkflowCondition.class);
    }

    @Override
    public List<WorkflowCondition> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM WorkflowCondition ORDER BY id ASC", offset, size);
    }

    @Override
    public List<WorkflowCondition> getAllNotIndexed(int offset, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(WorkflowCondition.class, id);
    }
}
