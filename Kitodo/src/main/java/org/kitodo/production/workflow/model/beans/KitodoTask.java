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
    private Integer editType;
    private Integer processingStatus;
    private boolean concurrent;
    private boolean typeMetadata;
    private boolean separateStructure;
    private boolean typeAutomatic;
    private boolean typeExportDms;
    private boolean typeImagesRead;
    private boolean typeImagesWrite;
    private boolean typeAcceptClose;
    private boolean typeCloseVerify;
    private boolean batchStep;
    private boolean repeatOnCorrection;
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
        this.editType = getIntegerValue(task.getAttributeValueNs(NAMESPACE, "editType"));
        this.processingStatus = getIntegerValue(task.getAttributeValueNs(NAMESPACE, "processingStatus"));
        this.concurrent = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "concurrent"));
        this.typeMetadata = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeMetadata"));
        this.separateStructure = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "separateStructure"));
        this.typeAutomatic = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeAutomatic"));
        this.typeExportDms = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeExportDMS"));
        this.typeImagesRead = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeImagesRead"));
        this.typeImagesWrite = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeImagesWrite"));
        this.typeAcceptClose = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeAcceptClose"));
        this.typeCloseVerify = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeCloseVerify"));
        this.batchStep = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "batchStep"));
        this.repeatOnCorrection = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "repeatOnCorrection"));
        this.conditionType = task.getAttributeValueNs(NAMESPACE, "kitodoConditionType");
        this.conditionValue = task.getAttributeValueNs(NAMESPACE, "kitodoConditionValue");
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
     * Get editType.
     *
     * @return value of editType
     */
    public Integer getEditType() {
        return editType;
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
     * Get concurrent.
     *
     * @return value of concurrent
     */
    public boolean isConcurrent() {
        return concurrent;
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
     * Get separate structure.
     *
     * @return value of separateStructure
     */
    public boolean isSeparateStructure() {
        return separateStructure;
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
     * Get typeExportDms.
     *
     * @return value of typeExportDms
     */
    public boolean isTypeExportDms() {
        return typeExportDms;
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
     * Get typeImagesWrite.
     *
     * @return value of typeImagesWrite
     */
    public boolean isTypeImagesWrite() {
        return typeImagesWrite;
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
     * Get typeCloseVerify.
     *
     * @return value of typeCloseVerify
     */
    public boolean isTypeCloseVerify() {
        return typeCloseVerify;
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
     * Get information if task should be repeated on correction.
     *
     * @return value of repeatOnCorrection
     */
    public boolean isRepeatOnCorrection() {
        return repeatOnCorrection;
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
     * Get condition value for conditional task.
     *
     * @return value of condition
     */
    public String getConditionValue() {
        return conditionValue;
    }

    /**
     * Get user roles as String '1, 2, 3,'.
     *
     * @return value of userRole
     */
    public String getUserRoles() {
        return userRoles;
    }
}
