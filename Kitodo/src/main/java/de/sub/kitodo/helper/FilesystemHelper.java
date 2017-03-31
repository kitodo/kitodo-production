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

package de.sub.kitodo.helper;

import de.sub.kitodo.config.ConfigCore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Helper class for file system operations.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class FilesystemHelper {
    private static final Logger logger = Logger.getLogger(FilesystemHelper.class);

    /**
     * Creates a directory with a name given. Under Linux a script is used to
     * set the file system permissions accordingly. This cannot be done from
     * within java code before version 1.7.
     *
     * @param dirName
     *            Name of directory to create
     * @throws InterruptedException
     *             If the thread running the script is interrupted by another
     *             thread while it is waiting, then the wait is ended and an
     *             InterruptedException is thrown.
     * @throws IOException
     *             If an I/O error occurs.
     */

    public static void createDirectory(String dirName) throws IOException, InterruptedException {
        if (!new File(dirName).exists()) {
            ShellScript createDirScript = new ShellScript(new File(ConfigCore.getParameter("script_createDirMeta")));
            createDirScript.run(Arrays.asList(new String[] {dirName }));
        }
    }

    /**
     * Creates a directory with a name given and assigns permissions to the
     * given user. Under Linux a script is used to set the file system
     * permissions accordingly. This cannot be done from within java code before
     * version 1.7.
     *
     * @param dirName
     *            Name of directory to create
     * @throws InterruptedException
     *             If the thread running the script is interrupted by another
     *             thread while it is waiting, then the wait is ended and an
     *             InterruptedException is thrown.
     * @throws IOException
     *             If an I/O error occurs.
     */

    public static void createDirectoryForUser(String dirName, String userName)
            throws IOException, InterruptedException {
        if (!new File(dirName).exists()) {
            ShellScript createDirScript = new ShellScript(
                    new File(ConfigCore.getParameter("script_createDirUserHome")));
            createDirScript.run(Arrays.asList(new String[] {userName, dirName }));
        }
    }

    /**
     * Delete sym link.
     *
     * @param symLink
     *            String
     */
    public static void deleteSymLink(String symLink) {
        String command = ConfigCore.getParameter("script_deleteSymLink");
        ShellScript deleteSymLinkScript;
        try {
            deleteSymLinkScript = new ShellScript(new File(command));
            deleteSymLinkScript.run(Arrays.asList(new String[] {symLink }));
        } catch (FileNotFoundException e) {
            logger.error("FileNotFoundException in deleteSymLink()", e);
            Helper.setFehlerMeldung("Couldn't find script file, error", e.getMessage());
        } catch (IOException e) {
            logger.error("IOException in deleteSymLink()", e);
            Helper.setFehlerMeldung("Aborted deleteSymLink(), error", e.getMessage());
        }
    }

    /**
     * This function implements file renaming. Renaming of files is full of
     * mischief under Windows which unaccountably holds locks on files.
     * Sometimes running the JVM’s garbage collector puts things right.
     *
     * @param oldFileName
     *            File to move or rename
     * @param newFileName
     *            New file name / destination
     * @throws IOException
     *             is thrown if the rename fails permanently
     * @throws FileNotFoundException
     *             is thrown if old file (source file of renaming) does not
     *             exists
     */
    public static void renameFile(String oldFileName, String newFileName) throws IOException {
        final int SLEEP_INTERVAL_MILLIS = 20;
        final int MAX_WAIT_MILLIS = 150000; // 2½ minutes
        File oldFile;
        File newFile;
        boolean success;
        int millisWaited = 0;

        if ((oldFileName == null) || (newFileName == null)) {
            return;
        }

        oldFile = new File(oldFileName);
        newFile = new File(newFileName);

        if (!oldFile.exists()) {
            if (logger.isDebugEnabled()) {
                logger.debug("File " + oldFileName + " does not exist for renaming.");
            }
            throw new FileNotFoundException(oldFileName + " does not exist for renaming.");
        }

        if (newFile.exists()) {
            String message = "Renaming of " + oldFileName + " into " + newFileName + " failed: Destination exists.";
            logger.error(message);
            throw new IOException(message);
        }

        do {
            if (SystemUtils.IS_OS_WINDOWS && millisWaited == SLEEP_INTERVAL_MILLIS) {
                if (logger.isEnabledFor(Level.WARN)) {
                    logger.warn("Renaming " + oldFileName
                            + " failed. This is Windows. Running the garbage collector may yield good results. Forcing immediate garbage collection now!");
                }
                System.gc();
            }
            success = oldFile.renameTo(newFile);
            if (!success) {
                if (millisWaited == 0 && logger.isInfoEnabled()) {
                    logger.info("Renaming " + oldFileName + " failed. File may be locked. Retrying...");
                }
                try {
                    Thread.sleep(SLEEP_INTERVAL_MILLIS);
                } catch (InterruptedException e) {
                }
                millisWaited += SLEEP_INTERVAL_MILLIS;
            }
        } while (!success && millisWaited < MAX_WAIT_MILLIS);

        if (!success) {
            logger.error("Rename " + oldFileName + " failed. This is a permanent error. Giving up.");
            throw new IOException("Renaming of " + oldFileName + " into " + newFileName + " failed.");
        }

        if (millisWaited > 0 && logger.isInfoEnabled()) {
            logger.info("Rename finally succeeded after" + Integer.toString(millisWaited) + " milliseconds.");
        }
    }
}
