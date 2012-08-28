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

import java.lang.InterruptedException;
import java.lang.Thread;

import org.junit.Test;

import static org.junit.Assert.*;

public class SupervisorTest {

	@Test
	public void canBeStarted() {
		Supervisor sv = new Supervisor();
		assertEquals("Supervisor thread should be in NEW state.", Thread.State.NEW, sv.getState());
	}

	@Test
	public void terminatesWhenStartedWithoutChildThreads()
	throws InterruptedException {
		Supervisor sv = new Supervisor();
		sv.start();

		Thread.sleep(200);

		assertEquals("Supervisor thread should be in TERMINATED state.", Thread.State.TERMINATED, sv.getState());
	}

	@Test
	public void addedChildThreadShouldRemainInNewState() {
		Supervisor sv = new Supervisor();
		
		Thread child = new Thread();
		sv.addChild(child);
		
		assertEquals("Child thread should be in NEW state.", Thread.State.NEW, child.getState());
	}

	@Test
	public void runsChildThreads()
	throws InterruptedException {
		Supervisor sv = new Supervisor();
		
		Thread child = new Thread();
		sv.addChild(child);
		sv.start();

		Thread.sleep(200);

		assertEquals("Child thread should have been run.", Thread.State.TERMINATED, child.getState());
	}
	 
	@Test
	public void runsHandlerWhenAllChildThreadsTerminated()
	throws InterruptedException {
		Supervisor sv = new Supervisor();

		final Trigger trigger = new Trigger();

		sv.addChild(new Thread());
		sv.addChild(new Thread());
		sv.addChild(new Thread());

		sv.ifAllTerminatedRun(new Runnable() { public void run() { trigger.pull(); } } );
		sv.start();

		Thread.sleep(200);

		assertTrue("Supervisor has not triggered Runnable on child termination.", trigger.hasBeenPulled());
	}

	private class Trigger {
		private Boolean pulled = false;
		public void pull() {
			pulled = true;
		}
		public Boolean hasBeenPulled() {
			return pulled;
		}
	}

}

