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

package org.kitodo.production.interfaces.activemq;

public enum TaskAction {
    /**
     * Adds a comment to the task.
     */
    COMMENT,

    /**
     * Add an error comment when task status is INWORK and set the task status to LOCKED if the correction id is set.
     */
    ERROR_OPEN,

    /**
     * Set task status of LOCKED (if correction id is set) or INWORK (if correction id is not set) task to OPEN.
     */
    ERROR_CLOSE,

    /**
     * Set task status of open task to INWORK.
     */
    PROCESS,

    /**
     * Close a task.
     */
    CLOSE
}
