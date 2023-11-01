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

package org.kitodo.api.docket;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DocketData {

    /** The metadata file. */
    private URI metadataFile;

    /** The docket data of the parent. */
    private DocketData parent;

    /** The name of the process. */
    private String processName;

    /** The id of the process. */
    private String processId;

    /** The name of the project. */
    private String projectName;

    /** The name of the used ruleset. */
    private String rulesetName;

    /** The creation Date of the process. */
    private String creationDate;

    /** The comments. */
    private List<String> comments = new ArrayList<>();

    /** The template properties. */
    private List<Property> templateProperties;

    /** The workpiece properties. */
    private List<Property> workpieceProperties;

    /** The process properties. */
    private List<Property> processProperties;

    /**
     * Gets the metadataFile.
     * 
     * @return The metadataFile.
     */
    public URI metadataFile() {
        return metadataFile;
    }

    /**
     * Sets the metadataFile.
     * 
     * @param metadataFile
     *            The metadata file.
     */
    public void setMetadataFile(URI metadataFile) {
        this.metadataFile = metadataFile;
    }

    /**
     * Gets the processName.
     * 
     * @return The processName.
     */
    public DocketData getParent() {
        return parent;
    }

    /**
     * Sets the parent.
     * 
     * @param parent
     *            The docket data of the parent.
     */
    public void setParent(DocketData parent) {
        this.parent = parent;
    }

    /**
     * Gets the parent.
     * 
     * @return The parent.
     */
    public String getProcessName() {
        return processName;
    }

    /**
     * Sets the processName.
     * 
     * @param processName
     *            The query to execute.
     */
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    /**
     * Gets the processId.
     * 
     * @return The processId.
     */
    public String getProcessId() {
        return processId;
    }

    /**
     * Sets the processId.
     * 
     * @param processId
     *            The processId.
     */
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    /**
     * Gets the projectName.
     * 
     * @return The projectName.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Sets the projectName.
     * 
     * @param projectName
     *            The projectName.
     */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Gets the rulesetName.
     * 
     * @return The rulesetName.
     */
    public String getRulesetName() {
        return rulesetName;
    }

    /**
     * Sets the rulesetName.
     * 
     * @param rulesetName
     *            The rulesetName.
     */
    public void setRulesetName(String rulesetName) {
        this.rulesetName = rulesetName;
    }

    /**
     * Gets the creationDate.
     * 
     * @return The creationDate.
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creationDate.
     * 
     * @param creationDate
     *            The creationDate.
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Gets the comments.
     * 
     * @return The comments.
     */
    public List<String> getComments() {
        return comments;
    }

    /**
     * Sets the comments.
     * 
     * @param comments
     *            The comments.
     */
    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    /**
     * Gets the templateProperties.
     * 
     * @return The templateProperties.
     */
    public List<Property> getTemplateProperties() {
        return templateProperties;
    }

    /**
     * Sets the templateProperties.
     * 
     * @param templateProperties
     *            The templateProperties.
     */
    public void setTemplateProperties(List<Property> templateProperties) {
        this.templateProperties = templateProperties;
    }

    /**
     * Gets the workpieceProperties.
     * 
     * @return The workpieceProperties.
     */
    public List<Property> getWorkpieceProperties() {
        if (Objects.isNull(workpieceProperties)) {
            workpieceProperties = new ArrayList<>();
        }
        return workpieceProperties;
    }

    /**
     * Sets the workpieceProperties.
     * 
     * @param workpieceProperties
     *            The workpieceProperties.
     */
    public void setWorkpieceProperties(List<Property> workpieceProperties) {
        this.workpieceProperties = workpieceProperties;
    }

    /**
     * Gets the processProperties.
     * 
     * @return The processProperties.
     */
    public List<Property> getProcessProperties() {
        if (Objects.isNull(processProperties)) {
            processProperties = new ArrayList<>();
        }
        return processProperties;
    }

    /**
     * Sets the processProperties.
     * 
     * @param processProperties
     *            The processProperties.
     */
    public void setProcessProperties(List<Property> processProperties) {
        this.processProperties = processProperties;
    }
}
