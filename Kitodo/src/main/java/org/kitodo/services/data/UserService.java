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
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.ldap.Ldap;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.hibernate.Session;
import org.joda.time.LocalDateTime;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.HibernateUtilOld;
import org.kitodo.data.database.persistence.UserDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.UserType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.encryption.DesEncrypter;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

public class UserService extends SearchService<User> {

    private UserDAO userDAO = new UserDAO();
    private UserType userType = new UserType();
    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(UserService.class);

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    public UserService() {
        super(new Searcher(User.class));
        this.indexer = new Indexer<>(User.class);
    }

    /**
     * Method saves user object to database.
     *
     * @param user
     *            object
     */
    public void saveToDatabase(User user) throws DAOException {
        userDAO.save(user);
    }

    /**
     * Method saves user document to the index of Elastic Search.
     *
     * @param user
     *            object
     */
    @SuppressWarnings("unchecked")
    public void saveToIndex(User user) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.PUT);
        if (user != null) {
            indexer.performSingleRequest(user, userType);
        }
    }

    /**
     * Method saves user groups, properties and tasks related to modified user.
     *
     * @param user
     *            object
     */
    protected void manageDependenciesForIndex(User user) throws CustomResponseException, IOException {
        manageFiltersDependenciesForIndex(user);
        manageProjectsDependenciesForIndex(user);
        manageTasksDependenciesForIndex(user);
        manageUserGroupsDependenciesForIndex(user);
    }

    /**
     * Check if IndexAction flag is delete. If true remove user from list of
     * users and re-save project, if false only re-save project object.
     *
     * @param user
     *            object
     */
    private void manageProjectsDependenciesForIndex(User user) throws CustomResponseException, IOException {
        if (user.getIndexAction() == IndexAction.DELETE) {
            for (Project project : user.getProjects()) {
                project.getUsers().remove(user);
                serviceManager.getProjectService().saveToIndex(project);
            }
        } else {
            for (Project project : user.getProjects()) {
                serviceManager.getProjectService().saveToIndex(project);
            }
        }
    }

    /**
     * Check if IndexAction flag is delete. If true remove filter from the
     * index, if false re-save filter object.
     *
     * @param user
     *            object
     */
    private void manageFiltersDependenciesForIndex(User user) throws CustomResponseException, IOException {
        if (user.getIndexAction() == IndexAction.DELETE) {
            for (Filter filter : user.getFilters()) {
                serviceManager.getFilterService().removeFromIndex(filter);
            }
        } else {
            for (Filter filter : user.getFilters()) {
                serviceManager.getFilterService().saveToIndex(filter);
            }
        }
    }

    /**
     * Check if IndexAction flag is delete. If true remove user from list of
     * users and re-save task, if false only re-save task object.
     *
     * @param user
     *            object
     */
    private void manageTasksDependenciesForIndex(User user) throws CustomResponseException, IOException {
        if (user.getIndexAction() == IndexAction.DELETE) {
            for (Task task : user.getTasks()) {
                task.getUsers().remove(user);
                serviceManager.getTaskService().saveToIndex(task);
            }
            for (Task task : user.getProcessingTasks()) {
                task.setProcessingUser(null);
                serviceManager.getTaskService().saveToIndex(task);
            }
        } else {
            for (Task task : user.getTasks()) {
                serviceManager.getTaskService().saveToIndex(task);
            }
            for (Task task : user.getProcessingTasks()) {
                serviceManager.getTaskService().saveToIndex(task);
            }
        }
    }

    /**
     * Check if IndexAction flag is delete. If true remove user from list of
     * users and re-save group, if false only re-save group object.
     *
     * @param user
     *            object
     */
    private void manageUserGroupsDependenciesForIndex(User user) throws CustomResponseException, IOException {
        if (user.getIndexAction() == IndexAction.DELETE) {
            for (UserGroup userGroup : user.getUserGroups()) {
                userGroup.getUsers().remove(user);
                serviceManager.getUserGroupService().saveToIndex(userGroup);
            }
        } else {
            for (UserGroup userGroup : user.getUserGroups()) {
                serviceManager.getUserGroupService().saveToIndex(userGroup);
            }
        }
    }

    public User find(Integer id) throws DAOException {
        return userDAO.find(id);
    }

    /**
     * Get al. users.
     * 
     * @return A List of all users
     */
    public List<User> findAll() {
        return userDAO.findAll();
    }

    /**
     * Get all visible users.
     * 
     * @return A list of all visible users
     */
    public List<User> getAllVisibleUsers() {
        return userDAO.getAllVisibleUsers();
    }

    /**
     * get all active users.
     * 
     * @return a list of all active users
     */
    public List<User> getAllActiveUsers() {
        return userDAO.getAllActiveUsers();
    }

    /**
     * Method removes user object from database.
     *
     * @param user
     *            object
     */
    public void removeFromDatabase(User user) throws DAOException {
        userDAO.remove(user);
    }

    /**
     * Method removes user object from database.
     *
     * @param id
     *            of template object
     */
    public void removeFromDatabase(Integer id) throws DAOException {
        userDAO.remove(id);
    }

    /**
     * Method removes user object from index of Elastic Search.
     *
     * @param user
     *            object
     */
    @SuppressWarnings("unchecked")
    public void removeFromIndex(User user) throws CustomResponseException, IOException {
        indexer.setMethod(HTTPMethods.DELETE);
        if (user != null) {
            indexer.performSingleRequest(user, userType);
        }
    }

    public List<User> search(String query) throws DAOException {
        return userDAO.search(query);
    }

    public List<User> search(String query, String parameter) throws DAOException {
        return userDAO.search(query, parameter);
    }

    public List<User> search(String query, String namedParameter, String parameter) throws DAOException {
        return userDAO.search(query, namedParameter, parameter);
    }

    /**
     * Count all users.
     *
     * @return amount of all users
     */
    public Long count() throws DataException {
        return searcher.countDocuments();
    }

    /**
     * Count users according to given query.
     *
     * @param query
     *            for index search
     * @return amount of users according to given query
     */
    public Long count(String query) throws DataException {
        return searcher.countDocuments(query);
    }

    /**
     * Get amount of users with exactly the same login like given but different id.
     * 
     * @param id
     *            of user
     * @param login
     *            of user
     * @return amount of users with exactly the same login like given but different
     *         id
     */
    public Long getAmountOfUsersWithExactlyTheSameLogin(String id, String login) throws DataException {
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.mustNot(createSimpleQuery("_id", id, true));
        boolQuery.must(createSimpleQuery("login", login, true));
        return count(boolQuery.toString());

    }

    /**
     * Refresh user object after update.
     *
     * @param user
     *            object
     */
    public void refresh(User user) {
        userDAO.refresh(user);
    }

    /**
     * Find users with exact name.
     *
     * @param name
     *            of the searched user
     * @return list of JSON objects
     */
    public List<JSONObject> findByName(String name) throws DataException {
        QueryBuilder query = createSimpleQuery("name", name, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users with exact surname.
     *
     * @param surname
     *            of the searched user
     * @return list of JSON objects
     */
    public List<JSONObject> findBySurname(String surname) throws DataException {
        QueryBuilder query = createSimpleQuery("surname", surname, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users with exact full name.
     *
     * @param name
     *            of the searched user
     * @param surname
     *            of the searched user
     * @return list of JSON objects
     */
    public List<JSONObject> findByFullName(String name, String surname) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery("name", name, true, Operator.AND));
        query.must(createSimpleQuery("surname", surname, true, Operator.AND));
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find user with exact login.
     *
     * @param login
     *            of the searched user
     * @return JSON objects
     */
    public JSONObject findByLogin(String login) throws DataException {
        QueryBuilder query = createSimpleQuery("login", login, true, Operator.AND);
        return searcher.findDocument(query.toString());
    }

    /**
     * Find user with exact LDAP login.
     *
     * @param ldapLogin
     *            of the searched user
     * @return search result
     */
    public JSONObject findByLdapLogin(String ldapLogin) throws DataException {
        QueryBuilder query = createSimpleQuery("ldapLogin", ldapLogin, true, Operator.AND);
        return searcher.findDocument(query.toString());
    }

    /**
     * Find active or inactive users.
     *
     * @param active
     *            true -active user or false - inactive user
     * @return list of JSON objects
     */
    public List<JSONObject> findByActive(boolean active) throws DataException {
        QueryBuilder query = createSimpleQuery("active", String.valueOf(active), true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users with exact location.
     *
     * @param location
     *            of the searched user
     * @return list of JSON objects
     */
    public List<JSONObject> findByLocation(String location) throws DataException {
        QueryBuilder query = createSimpleQuery("location", location, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users with exact metadata language.
     *
     * @param metadataLanguage
     *            of the searched user
     * @return list of JSON objects
     */
    public List<JSONObject> findByMetadataLanguage(String metadataLanguage) throws DataException {
        QueryBuilder query = createSimpleQuery("metadataLanguage", metadataLanguage, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users by id of user group.
     *
     * @param id
     *            of user group
     * @return list of JSON objects with users for specific user group id
     */
    public List<JSONObject> findByUserGroupId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery("userGroups.id", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users by title of user group.
     *
     * @param title
     *            of user group
     * @return list of JSON objects with users for specific user group title
     */
    public List<JSONObject> findByUserGroupTitle(String title) throws DataException {
        List<JSONObject> users = new ArrayList<>();

        List<JSONObject> userGroups = serviceManager.getUserGroupService().findByTitle(title, true);
        for (JSONObject userGroup : userGroups) {
            users.addAll(findByUserGroupId(getIdFromJSONObject(userGroup)));
        }
        return users;
    }

    /**
     * Find users by filter.
     *
     * @param value
     *            of filter
     * @return list of JSON objects with users for specific filter
     */
    public List<JSONObject> findByFilter(String value) throws DataException {
        List<JSONObject> users = new ArrayList<>();

        List<JSONObject> filters = serviceManager.getFilterService().findByValue(value, true);
        for (JSONObject filter : filters) {
            users.addAll(findByFilterId(getIdFromJSONObject(filter)));
        }
        return users;
    }

    /**
     * Simulate relationship between filter and user type.
     *
     * @param id
     *            of filter
     * @return list of JSON objects with users for specific filter id
     */
    private List<JSONObject> findByFilterId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery("filters.id", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Method adds all object found in database to Elastic Search index.
     */
    public void addAllObjectsToIndex() throws CustomResponseException, InterruptedException, IOException {
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
            return 10;
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
        if (user.getFilters() == null) {
            return 0;
        } else {
            return user.getFilters().size();
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
     * @throws IOException
     *             add description
     */
    public URI getHomeDirectory(User user) throws IOException {
        URI result;
        if (ConfigCore.getBooleanParameter("ldap_use")) {
            Ldap ldap = new Ldap();
            result = Paths.get(ldap.getUserHomeDirectory(user)).toUri();
        } else {
            result = Paths.get(ConfigCore.getParameter("dir_Users"), user.getLogin()).toUri();
        }

        if (!new File(result).exists()) {
            serviceManager.getFileService().createDirectoryForUser(result, user.getLogin());
        }
        return result;
    }

    /**
     * Adds a new filter to list.
     *
     * @param user
     *            object
     * @param filter
     *            the filter to add
     */
    public void addFilter(User user, String filter) {
        if (getFilters(user).contains(filter)) {
            return;
        }
        try {
            addFilterToUser(user, filter);
        } catch (DataException e) {
            logger.error("Cannot not add filter to user with id " + user.getId(), e);
        }
    }

    /**
     * Removes filter from list.
     *
     * @param user
     *            object
     * @param filter
     *            the filter to remove
     */
    public void removeFilter(User user, String filter) {
        if (!getFilters(user).contains(filter)) {
            return;
        }
        try {
            removeFilterFromUser(user, filter);
        } catch (DataException e) {
            logger.error("Cannot not remove filter from user with id " + user.getId(), e);
        }
    }

    /**
     * Get list of filters.
     *
     * @param user
     *            object
     * @return List of filters as strings
     */
    public List<String> getFilters(User user) {
        return getFiltersForUser(user);
    }

    /**
     * Add filter to user.
     *
     * @param user
     *            object
     * @param userFilter
     *            String
     */
    private void addFilterToUser(User user, String userFilter) throws DataException {
        LocalDateTime localDateTime = new LocalDateTime();
        Filter filter = new Filter();
        filter.setValue(userFilter);
        filter.setCreationDate(localDateTime.toDate());
        filter.setUser(user);
        serviceManager.getFilterService().save(filter);
        refresh(user);
    }

    /**
     * Get filters for user.
     *
     * @param user
     *            object
     * @return list of filters
     */
    private List<String> getFiltersForUser(User user) {
        List<String> userFilters = new ArrayList<>();
        List<Filter> filters = user.getFilters();
        for (Filter filter : filters) {
            userFilters.add(filter.getValue());
        }
        return userFilters;
    }

    /**
     * Remove filter from user.
     *
     * @param user
     *            object
     * @param userFilter
     *            String
     */
    private void removeFilterFromUser(User user, String userFilter) throws DataException {
        for (Filter filter : user.getFilters()) {
            if (filter.getValue().equals(userFilter)) {
                serviceManager.getFilterService().remove(filter);
            }
        }
    }

    /**
     * Get filtered users by name.
     * 
     * @param filter
     *            the name filter
     * @return a list of filtered users
     */
    public List<User> getFilteredUsersByName(String filter) {
        return userDAO.getFilteredUsersByName(filter);
    }
}
