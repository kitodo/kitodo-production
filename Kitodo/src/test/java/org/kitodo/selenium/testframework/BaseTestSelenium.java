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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.User;
import org.kitodo.selenium.testframework.helper.MailSender;
import org.kitodo.services.ServiceManager;
import org.openqa.selenium.WebDriverException;

public class BaseTestSelenium {
    private static final Logger logger = LogManager.getLogger(BaseTestSelenium.class);
    private ServiceManager serviceManager = new ServiceManager();

    private static final String TRAVIS_BUILD_NUMBER = "TRAVIS_BUILD_NUMBER";
    private static final String TRAVIS_BRANCH = "TRAVIS_BRANCH";
    private static final String TRAVIS_REPO_SLUG = "TRAVIS_REPO_SLUG";
    private static final String TRAVIS_BUILD_ID = "TRAVIS_BUILD_ID";
    private static final String MAIL_USER = "MAIL_USER";
    private static final String MAIL_PASSWORD = "MAIL_PASSWORD";
    private static final String MAIL_RECIPIENT = "MAIL_RECIPIENT";

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.startDatabaseServer();

        Browser.Initialize();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopDatabaseServer();
        MockDatabase.stopNode();

        Browser.close();
    }

    @Before
    public void login() throws Exception {
        User user = serviceManager.getUserService().getById(1);
        user.setPassword("test");

        Pages.getLoginPage().goTo();
        Pages.getLoginPage().performLogin(user);
    }

    @After
    public void logout() throws Exception {
        Pages.getTopNavigation().logout();
    }

    /**
     * Watcher for WebDriverExceptions on travis which makes screenshot and sends
     * email
     */
    @Rule
    public TestRule seleniumExceptionWatcher = new TestWatcher() {

        @Override
        protected void failed(Throwable ex, Description description) {
            if (Browser.isOnTravis() && (ex instanceof WebDriverException)) {
                try {
                    File screenshot = Browser.captureScreenShot();
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

                    MailSender.sendEmail(user, password, emailSubject, emailMessage, screenshot, recipient);
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
}
