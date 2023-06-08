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

package org.kitodo.production.services.data;

import static com.xebialabs.restito.builder.stub.StubHttp.whenHttp;
import static com.xebialabs.restito.semantics.Action.contentType;
import static com.xebialabs.restito.semantics.Action.ok;
import static com.xebialabs.restito.semantics.Action.stringContent;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.parameter;
import static org.awaitility.Awaitility.await;

import com.xebialabs.restito.server.StubServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.ExecutionPermission;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.UrlParameter;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.ImportException;
import org.kitodo.production.services.ServiceManager;

public class ImportServiceIT {

    private static final ProcessService processService = ServiceManager.getProcessService();
    private static final ImportService importService = ServiceManager.getImportService();
    private static StubServer server;
    private static final String TEST_FILE_PATH = "src/test/resources/sruTestRecord.xml";
    private static final String TEST_FILE_ERROR_SUCCESS_PATH = "src/test/resources/customInterfaceSuccessResponse.xml";
    private static final String TEST_FILE_ERROR_RESPONSE_PATH = "src/test/resources/customInterfaceErrorResponse.xml";
    private static final String RECORD_ID = "11111";
    private static final String CUSTOM_INTERFACE_RECORD_ID = "12345";
    private static final int PORT = 8888;
    private static final String firstProcess = "First process";

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
        MockDatabase.insertImportconfigurationWithCustomUrlParameters();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertProcessesForHierarchyTests();
        MockDatabase.setUpAwaitility();
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
        await().until(() -> {
            SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
            return !processService.findByTitle(firstProcess).isEmpty();
        });
        server = new StubServer(PORT).run();
        setupServer();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        server.stop();
    }

    @Test
    public void testImportProcess() throws Exception {
        Assert.assertEquals("Not the correct amount of processes found", 7, (long) processService.count());
        Process importedProcess = importProcess(RECORD_ID, MockDatabase.getK10PlusImportConfiguration());
        Assert.assertEquals("WrongProcessTitle", "Kitodo_" + RECORD_ID, importedProcess.getTitle());
        Assert.assertEquals("Wrong project used", 1, (long) importedProcess.getProject().getId());
        Assert.assertEquals("Wrong template used", 1, (long) importedProcess.getTemplate().getId());
        Assert.assertEquals("Not the correct amount of processes found", 8, (long) processService.count());
    }

    @Test
    public void shouldCreateUrlWithCustomParameters() throws DAOException, ImportException, IOException {
        Process importedProcess = importProcess(CUSTOM_INTERFACE_RECORD_ID, MockDatabase.getCustomTypeImportConfiguration());
        Assert.assertNotNull(importedProcess);
    }

    @Test(expected = ImportException.class)
    public void shouldFailToImportFromCustomInterfaceWithoutConfiguredUrlParameters() throws DAOException,
            ImportException, IOException {
        ImportConfiguration customConfiguration = MockDatabase.getCustomTypeImportConfiguration();
        UrlParameter wrongUrlParameter = new UrlParameter();
        wrongUrlParameter.setParameterKey("firstKey");
        wrongUrlParameter.setParameterValue("wrongValue");
        customConfiguration.setUrlParameters(Collections.singletonList(wrongUrlParameter));
        importProcess(CUSTOM_INTERFACE_RECORD_ID, customConfiguration);
    }

    private static void setupServer() throws IOException {
        // endpoint for importing record by id
        try (InputStream inputStream = Files.newInputStream(Paths.get(TEST_FILE_PATH))) {
            String serverResponse = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            whenHttp(server)
                    .match(get("/sru"),
                            parameter("version", "1.1"),
                            parameter("operation", "searchRetrieve"),
                            parameter("recordSchema", "picaxml"),
                            parameter("maximumRecords", "1"),
                            parameter("query", "pica.ppn=" + RECORD_ID))
                    .then(ok(), contentType("text/xml"), stringContent(serverResponse));
        }
        try (InputStream inputStream = Files.newInputStream(Paths.get(TEST_FILE_ERROR_SUCCESS_PATH))) {
            String serverResponse = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            whenHttp(server)
                    .match(get("/custom"),
                            parameter("firstKey", "firstValue"),
                            parameter("secondKey", "secondValue"),
                            parameter("id", CUSTOM_INTERFACE_RECORD_ID))
                    .then(ok(), contentType("text/xml"), stringContent(serverResponse));
        }
        try (InputStream inputStream = Files.newInputStream(Paths.get(TEST_FILE_ERROR_RESPONSE_PATH))) {
            String serverResponse = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            whenHttp(server)
                    .match(get("/custom"),
                            parameter("firstKey", "wrongValue"),
                            parameter("id", CUSTOM_INTERFACE_RECORD_ID))
                    .then(ok(), contentType("text/xml"), stringContent(serverResponse));
        }
    }

    private Process importProcess(String recordId, ImportConfiguration importConfiguration)
            throws IOException, ImportException {
        File script = new File(ConfigCore.getParameter(ParameterCore.SCRIPT_CREATE_DIR_META));
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setExecutePermission(script);
        }
        Process importedProcess = importService.importProcess(recordId, 1, 1,
                importConfiguration, new HashMap<>());
        if (!SystemUtils.IS_OS_WINDOWS) {
            ExecutionPermission.setNoExecutePermission(script);
        }
        return importedProcess;
    }
}
