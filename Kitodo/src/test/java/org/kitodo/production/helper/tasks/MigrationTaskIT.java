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

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.services.ServiceManager;

public class MigrationTaskIT {

    private static Project project;

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();

        project = ServiceManager.getProjectService().getById(1);
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void testMigrationTask() throws InterruptedException {
        MigrationTask migrationTask = new MigrationTask(project);
        migrationTask.start();
        Assert.assertTrue(migrationTask.isAlive());
        migrationTask.join();
        Assert.assertFalse(migrationTask.isAlive());
        Assert.assertEquals(100, migrationTask.getProgress());
    }
}
