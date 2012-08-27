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

import java.lang.Thread;

import org.junit.Test;

import static org.junit.Assert.*;

public class SupervisorTest {

	@Test
	public void canBeStarted() {
		Supervisor sv = new Supervisor();
		sv.start();

		assertEquals("Supervisor thread should be in RUNNABLE state.", Thread.State.RUNNABLE, sv.getState());
	}

	@Test
	public void addedChildThreadShouldRemainInNewState() {

		Thread child = new Thread() {
			public int runCount = 0;
			public void run() {
				runCount++;  
			}
		};

		Supervisor sv = new Supervisor();
		sv.addChild(child);

		assertEquals("Child thread should be in NEW state.", Thread.State.NEW, child.getState());
	}

}

