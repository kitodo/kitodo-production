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

package de.sub.goobi.forms;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Project;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.data.ProjectService;

public class ProjekteFormIT {

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(500);
    }

    @Ignore("this test executes some updates after class")
    @Test
    public void shouldGenerateValuesForStatistics() throws Exception {
        ProjekteForm projekteForm = new ProjekteForm();
        ProjectService projectService = new ServiceManager().getProjectService();

        Project initialProject = projectService.getById(1);

        projekteForm.setMyProjekt(initialProject);
        //TODO: more likely this line
        projekteForm.generateValuesForStatistics();
        Project project = projekteForm.getMyProjekt();

        assertEquals("Number of pages was not counted correctly!", Integer.valueOf(50), project.getNumberOfPages());
        assertEquals("Number of volumes was not counted correctly!", Integer.valueOf(3), project.getNumberOfVolumes());
    }

    @Test
    public void shouldGetStatisticsManager() throws Exception {
        ProjekteForm projekteForm = new ProjekteForm();
        ProjectService projectService = new ServiceManager().getProjectService();

        Project initialProject = projectService.getById(1);

        projekteForm.setMyProjekt(initialProject);
        //TODO: think how to test this manager
        //StatisticsManager statisticsManager = projekteForm.getStatisticsManager1();

        //assertEquals("Statistics Manager was not counted correctly!", Integer.valueOf(50), statisticsManager.getNumberOfPages());
        //assertEquals("Statistics Manager was not counted correctly!", Integer.valueOf(3), statisticsManager.getNumberOfVolumes());
    }

    @Test
    public void shouldDuplicateProject() throws Exception {
        ProjekteForm projekteForm = new ProjekteForm();
        ProjectService projectService = new ServiceManager().getProjectService();

        Project initialProject = projectService.getById(1);

        projekteForm.duplicateProject(initialProject.getId());

        assertEquals(projekteForm.getMyProjekt().getFileFormatDmsExport(), initialProject.getFileFormatDmsExport());
    }
}
