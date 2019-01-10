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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.LocalDateTime;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Filter;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.helper.enums.TaskStatus;
import org.kitodo.data.database.persistence.UserDAO;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.dto.UserDTO;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.security.SecurityUserDetails;
import org.kitodo.production.security.password.SecurityPasswordEncoder;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.base.SearchDatabaseService;
import org.primefaces.model.SortOrder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserService extends SearchDatabaseService<User, UserDAO> implements UserDetailsService {

    private static final Logger logger = LogManager.getLogger(UserService.class);
    private static UserService instance = null;
    private static final String LOGIN_NOT_VALID = "loginNotValid";
    private SecurityPasswordEncoder passwordEncoder = new SecurityPasswordEncoder();

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
        if (Objects.equals(instance, null)) {
            synchronized (UserService.class) {
                if (Objects.equals(instance, null)) {
                    instance = new UserService();
                }
            }
        }
        return instance;
    }

    @Override
    public Long countDatabaseRows() throws DAOException {
        return countDatabaseRows("SELECT COUNT(*) FROM User WHERE deleted = 0");
    }

    @Override
    public Long countResults(Map filters) throws DAOException {
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewUserList()) {
            return countDatabaseRows();
        }

        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewUserList()) {
            return countDatabaseRows("SELECT COUNT(*) FROM User u INNER JOIN u.clients AS c WITH c.id = :clientId WHERE deleted = 0");
        }
        return 0L;
    }

    @Override
    public List<User> getAllForSelectedClient() {
        return dao.getByQuery(
                "SELECT u FROM User AS u INNER JOIN u.clients AS c WITH c.id = :clientId WHERE deleted = 0",
                Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()));
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

    @Override
    @SuppressWarnings("unchecked")
    public List<User> loadData(int first, int pageSize, String sortField, SortOrder sortOrder, Map filters) {
        if (ServiceManager.getSecurityAccessService().hasAuthorityGlobalToViewUserList()) {
            return dao.getByQuery("FROM User WHERE deleted = 0", filters, first, pageSize);
        }
        if (ServiceManager.getSecurityAccessService().hasAuthorityToViewUserList()) {
            return dao.getByQuery(
                    "SELECT u FROM User AS u INNER JOIN u.clients AS c WITH c.id = :clientId WHERE deleted = 0",
                    Collections.singletonMap("clientId", ServiceManager.getUserService().getSessionClientId()), first,
                    pageSize);
        }
        return new ArrayList<>();
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

    public List<User> getByQuery(String query, String parameter) throws DAOException {
        return dao.search(query, parameter);
    }

    public List<User> getByQuery(String query, String namedParameter, String parameter) throws DAOException {
        return dao.search(query, namedParameter, parameter);
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
    public Long getAmountOfUsersWithExactlyTheSameLogin(String id, String login) throws DAOException {
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
        saveToDatabase(userWithNewPassword);
    }
}
