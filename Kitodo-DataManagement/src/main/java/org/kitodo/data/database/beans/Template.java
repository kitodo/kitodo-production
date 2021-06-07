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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.kitodo.data.database.persistence.TemplateDAO;

@Entity
@Table(name = "template")
public class Template extends BaseTemplateBean {

    @Column(name = "active")
    private Boolean active = true;

    @ManyToOne
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "FK_template_client_id"))
    private Client client;

    @ManyToOne
    @JoinColumn(name = "docket_id", foreignKey = @ForeignKey(name = "FK_template_docket_id"))
    private Docket docket;

    @ManyToOne
    @JoinColumn(name = "ruleset_id", foreignKey = @ForeignKey(name = "FK_template_ruleset_id"))
    private Ruleset ruleset;

    @ManyToOne
    @JoinColumn(name = "workflow_id", foreignKey = @ForeignKey(name = "FK_template_workflow_id"))
    private Workflow workflow;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Process> processes;

    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "project_x_template", joinColumns = {
            @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "FK_project_x_template_template_id")) },
            inverseJoinColumns = {
                    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_project_x_template_project_id")) })
    private List<Project> projects;

    /**
     * Constructor.
     */
    public Template() {
        this.title = "";
        this.tasks = new ArrayList<>();
        this.creationDate = new Date();
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
    public void setActive(boolean active) {
        this.active = active;
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
     * Get workflow.
     *
     * @return value of workflow
     */
    public Workflow getWorkflow() {
        return workflow;
    }

    /**
     * Set workflow.
     *
     * @param workflow as Workflow object
     */
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     * Get projects list.
     *
     * @return list of projects
     */
    public List<Project> getProjects() {
        initialize(new TemplateDAO(), this.projects);
        if (Objects.isNull(this.projects)) {
            this.projects = new ArrayList<>();
        }
        return this.projects;
    }

    /**
     * Set list of projects.
     *
     * @param projects as Project list
     */
    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    /**
     * Get processes.
     *
     * @return value of processes
     */
    public List<Process> getProcesses() {
        initialize(new TemplateDAO(), this.processes);
        if (Objects.isNull(this.processes)) {
            this.processes = new ArrayList<>();
        }
        return this.processes;
    }

    /**
     * Set processes.
     *
     * @param processes as Lis of Process
     */
    public void setProcesses(List<Process> processes) {
        this.processes = processes;
    }

    /**
     * Get list of task.
     *
     * @return list of Task objects or empty list
     */
    public List<Task> getTasks() {
        initialize(new TemplateDAO(), this.tasks);
        if (Objects.isNull(this.tasks)) {
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
     * {@code Template} are equal if the values of their {@code Id} member fields are the same.
     *
     * @param object
     *            An object to be compared with this {@code Template}.
     * @return {@code true} if the object to be compared is an instance of
     *         {@code Template} and has the same values; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof Template) {
            Template template = (Template) object;
            return Objects.equals(this.getId(), template.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(client, docket, ruleset, workflow);
    }
}
