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
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "template")
public class Template extends BaseTemplateBean {

    private static final long serialVersionUID = -6503346767655786275L;

    @ManyToOne
    @JoinColumn(name = "docket_id", foreignKey = @ForeignKey(name = "FK_template_docket_id"))
    private Docket docket;

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_template_project_id"))
    private Project project;

    @ManyToOne
    @JoinColumn(name = "ruleset_id", foreignKey = @ForeignKey(name = "FK_template_ruleset_id"))
    private Ruleset ruleset;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    /**
     * Constructor.
     */
    public Template() {
        this.title = "";
        this.inChoiceListShown = true;
        this.tasks = new ArrayList<>();
        this.creationDate = new Date();
    }

    /**
     * Get project.
     *
     * @return value of project
     */
    public Project getProject() {
        return this.project;
    }

    /**
     * Set project.
     *
     * @param project as Project
     */
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * Get ruleset.
     *
     * @return value of ruleset
     */
    public Ruleset getRuleset() {
        return this.ruleset;
    }

    /**
     * Set ruleset.
     *
     * @param ruleset as Ruleset object
     */
    public void setRuleset(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    /**
     * Get docket.
     *
     * @return value of docket
     */
    public Docket getDocket() {
        return docket;
    }

    /**
     * Set docket.
     *
     * @param docket as Docket object
     */
    public void setDocket(Docket docket) {
        this.docket = docket;
    }

    /**
     * Get list of task.
     *
     * @return list of Task objects or empty list
     */
    public List<Task> getTasks() {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        return this.tasks;
    }

    /**
     * Set tasks.
     *
     * @param tasks as list of task
     */
    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    /**
     * Determines whether or not two processes are equal. Two instances of
     * {@code Process} are equal if the values of their {@code Id}, {@code Title},
     * {@code OutputName} and {@code CreationDate} member fields are the same.
     *
     * @param o
     *            An object to be compared with this {@code Process}.
     * @return {@code true} if the object to be compared is an instance of
     *         {@code Process} and has the same values; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Process process = (Process) o;
        return Objects.equals(getTitle(), process.getTitle()) && Objects.equals(getId(), process.getId())
                && Objects.equals(getOutputName(), process.getOutputName())
                && Objects.equals(getCreationDate(), process.getCreationDate());
    }
}
