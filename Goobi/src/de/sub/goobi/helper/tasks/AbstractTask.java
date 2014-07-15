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

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.joda.time.Duration;

import de.sub.goobi.helper.Helper;

public class AbstractTask extends Thread {
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
	public enum Behaviour {
		DELETE_IMMEDIATELY, KEEP_FOR_A_WHILE, PREPARE_FOR_RESTART
	}

	public static final Thread.UncaughtExceptionHandler CATCH_ALL = new Thread.UncaughtExceptionHandler() {
		@Override
		public void uncaughtException(Thread th, Throwable ex) {
			if (th instanceof AbstractTask) {
				AbstractTask that = (AbstractTask) th;
				that.setException(ex);
			}
		}
	};

	private static final Behaviour DEFAULT_BEHAVIOUR = Behaviour.KEEP_FOR_A_WHILE;

	private Behaviour behaviourAfterTermination; // what to do if the thread crashed

	private String detail = null; // a string telling details, which file is processed
	private Throwable exception = null; // an exception caught
	private Long passedAway = null;

	private int progress = 0; // a value from 0 to 100

	public AbstractTask() {
		setNameDetail(null);
	}

	Behaviour getBehaviourAfterTermination() {
		return behaviourAfterTermination;
	}

	Duration getDurationDead() {
		if (passedAway == null) {
			return null;
		}
		long elapsed = System.nanoTime() - passedAway;
		return new Duration(TimeUnit.MILLISECONDS.convert(elapsed, TimeUnit.NANOSECONDS));
	}

	/**
	 * Causes this thread to begin execution; the Java Virtual Machine calls the
	 * run method of this thread. The result is that two threads are running
	 * concurrently: the current thread which returns from the call to the start
	 * method and the other thread which executes its run method. In addition,
	 * this method override ensures that the thread is properly registered in
	 * the task manager and that its uncaught exception handler has been
	 * properly set.
	 * 
	 * @see java.lang.Thread#start()
	 */
	@Override
	public void start() {
		TaskManager.addTaskIfMissing(this);
		setUncaughtExceptionHandler(CATCH_ALL);
		super.start();
	}

	Throwable getException() {
		return exception;
	}

	public int getProgress() {
		return progress;
	}

	/**
	 * The function getStateDescription() returns a text string representing the
	 * state of the current task as read-only property "stateDescription".
	 * 
	 * @return a string representing the state of the task
	 */
	public String getStateDescription() {
		TaskState state = getTaskState();
		String label = Helper.getTranslation(state.toString().toLowerCase());
		switch (state) {
		case WORKING:
			if (detail != null) {
				return label + " (" + detail + ")";
			} else {
				return label;
			}
		case CRASHED:
			if (detail != null) {
				return label + " (" + detail + ")";
			} else if (exception.getMessage() != null) {
				return label + " (" + exception.getMessage() + ")";
			} else {
				return label + " (" + exception.getClass().getSimpleName() + ")";
			}
		default:
			return label;
		}
	}

	/**
	 * The function getTaskState() returns the task state, which can be one of
	 * the followings:
	 * 
	 * <dl>
	 * <dt><code>CRASHED</code></dt>
	 * <dd>The thread has terminated abnormally. The field “exception” is
	 * holding the exception that occurred.</dd>
	 * <dt><code>FINISHED</code></dt>
	 * <dd>The thread has finished its work without errors.</dd>
	 * <dt><code>NEW</code></dt>
	 * <dd>The thread has not yet been started.</dd>
	 * <dt><code>STOPPED</code></dt>
	 * <dd>The thread was stopped and is able to restart after cloning and
	 * replacing it. This state is reached if the thread performed a clean
	 * interruption and called leaveRestartable() before terminating.</dd>
	 * <dt><code>STOPPING</code></dt>
	 * <dd>The thread has received a request to interrupt but didn’t stop yet.</dd>
	 * <dt><code>WORKING</code></dt>
	 * <dd>The thread is in operation.</dd>
	 * </dl>
	 * 
	 * @return the task state
	 */
	TaskState getTaskState() {
		switch (getState()) {
		case NEW:
			return TaskState.NEW;
		case TERMINATED:
			if (behaviourAfterTermination == null) {
				behaviourAfterTermination = DEFAULT_BEHAVIOUR;
			}
			if (exception != null) {
				return TaskState.CRASHED;
			}
			if (Behaviour.PREPARE_FOR_RESTART.equals(behaviourAfterTermination)) {
				return TaskState.STOPPED;
			} else {
				return TaskState.FINISHED;
			}
		default:
			if (isInterrupted()) {
				return TaskState.STOPPING;
			} else {
				return TaskState.WORKING;
			}
		}
	}

	/**
	 * The function getLongMessage() returns the read-only field "longMessage"
	 * which will be shown in a pop-up window.
	 * 
	 * @return the stack trace of the exception, if any
	 */
	public String getLongMessage() {
		if (exception == null) {
			return null;
		}
		return ExceptionUtils.getStackTrace(exception);
	}

	/**
	 * The function interrupt() interrupts this thread and allows to set a
	 * behaviour after interruption.
	 * 
	 * @param mode
	 *            how to behave after interruption
	 */
	public void interrupt(Behaviour mode) {
		behaviourAfterTermination = mode;
		interrupt();
	}

	/**
	 * The procedure leaveRestartable() sets a flag that the thread can be
	 * restarted. The thread may call it when it has detected an interrupt to
	 * notify the task manager that it can successfully be replaced for
	 * restarting it. The function call will be ignored if a desired behaviour
	 * already has been requested.
	 */
	protected void leaveRestartable() {
		if (behaviourAfterTermination == null) {
			behaviourAfterTermination = Behaviour.PREPARE_FOR_RESTART;
		}
	}

	/**
	 * This is a sample implementation of run() which simulates a “long running
	 * task” but does nothing and just fills up the percentage gauge. It isn’t
	 * useful for anything but testing or demonstration purposes.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		for (int i = 1; i <= 100; i++) {

			// tell user some details what you are currently working on
			setWorkDetail(Integer.toString(i));

			// do something …
			try {
				sleep(150);
			} catch (InterruptedException e) {
			}

			// set progress
			setProgress(i);

			// The thread may have been signaled to stop. If so, leave
			if (isInterrupted()) {
				leaveRestartable(); // Mark this as not finished
				return; // leave procedure
			}
		}
		// we’re done. There is nothing more to do.
	}

	protected void setException(Throwable ex) {
		this.exception = ex;
	}

	protected void setNameDetail(String detail) {
		StringBuilder composer = new StringBuilder(119);
		composer.append(Helper.getTranslation(getClass().getSimpleName()));
		if (detail != null) {
			composer.append(": ");
			composer.append(detail);
		}
		super.setName(composer.toString());
	}

	protected void setProgress(int progress) {
		if (progress >= 0 || progress <= 100) {
			this.progress = progress;
		} else if (exception == null) {
			exception = new IllegalArgumentException(String.valueOf(progress));
		}
	}

	void setTimeOfDeath() {
		passedAway = System.nanoTime();
	}

	protected void setWorkDetail(String detail) {
		this.detail = detail;
	}

}
