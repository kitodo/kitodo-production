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

import java.util.List;

/**
 * Authorization DTO object.
 */
public class AuthorizationDTO extends BaseDTO {
    private String title;
    private List<UserGroupDTO> userGroups;
    private Integer userGroupsSize;
    private boolean panelShown = false;

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
     * Get list of user groups.
     *
     * @return list of user groups as userGroupDTO
     */
    public List<UserGroupDTO> getUserGroups() {
        return userGroups;
    }

    /**
     * Set list of user groups.
     *
     * @param userGroups
     *            list of users as UserGroupDTO
     */
    public void setUserGroups(List<UserGroupDTO> userGroups) {
        this.userGroups = userGroups;
    }

    /**
     * Get size of user groups.
     *
     * @return size of user groups as Integer
     */
    public Integer getUserGroupsSize() {
        return userGroupsSize;
    }

    /**
     * Set size of user groups.
     *
     * @param usersSize
     *            as Integer
     */
    public void setUserGroupsSize(Integer usersSize) {
        this.userGroupsSize = usersSize;
    }

    /**
     * Get information if panel is shown.
     *
     * @return true or false
     */
    public boolean isPanelShown() {
        return this.panelShown;
    }

    /**
     * Set information if panel is shown.
     *
     * @param panelShown
     *            as boolean
     */
    public void setPanelShown(boolean panelShown) {
        this.panelShown = panelShown;
    }
}
