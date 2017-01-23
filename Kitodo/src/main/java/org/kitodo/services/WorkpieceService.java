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

import org.kitodo.data.database.beans.Workpiece;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.WorkpieceDAO;

public class WorkpieceService {

	private WorkpieceDAO workpieceDao = new WorkpieceDAO();

	public void save(Workpiece workpiece) throws DAOException {
		workpieceDao.save(workpiece);
	}

	public Workpiece find(Integer id) throws DAOException {
		return workpieceDao.find(id);
	}

	public void remove(Workpiece workpiece) throws DAOException {
		workpieceDao.remove(workpiece);
	}

	public void remove(Integer id) throws DAOException {
		workpieceDao.remove(id);
	}

	/**
	 * Get size of properties list.
	 *
	 * @param workpiece object
	 * @return properties list size
	 */
	public int getPropertiesSize(Workpiece workpiece) {
		if (workpiece.getProperties() == null) {
			return 0;
		} else {
			return workpiece.getProperties().size();
		}
	}
}
