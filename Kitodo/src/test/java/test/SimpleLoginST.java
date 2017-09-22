package test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
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
import org.kitodo.MockDatabase;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;

public class SimpleLoginST {
    private static String pw = "kitodo@selenium";
    private static final Logger logger = LogManager.getLogger(SimpleLoginST.class);
    private static WebDriver driver;
    private static final String userName = "kowal";
    private static final String userPassword = "test";

    private static final int BUFFER_SIZE = 8 * 1024;

    private static final String TRAVIS_BUILD_NUMBER = "TRAVIS_BUILD_NUMBER";
    private static final String TRAVIS_BRANCH = "TRAVIS_BRANCH";
    private static final String TRAVIS_REPO_SLUG = "TRAVIS_REPO_SLUG";
    private static final String TRAVIS_BUILD_ID = "TRAVIS_BUILD_ID";

    @BeforeClass
    public static void setUp() throws Exception {
        String userDir = System.getProperty("user.dir");
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.startDatabaseServer();
        provideGeckoDriver("0.18.0", userDir + "/target/downloads/", userDir + "/target/extracts/");
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }
//
    @AfterClass
    public static void tearDown() throws Exception {
        // WebElement LogoutButton = driver.findElement(By.id("loginform:logout"));
        // LogoutButton.click();
        driver.close();
        MockDatabase.stopDatabaseServer();
        MockDatabase.stopNode();

        if (SystemUtils.IS_OS_WINDOWS){
            // Necessary for Firefox ESR
            Runtime.getRuntime().exec("taskkill /F /IM geckodriver.exe");
        }
    }

    @Rule
    public TestRule seleniumExceptionWatcher = new TestWatcher() {

        @Override
        protected void failed(Throwable ex, Description description) {
            if ("true".equals(System.getenv().get("TRAVIS")) //make sure we are on travis-ci
                    && (ex instanceof WebDriverException || ex instanceof NoSuchElementException)) { //is this a Selenium or WebDriver exception?
                try {
                    File screenshot = captureScreenShot(driver);
                    Map<String, String> ciProperties = getTravisProperties();

                    String emailSubject =
                            String.format("%s - #%s: Test Failure: %s:%s",
                                    ciProperties.get(TRAVIS_BRANCH), ciProperties.get(TRAVIS_BUILD_NUMBER),
                                    description.getDisplayName(), description.getMethodName());

                    String emailMessage =
                            String.format("Selenium Test failed on build #%s: https://travis-ci.com/%s/builds/%s",
                                    ciProperties.get(TRAVIS_BUILD_NUMBER),
                                    ciProperties.get(TRAVIS_REPO_SLUG),
                                    ciProperties.get(TRAVIS_BUILD_ID));

                    sendEmail(emailSubject, emailMessage, screenshot);
                } catch (Exception mailException) {
                    logger.error("Unable to send screenshot", mailException);
                }

            }
            super.failed(ex, description);
        }

        // Travis places build information as Environment Variables
        private Map<String, String> getTravisProperties() {
            Map<String, String> properties = new HashMap<>();
            properties.put(TRAVIS_BRANCH, System.getenv().get(TRAVIS_BRANCH));
            properties.put(TRAVIS_BUILD_ID, System.getenv().get(TRAVIS_BUILD_ID));
            properties.put(TRAVIS_BUILD_NUMBER, System.getenv().get(TRAVIS_BUILD_NUMBER));
            properties.put(TRAVIS_REPO_SLUG, System.getenv().get(TRAVIS_REPO_SLUG));
            return properties;
        }
    };
//
    @Test
    public void seleniumTest() throws Exception {

        String appUrl = "http://localhost:8080/kitodo";

        // launch the firefox browser and open the application url
        driver.get(appUrl);


        WebElement username = driver.findElement(By.id("login"));
        Thread.sleep(2000);

        username.clear();
        username.sendKeys(userName);
        Thread.sleep(2000);

        WebElement password = driver.findElement(By.id("passwort"));
        password.clear();
        password.sendKeys(userPassword);
        Thread.sleep(2000);
        WebElement LoginButton = driver.findElement(By.linkText("Einloggen"));

        //((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", LoginButton);
        Thread.sleep(2000);
        LoginButton.click();
        Thread.sleep(2000);

//        String message = driver.findElement(By.id("loginform:messlogin")).getText();
//        Assert.assertEquals("Fehler bei Authentifizierung",message);
        //Assert.assertNotNull(failText);


        WebElement VorgaengeButton = driver.findElement(By.linkText("Vorgänge"));
        VorgaengeButton.click();
        Thread.sleep(2000);

        WebElement RulesetsButton = driver.findElement(By.linkText("Regelsätze"));
        RulesetsButton.click();
        Thread.sleep(2000);

        WebElement LogoutButton = driver.findElement(By.id("loginform:logout"));
        Assert.assertNotNull(LogoutButton);

        Thread.sleep(2000);

    }

    public void sendEmail(String subject, String message, File attachedFile) throws EmailException, AddressException {

        InternetAddress address = new InternetAddress("Tim.Boerner@slub-dresden.de");
        ArrayList<InternetAddress> addressList = new ArrayList<>();
        addressList.add(address);

        // Create the attachment
        EmailAttachment attachment = new EmailAttachment();
        attachment.setPath(attachedFile.getAbsolutePath());
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setDescription("SeleniumScreenShot");
        attachment.setName("SeleniumScreenShot");

        MultiPartEmail email = new MultiPartEmail();
        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("kitodo.dev@gmail.com",pw));
        email.setSSLOnConnect(true);
        email.setFrom("Travis CI Screenshot <kitodo.dev@gmail.com>");
        email.setSubject(subject);
        email.setMsg(message);
        email.setTo(addressList);
        email.attach(attachment);

        email.send();
    }

    public static File captureScreenShot(WebDriver driver){

        File src= ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        File screenshotFile = new File(System.getProperty("user.dir")+"/target/Selenium/"+"screen.png");
        try {
            FileUtils.copyFile(src, screenshotFile);
        }

        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        return screenshotFile;
    }


    private static void setExecutePermission(File file) throws IOException {
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        perms.add(PosixFilePermission.OTHERS_READ);
        perms.add(PosixFilePermission.OTHERS_WRITE);
        perms.add(PosixFilePermission.OTHERS_EXECUTE);

        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);

        Files.setPosixFilePermissions(file.toPath(), perms);
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
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(zip.getInputStream(entry));
                    int currentByte;
                    byte data[] = new byte[BUFFER_SIZE];

                    FileOutputStream fileOutputStream = new FileOutputStream(destFile);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);

                    while ((currentByte = bufferedInputStream.read(data, 0, BUFFER_SIZE)) != -1) {
                        bufferedOutputStream.write(data, 0, currentByte);
                    }
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    bufferedInputStream.close();
                }
            }
        } catch (Exception e) {
            System.out.println("ERROR: " + e.getMessage());
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
        FileInputStream fileInputStream = null;
        TarInputStream tarArchiveInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            tarArchiveInputStream = (isGZipped) ? new TarInputStream(new GZIPInputStream(fileInputStream, BUFFER_SIZE))
                    : new TarInputStream(new BufferedInputStream(fileInputStream, BUFFER_SIZE));
            unTar(tarArchiveInputStream, outputDir);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally {
            if (tarArchiveInputStream != null) {
                try {
                    tarArchiveInputStream.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            } else if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
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
                    BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                    try {
                        int length;
                        byte data[] = new byte[BUFFER_SIZE];
                        while ((length = tarInputStream.read(data, 0, BUFFER_SIZE)) != -1) {
                            out.write(data, 0, length);
                        }
                        out.flush();
                    } finally {
                        try {
                            out.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage());
                        }
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
                setExecutePermission(geckoDriverFile);
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
