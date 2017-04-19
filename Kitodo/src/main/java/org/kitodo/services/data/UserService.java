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

import com.sun.research.ws.wadl.HTTPMethods;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.FilesystemHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ldap.Ldap;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.HibernateUtilOld;
import org.kitodo.data.database.persistence.UserDAO;
import org.kitodo.data.database.persistence.apache.MySQLHelper;
import org.kitodo.data.elasticsearch.exceptions.ResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.UserType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.encryption.DesEncrypter;
import org.kitodo.services.data.base.SearchService;

public class UserService extends SearchService {

    private static final Logger logger = Logger.getLogger(MySQLHelper.class);

    private UserDAO userDao = new UserDAO();
    private UserType userType = new UserType();
    private Indexer<User, UserType> indexer = new Indexer<>(User.class);

    /**
     * Constructor with searcher's assigning.
     */
    public UserService() {
        super(new Searcher(User.class));
    }

    /**
     * Method saves object to database and insert document to the index of
     * Elastic Search.
     *
     * @param user
     *            object
     */
    public void save(User user) throws DAOException, IOException, ResponseException {
        userDao.save(user);
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performSingleRequest(user, userType);
    }

    public User find(Integer id) throws DAOException {
        return userDao.find(id);
    }

    public List<User> findAll() throws DAOException {
        return userDao.findAll();
    }

    /**
     * Method removes object from database and document from the index of
     * Elastic Search.
     *
     * @param user
     *            object
     */
    public void remove(User user) throws DAOException, IOException, ResponseException {
        userDao.remove(user);
        indexer.setMethod(HTTPMethods.DELETE);
        indexer.performSingleRequest(user, userType);
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
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws DAOException, InterruptedException, IOException, ResponseException {
        indexer.setMethod(HTTPMethods.PUT);
        indexer.performMultipleRequests(findAll(), userType);
    }

    /**
     * Get the current object for this row.
     *
     * @return the current object representing a row.
     */
    public User getCurrent(User user) {
        boolean hasOpen = HibernateUtilOld.hasOpenSession();
        Session session = Helper.getHibernateSession();

        User current = (User) session.get(User.class, user.getId());
        if (current == null) {
            current = (User) session.load(User.class, user.getId());
        }
        if (!hasOpen) {
            session.close();
        }
        return current;
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

    public Integer getSessionTimeoutInMinutes(User user) {
        return user.getSessionTimeout() / 60;
    }

    /**
     * Convert session timeout to minutes.
     *
     * @param user
     *            object
     * @param sessionTimeout
     *            in minutes
     */
    public void setSessionTimeoutInMinutes(User user, Integer sessionTimeout) {
        if (sessionTimeout < 5) {
            user.setSessionTimeout(5 * 60);
        } else {
            user.setSessionTimeout(sessionTimeout * 60);
        }
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
     * @param user
     *            object
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
     * @param user
     *            object
     * @return result size of steps
     */
    public int getTasksSize(User user) {
        if (user.getTasks() == null) {
            return 0;
        } else {
            return user.getTasks().size();
        }
    }

    /**
     * Get size of processing steps result list.
     *
     * @param user
     *            object
     * @return result size of processing steps
     */
    public int getProcessingTasksSize(User user) {
        if (user.getProcessingTasks() == null) {
            return 0;
        } else {
            return user.getProcessingTasks().size();
        }
    }

    /**
     * Get size of projects result list.
     *
     * @param user
     *            object
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

    /**
     * Get properties list size.
     *
     * @param user
     *            object
     * @return properties list size
     */
    public int getPropertiesSize(User user) {
        if (user.getProperties() == null) {
            return 0;
        } else {
            return user.getProperties().size();
        }
    }

    // TODO: check if this class should be here or in some other place
    public boolean isPasswordCorrect(User user, String inputPassword) {
        if (inputPassword == null || inputPassword.length() == 0) {
            return false;
        } else {
            if (ConfigCore.getBooleanParameter("ldap_use")) {
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
     * Get user home directory (either from the LDAP or directly from the
     * configuration). If LDAP is used, find home directory there, otherwise in
     * configuration.
     *
     * @return path as String
     * @throws InterruptedException
     *             add description
     * @throws IOException
     *             add description
     */
    public String getHomeDirectory(User user) throws IOException, InterruptedException {
        String result;
        if (ConfigCore.getBooleanParameter("ldap_use")) {
            Ldap ldap = new Ldap();
            result = ldap.getUserHomeDirectory(user);
        } else {
            result = ConfigCore.getParameter("dir_Users") + user.getLogin();
        }

        if (result.equals("")) {
            return "";
        }

        if (!result.endsWith(File.separator)) {
            result += File.separator;
        }
        // if the directory is not "", but does not yet exist, then create it
        // now
        FilesystemHelper.createDirectoryForUser(result, user.getLogin());
        return result;
    }

    /**
     * Adds a new filter to list.
     *
     * @param inputFilter
     *            the filter to add
     */
    public void addFilter(int userId, String inputFilter) {
        if (getFilters(userId).contains(inputFilter)) {
            return;
        }
        try {
            MySQLHelper.addFilterToUser(userId, inputFilter);
        } catch (SQLException e) {
            logger.error("Cannot not add filter to user with id " + userId, e);
        }

    }

    /**
     * Removes filter from list.
     *
     * @param inputFilter
     *            the filter to remove
     */
    public void removeFilter(int userId, String inputFilter) {
        if (!getFilters(userId).contains(inputFilter)) {
            return;
        }
        try {
            MySQLHelper.removeFilterFromUser(userId, inputFilter);
        } catch (SQLException e) {
            logger.error("Cannot not remove filter from user with id " + userId, e);
        }

    }

    /**
     * Get list of filters.
     *
     * @param userId
     *            object
     * @return List of filters as strings
     */
    public List<String> getFilters(int userId) {
        List<String> answer = new ArrayList<>();
        try {
            answer = MySQLHelper.getFilterForUser(userId);
        } catch (SQLException e) {
            logger.error("Cannot not load filter for user with id " + userId, e);
        }

        return answer;
    }
}
