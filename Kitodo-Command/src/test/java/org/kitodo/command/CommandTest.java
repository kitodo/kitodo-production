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

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.kitodo.api.command.CommandResult;
import org.apache.commons.lang3.SystemUtils;

public class CommandTest {
    private String scriptExtention;
    private int processId = 3;

    @Before
    public void setScriptExtensionByOS() {

        if (SystemUtils.IS_OS_WINDOWS) {
            scriptExtention = ".bat";
        }

        if (SystemUtils.IS_OS_LINUX) {
            scriptExtention = ".sh";
        }
    }

    @Test
    public void shouldRunCommand() {
        Command command = new Command();

        String commandString = "src/test/resources/working_script" + scriptExtention;
        CommandResult commandResult = command.runCommand(processId, commandString);

        ArrayList<String> expectedMessages = new ArrayList<>();
        expectedMessages.add("");
        expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo Hello World ");
        expectedMessages.add("Hello World");

        CommandResult expectedCommandResult = new CommandResult(processId, commandString, true, expectedMessages);

        assertEquals("successful booleans of CommandResults are not identical", expectedCommandResult.isSuccessful(), commandResult.isSuccessful());
        assertEquals("Command of CommandResults are not identical", expectedCommandResult.getCommand(), commandResult.getCommand());
        assertEquals("Result messages of CommandResults are not identical", expectedCommandResult.getMessages(), commandResult.getMessages());
    }

    @Test
    public void shouldNotRunNotExistingCommand() {
        Command command = new Command();

        String commandString = "src/test/resources/notExistingScript" + scriptExtention;
        CommandResult commandResult = command.runCommand(processId, commandString);

        CommandResult expectedCommandResult = new CommandResult(processId, commandString, false, null);

        assertEquals("Should not run not existing Command", expectedCommandResult.isSuccessful(),
                commandResult.isSuccessful());
    }

    @Test
    public void shouldNotRunCommandWithFalseSyntax() {
        Command command = new Command();

        String commandString = "src/test/resources/not_working_script" + scriptExtention;
        CommandResult commandResult = command.runCommand(processId, commandString);

        CommandResult expectedCommandResult = new CommandResult(processId, commandString, false, null);

        assertEquals("Should not run command with false syntax", expectedCommandResult.isSuccessful(),
                commandResult.isSuccessful());
    }

    @Test
    public void shouldRunCommandWithParameter() {
        Command command = new Command();

        String commandString = "src/test/resources/working_script_with_parameters" + scriptExtention
                + " testParameter";
        CommandResult commandResult = command.runCommand(processId, commandString);

        ArrayList<String> expectedMessages = new ArrayList<>();
        expectedMessages.add("");
        expectedMessages.add(Paths.get(".").toAbsolutePath().normalize().toString() + ">echo testParameter ");
        expectedMessages.add("testParameter");

        CommandResult expectedCommandResult = new CommandResult(processId, commandString, true, expectedMessages);

        assertEquals("successful booleans of CommandResults are not identical", expectedCommandResult.isSuccessful(), commandResult.isSuccessful());
        assertEquals("Command of CommandResults are not identical", expectedCommandResult.getCommand(), commandResult.getCommand());
        assertEquals("Result messages of CommandResults are not identical", expectedCommandResult.getMessages(), commandResult.getMessages());
    }
}
