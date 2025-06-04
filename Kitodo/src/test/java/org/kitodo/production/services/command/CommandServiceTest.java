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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.api.command.CommandResult;

public class CommandServiceTest {
    private static String scriptExtension;
    private static String scriptPath = "src/test/resources/scripts/";
    private static boolean windows = false;
    private static File workingScript = new File(
            System.getProperty("user.dir") + "/" + scriptPath + "working_script.sh");
    private static File workingScriptWithParameters = new File(
            System.getProperty("user.dir") + "/" + scriptPath + "working_script_with_parameters.sh");
    private static File longWorkingScript2s = new File(
            System.getProperty("user.dir") + "/" + scriptPath + "long_working_script_2s.sh");
    private static File longWorkingScript1s = new File(
            System.getProperty("user.dir") + "/" + scriptPath + "long_working_script_1s.sh");

    @BeforeAll
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

    @AfterAll
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
        String commandString = scriptPath + "working_script" + scriptExtension;
        CommandService service = new CommandService();
        CommandResult result = service.runCommand(commandString);

        ArrayList<String> expectedMessages = new ArrayList<>();
        if (windows) {
            expectedMessages.add("");
            expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo Hello World ");
        }
        expectedMessages.add("Hello World");

        assertEquals(expectedMessages, result.getMessages(), "result messages are not identical");
    }

    @Test
    public void runScriptWithFile() throws IOException {
        String commandString = scriptPath + "working_script" + scriptExtension;
        File file = new File(commandString);
        CommandService service = new CommandService();
        CommandResult result = service.runCommand(file);

        ArrayList<String> expectedMessages = new ArrayList<>();
        if (windows) {
            expectedMessages.add("");
            expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo Hello World ");
        }
        expectedMessages.add("Hello World");

        assertEquals(expectedMessages, result.getMessages(), "result messages are not identical");
        assertTrue(result.isSuccessful(), "successful booleans are not matching");
    }

    @Test
    public void runScriptParameters() throws IOException {
        String commandString = scriptPath + "working_script_with_parameters" + scriptExtension;
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

        assertEquals(expectedMessages, result.getMessages(), "result messages are not identical");
        assertTrue(result.isSuccessful(), "successful booleans are not matching");
    }

    @Test
    public void runNotExistingScript() throws IOException {
        String commandString = scriptPath + "not_existing_script" + scriptExtension;
        CommandService service = new CommandService();
        assertThrows(IOException.class, () -> service.runCommand(commandString));
    }

    @Test
    public void runScriptAsync() throws InterruptedException {
        String commandString = scriptPath + "working_script" + scriptExtension;
        CommandService service = new CommandService();
        service.runCommandAsync(commandString);
        Thread.sleep(1000); // wait for async thread to finish;
        CommandResult result = getLastFinishedCommandResult(service.getFinishedCommandResults());
        assertNotNull(result, "There were not results!");
        assertEquals(result.getCommand(), commandString, "path to scripts are not identical");
    }

    @Test
    public void runLongScriptAsync() throws InterruptedException {
        String commandString2s = scriptPath + "long_working_script_2s" + scriptExtension;
        String commandString1s = scriptPath + "long_working_script_1s" + scriptExtension;
        CommandService service = new CommandService();
        service.runCommandAsync(commandString2s);
        service.runCommandAsync(commandString1s);
        Thread.sleep(3000); // wait for async thread to finish;
        CommandResult result = getLastFinishedCommandResult(service.getFinishedCommandResults());
        assertNotNull(result, "There were no results!");
        assertEquals(result.getCommand(), commandString2s, "latest finished command should be the 2 s one");
    }

    @Test
    public void runNotExistingScriptAsync() throws InterruptedException {
        String commandString = scriptPath + "not_existing_script" + scriptExtension;
        CommandService service = new CommandService();
        service.runCommandAsync(commandString);
        Thread.sleep(2000); // wait for async thread to finish;
        CommandResult result = getLastFinishedCommandResult(service.getFinishedCommandResults());
        assertNotNull(result, "There were no results!");
        assertTrue(result.getMessages().get(0).contains("IOException"), "result message should contain IOException");
    }

    @Test
    public void runScriptParametersAsync() throws InterruptedException {
        String commandString = scriptPath + "working_script_with_parameters" + scriptExtension;
        File file = new File(commandString);
        List<String> parameter = new ArrayList<>();
        parameter.add("HelloWorld");
        CommandService service = new CommandService();
        service.runCommandAsync(file, parameter);
        Thread.sleep(2000); // wait for async thread to finish;
        CommandResult result = getLastFinishedCommandResult(service.getFinishedCommandResults());

        ArrayList<String> expectedMessages = new ArrayList<>();
        if (windows) {
            expectedMessages.add("");
            expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo HelloWorld ");
        }
        expectedMessages.add("HelloWorld");

        assertNotNull(result, "There were no results!");
        assertEquals(expectedMessages, result.getMessages(), "result messages are not identical");
        assertTrue(result.isSuccessful(), "successful booleans are not identical");
    }

    /**
     * Returns the last finished CommandResult.
     * 
     * @param commandResults
     *            The CommandResults.
     * 
     * @return The CommandResult.
     */
    private CommandResult getLastFinishedCommandResult(List<CommandResult> commandResults) {
        if (commandResults.isEmpty()) {
            return null;
        }
        return commandResults.get(commandResults.size() - 1);
    }
}
