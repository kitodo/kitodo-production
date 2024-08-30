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
 * An interface for managing user roles. Users can have different task-related
 * or general-administrative roles, including different roles for different
 * clients they work for. A role is always assigned to one client.
 */
public interface RoleInterface extends BaseBeanInterface {

    /**
     * Returns the name of the role.
     *
     * @return the name of the role
     */
    String getTitle();

    /**
     * Sets the name of the role.
     *
     * @param title
     *            name of the role to set
     */
    void setTitle(String title);

    /**
     * Specifies the users who hold this role. This list is not guaranteed to be
     * in reliable order.
     *
     * @return list of users who hold this role
     */
    List<User> getUsers();

    /**
     * Sets the list of users who hold this role.
     *
     * @param users
     *            list of users who hold this role to set
     */
    void setUsers(List<User> users);

    /**
     * Returns how many users hold this role.
     *
     * @return how many users hold this role
     * @deprecated Use {@link #getUsers()}{@code .size()}.
     */
    @Deprecated
    default Integer getUsersSize() {
        List<User> users = getUsers();
        return Objects.nonNull(users) ? users.size() : null;
    }

    /**
     * Sets how many users hold this role. The setter can be used when
     * representing data from a third-party source. Internally it depends on,
     * whether there are user objects in the database linked to the role. No
     * additional users can be added to the role here.
     *
     * @param size
     *            how many users hold this role to set
     * @throws SecurityException
     *             when trying to assign this role to unspecified users
     * @throws IndexOutOfBoundsException
     *             for an illegal endpoint index value
     * @deprecated {@link #getUsers()} and edit them consciously.
     */
    @Deprecated
    default void setUsersSize(Integer size) {
        int newSize = Objects.nonNull(size) ? size : 0;
        List<User> users = Optional.of(getUsers()).orElse(Collections.emptyList());
        int currentSize = users.size();
        if (newSize == currentSize) {
            return;
        }
        if (newSize > currentSize) {
            throw new SecurityException("cannot add arbitrary users");
        }
        setUsers(users.subList(0, newSize));
    }

    /**
     * Returns the client in whose realm this role grants permissions.
     *
     * @return the client in whose realm this role grants permissions
     */
    Client getClient();

    /**
     * Sets the client in whose realm this role grants permissions.
     *
     * @param client
     *            client in whose realm this role grants permissions to set.
     */
    void setClient(Client client);
}
