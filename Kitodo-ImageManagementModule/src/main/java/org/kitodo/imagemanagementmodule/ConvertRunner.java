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

package org.kitodo.imagemanagementmodule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.im4java.core.IMOperation;
import org.kitodo.config.Config;

/**
 * Executes the {@code convert} command.
 */
class ConvertRunner {
    private static final Logger logger = LogManager.getLogger(ConvertRunner.class);

    /**
     * {@code convert} command without path.
     */
    private static final String CONVERT_COMMAND = "convert";

    /**
     * Default timeout of two hours.
     */
    private static final int TWO_HOURS = 7200;

    /**
     * {@code convert} command, optionally with full path.
     */
    private String convertCommand = CONVERT_COMMAND;

    /**
     * Executes the ImageMagick command using Apache Commons Exec.
     *
     * @param commandLine
     *            command line to execute
     * @throws IOException
     *             if I/O fails
     */
    void run(IMOperation commandLine) throws IOException {
        Executor executor = new DefaultExecutor();
        OutputStream stdout = new ByteArrayOutputStream();
        OutputStream stderr = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(stdout, stderr));
        long timeoutMillis = Config.getIntParameter("ImageManagementModule.timeoutSec", TWO_HOURS) * 1000;
        executor.setWatchdog(new ExecuteWatchdog(timeoutMillis));
        CommandLine command = new CommandLine(convertCommand);
        command.addArguments(commandLine.toString());
        boolean exception = false;
        try {
            logger.debug("Executing: {}", command);
            logger.trace("Timeout: {} mins", timeoutMillis / 60000d);
            executor.execute(command);
        } catch (IOException | RuntimeException e) {
            exception = true;
            throw e;
        } finally {
            String output = stdout.toString();
            if (!output.isEmpty()) {
                logger.log(exception ? Level.ERROR : Level.TRACE, "Command output:{}{}", System.lineSeparator(),
                    output);
            }
            String errorOutput = stderr.toString();
            if (!errorOutput.isEmpty()) {
                logger.log(exception ? Level.ERROR : Level.DEBUG, "Error output:{}{}", System.lineSeparator(),
                    errorOutput);
            }
        }
    }

    /**
     * Set the search path. This can either be the directory, or the name of the
     * executable.
     *
     * @param path
     *            the path
     */
    void setSearchPath(String path) {
        if (!new File(path).exists()) {
            throw new IllegalArgumentException("path must exist: " + path);
        } else if (new File(path).isDirectory()) {
            convertCommand = FilenameUtils.concat(path, CONVERT_COMMAND);
        } else if (new File(path).canExecute()) {
            convertCommand = path;
        } else {
            throw new IllegalArgumentException("path must either be a directory, or an executable: " + path);
        }
    }
}
