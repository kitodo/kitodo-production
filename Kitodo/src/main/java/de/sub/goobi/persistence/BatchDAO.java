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

package de.sub.goobi.persistence;

import de.sub.goobi.beans.Batch;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * The class BatchDAO provides for to create, restore, update and delete {@link de.sub.goobi.beans.Batch} objects
 * by Hibernate.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class BatchDAO extends BaseDAO {
	private static final long serialVersionUID = 1L;

	/**
	 * The method deleteAll() removes all batches specified by the given IDs from the database
	 *
	 * @param ids IDs of batches to delete
	 * @throws DAOException if the current session can't be retrieved or an exception is thrown while performing
	 * 						the rollback
	 */
	public static void deleteAll(Iterable<Integer> ids) throws DAOException {
		for (Integer id : ids) {
			removeObj(Batch.class, id);
		}
	}

	/**
	 * The function read() retrieves a Batch identified by the given ID from the database
	 *
	 * @param id number of batch to load
	 * @return persisted batch
	 * @throws DAOException if a HibernateException is thrown
	 */
	public static Batch read(Integer id) throws DAOException {
		return (Batch) retrieveObj(Batch.class, id);
	}

	/**
	 * The function readAll() retrieves all batches Batches from the database
	 *
	 * @return all persisted batches
	 */
	@SuppressWarnings("unchecked")
	public static List<Batch> readAll() {
		Session session = Helper.getHibernateSession();
		Criteria criteria = session.createCriteria(Batch.class);
		return criteria.list();
	}

	/**
	 * The function reattach() reattaches a batch to a Hibernate session, i.e.
	 * for accessing properties that are lazy loaded.
	 *
	 * @param batch Batch to reattach
	 * @return the batch
	 */
	public static Batch reattach(Batch batch) {
		Session session = Helper.getHibernateSession();
		session.refresh(batch);
		return batch;
	}

	/**
	 * The method save() saves a batch to the database.
	 *
	 * @param batch batch to persist
	 * @throws DAOException if the current session can't be retrieved or an exception is thrown while performing
	 * 						the rollback
	 */
	public static void save(Batch batch) throws DAOException {
		storeObj(batch);
	}
}
