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

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

public class Browser {
    private static final String BASE_URL = "http://localhost:8080/kitodo/";
    private static RemoteWebDriver webDriver = new FirefoxDriver();
    public static String title;

    public static void Initialize() {
        webDriver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        goTo("");
        webDriver.manage().window().setSize(new Dimension(1280, 1024));
    }

    public static String getTitle() {
        return webDriver.getTitle();
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

}
