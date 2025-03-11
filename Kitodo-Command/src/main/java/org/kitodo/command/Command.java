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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.command.CommandInterface;
import org.kitodo.api.command.CommandResult;

public class Command implements CommandInterface {

    private static final Logger logger = LogManager.getLogger(Command.class);
    private static final String CHARSET = "UTF-8";

    /**
     * Method executes a script.
     *
     * @param command
     *            The command as a String.
     * @return The command result.
     */
    @Override
    public CommandResult runCommand(String command) {
        CommandResult commandResult;
        Process process;
        String[] callSequence = command.split("[\\r\\n\\s]+");

        try {
            process = new ProcessBuilder(callSequence).start();
            try (InputStream inputStream = process.getInputStream();
                    InputStream errorInputStream = process.getErrorStream()) {
                List<String> outputMessage = inputStreamArrayToList(inputStream);
                List<String> errorMessage = inputStreamArrayToList(errorInputStream);
                List<String> combinedMessages = Stream.concat(outputMessage.stream(), errorMessage.stream())
                        .collect(Collectors.toList());
                int errCode = process.waitFor();
                if (Integer.valueOf(errCode).equals(0)) {
                    commandResult = new CommandResult(command, true, outputMessage);
                    logger.info("Execution of Command {} was successful!: {}",
                            commandResult.getCommand(), combinedMessages);
                } else {
                    commandResult = new CommandResult(command, false, errorMessage);
                    logger.error("Execution of Command {} failed!: {}",
                            commandResult.getCommand(), combinedMessages);
                }
            }
        } catch (InterruptedException e) {
            commandResult = new CommandResult(command, false, Collections.singletonList(e.getMessage()));
            logger.error("Execution of Command Thread was interrupted!");
            Thread.currentThread().interrupt();
            return commandResult;
        } catch (IOException e) {
            List<String> errorMessages = new ArrayList<>();
            errorMessages.add(e.getCause().toString());
            errorMessages.add(e.getMessage());
            commandResult = new CommandResult(command, false, errorMessages);
            logger.error("Execution of Command {} failed!: {}", commandResult.getCommand(),
                commandResult.getMessages());
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
        ArrayList<String> list = new ArrayList<>();

        try (Scanner inputLines = new Scanner(inputStream, CHARSET)) {
            while (inputLines.hasNextLine()) {
                String myLine = inputLines.nextLine();
                list.add(myLine);
            }
        }
        return list;
    }
}
