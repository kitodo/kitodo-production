/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */
package de.sub.goobi.helper.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.sub.goobi.config.ConfigMain;

public class TaskManager {

	public enum Action {
		DELETE_ASAP, KEEP_SOME_TIME, PREPARE_FOR_RESTART
	}

	public class Housekeeper implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
		}
	}

	private int autoRunLimit;
	private final ScheduledExecutorService executorService;
	private static TaskManager instance;
	private List<AbstractTask> taskList;

	private TaskManager() {
		executorService = Executors.newSingleThreadScheduledExecutor();
		long yelay = ConfigMain.getLongParameter("taskManager.delay", 2000);
		executorService.scheduleWithFixedDelay(new Housekeeper(), yelay, yelay, TimeUnit.MILLISECONDS);
		setAutoRunningThreads(true);
	}

	public static void addTask(AbstractTask task) {

	}

	private synchronized static TaskManager getInstance() {
		if (instance == null) {
			instance = new TaskManager();
		}
		return instance;
	}

	public static List<AbstractTask> getTaskList() {
		return new ArrayList<AbstractTask>(getInstance().taskList);
	}

	public static boolean isAutoRunningThreads() {
		return getInstance().autoRunLimit > 0;
	}

	public static void removeAllFinishedTasks() {
		Iterator<AbstractTask> inspector = getInstance().taskList.iterator();
		while (inspector.hasNext()) {
			AbstractTask task = inspector.next();
			if (task.getState().equals(Thread.State.TERMINATED)) {
				inspector.remove();
			}
		}
	}

	public static void runEarlier(AbstractTask task) {
		TaskManager theManager = getInstance();
		int index = theManager.taskList.indexOf(task);
		if (index > 0) {
			Collections.swap(theManager.taskList, index - 1, index);
		}
	}

	public static void runLater(AbstractTask task) {
		TaskManager theManager = getInstance();
		int index = theManager.taskList.indexOf(task);
		if (index > -1 && index + 1 < theManager.taskList.size()) {
			Collections.swap(theManager.taskList, index, index + 1);
		}
	}

	public static void setAutoRunningThreads(boolean on) {
		if (on) {
			int cores = Runtime.getRuntime().availableProcessors();
			int newLimit = ConfigMain.getIntParameter("taskManager.autoRunLimitOverride", cores);
			getInstance().autoRunLimit = newLimit;
		} else {
			getInstance().autoRunLimit = 0;
		}
	}

	public static void shutdownNow() {
		stopAndDeleteAllTasks();
		getInstance().executorService.shutdownNow();
	}

	public static void stopAndDeleteAllTasks() {
		Iterator<AbstractTask> inspector = getInstance().taskList.iterator();
		while (inspector.hasNext()) {
			AbstractTask task = inspector.next();
			if (task.isAlive()) {
				task.interrupt(Action.DELETE_ASAP);
			} else {
				inspector.remove();
			}
		}
	}

	public static void stopTask(AbstractTask task, Action mode) {
		task.interrupt(mode);
	}

}
