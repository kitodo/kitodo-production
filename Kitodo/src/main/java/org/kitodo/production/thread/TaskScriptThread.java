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

package org.kitodo.production.thread;

import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.tasks.EmptyTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.TaskService;

public class TaskScriptThread extends EmptyTask {

    private final Task task;
    private static final Logger logger = LogManager.getLogger(TaskScriptThread.class);
    private ServiceManager serviceManager = new ServiceManager();
    private TaskService taskService = serviceManager.getTaskService();

    public TaskScriptThread(Task task) {
        super(task.toString());
        this.task = task;
    }

    /**
     * The clone constructor creates a new instance of this object. This is
     * necessary for Threads that have terminated in order to render to run them
     * again possible.
     *
     * @param origin
     *            copy master to create a clone of
     */
    private TaskScriptThread(TaskScriptThread origin) {
        super(origin);
        this.task = origin.task;
    }

    @Override
    public void run() {
        boolean automatic = this.task.isTypeAutomatic();
        logger.debug("task is automatic: {}", automatic);
        String scriptPath = taskService.getScriptPath(this.task);
        if (!scriptPath.equals("")) {
            try {
                this.taskService.executeScript(this.task, automatic);
            } catch (DataException e) {
                logger.error("Data Error occurred", e);
            }
        } else if (this.task.isTypeExportDMS()) {
            try {
                serviceManager.getTaskService().executeDmsExport(this.task);
            } catch (DataException e) {
                logger.error("Data Exception occurred", e);
            }
        }
    }

    /**
     * Calls the clone constructor to create a not yet executed instance of this
     * thread object. This is necessary for threads that have terminated in
     * order to render possible to restart them.
     *
     * @return a not-yet-executed replacement of this thread
     * @see de.sub.goobi.helper.tasks.EmptyTask#replace()
     */
    @Override
    public TaskScriptThread replace() {
        return new TaskScriptThread(this);
    }

    /**
     * Returns the display name of the task to show to the user.
     *
     * @see de.sub.goobi.helper.tasks.INameableTask#getDisplayName()
     */
    @Override
    public String getDisplayName() {
        return Helper.getTranslation("automaticTask");
    }
}
