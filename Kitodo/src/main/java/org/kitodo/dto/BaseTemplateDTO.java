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

public abstract class BaseTemplateDTO extends BaseDTO {

    private String title;
    private String outputName;
    private String wikiField;
    private String creationDate;
    private DocketDTO docket;
    private RulesetDTO ruleset;
    private List<TaskDTO> tasks = new ArrayList<>();

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
     * Get output name.
     *
     * @return output name as String
     */
    public String getOutputName() {
        return outputName;
    }

    /**
     * Set output name.
     *
     * @param outputName
     *            as String
     */
    public void setOutputName(String outputName) {
        this.outputName = outputName;
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
     * Get creation date.
     *
     * @return creation date as String
     */
    public String getCreationDate() {
        return creationDate;
    }

    /**
     * Set creation date.
     *
     * @param creationDate
     *            as String
     */
    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Get docket.
     *
     * @return docket as DocketDTO
     */
    public DocketDTO getDocket() {
        return docket;
    }

    /**
     * Set docket.
     *
     * @param docket
     *            as DocketDTO
     */
    public void setDocket(DocketDTO docket) {
        this.docket = docket;
    }

    /**
     * Get ruleset.
     *
     * @return ruleset as RulesetDTO
     */
    public RulesetDTO getRuleset() {
        return ruleset;
    }

    /**
     * Set ruleset.
     *
     * @param ruleset
     *            as RulesetDTO
     */
    public void setRuleset(RulesetDTO ruleset) {
        this.ruleset = ruleset;
    }

    /**
     * Get list of tasks.
     *
     * @return list of tasks as TaskDTO
     */
    public List<TaskDTO> getTasks() {
        return tasks;
    }

    /**
     * Set list of tasks.
     *
     * @param tasks
     *            list of tasks as TaskDTO
     */
    public void setTasks(List<TaskDTO> tasks) {
        this.tasks = tasks;
    }
}
