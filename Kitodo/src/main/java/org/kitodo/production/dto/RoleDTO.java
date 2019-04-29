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

/**
 * Role DTO object.
 */
public class RoleDTO extends BaseDTO {

    private String title;
    private List<UserDTO> users = new ArrayList<>();
    private Integer usersSize;
    private ClientDTO client;

    /**
     * Get title.
     *
     * @return title as String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get list of users.
     *
     * @return list of users as UserDTO
     */
    public List<UserDTO> getUsers() {
        return users;
    }

    /**
     * Set list of users.
     *
     * @param users
     *            list of users as UserDTO
     */
    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }

    /**
     * Get size of users.
     *
     * @return size of users as Integer
     */
    public Integer getUsersSize() {
        return usersSize;
    }

    /**
     * Set size of users.
     *
     * @param usersSize
     *            as Integer
     */
    public void setUsersSize(Integer usersSize) {
        this.usersSize = usersSize;
    }

    /**
     * Get client FTO object.
     *
     * @return the client DTO object
     */
    public ClientDTO getClient() {
        return client;
    }

    /**
     * Set client DTO object.
     *
     * @param client as DTO object.
     */
    public void setClient(ClientDTO client) {
        this.client = client;
    }
}
