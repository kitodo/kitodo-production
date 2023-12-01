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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.api.command.CommandResult;
import org.apache.commons.lang3.SystemUtils;

public class CommandTest {
    private static String scriptExtension;
    private static boolean windows = false;
    private static File workingScript = new File(
            System.getProperty("user.dir") + "/src/test/resources/working_script.sh");
    private static File workingScriptWithParameters = new File(
            System.getProperty("user.dir") + "/src/test/resources/working_script_with_parameters.sh");
    private static File notWorkingScript = new File(
            System.getProperty("user.dir") + "/src/test/resources/not_working_script.sh");

    @BeforeClass
    public static void setUp() throws IOException {

        if (SystemUtils.IS_OS_WINDOWS) {
            scriptExtension = ".bat";
            windows = true;
        } else {
            scriptExtension = ".sh";

            setFileExecuteable(workingScript);
            setFileExecuteable(workingScriptWithParameters);
            setFileExecuteable(notWorkingScript);
        }

    }

    @AfterClass
    public static void tearDown() throws IOException {
        if (!windows) {
            setFileNotExecuteable(workingScript);
            setFileNotExecuteable(workingScriptWithParameters);
            setFileNotExecuteable(notWorkingScript);
        }
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

        assertEquals("successful booleans of CommandResults are not identical", expectedCommandResult.isSuccessful(),
                commandResult.isSuccessful());
        assertEquals("Command of CommandResults are not identical", expectedCommandResult.getCommand(),
                commandResult.getCommand());
        assertEquals("Result messages of CommandResults are not identical", expectedCommandResult.getMessages(),
                commandResult.getMessages());
    }

    @Test
    public void shouldNotRunNotExistingCommand() {
        Command command = new Command();

        String commandString = "src/test/resources/notExistingScript" + scriptExtension;
        CommandResult commandResult = command.runCommand(commandString);

        CommandResult expectedCommandResult = new CommandResult(commandString, false, null);

        assertEquals("Should not run not existing Command", expectedCommandResult.isSuccessful(),
                commandResult.isSuccessful());
    }

    @Test
    public void shouldNotRunCommandWithFalseSyntax() {
        Command command = new Command();

        String commandString = "src/test/resources/not_working_script" + scriptExtension;
        CommandResult commandResult = command.runCommand(commandString);

        CommandResult expectedCommandResult = new CommandResult(commandString, false, null);

        assertEquals("Should not run command with false syntax", expectedCommandResult.isSuccessful(),
                commandResult.isSuccessful());
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

        assertEquals("successful booleans of CommandResults are not identical", expectedCommandResult.isSuccessful(),
                commandResult.isSuccessful());
        assertEquals("Command of CommandResults are not identical", expectedCommandResult.getCommand(),
                commandResult.getCommand());
        assertEquals("Result messages of CommandResults are not identical", expectedCommandResult.getMessages(),
                commandResult.getMessages());
    }

    private static void setFileExecuteable(File file) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();

        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);

        Files.setPosixFilePermissions(file.toPath(), perms);
    }

    private static void setFileNotExecuteable(File file) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();

        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);

        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);

        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);

        Files.setPosixFilePermissions(file.toPath(), perms);
    }
}
