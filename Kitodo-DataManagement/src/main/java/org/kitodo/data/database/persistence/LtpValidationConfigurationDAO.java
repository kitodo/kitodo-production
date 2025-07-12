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

import org.kitodo.data.database.beans.LtpValidationConfiguration;
import org.kitodo.data.database.exceptions.DAOException;

/**
 * DAO implementation for LTP validation configurations.
 */
public class LtpValidationConfigurationDAO extends BaseDAO<LtpValidationConfiguration> {
    /**
     * Retrieves a BaseBean identified by the given id from the database.
     *
     * @param id
     *            of bean to load
     * @return persisted bean
     * @throws DAOException
     *             if a HibernateException is thrown
     */
    @Override
    public LtpValidationConfiguration getById(Integer id) throws DAOException {
        LtpValidationConfiguration mappingFile = retrieveObject(LtpValidationConfiguration.class, id);
        if (Objects.isNull(mappingFile)) {
            throw new DAOException("Unable to find ltp validation configuration object with ID " + id + "!");
        }
        return mappingFile;
    }

    /**
     * Retrieves all BaseBean objects from the database.
     *
     * @return all persisted beans
     */
    @Override
    public List<LtpValidationConfiguration> getAll() throws DAOException {
        return retrieveAllObjects(LtpValidationConfiguration.class);
    }

    /**
     * Retrieves all BaseBean objects in given range.
     *
     * @param offset
     *            result
     * @param size
     *            amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<LtpValidationConfiguration> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM LtpValidationConfiguration ORDER BY id ASC", offset, size);
    }

    /**
     * Retrieves all not indexed BaseBean objects in given range.
     *
     * @param offset
     *            result
     * @param size
     *            amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<LtpValidationConfiguration> getAllNotIndexed(int offset, int size) throws DAOException {
        return getAll();
    }

    /**
     * Removes BaseBean object specified by the given id from the database.
     *
     * @param id
     *            of bean to delete
     * @throws DAOException
     *             if the current session can't be retrieved or an exception is
     *             thrown while performing the rollback
     */
    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(LtpValidationConfiguration.class, id);
    }
}
