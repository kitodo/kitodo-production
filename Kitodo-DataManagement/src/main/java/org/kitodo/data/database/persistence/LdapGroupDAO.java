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
public class LdapGroupDAO extends BaseDAO {

    public LdapGroup save(LdapGroup ldapGroup) throws DAOException {
        storeObject(ldapGroup);
        return (LdapGroup) retrieveObject(LdapGroup.class, ldapGroup.getId());
    }

    /**
     * Find LDAP group object by id.
     *
     * @param id
     *            of LDAP group
     * @return LDAP group
     * @throws DAOException
     *             hibernate
     */
    public LdapGroup find(Integer id) throws DAOException {
        LdapGroup result = (LdapGroup) retrieveObject(LdapGroup.class, id);
        if (result == null) {
            throw new DAOException("Object can not be found in database");
        }
        return result;
    }

    /**
     * Remove LDAP group.
     *
     * @param ldapGroup
     *            object
     * @throws DAOException
     *             hibernate
     */
    public void remove(LdapGroup ldapGroup) throws DAOException {
        if (ldapGroup.getId() != null) {
            removeObject(ldapGroup);
        }
    }

    public void remove(Integer id) throws DAOException {
        removeObject(LdapGroup.class, id);
    }

    @SuppressWarnings("unchecked")
    public List<LdapGroup> search(String query) throws DAOException {
        return retrieveObjects(query);
    }

    /**
     * Gets all LDAP Groups.
     * 
     * @return a list of ldapgroups
     */
    public List<LdapGroup> findAll() {
        return retrieveAllObjects(LdapGroup.class);
    }
}
