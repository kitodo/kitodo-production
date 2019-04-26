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
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.kitodo.data.database.persistence.AuthorityDAO;

@Entity
@Table(name = "authority")
public class Authority extends BaseBean {

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @ManyToMany(mappedBy = "authorities", cascade = CascadeType.PERSIST)
    private List<Role> roles;

    /**
     * The constructor for setting title and assignables.
     *
     * @param title
     *            The title.
     */
    public Authority(String title) {
        this.title = title;
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
     * Get all roles in which this authority is used.
     *
     * @return the roles
     */
    public List<Role> getRoles() {
        initialize(new AuthorityDAO(), this.roles);
        if (Objects.isNull(this.roles)) {
            this.roles = new ArrayList<>();
        }
        return roles;
    }

    /**
     * Set the roles.
     *
     * @param roles
     *            the roles
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    /**
     * Get the title without suffixes separated by "_" e.g.
     * "admin_globallyAssignable" will become "admin".
     *
     * @return the title without suffix
     */
    public String getTitleWithoutSuffix() {
        return this.title.split("_")[0];
    }

    /**
     * Get the title without suffixes separated by "_" e.g.
     * "admin_globallyAssignable" will become "globallyAssignable".
     *
     * @return the type without suffix
     */
    public String getType() {
        return this.title.split("_")[1];
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof Authority) {
            Authority authority = (Authority) object;
            return Objects.equals(this.getId(), authority.getId());
        }

        return false;
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
