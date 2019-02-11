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

    private static final long serialVersionUID = 4984545426562271217L;

    @Override
    public WorkflowCondition getById(Integer id) throws DAOException {
        WorkflowCondition result = retrieveObject(WorkflowCondition.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
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
    public WorkflowCondition save(WorkflowCondition workflowCondition) throws DAOException {
        storeObject(workflowCondition);
        return retrieveObject(WorkflowCondition.class, workflowCondition.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(WorkflowCondition.class, id);
    }
}
