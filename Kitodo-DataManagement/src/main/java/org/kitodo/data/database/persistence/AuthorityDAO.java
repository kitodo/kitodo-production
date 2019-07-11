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

import java.util.Collections;
import java.util.List;

import org.kitodo.data.database.beans.Authority;
import org.kitodo.data.database.exceptions.DAOException;

public class AuthorityDAO extends BaseDAO<Authority> {

    @Override
    public Authority getById(Integer id) throws DAOException {
        Authority authority = retrieveObject(Authority.class, id);
        if (authority == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return authority;
    }

    @Override
    public List<Authority> getAll() throws DAOException {
        return retrieveAllObjects(Authority.class);
    }

    @Override
    public List<Authority> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM Authority ORDER BY id ASC", offset, size);
    }

    @Override
    public List<Authority> getAllNotIndexed(int offset, int size) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(Authority.class, id);
    }

    /**
     * Get authority by title.
     *
     * @param title
     *            of the searched authority
     * @return matching authority
     */
    public Authority getByTitle(String title) throws DAOException {
        List<Authority> authorities = getByQuery("FROM Authority WHERE title = :title", Collections.singletonMap("title", title));

        if (!authorities.isEmpty()) {
            return authorities.get(0);
        }
        throw new DAOException("Object cannot be found in database");
    }
}
