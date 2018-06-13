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

package org.kitodo.command;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.command.CommandInterface;
import org.kitodo.api.command.CommandResult;

public class Command implements CommandInterface {

    private static final Logger logger = LogManager.getLogger(Command.class);
    private static final String CHARSET = "UTF-8";
    private static final String MESSAGE = "Execution of Command ";

    /**
     * Method executes a script.
     *
     * @param id
     *            The id, to identify the command and it's results.
     * @param command
     *            The command as a String.
     * @return The command result.
     */
    @Override
    public CommandResult runCommand(Integer id, String command) {
        CommandResult commandResult;
        Process process;
        String[] callSequence = command.split("[\\r\\n\\s]+");

        try {
            process = new ProcessBuilder(callSequence).start();
            try (InputStream inputStream = process.getInputStream();
                    InputStream errorInputStream = process.getErrorStream()) {
                List<String> outputMessage = inputStreamArrayToList(inputStream);
                List<String> errorMessage = inputStreamArrayToList(errorInputStream);
                int errCode = process.waitFor();

                outputMessage.addAll(errorMessage);

                commandResult = new CommandResult(id, command, errCode == 0, outputMessage);
                if (!commandResult.isSuccessful()) {
                    logger.error(MESSAGE + commandResult.getId() + " " + commandResult.getCommand()
                            + " failed!: " + commandResult.getMessages());
                } else {
                    logger.info(MESSAGE + commandResult.getId() + " " + commandResult.getCommand()
                        + " was successful!: " + commandResult.getMessages());
                }
            }
        } catch (InterruptedException e) {
            commandResult = new CommandResult(id, command, false, Collections.singletonList(e.getMessage()));
            logger.error(MESSAGE + "Thread was interrupted!");
            Thread.currentThread().interrupt();
            return commandResult;
        } catch (IOException e) {
            List<String> errorMessages = new ArrayList<>();
            errorMessages.add(e.getCause().toString());
            errorMessages.add(e.getMessage());
            commandResult = new CommandResult(id, command, false, errorMessages);
            logger.error(MESSAGE + commandResult.getId() + " " + commandResult.getCommand()
                    + " failed!: " + commandResult.getMessages());
            return commandResult;
        }
        return commandResult;
    }

    /**
     * The method reads an InputStream and returns it as a ArrayList.
     *
     * @param inputStream
     *            The Stream to convert.
     * @return A ArrayList holding the single lines.
     */
    private static ArrayList<String> inputStreamArrayToList(InputStream inputStream) {
        ArrayList<String> result = new ArrayList<>();

        try (Scanner inputLines = new Scanner(inputStream, CHARSET)) {
            while (inputLines.hasNextLine()) {
                String myLine = inputLines.nextLine();
                result.add(myLine);
            }
        }
        return result;
    }
}
