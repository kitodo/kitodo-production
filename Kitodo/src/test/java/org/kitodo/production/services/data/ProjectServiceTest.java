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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.services.ServiceManager;

public class ProjectServiceTest {

    @Test
    public void testProjectForCompletness() throws IOException {
        ProjectService projectService = ServiceManager.getProjectService();

        // A project without dmsExportFormat, internal format or templates
        Project project = new Project();
        Assert.assertFalse("Project shouldn't be complete", projectService.isProjectComplete(project));

        // Add title, still not complete
        project.setTitle("testProject");
        Assert.assertFalse("Project shouldn't be complete", projectService.isProjectComplete(project));

        // Add xmls to complete project
        KitodoConfigFile.PROJECT_CONFIGURATION.getFile().createNewFile();
        Assert.assertTrue("Project should be complete", projectService.isProjectComplete(project));
        KitodoConfigFile.PROJECT_CONFIGURATION.getFile().delete();
    }

}
