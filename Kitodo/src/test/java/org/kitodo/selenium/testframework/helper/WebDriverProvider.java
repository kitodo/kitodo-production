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

package org.kitodo.selenium.testframework.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.kitodo.ExecutionPermission;

public class WebDriverProvider {

    private static final Logger logger = LogManager.getLogger(WebDriverProvider.class);
    private static final UnArchiver zipUnArchiver = new ZipUnArchiver();
    private static final UnArchiver tarGZipUnArchiver = new TarGZipUnArchiver();

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
    public static void provideGeckoDriver(String geckoDriverVersion, String downloadFolder, String extractFolder)
            throws IOException, URISyntaxException {
        String geckoDriverUrl = "https://github.com/mozilla/geckodriver/releases/download/v" + geckoDriverVersion + "/";
        String geckoDriverFileName;
        if (SystemUtils.IS_OS_WINDOWS) {
            geckoDriverFileName = "geckodriver.exe";
            File geckoDriverZipFile = new File(downloadFolder + "geckodriver.zip");
            URL geckoUrl = new URI(geckoDriverUrl + "geckodriver-v" + geckoDriverVersion + "-win64.zip").toURL();
            logger.info("Downloading GeckoDriver for Windows from {}", geckoUrl);
            FileUtils.copyURLToFile(geckoUrl, geckoDriverZipFile);
            extractZipFileToFolder(geckoDriverZipFile, new File(extractFolder));
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            geckoDriverFileName = "geckodriver";
            File geckoDriverTarFile = new File(downloadFolder + "geckodriver.tar.gz");
            URL geckoUrl = new URI(geckoDriverUrl + "geckodriver-v" + geckoDriverVersion + "-macos.tar.gz").toURL();
            logger.info("Downloading GeckoDriver for MACOS from {}", geckoUrl);
            FileUtils.copyURLToFile(geckoUrl, geckoDriverTarFile);
            File theDir = new File(extractFolder);
            if (!theDir.mkdir()) {
                logger.error("Unable to create directory '" + theDir.getPath() + "'!");
            }
            extractTarFileToFolder(geckoDriverTarFile, theDir);
        } else {
            geckoDriverFileName = "geckodriver";
            File geckoDriverTarFile = new File(downloadFolder + "geckodriver.tar.gz");
            URL geckoUrl = new URI(geckoDriverUrl + "geckodriver-v" + geckoDriverVersion + "-linux64.tar.gz").toURL();
            logger.info("Downloading GeckoDriver for Linux from {}", geckoUrl);
            FileUtils.copyURLToFile(geckoUrl, geckoDriverTarFile);
            extractTarFileToFolder(geckoDriverTarFile, new File(extractFolder));
        }
        File geckoDriverFile = new File(extractFolder, geckoDriverFileName);

        if (geckoDriverFile.exists()) {
            if (!SystemUtils.IS_OS_WINDOWS) {
                ExecutionPermission.setExecutePermission(geckoDriverFile);
            }

            if (geckoDriverFile.canExecute()) {
                System.setProperty("webdriver.gecko.driver", geckoDriverFile.getPath());
            } else {
                logger.error("Geckodriver not executable");
            }
        } else {
            logger.error("Geckodriver file not found");
        }
    }


    private static void extractZipFileToFolder(File zipFile, File destinationFolder) {
        zipUnArchiver.setSourceFile(zipFile);
        zipUnArchiver.extract("", destinationFolder);
    }

    /**
     * Unpack the archive to specified directory.
     *
     * @param file
     *            The tar or tar.gz file.
     * @param destinationFolder
     *            The destination directory.
     */
    private static void extractTarFileToFolder(File file, File destinationFolder) {
        tarGZipUnArchiver.setSourceFile(file);
        tarGZipUnArchiver.extract("", destinationFolder);
    }

}
