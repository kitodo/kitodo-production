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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "workflow")
public class Workflow extends BaseBean {
    private static final long serialVersionUID = 6831844584235763486L;

    @Column(name = "title")
    private String title;

    @Column(name = "file")
    private String file;

    /**
     * Public constructor.
     * 
     * @param title
     *            of workflow
     * @param file
     *            in which diagram is stored
     */
    public Workflow(String title, String file) {
        this.title = title;
        this.file = file;
    }

    /**
     * Get title.
     *
     * @return value of title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Set title.
     *
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Get file.
     *
     * @return value of file
     */
    public String getFile() {
        return file;
    }

    /**
     * Set file.
     *
     * @param file
     *            as String
     */
    public void setFile(String file) {
        this.file = file;
    }
}
