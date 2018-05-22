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

package org.kitodo.services.command;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.kitodo.api.command.CommandInterface;
import org.kitodo.api.command.CommandResult;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class CommandService {

    private ArrayList<CommandResult> finishedCommandResults = new ArrayList<>();

    /**
     * Method executes a script string.
     *
     * @param script
     *            Path to the script file with optional arguments (filepath
     *            parameter1 parameter2 ...).
     *
     * @return The CommandResult.
     *
     * @throws IOException
     *             an IOException
     */
    public CommandResult runCommand(String script) throws IOException {
        if (script == null) {
            return null;
        }
        KitodoServiceLoader<CommandInterface> serviceLoader = new KitodoServiceLoader<>(CommandInterface.class);
        CommandInterface command = serviceLoader.loadModule();

        CommandResult commandResult = command.runCommand(generateId(), script);
        List<String> commandResultMessages = commandResult.getMessages();
        if (!commandResultMessages.isEmpty() && commandResultMessages.get(0).contains("IOException")) {
            throw new IOException(commandResultMessages.get(1));
        }
        return commandResult;
    }

    /**
     * Method executes a script file with parameters.
     *
     * @param scriptFile
     *            The script file.
     * @param parameter
     *            The script parameters.
     * @return The CommandResult.
     *
     * @throws IOException
     *             an IOException
     */
    public CommandResult runCommand(File scriptFile, List<String> parameter) throws IOException {
        if (scriptFile == null) {
            return null;
        }
        String script = generateScriptString(scriptFile, parameter);
        return runCommand(script);
    }

    /**
     * Method executes a script file.
     *
     * @param scriptFile
     *            The script file.
     *
     * @return The CommandResult.
     *
     * @throws IOException
     *             an IOException
     */
    public CommandResult runCommand(File scriptFile) throws IOException {
        if (scriptFile == null) {
            return null;
        }
        return runCommand(scriptFile.getAbsolutePath());

    }

    /**
     * Method runs a specified script file asynchron.
     *
     * @param script
     *            The script.
     */
    public void runCommandAsync(String script) {
        if (script != null) {
            KitodoServiceLoader<CommandInterface> serviceLoader = new KitodoServiceLoader<>(CommandInterface.class);
            CommandInterface commandInterface = serviceLoader.loadModule();

            Flowable<CommandResult> source = Flowable.fromCallable(() ->
                commandInterface.runCommand(generateId(), script)
            );

            Flowable<CommandResult> commandBackgroundWorker = source.subscribeOn(Schedulers.io());
            Flowable<CommandResult> commandResultListener = commandBackgroundWorker.observeOn(Schedulers.single());
            commandResultListener.subscribe(commandResult -> handleCommandResult(commandResult));
        }
    }

    /**
     * Method executes a script file with parameters asynchron.
     *
     * @param scriptFile
     *            The script file.
     * @param parameter
     *            The script parameters.
     */
    public void runCommandAsync(File scriptFile, List<String> parameter) {
        if (scriptFile != null) {
            String script = generateScriptString(scriptFile, parameter);
            runCommandAsync(script);
        }
    }

    /**
     * Method executes a script file asynchron.
     *
     * @param scriptFile
     *            The script file.
     */
    public void runCommandAsync(File scriptFile) {
        if (scriptFile != null) {
            runCommandAsync(scriptFile.getAbsolutePath());
        }
    }

    /**
     * Should be used to handle finished asynchron script executions.
     *
     * @param commandResult
     *            The finished command result.
     */
    private void handleCommandResult(CommandResult commandResult) {

        finishedCommandResults.add(commandResult);

        // TODO add more result handling for frontend here
    }

    /**
     * Generates a String in the form of (filepath parameter1 parameter2 ...).
     *
     * @param file
     *            The file.
     *
     * @param parameter
     *            The parameters.
     *
     * @return The String.
     */
    private String generateScriptString(File file, List<String> parameter) {
        String scriptString = file.getAbsolutePath();
        if (parameter != null) {
            scriptString = scriptString + " " + String.join(" ", parameter);
        }
        return scriptString;
    }

    /**
     * Generates a random integer in the range of 0-1000000.
     *
     * @return The integer value.
     */
    private int generateId() {
        Random random = new Random();
        return random.nextInt(1000000);
    }

    /**
     * Returns all finished CommandResults.
     *
     * @return The CommandResults.
     */
    public List<CommandResult> getFinishedCommandResults() {
        return finishedCommandResults;
    }
}
