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

package org.kitodo.production.helper.tasks;

import org.kitodo.production.helper.tasks.EmptyTask.Behaviour;

/**
 * TaskState defines a set of states that a task can be in. Their meanings are
 * the following:
 * 
 * <dl>
 * <dt>{@code CRASHED}</dt>
 * <dd>The thread has terminated abnormally. The field “exception” is holding
 * the exception that has occurred.</dd>
 * <dt>{@code FINISHED}</dt>
 * <dd>The thread has finished its work without errors and is available for
 * clean-up.</dd>
 * <dt>{@code NEW}</dt>
 * <dd>The thread has not yet been started.</dd>
 * <dt>{@code STOPPED}</dt>
 * <dd>The thread was stopped by a front end user—resulting in a call to its
 * {@link EmptyTask#interrupt(Behaviour)} method with
 * {@link EmptyTask.Behaviour}.PREPARE_FOR_RESTART— and is able to restart after
 * cloning and replacing it.</dd>
 * <dt>{@code STOPPING}</dt>
 * <dd>The thread has received a request to interrupt but didn’t stop yet.</dd>
 * <dt>{@code WORKING}</dt>
 * <dd>The thread is in operation.</dd>
 * </dl>
 */
public enum TaskState {
    CRASHED, FINISHED, NEW, STOPPED, STOPPING, WORKING
}
