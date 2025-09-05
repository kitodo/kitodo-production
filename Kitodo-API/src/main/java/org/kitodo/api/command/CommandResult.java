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
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class CommandResult {

    /** The command as a String. */
    private String command;

    private final int exitCode;

    /** If the command execution was successful. */
    private boolean successful;

    private final List<String> stdOutMessages;

    private final List<String> stdErrMessages;

    /**
     * Constructor.
     *
     * @param command
     *            The command.
     */
    public CommandResult(String command, int exitCode,
                         List<String> stdOutMessages, List<String> stdErrMessages) {
        this.command = command;
        this.exitCode = exitCode;
        this.successful = (exitCode == 0);
        this.stdOutMessages = stdOutMessages;
        this.stdErrMessages = stdErrMessages;
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
     * Gets the exit code.
     *
     * @return The exit code.
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Gets if command was successful.
     * 
     * @return The successful.
     */
    public boolean isSuccessful() {
        return successful;
    }

    public List<String> getStdOutMessages() {
        return stdOutMessages;
    }

    public List<String> getStdErrMessages() {
        return stdErrMessages;
    }

    /**
     * Fallback für bestehenden Code:
     * kombiniert stdout und stderr.
     */
    public List<String> getMessages() {
        return Stream.concat(stdOutMessages.stream(), stdErrMessages.stream())
                .collect(Collectors.toList());
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

            return this.exitCode == that.exitCode
                    && this.successful == that.successful
                    && Objects.equals(this.command, that.command)
                    && Objects.equals(this.stdOutMessages, that.stdOutMessages)
                    && Objects.equals(this.stdErrMessages, that.stdErrMessages);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, exitCode, successful, stdOutMessages, stdErrMessages);
    }
}
