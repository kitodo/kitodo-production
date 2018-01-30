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
 * UserGroup DTO object.
 */
public class UserGroupDTO extends BaseDTO {

    private String title;
    private List<UserDTO> users;
    private Integer usersSize;
    private List<AuthorityDTO> authorizations;
    private Integer authorizationsSize;
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
     * Get list of authorizations.
     *
     * @return list of authorizations as AuthorityDTO
     */
    public List<AuthorityDTO> getAuthorizations() {
        return authorizations;
    }

    /**
     * Set list of authorizations.
     *
     * @param authorizations
     *            list of authorizations as AuthorityDTO
     */
    public void setAuthorizations(List<AuthorityDTO> authorizations) {
        this.authorizations = authorizations;
    }

    /**
     * Get size of authorizations.
     *
     * @return size of authorizations as Integer
     */
    public Integer getAuthorizationsSize() {
        return authorizationsSize;
    }

    /**
     * Set size of authorizations.
     *
     * @param authorizationsSize
     *            as Integer
     */
    public void setAuthorizationsSize(Integer authorizationsSize) {
        this.authorizationsSize = authorizationsSize;
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
