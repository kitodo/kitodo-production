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

import org.kitodo.data.database.beans.LdapServer;
import org.kitodo.data.database.exceptions.DAOException;

public class LdapServerDAO extends BaseDAO<LdapServer> {

    @Override
    public LdapServer getById(Integer id) throws DAOException {
        LdapServer ldapServer = retrieveObject(LdapServer.class, id);
        if (ldapServer == null) {
            throw new DAOException("Object cannot be found in database");
        }
        return ldapServer;
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(LdapServer.class, id);
    }

    @Override
    public List<LdapServer> getAll() throws DAOException {
        return retrieveAllObjects(LdapServer.class);
    }

    @Override
    public List<LdapServer> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM LdapServer ORDER BY id ASC", offset, size);
    }

    @Override
    public List<LdapServer> getAllNotIndexed(int offset, int size) {
        throw new UnsupportedOperationException();
    }
}
