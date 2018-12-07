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
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "role")
public class Role extends BaseIndexedBean implements Comparable<Role> {
    private static final long serialVersionUID = -5924845694417474352L;

    @Column(name = "title", nullable = false, unique = true)
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

    /**
     * The Constructor.
     */
    public Role() {
        this.tasks = new ArrayList<>();
        this.users = new ArrayList<>();
        this.authorities = new ArrayList<>();
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
     * Gets authorities.
     *
     * @return The authorities.
     */
    public List<Authority> getAuthorities() {
        if (this.authorities == null) {
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

    /**
     * Get client.
     *
     * @return the client bean
     */
    public Client getClient() {
        return client;
    }

    /**
     * Set client.
     *
     * @param client
     *            bean
     */
    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public int compareTo(Role o) {
        return this.getTitle().compareTo(o.getTitle());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Role)) {
            return false;
        }
        Role other = (Role) obj;
        return this.getTitle().equals(other.getTitle());
    }

    @Override
    public int hashCode() {
        return this.getTitle().hashCode();
    }

}
