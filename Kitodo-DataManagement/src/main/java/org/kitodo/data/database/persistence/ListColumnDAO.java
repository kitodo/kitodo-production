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
import java.util.Objects;

import org.kitodo.data.database.beans.ListColumn;
import org.kitodo.data.database.exceptions.DAOException;

public class ListColumnDAO extends BaseDAO<ListColumn> {
    @Override
    public ListColumn getById(Integer id) throws DAOException {
        ListColumn column = retrieveObject(ListColumn.class, id);
        if (Objects.isNull(column)) {
            throw new DAOException("ListColumn cannot be found in database.");
        }
        return column;
    }

    @Override
    public List<ListColumn> getAll() throws DAOException {
        return retrieveAllObjects(ListColumn.class);
    }

    @Override
    public List<ListColumn> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM ListColumn ORDER BY id ASC", offset, size);
    }

    /**
     * Retrieve and return list of standard list columns.
     *
     * @return list of standard list columns
     */
    public List<ListColumn> getAllStandard() {
        return getByQuery("FROM ListColumn WHERE custom = 0");
    }

    /**
     * Retrieve and return list of custom list columns.
     *
     * @return list of custom list columns
     */
    public List<ListColumn> getAllCustom() {
        return getByQuery("FROM ListColumn WHERE custom = 1");
    }

    @Override
    public List<ListColumn> getAllNotIndexed(int offset, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListColumn save(ListColumn column) throws DAOException {
        storeObject(column);
        return retrieveObject(ListColumn.class, column.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(ListColumn.class, id);
    }
}
