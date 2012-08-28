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

package org.goobi.thread;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/**
 * Thread supervisor that triggers code if all child threads have terminated.
 *
 * The supervisor is watching a list of child threads and executes a passed
 * Runnable object if all child threads have finished execution. It in turn
 * starts all child threads that have not been started when the supervisor starts.
 * 
 * If all child threads have finished execution, the supervisor may start a given
 * Runnable implementation and then itself ends its execution.
 */
public class Supervisor extends Thread {

	private List<Thread> threads = new CopyOnWriteArrayList<Thread>();

	private Runnable onAllTerminated = null;

	private int yieldWaitTime = 1000;

	/**
	 * Add a child thread to the watch list.
	 *
	 * @param child Child thread to watch.
	 */
	public void addChild(Thread child) {
		threads.add(child);
	}

	/**
	 * Set time in milliseconds that delay the supervisor execution after it
	 * has called yield() to enable other threads to run.
	 *
	 * The supervisor sleeps for this time span until it watches the list of
	 * child threads again. Default yield wait time is 1000 milliseconds.
	 * Setting the wait time to zero will let the supervisor thread spin
	 * useless CPU cycles polling the child thread list.
	 *
	 * @param millis Yield wait time in milliseconds.
	 * @throws IllegalArgumentException if the value of millis is negative.
	 */
	public void setYieldWaitTime(int millis) {
		if (millis < 0) {
			throw new IllegalArgumentException("Length of time to wait cannot be negative.");
		}
		yieldWaitTime = millis;
	}

	/**
	 * Set up a Runnable implementation that gets executed when all child
	 * threads have terminated.
 	 *
	 * The passed Runnable is executed only once.
	 * 
	 * @param r java.lang.Runnable implememtation
	 */
	public void ifAllTerminatedRun(Runnable r) {
		onAllTerminated = r;
	}

	/**
	 * Start watching the list of child threads and execute a given Runnable
	 * when all child threads have reached termination state.
	 *
	 * Child threads that have not been started yet get started here.
	 */
	public void run() {
		while (true) {
			for(Thread t: threads) {
				switch (t.getState()) {
					case NEW:
						t.start();
						break;
					case TERMINATED:
						threads.remove(t);
						break;
				}
			}
			if (threads.isEmpty()) {
				if (onAllTerminated != null) {
					onAllTerminated.run();
				}
				break;
			}
			yieldFor(yieldWaitTime);	
		}
	}

	private void yieldFor(int millis) {
		try {
			yield();
			sleep(millis);
		} catch (InterruptedException ire) {
		}
	}

}

