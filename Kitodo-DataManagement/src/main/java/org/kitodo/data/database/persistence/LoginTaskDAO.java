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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.kitodo.api.logintask.LoginTaskStatus;
import org.kitodo.api.logintask.LoginTaskType;
import org.kitodo.data.database.beans.LoginTask;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;

/**
 * DAO implementation for login tasks.
 */
public class LoginTaskDAO extends BaseDAO<LoginTask> {

    /**
     * Retrieves a BaseBean identified by the given id from the database.
     *
     * @param id
     *            of bean to load
     * @return persisted bean
     * @throws DAOException
     *             if a HibernateException is thrown
     */
    @Override
    public LoginTask getById(Integer id) throws DAOException {
        LoginTask loginTask = retrieveObject(LoginTask.class, id);
        if (Objects.isNull(loginTask)) {
            throw new DAOException("Unable to find login task with ID " + id + "!");
        }
        return loginTask;
    }

    /**
     * Retrieves all BaseBean objects from the database.
     *
     * @return all persisted beans
     */
    @Override
    public List<LoginTask> getAll() throws DAOException {
        return retrieveAllObjects(LoginTask.class);
    }

    /**
     * Retrieves all BaseBean objects in given range.
     *
     * @param offset
     *            result
     * @param size
     *            amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<LoginTask> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM LoginTask ORDER BY id ASC", offset, size);
    }

    /**
     * Retrieves all not indexed BaseBean objects in given range.
     *
     * @param offset
     *            result
     * @param size
     *            amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<LoginTask> getAllNotIndexed(int offset, int size) throws DAOException {
        return getAll();
    }

    /**
     * Removes BaseBean object specified by the given id from the database.
     *
     * @param id
     *            of bean to delete
     * @throws DAOException
     *             if the current session can't be retrieved or an exception is
     *             thrown while performing the rollback
     */
    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(LoginTask.class, id);
    }

    /**
     * Return all login tasks with a specific status for a user ordered by creation date.
     * 
     * @param user the user
     * @param status the login task status
     * @return the list of login tasks for a user of a given status
     */
    public List<LoginTask> getByUserAndStatus(User user, LoginTaskStatus status) {
        if (Objects.isNull(user) || Objects.isNull(status)) {
            return Collections.emptyList();
        }
        return getByQuery("FROM LoginTask WHERE user.id = :userId AND status = :status ORDER BY createdAt DESC", Map.of(
            "userId", user.getId(),
            "status", status
        ));
    }

    /**
     * Return all login tasks for a specific user, task type and task status ordered by creation date.
     * 
     * @param user the user
     * @param status the login task status
     * @param type the login task type
     * @return the list of login tasks for a user of a given status
     */
    public List<LoginTask> getByUserAndStatusAndType(User user, LoginTaskStatus status, LoginTaskType type) {
        if (Objects.isNull(user) || Objects.isNull(status) || Objects.isNull(type)) {
            return Collections.emptyList();
        }
        return getByQuery("FROM LoginTask WHERE user.id = :userId AND status = :status AND type = :type ORDER BY createdAt DESC", Map.of(
            "userId", user.getId(),
            "status", status,
            "type", type
        ));
    }

    /**
     * Return all login tasks for a specific user and task type ordered by creation date.
     * 
     * @param user the user
     * @param type the login task type
     * @return the list of login tasks for a user of a given status
     */
    public List<LoginTask> getByUserAndType(User user, LoginTaskType type) {
        if (Objects.isNull(user) || Objects.isNull(type)) {
            return Collections.emptyList();
        }
        return getByQuery("FROM LoginTask WHERE user.id = :userId AND type = :type ORDER BY createdAt DESC", Map.of(
            "userId", user.getId(),
            "type", type
        ));
    }


}
