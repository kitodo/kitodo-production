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
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kitodo.data.database.persistence.RoleDAO;
import org.kitodo.utils.Stopwatch;

@Entity
@Table(name = "role")
public class Role extends BaseBean implements Comparable<Role> {

    @Column(name = "title", nullable = false)
    private String title;

    @ManyToMany(mappedBy = "roles", cascade = CascadeType.PERSIST)
    private List<User> users;

    @ManyToMany(mappedBy = "roles", cascade = CascadeType.PERSIST)
    private List<Task> tasks;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "role_x_authority", joinColumns = {@JoinColumn(name = "role_id",
            foreignKey = @ForeignKey(name = "FK_role_x_authority_role_id")) },
            inverseJoinColumns = {@JoinColumn(name = "authority_id",
                    foreignKey = @ForeignKey(name = "FK_role_x_authority_authority_id")) })
    private List<Authority> authorities;

    @ManyToOne
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "FK_role_client_id"))
    private Client client;

    @Transient
    private Boolean usedInWorkflow = null;

    /**
     * The Constructor.
     */
    public Role() {
        this.tasks = new ArrayList<>();
        this.users = new ArrayList<>();
        this.authorities = new ArrayList<>();
    }

    /**
     * Returns the name of the role.
     *
     * @return the name of the role
     */
    public String getTitle() {
        if (this.title == null) {
            return "";
        } else {
            return this.title;
        }
    }

    /**
     * Sets the name of the role.
     *
     * @param title
     *            name of the role to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets authorities.
     *
     * @return The authorities.
     */
    public List<Authority> getAuthorities() {
        initialize(new RoleDAO(), this.authorities);
        if (Objects.isNull(this.authorities)) {
            this.authorities = new ArrayList<>();
        }
        return this.authorities;
    }

    /**
     * Sets authorities.
     *
     * @param authorities
     *            The authorities.
     */
    public void setAuthorities(List<Authority> authorities) {
        this.authorities = authorities;
    }

    /**
     * Specifies the users who hold this role. This list is not guaranteed to be
     * in reliable order.
     *
     * @return list of users who hold this role
     */
    public List<User> getUsers() {
        initialize(new RoleDAO(), this.users);
        if (Objects.isNull(this.users)) {
            this.users = new ArrayList<>();
        }
        return this.users;
    }

    /**
     * Sets the list of users who hold this role.
     *
     * @param users
     *            list of users who hold this role to set
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
        initialize(new RoleDAO(), this.tasks);
        if (Objects.isNull(this.tasks)) {
            this.tasks = new ArrayList<>();
        }
        return this.tasks;
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

    /**
     * Returns the client in whose realm this role grants permissions.
     *
     * @return the client in whose realm this role grants permissions
     */
    public Client getClient() {
        return client;
    }

    /**
     * Sets the client in whose realm this role grants permissions.
     *
     * @param client
     *            client in whose realm this role grants permissions to set.
     */
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public int compareTo(Role o) {
        return this.getTitle().compareTo(o.getTitle());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof Role) {
            Role role = (Role) object;
            return Objects.equals(this.getId(), role.getId()) && Objects.equals(this.getTitle() , role.getTitle());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.getTitle().hashCode();
    }

    @Override
    public String toString() {
        return title + '[' + client.getName() + ']';
    }

    /**
     * Returns whether the role is used in any task of the workflow engine.
     * There are roles that are assigned to tasks and roles that are only used
     * to grant permissions. This can save us from searching for tasks with
     * roles that would never find anything anyway.
     * 
     * @return whether the role is used in any task of the workflow engine
     */
    public boolean isUsedInWorkflow() {
        Stopwatch stopwatch = new Stopwatch(this, "isUsedInWorkflow");
        if (Objects.isNull(this.usedInWorkflow)) {
            this.usedInWorkflow = has(new RoleDAO(), "FROM Task AS task WHERE :role IN elements(task.roles)",
                Collections.singletonMap("role", this));
        }
        return stopwatch.stop(usedInWorkflow);
    }

    /**
     * Sets whether the role is used in a task of the workflow engine. This is
     * not a property of the role; it is determined dynamically in the database
     * and then stored transiently. This setter is there to change the cached
     * value when a role is assigned to a task for the first time, so that the
     * newly created tasks do not first go invisible.
     * 
     * @param usedInWorkflow
     *            whether the role is used in a task of the workflow engine
     */
    public void setUsedInWorkflow(boolean usedInWorkflow) {
        Stopwatch stopwatch = new Stopwatch(this, "setUsedInWorkflow", "usedInWorkflow", Boolean.toString(
            usedInWorkflow));
        this.usedInWorkflow = usedInWorkflow;
        stopwatch.stop();
    }
}
