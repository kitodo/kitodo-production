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

import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;

@SuppressWarnings("serial")
public class LdapGroupDAO extends BaseDAO<LdapGroup> {

    @Override
    public LdapGroup save(LdapGroup ldapGroup) throws DAOException {
        storeObject(ldapGroup);
        return retrieveObject(LdapGroup.class, ldapGroup.getId());
    }

    @Override
    public LdapGroup getById(Integer id) throws DAOException {
        LdapGroup result = retrieveObject(LdapGroup.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    @Override
    public void remove(Integer id) throws DAOException {
        removeObject(LdapGroup.class, id);
    }

    @Override
    public List<LdapGroup> getAll() throws DAOException {
        return retrieveAllObjects(LdapGroup.class);
    }

    @Override
    public List<LdapGroup> getAll(int offset, int size) throws DAOException {
        return retrieveObjects("FROM LdapGroup ORDER BY id ASC", offset, size);
    }
}
