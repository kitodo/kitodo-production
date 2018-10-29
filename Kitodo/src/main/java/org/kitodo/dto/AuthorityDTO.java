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

package org.kitodo.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Authority DTO object.
 */
public class AuthorityDTO extends BaseDTO {
    private String title;
    private List<RoleDTO> roles = new ArrayList<>();
    private int rolesSize;

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
     * Get list of roles.
     *
     * @return list of roles as RoleDTO
     */
    public List<RoleDTO> getRoles() {
        return roles;
    }

    /**
     * Set list of roles.
     *
     * @param roles
     *            list of roles as RoleDTO
     */
    public void setRoles(List<RoleDTO> roles) {
        this.roles = roles;
    }

    /**
     * Get size of roles.
     *
     * @return size of roles as int
     */
    public int getRolesSize() {
        return rolesSize;
    }

    /**
     * Set size of roles.
     *
     * @param rolesSize
     *            as int
     */
    public void setRolesSize(int rolesSize) {
        this.rolesSize = rolesSize;
    }
}
