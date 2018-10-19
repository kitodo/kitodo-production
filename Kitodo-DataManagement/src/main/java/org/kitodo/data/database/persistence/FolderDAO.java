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

import org.kitodo.data.database.beans.Folder;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.NotImplementedException;

public class FolderDAO extends BaseDAO<Folder> {
    private static final long serialVersionUID = -5506252462891480484L;

    @Override
    public Folder getById(Integer id) throws DAOException {
        Folder result = retrieveObject(Folder.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Folder> getAll() throws DAOException {
        return retrieveAllObjects(Folder.class);
    }

    @Override
    public List<Folder> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Folder ORDER BY id ASC", offset, size);
    }

    @Override
    public List<Folder> getAllNotIndexed(int offset, int size) {
        throw  new NotImplementedException();
    }

    @Override
    public Folder save(Folder folder) throws DAOException {
        storeObject(folder);
        return retrieveObject(Folder.class, folder.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Folder.class, id);
    }
}
