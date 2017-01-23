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


import org.kitodo.data.database.beans.History;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.HistoryDAO;

/**
 * HistoryService.
 */
public class HistoryService {

	private HistoryDAO historyDao = new HistoryDAO();

	public void save(History task) throws DAOException {
		historyDao.save(task);
	}

	public History find(Integer id) throws DAOException {
		return historyDao.find(id);
	}

	public void remove(History history) throws DAOException {
		historyDao.remove(history);
	}

	public void remove(Integer id) throws DAOException {
		historyDao.remove(id);
	}
}
