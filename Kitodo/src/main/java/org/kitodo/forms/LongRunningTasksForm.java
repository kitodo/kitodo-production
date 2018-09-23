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

package org.kitodo.forms;

import java.util.List;

import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.helper.tasks.EmptyTask;
import org.kitodo.helper.tasks.EmptyTask.Behaviour;
import org.kitodo.helper.tasks.TaskManager;
import org.kitodo.helper.tasks.TaskSitter;

public class LongRunningTasksForm {
    /**
     * When adding demo tasks, the task number is incremented and passed in as
     * task detail. This is to have some task detail showing, so they can be
     * told apart from each other in the screen.
     */
    private static long demoTaskNo = 0;

    /**
     * The field task can be populated by a task object by the Tomahawk
     * updateActionListener tag which updates the value of a backing bean
     * property when an action event is fired by the parent UI component. This
     * circumvents the JSF object model which expects <em>all</em> actions on
     * list items being implemented on the list <em>elements</em>, thus
     * rendering a simpler method design passing in an object when it actually
     * is the patient acted on—not the agent acting—possible.
     */
    private EmptyTask task;

    /**
     * The method getTasks() returns the task list held in the task manager.
     *
     * @return the task list
     */
    public List<EmptyTask> getTasks() {
        return TaskManager.getTaskList();
    }

    /**
     * The method addDemoTask() in executed if the user clicks the link to "add
     * a sample task" in the task manager. This is—if for anything at all—useful
     * for debugging or demonstration purposes only.
     */
    public void addDemoTask() {
        task = new EmptyTask("#".concat(Long.toString(++demoTaskNo)));
        TaskManager.addTask(task);
    }

    public void executeTask() {
        task.start();
    }

    public void clearFinishedTasks() {
        TaskManager.removeAllFinishedTasks();
    }

    public void clearAllTasks() {
        TaskManager.stopAndDeleteAllTasks();
    }

    public void moveTaskUp() {
        TaskManager.runEarlier(task);
    }

    public void moveTaskDown() {
        TaskManager.runLater(task);
    }

    public void cancelTask() {
        TaskSitter.setAutoRunningThreads(false);
        task.interrupt(Behaviour.PREPARE_FOR_RESTART);
    }

    public void removeTask() {
        task.interrupt(Behaviour.DELETE_IMMEDIATELY);
    }

    /**
     * The function isDemoTasksLinkShowing() returns true, if the boolean
     * parameter <code>taskManager.showSampleTask</code> is set to true in the
     * global configuration file. Depending on this an option to "add a sample
     * task" in been shown in the task manager. This is—if for anything at
     * all—useful for debugging or demonstration purposes only. Defaults to
     * false.
     *
     * @return whether <code>taskManager.showSampleTask</code> is set true in
     *         the configuration
     */
    public boolean isDemoTasksLinkShowing() {
        return ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.TASK_MANAGER_SHOW_SAMPLE_TASK);
    }

    /**
     * The method setTask() provides write access to the property "task" and
     * will be called by the Tomahawk updateActionListener tag when an action
     * event is fired by the parent UI component. This can be used to pass an
     * object to a method when it actually is the patient acted on—not the agent
     * acting.
     *
     * @param task
     *            value to populate the property field
     */
    public void setTask(EmptyTask task) {
        this.task = task;
    }

    public boolean isRunning() {
        return TaskSitter.isAutoRunningThreads();
    }

    public void toggleRunning() {
        boolean mode = !TaskSitter.isAutoRunningThreads();
        TaskSitter.setAutoRunningThreads(mode);
    }
}
