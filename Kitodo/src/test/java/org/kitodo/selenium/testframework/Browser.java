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
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.selenium.testframework.helper.GeckoDriverProvider;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Browser {
    private static final Logger logger = LogManager.getLogger(Browser.class);
    private static final String BASE_URL = "http://localhost:8080/kitodo/";
    private static RemoteWebDriver webDriver;
    private static Actions actions;
    private static final String GECKO_DRIVER_VERSION = "0.19.1";
    private static boolean onTravis = false;

    private static int delayIndexing = 3000;
    private static int delayAfterSave = 3000;
    private static int delayAfterLogin = 2000;
    private static int delayAfterLogout = 3000;
    private static int delayAfterLinkClick = 500;
    private static int delayAfterHoverMenu = 500;
    private static int delayAfterNewItemClick = 500;
    private static int delayAfterPickListClick = 3000;

    public static void Initialize() throws IOException {
        String userDir = System.getProperty("user.dir");
        GeckoDriverProvider.provide(GECKO_DRIVER_VERSION, userDir + "/target/downloads/",
            userDir + "/target/extracts/");

        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("moz:webdriverClick", false);

        FirefoxOptions firefoxOptions = new FirefoxOptions(capabilities);
        webDriver = new FirefoxDriver(firefoxOptions);
        actions = new Actions(Browser.getDriver());

        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        goTo("");
        webDriver.manage().window().setSize(new Dimension(1280, 1024));

        if ("true".equals(System.getenv().get("TRAVIS"))) {
            onTravis = true;
            doubleAllDelays();
        }
    }

    private static void doubleAllDelays() {
        delayIndexing = delayIndexing * 2;
        delayAfterSave = delayAfterSave * 2;
        delayAfterLogin = delayAfterLogin * 2;
        delayAfterLogout = delayAfterLogout * 2;
        delayAfterLinkClick = delayAfterLinkClick * 2;
        delayAfterHoverMenu = delayAfterHoverMenu * 2;
        delayAfterNewItemClick = delayAfterNewItemClick * 2;
        delayAfterPickListClick = delayAfterPickListClick * 2;
    }

    public static String getCurrentUrl() {
        return webDriver.getCurrentUrl();
    }

    public static RemoteWebDriver getDriver() {
        return webDriver;
    }

    public static void goTo(String url) {
        webDriver.get(BASE_URL + url);
    }

    public static void close() {
        webDriver.close();
    }

    public static void hoverWebElement(WebElement webElement) throws InterruptedException {
        actions.moveToElement(webElement).pause(delayAfterHoverMenu).build().perform();
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
}
