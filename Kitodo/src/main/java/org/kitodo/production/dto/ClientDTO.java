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

package org.kitodo.production.dto;

import java.util.ArrayList;
import java.util.List;

import org.kitodo.data.interfaces.ClientInterface;
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.data.interfaces.UserInterface;

public class ClientDTO extends BaseDTO implements ClientInterface {

    private String name;
    private List<? extends UserInterface> users = new ArrayList<>();
    private List<? extends ProjectInterface> projects = new ArrayList<>();

    /**
     * Gets title.
     *
     * @return The title.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets title.
     *
     * @param name
     *            The title.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets users.
     *
     * @return The users.
     */
    public List<? extends UserInterface> getUsers() {
        return users;
    }

    /**
     * Sets users.
     *
     * @param users
     *            The users.
     */
    public void setUsers(List<? extends UserInterface> users) {
        this.users = users;
    }

    /**
     * Gets projects.
     *
     * @return The projects.
     */
    public List<? extends ProjectInterface> getProjects() {
        return projects;
    }

    /**
     * Sets projects.
     *
     * @param projects
     *            The projects.
     */
    public void setProjects(List<? extends ProjectInterface> projects) {
        this.projects = projects;
    }
}
