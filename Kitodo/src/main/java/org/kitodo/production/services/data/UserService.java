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

package org.kitodo.production.services.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.LocalDateTime;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.IndexAction;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.UserDAO;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.index.Indexer;
import org.kitodo.data.elasticsearch.index.type.UserType;
import org.kitodo.data.elasticsearch.index.type.enums.FilterTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.RoleTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.UserTypeField;
import org.kitodo.data.elasticsearch.search.Searcher;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.FilterDTO;
import org.kitodo.production.dto.ProjectDTO;
import org.kitodo.production.dto.RoleDTO;
import org.kitodo.production.dto.UserDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.RelatedProperty;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.security.password.SecurityPasswordEncoder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserService extends SearchService<User, UserDTO, UserDAO> implements UserDetailsService {

    private static final Logger logger = LogManager.getLogger(UserService.class);
    private static UserService instance = null;
    private static final String LOGIN_NOT_VALID = "loginNotValid";
    private SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();

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
     * Method saves roles, properties and tasks related to modified user.
     *
     * @param user
     *            object
     */
    @Override
    protected void manageDependenciesForIndex(User user) throws CustomResponseException, IOException {
        manageFiltersDependenciesForIndex(user);
        manageProjectsDependenciesForIndex(user);
        manageTasksDependenciesForIndex(user);
        manageRolesDependenciesForIndex(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user;
        try {
            user = getByLogin(username);
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
        return new SecurityUserDetails(user);
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
                ServiceManager.getProjectService().saveToIndex(project, false);
            }
        } else {
            for (Project project : user.getProjects()) {
                ServiceManager.getProjectService().saveToIndex(project, false);
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
                ServiceManager.getFilterService().removeFromIndex(filter, false);
            }
        } else {
            for (Filter filter : user.getFilters()) {
                ServiceManager.getFilterService().saveToIndex(filter, false);
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
            for (Task task : user.getProcessingTasks()) {
                task.setProcessingUser(null);
                ServiceManager.getTaskService().saveToIndex(task, false);
            }
        } else {
            for (Task task : user.getProcessingTasks()) {
                ServiceManager.getTaskService().saveToIndex(task, false);
            }
        }
    }

    /**
     * Check if IndexAction flag is delete. If true remove user from list of
     * users and re-save role, if false only re-save role object.
     *
     * @param user
     *            object
     */
    private void manageRolesDependenciesForIndex(User user) throws CustomResponseException, IOException {
        if (user.getIndexAction() == IndexAction.DELETE) {
            for (Role role : user.getRoles()) {
                role.getUsers().remove(user);
                ServiceManager.getRoleService().saveToIndex(role, false);
            }
        } else {
            for (Role role : user.getRoles()) {
                ServiceManager.getRoleService().saveToIndex(role, false);
            }
        }
    }

    /**
     * Gets user by ldap login and in case that no user can be found the normal
     * login is used as fallback.
     *
     * @param login
     *            The login of the user.
     * @return The user object.
     * @throws DAOException
     *             if there is an error at connection or reading database
     * @throws UsernameNotFoundException
     *             if no user can be found by ldaplogin and normal login
     */
    public User getByLdapLoginWithFallback(String login) throws DAOException, UsernameNotFoundException {
        User user;
        try {
            user = ServiceManager.getUserService().getByLdapLogin(login);
        } catch (UsernameNotFoundException e) {
            user = ServiceManager.getUserService().getByLogin(login);
        }
        return user;
    }

    /**
     * Gets user by login.
     *
     * @param login
     *            The login.
     * @return The user.
     */
    public User getByLogin(String login) throws DAOException {
        return getByLoginQuery(login, "from User where login = :username");
    }

    /**
     * Gets user by ldap login.
     *
     * @param ldapLogin
     *            The ldapLogin.
     * @return The user.
     */
    public User getByLdapLogin(String ldapLogin) throws DAOException {
        return getByLoginQuery(ldapLogin, "from User where ldapLogin = :username");
    }

    private User getByLoginQuery(String login, String query) throws DAOException {
        List<User> users = getByQuery(query, "username", login);
        if (users.size() == 1) {
            return users.get(0);
        } else if (users.isEmpty()) {
            throw new UsernameNotFoundException("Username " + login + " not found!");
        } else {
            throw new UsernameNotFoundException("Username " + login + " was found more than once");
        }
    }

    /**
     * Gets the current authenticated user of current threads security context.
     *
     * @return The SecurityUserDetails object or null if no user is authenticated.
     */
    public SecurityUserDetails getAuthenticatedUser() {
        return ServiceManager.getSecurityAccessService().getAuthenticatedSecurityUserDetails();
    }

    /**
     * Get the current authenticated user as User bean.
     *
     * @return the User object
     */
    public User getCurrentUser() {
        return new User(getAuthenticatedUser());
    }

    /**
     * Gets the session client of the current authenticated user.
     * 
     * @return The client object or null if no session client is set or no user is
     *         authenticated.
     */
    public Client getSessionClientOfAuthenticatedUser() {
        if (Objects.nonNull(getAuthenticatedUser())) {
            return getAuthenticatedUser().getSessionClient();
        } else {
            return null;
        }
    }

    /**
     * Gets the selected session client id of the current authenticated user.
     *
     * @return session client id
     */
    public int getSessionClientId() {
        if (Objects.nonNull(getSessionClientOfAuthenticatedUser())) {
            return getSessionClientOfAuthenticatedUser().getId();
        }
        return 0;
    }

    /**
     * Finds the current authenticated user and loads object dto from index.
     *
     * @return The user dto or null if no user is authenticated.
     */
    public UserDTO findAuthenticatedUser() throws DataException {
        return findById(getAuthenticatedUser().getId());
    }

    public List<User> getByQuery(String query, String parameter) throws DAOException {
        return dao.search(query, parameter);
    }

    public List<User> getByQuery(String query, String namedParameter, String parameter) throws DAOException {
        return dao.search(query, namedParameter, parameter);
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM User WHERE deleted = 0");
    }

    @Override
    public Long countNotIndexedDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM User WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public String createCountQuery(Map filters) {
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewUserList()) {
            return null;
        }

        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewUserList()) {
            return createQueryAllActiveUsersForCurrentUser().toString();
        }
        return null;
    }

    @Override
    public List<User> getAllNotIndexed() {
        return getByQuery("FROM User WHERE indexAction = 'INDEX' OR indexAction IS NULL");
    }

    @Override
    public List<User> getAllForSelectedClient() {
        return dao.getByQuery("SELECT u FROM User AS u INNER JOIN u.clients AS c WITH c.id = :clientId",
            Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
    }

    @Override
    public List<UserDTO> findAll(String sort, Integer offset, Integer size, Map filters) throws DataException {
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewUserList()) {
            return convertJSONObjectsToDTOs(findAllDocuments(sortByLogin(), offset, size), false);
        }
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewUserList()) {
            return convertJSONObjectsToDTOs(searcher.findDocuments(createQueryAllActiveUsersForCurrentUser().toString(),
                sortByLogin(), offset, size), false);
        }
        return new ArrayList<>();
    }

    /**
     * Get amount of users with exactly the same login like given but different
     * id.
     *
     * @param id
     *            of user
     * @param login
     *            of user
     * @return amount of users with exactly the same login like given but
     *         different id
     */
    public Long getAmountOfUsersWithExactlyTheSameLogin(String id, String login) throws DataException {
        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
        if (id != null) {
            boolQuery.must(createSimpleQuery("_id", id, false));
        }
        boolQuery.must(createSimpleQuery(UserTypeField.LOGIN.getKey(), login, true));
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
    List<JsonObject> findByName(String name) throws DataException {
        QueryBuilder query = createSimpleQuery(UserTypeField.NAME.getKey(), name, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users with exact surname.
     *
     * @param surname
     *            of the searched user
     * @return list of JSON objects
     */
    List<JsonObject> findBySurname(String surname) throws DataException {
        QueryBuilder query = createSimpleQuery(UserTypeField.SURNAME.getKey(), surname, true, Operator.AND);
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
    List<JsonObject> findByFullName(String name, String surname) throws DataException {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(createSimpleQuery(UserTypeField.NAME.getKey(), name, true, Operator.AND));
        query.must(createSimpleQuery(UserTypeField.SURNAME.getKey(), surname, true, Operator.AND));
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find user with exact login.
     *
     * @param login
     *            of the searched user
     * @return JSON objects
     */
    JsonObject findByLogin(String login) throws DataException {
        QueryBuilder query = createSimpleQuery(UserTypeField.LOGIN.getKey(), login, true, Operator.AND);
        return searcher.findDocument(query.toString());
    }

    /**
     * Find user with exact LDAP login.
     *
     * @param ldapLogin
     *            of the searched user
     * @return search result
     */
    JsonObject findByLdapLogin(String ldapLogin) throws DataException {
        QueryBuilder query = createSimpleQuery(UserTypeField.LDAP_LOGIN.getKey(), ldapLogin, true, Operator.AND);
        return searcher.findDocument(query.toString());
    }

    /**
     * Find active or inactive users.
     *
     * @param active
     *            true - active user or false - inactive user
     * @return list of JSON objects
     */
    List<JsonObject> findByActive(boolean active) throws DataException {
        return searcher.findDocuments(getQueryForActive(active).toString(), sortByLogin());
    }

    private QueryBuilder getQueryForActive(boolean active) {
        return createSimpleQuery(UserTypeField.ACTIVE.getKey(), active, true);
    }

    /**
     * Find users with exact location.
     *
     * @param location
     *            of the searched user
     * @return list of JSON objects
     */
    List<JsonObject> findByLocation(String location) throws DataException {
        QueryBuilder query = createSimpleQuery(UserTypeField.LOCATION.getKey(), location, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users by id of role.
     *
     * @param id
     *            of role
     * @return list of JSON objects with users for specific role id
     */
    List<JsonObject> findByRoleId(Integer id) throws DataException {
        QueryBuilder query = createSimpleQuery(UserTypeField.ROLES + ".id", id, true);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users by title of role.
     *
     * @param title
     *            of role
     * @return list of JSON objects with users for specific role title
     */
    List<JsonObject> findByRoleTitle(String title) throws DataException {
        QueryBuilder query = createSimpleQuery(UserTypeField.ROLES + ".title", title, true, Operator.AND);
        return searcher.findDocuments(query.toString());
    }

    /**
     * Find users by filter.
     *
     * @param value
     *            of filter
     * @return list of JSON objects with users for specific filter
     */
    List<JsonObject> findByFilter(String value) throws DataException {
        List<JsonObject> filters = ServiceManager.getFilterService().findByValue(value, true);
        return searcher.findDocuments(createSetQuery("filters.id", filters, true).toString());
    }

    /**
     * Find users by processing tasks.
     *
     * @param id
     *            of filter
     * @return list of JSON objects with users for specific filter
     */
    List<UserDTO> findByProcessingTask(Integer id, boolean related) throws DataException {
        List<JsonObject> jsonObjects = searcher
                .findDocuments(createSimpleQuery("processingTasks.id", id, true).toString());
        return convertJSONObjectsToDTOs(jsonObjects, related);
    }

    /**
     * Find all visible users.
     *
     * @return a list of all visible users as UserDTO
     */
    public List<UserDTO> findAllVisibleUsers() throws DataException {
        List<JsonObject> jsonObjects = findAllDocuments(sortByLogin());
        return convertJSONObjectsToDTOs(jsonObjects, true);
    }

    /**
     * Find all visible users with related objects.
     *
     * @return a list of all visible users as UserDTO
     */
    public List<UserDTO> findAllVisibleUsersWithRelations() throws DataException {
        List<JsonObject> jsonObjects = findAllDocuments(sortByLogin());
        return convertJSONObjectsToDTOs(jsonObjects, false);
    }

    /**
     * Find all active users.
     *
     * @return a list of all active users as UserDTO
     */
    public List<UserDTO> findAllActiveUsers() throws DataException {
        List<JsonObject> jsonObjects = findByActive(true);
        return convertJSONObjectsToDTOs(jsonObjects, true);
    }

    /**
     * Find all active users wit related objects.
     *
     * @return a list of all active users as UserDTO
     */
    public List<UserDTO> findAllActiveUsersWithRelations() throws DataException {
        List<JsonObject> jsonObjects = findByActive(true);
        return convertJSONObjectsToDTOs(jsonObjects, false);
    }

    private String sortByLogin() {
        return SortBuilders.fieldSort(UserTypeField.LOGIN.getKey()).order(SortOrder.ASC).toString();
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
    public UserDTO convertJSONObjectToDTO(JsonObject jsonObject, boolean related) throws DataException {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(getIdFromJSONObject(jsonObject));
        JsonObject userJSONObject = jsonObject.getJsonObject("_source");
        userDTO.setLogin(UserTypeField.LOGIN.getStringValue(userJSONObject));
        userDTO.setName(UserTypeField.NAME.getStringValue(userJSONObject));
        userDTO.setSurname(UserTypeField.SURNAME.getStringValue(userJSONObject));
        userDTO.setActive(UserTypeField.ACTIVE.getBooleanValue(userJSONObject));
        userDTO.setLdapLogin(UserTypeField.LDAP_LOGIN.getStringValue(userJSONObject));
        userDTO.setLocation(UserTypeField.LOCATION.getStringValue(userJSONObject));
        userDTO.setFullName(getFullName(userDTO));
        userDTO.setFiltersSize(UserTypeField.FILTERS.getSizeOfProperty(userJSONObject));
        userDTO.setProjectsSize(UserTypeField.PROJECTS.getSizeOfProperty(userJSONObject));
        userDTO.setClientsSize(UserTypeField.CLIENTS.getSizeOfProperty(userJSONObject));
        userDTO.setRolesSize(UserTypeField.ROLES.getSizeOfProperty(userJSONObject));

        if (!related) {
            convertRelatedJSONObjects(userJSONObject, userDTO);
        } else {
            addBasicFilterRelation(userDTO, userJSONObject);
            addBasicProjectRelation(userDTO, userJSONObject);
            addBasicRoleRelation(userDTO, userJSONObject);
        }

        return userDTO;
    }

    private void convertRelatedJSONObjects(JsonObject jsonObject, UserDTO userDTO) throws DataException {
        userDTO.setFilters(convertRelatedJSONObjectToDTO(jsonObject, UserTypeField.FILTERS.getKey(),
            ServiceManager.getFilterService()));
        userDTO.setProjects(convertRelatedJSONObjectToDTO(jsonObject, UserTypeField.PROJECTS.getKey(),
            ServiceManager.getProjectService()));
        userDTO.setClients(convertRelatedJSONObjectToDTO(jsonObject, UserTypeField.CLIENTS.getKey(),
            ServiceManager.getClientService()));
        userDTO.setProcessingTasks(convertRelatedJSONObjectToDTO(jsonObject, UserTypeField.PROCESSING_TASKS.getKey(),
            ServiceManager.getTaskService()));
        userDTO.setRoles(
            convertRelatedJSONObjectToDTO(jsonObject, UserTypeField.ROLES.getKey(), ServiceManager.getRoleService()));
    }

    private void addBasicFilterRelation(UserDTO userDTO, JsonObject jsonObject) {
        if (userDTO.getFiltersSize() > 0) {
            List<FilterDTO> filters = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add(FilterTypeField.VALUE.getKey());
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject,
                UserTypeField.FILTERS.getKey(), subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                FilterDTO filter = new FilterDTO();
                filter.setId(relatedProperty.getId());
                if (!relatedProperty.getValues().isEmpty()) {
                    filter.setValue(relatedProperty.getValues().get(0));
                }
                filters.add(filter);
            }
            userDTO.setFilters(filters);
        }
    }

    private void addBasicProjectRelation(UserDTO userDTO, JsonObject jsonObject) {
        if (userDTO.getProjectsSize() > 0) {
            List<ProjectDTO> projects = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add(ProcessTypeField.TITLE.getKey());
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject,
                UserTypeField.PROJECTS.getKey(), subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                ProjectDTO project = new ProjectDTO();
                project.setId(relatedProperty.getId());
                if (!relatedProperty.getValues().isEmpty()) {
                    project.setTitle(relatedProperty.getValues().get(0));
                }
                project.setTitle(relatedProperty.getValues().get(0));
                projects.add(project);
            }
            userDTO.setProjects(projects);
        }
    }

    private void addBasicRoleRelation(UserDTO userDTO, JsonObject jsonObject) {
        if (userDTO.getRolesSize() > 0) {
            List<RoleDTO> roles = new ArrayList<>();
            List<String> subKeys = new ArrayList<>();
            subKeys.add(RoleTypeField.TITLE.getKey());
            List<RelatedProperty> relatedProperties = getRelatedArrayPropertyForDTO(jsonObject,
                UserTypeField.ROLES.getKey(), subKeys);
            for (RelatedProperty relatedProperty : relatedProperties) {
                RoleDTO role = new RoleDTO();
                role.setId(relatedProperty.getId());
                if (!relatedProperty.getValues().isEmpty()) {
                    role.setTitle(relatedProperty.getValues().get(0));
                }
                roles.add(role);
            }
            userDTO.setRoles(roles);
        }
    }

    /**
     * Check validity of given login.
     * 
     * @param login
     *            to validation
     * @return true or false
     */
    public boolean isLoginValid(String login) {
        String patternString = "[A-Za-z0-9@_\\-.]*";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(login);
        if (!matcher.matches()) {
            Helper.setErrorMessage(LOGIN_NOT_VALID, new Object[] {login });
            return false;
        }

        return isLoginAllowed(login);
    }

    private boolean isLoginAllowed(String login) {
        // If user defined blacklist doesn't exists, use default one
        if (!KitodoConfigFile.LOGIN_BLACKLIST.exists()) {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            try (InputStream inputStream = classloader.getResourceAsStream(KitodoConfigFile.LOGIN_BLACKLIST.getName());
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(inputStreamReader)) {
                if (isLoginFoundOnBlackList(reader, login)) {
                    return false;
                }
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
                return false;
            }
        }
        // Go through the user defined blacklist file line by line and compare with login
        try (FileInputStream inputStream = new FileInputStream(KitodoConfigFile.LOGIN_BLACKLIST.getFile());
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(inputStreamReader)) {
            if (isLoginFoundOnBlackList(reader, login)) {
                return false;
            }
        } catch (IOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            return false;
        }
        return true;
    }

    private boolean isLoginFoundOnBlackList(BufferedReader reader, String login) throws IOException {
        String notAllowedLogin;
        while ((notAllowedLogin = reader.readLine()) != null) {
            if (notAllowedLogin.length() > 0 && login.equalsIgnoreCase(notAllowedLogin)) {
                Helper.setErrorMessage(LOGIN_NOT_VALID, new Object[] {login });
                return true;
            }
        }
        return false;
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
        if (Objects.nonNull(user)) {
            if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.LDAP_USE)) {
                result = Paths.get(ServiceManager.getLdapServerService().getUserHomeDirectory(user)).toUri();
            } else {
                result = Paths.get(ConfigCore.getParameter(ParameterCore.DIR_USERS), user.getLogin()).toUri();
            }

            if (!new File(result).exists()) {
                ServiceManager.getFileService().createDirectoryForUser(result, user.getLogin());
            }
        } else {
            throw new IOException("No user for home directory!");
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
        ServiceManager.getFilterService().save(filter);
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
                ServiceManager.getFilterService().remove(filter);
            }
        }
        refresh(user);
    }

    /**
     * Retrieve and return the list of tasks that are assigned to the user and
     * that are "INWORK" and belong to process, not template.
     *
     * @return list of tasks that are currently assigned to the user and that
     *         are "INWORK" and belong to process, not template
     */
    public List<Task> getTasksInProgress(User user) {
        return user.getProcessingTasks().stream().filter(
            task -> task.getProcessingStatusEnum().equals(TaskStatus.INWORK) && Objects.nonNull(task.getProcess()))
                .collect(Collectors.toList());
    }

    private QueryBuilder createQueryAllActiveUsersForCurrentUser() {
        BoolQueryBuilder query = new BoolQueryBuilder();
        query.must(getQueryForActive(true));
        query.must(createSimpleQuery(UserTypeField.CLIENTS + ".id", getSessionClientId(), true));
        return query;
    }

    /**
     * Changes the password for given User object.
     * 
     * @param user
     *            The User object.
     * @param newPassword
     *            The new password.
     */
    public void changeUserPassword(User user, String newPassword) throws DataException {
        User userWithNewPassword;
        if (user instanceof SecurityUserDetails) {
            userWithNewPassword = new User(user);
        } else {
            userWithNewPassword = user;
        }
        userWithNewPassword.setPassword(passwordEncoder.encrypt(newPassword));
        save(userWithNewPassword);
    }
}
