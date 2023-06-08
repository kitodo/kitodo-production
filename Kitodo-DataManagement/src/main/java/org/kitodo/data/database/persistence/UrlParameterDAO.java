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

import org.kitodo.data.database.beans.UrlParameter;
import org.kitodo.data.database.exceptions.DAOException;

public class UrlParameterDAO extends BaseDAO<UrlParameter> {

    /**
     * Retrieves a UrlParameter identified by the given urlParameterId from the database.
     * @param urlParameterId
     *            of bean to load
     * @return persisted bean
     * @throws DAOException if a HibernateException is thrown
     */
    @Override
    public UrlParameter getById(Integer urlParameterId) throws DAOException {
        return retrieveObject(UrlParameter.class, urlParameterId);
    }

    /**
     * Retrieves all UrlParameter objects from the database.
     *
     * @return all persisted beans
     * @throws DAOException if a HibernateException is thrown
     */
    @Override
    public List<UrlParameter> getAll() throws DAOException {
        return retrieveAllObjects(UrlParameter.class);
    }

    /**
     * Retrieves all UrlParameter objects in the given range.
     * @param offset
     *            result
     * @param size
     *            amount of results
     * @return constrained list of persisted beans
     * @throws DAOException if a HibernateException is thrown
     */
    @Override
    public List<UrlParameter> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM UrlParameter ORDER BY ID ASC", offset, size);
    }

    /**
     * Retrieves all not indexed UrlParameter objects in given range.
     * @param offset
     *            result
     * @param size
     *            amount of results
     * @return constrained list of persisted beans
     * @throws DAOException if a HibernateException is thrown
     */
    @Override
    public List<UrlParameter> getAllNotIndexed(int offset, int size) throws DAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes a UrlParameter object specified by the given urlParameterId from the database.
     * @param urlParameterId
     *            of bean to delete
     * @throws DAOException if a HibernateException is thrown
     */
    @Override
    public void remove(Integer urlParameterId) throws DAOException {
        removeObject(UrlParameter.class, urlParameterId);
    }
}
