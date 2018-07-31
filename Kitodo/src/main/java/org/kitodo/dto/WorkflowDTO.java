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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.kitodo.services.ServiceManager;

public class WorkflowDTO extends BaseDTO {

    private String title;
    private String fileName;
    private boolean ready;
    private boolean active;
    private List<TaskDTO> tasks = new ArrayList<>();

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
     * Get file name.
     *
     * @return value of fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Set file name.
     *
     * @param fileName
     *            as String
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Get ready.
     *
     * @return value of ready
     */
    public boolean isReady() {
        return ready;
    }

    /**
     * Set ready.
     *
     * @param ready
     *            as boolean
     */
    public void setReady(boolean ready) {
        this.ready = ready;
    }

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
     * Get diagram image.
     *
     * @return value of diagramImage
     */
    public InputStream getDiagramImage() {
        return new ServiceManager().getWorkflowService().getTasksDiagram(this.getFileName());
    }
}
