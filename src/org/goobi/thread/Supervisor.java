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

public class Supervisor extends Thread {

	private List<Thread> threads = new CopyOnWriteArrayList<Thread>();

	private Runnable onAllTerminated = null;

	private int yieldWaitTime = 1000;

	public void addChild(Thread child) {
		threads.add(child);
	}

	public void setYieldWaitTime(int millis) {
		yieldWaitTime = millis;
	}

	public void ifAllTerminatedRun(Runnable r) {
		onAllTerminated = r;
	}

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

