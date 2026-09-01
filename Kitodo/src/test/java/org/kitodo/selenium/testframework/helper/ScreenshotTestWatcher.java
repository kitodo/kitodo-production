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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.kitodo.selenium.testframework.Browser;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

public class ScreenshotTestWatcher implements TestWatcher {

    private static final Logger logger = LogManager.getLogger(ScreenshotTestWatcher.class);

    public static final String SCREENSHOT_DIRECTORY = System.getenv().getOrDefault(
        "SELENIUM_SCREENSHOT_DIRECTORY", 
        "target/selenium-screenshots"
    );
    
    public void testFailed(ExtensionContext context, @Nullable Throwable cause) {
        WebDriver driver = Browser.getDriver();
        if (!(driver instanceof TakesScreenshot)) {
            logger.warn("cannot take screenshot with driver that doesn't support screenshots: {}", driver);
            return;
        }

        try {
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            Path directory = Path.of(SCREENSHOT_DIRECTORY).toAbsolutePath().normalize();
            Files.createDirectories(directory);

            String className = context.getRequiredTestClass().getSimpleName();
            String methodName = context.getTestMethod()
                    .map(method -> method.getName())
                    .orElse("unknown");

            Path filepath = directory.resolve(String.format("%s-%s-%d.png", className, methodName, System.currentTimeMillis()));
            logger.debug("writing selenium screenshot to {}", filepath);
            Files.write(filepath, screenshot);
        } catch (IOException e) {
            logger.error("error generating screenshot after test failure", e);
        }
	}
}
