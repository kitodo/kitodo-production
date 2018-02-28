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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

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
     * @param id
     *            The id, to identify the command and it's results.
     * @param command
     *            The command as a String.
     * @return The command result.
     */
    @Override
    public CommandResult runCommand(Integer id, String command) {

        CommandResult commandResult = null;
        Process process = null;
        String[] callSequence = command.split("[\\r\\n\\s]+");

        try {
            process = new ProcessBuilder(callSequence).start();
            ArrayList<String> outputMessage = inputStreamArrayToList(process.getInputStream());
            ArrayList<String> errorMessage = inputStreamArrayToList(process.getErrorStream());
            int errCode = process.waitFor();

            outputMessage.addAll(errorMessage);

            commandResult = new CommandResult(id, command, errCode == 0, outputMessage);
            if (!commandResult.isSuccessful()) {
                logger.error("Execution of Command " + commandResult.getId() + " " + commandResult.getCommand()
                        + " failed!: " + commandResult.getMessages());
            }

            if (commandResult.isSuccessful()) {
                logger.info("Execution of Command " + commandResult.getId() + " " + commandResult.getCommand()
                        + " was succesfull!: " + commandResult.getMessages());
            }

        } catch (IOException | InterruptedException exception) {
            ArrayList<String> errorMessages = new ArrayList<>();
            errorMessages.add(exception.getCause().toString());
            errorMessages.add(exception.getMessage());
            commandResult = new CommandResult(id, command, false, errorMessages);
            logger.error("Execution of Command " + commandResult.getId() + " " + commandResult.getCommand()
                    + " failed!: " + commandResult.getMessages());
            return commandResult;
        } finally {
            if (process != null) {
                closeStream(process.getInputStream());
                closeStream(process.getOutputStream());
                closeStream(process.getErrorStream());
            }
        }
        return commandResult;
    }

    /**
     * The method reads an InputStream and returns it as a ArrayList.
     *
     * @param myInputStream
     *            The Stream to convert.
     * @return A ArrayList holding the single lines.
     */
    private static ArrayList<String> inputStreamArrayToList(InputStream myInputStream) {
        ArrayList<String> result = new ArrayList<>();

        try (Scanner inputLines = new Scanner(myInputStream, CHARSET)) {
            while (inputLines.hasNextLine()) {
                String myLine = inputLines.nextLine();
                result.add(myLine);
            }
        }
        return result;
    }

    /**
     * This behaviour was already implemented. I can’t say if it’s necessary.
     *
     * @param inputStream
     *            A stream to close.
     */
    private static void closeStream(Closeable inputStream) {
        if (inputStream == null) {
            return;
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            logger.warn("Could not close stream.", e);
        }
    }
}
