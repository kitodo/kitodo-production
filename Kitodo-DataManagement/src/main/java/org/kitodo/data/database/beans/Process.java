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

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.persistence.HibernateUtil;
import org.kitodo.data.database.persistence.ProcessDAO;
import org.kitodo.data.elasticsearch.index.converter.ProcessConverter;
import org.kitodo.data.interfaces.BatchInterface;
import org.kitodo.data.interfaces.DocketInterface;
import org.kitodo.data.interfaces.ProcessInterface;
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.data.interfaces.PropertyInterface;
import org.kitodo.data.interfaces.RulesetInterface;
import org.kitodo.data.interfaces.TaskInterface;
import org.kitodo.data.interfaces.UserInterface;

@Entity
@Table(name = "process")
public class Process extends BaseTemplateBean implements ProcessInterface {

    @Column(name = "sortHelperImages")
    private Integer sortHelperImages;

    @Column(name = "sortHelperArticles")
    private Integer sortHelperArticles;

    @Column(name = "sortHelperMetadata")
    private Integer sortHelperMetadata;

    @Column(name = "sortHelperDocstructs")
    private Integer sortHelperDocstructs;

    @Column(name = "wikiField", columnDefinition = "longtext")
    private String wikiField = "";

    @Column(name = "processBaseUri")
    private String processBaseUri;

    @Column(name = "ordering")
    private Integer ordering;

    @ManyToOne
    @JoinColumn(name = "docket_id", foreignKey = @ForeignKey(name = "FK_process_docket_id"))
    private Docket docket;

    @ManyToOne
    @JoinColumn(name = "project_id", foreignKey = @ForeignKey(name = "FK_process_project_id"))
    private Project project;

    @ManyToOne
    @JoinColumn(name = "ruleset_id", foreignKey = @ForeignKey(name = "FK_process_ruleset_id"))
    private Ruleset ruleset;

    @ManyToOne
    @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "FK_process_template_id"))
    private Template template;

    @ManyToOne
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "FK_process_parent_id"))
    private Process parent;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
    private List<Process> children;

    @OneToMany(mappedBy = "process", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordering")
    private List<Task> tasks;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "process", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<Comment> comments;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "process_x_property", joinColumns = {
        @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_process_x_property_process_id")) }, inverseJoinColumns = {
            @JoinColumn(name = "property_id", foreignKey = @ForeignKey(name = "FK_process_x_property_property_id")) })
    private List<Property> properties;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "template_x_property", joinColumns = {
        @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_template_x_property_process_id")) }, inverseJoinColumns = {
            @JoinColumn(name = "property_id", foreignKey = @ForeignKey(name = "FK_template_x_property_property_id")) })
    private List<Property> templates;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "workpiece_x_property", joinColumns = {
        @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_workpiece_x_property_process_id")) },
            inverseJoinColumns = {
                @JoinColumn(name = "property_id", foreignKey = @ForeignKey(name = "FK_workpiece_x_property_property_id")) })
    private List<Property> workpieces;

    @ManyToMany(mappedBy = "processes")
    private List<Batch> batches = new ArrayList<>();

    @Column(name = "exported")
    private boolean exported;

    @Column(name = "inChoiceListShown")
    Boolean inChoiceListShown;

    @Column(name = "ocrd_workflow_id")
    private String ocrdWorkflowId;

    @Transient
    private UserInterface blockedUser;

    @Transient
    private List<Map<String, Object>> metadata;

    @Transient
    private int numberOfMetadata;

    @Transient
    private int numberOfImages;

    @Transient
    private int numberOfStructures;

    @Transient
    private String baseType;

    /**
     * Constructor.
     */
    public Process() {
        this.title = "";
        this.properties = new ArrayList<>();
        this.workpieces = new ArrayList<>();
        this.templates = new ArrayList<>();
        this.children = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.inChoiceListShown = false;
        this.creationDate = new Date();
    }

    /**
     * {@inheritDoc} In case of {@code null}, it returns 0.
     */
    @Override
    public Integer getSortHelperImages() {
        if (this.sortHelperImages == null) {
            this.sortHelperImages = 0;
        }
        return this.sortHelperImages;
    }

    @Override
    public void setSortHelperImages(Integer sortHelperImages) {
        this.sortHelperImages = sortHelperImages;
    }

    /**
     * {@inheritDoc} In case of {@code null}, it returns 0.
     */
    @Override
    public Integer getSortHelperArticles() {
        if (this.sortHelperArticles == null) {
            this.sortHelperArticles = 0;
        }
        return this.sortHelperArticles;
    }

    @Override
    public void setSortHelperArticles(Integer sortHelperArticles) {
        this.sortHelperArticles = sortHelperArticles;
    }

    /**
     * {@inheritDoc} In case of {@code null}, it returns 0.
     */
    @Override
    public Integer getSortHelperDocstructs() {
        if (this.sortHelperDocstructs == null) {
            this.sortHelperDocstructs = 0;
        }
        return this.sortHelperDocstructs;
    }

    @Override
    public void setSortHelperDocstructs(Integer sortHelperDocstructs) {
        this.sortHelperDocstructs = sortHelperDocstructs;
    }

    /**
     * {@inheritDoc} In case of {@code null}, it returns 0.
     */
    @Override
    public Integer getSortHelperMetadata() {
        if (this.sortHelperMetadata == null) {
            this.sortHelperMetadata = 0;
        }
        return this.sortHelperMetadata;
    }

    @Override
    public void setSortHelperMetadata(Integer sortHelperMetadata) {
        this.sortHelperMetadata = sortHelperMetadata;
    }

    @Override
    public String getWikiField() {
        return this.wikiField;
    }

    @Override
    public void setWikiField(String wikiField) {
        this.wikiField = wikiField;
    }

    @Override
    public URI getProcessBaseUri() {
        return Objects.isNull(processBaseUri) ? null : URI.create(processBaseUri);
    }

    @Override
    public void setProcessBaseUri(URI processBaseUri) {
        this.processBaseUri = Objects.isNull(processBaseUri) ? null : processBaseUri.toString();
    }

    /**
     * Get ordering.
     *
     * @return value of ordering
     */
    public Integer getOrdering() {
        return ordering;
    }

    /**
     * Set ordering.
     *
     * @param ordering
     *            as java.lang.Integer
     */
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    @Override
    public Project getProject() {
        return this.project;
    }

    @Override
    public void setProject(ProjectInterface project) {
        this.project = (Project) project;
    }

    /**
     * Specifies the project to which the process belongs.
     * 
     * <p>
     * <b>API Note:</b><br>
     * This function exists because Faces does not recognize the more generic
     * function {@link #setProject(ProjectInterface)} as a setter for the
     * property {@code project} and otherwise throws a
     * {@code PropertyNotWritableException}.
     *
     * @param project
     *            project to which the process should belong
     */
    public void setProject(Project project) {
        this.project = project;
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
     * Sets the business domain specification this process is using.
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
    public Docket getDocket() {
        return docket;
    }

    @Override
    public void setDocket(DocketInterface docket) {
        this.docket = (Docket) docket;
    }

    /**
     * Sets the docket generation statement to use when creating a docket for
     * this process.
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

    /**
     * Get template.
     *
     * @return value of template
     */
    public Template getTemplate() {
        return template;
    }

    /**
     * Set template.
     *
     * @param template
     *            as Template object
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * Get parent.
     *
     * @return value of parent
     */
    public Process getParent() {
        return parent;
    }

    /**
     * Set parent.
     *
     * @param parent
     *            as org.kitodo.data.database.beans.Process
     */
    public void setParent(Process parent) {
        this.parent = parent;
    }

    /**
     * Get children.
     *
     * @return value of children
     */
    public List<Process> getChildren() {
        initialize(new ProcessDAO(), this.children);
        if (Objects.isNull(this.children)) {
            this.children = new ArrayList<>();
        }
        return this.children;
    }

    /**
     * Set children.
     *
     * @param children
     *            as List of Process objects
     */
    public void setChildren(List<Process> children) {
        this.children = children;
    }

    @Override
    public List<Task> getTasks() {
        initialize(new ProcessDAO(), this.tasks);
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
     * Get list of templates.
     *
     * @return list of Property objects or empty list
     */
    public List<Property> getTemplates() {
        initialize(new ProcessDAO(), this.templates);
        if (Objects.isNull(this.templates)) {
            this.templates = new ArrayList<>();
        }
        return this.templates;

    }

    /**
     * Set list of templates.
     *
     * @param templates
     *            as list of Property objects
     */
    public void setTemplates(List<Property> templates) {
        this.templates = templates;
    }

    /**
     * Get list of workpieces.
     *
     * @return list of Property objects or empty list
     */
    public List<Property> getWorkpieces() {
        initialize(new ProcessDAO(), this.workpieces);
        if (Objects.isNull(this.workpieces)) {
            this.workpieces = new ArrayList<>();
        }
        return this.workpieces;
    }

    /**
     * Set list of workpieces.
     *
     * @param workpieces
     *            as list of Property objects
     */
    public void setWorkpieces(List<Property> workpieces) {
        this.workpieces = workpieces;
    }

    @Override
    public List<Batch> getBatches() {
        initialize(new ProcessDAO(), this.batches);
        if (Objects.isNull(this.batches)) {
            this.batches = new ArrayList<>();
        }
        return this.batches;
    }

    /*
     * Set batches, if list is empty just set, if not first clear and next set.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setBatches(List<? extends BatchInterface> batches) {
        if (this.batches == null) {
            this.batches = (List<Batch>) batches;
        } else {
            this.batches.clear();
            this.batches.addAll((List<? extends Batch>) batches);
        }
    }

    /**
     * Get comments.
     *
     * @return value of comments
     */
    public List<Comment> getComments() {
        initialize(new ProcessDAO(), this.comments);
        if (Objects.isNull(this.comments)) {
            this.comments = new ArrayList<>();
        }
        return this.comments;
    }

    /**
     * Set comments.
     *
     * @param comments as List of Comment objects
     */
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    @Override
    public List<Property> getProperties() {
        initialize(new ProcessDAO(), this.properties);
        if (Objects.isNull(this.properties)) {
            this.properties = new ArrayList<>();
        }
        return this.properties;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void setProperties(List<? extends PropertyInterface> properties) {
        this.properties = (List<Property>) properties;
    }

    /**
     * Get exported.
     *
     * @return value of exported
     */
    public boolean isExported() {
        return exported;
    }

    /**
     * Set exported.
     *
     * @param exported as boolean
     */
    public void setExported(boolean exported) {
        this.exported = exported;
    }

    /**
     * Get metadata.
     *
     * @return value of metadata
     */
    public List<Map<String, Object>> getMetadata() {
        return metadata;
    }

    /**
     * Set metadata.
     *
     * @param metadata as Map
     */
    public void setMetadata(List<Map<String, Object>> metadata) {
        this.metadata = metadata;
    }

    @Override
    public UserInterface getBlockedUser() {
        return blockedUser;
    }

    @Override
    public void setBlockedUser(UserInterface blockedUser) {
        this.blockedUser = blockedUser;
    }

    @Override
    public String getBaseType() {
        return baseType;
    }

    @Override
    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }

    /**
     * Get inChoiceListShown.
     *
     * @return value of inChoiceListShown
     */
    public Boolean getInChoiceListShown() {
        return this.inChoiceListShown;
    }

    /**
     * Set inChoiceListShown.
     *
     * @param inChoiceListShown as java.lang.Boolean
     */
    public void setInChoiceListShown(Boolean inChoiceListShown) {
        this.inChoiceListShown = inChoiceListShown;
    }

    /**
     * Determines whether or not two processes are equal. Two instances of
     * {@code Process} are equal if the values of their {@code Id}, {@code Title},
     * {@code OutputName} and {@code CreationDate} member fields are the same.
     *
     * @param object
     *            An object to be compared with this {@code Process}.
     * @return {@code true} if the object to be compared is an instance of
     *         {@code Process} and has the same values; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof Process) {
            Process process = (Process) object;
            return Objects.equals(this.getId(), process.getId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }

    @Override
    public Integer getNumberOfStructures() {
        return numberOfStructures;
    }

    @Override
    public Integer getNumberOfMetadata() {
        return numberOfMetadata;
    }

    @Override
    public void setNumberOfMetadata(Integer numberOfMetadata) {
        this.numberOfMetadata = numberOfMetadata;
    }

    @Override
    public Integer getNumberOfImages() {
        return numberOfImages;
    }

    @Override
    public void setNumberOfImages(Integer numberOfImages) {
        this.numberOfImages = numberOfImages;
    }

    @Override
    public void setNumberOfStructures(Integer numberOfStructures) {
        this.numberOfStructures = numberOfStructures;
    }

    /**
     * Get OCR-D workflow identifier.
     *
     * @return The OCR-D workflow identifier
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
    public Double getProgressClosed() {
        if (CollectionUtils.isEmpty(tasks)) {
            return 0.0;
        }
        return getProgressPercentageExact(TaskStatus.DONE);
    }

    @Override
    public Double getProgressInProcessing() {
        if (CollectionUtils.isEmpty(tasks)) {
            return 0.0;
        }
        return getProgressPercentageExact(TaskStatus.INWORK);
    }

    @Override
    public Double getProgressLocked() {
        if (CollectionUtils.isEmpty(tasks)) {
            return 100.0;
        }
        return getProgressPercentageExact(TaskStatus.LOCKED);

    }

    @Override
    public Double getProgressOpen() {
        if (CollectionUtils.isEmpty(tasks)) {
            return 0.0;
        }
        return getProgressPercentageExact(TaskStatus.OPEN);
    }

    private Double getProgressPercentageExact(TaskStatus status) {
        Map<TaskStatus, Double> taskProgress = ProcessConverter.getTaskProgressPercentageOfProcess(this, true);
        return taskProgress.get(status);
    }

    @Override
    public String getProgressCombined() {
        Map<TaskStatus, Double> taskProgress = ProcessConverter.getTaskProgressPercentageOfProcess(this, true);
        return ProcessConverter.getCombinedProgressFromTaskPercentages(taskProgress);
    }

    @Override
    public String getBatchID() {
        return batches.stream().map(Batch::getTitle).collect(Collectors.joining(", "));
    }

    @Override
    public Integer getParentID() {
        return Objects.nonNull(parent) ? parent.getId() : null;
    }

    @Override
    public void setParentID(Integer parentID) {
        this.parent = HibernateUtil.getSession().get(Process.class, parentID);
    }

    @Override
    public boolean hasChildren() {
        return CollectionUtils.isNotEmpty(children);
    }

    @Override
    public void setHasChildren(boolean hasChildren) {
        if (!hasChildren && Objects.nonNull(children)) {
            children.forEach(child -> child.setParent(null));
            children.clear();
        } else if (hasChildren && CollectionUtils.isEmpty(children)) {
            throw new UnsupportedOperationException("cannot insert child processes");
        }
    }

    @Override
    public String getLastEditingUser() {
        return ProcessConverter.getLastEditingUser(this);
    }

    @Override
    public Date getProcessingBeginLastTask() {
        return ProcessConverter.getLastProcessingBegin(this);
    }

    @Override
    public Date getProcessingEndLastTask() {
        return ProcessConverter.getLastProcessingEnd(this);
    }

    @Override
    public Integer getCorrectionCommentStatus() {
        return ProcessConverter.getCorrectionCommentStatus(this).getValue();
    }

    @Override
    public boolean hasComments() {
        return CollectionUtils.isNotEmpty(comments);
    }

    @Override
    public void setHasComments(boolean hasComments) {
        if (!hasComments && Objects.nonNull(comments)) {
            comments.forEach(comment -> comment.setProcess(null));
            comments.clear();
        } else if (hasComments && CollectionUtils.isEmpty(comments)) {
            throw new UnsupportedOperationException("cannot insert comments");
        }
    }
}
