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

import java.util.List;

/**
 * Interface for working with clients. Clients are related organizations that
 * are allowed to use this instance of Production for their own projects. Users
 * can work for several clients or just one.
 */
public interface ClientInterface extends DataInterface {

    /**
     * Returns the name of the client.
     *
     * @return the name of the client
     */
    String getName();

    /**
     * Sets the name of the client.
     *
     * @param name
     *            the name of the client
     */
    void setName(String name);

    /**
     * Specifies the users who work for this client. This list is not guaranteed
     * to be in reliable order.
     *
     * @return the users who work for this client
     */
    List<UserInterface> getUsers();

    /**
     * Sets the list of users working for this client. The list should not
     * contain duplicates, and must not contain {@code null}s.
     *
     * @param users
     *            The users.
     */
    void setUsers(List<UserInterface> users);

    /**
     * Returns the client's projects. This list is not guaranteed to be in
     * reliable order.
     *
     * @return the client's projects
     */
    List<ProjectInterface> getProjects();

    /**
     * Sets the lists of the client's projects. The list should not contain
     * duplicates, and must not contain {@code null}s.
     *
     * @param projects
     *            The projects.
     */
    void setProjects(List<ProjectInterface> projects);
}
