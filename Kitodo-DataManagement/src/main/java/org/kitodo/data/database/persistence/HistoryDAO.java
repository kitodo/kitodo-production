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

    @Override
    public History getById(Integer id) throws DAOException {
        History result = retrieveObject(History.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<History> getAll() {
        return retrieveAllObjects(History.class);
    }

    @Override
    public List<History> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM History ORDER BY id ASC", offset, size);
    }

    @Override
    public History save(History history) throws DAOException {
        storeObject(history);
        return retrieveObject(History.class, history.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(History.class, id);
    }
}
