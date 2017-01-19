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

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.ldap.Ldap;
import de.sub.goobi.persistence.apache.UserManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.UserDAO;
import org.kitodo.data.encryption.DesEncrypter;

public class UserService  {

	private UserDAO userDao = new UserDAO();

	public User save(User user) throws DAOException {
		return userDao.save(user);
	}

	public User find(Integer id) throws DAOException {
		return userDao.find(id);
	}

	public List<User> findAll() throws DAOException {
		return userDao.findAll();
	}

	public void remove(User user) throws DAOException {
		userDao.remove(user);
	}

	public List<User> search(String query) throws DAOException {
		return userDao.search(query);
	}

	public List<User> search(String query, String parameter) throws DAOException {
		return userDao.search(query, parameter);
	}

	public List<User> search(String query, String namedParameter, String parameter) throws DAOException {
		return userDao.search(query, namedParameter, parameter);
	}

	public Long count(String query) throws DAOException {
		return userDao.count(query);
	}

	/**
	 * Table size.
	 *
	 * @return table size
	 */
	public Integer getTableSize(User user) {
		if (user.getTableSize() == null) {
			return Integer.valueOf(10);
		}
		return user.getTableSize();
	}

	/**
	 * Session timeout.
	 *
	 * @return session timeout
	 */
	public Integer getSessionTimeout(User user) {
		if (user.getSessionTimeout() == null) {
			user.setSessionTimeout(7200);
		}
		return user.getSessionTimeout();
	}

	/**
	 * CSS style.
	 *
	 * @return CSS style
	 */
	public String getCss(User user) {
		if (user.getCss() == null || user.getCss().length() == 0) {
			user.setCss("/css/default.css");
		}
		return user.getCss();
	}

	/**
	 * Get size of user group result.
	 *
	 * @param user object
	 * @return size
	 */
	public int getUserGroupSize(User user) {
		if (user.getUserGroups() == null) {
			return 0;
		} else {
			return user.getUserGroups().size();
		}
	}

	/**
	 * Get size of steps result list.
	 *
	 * @param user object
	 * @return result size of steps
	 */
	public int getStepsSize(User user) {
		if (user.getSteps() == null) {
			return 0;
		} else {
			return user.getSteps().size();
		}
	}

	/**
	 * Get size of processing steps result list.
	 *
	 * @param user object
	 * @return result size of processing steps
	 */
	public int getProcessingStepsSize(User user) {
		if (user.getProcessingSteps() == null) {
			return 0;
		} else {
			return user.getProcessingSteps().size();
		}
	}

	/**
	 * Get size of projects result list.
	 *
	 * @param user object
	 * @return result size of projects
	 *
	 */
	public int getProjectsSize(User user) {
		if (user.getProjects() == null) {
			return 0;
		} else {
			return user.getProjects().size();
		}
	}

	//TODO: check if this class should be here or in some other place
	public boolean isPasswordCorrect(User user, String inputPassword) {
		if (inputPassword == null || inputPassword.length() == 0) {
			return false;
		} else {
			if (ConfigMain.getBooleanParameter("ldap_use")) {
				Ldap ldap = new Ldap();
				return ldap.isUserPasswordCorrect(user, inputPassword);
			} else {
				DesEncrypter encrypter = new DesEncrypter();
				String encoded = encrypter.encrypt(inputPassword);
				return user.getPassword().equals(encoded);
			}
		}
	}

	public String getFullName(User user) {
		return user.getSurname() + ", " + user.getName();
	}

	/**
	 * Get user home directory (either from the LDAP or directly from the configuration). If LDAP is used, find home
	 * directory there, otherwise in configuration.
	 *
	 * @return path as String
	 * @throws InterruptedException add description
	 * @throws IOException add description
	 */
	public String getHomeDirectory(User user) throws IOException, InterruptedException {
		String result;
		if (ConfigMain.getBooleanParameter("ldap_use")) {
			Ldap ldap = new Ldap();
			result = ldap.getUserHomeDirectory(user);
		} else {
			result = ConfigMain.getParameter("dir_Users") + user.getLogin();
		}

		if (result.equals("")) {
			return "";
		}

		if (!result.endsWith(File.separator)) {
			result += File.separator;
		}
		// if the directory is not "", but does not yet exist, then create it now
		FilesystemHelper.createDirectoryForUser(result, user.getLogin());
		return result;
	}

	public Integer getSessionTimeoutInMinutes(User user) {
		return user.getSessionTimeout() / 60;
	}

	/**
	 * Convert session timeout to minutes.
	 *
	 * @param user object
	 * @param sessionTimeout in minutes
	 */
	public void setSessionTimeoutInMinutes(User user, Integer sessionTimeout) {
		if (sessionTimeout < 5) {
			user.setSessionTimeout(5 * 60);
		} else {
			user.setSessionTimeout(sessionTimeout * 60);
		}
	}

	/**
	 * Get properties list size.
	 *
	 * @param user object
	 * @return properties list size
	 */
	public int getPropertiesSize(User user) {
		if (user.getProperties() == null) {
			return 0;
		} else {
			return user.getProperties().size();
		}
	}

	/**
	 * Get list of filters.
	 *
	 * @param user object
	 * @return List of filters as strings
	 */
	//TODO: check what is this UserManager
	public List<String> getFilters(User user) {
		return UserManager.getFilters(user.getId());
	}

	/**
	 * Adds a new filter to list.
	 *
	 * @param inputFilter the filter to add
	 */

	public void addFilter(User user, String inputFilter) {
		UserManager.addFilter(user.getId(), inputFilter);
	}

	/**
	 * Removes filter from list.
	 *
	 * @param inputFilter the filter to remove
	 */
	public void removeFilter(User user, String inputFilter) {
		UserManager.removeFilter(user.getId(), inputFilter);
	}
}
