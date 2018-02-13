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
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Browser {
    private static final Logger logger = LogManager.getLogger(Browser.class);
    private static final String BASE_URL = "http://localhost:8080/kitodo/";
    private static RemoteWebDriver webDriver;

    private static final String GECKO_DRIVER_VERSION = "0.19.0";

    public static void Initialize() throws IOException {
        String userDir = System.getProperty("user.dir");
        GeckoDriverProvider.provide(GECKO_DRIVER_VERSION, userDir + "/target/downloads/",
            userDir + "/target/extracts/");

        webDriver = new FirefoxDriver();

        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        goTo("");
        webDriver.manage().window().setSize(new Dimension(1280, 1024));
    }


    public static RemoteWebDriver getDriver() {
        return webDriver;
    }

    public static void goTo(String url) {
        webDriver.get(BASE_URL + url);
    }

    public static void Close() {
        webDriver.close();
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
}
