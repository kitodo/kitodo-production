package de.sub.goobi.helper.tasks;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.joda.time.Duration;

import de.sub.goobi.config.ConfigMain;

/**
 * The class Housekeeper is a Runnable which implements the removing of old
 * threads and starting of new ones.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class TaskManagerHousekeeper implements Runnable, ServletContextListener {
	private static final Logger logger = Logger.getLogger(TaskManagerHousekeeper.class);

	private static final int KEEP_FAILED = 10;
	private static final long KEEP_FAILED_MINS = 250;
	private static final int KEEP_SUCCESSFUL = 3;
	private static final long KEEP_SUCCESSFUL_MINS = 20;

	/**
	 * The field autoRunLimit holds the number of threads which at most are
	 * allowed to be started automatically. It is by default initialised by the
	 * number of available processors of the runtime and set to 0 while the
	 * feature is disabled.
	 */
	private static int autoRunLimit;

	/**
	 * When the servlet is unloaded, i.e. on container shutdown, the TaskManager
	 * shall be shutd down gracefully.
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		TaskManager.shutdownNow();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
	}

	static int getAutoRunLimit() {
		return autoRunLimit;
	}

	/**
	 * The function newLegacyTask() will clone a LongRunningTask implementation
	 * for restart
	 * 
	 * @param legacyTask
	 *            a LongRunningTask to clone
	 * @return the clone of the LongRunningTask
	 */
	private AbstractTask newLegacyTask(LongRunningTask legacyTask) {
		LongRunningTask lrt = null;
		try {
			lrt = legacyTask.getClass().newInstance();
			lrt.initialize(legacyTask.getProzess());
		} catch (InstantiationException e) {
			logger.error(e);
		} catch (IllegalAccessException e) {
			logger.error(e);
		}
		return lrt;
	}

	/**
	 * The function run() examines the task list, deletes threads that have
	 * died, replaces threads that are to be restarted by new copies of
	 * themselves and finally starts new threads up to the given limit.
	 * 
	 * Several limits are configurable: There are both limits in number and in
	 * time for successfully finished or erroneous threads which can be set in
	 * the configuration. The limit for auto starting threads can accessed using
	 * getter and setter. There are internal default values for these too, which
	 * will applied in case of missing configuration. Keep in mind that zombie
	 * processes still occupy all their ressources and aren’t available for
	 * garbage collection, so these values have been chosen rather restrictive.
	 * 
	 * If a ConcurrentModificationException arises during list examination, the
	 * method will exit silently and do not do any more work. This is not a pity
	 * because it is likely to be restarted every some seconds.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		TaskManager taskManager = TaskManager.singleton();
		if (taskManager.taskList.size() == 0) {
			return;
		}

		LinkedList<AbstractTask> launchableThreads = new LinkedList<AbstractTask>();
		LinkedList<AbstractTask> finishedThreads = new LinkedList<AbstractTask>();
		LinkedList<AbstractTask> failedThreads = new LinkedList<AbstractTask>();
		int currentClearance = autoRunLimit;

		int successfulMaxCount = ConfigMain
				.getIntParameter("taskManager.keepThreads.successful.count", KEEP_SUCCESSFUL);
		int failedMaxCount = ConfigMain.getIntParameter("taskManager.keepThreads.failed.count", KEEP_FAILED);
		Duration successfulMaxAge = ConfigMain.getDurationParameter("taskManager.keepThreads.successful.minutes",
				TimeUnit.MINUTES, KEEP_SUCCESSFUL_MINS);
		Duration failedMaxAge = ConfigMain.getDurationParameter("taskManager.keepThreads.failed.minutes",
				TimeUnit.MINUTES, KEEP_FAILED_MINS);

		ListIterator<AbstractTask> position = taskManager.taskList.listIterator();
		AbstractTask task;
		try {
			while (position.hasNext()) {
				task = position.next();
				switch (task.getTaskState()) {
				case WORKING:
				case STOPPING:
					currentClearance = Math.max(currentClearance - 1, 0);
					break;
				case NEW:
					launchableThreads.addLast(task);
					break;
				default: // cases STOPPED, FINISHED, CRASHED
					switch (task.getBehaviourAfterTermination()) {
					case DELETE_IMMEDIATELY:
						position.remove();
						break;
					default: // case KEEP_FOR_A_WHILE 
						boolean successful = task.getException() == null;
						Duration durationDead = task.getDurationDead();
						if (durationDead == null) {
							task.setTimeOfDeath();
						} else if (durationDead.isLongerThan(successful ? successfulMaxAge : failedMaxAge)) {
							position.remove();
							break;
						}
						if (successful) {
							finishedThreads.add(task);
						} else {
							failedThreads.add(task);
						}
					case PREPARE_FOR_RESTART:
						AbstractTask replacement = null;
						if (task instanceof LongRunningTask) {
							replacement = newLegacyTask((LongRunningTask) task);
						}
						if (replacement != null) {
							position.set(replacement);
							launchableThreads.addLast(replacement);
						}
						break;
					}
				}
			}
		} catch (ConcurrentModificationException e) {
			return;
		}

		while (finishedThreads.size() > successfulMaxCount && (task = finishedThreads.pollFirst()) != null) {
			taskManager.taskList.remove(task);
		}

		while (failedThreads.size() > failedMaxCount && (task = failedThreads.pollFirst()) != null) {
			taskManager.taskList.remove(task);
		}

		while (launchableThreads.size() > currentClearance) {
			launchableThreads.removeLast();
		}
		while ((task = launchableThreads.pollFirst()) != null) {
			task.run();
		}
	}

	static void setAutoRunLimit(int newLimit) {
		autoRunLimit = newLimit;
	}
}