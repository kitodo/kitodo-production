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
import java.util.Objects;

import javax.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;

public class TaskDAO extends BaseDAO<Task> {

    private static final String KEY_PROCESS_ID = "processId";

    @Override
    public Task getById(Integer id) throws DAOException {
        Task task = retrieveObject(Task.class, id);
        if (task == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return task;
    }

    @Override
    public List<Task> getAll() throws DAOException {
        return retrieveAllObjects(Task.class);
    }

    @Override
    public List<Task> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Task WHERE " + getDateFilter("processingBegin") + " ORDER BY id ASC", offset,
            size);
    }

    @Override
    public List<Task> getAllNotIndexed(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Task WHERE " + getDateFilter("processingBegin")
                + " AND (indexAction = 'INDEX' OR indexAction IS NULL) ORDER BY id ASC",
            offset,
            size);
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Task.class, id);
    }

    public void update(Task task) {
        updateObject(task);
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
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("title", title);
        parameters.put("batchId", batchId);
        return getByQuery("SELECT t FROM Task AS t INNER JOIN t.process AS p INNER JOIN p.batches AS b WHERE t.title = "
                + ":title AND batchStep = 1 AND b.id = :batchId",
            parameters);
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
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("orderingMax", orderingMax);
        parameters.put("orderingMin", orderingMin);
        parameters.put(KEY_PROCESS_ID, processId);
        return getByQuery("FROM Task WHERE process_id = :processId AND ordering < :orderingMin"
                + " AND ordering > :orderingMax ORDER BY ordering ASC",
            parameters);
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
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ordering", ordering);
        parameters.put(KEY_PROCESS_ID, processId);
        return getByQuery("FROM Task WHERE process_id = :processId AND ordering > :ordering AND repeatOnCorrection = 1",
            parameters);
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
    public List<Task> getPreviousTasksForProblemReporting(Integer ordering, Integer processId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ordering", ordering);
        parameters.put(KEY_PROCESS_ID, processId);
        return getByQuery(
            "FROM Task WHERE process_id = :processId AND ordering < :ordering" + " ORDER BY ordering DESC", parameters);
    }

    /**
     * Counts how many tasks have a certain status for the provided process and all its ancestor processes.
     * 
     * <p>The counts are used to calculate the process progress status</p>
     * 
     * @param process the process to be queried for tasks and their status counts
     * @return a count for each TaskStatus 
     */
    @SuppressWarnings("unchecked")
    public Map<TaskStatus, Integer> countTaskStatusForProcessAndItsAncestors(Process process) throws DAOException {
        if (Objects.isNull(process)) {
            throw new DAOException("can not count task status for process that is null");
        }
        if (Objects.isNull(process.getId())) {
            throw new DAOException("can not count task status for process that has id of null");
        }
        
        // initialize counts
        Map<TaskStatus, Integer> counts = new HashMap<>();
        counts.put(TaskStatus.OPEN, 0);
        counts.put(TaskStatus.INWORK, 0);
        counts.put(TaskStatus.LOCKED, 0);
        counts.put(TaskStatus.DONE, 0);

        // perform query and read data
        try (Session session = HibernateUtil.getSession()) {
            // do not use hibernate query language, which does not support recursive queries, use native SQL instead
            // do not use query parameter with recursive query, which works with MySQL and MariaDB but not h2 database
            NativeQuery<Object[]> query = session.createSQLQuery(
                "SELECT t.processingStatus as status, COUNT(*) as count FROM task t, ("
                    + "    WITH RECURSIVE process_children(id) as ("
                    + "        (SELECT id FROM process WHERE id = " + process.getId() + ")"
                    + "        UNION ALL"
                    + "        (SELECT p1.id from process as p1, process_children as p2 WHERE p2.id = p1.parent_id)"
                    + "    ) SELECT id FROM process_children"
                    + ") as p WHERE t.process_id = p.id GROUP BY t.processingStatus;"
            );
            query.addScalar("status", StandardBasicTypes.INTEGER);
            query.addScalar("count", StandardBasicTypes.INTEGER);
            
            for (Object[] row : query.list()) {
                TaskStatus status = TaskStatus.getStatusFromValue((int)row[0]);
                Integer count = (int)row[1];
                counts.put(status, count);
            }

            return counts;
        } catch (PersistenceException e) {
            // catch any exceptions that might be thrown by internals of database connector
            // due to recursive query, which might not be supported by some databases
            throw new DAOException(e);
        }
    }
}
