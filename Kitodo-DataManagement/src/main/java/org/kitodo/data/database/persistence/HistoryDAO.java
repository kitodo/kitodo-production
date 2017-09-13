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

import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.exceptions.DAOException;

/**
 * HistorySAO for any kind of history event of a {@link Process}.
 */

public class HistoryDAO extends BaseDAO<History> {
    private static final long serialVersionUID = 991946176515032238L;

    /**
     * Find history object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public History find(Integer id) throws DAOException {
        History result = retrieveObject(History.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all histories from the database.
     *
     * @return all persisted histories
     */
    public List<History> findAll() {
        return retrieveAllObjects(History.class);
    }

    /**
     * Retrieves all histories in given range.
     *
     * @param first
     *            result
     * @param max
     *            amount of results
     * @return constrained list of results
     */
    public List<History> getAll(int first, int max) throws DAOException {
        return retrieveObjects("FROM History ORDER BY id ASC", first, max);
    }

    public History save(History history) throws DAOException {
        storeObject(history);
        return retrieveObject(History.class, history.getId());
    }

    /**
     * The function remove() removes a history from database.
     *
     * @param history
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(History history) throws DAOException {
        if (history.getId() != null) {
            removeObject(history);
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
        removeObject(History.class, id);
    }

    public List<History> search(String query) {
        return retrieveObjects(query);
    }

    /**
     * Counts the amount of results from the query.
     * 
     * @param query
     *            The given query.
     * @return the amount of results.
     * @throws DAOException
     *             if database throws an error.
     */
    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}
