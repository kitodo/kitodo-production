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

package org.kitodo.production.services.catalogimport;

import com.xebialabs.restito.server.StubServer;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.test.utils.TestConstants;

public class CatalogImportIT {

    private static StubServer server;
    private static final int PORT = 8888;
    private static final String CHILD_RECORD_ID = "1";
    private static final String PARENT_RECORD_ID = "2";
    private static final int PROJECT_ID = 1;
    private static final int TEMPLATE_ID = 1;
    private static final int IMPORT_DEPTH = 2;

    @BeforeClass
    public static void setup() throws Exception {
        setupServer();
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    @AfterClass
    public static void shutdown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        server.stop();
    }

    @Test
    public void shouldImportProcessHierarchy() throws Exception {
        LinkedList<TempProcess> processes = ServiceManager.getImportService().importProcessHierarchy(CHILD_RECORD_ID,
                MockDatabase.getKalliopeImportConfiguration(), PROJECT_ID, TEMPLATE_ID, IMPORT_DEPTH,
                Collections.singleton("CatalogIDPredecessorPeriodical"));
        Assert.assertEquals(IMPORT_DEPTH, processes.size());
    }

    private static void setupServer() throws IOException {
        server = new StubServer(PORT).run();
        MockDatabase.addRestEndPointForSru(server, "ead.id=" + CHILD_RECORD_ID, TestConstants.CHILD_RECORD_PATH, "mods", 1);
        MockDatabase.addRestEndPointForSru(server, "ead.id=" + PARENT_RECORD_ID, TestConstants.PARENT_RECORD_PATH, "mods",  1);
        MockDatabase.addRestEndPointForSru(server, "ead.title=test", TestConstants.HITLIST_RECORD_PATH, "mods",10);
    }
}
