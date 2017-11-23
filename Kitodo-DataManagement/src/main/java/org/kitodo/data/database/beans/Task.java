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

import org.apache.commons.lang.StringUtils;
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

    @Column(name = "typeImportFileUpload")
    private boolean typeImportFileUpload = false;

    @Column(name = "typeExportRussian")
    private boolean typeExportRussian = false;

    @Column(name = "typeImagesRead")
    private boolean typeImagesRead = false;

    @Column(name = "typeImagesWrite")
    private boolean typeImagesWrite = false;

    @Column(name = "typeExportDms")
    private boolean typeExportDMS = false;

    @Column(name = "typeAcceptClose")
    private boolean typeAcceptClose = false;

    @Column(name = "typeScriptStep")
    private Boolean typeScriptStep = false;

    @Column(name = "scriptName")
    private String scriptName;

    @Column(name = "typeAutomaticScriptPath")
    private String typeAutomaticScriptPath;

    @Column(name = "typeCloseVerify")
    private boolean typeCloseVerify = false;

    @Column(name = "batchStep")
    private Boolean batchStep = false;

    @Column(name = "stepPlugin")
    private String stepPlugin;

    @Column(name = "validationPlugin")
    private String validationPlugin;

    /**
     * This field contains information about user, which works on this task.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_task_user_id"))
    private User processingUser;

    @ManyToOne
    @JoinColumn(name = "process_id", foreignKey = @ForeignKey(name = "FK_task_process_id"))
    private Process process;

    /**
     * This field contains information about users, which are allowed to work on
     * this task.
     */
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "task_x_user", joinColumns = {
            @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "FK_task_x_user_task_id")) }, inverseJoinColumns = {
                    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_task_x_user_user_id")) })
    private List<User> users;

    /**
     * This field contains information about user's groups, which are allowed to
     * work on this task.
     */
    @ManyToMany(cascade = CascadeType.PERSIST)
    @JoinTable(name = "task_x_userGroup", joinColumns = {
            @JoinColumn(name = "task_id", foreignKey = @ForeignKey(name = "FK_task_x_userGroup_task_id")) }, inverseJoinColumns = {
                    @JoinColumn(name = "userGroup_id", foreignKey = @ForeignKey(name = "FK_task_x_user_userGroup_id")) })
    private List<UserGroup> userGroups;

    @Transient
    private boolean panelShown = false;

    /**
     * Constructor.
     */
    public Task() {
        this.title = "";
        this.users = new ArrayList<>();
        this.userGroups = new ArrayList<>();
        this.priority = 0;
        this.ordering = 0;
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

    public boolean isPanelShown() {
        return this.panelShown;
    }

    public void setPanelShown(boolean panelShown) {
        this.panelShown = panelShown;
    }

    public List<User> getUsers() {
        if (this.users == null) {
            this.users = new ArrayList<>();
        }
        return this.users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<UserGroup> getUserGroups() {
        if (this.userGroups == null) {
            this.userGroups = new ArrayList<>();
        }
        return this.userGroups;
    }

    public void setUserGroups(List<UserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    public boolean isTypeExportRussian() {
        return this.typeExportRussian;
    }

    public void setTypeExportRussian(boolean typeExportRussian) {
        this.typeExportRussian = typeExportRussian;
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

    public boolean isTypeImportFileUpload() {
        return this.typeImportFileUpload;
    }

    public void setTypeImportFileUpload(boolean typeImportFileUpload) {
        this.typeImportFileUpload = typeImportFileUpload;
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

    public Boolean getTypeScriptStep() {
        if (this.typeScriptStep == null) {
            this.typeScriptStep = false;
        }
        return this.typeScriptStep;
    }

    public void setTypeScriptStep(Boolean typeScriptStep) {
        this.typeScriptStep = typeScriptStep;
    }

    public String getScriptName() {
        return this.scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getTypeAutomaticScriptPath() {
        return this.typeAutomaticScriptPath;
    }

    public void setTypeAutomaticScriptPath(String typeAutomaticScriptPath) {
        this.typeAutomaticScriptPath = typeAutomaticScriptPath;
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

    public String getStepPlugin() {
        return stepPlugin;
    }

    public void setStepPlugin(String stepPlugin) {
        this.stepPlugin = stepPlugin;
    }

    public String getValidationPlugin() {
        return validationPlugin;
    }

    public void setValidationPlugin(String validationPlugin) {
        this.validationPlugin = validationPlugin;
    }

    // Here will be methods which should be in TaskService but are used by jsp
    // files

    public String getTitleWithUserName() {
        String result = this.getTitle();
        if (this.getProcessingUser() != null && this.getProcessingUser().getId() != null
                && this.getProcessingUser().getId() != 0) {
            result += " (" + this.getProcessingUser().getFullName() + ")";
        }
        return result;
    }

    public String getLocalizedTitle() {
        return this.title;
        // return Helper.getTranslation(task.getTitle());
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

    /**
     * Get script names for displaying in the frontend.
     * 
     * @return script name as String
     */
    public String getListOfPaths() {
        if (!StringUtils.isBlank(this.scriptName)) {
            return this.scriptName;
        }
        return "";

    }

}
