/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2014 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 * 
 * Visit the websites for more information.
 *     		- http://www.kitodo.org/en/
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
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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

import de.sub.goobi.helper.tasks.EmptyTask.Behaviour;

/**
 * TaskState defines a set of states that a task can be in. Their meanings are the followings:
 *
 * <dl>
 * <dt><code>CRASHED</code></dt>
 * <dd>The thread has terminated abnormally. The field “exception” is holding
 * the exception that has occurred.</dd>
 * <dt><code>FINISHED</code></dt>
 * <dd>The thread has finished its work without errors and is available for clean-up.</dd>
 * <dt><code>NEW</code></dt>
 * <dd>The thread has not yet been started.</dd>
 * <dt><code>STOPPED</code></dt>
 * <dd>The thread was stopped by a front end user—resulting in a call to its
 * {@link EmptyTask#interrupt(Behaviour)} method with
 * {@link EmptyTask.Behaviour}.PREPARE_FOR_RESTART— and is able to restart after cloning and replacing it.</dd>
 * <dt><code>STOPPING</code></dt>
 * <dd>The thread has received a request to interrupt but didn’t stop yet.</dd>
 * <dt><code>WORKING</code></dt>
 * <dd>The thread is in operation.</dd>
 * </dl>
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
enum TaskState {
	CRASHED, FINISHED, NEW, STOPPED, STOPPING, WORKING
}
