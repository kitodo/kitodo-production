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

import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;

public class DocketDAO extends BaseDAO<Docket> {

    private static final long serialVersionUID = 1913256950316879121L;

    /**
     * Find docket object by id.
     *
     * @param id
     *            of searched object
     * @return result
     * @throws DAOException
     *             an exception that can be thrown from the underlying find()
     *             procedure failure.
     */
    public Docket find(Integer id) throws DAOException {
        Docket result = retrieveObject(Docket.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * The function findAll() retrieves all docket from the database.
     *
     * @return all persisted dockets
     */
    public List<Docket> findAll() {
        return retrieveAllObjects(Docket.class);
    }

    public Docket save(Docket docket) throws DAOException {
        storeObject(docket);
        return retrieveObject(Docket.class, docket.getId());
    }

    /**
     * The function remove() removes a docket from database.
     *
     * @param docket
     *            to be removed
     * @throws DAOException
     *             an exception that can be thrown from the underlying save()
     *             procedure upon database failure.
     */
    public void remove(Docket docket) throws DAOException {
        if (docket.getId() != null) {
            removeObject(docket);
        }
    }

    public void remove(Integer id) throws DAOException {
        removeObject(Docket.class, id);
    }

    public List<Docket> search(String query) throws DAOException {
        return retrieveObjects(query);
    }

    public Long count(String query) throws DAOException {
        return retrieveAmount(query);
    }
}
