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


public class TemplateDTO extends BaseTemplateDTO {

    private boolean active;
    private WorkflowDTO workflow;
    private boolean canBeUsedForProcess;
    private List<ProjectDTO> projects = new ArrayList<>();

    /**
     * Get active.
     *
     * @return value of active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set active.
     *
     * @param active
     *            as boolean
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Set workflow.
     *
     * @param workflow as org.kitodo.production.dto.WorkflowDTO
     */
    public void setWorkflow(WorkflowDTO workflow) {
        this.workflow = workflow;
    }

    /**
     * Get workflow.
     *
     * @return value of workflow
     */
    public WorkflowDTO getWorkflow() {
        return workflow;
    }

    /**
     * Get information if template doesn't contain unreachable tasks and is active.
     *
     * @return true or false
     */
    public boolean isCanBeUsedForProcess() {
        return canBeUsedForProcess;
    }

    /**
     * Set information if template doesn't contain unreachable tasks and is active.
     *
     * @param canBeUsedForProcess
     *            as boolean
     */
    public void setCanBeUsedForProcess(boolean canBeUsedForProcess) {
        this.canBeUsedForProcess = canBeUsedForProcess;
    }

    /**
     * Get projects.
     *
     * @return value of projects
     */
    public List<ProjectDTO> getProjects() {
        return projects;
    }

    /**
     * Set projects.
     *
     * @param projects
     *            as List of ProjectDTO
     */
    public void setProjects(List<ProjectDTO> projects) {
        this.projects = projects;
    }
}
