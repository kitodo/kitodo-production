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

import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.exceptions.DAOException;

public class ImportConfigurationDAO extends BaseDAO<ImportConfiguration> {
    /**
     * Retrieves a ImportConfiguration identified by the given id from the database.
     *
     * @param id of ImportConfiguration to load
     * @return persisted bean
     * @throws DAOException if a HibernateException is thrown
     */
    @Override
    public ImportConfiguration getById(Integer id) throws DAOException {
        ImportConfiguration importConfiguration = retrieveObject(ImportConfiguration.class, id);
        if (Objects.isNull(importConfiguration)) {
            throw new DAOException("Unable to find import configuration object with ID " + id + "!");
        }
        return importConfiguration;
    }

    /**
     * Retrieves all ImportConfiguration objects from the database.
     *
     * @return all persisted beans
     */
    @Override
    public List<ImportConfiguration> getAll() throws DAOException {
        return retrieveAllObjects(ImportConfiguration.class);
    }

    /**
     * Retrieves all ImportConfiguration objects in given range.
     *
     * @param offset result
     * @param size   amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<ImportConfiguration> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM importconfiguration ORDER BY id ASC", offset, size);
    }

    /**
     * Retrieves all not indexed ImportConfiguration objects in given range.
     *
     * @param offset result
     * @param size   amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<ImportConfiguration> getAllNotIndexed(int offset, int size) throws DAOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes ImportConfiguration object specified by the given importConfigurationId from the database.
     *
     * @param importConfigurationId of bean to delete
     * @throws DAOException if the current session can't be retrieved or an exception is
     *                      thrown while performing the rollback
     */
    @Override
    public void remove(Integer importConfigurationId) throws DAOException {
        removeObject(ImportConfiguration.class, importConfigurationId);
    }
}
