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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Template;
import org.kitodo.services.ServiceManager;

public class ProjectServiceTest {

    private static String absolutePath = KitodoConfigFile.DIGITAL_COLLECTIONS.getAbsolutePath();

    @BeforeClass
    public static void setUp() throws Exception {
        Files.createFile(Paths.get(absolutePath));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        Files.deleteIfExists(Paths.get(absolutePath));
    }

    @Test
    public void testProjectForCompletness() throws IOException {
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
        Template template = new Template();
        project.template = new ArrayList<>(Collections.singleton(template));
        Assert.assertFalse("Project shouldn't be complete", projectService.isProjectComplete(project));

        // Add xmls to complete project
        KitodoConfigFile.PROJECT_CONFIGURATION.getFile().createNewFile();
        Assert.assertTrue("Project should be complete", projectService.isProjectComplete(project));
        KitodoConfigFile.PROJECT_CONFIGURATION.getFile().delete();

    }

}
