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

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.ldap.Ldap;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.LocalDateTime;
import org.json.simple.JSONObject;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.beans.UserGroup;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.persistence.UserDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.UserType;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.encryption.DesEncrypter;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.FilterDTO;
import org.kitodo.dto.ProjectDTO;
import org.kitodo.dto.UserDTO;
import org.kitodo.dto.UserGroupDTO;
import org.kitodo.helper.RelatedProperty;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.base.SearchService;

public class UserService extends SearchService<User, UserDTO, UserDAO> {

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private static UserService instance = null;

    /**
     * Constructor with Searcher and Indexer assigning.
     */
    private UserService() {
        super(new UserDAO(), new UserType(), new Indexer<>(User.class), new Searcher(User.class));
    }

    /**
     * Return singleton variable of type UserService.
     *
     * @return unique instance of UserService
     */
    public static UserService getInstance() {
        if (Objects.equals(instance, null)) {
            synchronized (UserService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new UserService();
                }
            }
        }
        return instance;
    }

    /**
     * Method saves user groups, properties and tasks related to modified user.
     *
     * @param user
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(User user) throws CustomResponseException, IOException {
        manageFiltersDependenciesForIndex(user);
        manageProjectsDependenciesForIndex(user);
        manageTasksDependenciesForIndex(user);
        manageUserGroupsDependenciesForIndex(user);
    }

    /**
     * Check if IndexAction flag is delete. If true remove user from list of users
     * and re-save project, if false only re-save project object.
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
     * Check if IndexAction flag is delete. If true remove filter from the index, if
     * false re-save filter object.
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
     * Check if IndexAction flag is delete. If true remove user from list of users
     * and re-save task, if false only re-save task object.
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
     * Check if IndexAction flag is delete. If true remove user from list of users
     * and re-save group, if false only re-save group object.
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

    public List<User> getByQuery(String query, String parameter) throws DAOException {
        return dao.search(query, parameter);
    }

    public List<User> getByQuery(String query, String namedParameter, String parameter) throws DAOException {
        return dao.search(query, namedParameter, parameter);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("FROM User WHERE deleted = 0");
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
        if (id != null) {
            boolQuery.must(createSimpleQuery("_id", id, false));
        }
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
        dao.refresh(user);
    }

    /**
     * Find users with exact name.
     *
     * @param name
     *            of the searched user
     * @return list of JSON objects
     */
    List<JSONObject> findByName(String name) throws DataException {
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
    List<JSONObject> findBySurname(String surname) throws DataException {
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
    List<JSONObject> findByFullName(String name, String surname) throws DataException {
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
    JSONObject findByLogin(String login) throws DataException {
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
    JSONObject findByLdapLogin(String ldapLogin) throws DataException {
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
    List<JSONObject> findByActive(boolean active) throws DataException {
        QueryBuilder query = createSimpleQuery("active", active, true);
        return searcher.findDocuments(query.toString(), sortByLogin());
    }

    /**
     * Find active users by name or surname.
     *
     * @param active
     *            true or false
     * @param name
     *            name or surname
     * @return list of JSONObjects
     */
    List<JSONObject> findByActiveAndName(boolean active, String name) throws DataException {
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        boolQuery.must(createSimpleQuery("active", active, true));
        BoolQueryBuilder nestedBoolQuery = new BoolQueryBuilder();
        nestedBoolQuery.should(createSimpleWildcardQuery("name", name));
        nestedBoolQuery.should(createSimpleWildcardQuery("surname", name));
        boolQuery.must(nestedBoolQuery);
        return searcher.findDocuments(boolQuery.toString(), sortByLogin());
    }

    /**
     * Find users with exact location.
     *
     * @param location
     *            of the searched user
     * @return list of JSON objects
     */
    List<JSONObject> findByLocation(String location) throws DataException {
        QueryBuilder query = createSimpleQuery("location", location, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users by id of user group.
     *
     * @param id
     *            of user group
     * @return list of JSON objects with users for specific user group id
     */
    List<JSONObject> findByUserGroupId(Integer id) throws DataException {
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
    List<JSONObject> findByUserGroupTitle(String title) throws DataException {
        QueryBuilder query = createSimpleQuery("userGroups.title", title, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users by filter.
     *
     * @param value
     *            of filter
     * @return list of JSON objects with users for specific filter
     */
    List<JSONObject> findByFilter(String value) throws DataException {
        Set<Integer> filterIds = new HashSet<>();

        List<JSONObject> filters = serviceManager.getFilterService().findByValue(value, true);

        for (JSONObject filter : filters) {
            filterIds.add(getIdFromJSONObject(filter));
        }
        return searcher.findDocuments(createSetQuery("filters.id", filterIds, true).toString());
    }

    /**
     * Find users by processing tasks.
     *
     * @param id
     *            of filter
     * @return list of JSON objects with users for specific filter
     */
    List<UserDTO> findByProcessingTask(Integer id, boolean related) throws DataException {
        List<JSONObject> jsonObjects = searcher.findDocuments(createSimpleQuery("processingTasks.id", id, true).toString());
        return convertJSONObjectsToDTOs(jsonObjects, related);
    }

    /**
     * Find all visible users.
     *
     * @return a list of all visible users as UserDTO
     */
    public List<UserDTO> findAllVisibleUsers() throws DataException {
        List<JSONObject> jsonObjects = findAllDocuments(sortByLogin());
        return convertJSONObjectsToDTOs(jsonObjects, true);
    }

    /**
     * Find all visible users with related objects.
     *
     * @return a list of all visible users as UserDTO
     */
    public List<UserDTO> findAllVisibleUsersWithRelations() throws DataException {
        List<JSONObject> jsonObjects = findAllDocuments(sortByLogin());
        return convertJSONObjectsToDTOs(jsonObjects, false);
    }

    /**
     * Find all active users.
     *
     * @return a list of all active users as UserDTO
     */
    public List<UserDTO> findAllActiveUsers() throws DataException {
        List<JSONObject> jsonObjects = findByActive(true);
        return convertJSONObjectsToDTOs(jsonObjects, true);
    }

    /**
     * Find all active users wit related objects.
     *
     * @return a list of all active users as UserDTO
     */
    public List<UserDTO> findAllActiveUsersWithRelations() throws DataException {
        List<JSONObject> jsonObjects = findByActive(true);
        return convertJSONObjectsToDTOs(jsonObjects, false);
    }

    /**
     * Find filtered users by name.
     *
     * @param name
     *            the name filter
     * @return a list of filtered users
     */
    public List<UserDTO> findActiveUsersByName(String name) throws DataException {
        List<JSONObject> jsonObjects = findByActiveAndName(true, name);
        return convertJSONObjectsToDTOs(jsonObjects, true);
    }

    private String sortByLogin() {
        return SortBuilders.fieldSort("login").order(SortOrder.ASC).toString();
    }

    /**
     * Get all active users sorted by surname and name.
     *
     * @return sorted list of all active users as User objects
     */
    public List<User> getAllActiveUsersSortedByNameAndSurname() {
        return dao.getAllActiveUsersSortedByNameAndSurname();
    }

    @Override
    public UserDTO convertJSONObjectToDTO(JSONObject jsonObject, boolean related) throws DataException {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(getIdFromJSONObject(jsonObject));
        JSONObject userJSONObject = getSource(jsonObject);
        userDTO.setLogin(getStringPropertyForDTO(userJSONObject, "login"));
        userDTO.setName(getStringPropertyForDTO(userJSONObject, "name"));
        userDTO.setSurname(getStringPropertyForDTO(userJSONObject, "surname"));
        userDTO.setActive(getBooleanPropertyForDTO(userJSONObject, "active"));
        userDTO.setLdapLogin(getStringPropertyForDTO(userJSONObject, "ldapLogin"));
        userDTO.setLocation(getStringPropertyForDTO(userJSONObject, "location"));
        userDTO.setFullName(getFullName(userDTO));
        userDTO.setFiltersSize(getSizeOfRelatedPropertyForDTO(userJSONObject, "filters"));
        userDTO.setProjectsSize(getSizeOfRelatedPropertyForDTO(userJSONObject, "projects"));
        userDTO.setTasksSize(getSizeOfRelatedPropertyForDTO(userJSONObject, "tasks"));
        userDTO.setProcessingTasksSize(getSizeOfRelatedPropertyForDTO(userJSONObject, "processingTasks"));
        userDTO.setUserGroupSize(getSizeOfRelatedPropertyForDTO(userJSONObject, "userGroups"));

        if (!related) {
            userDTO = convertRelatedJSONObjects(userJSONObject, userDTO);
        } else {
            userDTO = addBasicFilterRelation(userDTO, userJSONObject);
            userDTO = addBasicProjectRelation(userDTO, userJSONObject);
            userDTO = addBasicUserGroupRelation(userDTO, userJSONObject);
        }

        return userDTO;
    }

    private UserDTO convertRelatedJSONObjects(JSONObject jsonObject, UserDTO userDTO) throws DataException {
        userDTO.setFilters(convertRelatedJSONObjectToDTO(jsonObject, "filters", serviceManager.getFilterService()));
        userDTO.setProjects(convertRelatedJSONObjectToDTO(jsonObject, "projects", serviceManager.getProjectService()));
        userDTO.setTasks(convertRelatedJSONObjectToDTO(jsonObject, "tasks", serviceManager.getTaskService()));
        userDTO.setUserGroups(
                convertRelatedJSONObjectToDTO(jsonObject, "userGroups", serviceManager.getUserGroupService()));
        return userDTO;
    }

    private UserDTO addBasicFilterRelation(UserDTO userDTO, JSONObject jsonObject) {
        if (userDTO.getFiltersSize() > 0) {
            List<FilterDTO> filters = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add("value");
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject, "filters", subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                FilterDTO filter = new FilterDTO();
                filter.setId(relatedProperty.getId());
                if (relatedProperty.getValues().size() > 0) {
                    filter.setValue(relatedProperty.getValues().get(0));
                }
                filters.add(filter);
            }
            userDTO.setFilters(filters);
        }
        return userDTO;
    }

    private UserDTO addBasicProjectRelation(UserDTO userDTO, JSONObject jsonObject) {
        if (userDTO.getProjectsSize() > 0) {
            List<ProjectDTO> projects = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add("title");
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject, "projects", subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                ProjectDTO project = new ProjectDTO();
                project.setId(relatedProperty.getId());
                if (relatedProperty.getValues().size() > 0) {
                    project.setTitle(relatedProperty.getValues().get(0));
                }
                project.setTitle(relatedProperty.getValues().get(0));
                projects.add(project);
            }
            userDTO.setProjects(projects);
        }
        return userDTO;
    }

    private UserDTO addBasicUserGroupRelation(UserDTO userDTO, JSONObject jsonObject) {
        if (userDTO.getUserGroupSize() > 0) {
            List<UserGroupDTO> userGroups = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add("title");
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject, "userGroups", subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                UserGroupDTO userGroup = new UserGroupDTO();
                userGroup.setId(relatedProperty.getId());
                if (relatedProperty.getValues().size() > 0) {
                    userGroup.setTitle(relatedProperty.getValues().get(0));
                }
                userGroups.add(userGroup);
            }
            userDTO.setUserGroups(userGroups);
        }
        return userDTO;
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
            user.setCss("old/userStyles/default.css");
        }
        return user.getCss();
    }

    // TODO: check if this class should be here or in some other place
    /**
     * Check if password is correct.
     * 
     * @param user
     *            as User object
     * @param inputPassword
     *            as String
     * @return true or false
     */
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
     * At that moment only add this method.
     *
     * @param user
     *            object as UserDTo
     * @return full name of the user as String
     */
    public String getFullName(UserDTO user) {
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
        List<Filter> filters = user.getFilters();
        for (Filter filter : filters) {
            if (filter.getValue().equals(userFilter)) {
                serviceManager.getFilterService().remove(filter);
            }
        }
        refresh(user);
    }
}
