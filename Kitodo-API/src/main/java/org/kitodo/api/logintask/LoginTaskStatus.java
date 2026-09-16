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

package org.kitodo.api.logintask;

public enum LoginTaskStatus {

    /**
     * Task is going to be executed at next login of user.
     */
    PENDING,

    /**
     * Task was successfully executed.
     */
    COMPLETED,

    /**
     * Task execution failed with error.
     */
    FAILED

}
