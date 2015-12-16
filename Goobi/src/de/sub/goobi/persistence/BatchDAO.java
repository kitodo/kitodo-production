/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package de.sub.goobi.persistence;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import de.sub.goobi.beans.Batch;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;

/**
 * The class BatchDAO provides for to create, restore, update and delete
 * {@link de.sub.goobi.beans.Batch} objects by Hibernate.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class BatchDAO extends BaseDAO {
	private static final long serialVersionUID = 1L;

	/**
	 * The method deleteAll() removes all batches specified by the given IDs
	 * from the database
	 * 
	 * @param ids
	 *            IDs of batches to delete
	 * @throws DAOException
	 *             if the current session can't be retrieved or an exception is
	 *             thrown while performing the rollback
	 */
	public static void deleteAll(Iterable<Integer> ids) throws DAOException {
		for (Integer id : ids)
			removeObj(Batch.class, id);
	}

	/**
	 * The function read() retrieves a Batch identified by the given ID from the
	 * database
	 * 
	 * @param id
	 *            number of batch to load
	 * @return persisted batch
	 * @throws DAOException
	 *             if a HibernateException is thrown
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
	 * @param batch
	 *            Batch to reattach
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
	 * @param batch
	 *            batch to persist
	 * @throws DAOException
	 *             if the current session can't be retrieved or an exception is
	 *             thrown while performing the rollback
	 */
	public static void save(Batch batch) throws DAOException {
		storeObj(batch);
	}
}
