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
@Table(name = "workflow")
public class Workflow extends BaseIndexedBean {
    private static final long serialVersionUID = 6831844584235763486L;

    @Column(name = "title")
    private String title;

    @Column(name = "fileName")
    private String fileName;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "ready")
    private Boolean ready = false;

    /**
     * Empty constructor.
     */
    public Workflow() {
    }

    /**
     * Public constructor.
     * 
     * @param title
     *            of workflow
     * @param fileName
     *            in which diagram is stored
     */
    public Workflow(String title, String fileName) {
        this.title = title;
        this.fileName = fileName;
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
     * Get file name of file in which diagram workflow is stored.
     *
     * @return value of file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set file name of file in which diagram workflow is stored.
     *
     * @param fileName
     *            as String
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Check if workflow is active.
     *
     * @return true or false
     */
    public Boolean isActive() {
        if (Objects.isNull(this.active)) {
            this.active = true;
        }
        return this.active;
    }

    /**
     * Set workflow as active.
     *
     * @param active as Boolean
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Check if workflow is ready to use.
     *
     * @return true or false
     */
    public Boolean isReady() {
        if (Objects.isNull(this.ready)) {
            this.ready = false;
        }
        return this.ready;
    }

    /**
     * Set workflow as ready to use.
     *
     * @param ready as Boolean
     */
    public void setReady(Boolean ready) {
        this.ready = ready;
    }
}
