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

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.parameter;

import com.xebialabs.restito.server.StubServer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.services.ServiceManager;

public class CatalogImportIT {

    private static StubServer server;
    private static final int PORT = 8888;
    private static final String HITLIST_RECORD_PATH = "src/test/resources/importRecords/importHitlist.xml";
    private static final String CHILD_RECORD_PATH = "src/test/resources/importRecords/importChildRecord.xml";
    private static final String PARENT_RECORD_PATH = "src/test/resources/importRecords/importParentRecord.xml";
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
        addRestEndPointForImport("ead.id=" + CHILD_RECORD_ID, CHILD_RECORD_PATH, 1);
        addRestEndPointForImport("ead.id=" + PARENT_RECORD_ID, PARENT_RECORD_PATH, 1);
        addRestEndPointForImport("ead.title=test", HITLIST_RECORD_PATH, 10);
    }

    private static void addRestEndPointForImport(String query, String filePath, int numberOfRecords) throws IOException {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            String serverResponse = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            whenHttp(server)
                    .match(get("/sru"),
                            parameter("version", "1.2"),
                            parameter("operation", "searchRetrieve"),
                            parameter("recordSchema", "mods"),
                            parameter("maximumRecords", String.valueOf(numberOfRecords)),
                            parameter("query", query))
                    .then(ok(), contentType("text/xml"), stringContent(serverResponse));
        }
    }
}
