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

import de.sub.goobi.config.ConfigCore;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.command.CommandInterface;
import org.kitodo.api.command.CommandResult;
import org.kitodo.serviceloader.KitodoServiceLoader;

public class CommandService {

    private static final Logger logger = LogManager.getLogger(CommandService.class);
    private ArrayList<CommandResult> finishedCommands = new ArrayList<>();

    /**
     * Method executes a script string.
     *
     * @param script
     *            Path to the script file with optional arguments (<file path>
     *            <parameter1> <parameter2>...).
     * @throws IOException
     *             an IOException
     */
    public CommandResult runCommand(String script) {
        if (script == null) {
            throw new IllegalStateException("script must not be null");
        }
        KitodoServiceLoader<CommandInterface> serviceLoader = new KitodoServiceLoader<>(CommandInterface.class,
                ConfigCore.getParameter("moduleFolder"));
        CommandInterface commandInterface = serviceLoader.loadModule();

        int id = generateId();
        CommandResult commandResult = commandInterface.runCommand(id, script);
        return commandResult;
    }

    /**
     * Method executes a script file with parameters.
     *
     * @param scriptFile
     *            The script file.
     * @param parameter
     *            The script parameters.
     * @throws IOException
     *             an IOException
     * @throws InterruptedException
     *             an InterruptedException
     */
    public CommandResult runCommand(File scriptFile, List<String> parameter) throws IOException, InterruptedException {
        if (scriptFile == null) {
            throw new IllegalStateException("script file must not be null");
        }
        String script = generateScriptString(scriptFile, parameter);
        CommandResult commandResult = runCommand(script);
        return commandResult;
    }

    /**
     * Method executes a script file.
     *
     * @param scriptFile
     *            The script file.
     * @throws IOException
     *             an IOException
     * @throws InterruptedException
     *             an InterruptedException
     */
    public CommandResult runCommand(File scriptFile) throws IOException, InterruptedException {
        if (scriptFile == null) {
            throw new IllegalStateException("script file must not be null");
        }
        CommandResult commandResult = runCommand(scriptFile.getAbsolutePath());
        return commandResult;

    }

    /**
     * Method runs a specified script file asynchron.
     *
     * @param script
     *            The script.
     */
    public void runCommandAsync(String script) {
        if (script != null) {
            generateId();
            KitodoServiceLoader<CommandInterface> serviceLoader = new KitodoServiceLoader<>(CommandInterface.class,
                    ConfigCore.getParameter("moduleFolder"));
            CommandInterface commandInterface = serviceLoader.loadModule();

            Flowable<CommandResult> source = Flowable.fromCallable(() -> {
                CommandResult commandResult = commandInterface.runCommand(generateId(), script);
                return commandResult;
            });

            Flowable<CommandResult> commandBackgroundWorker = source.subscribeOn(Schedulers.io());
            Flowable<CommandResult> commandResultListener = commandBackgroundWorker.observeOn(Schedulers.single());
            commandResultListener.subscribe(commandResult -> handleCommandResult(commandResult),
                    Throwable::printStackTrace);
        }
    }

    /**
     * Should be used to handle finished asynchron scrip executions.
     * 
     * @param commandResult
     *            The finished command result.
     */
    private void handleCommandResult(CommandResult commandResult) {
        if (!commandResult.isSuccessful()) {
            logger.error("Execution of Command " + commandResult.getId() + " " + commandResult.getCommand()
                    + " failed!: " + commandResult.getMessages());
        }

        if (commandResult.isSuccessful()) {
            logger.info("Execution of Command " + commandResult.getId() + " " + commandResult.getCommand()
                    + " was succesfull!: " + commandResult.getMessages());
        }

        finishedCommands.add(commandResult);

        // TODO add more result handling for frontend here
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
        if (scriptFile == null) {
            throw new IllegalStateException("script file must not be null");
        }
        String script = generateScriptString(scriptFile, parameter);
        runCommandAsync(script);
    }

    /**
     * Method executes a script file asynchron.
     *
     * @param scriptFile
     *            The script file.
     */
    public void runCommandAsync(File scriptFile) {
        if (scriptFile == null) {
            throw new IllegalStateException("script file must not be null");
        }
        runCommandAsync(scriptFile.getAbsolutePath());
    }

    /**
     * Generates a String in the form of (<file path> <parameter1> <parameter2>...).
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
            scriptString = scriptString + " " + scriptString.join(" ", parameter);
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
     * Returns the last finished CommandResult.
     * 
     * @return The ComandResult.
     */
    public CommandResult getLastFinishedCommand() {
        if (finishedCommands.isEmpty())
            throw new IllegalStateException("Finished commands must not be empty");

        return finishedCommands.get(finishedCommands.size() - 1);
    }
}
