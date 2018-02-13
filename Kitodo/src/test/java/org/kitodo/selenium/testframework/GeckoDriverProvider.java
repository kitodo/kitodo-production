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

package org.kitodo.selenium.testframework;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.ExecutionPermission;
import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;

public class GeckoDriverProvider {

    private static final Logger logger = LogManager.getLogger(GeckoDriverProvider.class);
    private static final int BUFFER_SIZE = 8 * 1024;

    /**
     * Downloads Geckodriver, extracts archive file and set system property
     * "webdriver.gecko.driver". On Linux the method also sets executable
     * permission.
     *
     * @param geckoDriverVersion
     *            The geckodriver version.
     * @param downloadFolder
     *            The folder in which the downloaded files will be put in.
     * @param extractFolder
     *            The folder in which the extracted files will be put in.
     */
    public static void provide(String geckoDriverVersion, String downloadFolder, String extractFolder)
            throws IOException {
        String geckoDriverUrl = "https://github.com/mozilla/geckodriver/releases/download/v" + geckoDriverVersion + "/";
        String geckoDriverFileName;
        if (SystemUtils.IS_OS_WINDOWS) {
            geckoDriverFileName = "geckodriver.exe";
            File geckoDriverZipFile = new File(downloadFolder + "geckodriver.zip");
            FileUtils.copyURLToFile(new URL(geckoDriverUrl + "geckodriver-v" + geckoDriverVersion + "-win64.zip"),
                geckoDriverZipFile);
            extractZipFileToFolder(geckoDriverZipFile, new File(extractFolder));
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            geckoDriverFileName = "geckodriver";
            File geckoDriverTarFile = new File(downloadFolder + "geckodriver.tar.gz");
            FileUtils.copyURLToFile(new URL(geckoDriverUrl + "geckodriver-v" + geckoDriverVersion + "-macos.tar.gz"),
                geckoDriverTarFile);
            File theDir = new File(extractFolder);
            if (!theDir.exists()) {
                theDir.mkdir();
            }
            extractTarFileToFolder(geckoDriverTarFile, theDir, true);
        } else {
            geckoDriverFileName = "geckodriver";
            File geckoDriverTarFile = new File(downloadFolder + "geckodriver.tar.gz");
            FileUtils.copyURLToFile(new URL(geckoDriverUrl + "geckodriver-v" + geckoDriverVersion + "-linux64.tar.gz"),
                geckoDriverTarFile);
            extractTarFileToFolder(geckoDriverTarFile, new File(extractFolder), true);
        }
        File geckoDriverFile = new File(extractFolder, geckoDriverFileName);

        if (geckoDriverFile.exists()) {
            if (!SystemUtils.IS_OS_WINDOWS) {
                ExecutionPermission.setExecutePermission(geckoDriverFile);
            }

            if (geckoDriverFile.canExecute()) {
                System.setProperty("webdriver.gecko.driver", geckoDriverFile.getPath());
            } else {
                logger.error("Geckodriver not executeable");
            }
        } else {
            logger.error("Geckodriver file not found");
        }
    }

    private static void extractZipFileToFolder(File zipFile, File destinationFolder) {
        try {
            ZipFile zip = new ZipFile(zipFile);

            destinationFolder.mkdir();
            Enumeration zipFileEntries = zip.entries();

            while (zipFileEntries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
                String currentEntry = entry.getName();

                File destFile = new File(destinationFolder, currentEntry);
                File destinationParent = destFile.getParentFile();

                destinationParent.mkdirs();

                if (!entry.isDirectory()) {
                    int currentByte;
                    byte data[] = new byte[BUFFER_SIZE];

                    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(zip.getInputStream(entry));
                            FileOutputStream fileOutputStream = new FileOutputStream(destFile);
                            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream,
                                    BUFFER_SIZE)) {

                        while ((currentByte = bufferedInputStream.read(data, 0, BUFFER_SIZE)) != -1) {
                            bufferedOutputStream.write(data, 0, currentByte);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Unpack the archive to specified directory.
     *
     * @param file
     *            The tar or tar.gz file.
     * @param outputDir
     *            The destination directory.
     * @param isGZipped
     *            True if the file is gzipped.
     */
    private static void extractTarFileToFolder(File file, File outputDir, boolean isGZipped) {
        try (FileInputStream fileInputStream = new FileInputStream(file);
                TarInputStream tarArchiveInputStream = (isGZipped)
                        ? new TarInputStream(new GZIPInputStream(fileInputStream, BUFFER_SIZE))
                        : new TarInputStream(new BufferedInputStream(fileInputStream, BUFFER_SIZE))) {

            unTar(tarArchiveInputStream, outputDir);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Unpack data from the stream to specified directory.
     *
     * @param tarInputStream
     *            Stream with tar data.
     * @param outputDir
     *            The destination directory.
     */
    private static void unTar(TarInputStream tarInputStream, File outputDir) {
        try {
            TarEntry tarEntry;
            while ((tarEntry = tarInputStream.getNextEntry()) != null) {
                final File file = new File(outputDir, tarEntry.getName());
                if (tarEntry.isDirectory()) {
                    if (!file.exists()) {
                        if (!file.mkdirs()) {
                            logger.error(file + " failure to create directory");
                        }
                    }
                } else {
                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                        int length;
                        byte data[] = new byte[BUFFER_SIZE];
                        while ((length = tarInputStream.read(data, 0, BUFFER_SIZE)) != -1) {
                            out.write(data, 0, length);
                        }
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

}
