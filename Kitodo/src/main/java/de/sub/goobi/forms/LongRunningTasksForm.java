/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package de.sub.goobi.forms;

import java.util.List;

import de.sub.goobi.config.ConfigMain;
import de.sub.goobi.helper.tasks.EmptyTask;
import de.sub.goobi.helper.tasks.EmptyTask.Behaviour;
import de.sub.goobi.helper.tasks.TaskManager;
import de.sub.goobi.helper.tasks.TaskSitter;

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
	 * The method addDemoTask() in executed if the user clicks the link to
	 * "add a sample task" in the task manager. This is—if for anything at
	 * all—useful for debugging or demonstration purposes only.
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
	 * global configuration file. Depending on this an option to
	 * "add a sample task" in been shown in the task manager. This is—if for
	 * anything at all—useful for debugging or demonstration purposes only.
	 * Defaults to false.
	 * 
	 * @return whether <code>taskManager.showSampleTask</code> is set true in
	 *         the configuration
	 */
	public boolean isDemoTasksLinkShowing() {
		return ConfigMain.getBooleanParameter("taskManager.showSampleTask", false);
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
