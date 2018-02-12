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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.services.ServiceManager;

public class ProjectServiceTest {

    @Test
    public void testProjectForCompletness() {
        ProjectService projectService = new ServiceManager().getProjectService();

        Project project = new Project();
        Assert.assertFalse("A project without anything shouldn't be complete",
            projectService.isProjectComplete(project));

        project.setTitle("testProject");
        Assert.assertFalse("Project with added title shouldn't be complete", projectService.isProjectComplete(project));

        project.setFileFormatDmsExport("METS");
        project.setFileFormatInternal("METS");
        Assert.assertFalse("Project with added dms and internal format shouldn't be complete",
            projectService.isProjectComplete(project));

        Process process = new Process();
        List<Process> templates = Arrays.asList(process);
        project.template = templates;
        Assert.assertTrue("Project with added templates should be complete", projectService.isProjectComplete(project));
    }
}
