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

import org.kitodo.data.database.beans.Authorization;
import org.kitodo.data.database.exceptions.DAOException;

import java.util.List;

public class AuthorizationDAO extends BaseDAO<Authorization> {

    private static final long serialVersionUID = 4987176626562271217L;

    @Override
    public Authorization getById(Integer id) throws DAOException {
        Authorization result = retrieveObject(Authorization.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Authorization> getAll() {
        return retrieveAllObjects(Authorization.class);
    }

    @Override
    public List<Authorization> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Authorization ORDER BY id ASC", offset, size);
    }

    @Override
    public Authorization save(Authorization authorization) throws DAOException {
        storeObject(authorization);
        return retrieveObject(Authorization.class, authorization.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Authorization.class, id);
    }

    /**
     * Refresh Authorization object after some changes.
     *
     * @param authorization
     *            object
     */
    public void refresh(Authorization authorization) {
        refreshObject(authorization);
    }
}
