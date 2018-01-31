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
}
