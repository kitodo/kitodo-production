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

import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.PluginLoader;
import org.goobi.production.plugin.interfaces.IStepPlugin;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
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
        if (logger.isDebugEnabled()) {
            logger.debug("task is automatic: " + automatic);
        }
        List<String> scriptPaths = taskService.getAllScriptPaths(this.task);
        if (logger.isDebugEnabled()) {
            logger.debug("found " + scriptPaths.size() + " scripts");
        }
        if (scriptPaths.size() > 0) {
            try {
                this.taskService.executeAllScripts(this.task, automatic);
            } catch (CustomResponseException e) {
                logger.error("Index Error occured", e);
            } catch (IOException e) {
                logger.error("IOException occured", e);
            } catch (DAOException e) {
                logger.error("Database Error occured", e);
            }
        } else if (this.task.isTypeExportDMS()) {
            try {
                serviceManager.getTaskService().executeDmsExport(this.task, false);
            } catch (CustomResponseException | IOException | DAOException e) {
                logger.error("IO Exception occured", e);
            } catch (ConfigurationException e) {
                logger.error("Configuration could not be read", e);
            }
        } else if ((this.task.getStepPlugin() != null) && (this.task.getStepPlugin().length() > 0)) {
            IStepPlugin isp = (IStepPlugin) PluginLoader.getPluginByTitle(PluginType.Step, task.getStepPlugin());
            isp.initialize(task, "");
            if (isp.execute()) {
                try {
                    taskService.close(task, false);
                } catch (CustomResponseException e) {
                    logger.error("Index Error occured", e);
                } catch (IOException e) {
                    logger.error("IOException occured", e);
                } catch (DAOException e) {
                    logger.error("Database Error occured", e);
                }
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
        return Helper.getTranslation("TaskScriptThread");
    }
}
