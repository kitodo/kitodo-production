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
    private TaskEditType editType;
    private UserDTO processingUser;
    private String processingTime;
    private String processingBegin;
    private String processingEnd;
    private ProcessDTO process;
    private List<UserDTO> users;
    private Integer usersSize;
    private List<UserGroupDTO> userGroups;
    private Integer userGroupsSize;
    private boolean batchStep;
    private boolean typeImagesWrite;
    private boolean panelShown = false;
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
     * Get information if task is batch(step).
     * 
     * @return true or false
     */
    public boolean isBatchStep() {
        return batchStep;
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
     * Get information if panel is shown.
     *
     * @return true or false
     */
    public boolean isPanelShown() {
        return this.panelShown;
    }

    /**
     * Set information if panel is shown.
     *
     * @param panelShown
     *            as boolean
     */
    public void setPanelShown(boolean panelShown) {
        this.panelShown = panelShown;
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
