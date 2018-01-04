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
import javax.persistence.Table;

/**
 * User groups owning different access rights, represented by integer values.
 *
 * <p>
 * 1: Administration - can do anything 2: Projectmanagement - may do a lot (but
 * not user management, no user switch, no administrative form) 3: User and
 * process (basically like 4 but can be used for setting additional boundaries
 * later, if so desired) 4: User only: can see current steps
 * </p>
 */
@Entity
@Table(name = "userGroup")
public class UserGroup extends BaseIndexedBean implements Comparable<UserGroup> {
    private static final long serialVersionUID = -5924845694417474352L;

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "permission")
    private Integer permission;

    @ManyToMany(mappedBy = "userGroups", cascade = CascadeType.PERSIST)
    private List<User> users;

    @ManyToMany(mappedBy = "userGroups", cascade = CascadeType.PERSIST)
    private List<Task> tasks;

    @ManyToMany(mappedBy = "userGroups", cascade = CascadeType.PERSIST)
    private List<Authorization> authorizations;

    /**
     * The Constructor.
     */
    public UserGroup() {
        this.tasks = new ArrayList<>();
        this.users = new ArrayList<>();
        this.authorizations = new ArrayList<>();
    }

    /**
     * Gets title.
     *
     * @return The title.
     */
    public String getTitle() {
        if (this.title == null) {
            return "";
        } else {
            return this.title;
        }
    }

    /**
     * Sets title.
     *
     * @param title
     *            The title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets authorizations.
     *
     * @return The authorizations.
     */
    public List<Authorization> getAuthorizations() {
        if (this.authorizations == null) {
            this.authorizations = new ArrayList<>();
        }
        return this.authorizations;
    }

    /**
     * Sets authorizations.
     *
     * @param authorizations
     *            The authorizations.
     */
    public void setAuthorizations(List<Authorization> authorizations) {
        this.authorizations = authorizations;
    }

    /**
     * Gets users.
     *
     * @return The users.
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Sets users.
     *
     * @param users
     *            The users.
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
     * Gets tasks.
     *
     * @return The tasks.
     */
    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * Sets tasks.
     *
     * @param tasks
     *            The tasks.
     */
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public int compareTo(UserGroup o) {
        return this.getTitle().compareTo(o.getTitle());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserGroup)) {
            return false;
        }
        UserGroup other = (UserGroup) obj;
        return this.getTitle().equals(other.getTitle());
    }

    @Override
    public int hashCode() {
        return this.getTitle().hashCode();
    }

}
