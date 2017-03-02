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

import java.util.ArrayList;

public class DocketData {

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
    /** A comment. */
    private String comment;
    /** The template properties. */
    private ArrayList<TemplateProperty> templateProperties;
    /** The workpiece properties. */
    private ArrayList<WorkpieceProperty> workpieceProperties;
    /** The process properties. */
    private ArrayList<ProcessProperty> processProperties;

    /** Gets the processName. */
    public String getProcessName() {
        return processName;
    }

    /** Sets the processName. */
    public void setProcessName(String processName) {
        this.processName = processName;
    }

    /** Gets the processId. */
    public String getProcessId() {
        return processId;
    }

    /** Sets the processId. */
    public void setProcessId(String processId) {
        this.processId = processId;
    }

    /** Gets the projectName. */
    public String getProjectName() {
        return projectName;
    }

    /** Sets the projectName. */
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /** Gets the rulesetName. */
    public String getRulesetName() {
        return rulesetName;
    }

    /** Sets the rulesetName. */
    public void setRulesetName(String rulesetName) {
        this.rulesetName = rulesetName;
    }

    /** Gets the creationDate. */
    public String getCreationDate() {
        return creationDate;
    }

    /** Sets the creationDate. */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /** Gets the comment. */
    public String getComment() {
        return comment;
    }

    /** Sets the comment. */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /** Gets the templateProperties. */
    public ArrayList<TemplateProperty> getTemplateProperties() {
        return templateProperties;
    }

    /** Sets the templateProperties. */
    public void setTemplateProperties(ArrayList<TemplateProperty> templateProperties) {
        this.templateProperties = templateProperties;
    }

    /** Gets the workpieceProperties. */
    public ArrayList<WorkpieceProperty> getWorkpieceProperties() {
        return workpieceProperties;
    }

    /** Sets the workpieceProperties. */
    public void setWorkpieceProperties(ArrayList<WorkpieceProperty> workpieceProperties) {
        this.workpieceProperties = workpieceProperties;
    }

    /** Gets the processProperties. */
    public ArrayList<ProcessProperty> getProcessProperties() {
        return processProperties;
    }

    /** Sets the processProperties. */
    public void setProcessProperties(ArrayList<ProcessProperty> processProperties) {
        this.processProperties = processProperties;
    }
}
