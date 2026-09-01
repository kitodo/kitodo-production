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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.massindexing.MassIndexer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kitodo.ExecutionPermission;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.persistence.HibernateUtil;
import org.kitodo.selenium.testframework.helper.ScreenshotTestWatcher;

import static org.awaitility.Awaitility.await;

@ExtendWith(ScreenshotTestWatcher.class)
public class BaseTestSelenium {

    private static final Logger logger = LogManager.getLogger(BaseTestSelenium.class);
    private static final File usersDirectory = new File("src/test/resources/users");

    @BeforeAll
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.startDatabaseServer();

        try (Session ormSession = HibernateUtil.getSession()) {
            MassIndexer massIndexer = Search.session(ormSession).massIndexer(Process.class);
            massIndexer.dropAndCreateSchemaOnStart(true);
            massIndexer.startAndWait();
        }

        usersDirectory.mkdir();

        FileLoader.createDiagramTestFile();
        FileLoader.createConfigProjectsFile();

        if (SystemUtils.IS_OS_LINUX) {
            File scriptCreateDirMeta = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
            File scriptCreateDirUserHome = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));
            ExecutionPermission.setExecutePermission(scriptCreateDirMeta);
            ExecutionPermission.setExecutePermission(scriptCreateDirUserHome);
        }

        Browser.Initialize();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        try {
            Browser.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (SystemUtils.IS_OS_LINUX) {
            File scriptCreateDirMeta = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
            File scriptCreateDirUserHome = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));
            ExecutionPermission.setNoExecutePermission(scriptCreateDirMeta);
            ExecutionPermission.setNoExecutePermission(scriptCreateDirUserHome);
        }

        FileLoader.deleteConfigProjectsFile();
        FileLoader.deleteDiagramTestFile();

        usersDirectory.delete();

        // The search server node is not stopped here, but kept running across
        // all selenium test classes, because the Tomcat application would keep
        // stale connections to a restarted index server and the first index
        // query of every test class would stall. The node is stopped when the
        // JVM terminates, so it is kept only within this surefire execution.
        MockDatabase.stopDatabaseServer();
        MockDatabase.cleanDatabase();
    }

    @BeforeEach
    public void debugLogBefore(TestInfo testInfo) {
        String className = testInfo.getTestClass().get().getSimpleName();
        String methodName = testInfo.getTestMethod().get().getName();
        logger.debug("execute test: {}#{}", className, methodName);
    }

    @AfterEach
    public void debugLogAfter(TestInfo testInfo) {
        String className = testInfo.getTestClass().get().getSimpleName();
        String methodName = testInfo.getTestMethod().get().getName();
        logger.debug("finished test: {}#{}", className, methodName);
    }

    protected void pollAssertTrue(Callable<Boolean> conditionEvaluator) {
        await().ignoreExceptions()
                .pollDelay(1, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .atMost(5, TimeUnit.SECONDS)
                .until(conditionEvaluator);
    }
}
