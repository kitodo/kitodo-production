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

    @Column(name = "title")
    private String title;

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

    public String getTitle() {
        if (this.title == null) {
            return "";
        } else {
            return this.title;
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Authorization> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(List<Authorization> authorizations) {
        this.authorizations = authorizations;
    }

    public List<User> getUsers() {
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<Task> getTasks() {
        return this.tasks;
    }

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
