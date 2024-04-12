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

package org.kitodo.production.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.util.Strings;
import org.kitodo.data.database.enums.TaskEditType;
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.interfaces.ProcessInterface;
import org.kitodo.data.interfaces.ProjectInterface;
import org.kitodo.data.interfaces.TaskInterface;
import org.kitodo.data.interfaces.TemplateInterface;
import org.kitodo.data.interfaces.UserInterface;

/**
 * Task DTO object.
 */
public class TaskDTO extends BaseDTO implements TaskInterface {

    private String title;
    private String localizedTitle;
    private Integer ordering;
    private TaskStatus processingStatus;
    private String processingStatusTitle;
    private TaskEditType editType;
    private String editTypeTitle;
    private UserInterface processingUser;
    private String processingTime;
    private String processingBegin;
    private String processingEnd;
    private ProcessInterface process;
    private ProjectInterface project;
    private TemplateInterface template;
    private List<Integer> roleIds = new ArrayList<>();
    private int rolesSize;
    private boolean correction;
    private Integer correctionCommentStatus;
    private boolean typeAutomatic;
    private boolean typeMetadata;
    private boolean typeImagesRead;
    private boolean typeImagesWrite;
    private boolean batchStep;
    private boolean batchAvailable;
    

    /**
     * Get title.
     *
     * @return title as String
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
     * Get localized title.
     *
     * @return localized title as String
     */
    public String getLocalizedTitle() {
        return localizedTitle;
    }

    /**
     * Set localized title.
     *
     * @param localizedTitle
     *            as String
     */
    public void setLocalizedTitle(String localizedTitle) {
        this.localizedTitle = localizedTitle;
    }

    /**
     * Get ordering as Integer.
     *
     * @return ordering as Integer
     */
    public Integer getOrdering() {
        return this.ordering;
    }

    /**
     * Set ordering as Integer.
     *
     * @param ordering
     *            as Integer
     */
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    /**
     * Get processing status as TaskStatus object.
     *
     * @return processing status as TaskStatus object
     */
    public TaskStatus getProcessingStatus() {
        return processingStatus;
    }

    /**
     * Set processing status as TaskStatus object.
     *
     * @param processingStatus
     *            as TaskStatus object
     */
    public void setProcessingStatus(TaskStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    /**
     * Get processing status title as String.
     *
     * @return processing status title as String
     */
    public String getProcessingStatusTitle() {
        return processingStatusTitle;
    }

    /**
     * Set processing status title as String.
     *
     * @param processingStatusTitle
     *            as String
     */
    public void setProcessingStatusTitle(String processingStatusTitle) {
        this.processingStatusTitle = processingStatusTitle;
    }

    /**
     * Get editType as {@link TaskEditType}.
     *
     * @return current edit type
     */
    public TaskEditType getEditType() {
        return editType;
    }

    /**
     * Set editType to specific value from {@link TaskEditType}.
     *
     * @param editType
     *            as {@link TaskEditType}
     */
    public void setEditType(TaskEditType editType) {
        this.editType = editType;
    }

    /**
     * Get edit type title as String.
     *
     * @return current edit type title as String
     */
    public String getEditTypeTitle() {
        return editTypeTitle;
    }

    /**
     * Set edit type title as String.
     *
     * @param editTypeTitle
     *            as String
     */
    public void setEditTypeTitle(String editTypeTitle) {
        this.editTypeTitle = editTypeTitle;
    }

    /**
     * Get processing user as UserInterface.
     *
     * @return processing user as UserInterface
     */
    public UserInterface getProcessingUser() {
        return processingUser;
    }

    /**
     * Set processing user as UserInterface.
     *
     * @param processingUser
     *            as UserInterface
     */
    public void setProcessingUser(UserInterface processingUser) {
        this.processingUser = processingUser;
    }

    /**
     * Get processing time as String.
     *
     * @return processing time as String
     */
    public String getProcessingMoment() {
        return processingTime;
    }

    /**
     * Set processing time as String.
     *
     * @param processingTime
     *            as String
     */
    public void setProcessingMoment(String processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Get processing begin time as String.
     *
     * @return processing begin time as String
     */
    public String getProcessingBeginTime() {
        return processingBegin;
    }

    /**
     * Set processing begin time as String.
     *
     * @param processingBegin
     *            as String
     */
    public void setProcessingBeginTime(String processingBegin) {
        this.processingBegin = processingBegin;
    }

    /**
     * Get processing end time as String.
     *
     * @return processing end time as String
     */
    public String getProcessingEndTime() {
        return processingEnd;
    }

    /**
     * Set processing end time as String.
     *
     * @param processingEnd
     *            as String
     */
    public void setProcessingEndTime(String processingEnd) {
        this.processingEnd = processingEnd;
    }

    /**
     * Get process as ProcessInterface.
     *
     * @return process as ProcessInterface
     */
    public ProcessInterface getProcess() {
        return process;
    }

    /**
     * Set process as ProcessInterface.
     *
     * @param process
     *            as ProcessInterface
     */
    public void setProcess(ProcessInterface process) {
        this.process = process;
    }

    /**
     * Get project.
     *
     * @return project as ProjectInterface
     */
    public ProjectInterface getProject() {
        return project;
    }

    /**
     * Set project.
     *
     * @param project
     *            as ProjectInterface
     */
    public void setProject(ProjectInterface project) {
        this.project = project;
    }

    /**
     * Get template as TemplateInterface.
     *
     * @return template as TemplateInterface
     */
    public TemplateInterface getTemplate() {
        return template;
    }

    /**
     * Set template as TemplateInterface.
     *
     * @param template
     *            as TemplateInterface
     */
    public void setTemplate(TemplateInterface template) {
        this.template = template;
    }

    /**
     * Get list of roles.
     *
     * @return list of role ids as Integer
     */
    public List<Integer> getRoleIds() {
        return roleIds;
    }

    /**
     * Set list of roles.
     *
     * @param roleIds
     *            list of role ids as Integer
     */
    public void setRoleIds(List<Integer> roleIds) {
        this.roleIds = roleIds;
    }

    /**
     * Get roles size.
     *
     * @return the size of the roles list
     */
    public int getRolesSize() {
        return this.rolesSize;
    }

    /**
     * Set roles size.
     *
     * @param rolesSize
     *            as int
     */
    public void setRolesSize(int rolesSize) {
        this.rolesSize = rolesSize;
    }

    /**
     * Get correction.
     *
     * @return value of correction
     */
    public boolean isCorrection() {
        return correction;
    }

    /**
     * Set correction.
     *
     * @param correction as boolean
     */
    public void setCorrection(boolean correction) {
        this.correction = correction;
    }

    /**
     * Get information if task type is automatic.
     *
     * @return true or false
     */
    public boolean isTypeAutomatic() {
        return typeAutomatic;
    }

    /**
     * Set information if task type is automatic.
     *
     * @param typeAutomatic
     *            as boolean
     */
    public void setTypeAutomatic(boolean typeAutomatic) {
        this.typeAutomatic = typeAutomatic;
    }

    /**
     * Get information if task type is metadata.
     *
     * @return true or false
     */
    public boolean isTypeMetadata() {
        return typeMetadata;
    }

    /**
     * Set information if task type is metadata.
     *
     * @param typeMetadata
     *            as boolean
     */
    public void setTypeMetadata(boolean typeMetadata) {
        this.typeMetadata = typeMetadata;
    }

    /**
     * Get information if task type is images read.
     *
     * @return true or false
     */
    public boolean isTypeImagesRead() {
        return typeImagesRead;
    }

    /**
     * Set information if task type is images read.
     *
     * @param typeImagesRead
     *            as boolean
     */
    public void setTypeImagesRead(boolean typeImagesRead) {
        this.typeImagesRead = typeImagesRead;
    }

    /**
     * Get information if task type is images write.
     *
     * @return true or false
     */
    public boolean isTypeImagesWrite() {
        return typeImagesWrite;
    }

    /**
     * Set information if task type is images write.
     *
     * @param typeImagesWrite
     *            as boolean
     */
    public void setTypeImagesWrite(boolean typeImagesWrite) {
        this.typeImagesWrite = typeImagesWrite;
    }

    /**
     * Get information if task is batch(step).
     *
     * @return true or false
     */
    public boolean isBatchStep() {
        return batchStep;
    }

    /**
     * Set information if batch is available for task - there is more than one task
     * with the same title assigned to the batch.).
     *
     * @param batchAvailable
     *            as boolean
     */
    public void setBatchAvailable(boolean batchAvailable) {
        this.batchAvailable = batchAvailable;
    }

    /**
     * Get information if batch is available for task - there is more than one task
     * with the same title assigned to the batch.
     *
     * @return true or false
     */
    public boolean isBatchAvailable() {
        return batchAvailable;
    }

    /**
     * Set information if task is batch(step).
     *
     * @param batchStep
     *            as boolean
     */
    public void setBatchStep(boolean batchStep) {
        this.batchStep = batchStep;
    }

    /**
     * Get the correction comment status, see CorrectionComments enum, as int value.
     * 
     * @return the correction comment status as integer
     */
    public Integer getCorrectionCommentStatus() {
        return this.correctionCommentStatus;
    }

    /**
     * Set the correction comment status as int value.
     * 
     * @param status the status as integer
     */
    public void setCorrectionCommentStatus(Integer status) {
        this.correctionCommentStatus = status;
    }

    @Override
    public Date getProcessingTime() {
        try {
            return Strings.isNotEmpty(this.processingTime)
                    ? new SimpleDateFormat(DATE_FORMAT).parse(this.processingTime)
                    : null;
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void setProcessingTime(Date processingTime) {
        this.processingTime = Objects.nonNull(processingTime) ? new SimpleDateFormat(DATE_FORMAT).format(processingTime)
                : null;
    }

    @Override
    public Date getProcessingBegin() {
        try {
            return Strings.isNotEmpty(this.processingBegin)
                    ? new SimpleDateFormat(DATE_FORMAT).parse(this.processingBegin)
                    : null;
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void setProcessingBegin(Date processingBegin) {
        this.processingBegin = Objects.nonNull(processingBegin)
                ? new SimpleDateFormat(DATE_FORMAT).format(processingBegin)
                : null;
    }

    @Override
    public Date getProcessingEnd() {
        try {
            return Strings.isNotEmpty(this.processingEnd) ? new SimpleDateFormat(DATE_FORMAT).parse(this.processingEnd)
                    : null;
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void setProcessingEnd(Date processingEnd) {
        this.processingEnd = Objects.nonNull(processingEnd) ? new SimpleDateFormat(DATE_FORMAT).format(processingEnd)
                : null;
    }
}
