/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package de.sub.goobi.forms;

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
		task = new LongRunningTask();
		task.initialize(p);
		LongRunningTaskManager.getInstance().addTask(task);
	}

	/**
	 * Thread entweder starten oder restarten ================================================================
	 */
	public void executeTask() {
		if (task.getStatusProgress() == 0)
			LongRunningTaskManager.getInstance().executeTask(task);
		else {
			/* Thread lief schon und wurde abgebrochen */
			try {
				LongRunningTask lrt = task.getClass().newInstance();
				lrt.initialize(task.getProzess());
				LongRunningTaskManager.getInstance().replaceTask(task, lrt);
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
		LongRunningTaskManager.getInstance().moveTaskUp(task);
	}

	public void moveTaskDown() {
		LongRunningTaskManager.getInstance().moveTaskDown(task);
	}

	public void cancelTask() {
		LongRunningTaskManager.getInstance().cancelTask(task);
	}

	public void removeTask() {
		LongRunningTaskManager.getInstance().removeTask(task);
	}

	public Prozess getProzess() {
		return prozess;
	}

	public void setProzess(Prozess prozess) {
		this.prozess = prozess;
	}

	public LongRunningTask getTask() {
		return task;
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
