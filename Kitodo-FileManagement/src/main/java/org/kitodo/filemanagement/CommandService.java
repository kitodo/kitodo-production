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

package org.kitodo.filemanagement;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.kitodo.api.command.CommandInterface;
import org.kitodo.api.command.CommandResult;
import org.kitodo.serviceloader.KitodoServiceLoader;

class CommandService {

    /**
     * Method executes a script string.
     *
     * @param script
     *            Path to the script file with optional arguments (filepath
     *            parameter1 parameter2 ...).
     * @return CommandResult objects
     */
    CommandResult runCommand(String script) throws IOException {
        if (script == null) {
            return null;
        }
        KitodoServiceLoader<CommandInterface> serviceLoader = new KitodoServiceLoader<>(CommandInterface.class);
        CommandInterface command = serviceLoader.loadModule();

        CommandResult commandResult = command.runCommand(script);
        List<String> commandResultMessages = commandResult.getMessages();
        if (!commandResultMessages.isEmpty() && commandResultMessages.getFirst().contains("IOException")) {
            throw new IOException(commandResultMessages.getFirst());
        }
        return commandResult;
    }

    /**
     * Method executes a script file with parameters.
     *
     * @param scriptFile
     *            script file
     * @param parameter
     *            script parameters
     * @return CommandResult object
     */
    CommandResult runCommand(File scriptFile, List<String> parameter) throws IOException {
        if (scriptFile == null) {
            return null;
        }
        String script = generateScriptString(scriptFile, parameter);
        return runCommand(script);
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
}
