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

package org.kitodo.imagemanagement;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.im4java.core.IMOperation;
import org.kitodo.config.KitodoConfig;
import org.kitodo.config.enums.ParameterImageManagement;

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
     * Randomness generator used to distribute the requests evenly on several
     * configured SSH hosts.
     */
    private static final Random RANDOMNESS_GENERATOR = new Random();

    /**
     * Default timeout.
     */
    private static final int DEFAULT_TIMEOUT_MINS = (int) TimeUnit.MINUTES.convert(2, TimeUnit.HOURS);

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

        OutputStream outAndErr = new ByteArrayOutputStream();
        executor.setStreamHandler(new PumpStreamHandler(outAndErr));

        long timeoutMillis = 1000 * KitodoConfig.getIntParameter(ParameterImageManagement.TIMEOUT_SEC, DEFAULT_TIMEOUT_MINS);
        executor.setWatchdog(new ExecuteWatchdog(timeoutMillis));

        CommandLine command;
        try {
            String sshHosts = KitodoConfig.getParameter(ParameterImageManagement.SSH_HOST);
            command = new CommandLine("ssh");
            String[] hosts = sshHosts.split(",");
            String host = hosts[RANDOMNESS_GENERATOR.nextInt(hosts.length)];
            command.addArgument(host, false);
            command.addArgument(convertCommand + ' ' + commandLine.toString(), false);
        } catch (NoSuchElementException e) {
            logger.trace("SSH not configured.", e);
            command = new CommandLine(convertCommand);
            command.addArguments(commandLine.toString(), false);
        }

        try {
            logger.debug("Executing: {}", command);
            logger.trace("Timeout: {} mins", timeoutMillis / 60000d);
            executor.execute(command);
            logger.debug("Command output:{}{}", System.lineSeparator(), outAndErr.toString());
        } catch (IOException | RuntimeException e) {
            logger.error("Command output:{}{}", System.lineSeparator(), outAndErr.toString());
            throw e;
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
