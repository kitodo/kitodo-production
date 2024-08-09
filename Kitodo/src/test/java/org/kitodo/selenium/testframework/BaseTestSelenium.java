/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.kitodo.selenium.testframework;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;

public class BaseTestSelenium {

    private static final Logger logger = LogManager.getLogger(BaseTestSelenium.class);
    private static final File usersDirectory = new File("src/test/resources/users");

    @BeforeAll
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.startDatabaseServer();

        usersDirectory.mkdir();

        FileLoader.createDiagramTestFile();
        FileLoader.createConfigProjectsFile();

        if (SystemUtils.IS_OS_LINUX) {
            File scriptCreateDirMeta = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
            File scriptCreateDirUserHome = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_USER_HOME));
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
        }

        FileLoader.deleteConfigProjectsFile();
        FileLoader.deleteDiagramTestFile();

        usersDirectory.delete();

        MockDatabase.stopNode();
        MockDatabase.stopDatabaseServer();
        MockDatabase.cleanDatabase();
    }
}
