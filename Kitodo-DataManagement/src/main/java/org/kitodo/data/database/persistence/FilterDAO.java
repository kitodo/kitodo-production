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

import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.exceptions.DAOException;

/**
 * DAO class for Filter bean.
 */
public class FilterDAO extends BaseDAO<Filter> {

    private static final long serialVersionUID = 234210246673032251L;

    @Override
    public Filter getById(Integer id) throws DAOException {
        Filter result = retrieveObject(Filter.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Filter> getAll() throws DAOException {
        return retrieveAllObjects(Filter.class);
    }

    @Override
    public List<Filter> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Filter ORDER BY id ASC", offset, size);
    }

    @Override
    public List<Filter> getAllNotIndexed(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Filter WHERE indexAction = 'INDEX' OR indexAction IS NULL ORDER BY id ASC", offset,
                size);
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Filter.class, id);
    }
}
