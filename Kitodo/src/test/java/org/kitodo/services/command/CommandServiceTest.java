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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.api.command.CommandResult;

public class CommandServiceTest {
    private static String scriptExtension;
    private static boolean windows = false;
    private static File workingScript = new File(
            System.getProperty("user.dir") + "/src/test/resources/working_script.sh");
    private static File workingScriptWithParameters = new File(
            System.getProperty("user.dir") + "/src/test/resources/working_script_with_parameters.sh");
    private static File longWorkingScript2s = new File(
            System.getProperty("user.dir") + "/src/test/resources/long_working_script_2s.sh");
    private static File longWorkingScript1s = new File(
            System.getProperty("user.dir") + "/src/test/resources/long_working_script_1s.sh");

    @BeforeClass
    public static void setUp() throws IOException {

        if (SystemUtils.IS_OS_WINDOWS) {
            scriptExtension = ".bat";
            windows = true;
        } else {
            scriptExtension = ".sh";

            ExecutionPermission.setExecutePermission(workingScript);
            ExecutionPermission.setExecutePermission(workingScriptWithParameters);
            ExecutionPermission.setExecutePermission(longWorkingScript2s);
            ExecutionPermission.setExecutePermission(longWorkingScript1s);
        }

    }

    @AfterClass
    public static void tearDown() throws IOException {
        if (!windows) {
            ExecutionPermission.setNoExecutePermission(workingScript);
            ExecutionPermission.setNoExecutePermission(workingScriptWithParameters);
            ExecutionPermission.setNoExecutePermission(longWorkingScript2s);
            ExecutionPermission.setNoExecutePermission(longWorkingScript1s);
        }
    }

    @Test
    public void runScriptWithString() throws IOException {
        String commandString = "src/test/resources/working_script" + scriptExtension;
        CommandService service = new CommandService();
        CommandResult result = service.runCommand(commandString);

        ArrayList<String> expectedMessages = new ArrayList<>();
        if (windows) {
            expectedMessages.add("");
            expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo Hello World ");
        }
        expectedMessages.add("Hello World");

        assertEquals("result messages are not identical", expectedMessages, result.getMessages());
    }

    @Test
    public void runScriptWithFile() throws IOException {
        String commandString = "src/test/resources/working_script" + scriptExtension;
        File file = new File(commandString);
        CommandService service = new CommandService();
        CommandResult result = service.runCommand(file);

        ArrayList<String> expectedMessages = new ArrayList<>();
        if (windows) {
            expectedMessages.add("");
            expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo Hello World ");
        }
        expectedMessages.add("Hello World");

        assertEquals("result messages are not identical", expectedMessages, result.getMessages());
        assertEquals("successful booleans are not matching", true, result.isSuccessful());
    }

    @Test
    public void runScriptParameters() throws IOException {
        String commandString = "src/test/resources/working_script_with_parameters" + scriptExtension;
        File file = new File(commandString);
        List<String> parameter = new ArrayList<>();
        parameter.add("HelloWorld");
        CommandService service = new CommandService();
        CommandResult result = service.runCommand(file, parameter);

        ArrayList<String> expectedMessages = new ArrayList<>();
        if (windows) {
            expectedMessages.add("");
            expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo HelloWorld ");
        }
        expectedMessages.add("HelloWorld");

        assertEquals("result messages are not identical", expectedMessages, result.getMessages());
        assertEquals("successful booleans are not matching", true, result.isSuccessful());
    }

    @Test(expected = IOException.class)
    public void runNotExistingScript() throws InterruptedException, IOException {
        String commandString = "src/test/resources/not_existing_script" + scriptExtension;
        CommandService service = new CommandService();
        service.runCommand(commandString);
    }

    @Test
    public void runScriptAsync() throws InterruptedException {
        String commandString = "src/test/resources/working_script" + scriptExtension;
        CommandService service = new CommandService();
        service.runCommandAsync(commandString);
        Thread.sleep(1000); // wait for async thread to finish;
        CommandResult commandResult = getLastFinishedCommandResult(service.getFinishedCommandResults());
        assertEquals("path to scripts are not identical", commandResult.getCommand(), commandString);
    }

    @Test
    public void runLongScriptAsync() throws InterruptedException {
        String commandString2s = "src/test/resources/long_working_script_2s" + scriptExtension;
        String commandString1s = "src/test/resources/long_working_script_1s" + scriptExtension;
        CommandService service = new CommandService();
        service.runCommandAsync(commandString2s);
        service.runCommandAsync(commandString1s);
        Thread.sleep(3000); // wait for async thread to finish;
        CommandResult commandResult = getLastFinishedCommandResult(service.getFinishedCommandResults());
        assertEquals("latest finished command should be the 2 s one", commandResult.getCommand(), commandString2s);
    }

    @Test
    public void runNotExistingScriptAsync() throws InterruptedException {
        String commandString = "src/test/resources/not_existing_script" + scriptExtension;
        CommandService service = new CommandService();
        service.runCommandAsync(commandString);
        Thread.sleep(1000); // wait for async thread to finish;
        CommandResult commandResult = getLastFinishedCommandResult(service.getFinishedCommandResults());
        assertEquals("result message should contain IOException",
                commandResult.getMessages().get(0).contains("IOException"), true);
    }

    @Test
    public void runScriptParametersAsync() throws InterruptedException {
        String commandString = "src/test/resources/working_script_with_parameters" + scriptExtension;
        File file = new File(commandString);
        List<String> parameter = new ArrayList<>();
        parameter.add("HelloWorld");
        CommandService service = new CommandService();
        service.runCommandAsync(file, parameter);
        Thread.sleep(1000); // wait for async thread to finish;
        CommandResult result = getLastFinishedCommandResult(service.getFinishedCommandResults());

        ArrayList<String> expectedMessages = new ArrayList<>();
        if (windows) {
            expectedMessages.add("");
            expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo HelloWorld ");
        }
        expectedMessages.add("HelloWorld");

        assertEquals("result messages are not identical", expectedMessages, result.getMessages());
        assertEquals("successful booleans are not identical", true, result.isSuccessful());
    }

    /**
     * Returns the last finished CommandResult.
     * 
     * @param commandResults
     *            The CommandResults.
     * 
     * @return The CommandResult.
     */
    public CommandResult getLastFinishedCommandResult(ArrayList<CommandResult> commandResults) {
        if (commandResults.isEmpty())
            return null;

        return commandResults.get(commandResults.size() - 1);
    }
}
