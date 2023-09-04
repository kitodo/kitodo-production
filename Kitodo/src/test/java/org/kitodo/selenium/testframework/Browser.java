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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.selenium.testframework.enums.BrowserType;
import org.kitodo.selenium.testframework.helper.WebDriverProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Browser {
    private static final Logger logger = LogManager.getLogger(Browser.class);

    private static final String BASE_URL = "http://localhost:8080/kitodo/";
    private static final BrowserType BROWSER_TYPE = BrowserType.CHROME;
    private static final int DELAY_AFTER_CATALOG_SELECTION = 500;
    private static final int DELAY_AFTER_DELETE = 7000;
    private static final int DELAY_AFTER_CLICK_LINK_MIN = 500;
    private static final int DELAY_AFTER_CLICK_LINK_MAX = 1500;
    private static final int DELAY_AFTER_HOVER_MENU = 500;
    private static final int DELAY_AFTER_INDEXING = 2000;
    private static final int DELAY_AFTER_LOGOUT = 3000;
    private static final int DELAY_AFTER_NEW_ITEM_CLICK = 500;
    private static final int DELAY_AFTER_PICK_LIST_CLICK = 1500;
    private static final int DELAY_INDEXING = 3000;
    private static final String USER_DIR = System.getProperty("user.dir");
    public static final String DOWNLOAD_DIR = USER_DIR + "/target/downloads/";
    private static final String DRIVER_DIR = USER_DIR + "/target/extracts/";
    private static final String GECKO_DRIVER_VERSION = "0.19.1";

    private static Actions actions;

    private static RemoteWebDriver webDriver;

    /**
     * Provides the web driver, sets timeout and window size.
     */
    public static void Initialize() throws IOException {
        if (BROWSER_TYPE.equals(BrowserType.CHROME)) {
            provideChromeDriver();
        }
        if (BROWSER_TYPE.equals(BrowserType.FIREFOX)) {
            provideGeckoDriver();
        }

        actions = new Actions(webDriver);
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        goTo("");
        webDriver.manage().window().setSize(new Dimension(1280, 1024));
    }

    private static void provideChromeDriver() throws IOException {
        File driverFile = getDriverFile();

        if (!driverFile.exists()) {
            logger.debug("{} does not exist, providing chrome driver now", driverFile.getAbsolutePath());
            WebDriverProvider.provideChromeDriver(DOWNLOAD_DIR, DRIVER_DIR);
        }

        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(driverFile)
                .usingAnyFreePort()
                .build();

        Map<String, Object> chromePrefs = new HashMap<>();
        chromePrefs.put("download.default_directory", DOWNLOAD_DIR);
        chromePrefs.put("download.prompt_for_download", false);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", chromePrefs);

        webDriver = new ChromeDriver(service, options);
    }

    private static File getDriverFile() {
        String driver = WebDriverProvider.CHROME_DRIVER;
        if (SystemUtils.IS_OS_WINDOWS) {
            driver = WebDriverProvider.CHROME_DRIVER_WIN_SUBDIR + "/" + driver.concat(WebDriverProvider.EXE);
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            driver = WebDriverProvider.CHROME_DRIVER_MAC_SUBDIR + "/" + driver;
        } else {
            driver = WebDriverProvider.CHROME_DRIVER_LINUX_SUBDIR + "/" + driver;
        }
        return new File(DRIVER_DIR + driver);
    }

    private static void provideGeckoDriver() throws IOException {
        String driverFileName = "geckodriver";
        if (SystemUtils.IS_OS_WINDOWS) {
            driverFileName = driverFileName.concat(".exe");
        }
        File driverFile = new File(DRIVER_DIR + driverFileName);
        if (!driverFile.exists()) {
            WebDriverProvider.provideGeckoDriver(GECKO_DRIVER_VERSION, DOWNLOAD_DIR, DRIVER_DIR);
        }

        FirefoxProfile profile = new FirefoxProfile();
        profile.setAssumeUntrustedCertificateIssuer(false);
        profile.setPreference("browser.helperApps.alwaysAsk.force", false);
        profile.setPreference("browser.download.dir", DOWNLOAD_DIR);

        FirefoxOptions options = new FirefoxOptions();
        options.setProfile(profile);

        webDriver = new FirefoxDriver(options);
    }

    public static boolean isAlertPresent() {
        try {
            webDriver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
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
    public static void hoverWebElement(WebElement webElement) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.debug("interrupted sleeping");
        }
        actions.moveToElement(webElement).pause(DELAY_AFTER_HOVER_MENU).build().perform();
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

    public static List<WebElement> getRowsOfTable(WebElement table) {
        return table.findElements(By.tagName("tr"));
    }

    /**
     * Gets number of selected rows in a given table.
     * @param table as a WebElement
     * @return number of selected rows
     */
    public static long getSelectedRowsOfTable(WebElement table) {
        return getRowsOfTable(table)
                .stream()
                .filter(element -> element.getAttribute("aria-selected").equals("true"))
                .count();
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
        if (cells.size() <= columnIndex) {
            return "";
        }
        return cells.get(columnIndex).getText();
    }

    public static void closeDialog(WebElement dialog) {
        dialog.findElement(By.className("close")).click();
    }

    /**
     * Gets delayAfterLogin.
     *
     * @return The delayAfterLogin.
     */
    public static int getDelayAfterLogin() {
        return DELAY_AFTER_INDEXING;
    }

    /**
     * Gets delayAfterLogout.
     *
     * @return The delayAfterLogout.
     */
    public static int getDelayAfterLogout() {
        return DELAY_AFTER_LOGOUT;
    }

    /**
     * Gets delayAfterHoverMenu.
     *
     * @return The delayAfterHoverMenu.
     */
    public static int getDelayAfterHoverMenu() {
        return DELAY_AFTER_HOVER_MENU;
    }

    /**
     * Gets delayAfterNewItemClick.
     *
     * @return The delayAfterNewItemClick.
     */
    public static int getDelayAfterNewItemClick() {
        return DELAY_AFTER_NEW_ITEM_CLICK;
    }

    /**
     * Gets delayIndexing.
     *
     * @return The delayIndexing.
     */
    public static int getDelayIndexing() {
        return DELAY_INDEXING;
    }

    /**
     * Gets delayAfterPickListClick.
     *
     * @return The delayAfterPickListClick.
     */
    public static int getDelayAfterPickListClick() {
        return DELAY_AFTER_PICK_LIST_CLICK;
    }

    /**
     * Get min delay after link was clicked.
     *
     * @return min delay after link was clicked
     */
    public static int getDelayMinAfterLinkClick() {
        return DELAY_AFTER_CLICK_LINK_MIN;
    }

    /**
     * Get max delay after link was clicked.
     *
     * @return max delay after link was clicked
     */
    public static int getDelayMaxAfterLinkClick() {
        return DELAY_AFTER_CLICK_LINK_MAX;
    }

    /**
     * Gets delayAfterDelete.
     *
     * @return The delayAfterDelete.
     */
    public static int getDelayAfterDelete() {
        return DELAY_AFTER_DELETE;
    }

    /**
     * Get delayAfterCatalogSelection.
     *
     * @return value of delayAfterCatalogSelection
     */
    public static int getDelayAfterCatalogSelection() {
        return DELAY_AFTER_CATALOG_SELECTION;
    }

}
