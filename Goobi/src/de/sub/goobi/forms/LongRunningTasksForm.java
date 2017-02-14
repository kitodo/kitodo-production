package de.sub.goobi.forms;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
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
import java.util.LinkedList;

import org.apache.log4j.Logger;

import de.sub.goobi.beans.Prozess;
import de.sub.goobi.helper.tasks.LongRunningTask;
import de.sub.goobi.helper.tasks.LongRunningTaskManager;

public class LongRunningTasksForm {
	private Prozess prozess;
	private LongRunningTask task;
	private static final Logger logger = Logger.getLogger(LongRunningTask.class);

	public LinkedList<LongRunningTask> getTasks() {
		return LongRunningTaskManager.getInstance().getTasks();
	}


	public void addNewMasterTask() {
		Prozess p = new Prozess();
		p.setTitel("hallo Titel " + System.currentTimeMillis());
		this.task = new LongRunningTask();
		this.task.initialize(p);
		LongRunningTaskManager.getInstance().addTask(this.task);
	}

	/**
	 * Thread entweder starten oder restarten ================================================================
	 */
	public void executeTask() {
		if (this.task.getStatusProgress() == 0) {
			LongRunningTaskManager.getInstance().executeTask(this.task);
		} else {
			/* Thread lief schon und wurde abgebrochen */
			try {
				LongRunningTask lrt = this.task.getClass().newInstance();
				lrt.initialize(this.task.getProzess());
				LongRunningTaskManager.getInstance().replaceTask(this.task, lrt);
				LongRunningTaskManager.getInstance().executeTask(lrt);
			} catch (InstantiationException e) {
				logger.error(e);
			} catch (IllegalAccessException e) {
				logger.error(e);
			}
		}
	}

	public void clearFinishedTasks() {
		LongRunningTaskManager.getInstance().clearFinishedTasks();
	}

	public void clearAllTasks() {
		LongRunningTaskManager.getInstance().clearAllTasks();
	}

	public void moveTaskUp() {
		LongRunningTaskManager.getInstance().moveTaskUp(this.task);
	}

	public void moveTaskDown() {
		LongRunningTaskManager.getInstance().moveTaskDown(this.task);
	}

	public void cancelTask() {
		LongRunningTaskManager.getInstance().cancelTask(this.task);
	}

	public void removeTask() {
		LongRunningTaskManager.getInstance().removeTask(this.task);
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
		return LongRunningTaskManager.getInstance().isRunning();
	}

	public void toggleRunning() {
		LongRunningTaskManager.getInstance().setRunning(!LongRunningTaskManager.getInstance().isRunning());
	}
}
