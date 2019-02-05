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

package org.kitodo.production.workflow.model.beans;

import java.util.Objects;

import org.camunda.bpm.model.bpmn.instance.Task;

public class KitodoTask {

    private String workflowId;
    private String title;
    private Integer priority;
    private Integer editType;
    private Integer processingStatus;
    private boolean concurrent;
    private boolean typeMetadata;
    private boolean typeAutomatic;
    private boolean typeExportDms;
    private boolean typeImagesRead;
    private boolean typeImagesWrite;
    private boolean typeAcceptClose;
    private boolean typeCloseVerify;
    private boolean batchStep;
    private String conditionType;
    private String conditionValue;
    private String userRoles;

    static final String NAMESPACE = "http://www.kitodo.org/template";

    /**
     * Constructor.
     * 
     * @param task
     *            BPMN model task
     */
    public KitodoTask(Task task) {
        this.workflowId = task.getId();
        this.title = task.getName();
        this.priority = getIntegerValue(task.getAttributeValueNs(NAMESPACE, "priority"));
        this.editType = getIntegerValue(task.getAttributeValueNs(NAMESPACE, "editType"));
        this.processingStatus = getIntegerValue(task.getAttributeValueNs(NAMESPACE, "processingStatus"));
        this.concurrent = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "concurrent"));
        this.typeMetadata = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeMetadata"));
        this.typeAutomatic = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeAutomatic"));
        this.typeExportDms = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeExportDMS"));
        this.typeImagesRead = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeImagesRead"));
        this.typeImagesWrite = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeImagesWrite"));
        this.typeAcceptClose = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeAcceptClose"));
        this.typeCloseVerify = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeCloseVerify"));
        this.batchStep = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "batchStep"));
        this.conditionType = task.getAttributeValueNs(NAMESPACE, "conditionType");
        this.conditionValue = task.getAttributeValueNs(NAMESPACE, "conditionValue");
        this.userRoles = task.getAttributeValueNs(NAMESPACE, "permittedUserRole");
    }

    private Boolean getBooleanValue(String value) {
        if (Objects.nonNull(value)) {
            return Boolean.valueOf(value);
        }
        return false;
    }

    private Integer getIntegerValue(String value) {
        if (Objects.nonNull(value)) {
            return Integer.valueOf(value);
        }
        return -1;
    }

    /**
     * Get workflow id.
     *
     * @return workflow id as String
     */
    public String getWorkflowId() {
        return workflowId;
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
     * Get priority.
     *
     * @return value of priority
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Set priority.
     *
     * @param priority
     *            as Integer
     */
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    /**
     * Get editType.
     *
     * @return value of editType
     */
    public Integer getEditType() {
        return editType;
    }

    /**
     * Set editType.
     *
     * @param editType
     *            as Integer
     */
    public void setEditType(Integer editType) {
        this.editType = editType;
    }

    /**
     * Get processingStatus.
     *
     * @return value of processingStatus
     */
    public Integer getProcessingStatus() {
        return processingStatus;
    }

    /**
     * Set processingStatus.
     *
     * @param processingStatus
     *            as java.lang.Integer
     */
    public void setProcessingStatus(Integer processingStatus) {
        this.processingStatus = processingStatus;
    }

    /**
     * Get concurrent.
     *
     * @return value of concurrent
     */
    public boolean isConcurrent() {
        return concurrent;
    }

    /**
     * Set concurrent.
     *
     * @param concurrent
     *            as true or false
     */
    public void setConcurrent(boolean concurrent) {
        this.concurrent = concurrent;
    }

    /**
     * Get typeMetadata.
     *
     * @return value of typeMetadata
     */
    public boolean isTypeMetadata() {
        return typeMetadata;
    }

    /**
     * Set typeMetadata.
     *
     * @param typeMetadata
     *            as true or false
     */
    public void setTypeMetadata(boolean typeMetadata) {
        this.typeMetadata = typeMetadata;
    }

    /**
     * Get typeAutomatic.
     *
     * @return value of typeAutomatic
     */
    public boolean isTypeAutomatic() {
        return typeAutomatic;
    }

    /**
     * Set typeAutomatic.
     *
     * @param typeAutomatic
     *            as true or false
     */
    public void setTypeAutomatic(boolean typeAutomatic) {
        this.typeAutomatic = typeAutomatic;
    }

    /**
     * Get typeExportDms.
     *
     * @return value of typeExportDms
     */
    public boolean isTypeExportDms() {
        return typeExportDms;
    }

    /**
     * Set typeExportDms.
     *
     * @param typeExportDms
     *            as true or false
     */
    public void setTypeExportDms(boolean typeExportDms) {
        this.typeExportDms = typeExportDms;
    }

    /**
     * Get typeImagesRead.
     *
     * @return value of typeImagesRead
     */
    public boolean isTypeImagesRead() {
        return typeImagesRead;
    }

    /**
     * Set typeImagesRead.
     *
     * @param typeImagesRead
     *            as true or false
     */
    public void setTypeImagesRead(boolean typeImagesRead) {
        this.typeImagesRead = typeImagesRead;
    }

    /**
     * Get typeImagesWrite.
     *
     * @return value of typeImagesWrite
     */
    public boolean isTypeImagesWrite() {
        return typeImagesWrite;
    }

    /**
     * Set typeImagesWrite.
     *
     * @param typeImagesWrite
     *            as true or false
     */
    public void setTypeImagesWrite(boolean typeImagesWrite) {
        this.typeImagesWrite = typeImagesWrite;
    }

    /**
     * Get typeAcceptClose.
     *
     * @return value of typeAcceptClose
     */
    public boolean isTypeAcceptClose() {
        return typeAcceptClose;
    }

    /**
     * Set typeAcceptClose.
     *
     * @param typeAcceptClose
     *            as true or false
     */
    public void setTypeAcceptClose(boolean typeAcceptClose) {
        this.typeAcceptClose = typeAcceptClose;
    }

    /**
     * Get typeCloseVerify.
     *
     * @return value of typeCloseVerify
     */
    public boolean isTypeCloseVerify() {
        return typeCloseVerify;
    }

    /**
     * Set typeCloseVerify.
     *
     * @param typeCloseVerify
     *            as true or false
     */
    public void setTypeCloseVerify(boolean typeCloseVerify) {
        this.typeCloseVerify = typeCloseVerify;
    }

    /**
     * Get batchStep.
     *
     * @return value of batchStep
     */
    public boolean isBatchStep() {
        return batchStep;
    }

    /**
     * Set batchStep.
     *
     * @param batchStep
     *            true or false
     */
    public void setBatchStep(boolean batchStep) {
        this.batchStep = batchStep;
    }


    /**
     * Get condition type (XPath or Script) for conditional task.
     *
     * @return type of condition
     */
    public String getConditionType() {
        return conditionType;
    }

    /**
     * Set condition type (XPath or Script) for conditional task.
     *
     * @param conditionType
     *            as java.lang.String
     */
    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    /**
     * Get condition value for conditional task.
     *
     * @return value of condition
     */
    public String getConditionValue() {
        return conditionValue;
    }

    /**
     * Set condition value for conditional task.
     *
     * @param conditionValue
     *            as java.lang.String
     */
    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    /**
     * Get user roles as String '1, 2, 3,'.
     *
     * @return value of userRole
     */
    public String getUserRoles() {
        return userRoles;
    }

    /**
     * Set user role.
     *
     * @param userRoles
     *            as java.lang.Integer
     */
    public void setUserRoles(String userRoles) {
        this.userRoles = userRoles;
    }
}
