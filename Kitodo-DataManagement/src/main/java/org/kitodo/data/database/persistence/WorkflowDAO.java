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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.database.exceptions.DAOException;

public class WorkflowDAO extends BaseDAO<Workflow> {

    private static final long serialVersionUID = 1913256950316879121L;

    @Override
    public Workflow getById(Integer id) throws DAOException {
        Workflow result = retrieveObject(Workflow.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Workflow> getAll() throws DAOException {
        return retrieveAllObjects(Workflow.class);
    }

    @Override
    public List<Workflow> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Workflow ORDER BY id", offset, size);
    }

    @Override
    public Workflow save(Workflow workflow) throws DAOException {
        storeObject(workflow);
        return retrieveObject(Workflow.class, workflow.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Workflow.class, id);
    }

    /**
     * Get workflow by given title and file name.
     *
     * @param title
     *            workflow title
     * @param fileName
     *            workflow file name
     * @return list of Workflow objects - expected is that list will have length 0
     *         or 1
     */
    public List<Workflow> getByTitleAndFile(String title, String fileName) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);
        parameters.put("fileName", fileName);
        return getByQuery("FROM Workflow WHERE title = :title AND fileName = :fileName", parameters);
    }

    /**
     * Get available workflows - available means that workflow is active and ready.
     * 
     * @return list of available Workflow objects
     */
    public List<Workflow> getAvailableWorkflows() {
        return getByQuery("FROM Workflow WHERE active = 1 AND ready = 1 ORDER BY title");
    }
}
