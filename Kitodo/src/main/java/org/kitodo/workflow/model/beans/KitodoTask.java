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

package org.kitodo.workflow.model.beans;

import java.util.Objects;

import org.camunda.bpm.model.bpmn.instance.Task;

public class KitodoTask {

    private String workflowId;
    private String title;
    private Integer priority;
    private Integer ordering;
    private Integer editType;
    private Boolean typeMetadata;
    private Boolean typeAutomatic;
    private Boolean typeImportFileUpload;
    private Boolean typeExportDms;
    private Boolean typeExportRussian;
    private Boolean typeImagesRead;
    private Boolean typeImagesWrite;
    private Boolean typeAcceptClose;
    private Boolean typeCloseVerify;
    private Boolean batchStep;

    static final String NAMESPACE = "http://www.kitodo.org/template";

    /**
     * Constructor.
     * 
     * @param task
     *            BPMN model task
     * @param ordering
     *            determined out of sequence flow
     */
    public KitodoTask(Task task, int ordering) {
        this.workflowId = task.getId();
        this.title = task.getName();
        this.priority = getIntegerValue(task.getAttributeValueNs(NAMESPACE, "priority"));
        this.ordering = ordering;
        this.editType = getIntegerValue(task.getAttributeValueNs(NAMESPACE, "editType"));
        this.typeMetadata = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeMetadata"));
        this.typeAutomatic = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeAutomatic"));
        this.typeImportFileUpload = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeImportFileUpload"));
        this.typeExportDms = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeExportDms"));
        this.typeExportRussian = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeExportRussian"));
        this.typeImagesRead = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeImagesRead"));
        this.typeImagesWrite = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeImagesWrite"));
        this.typeAcceptClose = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeAcceptClose"));
        this.typeCloseVerify = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "typeCloseVerify"));
        this.batchStep = getBooleanValue(task.getAttributeValueNs(NAMESPACE, "batchStep"));
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
     * Set workflow id.
     *
     * @param workflowId
     *            as String
     */
    public void setWorkflowId(String workflowId) {
        this.workflowId = workflowId;
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
     * Set title.
     *
     * @param title
     *            as String
     */
    public void setTitle(String title) {
        this.title = title;
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
     *            as Integer
     */
    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
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
     * Get typeMetadata.
     *
     * @return value of typeMetadata
     */
    public Boolean getTypeMetadata() {
        return typeMetadata;
    }

    /**
     * Set typeMetadata.
     *
     * @param typeMetadata
     *            as Boolean
     */
    public void setTypeMetadata(Boolean typeMetadata) {
        this.typeMetadata = typeMetadata;
    }

    /**
     * Get typeAutomatic.
     *
     * @return value of typeAutomatic
     */
    public Boolean getTypeAutomatic() {
        return typeAutomatic;
    }

    /**
     * Set typeAutomatic.
     *
     * @param typeAutomatic
     *            as Boolean
     */
    public void setTypeAutomatic(Boolean typeAutomatic) {
        this.typeAutomatic = typeAutomatic;
    }

    /**
     * Get typeImportFileUpload.
     *
     * @return value of typeImportFileUpload
     */
    public Boolean getTypeImportFileUpload() {
        return typeImportFileUpload;
    }

    /**
     * Set typeImportFileUpload.
     *
     * @param typeImportFileUpload
     *            as java.lang.Boolean
     */
    public void setTypeImportFileUpload(Boolean typeImportFileUpload) {
        this.typeImportFileUpload = typeImportFileUpload;
    }

    /**
     * Get typeExportDms.
     *
     * @return value of typeExportDms
     */
    public Boolean getTypeExportDms() {
        return typeExportDms;
    }

    /**
     * Set typeExportDms.
     *
     * @param typeExportDms
     *            as Boolean
     */
    public void setTypeExportDms(Boolean typeExportDms) {
        this.typeExportDms = typeExportDms;
    }

    /**
     * Get typeExportRussian.
     *
     * @return value of typeExportRussian
     */
    public Boolean getTypeExportRussian() {
        return typeExportRussian;
    }

    /**
     * Set typeExportRussian.
     *
     * @param typeExportRussian
     *            true or false
     */
    public void setTypeExportRussian(Boolean typeExportRussian) {
        this.typeExportRussian = typeExportRussian;
    }

    /**
     * Get typeImagesRead.
     *
     * @return value of typeImagesRead
     */
    public Boolean getTypeImagesRead() {
        return typeImagesRead;
    }

    /**
     * Set typeImagesRead.
     *
     * @param typeImagesRead
     *            true or false
     */
    public void setTypeImagesRead(Boolean typeImagesRead) {
        this.typeImagesRead = typeImagesRead;
    }

    /**
     * Get typeImagesWrite.
     *
     * @return value of typeImagesWrite
     */
    public Boolean getTypeImagesWrite() {
        return typeImagesWrite;
    }

    /**
     * Set typeImagesWrite.
     *
     * @param typeImagesWrite
     *            true or false
     */
    public void setTypeImagesWrite(Boolean typeImagesWrite) {
        this.typeImagesWrite = typeImagesWrite;
    }

    /**
     * Get typeAcceptClose.
     *
     * @return value of typeAcceptClose
     */
    public Boolean getTypeAcceptClose() {
        return typeAcceptClose;
    }

    /**
     * Set typeAcceptClose.
     *
     * @param typeAcceptClose
     *            true or false
     */
    public void setTypeAcceptClose(Boolean typeAcceptClose) {
        this.typeAcceptClose = typeAcceptClose;
    }

    /**
     * Get typeCloseVerify.
     *
     * @return value of typeCloseVerify
     */
    public Boolean getTypeCloseVerify() {
        return typeCloseVerify;
    }

    /**
     * Set typeCloseVerify.
     *
     * @param typeCloseVerify
     *            true or false
     */
    public void setTypeCloseVerify(Boolean typeCloseVerify) {
        this.typeCloseVerify = typeCloseVerify;
    }

    /**
     * Get batchStep.
     *
     * @return value of batchStep
     */
    public Boolean getBatchStep() {
        return batchStep;
    }

    /**
     * Set batchStep.
     *
     * @param batchStep
     *            true or false
     */
    public void setBatchStep(Boolean batchStep) {
        this.batchStep = batchStep;
    }
}
