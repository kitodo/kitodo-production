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
@Table(name = "listColumn")
public class ListColumn extends BaseBean {

    @Column(name = "title")
    private String title;

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
        return title != null ? title.hashCode() : 0;
    }

}
