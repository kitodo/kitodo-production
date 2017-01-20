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

import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.UserGroupDAO;

public class UserGroupService {
	private UserGroupDAO userGroupDao = new UserGroupDAO();

	public UserGroup find(Integer id) throws DAOException {
		return userGroupDao.find(id);
	}

	public UserGroup save(UserGroup userGroup) throws DAOException {
		return userGroupDao.save(userGroup);
	}

	public void remove(UserGroup userGroup) throws DAOException {
		userGroupDao.remove(userGroup);
	}

	public List<UserGroup> search(String query) throws DAOException {
		return userGroupDao.search(query);
	}

	public Long count(String query) throws DAOException {
		return userGroupDao.count(query);
	}

	public String getPermissionAsString(UserGroup userGroup) {
		if (userGroup.getPermission() == null) {
			userGroup.setPermission(4);
		} else if (userGroup.getPermission() == 3) {
			userGroup.setPermission(4);
		}
		return String.valueOf(userGroup.getPermission().intValue());
	}

	public void setPermissionAsString(UserGroup userGroup, String permission) {
		userGroup.setPermission(Integer.parseInt(permission));
	}

	public int getStepsSize(UserGroup userGroup) {
		if (userGroup.getSteps() == null) {
			return 0;
		} else {
			return userGroup.getSteps().size();
		}
	}
}
