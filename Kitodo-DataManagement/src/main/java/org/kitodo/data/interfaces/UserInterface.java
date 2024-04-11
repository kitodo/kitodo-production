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

package org.kitodo.data.interfaces;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * An interface for editing users of the web application.
 */
public interface UserInterface extends DataInterface {

    /**
     * Returns the user's login name.
     *
     * @return the user name
     */
    String getLogin();

    /**
     * Sets the user's login name. Since the user is also created as a Linux
     * system user, the user name should follow the naming conventions and not
     * match any existing Linux user. To avoid hassle when accessing file space
     * using case-insensitive CIFS, it is recommended to only use lowercase
     * letters in the name.
     *
     * @param login
     *            user name to set
     */
    void setLogin(String login);

    /**
     * Returns the user's first name.
     *
     * @return the first name
     */
    String getName();

    /**
     * Sets the user's first name.
     *
     * @param name
     *            first name to set
     */
    void setName(String name);

    /**
     * Returns the user's surname.
     *
     * @return the surname
     */
    String getSurname();

    /**
     * Sets the user's surname.
     *
     * @param surname
     *            surname to set
     */
    void setSurname(String surname);

    /**
     * Returns the user's full name. The spelling is last name—comma—first name.
     *
     * @return the full name
     */
    String getFullName();

    /**
     * Sets the user's full name.
     *
     * @param fullName
     *            full name to set
     */
    void setFullName(String fullName);

    /**
     * Returns the user's location. Users from different locations collaborate
     * in this web application. Specifying the location simplifies user
     * maintenance.
     *
     * @return the location
     */
    String getLocation();

    /**
     * Sets the user's location.
     *
     * @param location
     *            place name
     */
    void setLocation(String location);

    /**
     * Returns the login name to the directory service. This provides the
     * ability to use a username independent of the Linux username to
     * authenticate against a corporate directory service. For logging into the
     * web application, the user can use either username.
     *
     * @return different user name for the directory service
     */
    String getLdapLogin();

    /**
     * Sets a different user name for the directory service. The emphasis here
     * is on "different", the remote username must not be the same as <i>any</i>
     * primary username. Multiple local users cannot be mapped to the same
     * remote username either.
     *
     * @param ldapLogin
     *            different user name to set
     */
    void setLdapLogin(String ldapLogin);

    /**
     * Returns whether the user is logged in. This allows a administrators to
     * check whether they can stop the application for maintenance purposes, or
     * who they needs to call first.
     *
     * @return whether the user is logged in
     */
    boolean isActive();

    /**
     * Sets whether the user is logged in. The setter can be used when
     * representing data from a third-party source. Internally, this depends on
     * the existence of a user session in the servlet container. This cannot be
     * changed here.
     *
     * @param active
     *            as boolean
     * @throws UnsupportedOperationException
     *             when trying to change this
     */
    default void setActive(boolean active) {
        if (active != isActive()) {
            throw new UnsupportedOperationException(active ? "cannot log user in" : "cannot log user out");
        }
    }

    /**
     * Returns the user's saved search queries. This list is not guaranteed to
     * be in reliable order.
     *
     * @return the saved search queries
     */
    List<? extends FilterInterface> getFilters();

    /**
     * Sets a list of the user's saved searches. The list should not contain
     * duplicates, and must not contain {@code null}s.
     *
     * @param filters
     *            list of saved search queries to set
     */
    void setFilters(List<? extends FilterInterface> filters);

    /**
     * Returns the number of saved search queries.
     *
     * @return the number of saved search queries
     */
    default Integer getFiltersSize() {
        List<? extends FilterInterface> queries = getFilters();
        return Objects.nonNull(queries) ? queries.size() : null;
    }

    /**
     * Sets the number of saved search queries. The setter can be used when
     * representing data from a third-party source. Internally it depends on,
     * whether there are filter objects in the database linked to the user. No
     * additional filters can be added to the process here.
     *
     * @param size
     *            how many users hold this role to set
     * @throws UnsupportedOperationException
     *             when trying to add unspecified filters to this user
     * @throws IndexOutOfBoundsException
     *             for an illegal endpoint index value
     */
    default void setFiltersSize(Integer filtersSize) {
        if (Objects.isNull(filtersSize)) {
            setFilters(null);
            return;
        }
        List<? extends FilterInterface> filters = Optional.of(getFilters()).orElse(Collections.emptyList());
        if (filtersSize == filters.size()) {
            return;
        }
        while (filtersSize > filters.size()) {
            throw new UnsupportedOperationException("cannot add arbitrary filters");
        }
        setFilters(filters.subList(0, filtersSize));
    }

    /**
     * Returns all of the user's roles. This list is not guaranteed to be in
     * reliable order.
     *
     * @return a list of all roles of the user
     */
    List<? extends RoleInterface> getRoles();

    /**
     * Sets a list of all of the user's roles.
     *
     * @param roles
     *            list to set
     */
    void setRoles(List<? extends RoleInterface> roles);

    /**
     * Returns the number of roles the user has.
     *
     * @return the number of roles
     */
    default int getRolesSize() {
        List<? extends RoleInterface> roles = getRoles();
        return Objects.nonNull(roles) ? roles.size() : 0;
    }

    /**
     * Sets the number of roles the user has. The setter can be used when
     * representing data from a third-party source. Internally it depends on,
     * whether there are role objects in the database linked to the user. No
     * additional roles can be added to the user here.
     *
     * @param size
     *            number of roles to set
     * @throws SecurityException
     *             when trying to assign unspecified roles to this user
     * @throws IndexOutOfBoundsException
     *             for an illegal endpoint index value
     */
    default void setRolesSize(Integer rolesSize) {
        int newSize = Objects.nonNull(rolesSize) ? rolesSize : 0;
        List<? extends RoleInterface> users = Optional.of(getRoles()).orElse(Collections.emptyList());
        int currentSize = users.size();
        if (newSize == currentSize) {
            return;
        }
        if (newSize > currentSize) {
            throw new SecurityException("cannot add arbitrary roles");
        }
        setRoles(users.subList(0, newSize));
    }

    /**
     * Returns all clients the user interacts with.
     *
     * @return the clients
     */
    List<? extends ClientInterface> getClients();

    /**
     * Sets the list of all clients that the user interacts with.
     *
     * @param clients
     *            clients to set
     */
    void setClients(List<? extends ClientInterface> clients);

    /**
     * Returns the number of clients the user interacts with.
     *
     * @return the number of clients
     */
    default int getClientsSize() {
        List<? extends ClientInterface> clients = getClients();
        return Objects.nonNull(clients) ? clients.size() : 0;
    }

    /**
     * Sets the number of roles the user has. The setter can be used when
     * representing data from a third-party source. Internally it depends on,
     * whether there are role objects in the database linked to the user. No
     * additional roles can be added to the user here.
     *
     * @param size
     *            number of roles to set
     * @throws SecurityException
     *             when trying to assign unspecified roles to this user
     * @throws IndexOutOfBoundsException
     *             for an illegal endpoint index value
     */
    default void setClientsSize(Integer clientsSize) {
        int newSize = Objects.nonNull(clientsSize) ? clientsSize : 0;
        List<? extends ClientInterface> users = Optional.of(getClients()).orElse(Collections.emptyList());
        int currentSize = users.size();
        if (newSize == currentSize) {
            return;
        }
        if (newSize > currentSize) {
            throw new SecurityException("cannot add arbitrary clients");
        }
        setClients(users.subList(0, newSize));
    }

    /**
     * Returns all projects the user collaborates on.
     *
     * @return all projects
     */
    List<? extends ProjectInterface> getProjects();

    /**
     * Sets the list of all projects the user is working on.
     *
     * @param projects
     *            list of projects to set
     */
    void setProjects(List<? extends ProjectInterface> projects);

    /**
     * Returns the number of projects the user is working on.
     *
     * @return the number of projects
     */
    default int getProjectsSize() {
        List<? extends ProjectInterface> projects = getProjects();
        return Objects.nonNull(projects) ? projects.size() : 0;
    }

    /**
     * Sets the number of roles the user has. The setter can be used when
     * representing data from a third-party source. Internally it depends on,
     * whether there are role objects in the database linked to the user. No
     * additional roles can be added to the user here.
     *
     * @param size
     *            number of roles to set
     * @throws UnsupportedOperationException
     *             when trying to assign unspecified projects to this user
     * @throws IndexOutOfBoundsException
     *             for an illegal endpoint index value
     */
    default void setProjectsSize(Integer projectsSize) {
        int newSize = Objects.nonNull(projectsSize) ? projectsSize : 0;
        List<? extends ProjectInterface> users = Optional.of(getProjects()).orElse(Collections.emptyList());
        int currentSize = users.size();
        if (newSize == currentSize) {
            return;
        }
        if (newSize > currentSize) {
            throw new UnsupportedOperationException("cannot add arbitrary projects");
        }
        setProjects(users.subList(0, newSize));
    }

    /**
     * Returns all tasks the user is currently working on.
     *
     * @return all tasks the user is working on
     */
    List<? extends TaskInterface> getProcessingTasks();

    /**
     * Sets a list of all tasks that the user should work on.
     *
     * @param processingTasks
     *            list of tasks to set
     */
    void setProcessingTasks(List<? extends TaskInterface> processingTasks);
}
