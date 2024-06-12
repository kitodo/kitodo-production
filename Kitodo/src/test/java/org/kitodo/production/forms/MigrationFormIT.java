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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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
    @BeforeAll
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
    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void testShowPossibleProjects() {
        assertEquals(Collections.emptyList(), migrationForm.getAllProjects(), "Projectslist should be empty");
        assertFalse(migrationForm.isProjectListRendered(), "Projectslist should not be shown");
        migrationForm.showPossibleProjects();
        assertEquals(3, migrationForm.getAllProjects().size(), "Should get all Projects");
        assertTrue(migrationForm.isProjectListRendered(), "Projectslist should be shown");
    }

    @Test
    public void testShowProcessesForProjects() throws DAOException {
        assertEquals(Collections.emptyList(), migrationForm.getAggregatedTasks());

        ArrayList<Project> selectedProjects = new ArrayList<>();
        selectedProjects.add(ServiceManager.getProjectService().getById(1));
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        String processesShouldBeFound = "Processes should be found";
        assertEquals(0, migrationForm.getAggregatedTasks().size(), processesShouldBeFound);

        selectedProjects.add(ServiceManager.getProjectService().getById(2));
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        assertEquals(1, migrationForm.getAggregatedTasks().size(), processesShouldBeFound);

        selectedProjects.add(ServiceManager.getProjectService().getById(2));
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        assertEquals(1, migrationForm.getAggregatedTasks().size(), processesShouldBeFound);

        selectedProjects.remove(2);
        selectedProjects.remove(1);
        migrationForm.setSelectedProjects(selectedProjects);
        migrationForm.showAggregatedProcesses();

        assertEquals(0, migrationForm.getAggregatedTasks().size(), processesShouldBeFound);
    }

}
