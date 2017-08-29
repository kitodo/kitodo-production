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

package org.kitodo.services.data;

import java.util.List;

import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LdapGroupDAO;

public class LdapGroupService {

    private LdapGroupDAO ldapGroupDAO = new LdapGroupDAO();

    public void save(LdapGroup ldapGroup) throws DAOException {
        ldapGroupDAO.save(ldapGroup);
    }

    public LdapGroup getById(Integer id) throws DAOException {
        return ldapGroupDAO.find(id);
    }

    public List<LdapGroup> getAll() {
        return ldapGroupDAO.findAll();
    }

    public List<LdapGroup> getByQuery(String query) throws DAOException {
        return ldapGroupDAO.search(query);
    }

    public void remove(LdapGroup ldapGroup) throws DAOException {
        ldapGroupDAO.remove(ldapGroup);
    }

    public void remove(Integer id) throws DAOException {
        ldapGroupDAO.remove(id);
    }
}
