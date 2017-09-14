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

import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;

public class TaskDAO extends BaseDAO<Task> {

    private static final long serialVersionUID = -2368830124391080142L;

    /**
     * Find task object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public Task find(Integer id) throws DAOException {
        Task result = retrieveObject(Task.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all tasks from the database.
     *
     * @return all persisted users
     */
    public List<Task> findAll() {
        return retrieveAllObjects(Task.class);
    }

    public Task save(Task task) throws DAOException {
        storeObject(task);
        return retrieveObject(Task.class, task.getId());
    }

    /**
     * The function remove() removes a task from database.
     *
     * @param task
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Task task) throws DAOException {
        if (task.getId() != null) {
            removeObject(task);
        }
    }

    /**
     * The function remove() removes a task from database.
     *
     * @param id
     *            of the task to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Integer id) throws DAOException {
        removeObject(Task.class, id);
    }

    public List<Task> search(String query) {
        return retrieveObjects(query);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }

    /**
     * Refresh task object after some changes.
     * 
     * @param task
     *            object
     */
    public void refresh(Task task) {
        refreshObject(task);
    }

    public void update(Task task) {
        updateObject(task);
    }

    public Task load(int id) throws DAOException {
        return loadObjects(Task.class, id);
    }

    /**
     * Get current tasks with exact title for batch with exact id.
     * 
     * @param title
     *            of task as String
     * @param batchId
     *            id of batch as Integer
     * @return list of Task objects
     */
    public List<Task> getCurrentTasksOfBatch(String title, Integer batchId) {
        return search("FROM Task AS t INNER JOIN t.process AS p INNER JOIN p.batches AS b WHERE t.title = '" + title
                + "' AND batchStep = 1 AND b.id = " + batchId);
    }

    /**
     * Get all tasks between two given ordering of tasks for given process id.
     * 
     * @param orderingMax
     *            as Integer
     * @param orderingMin
     *            as Integer
     * @param processId
     *            id of process for which tasks are searched as Integer
     * @return list of Task objects
     */
    public List<Task> getAllTasksInBetween(Integer orderingMax, Integer orderingMin, Integer processId) {
        return search("FROM Task WHERE process_id = " + processId + " AND ordering <= " + orderingMin
                + " AND ordering >= " + orderingMax + " ORDER BY ordering ASC");
    }

    /**
     * Get next tasks for problem solution for given process id.
     * 
     * @param ordering
     *            of Task for which it searches next ones as Integer
     * @param processId
     *            id of process for which tasks are searched as Integer
     * @return list of Task objects
     */
    public List<Task> getNextTasksForProblemSolution(Integer ordering, Integer processId) {
        return search(
                "FROM Task WHERE process_id = " + processId + " AND ordering > " + ordering + " AND priority = 10");
    }

    /**
     * Get previous tasks for problem solution for given process id.
     * 
     * @param ordering
     *            of Task for which it searches previous ones as Integer
     * @param processId
     *            id of process for which tasks are searched as Integer
     * @return list of Task objects
     */
    public List<Task> getPreviousTaskForProblemReporting(Integer ordering, Integer processId) {
        return search("FROM Task WHERE process_id = " + processId + " AND ordering < " + ordering
                + " ORDER BY ordering DESC");
    }
}
