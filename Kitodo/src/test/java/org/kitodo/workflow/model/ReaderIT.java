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

package org.kitodo.workflow.model;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReaderIT {

    private static FileService fileService = new ServiceManager().getFileService();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();

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
    public void shouldConvertConditionalWorkflowToTemplate() throws Exception {
        Reader reader = new Reader("gateway");
        reader.loadProcess();

        Template template = reader.convertWorkflowToTemplate();
        assertEquals("Process definition - workflow was read incorrectly!", 5, template.getTasks().size());

        List<Task> tasks = template.getTasks();
        tasks.sort(Comparator.comparing(Task::getOrdering, Comparator.nullsFirst(Comparator.naturalOrder())));

        for (Task task : tasks) {
            System.out.println(task.getOrdering() + " " + task.getTitle() + " " + task.getWorkflowCondition());
        }

        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 1,
                tasks.get(0).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "default",
                tasks.get(0).getWorkflowCondition());

        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 5,
                tasks.get(4).getOrdering().intValue());
        assertTrue("Process definition - workflow's task conditions were determined incorrectly!",
                tasks.get(4).getWorkflowCondition().contains("default")
                        && tasks.get(4).getWorkflowCondition().contains("${type == 1}")
                        && tasks.get(4).getWorkflowCondition().contains("${type == 2}"));
    }
}
