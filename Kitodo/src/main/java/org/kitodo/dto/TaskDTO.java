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

package org.kitodo.dto;

import java.util.ArrayList;
import java.util.List;

import org.kitodo.data.database.helper.enums.TaskEditType;
import org.kitodo.data.database.helper.enums.TaskStatus;

/**
 * Task DTO object.
 */
public class TaskDTO extends BaseDTO {

    private String title;
    private String localizedTitle;
    private Integer priority;
    private Integer ordering;
    private TaskStatus processingStatus;
    private String processingStatusTitle;
    private TaskEditType editType;
    private String editTypeTitle;
    private UserDTO processingUser;
    private String processingTime;
    private String processingBegin;
    private String processingEnd;
    private ProcessDTO process;
    private TemplateDTO template;
    private List<UserDTO> users = new ArrayList<>();
    private Integer usersSize;
    private List<UserGroupDTO> userGroups = new ArrayList<>();
    private Integer userGroupsSize;
    private boolean typeAutomatic;
    private boolean typeMetadata;
    private boolean typeImportFileUpload;
    private boolean typeExportRussian;
    private boolean typeImagesRead;
    private boolean typeImagesWrite;
    private boolean batchStep;
    private boolean batchAvailable;
    private boolean selected = false;

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
     * Get priority as Integer.
     * 
     * @return priority as Integer
     */
    public Integer getPriority() {
        return this.priority;
    }

    /**
     * Set priority as Integer.
     * 
     * @param priority
     *            as Integer
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
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
     * Get processing user as UserDTO.
     * 
     * @return processing user as UserDTO
     */
    public UserDTO getProcessingUser() {
        return processingUser;
    }

    /**
     * Set processing user as UserDTO.
     * 
     * @param processingUser
     *            as UserDTO
     */
    public void setProcessingUser(UserDTO processingUser) {
        this.processingUser = processingUser;
    }

    /**
     * Get processing time as String.
     * 
     * @return processing time as String
     */
    public String getProcessingTime() {
        return processingTime;
    }

    /**
     * Set processing time as String.
     * 
     * @param processingTime
     *            as String
     */
    public void setProcessingTime(String processingTime) {
        this.processingTime = processingTime;
    }

    /**
     * Get processing begin time as String.
     * 
     * @return processing begin time as String
     */
    public String getProcessingBegin() {
        return processingBegin;
    }

    /**
     * Set processing begin time as String.
     * 
     * @param processingBegin
     *            as String
     */
    public void setProcessingBegin(String processingBegin) {
        this.processingBegin = processingBegin;
    }

    /**
     * Get processing end time as String.
     * 
     * @return processing end time as String
     */
    public String getProcessingEnd() {
        return processingEnd;
    }

    /**
     * Set processing end time as String.
     * 
     * @param processingEnd
     *            as String
     */
    public void setProcessingEnd(String processingEnd) {
        this.processingEnd = processingEnd;
    }

    /**
     * Get process as ProcessDTO.
     * 
     * @return process as ProcessDTO
     */
    public ProcessDTO getProcess() {
        return process;
    }

    /**
     * Set process as ProcessDTO.
     * 
     * @param process
     *            as ProcessDTO
     */
    public void setProcess(ProcessDTO process) {
        this.process = process;
    }

    /**
     * Get template as TemplateDTO.
     *
     * @return template as TemplateDTO
     */
    public TemplateDTO getTemplate() {
        return template;
    }

    /**
     * Set template as TemplateDTO.
     *
     * @param template
     *            as TemplateDTO
     */
    public void setTemplate(TemplateDTO template) {
        this.template = template;
    }

    /**
     * Get list of users.
     * 
     * @return list of users as UserDTO
     */
    public List<UserDTO> getUsers() {
        return this.users;
    }

    /**
     * Set list of users.
     * 
     * @param users
     *            list of users as UserDTO
     */
    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }

    /**
     * Get user size.
     *
     * @return the size of the user list
     */
    public Integer getUsersSize() {
        return this.usersSize;
    }

    /**
     * Set users size.
     * 
     * @param usersSize
     *            as Integer
     */
    public void setUsersSize(Integer usersSize) {
        this.usersSize = usersSize;
    }

    /**
     * Get list of user's groups.
     * 
     * @return list of user's groups as UserGroupDTO.
     */
    public List<UserGroupDTO> getUserGroups() {
        return this.userGroups;
    }

    /**
     * Set list of user's groups.
     * 
     * @param userGroups
     *            as List of UserGroupDTO
     */
    public void setUserGroups(List<UserGroupDTO> userGroups) {
        this.userGroups = userGroups;
    }

    /**
     * Get user group size.
     *
     * @return the size of the userGroup list
     */
    public Integer getUserGroupsSize() {
        return this.userGroupsSize;
    }

    /**
     * Set user groups size.
     * 
     * @param userGroupsSize
     *            as Integer
     */
    public void setUserGroupsSize(Integer userGroupsSize) {
        this.userGroupsSize = userGroupsSize;
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
     * Get information if task type is import file upload.
     * 
     * @return true or false
     */
    public boolean isTypeImportFileUpload() {
        return typeImportFileUpload;
    }

    /**
     * Set information if task type is import file upload.
     * 
     * @param typeImportFileUpload
     *            as boolean
     */
    public void setTypeImportFileUpload(boolean typeImportFileUpload) {
        this.typeImportFileUpload = typeImportFileUpload;
    }

    /**
     * Get information if task type is export russian.
     *
     * @return true or false
     */
    public boolean isTypeExportRussian() {
        return typeExportRussian;
    }

    /**
     * Set information if task type is export russian.
     *
     * @param typeExportRussian
     *            as boolean
     */
    public void setTypeExportRussian(boolean typeExportRussian) {
        this.typeExportRussian = typeExportRussian;
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
     * Get information if task is selected.
     *
     * @return true or false
     */
    public boolean isSelected() {
        return this.selected;
    }

    /**
     * Set information if task is selected.
     *
     * @param selected
     *            as boolean
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
