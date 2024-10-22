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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.xebialabs.restito.server.StubServer;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.ImportConfiguration;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.helper.MetadataComparison;
import org.kitodo.production.helper.TempProcess;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataeditor.DataEditorService;
import org.kitodo.test.utils.ProcessTestUtils;
import org.kitodo.test.utils.TestConstants;

public class CatalogImportIT {

    private static StubServer server;
    private static final int PORT = 8888;
    private static final String CHILD_RECORD_ID = "1";
    private static final String PARENT_RECORD_ID = "2";
    private static final String REIMPORT_RECORD_ID = "789";
    private static final int PROJECT_ID = 1;
    private static final int TEMPLATE_ID = 1;
    private static final int IMPORT_DEPTH = 2;
    private static final String PUBLICATION_START_KEY = "PublicationStart";
    private static final String PUBLICATION_VALUE_OLD = "2019";
    private static final String PUBLICATION_VALUE_NEW = "2020";
    private int testProcessId = 0;
    private static final List<Locale.LanguageRange> languages = Locale.LanguageRange.parse("de, en");

    @BeforeAll
    public static void setup() throws Exception {
        setupServer();
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.insertMappingFiles();
        MockDatabase.insertImportConfigurations();
        MockDatabase.setUpAwaitility();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    @AfterAll
    public static void shutdown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
        server.stop();
    }

    @AfterEach
    public void removeTestProcess() throws DAOException {
        if (testProcessId > 0) {
            ProcessTestUtils.removeTestProcess(testProcessId);
            testProcessId = 0;
        }
    }

    @Test
    public void shouldImportProcessHierarchy() throws Exception {
        LinkedList<TempProcess> processes = ServiceManager.getImportService().importProcessHierarchy(CHILD_RECORD_ID,
                MockDatabase.getKalliopeImportConfiguration(), PROJECT_ID, TEMPLATE_ID, IMPORT_DEPTH,
                Collections.singleton("CatalogIDPredecessorPeriodical"));
        assertEquals(IMPORT_DEPTH, processes.size());
    }

    @Test
    public void shouldSuccessfullyUpdateMetadata() throws Exception {
        testProcessId = MockDatabase.insertTestProcess("Reimport-Test-Process", PROJECT_ID, TEMPLATE_ID, 1);
        ProcessTestUtils.copyTestMetadataFile(testProcessId, "testmetaReimport.xml");
        Process testProcess = ServiceManager.getProcessService().getById(testProcessId);
        URI processUri = ServiceManager.getProcessService().getMetadataFileUri(testProcess);
        Workpiece workpiece = ServiceManager.getMetsService().loadWorkpiece(processUri);
        ImportConfiguration configuration = MockDatabase.getKalliopeImportConfiguration();
        testProcess.setImportConfiguration(configuration);
        HashSet<Metadata> existingMetadata = workpiece.getLogicalStructure().getMetadata();
        List<MetadataComparison> metadataComparisons = DataEditorService.reimportCatalogMetadata(testProcess, workpiece,
                existingMetadata, languages, "Monograph");
        assertFalse(metadataComparisons.isEmpty(), "List of metadata comparisons should not be empty");
        MetadataComparison firstComparison = metadataComparisons.get(0);
        assertEquals(PUBLICATION_START_KEY, firstComparison.getMetadataKey(),
                String.format("Changed metadata should be '%s'", PUBLICATION_START_KEY));
        MetadataEntry publicationMetadataOld = new MetadataEntry();
        publicationMetadataOld.setKey(PUBLICATION_START_KEY);
        publicationMetadataOld.setValue(PUBLICATION_VALUE_OLD);
        MetadataEntry publicationMetadataNew = new MetadataEntry();
        publicationMetadataNew.setKey(PUBLICATION_START_KEY);
        publicationMetadataNew.setValue(PUBLICATION_VALUE_NEW);
        assertTrue(firstComparison.getOldValues().contains(publicationMetadataOld),
                String.format("Old values should contain publication year %s", publicationMetadataOld.getValue()));
        assertTrue(firstComparison.getNewValues().contains(publicationMetadataNew),
                String.format("New values should contain publication year %s", publicationMetadataNew.getValue()));
    }

    private static void setupServer() throws IOException {
        server = new StubServer(PORT).run();
        MockDatabase.addRestEndPointForSru(server, "ead.id=" + CHILD_RECORD_ID, TestConstants.CHILD_RECORD_PATH, "mods", 1);
        MockDatabase.addRestEndPointForSru(server, "ead.id=" + PARENT_RECORD_ID, TestConstants.PARENT_RECORD_PATH, "mods",  1);
        MockDatabase.addRestEndPointForSru(server, "ead.title=test", TestConstants.HITLIST_RECORD_PATH, "mods",10);
        MockDatabase.addRestEndPointForSru(server, "ead.id=" + REIMPORT_RECORD_ID, TestConstants.REIMPORT_RECORD_PATH, "mods", 1);
    }
}
