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

package org.kitodo.production.forms;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import org.kitodo.production.helper.tasks.EmptyTask;
import org.kitodo.production.helper.tasks.EmptyTask.Behaviour;
import org.kitodo.production.helper.tasks.TaskManager;
import org.kitodo.production.helper.tasks.TaskSitter;

@Named("TaskManagerForm")
@RequestScoped
public class TaskManagerForm {

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
     * Returns the task list held in the task manager.
     *
     * @return the task list
     */
    public List<EmptyTask> getTasks() {
        return TaskManager.getTaskList();
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
     * Provides write access to the property "task" and
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
