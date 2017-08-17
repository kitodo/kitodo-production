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

import javax.persistence.Table;

import org.hibernate.Query;
import org.hibernate.Session;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.Helper;

public class TaskDAO extends BaseDAO {

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
        Task result = (Task) retrieveObject(Task.class, id);
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
    @SuppressWarnings("unchecked")
    public List<Task> findAll() {
        return retrieveAllObjects(Task.class);
    }

    public Task save(Task task) throws DAOException {
        storeObject(task);
        return (Task) retrieveObject(Task.class, task.getId());
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
        @SuppressWarnings("unused")
        Task task = (Task) retrieveObject(Task.class, id);
        removeObject(Task.class, id);
    }

    @SuppressWarnings("unchecked")
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
        Object o = task;
        updateObject(o);
    }

    public Task load(int id) throws DAOException {
        return (Task) loadObjects(Task.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<String> getTaskTitlesDistict() {
        Session session = Helper.getHibernateSession();
        Query query = session.createSQLQuery(
                "select distinct title from " + Task.class.getAnnotation(Table.class).name() + " order by title");
        return query.list();
    }

    /**
     * Get open tasks for current user.
     *
     * @param userId
     *            the user id for the current user.
     * @return a list of tasks
     */
    public List<Task> getOpenTasksForCurrentUser(int userId) {
        return search("from Task where processingStatus = '2' AND user_id =" + userId);
    }

    /**
     * get open tasks without correction for current user.
     *
     * @param userId
     *            the user id for the current user.
     * @return a list of tasks
     */
    public List<Task> getOpenTasksWithoutCorrectionForCurrentUser(Integer userId) {
        return search("from Task where processingStatus = '2' AND user_id =" + userId + " AND priority = '10'");
    }

    /**
     * Get open not automatic tasks for current user
     * 
     * @param userId
     *            the user id for the current user.
     * @return a list of tasks
     */
    public List<Task> getOpenNotAutomaticTasksForCurrentUser(Integer userId) {
        return search("from Task where processingStatus = '2' AND user_id =" + userId + " AND typeAutomatic = 'false'");
    }

    /**
     * Get open not automatic tasks without correction for current user with
     * filter.
     * 
     * @param userId
     *            the user id for the current user.
     * @return a list of tasks
     */
    public List<Task> getOpenNotAutomaticTasksWithoutCorrectionForCurrentUser(Integer userId) {
        return search("from Task where processingStatus = '2' AND user_id =" + userId
                + " AND priority = '10' AND typeAutomatic = 'false'");
    }
}
