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

import org.hibernate.Session;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.Helper;

/**
 * The class BatchDAO provides for to create, restore, update and delete
 * {@link org.kitodo.data.database.beans.Batch} objects by Hibernate.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class BatchDAO extends BaseDAO {
    private static final long serialVersionUID = 1L;

    /**
     * The function find() retrieves a Batch identified by the given ID from the
     * database.
     *
     * @param id
     *            of batch to load
     * @return persisted batch
     * @throws DAOException
     *             if a HibernateException is thrown
     */
    public Batch find(Integer id) throws DAOException {
        return (Batch) retrieveObject(Batch.class, id);
    }

    /**
     * The function findAll() retrieves all batches from the database.
     *
     * @return all persisted batches
     */
    @SuppressWarnings("unchecked")
    public List<Batch> findAll() {
        return retrieveAllObjects(Batch.class);
    }

    /**
     * The method save() saves a batch to the database.
     *
     * @param batch
     *            object to persist
     * @throws DAOException
     *             if the current session can't be retrieved or an exception is
     *             thrown while performing the rollback
     */
    public void save(Batch batch) throws DAOException {
        storeObject(batch);
    }

    /**
     * Search Batch objects in database by given query.
     * 
     * @param query
     *            as String
     * @return list of Batch objects
     */
    @SuppressWarnings("unchecked")
    public List<Batch> search(String query) throws DAOException {
        return retrieveObjects(query);
    }

    /**
     * The method remove() removes batch specified by the given ID from the
     * database.
     *
     * @param id
     *            of batches to delete
     * @throws DAOException
     *             if the current session can't be retrieved or an exception is
     *             thrown while performing the rollback
     */
    public void remove(Integer id) throws DAOException {
        removeObject(Batch.class, id);
    }

    public void remove(Batch batch) throws DAOException {
        removeObject(batch);
    }

    /**
     * Count all rows in database.
     * 
     * @param query
     *            for counting objects
     * @return amount of rows in database according to given query
     */
    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
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

    /**
     * The function reattach() reattaches a batch to a Hibernate session, i.e. for
     * accessing properties that are lazy loaded.
     *
     * @param batch
     *            object to reattach
     * @return the batch
     */
    public static Batch reattach(Batch batch) {
        Session session = Helper.getHibernateSession();
        session.refresh(batch);
        return batch;
    }
}
