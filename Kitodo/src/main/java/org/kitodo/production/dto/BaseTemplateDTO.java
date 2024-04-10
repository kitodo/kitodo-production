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

import java.util.ArrayList;
import java.util.List;

import org.kitodo.data.interfaces.DocketInterface;
import org.kitodo.data.interfaces.RulesetInterface;
import org.kitodo.data.interfaces.TaskInterface;

public abstract class BaseTemplateDTO extends BaseDTO {

    private String title;
    private String creationDate;
    private DocketInterface docket;
    private RulesetInterface ruleset;
    private List<TaskInterface> tasks = new ArrayList<>();

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
     * @return docket as DocketInterface
     */
    public DocketInterface getDocket() {
        return docket;
    }

    /**
     * Set docket.
     *
     * @param docket
     *            as DocketInterface
     */
    public void setDocket(DocketInterface docket) {
        this.docket = docket;
    }

    /**
     * Get ruleset.
     *
     * @return ruleset as RulesetInterface
     */
    public RulesetInterface getRuleset() {
        return ruleset;
    }

    /**
     * Set ruleset.
     *
     * @param ruleset
     *            as RulesetInterface
     */
    public void setRuleset(RulesetInterface ruleset) {
        this.ruleset = ruleset;
    }

    /**
     * Get list of tasks.
     *
     * @return list of tasks as TaskInterface
     */
    public List<TaskInterface> getTasks() {
        return tasks;
    }

    /**
     * Set list of tasks.
     *
     * @param tasks
     *            list of tasks as TaskInterface
     */
    public void setTasks(List<TaskInterface> tasks) {
        this.tasks = tasks;
    }
}
