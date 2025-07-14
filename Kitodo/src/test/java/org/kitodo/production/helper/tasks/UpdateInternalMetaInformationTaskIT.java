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

package org.kitodo.production.helper.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.database.persistence.ProcessDAO;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.test.utils.ProcessTestUtils;

public class UpdateInternalMetaInformationTaskIT {

    private static final ProcessService processService = ServiceManager.getProcessService();
    private static Project project;
    private static int testProcessId = -1;
    private static final String METADATA_SOURCE_FILE = "testmeta.xml";
    private static final String METADATA_PROCESS_TITLE = "update_internal_meta_information";
    private static final int PROJECT_ID = 3;
    private static final int RULESET_ID = 1;
    private static final int TEMPLATE_ID = 1;

    @BeforeAll
    public static void startSearchIndex() throws Exception {
        MockDatabase.startNode();
    }

    @BeforeEach
    public void prepareData() throws Exception {
        MockDatabase.insertProcessesFull();
        project = ServiceManager.getProjectService().getById(PROJECT_ID);
        testProcessId = MockDatabase.insertTestProcess(METADATA_PROCESS_TITLE, PROJECT_ID,
                TEMPLATE_ID, RULESET_ID);
        ProcessTestUtils.copyTestMetadataFile(testProcessId, METADATA_SOURCE_FILE);
    }

    @AfterEach
    public void cleanUpData() throws Exception {
        ProcessTestUtils.removeTestProcess(testProcessId);
        MockDatabase.cleanDatabase();
    }

    @AfterAll
    public static void stopSearchIndex() throws Exception {
        MockDatabase.stopNode();
    }

    @Test
    public void executeUpdateOfInternalMetaInformation() throws DAOException, InterruptedException {
        Process process;

        // manipulate stored values as correct values was inserted by ProcessTestUtils.copyTestMetadataFile() call
        process = processService.getById(testProcessId);

        process.setSortHelperImages(99);
        process.setSortHelperMetadata(199);
        process.setSortHelperDocstructs(299);
        ProcessDAO processDAO = new ProcessDAO();
        processDAO.save(process);

        // check that "wrong" values are stored in database
        assertEquals(99, process.getSortHelperImages());
        assertEquals(199, process.getSortHelperMetadata());
        assertEquals(299, process.getSortHelperDocstructs());

        UpdateInternalMetaInformationTask task = new UpdateInternalMetaInformationTask(project);

        task.start();
        // execution check
        assertTrue(task.isAlive());
        task.join();

        // after execution check
        assertFalse(task.isAlive());
        assertEquals(100, task.getProgress());

        // after execution database check
        // must reload process from database
        process = processService.getById(testProcessId);
        assertEquals(2, process.getSortHelperImages());
        assertEquals(6, process.getSortHelperMetadata());
        assertEquals(4, process.getSortHelperDocstructs());
    }
}
