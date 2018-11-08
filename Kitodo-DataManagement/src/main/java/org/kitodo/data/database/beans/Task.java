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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;

@Entity
@Table(name = "task")
public class Task extends BaseIndexedBean {
    private static final long serialVersionUID = 6831844584239811846L;

    @Column(name = "title")
    private String title;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "ordering")
    private Integer ordering;

    @Column(name = "processingStatus")
    private Integer processingStatus;

    @Column(name = "processingTime")
    private Date processingTime;

    @Column(name = "processingBegin")
    private Date processingBegin;

    @Column(name = "processingEnd")
    private Date processingEnd;

    @Column(name = "editType")
    private Integer editType;

    @Column(name = "homeDirectory")
    private short homeDirectory;

    @Column(name = "typeMetadata")
    private boolean typeMetadata = false;

    @Column(name = "typeAutomatic")
    private boolean typeAutomatic = false;

    @Column(name = "typeImagesRead")
    private boolean typeImagesRead = false;

    @Column(name = "typeImagesWrite")
    private boolean typeImagesWrite = false;

    @Column(name = "typeExportDms")
    private boolean typeExportDMS = false;

    @Column(name = "typeAcceptClose")
    private boolean typeAcceptClose = false;

    @Column(name = "scriptName")
    private String scriptName;

    @Column(name = "scriptPath")
    private String scriptPath;

    @Column(name = "typeCloseVerify")
    private boolean typeCloseVerify = false;

    @Column(name = "batchStep")
    private Boolean batchStep = false;

    @Column(name = "workflowId")
    private String workflowId;

    @Column(name = "workflowCondition")
    private String workflowCondition;

    @Column(name = "previousTasks")
    private String previousTasks;

    @Column(name = "concurrentTasks")
    private String concurrentTasks;

    @Column(name = "nextTasks")
    private String nextTasks;

    /**
     * This field contains information about user, which works on this task.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_task_user_id"))
    private User processingUser;

    @ManyToOne
    @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "FK_task_template_id"))
    private Template template;

    @ManyToOne
    @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_task_process_id"))
    private Process process;

    /**
     * This field contains information about user's roles, which are allowed to
     * work on this task.
     */
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "task_x_role", joinColumns = {
            @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "FK_task_x_role_task_id")) }, inverseJoinColumns = {
                    @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "FK_task_x_user_role_id")) })
    private List<Role> roles;

    /**
     * This field contains information about folders whose contents are to be
     * generated in this task.
     */
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "contentFolders_task_x_folder",
        joinColumns = @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "FK_contentFolders_task_x_folder_task_id")),
        inverseJoinColumns = @JoinColumn(name = "folder_id", foreignKey = @ForeignKey(name = "FK_task_x_folder_folder_id"))
    )
    private List<Folder> contentFolders;

    /**
     * This field contains information about folders whose contents are to be
     * validated in this task.
     */
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "validationFolders_task_x_folder",
        joinColumns = @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "FK_validationFolders_task_x_folder_task_id")),
        inverseJoinColumns = @JoinColumn(name = "folder_id", foreignKey = @ForeignKey(name = "FK_task_x_folder_folder_id"))
    )
    private List<Folder> validationFolders;

    @Transient
    private String localizedTitle;

    /**
     * Constructor.
     */
    public Task() {
        this.title = "";
        this.roles = new ArrayList<>();
        this.priority = 0;
        this.ordering = 0;
    }

    /**
     * Copy constructor.
     *
     * @param templateTask
     *            task to copy
     */
    public Task(Task templateTask) {
        this.title = templateTask.getTitle();
        this.ordering = templateTask.getOrdering();
        this.priority = templateTask.getPriority();
        this.typeAutomatic = templateTask.isTypeAutomatic();
        this.scriptName = templateTask.getScriptName();
        this.scriptPath = templateTask.getScriptPath();
        this.batchStep = templateTask.isBatchStep();
        this.typeAcceptClose = templateTask.isTypeAcceptClose();
        this.typeCloseVerify = templateTask.isTypeCloseVerify();
        this.typeExportDMS = templateTask.isTypeExportDMS();
        this.typeImagesRead = templateTask.isTypeImagesRead();
        this.typeImagesWrite = templateTask.isTypeImagesWrite();
        this.typeMetadata = templateTask.isTypeMetadata();
        this.processingStatus = templateTask.getProcessingStatus();
        this.homeDirectory = templateTask.getHomeDirectory();
        this.workflowId = templateTask.getWorkflowId();
        this.workflowCondition = templateTask.getWorkflowCondition();
        this.previousTasks = templateTask.getPreviousTasks();
        this.concurrentTasks = templateTask.getConcurrentTasks();
        this.nextTasks = templateTask.getNextTasks();

        // necessary to create new ArrayList in other case session problem!
        this.roles = new ArrayList<>(templateTask.getRoles());

        // necessary to create new ArrayList in other case session problem!
        this.contentFolders = new ArrayList<>(templateTask.getContentFolders());
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPriority() {
        return this.priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getOrdering() {
        return this.ordering;
    }

    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    /**
     * Getter for editType set to private for hibernate, for use in program use
     * getEditTypeEnum instead.
     *
     * @return editType as integer
     */
    @SuppressWarnings("unused")
    private Integer getEditType() {
        return this.editType;
    }

    /**
     * Set editType to defined integer. only for internal use through hibernate,
     * for changing editType use setEditTypeEnum instead.
     *
     * @param editType
     *            as Integer
     */
    public void setEditType(Integer editType) {
        this.editType = editType;
    }

    /**
     * Get editType as {@link TaskEditType}.
     *
     * @return current edit type
     */
    public TaskEditType getEditTypeEnum() {
        return TaskEditType.getTypeFromValue(this.editType);
    }

    /**
     * Set editType to specific value from {@link TaskEditType}.
     *
     * @param inputType
     *            as {@link TaskEditType}
     */
    public void setEditTypeEnum(TaskEditType inputType) {
        this.editType = inputType.getValue();
    }

    /**
     * Getter for processing status (set to private for hibernate), for use in
     * program use getProcessingStatusEnum instead.
     *
     * @return processingStatus as integer
     */
    public Integer getProcessingStatus() {
        return this.processingStatus;
    }

    /**
     * Set processing status to defined integer. only for internal use through
     * hibernate, for changing processing status use setProcessingStatusEnum
     * instead.
     *
     * @param processingStatus
     *            as Integer
     */
    public void setProcessingStatus(Integer processingStatus) {
        this.processingStatus = processingStatus;
    }

    /**
     * Set processing status to specific value from {@link TaskStatus}.
     *
     * @param inputStatus
     *            as {@link TaskStatus}
     */
    public void setProcessingStatusEnum(TaskStatus inputStatus) {
        this.processingStatus = inputStatus.getValue();
    }

    /**
     * Get processing status as {@link TaskStatus}.
     *
     * @return current processing status
     */
    public TaskStatus getProcessingStatusEnum() {
        return TaskStatus.getStatusFromValue(this.processingStatus);
    }

    public Date getProcessingTime() {
        return this.processingTime;
    }

    public void setProcessingTime(Date processingTime) {
        this.processingTime = processingTime;
    }

    public Date getProcessingBegin() {
        return this.processingBegin;
    }

    public void setProcessingBegin(Date processingBegin) {
        this.processingBegin = processingBegin;
    }

    public Date getProcessingEnd() {
        return this.processingEnd;
    }

    public void setProcessingEnd(Date processingEnd) {
        this.processingEnd = processingEnd;
    }

    public short getHomeDirectory() {
        return this.homeDirectory;
    }

    public void setHomeDirectory(short homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    public User getProcessingUser() {
        return this.processingUser;
    }

    public void setProcessingUser(User processingUser) {
        this.processingUser = processingUser;
    }

    public Process getProcess() {
        return this.process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    /**
     * Get template.
     *
     * @return value of template
     */
    public Template getTemplate() {
        return this.template;
    }

    /**
     * Set template.
     *
     * @param template
     *            as Template
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * Get list of roles.
     *
     * @return list of Role objects or empty list
     */
    public List<Role> getRoles() {
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        return this.roles;
    }

    /**
     * Set list of roles.
     *
     * @param roles
     *            as list of Role objects
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    /**
     * Get list of folders whose contents are to be generated.
     *
     * @return list of Folder objects or empty list
     */
    public List<Folder> getContentFolders() {
        if (this.contentFolders == null) {
            this.contentFolders = new ArrayList<>();
        }
        return contentFolders;
    }

    /**
     * Set list of folders whose contents are to be generated.
     *
     * @param contentFolders
     *            as list
     */
    public void setContentFolders(List<Folder> contentFolders) {
        this.contentFolders = contentFolders;
    }

    /**
     * Get list of folders whose contents are to be validated.
     *
     * @return list of Folder objects or empty list
     */
    public List<Folder> getValidationFolders() {
        if (this.validationFolders == null) {
            this.validationFolders = new ArrayList<>();
        }
        return validationFolders;
    }

    /**
     * Set list of folders whose contents are to be validated.
     *
     * @param validationFolders
     *            as list
     */
    public void setValidationFolders(List<Folder> validationFolders) {
        this.validationFolders = validationFolders;
    }

    public boolean isTypeImagesRead() {
        return this.typeImagesRead;
    }

    public void setTypeImagesRead(boolean typeImagesRead) {
        this.typeImagesRead = typeImagesRead;
    }

    public boolean isTypeImagesWrite() {
        return this.typeImagesWrite;
    }

    /**
     * Set task type images. If types is true, it also sets type images read to
     * true.
     *
     * @param typeImagesWrite
     *            true or false
     */
    public void setTypeImagesWrite(boolean typeImagesWrite) {
        this.typeImagesWrite = typeImagesWrite;
        if (typeImagesWrite) {
            this.typeImagesRead = true;
        }
    }

    public boolean isTypeExportDMS() {
        return this.typeExportDMS;
    }

    public void setTypeExportDMS(boolean typeExportDMS) {
        this.typeExportDMS = typeExportDMS;
    }

    public boolean isTypeMetadata() {
        return this.typeMetadata;
    }

    public void setTypeMetadata(boolean typeMetadata) {
        this.typeMetadata = typeMetadata;
    }

    public boolean isTypeAcceptClose() {
        return this.typeAcceptClose;
    }

    public void setTypeAcceptClose(boolean typeAcceptClose) {
        this.typeAcceptClose = typeAcceptClose;
    }

    public boolean isTypeAutomatic() {
        return this.typeAutomatic;
    }

    public void setTypeAutomatic(boolean typeAutomatic) {
        this.typeAutomatic = typeAutomatic;
    }

    public boolean isTypeCloseVerify() {
        return this.typeCloseVerify;
    }

    public void setTypeCloseVerify(boolean typeCloseVerify) {
        this.typeCloseVerify = typeCloseVerify;
    }

    public String getScriptName() {
        return this.scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptPath() {
        return this.scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
    }

    /**
     * Get workflow id - id of task object in diagram - by this id we can identify
     * change done to task.
     *
     * @return workflow id as String
     */
    public String getWorkflowId() {
        return workflowId;
    }

    /**
     * Set workflow id.
     *
     * @param workflowId
     *            id of task object in diagram - by this id we can identify change
     *            done to task
     */
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * Get workflowCondition.
     *
     * @return value of workflowCondition
     */
    public String getWorkflowCondition() {
        return workflowCondition;
    }

    /**
     * Set workflowCondition.
     *
     * @param workflowCondition
     *            as String
     */
    public void setWorkflowCondition(String workflowCondition) {
        this.workflowCondition = workflowCondition;
    }

    /**
     * Get workflow ids of previous tasks separated by commas. There are three
     * possible states for this field. First, task has zero previous tasks - it
     * means that it is first task. Second, task has one previous tasks - it means
     * that they are going to be executed right one after another. Third, task has
     * many previous tasks - it means that first all previous tasks must be executed
     * before this is available to take by user.
     *
     * @return workflow ids of previous tasks separated by commas
     */
    public String getPreviousTasks() {
        return previousTasks;
    }

    /**
     * Set workflow ids of previous tasks separated by commas.
     *
     * @param previousTasks
     *            workflow ids of previous tasks separated by commas as one String
     */
    public void setPreviousTasks(String previousTasks) {
        this.previousTasks = previousTasks;
    }

    /**
     * Get workflow ids of concurrent tasks separated by commas. There are three
     * possible states for this field. First, task has zero concurrent tasks - it
     * means that it is only task which is going to be executed. Second and third
     * are similar, task has concurrent tasks - they all are going to be available
     * for execution after previous one is ready and all needed to be finished
     * before next one is set up to available to take by user.
     *
     * @return workflow ids of previous tasks separated by commas
     */
    public String getConcurrentTasks() {
        return concurrentTasks;
    }

    /**
     * Set workflow ids of concurrent tasks separated by commas.
     *
     * @param concurrentTasks
     *            workflow ids of concurrent tasks separated by commas as one String
     */
    public void setConcurrentTasks(String concurrentTasks) {
        this.concurrentTasks = concurrentTasks;
    }

    /**
     * Get workflow ids of next tasks separated by commas. There are three possible
     * states for this field. First, task has zero next tasks - it means that it is
     * last task. Second, task has one next tasks - it means that they are going to
     * be executed right one after another. Third, task has many next tasks - it
     * means that after this task is executed those next tasks will be set up to
     * available to take by user.
     *
     * @return workflow ids of next tasks separated by commas
     */
    public String getNextTasks() {
        return nextTasks;
    }

    /**
     * Set workflow ids of next tasks separated by commas.
     *
     * @param nextTasks
     *            workflow ids of next tasks separated by commas as one String
     */
    public void setNextTasks(String nextTasks) {
        this.nextTasks = nextTasks;
    }

    public Boolean getBatchStep() {
        if (this.batchStep == null) {
            this.batchStep = Boolean.FALSE;
        }
        return this.batchStep;
    }

    public Boolean isBatchStep() {
        if (this.batchStep == null) {
            this.batchStep = Boolean.FALSE;
        }
        return this.batchStep;
    }

    public void setBatchStep(Boolean batchStep) {
        if (batchStep == null) {
            batchStep = Boolean.FALSE;
        }
        this.batchStep = batchStep;
    }

    /**
     * Get localized title.
     *
     * @return localized title as String
     */
    public String getLocalizedTitle() {
        return this.localizedTitle;
    }

    /**
     * Set localized titles as String.
     *
     * @param localizedTitle
     *            as String
     */
    public void setLocalizedTitle(String localizedTitle) {
        this.localizedTitle = localizedTitle;
    }

    // Here will be methods which should be in TaskService but are used by jsp
    // files
    /**
     * Get task title with user full name.
     *
     * @return task title with user full name as String
     */
    public String getTitleWithUserName() {
        String result = this.getTitle();
        if (this.getProcessingUser() != null && this.getProcessingUser().getId() != null
                && this.getProcessingUser().getId() != 0) {
            result += " (" + this.getProcessingUser().getFullName() + ")";
        }
        return result;
    }

    public String getProcessingStatusAsString() {
        return String.valueOf(this.processingStatus);
    }

    public void setProcessingStatusAsString(String inputProcessingStatus) {
        this.processingStatus = Integer.parseInt(inputProcessingStatus);
    }

    public String getProcessingBeginAsFormattedString() {
        return this.processingBegin.toString();
        // return Helper.getDateAsFormattedString(task.getProcessingBegin());
    }

    public String getProcessingTimeAsFormattedString() {
        return this.processingTime.toString();
        // return Helper.getDateAsFormattedString(task.getProcessingTime());
    }

    public String getProcessingEndAsFormattedString(Task task) {
        return task.processingEnd.toString();
        // return Helper.getDateAsFormattedString(task.getProcessingEnd());
    }

    public int getProcessingTimeNow() {
        return 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return homeDirectory == task.homeDirectory
                && typeMetadata == task.typeMetadata
                && typeAutomatic == task.typeAutomatic
                && typeImagesRead == task.typeImagesRead
                && typeImagesWrite == task.typeImagesWrite
                && typeExportDMS == task.typeExportDMS
                && typeAcceptClose == task.typeAcceptClose
                && typeCloseVerify == task.typeCloseVerify
                && Objects.equals(title, task.title)
                && Objects.equals(priority, task.priority)
                && Objects.equals(ordering, task.ordering)
                && Objects.equals(processingStatus, task.processingStatus)
                && Objects.equals(processingTime, task.processingTime)
                && Objects.equals(processingBegin, task.processingBegin)
                && Objects.equals(processingEnd, task.processingEnd)
                && Objects.equals(editType, task.editType)
                && Objects.equals(scriptName, task.scriptName)
                && Objects.equals(scriptPath, task.scriptPath)
                && Objects.equals(batchStep, task.batchStep)
                && Objects.equals(workflowId, task.workflowId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, priority, ordering, processingStatus, processingTime, processingBegin, processingEnd,
            editType, homeDirectory, typeMetadata, typeAutomatic, typeImagesRead, typeImagesWrite, typeExportDMS,
            typeAcceptClose, scriptName, scriptPath, typeCloseVerify, batchStep, workflowId, processingUser, template,
            localizedTitle);
    }
}
