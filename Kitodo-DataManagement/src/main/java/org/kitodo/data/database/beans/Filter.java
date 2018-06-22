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

import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Filter bean.
 */
@Entity
@Table(name = "filter")
public class Filter extends BaseIndexedBean {

    private static final long serialVersionUID = -5187937660333984868L;

    @Column(name = "value", columnDefinition = "longtext")
    private String value;

    @Column(name = "creationDate")
    private Date creationDate;

    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_filter_user_id"))
    private User user;

    /**
     * Get filter value.
     *
     * @return filter value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set filter value.
     *
     * @param value
     *            filter
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get creation date.
     *
     * @return creation date as Date
     */
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * Set creation date.
     *
     * @param creationDate
     *            as Date
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Get user.
     *
     * @return user
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Set user.
     *
     * @param user
     *            object
     */
    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Filter filter = (Filter) o;
        return Objects.equals(value, filter.value)
            && Objects.equals(creationDate, filter.creationDate)
            && Objects.equals(user, filter.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, creationDate, user);
    }
}
