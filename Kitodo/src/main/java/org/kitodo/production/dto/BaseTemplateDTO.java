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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.data.database.beans.BaseBean;

public abstract class BaseTemplateDTO extends BaseDTO {

    private String title;
    protected String creationDate;
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
     * Get creation date.
     *
     * @return creation date as String
     */
    public String getCreationTime() {
        return creationDate;
    }

    /**
     * Set creation date.
     *
     * @param creationDate
     *            as String
     */
    public void setCreationTime(String creationDate) {
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

    /**
     * Get creation date.
     *
     * @return creation date as Date
     */
    public Date getCreationDate() {
        try {
            return StringUtils.isNotBlank(this.creationDate)
                    ? new SimpleDateFormat(BaseBean.DATE_FORMAT).parse(this.creationDate)
                    : null;
        } catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Set creation date.
     *
     * @param creationDate
     *            as Date
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = Objects.nonNull(creationDate) ? new SimpleDateFormat(BaseBean.DATE_FORMAT).format(creationDate)
                : null;
    }
}
