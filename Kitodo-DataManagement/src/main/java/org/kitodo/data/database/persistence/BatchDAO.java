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

import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.exceptions.DAOException;

/**
 * The class BatchDAO provides for to create, restore, update and delete
 * {@link org.kitodo.data.database.beans.Batch} objects by Hibernate.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class BatchDAO extends BaseDAO<Batch> {
    private static final long serialVersionUID = 1L;

    @Override
    public Batch getById(Integer id) throws DAOException {
        Batch result = retrieveObject(Batch.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Batch> getAll() throws DAOException {
        return retrieveAllObjects(Batch.class);
    }

    @Override
    public List<Batch> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Batch ORDER BY id ASC", offset, size);
    }

    @Override
    public List<Batch> getAllNotIndexed(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Batch WHERE indexAction = 'INDEX' OR indexAction IS NULL ORDER BY id ASC", offset,
            size);
    }

    @Override
    public Batch save(Batch batch) throws DAOException {
        storeObject(batch);
        return retrieveObject(Batch.class, batch.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Batch.class, id);
    }

    /**
     * The method removeAll() removes all batches specified by the given IDs from
     * the database.
     *
     * @param ids
     *            of batches to delete
     * @throws DAOException
     *             if the current session can't be retrieved or an exception is
     *             thrown while performing the rollback
     */
    public void removeAll(Iterable<Integer> ids) throws DAOException {
        for (Integer id : ids) {
            removeObject(Batch.class, id);
        }
    }
}
