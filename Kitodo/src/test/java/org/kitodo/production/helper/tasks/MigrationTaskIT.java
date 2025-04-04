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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.test.utils.ProcessTestUtils;

public class MigrationTaskIT {

    private static final MetsService metsService = ServiceManager.getMetsService();
    private static final ProcessService processService = ServiceManager.getProcessService();
    private static Project project;
    private static final String METADATA_MIGRATION_SOURCE_FILE = "testMetadataMigrationSourceFile.xml";
    private static final String METADATA_MIGRATION_PROCESS_TITLE = "Migration process";
    private static int migrationTestProcessId = -1;
    private static final int MIGRATION_PROJECT_ID = 3;
    private static final int TEMPLATE_ID = 1;
    private static final int RULESET_ID = 1;

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        project = ServiceManager.getProjectService().getById(MIGRATION_PROJECT_ID);
        migrationTestProcessId = MockDatabase.insertTestProcess(METADATA_MIGRATION_PROCESS_TITLE, MIGRATION_PROJECT_ID,
                TEMPLATE_ID, RULESET_ID);
        ProcessTestUtils.copyTestMetadataFile(migrationTestProcessId, METADATA_MIGRATION_SOURCE_FILE);
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        ProcessTestUtils.removeTestProcess(migrationTestProcessId);
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void testMigrationTask() throws Exception {
        MigrationTask migrationTask = new MigrationTask(project);
        migrationTask.start();
        assertTrue(migrationTask.isAlive());
        migrationTask.join();
        assertFalse(migrationTask.isAlive());
        assertEquals(100, migrationTask.getProgress());
        assertNotNull(metsService.loadWorkpiece(processService.getMetadataFileUri(processService.getById(migrationTestProcessId))), "Process migration failed");
    }
}
