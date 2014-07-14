/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
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

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.tasks.AbstractTask;
import de.sub.goobi.helper.tasks.AbstractTask.Behaviour;
import de.sub.goobi.helper.tasks.LongRunningTask;
import de.sub.goobi.helper.tasks.TaskManager;

public class LongRunningTasksForm {
	private Prozess prozess;
	private LongRunningTask task;
	public List<AbstractTask> getTasks() {
		return TaskManager.getTaskList();
	}

	public void addNewMasterTask() {
		Prozess p = new Prozess();
		p.setTitel("hallo Titel " + System.currentTimeMillis());
		this.task = new LongRunningTask();
		this.task.initialize(p);
		TaskManager.addTask(this.task);
	}

	public void executeTask() {
		this.task.run();
	}

	public void clearFinishedTasks() {
		TaskManager.removeAllFinishedTasks();
	}

	public void clearAllTasks() {
		TaskManager.stopAndDeleteAllTasks();
	}

	public void moveTaskUp() {
		TaskManager.runEarlier(this.task);
	}

	public void moveTaskDown() {
		TaskManager.runLater(this.task);
	}

	public void cancelTask() {
		this.task.interrupt(Behaviour.PREPARE_FOR_RESTART);
	}

	public void removeTask() {
		this.task.interrupt(Behaviour.DELETE_IMMEDIATELY);
	}

	public Prozess getProzess() {
		return this.prozess;
	}

	public void setProzess(Prozess prozess) {
		this.prozess = prozess;
	}

	public LongRunningTask getTask() {
		return this.task;
	}

	public void setTask(LongRunningTask task) {
		this.task = task;
	}

	public boolean isRunning() {
		return TaskManager.isAutoRunningThreads();
	}

	public void toggleRunning() {
		boolean mode = !TaskManager.isAutoRunningThreads();
		TaskManager.setAutoRunningThreads(mode);
	}
}