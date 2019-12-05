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

package org.kitodo.production.forms;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.services.ServiceManager;

public class MigrationFormIT {
    private MigrationForm migrationForm = new MigrationForm();

    /**
     * Setup Database and start elasticsearch.
     *
     * @throws Exception
     *             If databaseConnection failed.
     */
    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    /**
     * Cleanup the database and stop elasticsearch.
     *
     * @throws Exception
     *             if elasticsearch could not been stopped.
     */
    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void testShowPossibleProjects() {
        Assert.assertEquals("Projectslist should be empty", Collections.emptyList(), migrationForm.getAllProjects());
        Assert.assertFalse("Projectslist should not be shown", migrationForm.isProjectListRendered());
        migrationForm.showPossibleProjects();
        Assert.assertEquals("Should get all Projects", 3, migrationForm.getAllProjects().size());
        Assert.assertTrue("Projectslist should be shown", migrationForm.isProjectListRendered());
    }

    @Test
    public void testShowProcessesForProjects() throws DAOException {
        Assert.assertEquals(Collections.emptyList(), migrationForm.getAggregatedTasks());

        ArrayList<Project> selectedProjects = new ArrayList<>();
        selectedProjects.add(ServiceManager.getProjectService().getById(1));
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        String processesShouldBeFound = "Processes should be found";
        Assert.assertEquals(processesShouldBeFound, 0, migrationForm.getAggregatedTasks().size());

        selectedProjects.add(ServiceManager.getProjectService().getById(2));
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        Assert.assertEquals(processesShouldBeFound, 1, migrationForm.getAggregatedTasks().size());

        selectedProjects.add(ServiceManager.getProjectService().getById(2));
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        Assert.assertEquals(processesShouldBeFound, 1, migrationForm.getAggregatedTasks().size());

        selectedProjects.remove(2);
        selectedProjects.remove(1);
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        Assert.assertEquals(processesShouldBeFound, 0, migrationForm.getAggregatedTasks().size());
    }

}
