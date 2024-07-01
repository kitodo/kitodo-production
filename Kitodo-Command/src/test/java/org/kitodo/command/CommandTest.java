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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;
import org.kitodo.api.command.CommandResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CommandTest {
    private static String scriptExtension;
    private static boolean windows = false;
    private static final File workingScript = new File(
            System.getProperty("user.dir") + "/src/test/resources/working_script.sh");
    private static final File workingScriptWithParameters = new File(
            System.getProperty("user.dir") + "/src/test/resources/working_script_with_parameters.sh");
    private static final File notWorkingScript = new File(
            System.getProperty("user.dir") + "/src/test/resources/not_working_script.sh");

    @BeforeAll
    public static void setUp() throws IOException {

        if (SystemUtils.IS_OS_WINDOWS) {
            scriptExtension = ".bat";
            windows = true;
        } else {
            scriptExtension = ".sh";
        }

    }

    @AfterAll
    public static void tearDown() throws IOException {
    }

    @Test
    public void shouldRunCommand() {
        Command command = new Command();

        String commandString = "src/test/resources/working_script" + scriptExtension;
        CommandResult commandResult = command.runCommand(commandString);

        List<String> expectedMessages = new ArrayList<>();
        if (windows) {
            expectedMessages.add("");
            expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo Hello World ");
        }

        expectedMessages.add("Hello World");

        CommandResult expectedCommandResult = new CommandResult(commandString, true, expectedMessages);

        assertEquals(expectedCommandResult.isSuccessful(),
                commandResult.isSuccessful(),
                "successful booleans of CommandResults are not identical");
        assertEquals(expectedCommandResult.getCommand(),
                commandResult.getCommand(),
                "Command of CommandResults are not identical");
        assertEquals(expectedCommandResult.getMessages(),
                commandResult.getMessages(),
                "Result messages of CommandResults are not identical");
    }

    @Test
    public void shouldNotRunNotExistingCommand() {
        Command command = new Command();

        String commandString = "src/test/resources/notExistingScript" + scriptExtension;
        CommandResult commandResult = command.runCommand(commandString);

        CommandResult expectedCommandResult = new CommandResult(commandString, false, null);

        assertEquals(expectedCommandResult.isSuccessful(),
                commandResult.isSuccessful(),
                "Should not run not existing Command");
    }

    @Test
    public void shouldNotRunCommandWithFalseSyntax() {
        Command command = new Command();

        String commandString = "src/test/resources/not_working_script" + scriptExtension;
        CommandResult commandResult = command.runCommand(commandString);

        CommandResult expectedCommandResult = new CommandResult(commandString, false, null);

        assertEquals(expectedCommandResult.isSuccessful(),
                commandResult.isSuccessful(),
                "Should not run command with false syntax");
    }

    @Test
    public void shouldRunCommandWithParameter() {
        Command command = new Command();

        String commandString = "src/test/resources/working_script_with_parameters" + scriptExtension + " testParameter";
        CommandResult commandResult = command.runCommand(commandString);

        ArrayList<String> expectedMessages = new ArrayList<>();

        if (windows) {
            expectedMessages.add("");
            expectedMessages.add(Paths.get(".").toAbsolutePath().normalize().toString() + ">echo testParameter ");
        }

        expectedMessages.add("testParameter");

        CommandResult expectedCommandResult = new CommandResult(commandString, true, expectedMessages);

        assertEquals(expectedCommandResult.isSuccessful(),
                commandResult.isSuccessful(),
                "successful booleans of CommandResults are not identical");
        assertEquals(expectedCommandResult.getCommand(),
                commandResult.getCommand(),
                "Command of CommandResults are not identical");
        assertEquals(expectedCommandResult.getMessages(),
                commandResult.getMessages(),
                "Result messages of CommandResults are not identical");
    }
}
