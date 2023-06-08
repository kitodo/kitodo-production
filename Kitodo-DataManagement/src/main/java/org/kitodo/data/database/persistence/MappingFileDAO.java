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

import org.kitodo.data.database.beans.MappingFile;
import org.kitodo.data.database.exceptions.DAOException;

public class MappingFileDAO extends BaseDAO<MappingFile> {
    /**
     * Retrieves a BaseBean identified by the given id from the database.
     *
     * @param id of bean to load
     * @return persisted bean
     * @throws DAOException if a HibernateException is thrown
     */
    @Override
    public MappingFile getById(Integer id) throws DAOException {
        MappingFile mappingFile = retrieveObject(MappingFile.class, id);
        if (Objects.isNull(mappingFile)) {
            throw new DAOException("Unable to find mapping file object with ID " + id + "!");
        }
        return mappingFile;
    }

    /**
     * Retrieves all BaseBean objects from the database.
     *
     * @return all persisted beans
     */
    @Override
    public List<MappingFile> getAll() throws DAOException {
        return retrieveAllObjects(MappingFile.class);
    }

    /**
     * Retrieves all BaseBean objects in given range.
     *
     * @param offset result
     * @param size   amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<MappingFile> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM mappingfile ORDER BY id ASC", offset, size);
    }

    /**
     * Retrieves all not indexed BaseBean objects in given range.
     *
     * @param offset result
     * @param size   amount of results
     * @return constrained list of persisted beans
     */
    @Override
    public List<MappingFile> getAllNotIndexed(int offset, int size) throws DAOException {
        return getAll();
    }

    /**
     * Removes BaseBean object specified by the given id from the database.
     *
     * @param mappingFileId of bean to delete
     * @throws DAOException if the current session can't be retrieved or an exception is
     *                      thrown while performing the rollback
     */
    @Override
    public void remove(Integer mappingFileId) throws DAOException {
        removeObject(MappingFile.class, mappingFileId);
    }
}
