/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
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

import org.apache.log4j.Logger;

/**
 * The class ShellScript is intended to run shell scripts (or other system
 * commands).
 * 
 * @author Matthias Ronge <matthias.ronge@zeutschel.de>
 */
public class ShellScript {
	private static final Logger logger = Logger.getLogger(ShellScript.class);

	public static final int ERRORLEVEL_ERROR = 1;

	private final String command;
	private LinkedList<String> outputChannel, errorChannel;
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
		if (!executable.exists())
			throw new FileNotFoundException("Could not find executable: " + executable.getAbsolutePath());
		command = executable.getAbsolutePath();
	}

	/**
	 * The function run() will execute the system command. This is a shorthand
	 * to run the script without arguments.
	 * 
	 * @throws IOException
	 *             If an I/O error occurs.
	 * @throws InterruptedException
	 *             If the current thread is interrupted by another thread while
	 *             it is waiting, then the wait is ended and an
	 *             InterruptedException is thrown.
	 */
	public int run() throws IOException, InterruptedException {
		return run(null);
	}

	/**
	 * The function run() will execute the system command. First, the call
	 * sequence is created, including the parameters passed to run(). Then, the
	 * underlying OS is contacted to run the command. Afterwards, the results
	 * are being processed and stored.
	 * 
	 * The behaviour is slightly different from the legacy callShell2() command,
	 * as it returns the error level as reported from the system process. Use
	 * this to get the old behaviour:
	 * 
	 * <pre>
	 *   Integer err = scr.run(args);
	 *   if (scr.getStdErr().size() &gt; 0) err = ShellScript.ERRORLEVEL_ERROR;
	 * </pre>
	 * 
	 * @param args
	 *            A list of arguments passed to the script. May be null.
	 * @throws IOException
	 *             If an I/O error occurs.
	 * @throws InterruptedException
	 *             If the current thread is interrupted by another thread while
	 *             it is waiting, then the wait is ended and an
	 *             InterruptedException is thrown.
	 */
	public int run(List<String> args) throws IOException, InterruptedException {

		List<String> commandLine = new ArrayList<String>();
		commandLine.add(command);
		if (args != null)
			commandLine.addAll(args);
		Process process = null;
		try {
			String[] callSequence = commandLine.toArray(new String[commandLine
					.size()]);
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
		errorLevel = process.waitFor();
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
	public static LinkedList<String> inputStreamToLinkedList(
			InputStream myInputStream) {
		LinkedList<String> result = new LinkedList<String>();
		Scanner inputLines = null;
		try {
			inputLines = new Scanner(myInputStream);
			while (inputLines.hasNextLine()) {
				String myLine = inputLines.nextLine();
				result.add(myLine);
			}
		} finally {
			if (inputLines != null)
				inputLines.close();
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
		if (inputStream == null)
			return;
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
	 * @throws InterruptedException
	 *             In case the script was interrupted due to concurrency
	 * @throws IOException
	 *             If an I/O error happens
	 */
	public static int legacyCallShell2(String nonSpacesafeScriptingCommand)
			throws IOException, InterruptedException {
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
			Helper.setFehlerMeldung(
					"Couldn't find script file in callShell2(), error",
					e.getMessage());
		}
		return err;
	}
}