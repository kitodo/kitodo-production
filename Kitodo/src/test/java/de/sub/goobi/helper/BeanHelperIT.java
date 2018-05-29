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

package de.sub.goobi.helper;

import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Template;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import org.kitodo.workflow.model.Reader;

import static org.junit.Assert.assertEquals;

public class BeanHelperIT {

    private static FileService fileService = new ServiceManager().getFileService();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertDockets();
        MockDatabase.insertRulesets();
        fileService.createDirectory(URI.create(""), "diagrams");

        FileLoader.createExtendedGatewayDiagramTestFile();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();

        FileLoader.deleteExtendedGatewayDiagramTestFile();

        fileService.delete(URI.create("diagrams"));
    }

    @Test
    public void shouldCopyTasks() throws Exception {
        Reader reader = new Reader("gateway");

        Template template = reader.convertWorkflowToTemplate();
        Process process = new Process();

        BeanHelper.copyTasks(template, process);
        int actual = process.getTasks().size();
        assertEquals("Task were copied incorrectly!", 5, actual);
    }
}
