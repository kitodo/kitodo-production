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

import org.apache.commons.lang.SystemUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.kitodo.api.command.CommandResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class CommandServiceTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String scriptExtention;

    @Before
    public void setScriptExtensionByOS() {

        if (SystemUtils.IS_OS_WINDOWS) {
            scriptExtention = ".bat";
        }

        if (SystemUtils.IS_OS_LINUX) {
            scriptExtention = ".sh";
        }
        logger.info("testLogging");
    }

    @Test
    public void runScriptWithString() {
        String commandString = "src/test/resources/working_script" + scriptExtention;
        CommandService service = new CommandService();
        CommandResult result = service.runCommand(commandString);

        ArrayList<String> expectedMessages = new ArrayList<>();
        expectedMessages.add("");
        expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo Hello World ");
        expectedMessages.add("Hello World");

        assertEquals("result messages are not identical",expectedMessages,result.getMessages());
    }

    @Test
    public void runScriptWithFile() throws IOException, InterruptedException {
        String commandString = "src/test/resources/working_script" + scriptExtention;
        File file = new File(commandString);
        CommandService service = new CommandService();
        CommandResult result = service.runCommand(file);

        ArrayList<String> expectedMessages = new ArrayList<>();
        expectedMessages.add("");
        expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo Hello World ");
        expectedMessages.add("Hello World");

        assertEquals("result messages are not identical",expectedMessages,result.getMessages());
        assertEquals("successful booleans are not matching",true,result.isSuccessful());
    }

    @Test
    public void runScriptParameters() throws IOException, InterruptedException {
        String commandString = "src/test/resources/working_script_with_parameters" + scriptExtention;
        File file = new File(commandString);
        List<String> parameter = new ArrayList<>();
        parameter.add("HelloWorld");
        CommandService service = new CommandService();
        CommandResult result = service.runCommand(file,parameter);

        ArrayList<String> expectedMessages = new ArrayList<>();
        expectedMessages.add("");
        expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo HelloWorld ");
        expectedMessages.add("HelloWorld");

        assertEquals("result messages are not identical",expectedMessages,result.getMessages());
        assertEquals("successful booleans are not matching",true,result.isSuccessful());
    }

    @Test
    public void runScriptAsync() throws InterruptedException {
        String commandString = "src/test/resources/working_script" + scriptExtention;
        CommandService service = new CommandService();
        service.runCommandAsync(commandString);
        Thread.sleep(1000); //wait for async thread to finish;
        CommandResult commandResult = service.getLastFinishedCommand();
        assertEquals("path to scripts are not identical",commandResult.getCommand(),commandString);
    }

    @Test
    public void runLongScriptAsync() throws InterruptedException {
        String commandString2s = "src/test/resources/long_working_script_2s" + scriptExtention;
        String commandString1s = "src/test/resources/long_working_script_1s" + scriptExtention;
        CommandService service = new CommandService();
        service.runCommandAsync(commandString2s);
        service.runCommandAsync(commandString1s);
        Thread.sleep(3000); //wait for async thread to finish;
        CommandResult commandResult = service.getLastFinishedCommand();
        assertEquals("latest finished command should be the 2 s one",commandResult.getCommand(),commandString2s);
    }

    @Test
    public void runNotExistingScriptAsync() throws InterruptedException {
        String commandString = "src/test/resources/not_existing_script" + scriptExtention;
        CommandService service = new CommandService();
        service.runCommandAsync(commandString);
        Thread.sleep(1000); //wait for async thread to finish;
        CommandResult commandResult = service.getLastFinishedCommand();
        assertEquals("result message should contain IOException",commandResult.getMessages().get(0),"IOException");
    }

    @Test
    public void runScriptParametersAsync() throws IOException, InterruptedException {
        String commandString = "src/test/resources/working_script_with_parameters" + scriptExtention;
        File file = new File(commandString);
        List<String> parameter = new ArrayList<>();
        parameter.add("HelloWorld");
        CommandService service = new CommandService();
        service.runCommandAsync(file,parameter);
        Thread.sleep(1000); //wait for async thread to finish;
        CommandResult result = service.getLastFinishedCommand();

        ArrayList<String> expectedMessages = new ArrayList<>();
        expectedMessages.add("");
        expectedMessages.add(Paths.get("").toAbsolutePath().normalize() + ">echo HelloWorld ");
        expectedMessages.add("HelloWorld");

        assertEquals("result messages are not identical",expectedMessages,result.getMessages());
        assertEquals("successful booleans are not identical",true,result.isSuccessful());
    }
}

