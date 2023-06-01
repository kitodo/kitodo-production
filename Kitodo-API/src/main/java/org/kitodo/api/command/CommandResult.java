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

import java.util.List;
import java.util.Objects;

public class CommandResult {

    /** The command as a String. */
    private String command;

    /** If the command execution was successful. */
    private boolean successful;

    /** The resultMessages. */
    private List<String> messages;

    /**
     * Constructor.
     * 
     * @param command
     *            The command.
     * @param successful
     *            If command was successful.
     * @param messages
     *            The resultMessages
     */
    public CommandResult(String command, boolean successful, List<String> messages) {
        this.command = command;
        this.successful = successful;
        this.messages = messages;
    }

    /**
     * Gets the command.
     * 
     * @return The command.
     */
    public String getCommand() {
        return command;
    }

    /**
     * Gets if command was successful.
     * 
     * @return The successful.
     */
    public boolean isSuccessful() {
        return successful;
    }

    /**
     * Gets the messages.
     * 
     * @return The messages.
     */
    public List<String> getMessages() {
        return messages;
    }

    /**
     * Indicates whether a CommandResults is "equal to" this one.
     *
     * @param object
     *            The reference CommandResults with which to compare.
     * @return True if CommandResults are equal.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof CommandResult) {
            CommandResult that = (CommandResult) object;

            return this.successful == that.successful
                    && this.command.equals(that.command)
                    && this.messages.equals(that.messages);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(successful, command, messages);
    }
}
