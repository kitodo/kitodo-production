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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.kitodo.ExecutionPermission;

public class WebDriverProvider {

    private static final Logger logger = LogManager.getLogger(WebDriverProvider.class);

    // https://sites.google.com/a/chromium.org/chromedriver/downloads/version-selection
    // Please don't rely on the LATEST_RELEASE file without a version suffix.
    // It exists for backward compatibility only, and will be removed in the near future.
    private static final String CHROME_DRIVER_LATEST_RELEASE_URL = "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_101";

    private static UnArchiver zipUnArchiver = new ZipUnArchiver();
    private static UnArchiver tarGZipUnArchiver = new TarGZipUnArchiver();

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
            extractTarFileToFolder(geckoDriverTarFile, theDir);
        } else {
            geckoDriverFileName = "geckodriver";
            File geckoDriverTarFile = new File(downloadFolder + "geckodriver.tar.gz");
            FileUtils.copyURLToFile(new URL(geckoDriverUrl + "geckodriver-v" + geckoDriverVersion + "-linux64.tar.gz"),
                    geckoDriverTarFile);
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

    /**
     * Downloads chrome driver, extracts archive file and set system property
     * "webdriver.chrome.driver". On Linux the method also sets executable
     * permission.
     *  @param downloadFolder
     *            The folder in which the downloaded files will be put in.
     *  @param extractFolder
     *            The folder in which the extracted files will be put in.
     */
    public static void provideChromeDriver(String downloadFolder, String extractFolder)
            throws IOException {

        String chromeDriverVersion = fetchLatestStableChromeDriverVersion();

        String chromeDriverUrl = "https://chromedriver.storage.googleapis.com/" + chromeDriverVersion + "/";
        String chromeDriverFileName;
        if (SystemUtils.IS_OS_WINDOWS) {
            chromeDriverFileName = "chromedriver.exe";
            File chromeDriverZipFile = new File(downloadFolder + "chromedriver.zip");
            FileUtils.copyURLToFile(new URL(chromeDriverUrl + "chromedriver_win32.zip"), chromeDriverZipFile);
            extractZipFileToFolder(chromeDriverZipFile, new File(extractFolder));

        } else if (SystemUtils.IS_OS_MAC_OSX) {
            chromeDriverFileName = "chromedriver";
            File chromeDriverZipFile = new File(downloadFolder + "chromedriver.zip");
            FileUtils.copyURLToFile(new URL(chromeDriverUrl + "chromedriver_mac64.zip"), chromeDriverZipFile);
            File theDir = new File(extractFolder);
            if (!theDir.exists()) {
                theDir.mkdir();
            }
            extractZipFileToFolder(chromeDriverZipFile, new File(extractFolder));

        } else {
            chromeDriverFileName = "chromedriver";
            File chromeDriverZipFile = new File(downloadFolder + "chromedriver.zip");
            FileUtils.copyURLToFile(new URL(chromeDriverUrl + "chromedriver_linux64.zip"), chromeDriverZipFile);
            extractZipFileToFolder(chromeDriverZipFile, new File(extractFolder));
        }
        File chromeDriverFile = new File(extractFolder, chromeDriverFileName);

        if (chromeDriverFile.exists()) {
            if (!SystemUtils.IS_OS_WINDOWS) {
                ExecutionPermission.setExecutePermission(chromeDriverFile);
            }

            if (chromeDriverFile.canExecute()) {
                System.setProperty("webdriver.chrome.driver", chromeDriverFile.getPath());
            } else {
                logger.error("Chromedriver not executable");
            }
        } else {
            logger.error("Chromedriver file not found");
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

    private static String fetchLatestStableChromeDriverVersion() {

        String version = "";
        try {

            URL url = new URL(CHROME_DRIVER_LATEST_RELEASE_URL);
            URLConnection urlConnection = url.openConnection();

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            StringBuilder content = new StringBuilder();
            content.append(bufferedReader.readLine());

            bufferedReader.close();

            version = content.toString();

            logger.info("Latest Chrome Driver Release found: {}", version);

        } catch (MalformedURLException exception) {
            logger.error("URL for fetching Chrome Release is malformed.");
        } catch (IOException exception) {
            logger.error("Failed to fetch latest Chrome Driver Release");
        }

        return version;
    }
}
