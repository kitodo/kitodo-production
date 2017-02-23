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

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getRulesetName() {
        return rulesetName;
    }

    public void setRulesetName(String rulesetName) {
        this.rulesetName = rulesetName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public ArrayList<TemplateProperty> getTemplateProperties() {
        return templateProperties;
    }

    public void setTemplateProperties(ArrayList<TemplateProperty> templateProperties) {
        this.templateProperties = templateProperties;
    }

    public ArrayList<WorkpieceProperty> getWorkpieceProperties() {
        return workpieceProperties;
    }

    public void setWorkpieceProperties(ArrayList<WorkpieceProperty> workpieceProperties) {
        this.workpieceProperties = workpieceProperties;
    }

    public ArrayList<ProcessProperty> getProcessProperties() {
        return processProperties;
    }

    public void setProcessProperties(ArrayList<ProcessProperty> processProperties) {
        this.processProperties = processProperties;
    }
}
