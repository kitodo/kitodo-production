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

import org.kitodo.data.database.beans.SearchField;
import org.kitodo.data.database.exceptions.DAOException;

public class SearchFieldDAO extends BaseDAO<SearchField> {
    /**
     * Retrieves a BaseBean identified by the given searchFieldId from the database.
     *
     * @param searchFieldId of bean to load
     * @return persisted bean
     * @throws DAOException if a HibernateException is thrown
     */
    @Override
    public SearchField getById(Integer searchFieldId) throws DAOException {
        return retrieveObject(SearchField.class, searchFieldId);
    }

    /**
     * Retrieves all BaseBean objects from the database.
     *
     * @return all persisted beans
     */
    @Override
    public List<SearchField> getAll() throws DAOException {
        return retrieveAllObjects(SearchField.class);
    }

    /**
     * Retrieves all BaseBean objects in given range.
     *
     * @param offset result
     * @param size   amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<SearchField> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM searchfield ORDER BY ID ASC", offset, size);
    }

    /**
     * Retrieves all not indexed BaseBean objects in given range.
     *
     * @param offset result
     * @param size   amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<SearchField> getAllNotIndexed(int offset, int size) throws DAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes BaseBean object specified by the given searchFieldId from the database.
     *
     * @param searchFieldId of bean to delete
     * @throws DAOException if the current session can't be retrieved or an exception is
     *                      thrown while performing the rollback
     */
    @Override
    public void remove(Integer searchFieldId) throws DAOException {
        removeObject(SearchField.class, searchFieldId);
    }
}
