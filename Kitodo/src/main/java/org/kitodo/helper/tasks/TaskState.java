/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.helper.tasks;

import org.kitodo.helper.tasks.EmptyTask.Behaviour;

/**
 * TaskState defines a set of states that a task can be in. Their meanings are
 * the followings:
 * 
 * <dl>
 * <dt><code>CRASHED</code></dt>
 * <dd>The thread has terminated abnormally. The field “exception” is holding
 * the exception that has occurred.</dd>
 * <dt><code>FINISHED</code></dt>
 * <dd>The thread has finished its work without errors and is available for
 * clean-up.</dd>
 * <dt><code>NEW</code></dt>
 * <dd>The thread has not yet been started.</dd>
 * <dt><code>STOPPED</code></dt>
 * <dd>The thread was stopped by a front end user—resulting in a call to its
 * {@link EmptyTask#interrupt(Behaviour)} method with
 * {@link EmptyTask.Behaviour}.PREPARE_FOR_RESTART— and is able to restart after
 * cloning and replacing it.</dd>
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
