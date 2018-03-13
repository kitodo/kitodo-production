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

package org.kitodo.data.database.beans;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "authority")
public class Authority extends BaseIndexedBean {

    private static final long serialVersionUID = -5187947220333987498L;

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "globalAssignable")
    private boolean globalAssignable = true;

    @Column(name = "clientAssignable")
    private boolean clientAssignable = true;

    @Column(name = "projectAssignable")
    private boolean projectAssignable = true;

    @ManyToMany(mappedBy = "authorities", cascade = CascadeType.PERSIST)
    private List<UserGroup> userGroups;

    @OneToMany(mappedBy = "authority", cascade = CascadeType.ALL)
    private List<UserGroupClientAuthorityRelation> userGroupClientAuthorityRelations;

    @OneToMany(mappedBy = "authority", cascade = CascadeType.ALL)
    private List<UserGroupProjectAuthorityRelation> userGroupProjectAuthorityRelations;

    /**
     * The constructor for setting title and assignables.
     * 
     * @param title
     *            The title.
     * @param globalAssignable
     *            True if it should be assignable global.
     * @param clientAssignable
     *            True if it should be assignable to clients.
     * @param projectAssignable
     *            True if it should be assignable to projects.
     */
    public Authority(String title, boolean globalAssignable, boolean clientAssignable, boolean projectAssignable) {
        this.title = title;
        this.globalAssignable = globalAssignable;
        this.clientAssignable = clientAssignable;
        this.projectAssignable = projectAssignable;
    }

    /**
     * The normal constructor.
     */
    public Authority() {
    }

    /**
     * Gets the title.
     *
     * @return The title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title.
     *
     * @param title
     *            The title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets globalAssignable.
     *
     * @return True if the authority is global assignable.
     */
    public boolean isGlobalAssignable() {
        return globalAssignable;
    }

    /**
     * Sets globalAssignable.
     *
     * @param globalAssignable
     *            The globalAssignable.
     */
    public void setGlobalAssignable(boolean globalAssignable) {
        this.globalAssignable = globalAssignable;
    }

    /**
     * Gets clientAssignable.
     *
     * @return True if the authority is assignable to any client.
     */
    public boolean isClientAssignable() {
        return clientAssignable;
    }

    /**
     * Sets clientAssignable.
     *
     * @param clientAssignable
     *            The clientAssignable.
     */
    public void setClientAssignable(boolean clientAssignable) {
        this.clientAssignable = clientAssignable;
    }

    /**
     * Gets projectAssignable.
     *
     * @return True if the authority is assignable to any project.
     */
    public boolean isProjectAssignable() {
        return projectAssignable;
    }

    /**
     * Sets projectAssignable.
     *
     * @param projectAssignable
     *            The projectAssignable.
     */
    public void setProjectAssignable(boolean projectAssignable) {
        this.projectAssignable = projectAssignable;
    }

    /**
     * Gets all user groups in which this authority is used.
     *
     * @return The user groups.
     */
    public List<UserGroup> getUserGroups() {
        if (this.userGroups == null) {
            this.userGroups = new ArrayList<>();
        }
        return userGroups;
    }

    /**
     * Sets the user groups.
     *
     * @param userGroups
     *            The user groups.
     */
    public void setUserGroups(List<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    /**
     * Gets userGroupClientAuthorityRelations.
     *
     * @return The userGroupClientAuthorityRelations.
     */
    public List<UserGroupClientAuthorityRelation> getUserGroupClientAuthorityRelations() {
        if (this.userGroupClientAuthorityRelations == null) {
            this.userGroupClientAuthorityRelations = new ArrayList<>();
        }
        return userGroupClientAuthorityRelations;
    }

    /**
     * Sets userGroupClientAuthorityRelations.
     *
     * @param userGroupClientAuthorityRelations
     *            The userGroupClientAuthorityRelations.
     */
    public void setUserGroupClientAuthorityRelations(
            List<UserGroupClientAuthorityRelation> userGroupClientAuthorityRelations) {
        this.userGroupClientAuthorityRelations = userGroupClientAuthorityRelations;
    }

    /**
     * Gets userGroupProjectAuthorityRelations.
     *
     * @return The userGroupProjectAuthorityRelations.
     */
    public List<UserGroupProjectAuthorityRelation> getUserGroupProjectAuthorityRelations() {
        if (this.userGroupProjectAuthorityRelations == null) {
            this.userGroupProjectAuthorityRelations = new ArrayList<>();
        }
        return userGroupProjectAuthorityRelations;
    }

    /**
     * Sets userGroupProjectAuthorityRelations.
     *
     * @param userGroupProjectAuthorityRelations
     *            The userGroupProjectAuthorityRelations.
     */
    public void setUserGroupProjectAuthorityRelations(
            List<UserGroupProjectAuthorityRelation> userGroupProjectAuthorityRelations) {
        this.userGroupProjectAuthorityRelations = userGroupProjectAuthorityRelations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Authority authority = (Authority) o;
        return title != null ? title.equals(authority.title) : authority.title == null;
    }

    @Override
    public int hashCode() {
        return title != null ? title.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Authority{" + "title='" + title + "\'" + '}' + "@" + Integer.toHexString(hashCode());
    }
}
