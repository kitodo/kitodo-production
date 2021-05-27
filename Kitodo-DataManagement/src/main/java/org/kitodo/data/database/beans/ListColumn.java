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

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "listcolumn")
public class ListColumn extends BaseBean {

    @Column(name = "title")
    private String title;

    @Column(name = "custom")
    private Boolean custom;

    /**
     * Empty standard constructor.
     */
    public ListColumn() {
    }

    /**
     * Constructor.
     *
     * @param title listColumn title
     */
    public ListColumn(String title) {
        this.title = title;
        this.custom = false;
    }

    /**
     * Constructor.
     *
     * @param title listColumn title
     * @param custom listColumn custom status
     */
    public ListColumn(String title, boolean custom) {
        this.title = title;
        this.custom = custom;
    }

    /**
     * Get title.
     *
     * @return ListColumn title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title
     *          ListColumn title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Check whether list column is a custom list column.
     * @return true if list column is a custom column, else false.
     */
    public boolean isCustom() {
        if (Objects.isNull(this.custom)) {
            this.custom = false;
        }
        return this.custom;
    }

    /**
     * Set whether list column is a custom list column.
     *
     * @param custom whether list column is a custom list colimn
     */
    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof ListColumn) {
            ListColumn column = (ListColumn) object;
            return Objects.equals(this.getId(), column.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.isNull(title) ? 0 : title.hashCode();
    }

}
