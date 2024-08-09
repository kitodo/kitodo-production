/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.data.database.beans;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * This bean contains properties common for Template and Process.
 */
@MappedSuperclass
public abstract class BaseTemplateBean extends BaseIndexedBean {

    @Column(name = "title")
    protected String title;

    @Column(name = "creationDate")
    protected Date creationDate;

    @Column(name = "sortHelperStatus")
    private String sortHelperStatus;

    /**
     * Get title.
     *
     * @return value of title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set title.
     *
     * @param title as String
     */
    public void setTitle(String title) {
        this.title = title.trim();
    }

    /**
     * Get sortHelperStatus.
     *
     * @return value of sortHelperStatus
     */
    public String getSortHelperStatus() {
        return this.sortHelperStatus;
    }

    /**
     * Set sortHelperStatus.
     *
     * @param sortHelperStatus as java.lang.String
     */
    public void setSortHelperStatus(String sortHelperStatus) {
        this.sortHelperStatus = sortHelperStatus;
    }

    /**
     * Get creationDate.
     *
     * @return value of creationDate
     */
    public Date getCreationDate() {
        return this.creationDate;
    }

    /**
     * Set creationDate.
     *
     * @param creationDate as java.util.Date
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }


    /**
     * Returns a string that textually represents this object.
     */
    @Override
    public String toString() {
        return title + " [" + id + ']';
    }
}
