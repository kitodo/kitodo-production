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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private static File exit1WithStdErrorScript = new File(
            System.getProperty("user.dir") + "/" + scriptPath + "exit1_with_stderr.sh");
    private static File exit0WithStdErrorScript = new File(
            System.getProperty("user.dir") + "/" + scriptPath + "exit0_with_stderr.sh");

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
            ExecutionPermission.setExecutePermission(exit1WithStdErrorScript);
            ExecutionPermission.setExecutePermission(exit0WithStdErrorScript);

        }

    }

    @AfterAll
    public static void tearDown() throws IOException {
        if (!windows) {
            ExecutionPermission.setNoExecutePermission(workingScript);
            ExecutionPermission.setNoExecutePermission(workingScriptWithParameters);
            ExecutionPermission.setNoExecutePermission(longWorkingScript2s);
            ExecutionPermission.setNoExecutePermission(longWorkingScript1s);
            ExecutionPermission.setNoExecutePermission(exit1WithStdErrorScript);
            ExecutionPermission.setNoExecutePermission(exit0WithStdErrorScript);
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

        CommandResult result = service.runCommand(commandString);

        assertNotNull(result);
        assertEquals(-1, result.getExitCode());
        assertTrue(result.getStdErrMessages().getFirst().contains("IOException"));
        assertFalse(result.isSuccessful());
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
        assertTrue(result.getMessages().getFirst().contains("IOException"), "result message should contain IOException");
    }

    @Test
    public void runScriptExit0WithStdErr() throws IOException {
        // test script that exits 0 but writes to stderr
        String commandString = scriptPath + "exit0_with_stderr" + scriptExtension;
        File file = new File(commandString);
        CommandService service = new CommandService();
        CommandResult result = service.runCommand(file);

        assertNotNull(result, "Result must not be null");
        assertEquals(0, result.getExitCode(), "Exit code should be 0");
        assertTrue(result.isSuccessful(), "Command should be marked successful");

        // stdout should contain our test line
        assertTrue(result.getStdOutMessages().contains("Test StdOut output"), "StdOut should contain expected message");
        // stderr should contain our test error line, but not cause failure
        assertTrue(result.getStdErrMessages().contains("Test StdErr output"), "StdErr should contain expected message");
    }

    @Test
    public void runFailingScriptShouldNotThrow() {
        String commandString = scriptPath + "exit1_with_stderr" + scriptExtension;
        CommandService service = new CommandService();
        File file = new File(commandString);
        CommandResult result = assertDoesNotThrow(
                () -> service.runCommand(file),
                "Non-zero exit code must not cause an exception"
        );

        assertNotNull(result, "Result should not be null");
        assertEquals(1, result.getExitCode(), "Exit code should be 1");
        assertFalse(result.isSuccessful(), "isSuccessful should be false");
        assertTrue(
                result.getStdErrMessages().stream().anyMatch(msg -> msg.contains("Test StdErr output")),
                "StdErr should contain expected message"
        );
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
        return commandResults.getLast();
    }
}
