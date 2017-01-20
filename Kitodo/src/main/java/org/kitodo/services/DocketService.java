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

import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.DocketDAO;

public class DocketService {
	private DocketDAO docketDao = new DocketDAO();

	public Docket find(Integer id) throws DAOException {
		return docketDao.find(id);
	}

	public List<Docket> findAll() throws DAOException {
		return docketDao.findAll();
	}

	public Docket save(Docket userGroup) throws DAOException {
		return docketDao.save(userGroup);
	}

	public void remove(Docket userGroup) throws DAOException {
		docketDao.remove(userGroup);
	}

	public List<Docket> search(String query) throws DAOException {
		return docketDao.search(query);
	}

	public Long count(String query) throws DAOException {
		return docketDao.count(query);
	}
}
