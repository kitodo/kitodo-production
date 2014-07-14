package de.sub.goobi.helper.tasks;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.TimeUnit;

import org.joda.time.Duration;

import de.sub.goobi.config.ConfigMain;

/**
 * The class Housekeeper is a Runnable which implements the removing of old
 * threads and starting of new ones.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class TaskManagerHousekeeper implements Runnable {
	private static final int KEEP_FAILED = 10;
	private static final long KEEP_FAILED_MINS = 250;
	private static final int KEEP_SUCCESSFUL = 3;
	private static final long KEEP_SUCCESSFUL_MINS = 20;

	/**
	 * TODO
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int successfulMaxCount = ConfigMain
				.getIntParameter("taskManager.keepThreads.successful.count", KEEP_SUCCESSFUL);
		int failedMaxCount = ConfigMain.getIntParameter("taskManager.keepThreads.failed.count", KEEP_FAILED);
		Duration successfulMaxAge = ConfigMain.getDurationParameter("taskManager.keepThreads.successful.minutes",
				TimeUnit.MINUTES, KEEP_SUCCESSFUL_MINS);
		Duration failedMaxAge = ConfigMain.getDurationParameter("taskManager.keepThreads.failed.minutes",
				TimeUnit.MINUTES, KEEP_FAILED_MINS);

		TaskManager taskManager = TaskManager.singleton();
		LinkedList<AbstractTask> launchableThreads = new LinkedList<AbstractTask>();
		LinkedList<AbstractTask> finishedThreads = new LinkedList<AbstractTask>();
		LinkedList<AbstractTask> failedThreads = new LinkedList<AbstractTask>();
		int clearance = taskManager.autoRunLimit;
		ListIterator<AbstractTask> position = taskManager.taskList.listIterator();
		AbstractTask task;
		try {
			while (position.hasNext()) {
				task = position.next();
				switch (task.getTaskState()) {
				case WORKING:
				case STOPPING:
					clearance = Math.max(clearance - 1, 0);
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
							LongRunningTask legacyTask = (LongRunningTask) task;
							try {
								LongRunningTask lrt = legacyTask.getClass().newInstance();
								lrt.initialize(legacyTask.getProzess());
								replacement = lrt;
							} catch (InstantiationException e) {
								throw new RuntimeException(e.getMessage(), e);
							} catch (IllegalAccessException e) {
								throw new RuntimeException(e.getMessage(), e);
							}
						}
						if (replacement != null) {
							position.set(replacement);
							launchableThreads.addLast(replacement);
						}
						break;
					}
				}
			}
		} catch (ConcurrentModificationException come) {
			return;
		}

		while (finishedThreads.size() > successfulMaxCount && (task = finishedThreads.pollFirst()) != null) {
			taskManager.taskList.remove(task);
		}

		while (failedThreads.size() > failedMaxCount && (task = failedThreads.pollFirst()) != null) {
			taskManager.taskList.remove(task);
		}

		while (launchableThreads.size() > clearance) {
			launchableThreads.removeLast();
		}
		while ((task = launchableThreads.pollFirst()) != null) {
			task.run();
		}
	}
}