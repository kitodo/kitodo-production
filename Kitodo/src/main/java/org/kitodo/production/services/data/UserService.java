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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.UserDAO;
import org.kitodo.exceptions.FilterException;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.security.password.SecurityPasswordEncoder;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.SortOrder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserService extends BaseBeanService<User, UserDAO> implements UserDetailsService {

    private static final Logger logger = LogManager.getLogger(UserService.class);
    private static volatile UserService instance = null;
    private static final String CLIENT_ID = "clientId";
    private final SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();
    private static final int DEFAULT_CLIENT_ID =
            ConfigCore.getIntParameterOrDefaultValue(ParameterCore.DEFAULT_CLIENT_ID);

    /**
     * Constructor.
     */
    private UserService() {
        super(new UserDAO());
    }

    /**
     * Return singleton variable of type UserService.
     *
     * @return unique instance of UserService
     */
    public static UserService getInstance() {
        UserService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (UserService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new UserService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    @Override
    public Long count() throws DAOException {
        return count("SELECT COUNT(*) FROM User WHERE deleted = false");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        HashMap<String, Object> filterMap;
        try {
            filterMap = ServiceManager.getFilterService().getSQLFilterMap(filters, User.class);
        } catch (NoSuchFieldException | NumberFormatException e) {
            throw new FilterException(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        String sqlFilterString = ServiceManager.getFilterService().mapToSQLFilterString(filterMap.keySet());
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewUserList()) {
            return count("SELECT COUNT(*) FROM User WHERE deleted = false" + sqlFilterString, filterMap);
        }

        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewUserList()) {
            filterMap.put(CLIENT_ID, getSessionClientId());
            return count(
                "SELECT COUNT(*) FROM User u INNER JOIN u.clients AS c WITH c.id = :clientId WHERE deleted = false"
                        + sqlFilterString, filterMap);
        }
        return 0L;
    }

    /**
     * Get list of all objects for selected client from database.
     *
     * @return list of all objects for selected client from database
     */
    public List<User> getAllForSelectedClient() {
        return getByQuery(
            "SELECT u FROM User AS u INNER JOIN u.clients AS c WITH c.id = :clientId WHERE deleted = false",
            Collections.singletonMap(CLIENT_ID, getSessionClientId()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return new SecurityUserDetails(getByLdapLoginOrLogin(username));
    }

    @Override
    public List<User> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        HashMap<String, Object> filterMap;
        try {
            filterMap = ServiceManager.getFilterService().getSQLFilterMap(filters, User.class);
        } catch (NoSuchFieldException | NumberFormatException e) {
            throw new FilterException(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        String sqlFilterString = ServiceManager.getFilterService().mapToSQLFilterString(filterMap.keySet());
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewUserList()) {
            return dao.getByQuery("FROM User WHERE deleted = false" + sqlFilterString + getSort(sortField, sortOrder),
                    filterMap, first, pageSize);
        }
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewUserList()) {
            filterMap.put(CLIENT_ID, getSessionClientId());
            return dao.getByQuery(
                "SELECT u FROM User AS u INNER JOIN u.clients AS c WITH c.id = :clientId WHERE deleted = false"
                        + sqlFilterString + getSort(sortField, sortOrder),
                filterMap, first, pageSize);
        }
        return new ArrayList<>();
    }

    /**
     * Returns a user by his or her LDAP login or common login.
     *
     * @param login
     *            the login of the user to look for
     * @return the user, if one is found
     * @throws UsernameNotFoundException
     *             if no unique user can be found
     */
    public User getByLdapLoginOrLogin(String login) {
        List<User> users = getByLoginQuery(login, "from User where ldapLogin = :login or login = :login");
        return uniqueResult(users, login);
    }

    /**
     * Gets user by login.
     *
     * @param login
     *            The login.
     * @return The user.
     * @throws UsernameNotFoundException
     *             if no unique user can be found
     */
    public User getByLogin(String login) {
        List<User> users = getByLoginQuery(login, "from User where login = :login");
        return uniqueResult(users, login);
    }

    private List<User> getByLoginQuery(String login, String query) {
        return getByQuery(query, Collections.singletonMap("login", login));
    }

    private User uniqueResult(List<User> users, String login) {
        if (users.size() == 1) {
            return users.get(0);
        } else if (users.isEmpty()) {
            throw new UsernameNotFoundException("Login '" + login + "' not found!");
        } else {
            logger.error("Login '{}' was found more than once! Affected IDs: {}", login,
                    users.stream().map(user -> Objects.toString(user.getId())).collect(Collectors.joining(", ")));
            throw new UsernameNotFoundException("Login '" + login + "' was found more than once!");
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
        SecurityUserDetails authenticatedUser = getAuthenticatedUser();
        if (Objects.nonNull(authenticatedUser)) {
            return new User(authenticatedUser);
        } else {
            throw new NoSuchElementException("There is currently no authenticated user for this thread");
        }
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
        return DEFAULT_CLIENT_ID;
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
    public Long getAmountOfUsersWithExactlyTheSameLogin(Integer id, String login) throws DAOException {
        return dao.countUsersWithExactlyTheSameLogin(id, login);
    }

    /**
     * Get all active users sorted by surname and name.
     *
     * @return sorted list of all active users as User objects
     */
    public List<User> getAllActiveUsersSortedByNameAndSurname() {
        return dao.getAllActiveUsersSortedByNameAndSurname();
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
            return false;
        }

        return isLoginAllowed(login);
    }

    private boolean isLoginAllowed(String login) {
        KitodoConfigFile blacklist = KitodoConfigFile.LOGIN_BLACKLIST;
        // If user defined blacklist doesn't exists, use default one
        try (InputStream inputStream = blacklist.exists() ? Files.newInputStream(blacklist.getFile().toPath())
                : Thread.currentThread().getContextClassLoader().getResourceAsStream(blacklist.getName());
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

    /**
     * Go through the user defined blacklist file line by line and compare with
     * login.
     */
    private boolean isLoginFoundOnBlackList(BufferedReader reader, String login) throws IOException {
        String notAllowedLogin;
        while ((notAllowedLogin = reader.readLine()) != null) {
            if (notAllowedLogin.length() > 0 && login.equalsIgnoreCase(notAllowedLogin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * At that moment only add this method.
     *
     * @param user
     *            object as UserDTo
     * @return full name of the user as String
     */
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
        URI homeDirectory;
        if (Objects.nonNull(user)) {
            if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.LDAP_USE)) {
                homeDirectory = Paths.get(ServiceManager.getLdapServerService().getUserHomeDirectory(user)).toUri();
            } else {
                homeDirectory = Paths.get(ConfigCore.getParameter(ParameterCore.DIR_USERS), user.getLogin()).toUri();
            }

            if (!new File(homeDirectory).exists()) {
                ServiceManager.getFileService().createDirectoryForUser(homeDirectory, user.getLogin());
            }
        } else {
            throw new IOException("No user for home directory!");
        }
        return homeDirectory;
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
        } catch (DAOException e) {
            logger.error("Cannot not add filter to user with id {}", user.getId(), e);
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
        } catch (DAOException e) {
            logger.error("Cannot not remove filter from user with id {}", user.getId(), e);
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
    private void addFilterToUser(User user, String userFilter) throws DAOException {
        Filter filter = new Filter();
        filter.setValue(userFilter);
        filter.setCreationDate(new Date());
        filter.setUser(user);
        ServiceManager.getFilterService().save(filter);
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
    private void removeFilterFromUser(User user, String userFilter) throws DAOException {
        List<Filter> filters = user.getFilters();
        for (Filter filter : filters) {
            if (filter.getValue().equals(userFilter)) {
                ServiceManager.getFilterService().remove(filter);
            }
        }
    }

    /**
     * Retrieve and return the list of tasks that are assigned to the user and
     * that are "INWORK" and belong to process, not template.
     *
     * @return list of tasks that are currently assigned to the user and that
     *         are "INWORK" and belong to process, not template
     */
    public List<Task> getTasksInProgress(User user) {
        return user.getProcessingTasks().stream()
                .filter(
                    task -> task.getProcessingStatus().equals(TaskStatus.INWORK) && Objects.nonNull(task.getProcess()))
                .collect(Collectors.toList());
    }

    /**
     * Changes the password for given User object.
     *
     * @param user
     *            The User object.
     * @param newPassword
     *            The new password.
     */
    public void changeUserPassword(User user, String newPassword) throws DAOException {
        User userWithNewPassword;
        if (user instanceof SecurityUserDetails) {
            userWithNewPassword = new User(user);
        } else {
            userWithNewPassword = user;
        }
        userWithNewPassword.setPassword(passwordEncoder.encrypt(newPassword));
        save(userWithNewPassword);
    }

    /**
     * Gets the configured metadatalanguage from the User.
     * @return a List of LanguageRange
     */
    public List<Locale.LanguageRange> getCurrentMetadataLanguage() {
        String metadataLanguage = getCurrentUser().getMetadataLanguage();
        return Locale.LanguageRange.parse(metadataLanguage.isEmpty() ? "en" : metadataLanguage);
    }

    /**
     * Return the keyboard shortcuts for the specified user.
     *
     * @param userId specifying the user
     * @return JSON object containing the user's shortcuts as java.lang.String
     * @throws DAOException when user object could not be loaded from database
     */
    public String getShortcuts(int userId) throws DAOException {
        return getById(userId).getShortcuts();
    }

    /**
     * Retrieves the list of clients available to the currently authenticated user.
     *
     * @return list of {@code Client} objects associated with the current user
     */
    public List<Client> getAvailableClientsOfCurrentUser() {
        return getClientsOfUser(getCurrentUser());
    }

    /**
     * Retrieves the list of clients available to the currently authenticated user, sorted by name.
     *
     * @return list of {@code Client} objects associated with the current user, sorted by name
     */
    public List<Client> getAvailableClientsOfCurrentUserSortedByName() {
        return getClientsOfUserSorted(getCurrentUser());
    }

    /**
     * Retrieves the list of clients associated with the specified user. This includes
     * clients directly linked to the user and clients of the projects the user is
     * associated with, ensuring no duplicate clients are included in the list.
     *
     * @param user the user for whom the associated clients are retrieved
     * @return a list of clients associated with the specified user
     */
    public static List<Client> getClientsOfUser(User user) {
        List<Client> clients = user.getClients();
        for (Project project : user.getProjects()) {
            if (!clients.contains(project.getClient())) {
                clients.add(project.getClient());
            }
        }
        return clients;
    }

    /**
     * Retrieves the list of clients associated with the specified user, sorted by
     * the client's name in a case-insensitive manner.
     *
     * @param user the user for whom the associated and sorted clients are retrieved
     * @return a list of clients associated with the specified user, sorted by name
     */
    public static List<Client> getClientsOfUserSorted(User user) {
        return getClientsOfUser(user).stream()
                .sorted(Comparator.comparing(Client::getName, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }
}
