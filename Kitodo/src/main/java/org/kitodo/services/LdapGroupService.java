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

package org.kitodo.services;

import java.util.List;

import org.kitodo.data.database.beans.LdapGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.LdapGroupDAO;

public class LdapGroupService {

	private LdapGroupDAO ldapGroupDao = new LdapGroupDAO();

	public void save(LdapGroup ldapGroup) throws DAOException {
		ldapGroupDao.save(ldapGroup);
	}

	public LdapGroup find(Integer id) throws DAOException {
		return ldapGroupDao.find(id);
	}

	public List<LdapGroup> search(String query) throws DAOException {
		return ldapGroupDao.search(query);
	}

	public void remove(LdapGroup ldapGroup) throws DAOException {
		ldapGroupDao.remove(ldapGroup);
	}

	public void remove(Integer id) throws DAOException {
		ldapGroupDao.remove(id);
	}
}
