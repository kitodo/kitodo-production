package org.kitodo.services.data;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.exceptions.DAOException;

public class ProjectServiceTest {

    @Test
    public void testProjectForCompletness() throws DAOException {
        ProjectService projectService = new ProjectService();

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

        // Add templates -> complete
        Process process = new Process();
        List<Process> templates = Arrays.asList(process);
        project.template = templates;
        Assert.assertTrue("Project should be complete", projectService.isProjectComplete(project));

    }

}
