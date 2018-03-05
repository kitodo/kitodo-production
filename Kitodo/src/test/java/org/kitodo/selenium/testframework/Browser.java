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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.selenium.testframework.enums.BrowserType;
import org.kitodo.selenium.testframework.helper.WebDriverProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Browser {
    private static final Logger logger = LogManager.getLogger(Browser.class);
    private static final String BASE_URL = "http://localhost:8080/kitodo/";
    private static RemoteWebDriver webDriver;
    private static Actions actions;
    private static final String GECKO_DRIVER_VERSION = "0.19.1";
    private static final String CHROME_DRIVER_VERSION = "2.35";
    private static boolean onTravis = false;
    private static BrowserType browserType = BrowserType.CHROME;

    private static int delayIndexing = 3000;
    private static int delayAfterSave = 6000;
    private static int delayAfterLogin = 2000;
    private static int delayAfterLogout = 3000;
    private static int delayAfterLinkClick = 500;
    private static int delayAfterHoverMenu = 500;
    private static int delayAfterNewItemClick = 500;
    private static int delayAfterPickListClick = 1500;
    private static int delayAfterUpdate = 3000;

    /**
     * Provides the web driver, sets timeout and window size.
     */
    public static void Initialize() throws IOException {

        if (browserType.equals(BrowserType.CHROME)) {
            provideChromeDriver();
        }
        if (browserType.equals(BrowserType.FIREFOX)) {
            provideGeckoDriver();
        }

        actions = new Actions(webDriver);
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        goTo("");
        webDriver.manage().window().setSize(new Dimension(1280, 1024));

        if ("true".equals(System.getenv().get("TRAVIS"))) {
            logger.debug("TRAVIS environment detected");
            onTravis = true;
        }
    }

    private static void provideChromeDriver() throws IOException {
        String userDir = System.getProperty("user.dir");
        String driverFilePath = userDir + "/target/extracts/";
        String driverFileName = "";
        driverFileName = "chromedriver";
        if (SystemUtils.IS_OS_WINDOWS) {
            driverFileName = driverFileName.concat(".exe");
        }
        File driverFile = new File(driverFilePath + driverFileName);

        if (!driverFile.exists()) {
            logger.debug(driverFile.getAbsolutePath() + " does not exist, providing chrome driver now");
            WebDriverProvider.provideChromeDriver(CHROME_DRIVER_VERSION, userDir + "/target/downloads/",
                userDir + "/target/extracts/");
        }
        webDriver = new ChromeDriver();
    }

    private static void provideGeckoDriver() throws IOException {
        String userDir = System.getProperty("user.dir");
        String driverFilePath = userDir + "/target/extracts/";
        String driverFileName = "";
        driverFileName = "geckodriver";
        if (SystemUtils.IS_OS_WINDOWS) {
            driverFileName = driverFileName.concat(".exe");
        }
        File driverFile = new File(driverFilePath + driverFileName);
        if (!driverFile.exists()) {
            WebDriverProvider.provideGeckoDriver(GECKO_DRIVER_VERSION, userDir + "/target/downloads/",
                userDir + "/target/extracts/");
        }
        webDriver = new FirefoxDriver();
    }

    /**
     * Gets current url.
     *
     * @return The current url.
     */
    public static String getCurrentUrl() {
        return webDriver.getCurrentUrl();
    }

    /**
     * Gets the driver for sending remote commands to the browser.
     *
     * @return The Web Driver.
     */
    public static RemoteWebDriver getDriver() {
        return webDriver;
    }

    /**
     * Can be used give an url to which the browser navigates.
     *
     * @param url
     *            The url as String.
     */
    public static void goTo(String url) {
        webDriver.get(BASE_URL + url);
    }

    /**
     * Closed the browser and quits the web driver.
     */
    public static void close() {
        webDriver.close();
        webDriver.quit();
    }

    /**
     * Hovers the given web element.
     *
     * @param webElement
     *            The web element.
     */
    public static void hoverWebElement(WebElement webElement) throws InterruptedException {
        actions.moveToElement(webElement).pause(delayAfterHoverMenu).build().perform();
    }

    /**
     * Scrolls to a given web element.
     *
     * @param webElement
     *            The web element
     */
    public static void scrollWebElementIntoView(WebElement webElement) {
        webDriver.executeScript("arguments[0].scrollIntoView();", webElement);
    }

    public static File captureScreenShot() {

        File src = webDriver.getScreenshotAs(OutputType.FILE);
        File screenshotFile = new File(System.getProperty("user.dir") + "/target/Selenium/" + "screen.png");
        try {
            FileUtils.copyFile(src, screenshotFile);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return screenshotFile;
    }

    /**
     * Click a save button that is disabled and enabled via Ajax, resulting in a
     * small delay before its 'disabled' state is updated on the page. For that
     * reason this function incorporates a short delay to allow the button to become
     * enabled properly.
     *
     * @param webElement
     *            the save button to be clicked
     * @throws InterruptedException
     *             when the thread gets interrupted
     */
    public static void clickAjaxSaveButton(WebElement webElement) throws InterruptedException {
        for (int attempt = 1; attempt < 4; attempt++) {
            try {
                Thread.sleep(Browser.getDelayAfterUpdate());
                webElement.click();
                return;
            } catch (StaleElementReferenceException e) {
                logger.error("Save button is not accessible, retry now (" + attempt + ". attempt)");
            }
        }
        throw new StaleElementReferenceException("Could not access save button!");
    }

    public static List<WebElement> getRowsOfTable(WebElement table) {
        return table.findElements(By.tagName("tr"));
    }

    public static List<WebElement> getCellsOfRow(WebElement row) {
        return row.findElements(By.tagName("td"));
    }

    public static List<String> getTableDataByColumn(WebElement table, int columnIndex) {
        List<WebElement> rows = getRowsOfTable(table);
        List<String> data = new ArrayList<>();
        for (WebElement row : rows) {
            data.add(getCellDataByRow(row, columnIndex));
        }
        return data;
    }

    public static String getCellDataByRow(WebElement row, int columnIndex) {
        List<WebElement> cells = getCellsOfRow(row);
        return cells.get(columnIndex).getText();
    }

    /**
     * Gets delayAfterSave.
     *
     * @return The delayAfterSave.
     */
    public static int getDelayAfterSave() {
        return delayAfterSave;
    }

    /**
     * Gets delayAfterLogin.
     *
     * @return The delayAfterLogin.
     */
    public static int getDelayAfterLogin() {
        return delayAfterLogin;
    }

    /**
     * Gets delayAfterLogout.
     *
     * @return The delayAfterLogout.
     */
    public static int getDelayAfterLogout() {
        return delayAfterLogout;
    }

    /**
     * Gets delayAfterHoverMenu.
     *
     * @return The delayAfterHoverMenu.
     */
    public static int getDelayAfterHoverMenu() {
        return delayAfterHoverMenu;
    }

    /**
     * Gets delayAfterNewItemClick.
     *
     * @return The delayAfterNewItemClick.
     */
    public static int getDelayAfterNewItemClick() {
        return delayAfterNewItemClick;
    }

    /**
     * Gets delayIndexing.
     *
     * @return The delayIndexing.
     */
    public static int getDelayIndexing() {
        return delayIndexing;
    }

    /**
     * Gets delayAfterPickListClick.
     *
     * @return The delayAfterPickListClick.
     */
    public static int getDelayAfterPickListClick() {
        return delayAfterPickListClick;
    }

    /**
     * Gets delayAfterLinkClick.
     *
     * @return The delayAfterLinkClick.
     */
    public static int getDelayAfterLinkClick() {
        return delayAfterLinkClick;
    }

    /**
     * Gets onTravis.
     *
     * @return True if this runs on Travis.
     */
    public static boolean isOnTravis() {
        return onTravis;
    }

    /**
     * Gets delayAfterUpdate.
     *
     * @return The delayAfterUpdate.
     */
    public static int getDelayAfterUpdate() {
        return delayAfterUpdate;
    }

}
