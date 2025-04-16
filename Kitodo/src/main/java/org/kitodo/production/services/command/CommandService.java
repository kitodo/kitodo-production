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

package org.kitodo.production.services.command;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.kitodo.api.command.CommandInterface;
import org.kitodo.api.command.CommandResult;
import org.kitodo.production.helper.Helper;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class CommandService {
    private final CommandInterface commandModule;
    private final ArrayList<CommandResult> finishedCommandResults = new ArrayList<>();

    /**
     * Initialize Command Service.
     */
    public CommandService() {
        commandModule = new KitodoServiceLoader<CommandInterface>(CommandInterface.class).loadModule();
    }

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
        if (Objects.isNull(script)) {
            return null;
        }
        CommandResult commandResult = commandModule.runCommand(script);
        List<String> commandResultMessages = commandResult.getMessages();
        if (!commandResult.isSuccessful() && !commandResultMessages.isEmpty()) {
            for (String message : commandResultMessages) {
                Helper.setErrorMessage(message);
            }
            String fullErrorMessage = String.join(" | ", commandResultMessages);
            throw new IOException(fullErrorMessage);
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
        if (Objects.isNull(scriptFile)) {
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
        if (Objects.isNull(scriptFile)) {
            return null;
        }
        return runCommand(scriptFile.getAbsolutePath());

    }

    /**
     * Method runs a specified script file asynchronously.
     *
     * @param script
     *            The script.
     */
    public void runCommandAsync(String script) {
        if (Objects.nonNull(script)) {
            Flowable<CommandResult> source = Flowable.fromCallable(() ->
                commandModule.runCommand(script)
            );

            Flowable<CommandResult> commandBackgroundWorker = source.subscribeOn(Schedulers.io());
            Flowable<CommandResult> commandResultListener = commandBackgroundWorker.observeOn(Schedulers.single());
            commandResultListener.subscribe(this::handleCommandResult);
        }
    }

    /**
     * Method executes a script file with parameters asynchronously.
     *
     * @param scriptFile
     *            The script file.
     * @param parameter
     *            The script parameters.
     */
    public void runCommandAsync(File scriptFile, List<String> parameter) {
        if (Objects.nonNull(scriptFile)) {
            String script = generateScriptString(scriptFile, parameter);
            runCommandAsync(script);
        }
    }

    /**
     * Method executes a script file asynchronously.
     *
     * @param scriptFile
     *            The script file.
     */
    public void runCommandAsync(File scriptFile) {
        if (Objects.nonNull(scriptFile)) {
            runCommandAsync(scriptFile.getAbsolutePath());
        }
    }

    /**
     * Should be used to handle finished asynchronous script executions.
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
        if (Objects.nonNull(parameter)) {
            scriptString = scriptString + " " + String.join(" ", parameter);
        }
        return scriptString;
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
