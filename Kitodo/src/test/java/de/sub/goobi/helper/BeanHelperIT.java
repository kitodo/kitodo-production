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
import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import org.kitodo.workflow.model.Reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    public void shouldCopyTasksWithoutWorkflowConditions() throws Exception {
        Reader reader = new Reader("gateway");
        reader.loadProcess();

        Template template = reader.convertWorkflowToTemplate();
        Process process = new Process();

        BeanHelper.copyTasks(template, process, null);
        int actual = process.getTasks().size();
        assertEquals("Task were copied incorrectly for cases without conditions!", 2, actual);
    }

    @Test
    public void shouldCopyTasksWithWorkflowConditions() throws Exception {
        Reader reader = new Reader("gateway");
        reader.loadProcess();

        Template template = reader.convertWorkflowToTemplate();
        Process process = new Process();

        List<String> workflowConditions = new ArrayList<>();
        workflowConditions.add("${type == 1}");
        BeanHelper.copyTasks(template, process, workflowConditions);
        List<Task> tasks = process.getTasks();
        int actual = tasks.size();
        assertEquals("Task were copied incorrectly for cases without conditions!", 3, actual);
        boolean condition = tasks.get(0).getTitle().equals("First task") && tasks.get(0).getOrdering().equals(1);
        assertTrue("First task was not created correctly!", condition);
        condition = tasks.get(1).getTitle().equals("Script task") && tasks.get(1).getOrdering().equals(2);
        assertTrue("Second task was not created correctly!", condition);
        condition = tasks.get(2).getTitle().equals("Ending task") && tasks.get(2).getOrdering().equals(3);
        assertTrue("Third task was not created correctly!", condition);

        workflowConditions = new ArrayList<>();
        workflowConditions.add("${type == 2}");
        BeanHelper.copyTasks(template, process, workflowConditions);
        tasks = process.getTasks();
        actual = tasks.size();
        assertEquals("Task were copied incorrectly for cases without conditions!", 4, actual);
        condition = tasks.get(0).getTitle().equals("First task") && tasks.get(0).getOrdering().equals(1);
        assertTrue("First task was not created correctly!", condition);
        condition = tasks.get(1).getTitle().equals("Some normal task") && tasks.get(1).getOrdering().equals(2);
        assertTrue("Second task was not created correctly!", condition);
        condition = tasks.get(2).getTitle().equals("Some other normal") && tasks.get(2).getOrdering().equals(3);
        assertTrue("Third task was not created correctly!", condition);
        condition = tasks.get(3).getTitle().equals("Ending task") && tasks.get(3).getOrdering().equals(4);
        assertTrue("Fourth task was not created correctly!", condition);
    }
}
