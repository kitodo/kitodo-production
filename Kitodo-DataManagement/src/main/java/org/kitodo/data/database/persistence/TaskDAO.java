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

import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;

public class TaskDAO extends BaseDAO<Task> {

    private static final long serialVersionUID = -2368830124391080142L;
    private static final String KEY_PROCESS_ID = "processId";
    private static final String KEY_PROJECT_ID = "projectId";
    private static final String KEY_PROCESSING_STATUS = "processingStatus";

    @Override
    public Task getById(Integer id) throws DAOException {
        Task result = retrieveObject(Task.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Task> getAll() throws DAOException {
        return retrieveAllObjects(Task.class);
    }

    @Override
    public List<Task> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Task ORDER BY id ASC", offset, size);
    }

    @Override
    public Task save(Task task) throws DAOException {
        storeObject(task);
        return retrieveObject(Task.class, task.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Task.class, id);
    }

    public void update(Task task) {
        updateObject(task);
    }

    public Task load(int id) throws DAOException {
        return loadObject(Task.class, id);
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
                + ":title AND batchStep = 1 AND b.id = :batchId", parameters);
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
                + " AND ordering > :orderingMax ORDER BY ordering ASC", parameters);
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
        return getByQuery(
                "FROM Task WHERE process_id = :processId AND ordering > :ordering AND priority = 10", parameters);
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
        return getByQuery("FROM Task WHERE process_id = :processId AND ordering < :ordering"
                + " ORDER BY ordering DESC", parameters);
    }

    /**
     * Get tasks for processes for given project id and ordered by
     * ordering.
     *
     * @param projectId
     *            as Integer
     * @return list of tasks
     */
    public List<Task> getTasksForProcessesForProjectIdOrderByOrdering(Integer projectId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(KEY_PROJECT_ID, projectId);
        return getByQuery(
                "SELECT t FROM Task AS t INNER JOIN t.process AS p INNER JOIN p.project AS pr "
                        + "WITH pr.id = :projectId GROUP BY t.title ORDER BY t.ordering", parameters);
    }

    /**
     * Get size of tasks for processes for given project id and ordered
     * by ordering.
     * 
     * @param projectId
     *            as Integer
     * @return list of tasks
     */
    public List<Long> getSizeOfTasksForProcessesForProjectIdOrderByOrdering(Integer projectId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(KEY_PROJECT_ID, projectId);
        return getCount("SELECT COUNT(t.id) FROM Task AS t INNER JOIN t.process AS p INNER JOIN p.project AS pr "
                        + "WITH pr.id = :projectId GROUP BY t.title ORDER BY t.ordering", parameters);
    }

    /**
     * Get average ordering of tasks for processes for given project id
     * and ordered by ordering.
     *
     * @param projectId
     *            as Integer
     * @return list of tasks
     */
    public List<Double> getAverageOrderingOfTasksForProcessesForProjectIdOrderByOrdering(Integer projectId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(KEY_PROJECT_ID, projectId);
        return getAverage("SELECT AVG(t.ordering) FROM Task AS t INNER JOIN t.process AS p INNER JOIN p.project AS pr "
                        + "WITH pr.id = :projectId GROUP BY t.title ORDER BY t.ordering", parameters);
    }

    /**
     * Get tasks for exact processing status for non template processes for given
     * project id and ordered by ordering.
     *
     * @param projectId
     *            as Integer
     * @return list of tasks
     */
    public List<Task> getTasksWithProcessingStatusForProcessesForProjectIdOrderByOrdering(
            Integer processingStatus, Integer projectId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(KEY_PROCESSING_STATUS, processingStatus);
        parameters.put(KEY_PROJECT_ID, projectId);
        return getByQuery(
                "SELECT t FROM Task AS t INNER JOIN t.process AS p INNER JOIN p.project AS pr "
                        + "WITH pr.id = :projectId WHERE t.processingStatus = :processingStatus "
                        + "GROUP BY t.title ORDER BY t.ordering", parameters);
    }

    /**
     * Get size of tasks for exact processing status for processes for
     * given project id and ordered by ordering.
     *
     * @param projectId
     *            as Integer
     * @return list of tasks sizes
     */
    public List<Long> getSizeOfTasksWithProcessingStatusForProcessesForProjectIdOrderByOrdering(
            Integer processingStatus, Integer projectId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(KEY_PROJECT_ID, projectId);
        parameters.put(KEY_PROCESSING_STATUS, processingStatus);
        return getCount("SELECT COUNT(t.id) FROM Task AS t INNER JOIN t.process AS p INNER JOIN p.project AS pr "
                        + "WITH pr.id = :projectId WHERE t.processingStatus = :processingStatus "
                        + "GROUP BY t.title ORDER BY t.ordering", parameters);
    }

    /**
     * Get average ordering of tasks for exact processing status for non template
     * processes for given project id and ordered by ordering.
     *
     * @param projectId
     *            as Integer
     * @return list of average tasks' ordering
     */
    public List<Long> getAmountOfImagesForTasksWithProcessingStatusForProcessesForProjectIdOrderByOrdering(
            Integer processingStatus, Integer projectId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(KEY_PROJECT_ID, projectId);
        parameters.put(KEY_PROCESSING_STATUS, processingStatus);
        return getSum(
                "SELECT SUM(p.sortHelperImages) FROM Task AS t INNER JOIN t.process AS p INNER JOIN p.project AS pr "
                        + "WITH pr.id = :projectId WHERE t.processingStatus = :processingStatus "
                        + "GROUP BY t.title ORDER BY t.ordering", parameters);
    }
}
