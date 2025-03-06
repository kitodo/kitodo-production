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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.data.database.beans.Project;
import org.kitodo.production.services.ServiceManager;

public class ProjectServiceTest {

    @Test
    public void testProjectForCompletness() throws IOException {
        ProjectService projectService = ServiceManager.getProjectService();

        // A project without dmsExportFormat, internal format or templates
        Project project = new Project();
        assertFalse(projectService.isProjectComplete(project), "Project shouldn't be complete");

        // Add title, still not complete
        project.setTitle("testProject");
        assertFalse(projectService.isProjectComplete(project), "Project shouldn't be complete");

        // Add xmls to complete project
        KitodoConfigFile.PROJECT_CONFIGURATION.getFile().createNewFile();
        assertTrue(projectService.isProjectComplete(project), "Project should be complete");
        KitodoConfigFile.PROJECT_CONFIGURATION.getFile().delete();
    }

}
