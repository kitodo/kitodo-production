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

package org.kitodo.services;

import java.util.ArrayList;
import java.util.List;

import org.goobi.production.flow.statistics.StepInformation;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;

/**
 * Tests for ProjectService class.
 */
public class ProjectServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        //MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindProject() throws Exception {
        ProjectService projectService = new ProjectService();

        Project project = projectService.find(1);
        boolean condition = project.getTitle().equals("First project") && project.getNumberOfPages().equals(30);
        assertTrue("Project was not found in database!", condition);
    }

    @Test
    public void shouldFindAllProjects() throws Exception {
        ProjectService projectService = new ProjectService();

        List<Project> projects = projectService.findAll();
        assertEquals("Not all projects were found in database!", 3, projects.size());
    }

    @Test
    public void shouldGetWorkFlow() throws Exception {
        ProjectService projectService = new ProjectService();

        //test passes... but it can mean that something is wrong...
        Project project = projectService.find(1);
        List<StepInformation> expected = new ArrayList<>();
        List<StepInformation> actual = projectService.getWorkFlow(project);
        assertEquals("Work flow doesn't match to given work flow!", expected, actual);
    }
}
