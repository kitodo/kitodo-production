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

package org.kitodo.api.command;

public interface CommandInterface {

    /**
     * Runs a given command.
     *
     * @param command
     *            The command as a String.
     * @return A commandResult, which contains id and result messages.
     */
    CommandResult runCommand(String command);
}
