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

package org.kitodo.exceptions;

/**
 * This exception is thrown when a project that is to be deleted
 * contains processes and therefore cannot be deleted.
 */
public class ProjectDeletionException extends Exception {

    /**
     * Standard constructor with a message String.
     *
     * @param message exception message
     */
    public ProjectDeletionException(String message) {
        super(message);
    }
}
