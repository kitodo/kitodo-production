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

package de.sub.goobi.helper;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The class ShellScript is intended to run shell scripts (or other system
 * commands).
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class ShellScript {
    private static final Logger logger = LogManager.getLogger(ShellScript.class);

    public static final int ERRORLEVEL_ERROR = 1;

    private final String command;

    private LinkedList<String> outputChannel;

    private LinkedList<String> errorChannel;

    private Integer errorLevel;

    /**
     * This returns the command.
     *
     * @return the command
     */
    public File getCommand() {
        return new File(command);
    }

    /**
     * This returns the command string.
     *
     * @return the command
     */
    public String getCommandString() {
        return command;
    }

    /**
     * Provides the results of the script written on standard out. Null if the
     * script has not been run yet.
     *
     * @return the output channel
     */
    public LinkedList<String> getStdOut() {
        return outputChannel;
    }

    /**
     * Provides the content of the standard error channel. Null if the script
     * has not been run yet.
     *
     * @return the error channel
     */
    public LinkedList<String> getStdErr() {
        return errorChannel;
    }

    /**
     * Provides the result error level.
     *
     * @return the error level
     */
    public Integer getErrorLevel() {
        return errorLevel;
    }

    /**
     * A shell script must be initialised with an existing file on the local
     * file system.
     *
     * @param executable
     *            Script to run
     * @throws FileNotFoundException
     *             is thrown if the given executable does not exist.
     */
    public ShellScript(File executable) throws FileNotFoundException {
        if (!executable.exists()) {
            throw new FileNotFoundException("Could not find executable: " + executable.getAbsolutePath());
        }
        command = executable.getAbsolutePath();
    }

    /**
     * The function run() will execute the system command. This is a shorthand
     * to run the script without arguments.
     *
     * @return the exit value of the script
     * @throws IOException
     *             If an I/O error occurs.
     */
    public int run() throws IOException {
        return run(null);
    }

    /**
     * The function run() will execute the system command. First, the call
     * sequence is created, including the parameters passed to run(). Then, the
     * underlying OS is contacted to run the command. Afterwards, the results
     * are being processed and stored.
     *
     * <p>
     * On interrupt request, the function will continue waiting for the script
     * and then set interrupted state again to allow the executing thread to
     * exit gracefully where defined.
     * </p>
     *
     * <p>
     * The behaviour is slightly different from the legacy callShell2() command,
     * as it returns the error level as reported from the system process. Use
     * this to get the old behaviour:
     * </p>
     *
     * <pre>
     * Integer err = scr.run(args);
     * if (scr.getStdErr().size() &gt; 0)
     *     err = ShellScript.ERRORLEVEL_ERROR;
     * </pre>
     *
     * @param args
     *            A list of arguments passed to the script. May be null.
     * @return the exit value of the script
     * @throws IOException
     *             If an I/O error occurs.
     */
    public int run(List<String> args) throws IOException {

        List<String> commandLine = new ArrayList<String>();
        commandLine.add(command);
        if (args != null) {
            commandLine.addAll(args);
        }
        Process process = null;
        try {
            String[] callSequence = commandLine.toArray(new String[commandLine.size()]);
            process = new ProcessBuilder(callSequence).start();
            outputChannel = inputStreamToLinkedList(process.getInputStream());
            errorChannel = inputStreamToLinkedList(process.getErrorStream());
        } catch (IOException error) {
            throw new IOException(error.getMessage());
        } finally {
            if (process != null) {
                closeStream(process.getInputStream());
                closeStream(process.getOutputStream());
                closeStream(process.getErrorStream());
            }
        }
        boolean interrupted = true;
        boolean interrupt = false;
        do {
            try {
                errorLevel = process.waitFor();
                interrupted = false;
            } catch (InterruptedException e) {
                Thread.interrupted();
                interrupt = true;
            }
        } while (interrupted);
        if (interrupt) {
            Thread.currentThread().interrupt();
        }
        return errorLevel;
    }

    /**
     * The function inputStreamToLinkedList() reads an InputStream and returns
     * it as a LinkedList.
     *
     * @param myInputStream
     *            Stream to convert
     * @return A linked list holding the single lines.
     */
    public static LinkedList<String> inputStreamToLinkedList(InputStream myInputStream) {
        LinkedList<String> result = new LinkedList<String>();
        Scanner inputLines = null;
        try {
            inputLines = new Scanner(myInputStream, "UTF-8");
            while (inputLines.hasNextLine()) {
                String myLine = inputLines.nextLine();
                result.add(myLine);
            }
        } finally {
            if (inputLines != null) {
                inputLines.close();
            }
        }
        return result;
    }

    /**
     * This behaviour was already implemented. I can’t say if it’s necessary.
     *
     * @param inputStream
     *            A stream to close.
     */
    private static void closeStream(Closeable inputStream) {
        if (inputStream == null) {
            return;
        }
        try {
            inputStream.close();
        } catch (IOException e) {
            logger.warn("Could not close stream.", e);
            Helper.setFehlerMeldung("Could not close open stream.");
        }
    }

    /**
     * This implements the legacy Helper.callShell2() command. This is subject
     * to whitespace problems and is maintained here for backward compatibility
     * only. Please don’t use.
     *
     * @param nonSpacesafeScriptingCommand
     *            A single line command which mustn’t contain parameters
     *            containing white spaces.
     * @return error level on success, 1 if an error occurs
     * @throws IOException
     *             If an I/O error happens
     */
    public static int legacyCallShell2(String nonSpacesafeScriptingCommand) throws IOException {
        String[] tokenisedCommand = nonSpacesafeScriptingCommand.split("\\s");
        ShellScript s;
        int err = ShellScript.ERRORLEVEL_ERROR;
        try {
            s = new ShellScript(new File(tokenisedCommand[0]));
            ArrayList<String> scriptingArgs = new ArrayList<String>();
            for (int i = 1; i < tokenisedCommand.length; i++) {
                scriptingArgs.add(tokenisedCommand[i]);
            }
            err = s.run(scriptingArgs);
            for (String line : s.getStdOut()) {
                Helper.setMeldung(line);
            }
            if (s.getStdErr().size() > 0) {
                err = ShellScript.ERRORLEVEL_ERROR;
                for (String line : s.getStdErr()) {
                    Helper.setFehlerMeldung(line);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException in callShell2()", e);
            Helper.setFehlerMeldung("Couldn't find script file in callShell2(), error", e.getMessage());
        }
        return err;
    }
}
