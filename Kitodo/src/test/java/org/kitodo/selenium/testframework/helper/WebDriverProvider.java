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
import java.net.MalformedURLException;
import java.net.URL;

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

    // https://chromedriver.chromium.org/downloads
    private static final String CHROME_DRIVER_LAST_GOOD_VERSIONS_URL
            = "https://googlechromelabs.github.io/chrome-for-testing/last-known-good-versions.json";
    private static final String CHROME_FOR_TESTING_URL = "https://storage.googleapis.com/chrome-for-testing-public/";
    public static final String CHROME_DRIVER = "chromedriver";
    private static final String CHROME_DRIVER_MAC_PREFIX = "mac-x64";
    private static final String CHROME_DRIVER_WIN_PREFIX = "win32";
    private static final String CHROME_DRIVER_LINUX_PREFIX = "linux64";
    public static final String CHROME_DRIVER_MAC_SUBDIR = CHROME_DRIVER + "-" + CHROME_DRIVER_MAC_PREFIX;
    public static final String CHROME_DRIVER_WIN_SUBDIR = CHROME_DRIVER + "-" +  CHROME_DRIVER_WIN_PREFIX;
    public static final String CHROME_DRIVER_LINUX_SUBDIR = CHROME_DRIVER + "-" +  CHROME_DRIVER_LINUX_PREFIX;
    private static final String ZIP = ".zip";
    public static final String EXE = ".exe";
    private static final String ZIP_FILE = CHROME_DRIVER + ZIP;
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
            if (!theDir.mkdir()) {
                logger.error("Unable to create directory '" + theDir.getPath() + "'!");
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
        String chromeDriverUrl = CHROME_FOR_TESTING_URL + chromeDriverVersion + File.separator;
        String driverFilename = CHROME_DRIVER;
        File chromeDriverFile;
        if (SystemUtils.IS_OS_WINDOWS) {
            driverFilename = driverFilename + EXE;
            File chromeDriverZipFile = new File(downloadFolder + CHROME_DRIVER_WIN_SUBDIR + File.separator + ZIP_FILE);
            FileUtils.copyURLToFile(new URL(chromeDriverUrl + CHROME_DRIVER_WIN_PREFIX + File.separator
                    + CHROME_DRIVER_WIN_SUBDIR + ZIP), chromeDriverZipFile);
            chromeDriverFile = extractZipFileToFolder(chromeDriverZipFile, new File(extractFolder), driverFilename,
                    CHROME_DRIVER_WIN_SUBDIR);
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            File chromeDriverZipFile = new File(downloadFolder + CHROME_DRIVER_MAC_SUBDIR + File.separator + ZIP_FILE);
            FileUtils.copyURLToFile(new URL(chromeDriverUrl + CHROME_DRIVER_MAC_PREFIX + File.separator
                    + CHROME_DRIVER_MAC_SUBDIR + ZIP), chromeDriverZipFile);
            File theDir = new File(extractFolder);
            if (!theDir.exists()) {
                if (!theDir.mkdir()) {
                    logger.error("Unable to create directory '" + theDir.getPath() + "'!");
                }
            }
            chromeDriverFile = extractZipFileToFolder(chromeDriverZipFile, new File(extractFolder), driverFilename,
                    CHROME_DRIVER_MAC_SUBDIR);
        } else {
            File chromeDriverZipFile = new File(downloadFolder + CHROME_DRIVER_LINUX_SUBDIR + File.separator + ZIP_FILE);
            FileUtils.copyURLToFile(new URL(chromeDriverUrl + CHROME_DRIVER_LINUX_PREFIX + File.separator
                    + CHROME_DRIVER_LINUX_SUBDIR + ZIP), chromeDriverZipFile);
            chromeDriverFile = extractZipFileToFolder(chromeDriverZipFile, new File(extractFolder), driverFilename,
                    CHROME_DRIVER_LINUX_SUBDIR);
        }
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

    private static File extractZipFileToFolder(File zipFile, File destinationFolder, String chromeDriverFilename,
                                               String chromeDriverVersion) {
        extractZipFileToFolder(zipFile, destinationFolder);
        return new File(destinationFolder.toURI().resolve(chromeDriverVersion + '/')
                .resolve(chromeDriverFilename));
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
            URL url = new URL(CHROME_DRIVER_LAST_GOOD_VERSIONS_URL);
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                String content = bufferedReader.readLine();
                JsonReader jsonReader = Json.createReader(new StringReader(content));
                JsonObject jsonObject = jsonReader.readObject();
                version = jsonObject.get("channels").asJsonObject().get("Stable").asJsonObject().get("version")
                        .toString();
                version = StringUtils.strip(version, "\"");
                jsonReader.close();
                logger.info("Latest Chrome Driver Release found: {}", version);
            }
        } catch (MalformedURLException exception) {
            logger.error("URL for fetching Chrome Release is malformed.");
        } catch (IOException exception) {
            logger.error("Failed to fetch latest Chrome Driver Release");
        }

        return version;
    }
}
