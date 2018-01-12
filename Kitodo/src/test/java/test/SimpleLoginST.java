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

package test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;

public class SimpleLoginST {

    private static final Logger logger = LogManager.getLogger(SimpleLoginST.class);
    private static WebDriver driver;

    private static final String userName = "kowal";
    private static final String userPassword = "test";

    private static final int BUFFER_SIZE = 8 * 1024;

    private static final String TRAVIS_BUILD_NUMBER = "TRAVIS_BUILD_NUMBER";
    private static final String TRAVIS_BRANCH = "TRAVIS_BRANCH";
    private static final String TRAVIS_REPO_SLUG = "TRAVIS_REPO_SLUG";
    private static final String TRAVIS_BUILD_ID = "TRAVIS_BUILD_ID";
    private static final String MAIL_USER = "MAIL_USER";
    private static final String MAIL_PASSWORD = "MAIL_PASSWORD";
    private static final String MAIL_RECIPIENT = "MAIL_RECIPIENT";

    private static final String GECKO_DRIVER_VERSION = "0.19.0";

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.startDatabaseServer();

        String userDir = System.getProperty("user.dir");
        provideGeckoDriver(GECKO_DRIVER_VERSION, userDir + "/target/downloads/", userDir + "/target/extracts/");
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    }

    @AfterClass
    public static void tearDown() throws Exception {
        driver.close();
        MockDatabase.stopDatabaseServer();
        MockDatabase.stopNode();

        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                Runtime.getRuntime().exec("taskkill /F /IM geckodriver.exe");
            } catch (Exception ex) {
                logger.error(ex.getMessage());
            }
        }
    }

    /**
     * Watcher for WebDriverExceptions on travis which makes screenshot and sends email
     */
    @Rule
    public TestRule seleniumExceptionWatcher = new TestWatcher() {

        @Override
        protected void failed(Throwable ex, Description description) {
            if ("true".equals(System.getenv().get("TRAVIS")) // make sure we are on travis-ci
                    && (ex instanceof WebDriverException)) {
                try {
                    File screenshot = captureScreenShot(driver);
                    Map<String, String> travisProperties = getTravisProperties();

                    String emailSubject = String.format("%s - #%s: Test Failure: %s: %s",
                            travisProperties.get(TRAVIS_BRANCH), travisProperties.get(TRAVIS_BUILD_NUMBER),
                            description.getClassName(), description.getMethodName());

                    String emailMessage = String.format(
                            "Selenium Test failed on build #%s: https://travis-ci.org/%s/builds/%s",
                            travisProperties.get(TRAVIS_BUILD_NUMBER), travisProperties.get(TRAVIS_REPO_SLUG),
                            travisProperties.get(TRAVIS_BUILD_ID));

                    String user = travisProperties.get(MAIL_USER);
                    String password = travisProperties.get(MAIL_PASSWORD);
                    String recipient = travisProperties.get(MAIL_RECIPIENT);

                    sendEmail(user, password, emailSubject, emailMessage, screenshot, recipient);
                } catch (Exception mailException) {
                    logger.error("Unable to send screenshot", mailException);
                }
            }
            super.failed(ex, description);
        }

        private Map<String, String> getTravisProperties() {
            Map<String, String> properties = new HashMap<>();
            properties.put(TRAVIS_BRANCH, System.getenv().get(TRAVIS_BRANCH));
            properties.put(TRAVIS_BUILD_ID, System.getenv().get(TRAVIS_BUILD_ID));
            properties.put(TRAVIS_BUILD_NUMBER, System.getenv().get(TRAVIS_BUILD_NUMBER));
            properties.put(TRAVIS_REPO_SLUG, System.getenv().get(TRAVIS_REPO_SLUG));
            properties.put(MAIL_USER, System.getenv().get(MAIL_USER));
            properties.put(MAIL_PASSWORD, System.getenv().get(MAIL_PASSWORD));
            properties.put(MAIL_RECIPIENT, System.getenv().get(MAIL_RECIPIENT));
            return properties;
        }
    };

    @Test
    public void seleniumTest() throws Exception {

        String appUrl = "http://localhost:8080/kitodo";

        driver.get(appUrl);
        Thread.sleep(2000);
        driver.manage().window().setSize(new Dimension(1280, 1024));

        Thread.sleep(2000);

        WebElement username = driver.findElement(By.id("username"));

        username.clear();
        username.sendKeys(userName);

        WebElement password = driver.findElement(By.id("password"));
        password.clear();
        password.sendKeys(userPassword);
        WebElement LoginButton = driver.findElement(By.id("login"));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", LoginButton);
        Thread.sleep(200);
        LoginButton.click();
        Thread.sleep(500);

        WebElement VorgaengeButton = driver.findElement(By.linkText("Vorgänge"));
        VorgaengeButton.click();
        Thread.sleep(2000);

        WebElement RulesetsButton = driver.findElement(By.linkText("Regelsätze"));
        RulesetsButton.click();
        Thread.sleep(500);

        WebElement LogoutButton = driver.findElement(By.id("logout"));
        Assert.assertNotNull(LogoutButton);

    }

    private void sendEmail(String user, String password, String subject, String message, File attachedFile,
            String recipient) throws EmailException, AddressException {

        if (user != null && password != null && recipient != null) {
            InternetAddress address = new InternetAddress(recipient);

            ArrayList<InternetAddress> addressList = new ArrayList<>();
            addressList.add(address);

            EmailAttachment attachment = new EmailAttachment();
            if (attachedFile != null) {
                // Create the attachment
                attachment.setPath(attachedFile.getAbsolutePath());
                attachment.setDisposition(EmailAttachment.ATTACHMENT);
                attachment.setDescription("SeleniumScreenShot");
                attachment.setName("screenshot.png");
            }

            MultiPartEmail email = new MultiPartEmail();
            email.setHostName("smtp.gmail.com");
            email.setSmtpPort(465);
            email.setAuthenticator(new DefaultAuthenticator(user, password));
            email.setSSLOnConnect(true);
            email.setFrom("Travis CI Screenshot <kitodo.dev@gmail.com>");
            if (subject != null) {
                email.setSubject(subject);
            }
            if (message != null) {
                email.setMsg(message);
            }
            email.setTo(addressList);
            email.attach(attachment);
            email.send();
        }
    }

    private static File captureScreenShot(WebDriver driver) {

        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File screenshotFile = new File(System.getProperty("user.dir") + "/target/Selenium/" + "screen.png");
        try {
            FileUtils.copyFile(src, screenshotFile);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return screenshotFile;
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

                    try(BufferedInputStream bufferedInputStream = new BufferedInputStream(zip.getInputStream(entry));
                        FileOutputStream fileOutputStream = new FileOutputStream(destFile);
                        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE)) {

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
             TarInputStream tarArchiveInputStream = (isGZipped) ? new TarInputStream(new GZIPInputStream(fileInputStream, BUFFER_SIZE))
                     : new TarInputStream(new BufferedInputStream(fileInputStream, BUFFER_SIZE))){

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
    private static void provideGeckoDriver(String geckoDriverVersion, String downloadFolder, String extractFolder)
            throws IOException {
        String geckoDriverUrl = "https://github.com/mozilla/geckodriver/releases/download/v" + geckoDriverVersion + "/";
        String geckoDriverFileName;
        if (SystemUtils.IS_OS_WINDOWS) {
            geckoDriverFileName = "geckodriver.exe";
            File geckoDriverZipFile = new File(downloadFolder + "geckodriver.zip");
            FileUtils.copyURLToFile(new URL(geckoDriverUrl + "geckodriver-v" + geckoDriverVersion + "-win64.zip"),
                    geckoDriverZipFile);
            extractZipFileToFolder(geckoDriverZipFile, new File(extractFolder));
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
}
