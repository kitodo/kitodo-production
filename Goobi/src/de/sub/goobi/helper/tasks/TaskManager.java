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
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.sub.goobi.config.ConfigMain;

/**
 * The class TaskManager serves to handle the execution of threads. It can be
 * user controlled by the “Long running task manager”, backed by
 * {@link de.sub.goobi.forms.LongRunningTaskForm}.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class TaskManager {

	/**
	 * The enum Actions lists the available instructions to the housekeeper what
	 * to do with a terminated thread. These are:
	 * 
	 * <dl>
	 * <dt><code>DELETE_IMMEDIATELY</code></dt>
	 * <dd>The thread shall be disposed of as soon as is has gracefully stopped.
	 * </dd>
	 * <dt><code>KEEP_FOR_A_WHILE</code></dt>
	 * <dd>The default behaviour: A thread that terminated either normally or
	 * abnormally is kept around in memory for a while and then removed
	 * automatically. Numeric and temporary limits can be configured.</dd>
	 * <dt><code>PREPARE_FOR_RESTART</code></dt>
	 * <dd>If the thread was interrupted by a user, replace it by a new one,
	 * passing in the state of the old one to be able to coninue work.</dd>
	 * </dl>
	 * 
	 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
	 */
	public enum Actions {
		DELETE_IMMEDIATELY, KEEP_FOR_A_WHILE, PREPARE_FOR_RESTART
	}

	/**
	 * The class Housekeeper is a Runnable which implements the removing of old
	 * threads and starting of new ones.
	 * 
	 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
	 */
	public class Housekeeper implements Runnable {
		@Override
		public void run() {
			// TODO Auto-generated method stub
		}
	}

	/**
	 * The field autoRunLimit holds the number of threads which at most are
	 * allowed to be started automatically. It is by default initialised by the
	 * number of available processors of the runtime and set to 0 while the
	 * feature is disabled.
	 */
	private int autoRunLimit;

	/**
	 * The field executorService holds a scheduled executor to repeatedly run
	 * the houskeeping task which will remove old threads and start new ones as
	 * configured to do.
	 */
	private final ScheduledExecutorService executorService;

	/**
	 * The field instance holds the singelton instance of the TaskManager. Tough
	 * the method signatures of TaskManager are static, it is implemented as
	 * singleton internally. All accesses to the instance must be done by
	 * calling the synchronized function singleton() or concurrency issues may
	 * arise.
	 */
	private static TaskManager singletonInstance;

	/**
	 * The field taskList holds the list of threads managed by the task manager.
	 */
	private final LinkedList<AbstractTask> taskList = new LinkedList<AbstractTask>();

	/**
	 * TaskManager is a singleton so its constructor is private. It will be
	 * called once and just once by the synchronized function singleton() and
	 * set up a housekeeping thread.
	 */
	private TaskManager() {
		setAutoRunningThreads(true);
		executorService = Executors.newSingleThreadScheduledExecutor();
		long yelay = ConfigMain.getLongParameter("taskManager.delay", 2000);
		executorService.scheduleWithFixedDelay(new Housekeeper(), yelay, yelay, TimeUnit.MILLISECONDS);
	}

	/**
	 * The function addTask() adds a task thread to the task list.
	 * 
	 * @param task
	 *            task to add
	 */
	public static void addTask(AbstractTask task) {
		singleton().taskList.addLast(task);
	}

	/**
	 * The function getTaskList() returns a copy of the task list usable for
	 * displaying. The result object cannot be used to modify the list. Use the
	 * appropriate functions for this.
	 * 
	 * @return a copy of the task list
	 */
	public static List<AbstractTask> getTaskList() {
		return new ArrayList<AbstractTask>(singleton().taskList);
	}

	/**
	 * The function isAutoRunningThreads() returns whether the TaskManager’s
	 * autorun mode is on or not.
	 * 
	 * @return whether the TaskManager is auto-running threds or not
	 */
	public static boolean isAutoRunningThreads() {
		return singleton().autoRunLimit > 0;
	}

	/**
	 * The function removeAllFinishedTasks() can be called to remove all
	 * terminated threads from the list.
	 */
	public static void removeAllFinishedTasks() {
		boolean redo;
		do {
			redo = false;
			try {
				Iterator<AbstractTask> inspector = singleton().taskList.iterator();
				while (inspector.hasNext()) {
					AbstractTask task = inspector.next();
					if (task.getState().equals(Thread.State.TERMINATED)) {
						inspector.remove();
					}
				}
			} catch (ConcurrentModificationException listModifiedByAnotherThreadWhileIterating) {
				redo = true;
			}
		} while (redo);
	}

	/**
	 * The function runEarlier() can be called to move a task by one forwards on
	 * the queue
	 * 
	 * @param task
	 *            task to move forwards
	 */
	public static void runEarlier(AbstractTask task) {
		TaskManager theManager = singleton();
		int index = theManager.taskList.indexOf(task);
		if (index > 0) {
			Collections.swap(theManager.taskList, index - 1, index);
		}
	}

	/**
	 * The function runLater() can be called to move a task by one backwards on
	 * the queue
	 * 
	 * @param task
	 *            task to move backwards
	 */
	public static void runLater(AbstractTask task) {
		TaskManager theManager = singleton();
		int index = theManager.taskList.indexOf(task);
		if (index > -1 && index + 1 < theManager.taskList.size()) {
			Collections.swap(theManager.taskList, index, index + 1);
		}
	}

	/**
	 * The function setAutoRunningThreads() turns the feature to auto-run tasks
	 * on or off
	 * 
	 * @param on
	 *            whether the TaskManager shall autorun threads
	 */
	public static void setAutoRunningThreads(boolean on) {
		if (on) {
			int cores = Runtime.getRuntime().availableProcessors();
			int newLimit = ConfigMain.getIntParameter("taskManager.autoRunLimitOverride", cores);
			singleton().autoRunLimit = newLimit;
		} else {
			singleton().autoRunLimit = 0;
		}
	}

	/**
	 * The synchronized function singleton() must be used to have singleton
	 * access to the TaskManager instance.
	 * 
	 * @return the singleton TaskManager instance
	 */
	private synchronized static TaskManager singleton() {
		if (singletonInstance == null) {
			singletonInstance = new TaskManager();
		}
		return singletonInstance;
	}

	/**
	 * The function shutdownNow() will be called by the
	 * TaskManagerShutdownListener to gracefully exit the task manager as well
	 * as its managed threads during container shutdown.
	 */
	static void shutdownNow() {
		stopAndDeleteAllTasks();
		singleton().executorService.shutdownNow();
	}

	/**
	 * The function stopAndDeleteAllTasks() can be called to both request
	 * interrupt and immediate deletion for all threads that are alive and at
	 * the same time remove all threads that aren’t alive anyhow.
	 */
	public static void stopAndDeleteAllTasks() {
		boolean redo;
		do {
			redo = false;
			try {
				Iterator<AbstractTask> inspector = singleton().taskList.iterator();
				while (inspector.hasNext()) {
					AbstractTask task = inspector.next();
					if (task.isAlive()) {
						task.interrupt(Actions.DELETE_IMMEDIATELY);
					} else {
						inspector.remove();
					}
				}
			} catch (ConcurrentModificationException listModifiedByAnotherThreadWhileIterating) {
				redo = true;
			}
		} while (redo);
	}
}
