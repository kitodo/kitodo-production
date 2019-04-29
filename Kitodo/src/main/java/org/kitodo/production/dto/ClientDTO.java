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

public class ClientDTO extends BaseDTO {

    private String name;
    private List<UserDTO> users = new ArrayList<>();
    private List<ProjectDTO> projects = new ArrayList<>();

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
    public List<UserDTO> getUsers() {
        return users;
    }

    /**
     * Sets users.
     *
     * @param users
     *            The users.
     */
    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }

    /**
     * Gets projects.
     *
     * @return The projects.
     */
    public List<ProjectDTO> getProjects() {
        return projects;
    }

    /**
     * Sets projects.
     *
     * @param projects
     *            The projects.
     */
    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }
}
