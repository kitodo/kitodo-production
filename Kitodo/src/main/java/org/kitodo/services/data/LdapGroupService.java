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

import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LdapGroupDAO;
import org.kitodo.services.data.base.SearchDatabaseService;

public class LdapGroupService extends SearchDatabaseService<LdapGroup, LdapGroupDAO> {

    public LdapGroupService() {
        super(new LdapGroupDAO());
    }

    public void save(LdapGroup ldapGroup) throws DAOException {
        dao.save(ldapGroup);
    }

    public void remove(LdapGroup ldapGroup) throws DAOException {
        dao.remove(ldapGroup);
    }

    public void remove(Integer id) throws DAOException {
        dao.remove(id);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM LdapGroup");
    }
}
