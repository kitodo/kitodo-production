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

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.kitodo.data.database.persistence.TemplateDAO;
import org.kitodo.data.interfaces.DocketInterface;
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.data.interfaces.RulesetInterface;
import org.kitodo.data.interfaces.TaskInterface;
import org.kitodo.data.interfaces.TemplateInterface;
import org.kitodo.data.interfaces.WorkflowInterface;

@Entity
@Table(name = "template")
public class Template extends BaseTemplateBean implements TemplateInterface {

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

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "project_x_template", joinColumns = {
        @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "FK_project_x_template_template_id")) },
            inverseJoinColumns = {
                @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_project_x_template_project_id")) })
    private List<Project> projects;

    @Column(name = "ocrd_workflow_id")
    private String ocrdWorkflowId;

    /**
     * Constructor.
     */
    public Template() {
        this.title = "";
        this.tasks = new ArrayList<>();
        this.creationDate = new Date();
    }

    @Override
    public boolean isActive() {
        if (Objects.isNull(this.active)) {
            this.active = true;
        }
        return this.active;
    }

    @Override
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

    @Override
    public Docket getDocket() {
        return docket;
    }

    @Override
    public void setDocket(DocketInterface docket) {
        this.docket = (Docket) docket;
    }

    /**
     * Sets the docket generation statement to use when creating dockets for
     * processes derived from this process template.
     *
     * <p>
     * <b>API Note:</b><br>
     * This function exists because Faces does not recognize the more generic
     * function {@link #setDocket(DocketInterface)} as a setter for the property
     * {@code docket} and otherwise throws a
     * {@code PropertyNotWritableException}.
     *
     * @param docket
     *            the docket generation statement
     */
    public void setDocket(Docket docket) {
        this.docket = docket;
    }

    @Override
    public Ruleset getRuleset() {
        return this.ruleset;
    }

    @Override
    public void setRuleset(RulesetInterface ruleset) {
        this.ruleset = (Ruleset) ruleset;
    }

    /**
     * Sets the business domain specification derived from this process template
     * template shall be using.
     *
     * <p>
     * <b>API Note:</b><br>
     * This function exists because Faces does not recognize the more generic
     * function {@link #setRuleset(RulesetInterface)} as a setter for the
     * property {@code ruleset} and otherwise throws a
     * {@code PropertyNotWritableException}.
     *
     * @param ruleset
     *            the business domain specification
     */
    public void setRuleset(Ruleset ruleset) {
        this.ruleset = ruleset;
    }
    
    @Override
    public Workflow getWorkflow() {
        return workflow;
    }

    @Override
    public void setWorkflow(WorkflowInterface workflow) {
        this.workflow = (Workflow) workflow;
    }

    /**
     * Sets the workflow from which the production template was created.
     *
     * <p>
     * <b>API Note:</b><br>
     * This function exists because Faces does not recognize the more generic
     * function {@link #setWorkflow(WorkflowInterface)} as a setter for the
     * property {@code workflow} and otherwise throws a
     * {@code PropertyNotWritableException}.
     *
     * @param workflow
     *            workflow to set
     */
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    /**
     * Get OCR-D workflow identifier.
     *
     * @return value of OCR-D workflow identifier
     */
    public String getOcrdWorkflowId() {
        return ocrdWorkflowId;
    }

    /**
     * Set the OCR-D workflow identifier.
     *
     * @param ocrdWorkflowId
     *         The identifier of the OCR-D workflow
     */
    public void setOcrdWorkflowId(String ocrdWorkflowId) {
        this.ocrdWorkflowId = ocrdWorkflowId;
    }

    @Override
    public List<Project> getProjects() {
        initialize(new TemplateDAO(), this.projects);
        if (Objects.isNull(this.projects)) {
            this.projects = new ArrayList<>();
        }
        return this.projects;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProjects(List<? extends ProjectInterface> projects) {
        this.projects = (List<Project>) projects;
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

    @Override
    public List<Task> getTasks() {
        initialize(new TemplateDAO(), this.tasks);
        if (Objects.isNull(this.tasks)) {
            this.tasks = new ArrayList<>();
        }
        return this.tasks;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setTasks(List<? extends TaskInterface> tasks) {
        this.tasks = (List<Task>) tasks;
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

    @Override
    public boolean isCanBeUsedForProcess() {
        if (Objects.isNull(tasks)) {
            return false;
        }
        return tasks.stream().allMatch(task -> CollectionUtils.isNotEmpty(task.getRoles()));
    }
}
