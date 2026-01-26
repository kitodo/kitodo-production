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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.PersistenceException;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.utils.Stopwatch;

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
                + ":title AND batchStep = true AND b.id = :batchId",
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
        return getByQuery("FROM Task WHERE process.id = :processId AND ordering < :orderingMin"
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
        return getByQuery("FROM Task WHERE process.id = :processId AND ordering > :ordering AND repeatOnCorrection = true",
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
            "FROM Task WHERE process.id = :processId AND ordering < :ordering" + " ORDER BY ordering DESC", parameters);
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
        Stopwatch stopwatch = new Stopwatch(process, "countTaskStatusForProcessAndItsAncestors");
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
            NativeQuery<Object[]> query = session.createNativeQuery(
                "SELECT t.processingStatus as status, COUNT(*) as count FROM task t, ("
                    + "    WITH RECURSIVE process_children(id) as ("
                    + "        (SELECT id FROM process WHERE id = " + process.getId() + ")"
                    + "        UNION ALL"
                    + "        (SELECT p1.id from process as p1, process_children as p2 WHERE p2.id = p1.parent_id)"
                    + "    ) SELECT id FROM process_children"
                    + ") as p WHERE t.process_id = p.id GROUP BY t.processingStatus;",
                Object[].class
            );
            query.addScalar("status", StandardBasicTypes.INTEGER);
            query.addScalar("count", StandardBasicTypes.INTEGER);
            
            for (Object[] row : query.list()) {
                TaskStatus status = TaskStatus.getStatusFromValue((int)row[0]);
                Integer count = (int)row[1];
                counts.put(status, count);
            }

            return stopwatch.stop(counts);
        } catch (PersistenceException e) {
            // catch any exceptions that might be thrown by internals of database connector
            // due to recursive query, which might not be supported by some databases
            throw new DAOException(e);
        }
    }

    /**
     * Loads task status counts for the given processes including all their descendant processes.
     *
     * <p>The result maps each root process ID to a count per TaskStatus.</p>
     *
     * @param processIds the IDs of the root processes to query
     * @return a map of process ID to task status counts
     */
    @SuppressWarnings("unchecked")
    public Map<Integer, EnumMap<TaskStatus, Integer>> loadTaskStatusCountsForProcesses(
            List<Integer> processIds) throws DAOException {
        Map<Integer, EnumMap<TaskStatus, Integer>> result = new HashMap<>();
        if (Objects.isNull(processIds) || processIds.isEmpty()) {
            return result;
        }
        Stopwatch stopwatch = new Stopwatch(this,"loadTaskStatusCountsForProcesses",
                "processIds", processIds.toString());
        try (Session session = HibernateUtil.getSession()) {
            NativeQuery<Object[]> query = session.createNativeQuery(
                    "WITH RECURSIVE process_tree (root_id, id) AS ("
                            + "  SELECT p.id, p.id FROM process p WHERE p.id IN (:ids) "
                            + "  UNION ALL "
                            + "  SELECT pt.root_id, c.id "
                            + "  FROM process c "
                            + "  JOIN process_tree pt ON c.parent_id = pt.id"
                            + ") "
                            + "SELECT "
                            + "  pt.root_id AS root_id, "
                            + "  t.processingStatus AS processingStatus, "
                            + "  COUNT(t.id) AS cnt "
                            + "FROM process_tree pt "
                            + "LEFT JOIN task t ON t.process_id = pt.id "
                            + "GROUP BY pt.root_id, t.processingStatus",
                    Object[].class
            );
            query.setParameter("ids", processIds);
            query.addScalar("root_id", StandardBasicTypes.INTEGER);
            query.addScalar("processingStatus", StandardBasicTypes.INTEGER);
            query.addScalar("cnt", StandardBasicTypes.INTEGER);

            List<Object[]> rows = query.list();
            for (Object[] row : rows) {
                Integer rootId = (Integer) row[0];
                Integer statusValue = (Integer) row[1];
                Integer count = (Integer) row[2];

                EnumMap<TaskStatus, Integer> map =
                        result.computeIfAbsent(rootId, id -> createEmptyStatusMap());
                if (Objects.nonNull(statusValue)) {
                    map.put(TaskStatus.getStatusFromValue(statusValue), count);
                }
            }
            return stopwatch.stop(result);
        } catch (PersistenceException e) {
            throw new DAOException(e);
        }
    }

    private static EnumMap<TaskStatus, Integer> createEmptyStatusMap() {
        EnumMap<TaskStatus, Integer> map = new EnumMap<>(TaskStatus.class);
        for (TaskStatus s : TaskStatus.values()) {
            map.put(s, 0);
        }
        return map;
    }
}
