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

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "userGroup_x_project_x_authority")
public class UserGroupProjectAuthorityRelation extends BaseBean {

    @ManyToOne
    @JoinColumn(name = "userGroup_id", foreignKey = @ForeignKey(name = "FK_userGroup_x_project_x_authority_userGroup_id"))
    private UserGroup userGroup;

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_userGroup_x_project_x_authority_project_id"))
    private Project project;

    @ManyToOne
    @JoinColumn(name = "authority_id", foreignKey = @ForeignKey(name = "FK_userGroup_x_project_x_authority_authority_id"))
    private Authority authority;

    public UserGroupProjectAuthorityRelation(UserGroup userGroup, Project project, Authority authority) {
        this.userGroup = userGroup;
        this.project = project;
        this.authority = authority;
    }

    public UserGroupProjectAuthorityRelation() {
    }

    /**
     * Gets userGroup.
     *
     * @return The userGroup.
     */
    public UserGroup getUserGroup() {
        return userGroup;
    }

    /**
     * Sets userGroup.
     *
     * @param userGroup
     *            The userGroup.
     */
    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    /**
     * Gets client.
     *
     * @return The client.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Sets client.
     *
     * @param project
     *            The client.
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Gets authority.
     *
     * @return The authority.
     */
    public Authority getAuthority() {
        return authority;
    }

    /**
     * Sets authority.
     *
     * @param authority
     *            The authority.
     */
    public void setAuthority(Authority authority) {
        this.authority = authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserGroupProjectAuthorityRelation that = (UserGroupProjectAuthorityRelation) o;

        if (userGroup != null ? !userGroup.equals(that.userGroup) : that.userGroup != null) {
            return false;
        }
        if (project != null ? !project.equals(that.project) : that.project != null) {
            return false;
        }
        return authority != null ? authority.equals(that.authority) : that.authority == null;
    }

    @Override
    public int hashCode() {
        int result = userGroup != null ? userGroup.hashCode() : 0;
        result = 31 * result + (project != null ? project.hashCode() : 0);
        result = 31 * result + (authority != null ? authority.hashCode() : 0);
        return result;
    }
}
