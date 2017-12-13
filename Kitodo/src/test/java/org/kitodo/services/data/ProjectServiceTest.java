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

package org.kitodo.services.data;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.goobi.production.constants.FileNames;
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.services.ServiceManager;

public class ProjectServiceTest {

    @Test
    public void testProjectForCompletness() throws DAOException, IOException {
        ProjectService projectService = new ServiceManager().getProjectService();

        // A project without dmsExportFormat, internal format or templates
        Project project = new Project();
        Assert.assertFalse("Project shouldn't be complete", projectService.isProjectComplete(project));

        // Add title, still not complete
        project.setTitle("testProject");
        Assert.assertFalse("Project shouldn't be complete", projectService.isProjectComplete(project));

        // Add dms and internal format, still not complete
        project.setFileFormatDmsExport("METS");
        project.setFileFormatInternal("METS");
        Assert.assertFalse("Project shouldn't be complete", projectService.isProjectComplete(project));

        // Add templates, still not complete
        Process process = new Process();
        List<Process> templates = Arrays.asList(process);
        project.template = templates;
        Assert.assertFalse("Project shouldn't be complete", projectService.isProjectComplete(project));

        // Add xmls to complete project
        File projectsXml = new File("src/test/resources/" + FileNames.PROJECT_CONFIGURATION_FILE);
        projectsXml.createNewFile();
        File digitalCollectionsXml = new File("src/test/resources/" + FileNames.DIGITAL_COLLECTIONS_FILE);
        digitalCollectionsXml.createNewFile();
        Assert.assertTrue("Project should be complete", projectService.isProjectComplete(project));
        projectsXml.delete();
        digitalCollectionsXml.delete();

    }

}
