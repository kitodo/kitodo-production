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
import java.net.URI;

import org.apache.commons.lang.SystemUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.kitodo.config.ConfigCore;
import org.kitodo.ExecutionPermission;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.selenium.testframework.helper.TestWatcherImpl;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class BaseTestSelenium {

    private static final FileService fileService = ServiceManager.getFileService();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.startDatabaseServer();

        fileService.createDirectory(URI.create(""), "users");
        FileLoader.createDiagramTestFile();
        FileLoader.createConfigProjectsFile();
        FileLoader.createDigitalCollectionsFile();

        if (SystemUtils.IS_OS_LINUX) {
            File scriptCreateDirMeta = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
            File scriptCreateDirUserHome = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));
            ExecutionPermission.setExecutePermission(scriptCreateDirMeta);
            ExecutionPermission.setExecutePermission(scriptCreateDirUserHome);
        }

        Browser.Initialize();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        Browser.close();

        if (SystemUtils.IS_OS_LINUX) {
            File scriptCreateDirMeta = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
            File scriptCreateDirUserHome = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));
            ExecutionPermission.setNoExecutePermission(scriptCreateDirMeta);
            ExecutionPermission.setNoExecutePermission(scriptCreateDirUserHome);
        }

        FileLoader.deleteDigitalCollectionsFile();
        FileLoader.deleteConfigProjectsFile();
        FileLoader.deleteDiagramTestFile();
        fileService.delete(URI.create("users"));

        MockDatabase.stopNode();
        MockDatabase.stopDatabaseServer();
        MockDatabase.cleanDatabase();
    }

    /**
     * Watcher for WebDriverExceptions on travis which takes screenshot and sends
     * email
     */
    @Rule
    public TestRule seleniumExceptionWatcher = new TestWatcherImpl();
}
