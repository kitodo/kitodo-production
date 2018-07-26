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
import javax.persistence.OneToMany;
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

    @OneToMany(mappedBy = "workflow", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Template> templates;

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
    public boolean isActive() {
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
    public boolean isReady() {
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

    /**
     * Get list of template assigned to this workflow.
     *
     * @return list of template assigned to this workflow
     */
    public List<Template> getTemplates() {
        if (Objects.isNull(this.templates)) {
            this.templates = new ArrayList<>();
        }
        return this.templates;
    }

    /**
     * Set list of template assigned to this workflow.
     *
     * @param templates
     *            list of template assigned to this workflow
     */
    public void setTemplates(List<Template> templates) {
        this.templates = templates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Workflow workflow = (Workflow) o;
        return Objects.equals(title, workflow.title)
            && Objects.equals(fileName, workflow.fileName)
            && Objects.equals(active, workflow.active)
            && Objects.equals(ready, workflow.ready);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, fileName, active, ready);
    }
}
