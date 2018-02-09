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

import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.exceptions.DAOException;

public class AuthorityDAO extends BaseDAO<Authority> {

    private static final long serialVersionUID = 4987176626562271217L;

    @Override
    public Authority getById(Integer id) throws DAOException {
        Authority result = retrieveObject(Authority.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public List<Authority> getAll() {
        return retrieveAllObjects(Authority.class);
    }

    @Override
    public List<Authority> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Authority ORDER BY id ASC", offset, size);
    }

    @Override
    public Authority save(Authority authority) throws DAOException {
        storeObject(authority);
        return retrieveObject(Authority.class, authority.getId());
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Authority.class, id);
    }

    /**
     * Refresh Authority object after some changes.
     *
     * @param authority
     *            object
     */
    public void refresh(Authority authority) {
        refreshObject(authority);
    }
}
