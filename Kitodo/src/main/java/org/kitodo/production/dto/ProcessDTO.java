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

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * Process DTO object.
 */
public class ProcessDTO extends BaseTemplateDTO implements ProcessInterface {

    private ProjectInterface project;
    private List<BatchDTO> batches = new ArrayList<>();
    private List<PropertyDTO> properties = new ArrayList<>();
    private UserInterface blockedUser;
    private Double progressClosed;
    private Double progressInProcessing;
    private Double progressOpen;
    private Double progressLocked;
    private String progressCombined;
    private String wikiField;
    private String processBaseUri;
    private String batchID;
    private Integer parentID;
    private boolean hasChildren;
    private Integer sortHelperArticles;
    private Integer sortHelperDocstructs;
    private Integer sortHelperImages;
    private Integer sortHelperMetadata;
    private Integer numberOfMetadata;
    private Integer numberOfImages;
    private Integer numberOfStructures;
    private String sortHelperStatus;
    private String baseType;
    private String lastEditingUser;
    private Date processingBeginLastTask;
    private Date processingEndLastTask;
    private Integer correctionCommentStatus;
    private boolean hasComments;

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
        this.project = (ProjectInterface) project;
    }

    /**
     * Get list of batches.
     *
     * @return list of batches as BatchInterface
     */
    public List<BatchDTO> getBatches() {
        return batches;
    }

    /**
     * Set list of batches.
     *
     * @param batches
     *            list of batches as BatchInterface
     */
    public void setBatches(List batches) {
        this.batches = batches;
    }

    /**
     * Get list of properties.
     *
     * @return list of properties as PropertyInterface
     */
    public List<PropertyDTO> getProperties() {
        if (Objects.isNull(this.properties)) {
            properties = new ArrayList<>();
        }
        return properties;
    }

    /**
     * Set list of properties.
     *
     * @param properties
     *            list of properties as PropertyInterface
     */
    public void setProperties(List<PropertyDTO> properties) {
        this.properties = properties;
    }

    /**
     * Get blocked user.
     *
     * @return blocked user as UserInterface
     */
    public UserInterface getBlockedUser() {
        return blockedUser;
    }

    /**
     * Set blocked user.
     *
     * @param blockedUser
     *            as UserInterface
     */
    public void setBlockedUser(UserInterface blockedUser) {
        this.blockedUser = blockedUser;
    }

    /**
     * Get progress of closed tasks.
     *
     * @return progress of closed tasks as Double
     */
    public Double getProgressClosed() {
        return progressClosed;
    }

    /**
     * Set progress of closed tasks.
     *
     * @param progressClosed
     *            as Double
     */
    public void setProgressClosed(Double progressClosed) {
        this.progressClosed = progressClosed;
    }

    /**
     * Get progress of processed tasks.
     *
     * @return progress of processed tasks as Double
     */
    public Double getProgressInProcessing() {
        return progressInProcessing;
    }

    /**
     * Set progress of processed tasks.
     *
     * @param progressInProcessing
     *            as Double
     */
    public void setProgressInProcessing(Double progressInProcessing) {
        this.progressInProcessing = progressInProcessing;
    }

    /**
     * Get progress of locked tasks.
     *
     * @return progress of locked tasks as Double
     */
    public Double getProgressLocked() {
        return progressLocked;
    }

    /**
     * Set progress of locked tasks.
     *
     * @param progressLocked
     *            as Double
     */
    public void setProgressLocked(Double progressLocked) {
        this.progressLocked = progressLocked;
    }

    /**
     * Get wiki field.
     *
     * @return wiki field as String
     */
    public String getWikiField() {
        return wikiField;
    }

    /**
     * Set wiki field.
     *
     * @param wikiField
     *            as String
     */
    public void setWikiField(String wikiField) {
        this.wikiField = wikiField;
    }

    /**
     * Get progress of open tasks.
     *
     * @return progress of open tasks as Integer
     */
    public Double getProgressOpen() {
        return progressOpen;
    }

    /**
     * Set progress of open tasks.
     *
     * @param progressOpen
     *            as Double
     */
    public void setProgressOpen(Double progressOpen) {
        this.progressOpen = progressOpen;
    }

    /**
     * Return a string representing the combined task status for a process.
     * 
     * <p>It consists of 3-digit percentage numbers (e.g. 000, 025, 100) 
     * for each task status (DONE, INWORK, OPEN, LOCKED). For example, the status
     * "000000025075" means that 25% of tasks are open, and 75% of tasks are locked.</p>
     * 
     * @return the process task progress as string
     */
    public String getProgressCombined() {
        return progressCombined;
    }

    /**
     * Sets the string representing the combined task status of a process.
     * 
     * @param progressCombined the task progress string
     */
    public void setProgressCombined(String progressCombined) {
        this.progressCombined = progressCombined;
    }

    /**
     * Get process base URI as String.
     *
     * @return process base URI as String.
     */
    public String getProcessBase() {
        return processBaseUri;
    }

    @Override
    public URI getProcessBaseUri() {
        return Objects.isNull(processBaseUri) ? null : URI.create(processBaseUri);
    }

    /**
     * Set process base URI as String.
     *
     * @param processBaseUri
     *            as String
     */
    public void setProcessBase(String processBaseUri) {
        this.processBaseUri = processBaseUri;
    }

    @Override
    public void setProcessBaseUri(URI processBaseUri) {
        this.processBaseUri = Objects.isNull(processBaseUri) ? null : processBaseUri.toString();
    }

    /**
     * Get batch id(label) as String.
     *
     * @return batch id(label) as String.
     */
    public String getBatchID() {
        return batchID;
    }

    /**
     * Set batch id(label) as String.
     *
     * @param batchID
     *            as String
     */
    public void setBatchID(String batchID) {
        this.batchID = batchID;
    }

    /**
     * Get parentID.
     *
     * @return value of parentID
     */
    public Integer getParentID() {
        return parentID;
    }

    /**
     * Set parentID.
     *
     * @param parentID as java.lang.Integer
     */
    public void setParentID(Integer parentID) {
        this.parentID = parentID;
    }

    /**
     * Get hasChildren.
     *
     * @return value of hasChildren
     */
    public boolean hasChildren() {
        return hasChildren;
    }

    /**
     * Set hasChildren.
     *
     * @param hasChildren as boolean
     */
    public void setHasChildren(boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    /**
     * Get sort helper for articles.
     *
     * @return sort helper for articles as Integer
     */
    public Integer getSortHelperArticles() {
        return this.sortHelperArticles;
    }

    /**
     * Set sort helper for articles.
     *
     * @param sortHelperArticles
     *            as Integer
     */
    public void setSortHelperArticles(Integer sortHelperArticles) {
        this.sortHelperArticles = sortHelperArticles;
    }

    /**
     * Get sort helper for document structure.
     *
     * @return sort helper for document structure as Integer
     */
    public Integer getSortHelperDocstructs() {
        return this.sortHelperDocstructs;
    }

    /**
     * Set sort helper for document structure.
     *
     * @param sortHelperDocstructs
     *            as Integer
     */
    public void setSortHelperDocstructs(Integer sortHelperDocstructs) {
        this.sortHelperDocstructs = sortHelperDocstructs;
    }

    /**
     * Get sort helper for task status.
     *
     * @return sort helper for task status as String
     */
    public String getSortHelperStatus() {
        return this.sortHelperStatus;
    }

    /**
     * Set sort helper for task status.
     *
     * @param sortHelperStatus
     *            as String
     */
    public void setSortHelperStatus(String sortHelperStatus) {
        this.sortHelperStatus = sortHelperStatus;
    }

    /**
     * Get sort helper for images.
     *
     * @return sort helper for images as Integer
     */
    public Integer getSortHelperImages() {
        return this.sortHelperImages;
    }

    /**
     * Set sort helper for images.
     *
     * @param sortHelperImages
     *            as Integer
     */
    public void setSortHelperImages(Integer sortHelperImages) {
        this.sortHelperImages = sortHelperImages;
    }

    /**
     * Get sort helper for metadata.
     *
     * @return sort helper for metadata as Integer
     */
    public Integer getSortHelperMetadata() {
        return this.sortHelperMetadata;
    }

    /**
     * Set sort helper for metadata.
     *
     * @param sortHelperMetadata
     *            as Integer
     */
    public void setSortHelperMetadata(Integer sortHelperMetadata) {
        this.sortHelperMetadata = sortHelperMetadata;
    }

    /**
     * Get baseType.
     *
     * @return value of baseType
     */
    public String getBaseType() {
        return baseType;
    }

    /**
     * Set baseType.
     *
     * @param baseType as java.lang.String
     */
    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }

    /**
     * Get numberOfMetadata .
     *
     * @return value of numberOfMetadata
     */
    public Integer getNumberOfMetadata() {
        return numberOfMetadata;
    }

    /**
     * Set numberOfMetadata.
     *
     * @param numberOfMetadata as Integer
     */
    public void setNumberOfMetadata(Integer numberOfMetadata) {
        this.numberOfMetadata = numberOfMetadata;
    }

    /**
     * Get numberOfImages.
     *
     * @return value of numberOfImages
     */
    public Integer getNumberOfImages() {
        return numberOfImages;
    }

    /**
     * Set numberOfImages.
     *
     * @param numberOfImages as Integer
     */
    public void setNumberOfImages(Integer numberOfImages) {
        this.numberOfImages = numberOfImages;
    }

    /**
     * Get numberOfStructures.
     *
     * @return value of numberOfStructures
     */
    public Integer getNumberOfStructures() {
        return numberOfStructures;
    }

    /**
     * Set numberOfStructures.
     *
     * @param numberOfStructures as Integer
     */
    public void setNumberOfStructures(Integer numberOfStructures) {
        this.numberOfStructures = numberOfStructures;
    }

    /**
     * Returns the user name of the user that was last handling a task of this process.
     *
     * @return name of last user handling task
     */
    public String getLastEditingUser() {
        return this.lastEditingUser;
    }

    /**
     * Sets the user name of the user that was last handling a task of this process.
     *
     * @param lastEditingUser
     *            as String
     */
    public void setLastEditingUser(String lastEditingUser) {
        this.lastEditingUser = lastEditingUser;
    }

    /**
     * Get date of begin of last processing task.
     *
     * @return date of begin of last processing task
     */
    public Date getProcessingBeginLastTask() {
        return this.processingBeginLastTask;
    }

    /**
     * Set date of begin of last processing task.
     *
     * @param processingBeginLastTask
     *            as Date
     */
    public void setProcessingBeginLastTask(Date processingBeginLastTask) {
        this.processingBeginLastTask = processingBeginLastTask;
    }

    /**
     * Get date of end of last processing task.
     *
     * @return date of end of last processing task
     */
    public Date getProcessingEndLastTask() {
        return this.processingEndLastTask;
    }

    /**
     * Set date of end of last processing task.
     *
     * @param processingEndLastTask
     *            as Date
     */
    public void setProcessingEndLastTask(Date processingEndLastTask) {
        this.processingEndLastTask = processingEndLastTask;
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

    /**
     * Get hasComments. Value is true when the process has any comments.
     *
     * @return value of hasComments
     */
    public boolean hasComments() {
        return hasComments;
    }

    /**
     * Set hasComments. Value should be set to true when the process has any comments.
     *
     * @param hasComments as boolean
     */
    public void setHasComments(boolean hasComments) {
        this.hasComments = hasComments;
    }
}
