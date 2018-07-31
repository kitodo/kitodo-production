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

import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import org.kitodo.workflow.model.beans.Diagram;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReaderTest {

    private static FileService fileService = new ServiceManager().getFileService();

    @BeforeClass
    public static void setUp() throws Exception {
        fileService.createDirectory(URI.create(""), "diagrams");

        FileLoader.createExtendedDiagramTestFile();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        FileLoader.deleteExtendedDiagramTestFile();

        fileService.delete(URI.create("diagrams"));
    }

    @Test
    public void shouldLoadProcess() throws Exception {
        Reader reader = new Reader("extended-test");

        boolean result = Objects.nonNull(reader.getModelInstance());
        assertTrue("Process definition was not loaded!", result);
    }

    @Test
    public void shouldGetWorkflow() throws Exception {
        Reader reader = new Reader("extended-test");

        Diagram workflow = reader.getWorkflow();
        assertEquals("Process definition - workflow was read incorrectly!", "say-hello", workflow.getTitle());
    }

    @Test
    public void shouldConvertWorkflowToTasks() throws Exception {
        Reader reader = new Reader("extended-test");

        Workflow workflow = new Workflow();
        workflow.setTitle("Title");
        workflow = reader.convertTasks(workflow);
        workflow.getTasks().sort(Comparator.comparing(Task::getOrdering));

        Task task = workflow.getTasks().get(0);
        assertEquals("Process definition - workflow's task was read incorrectly!", "Say hello", task.getTitle());
        assertEquals("Process definition - workflow's task was read incorrectly!", 1, task.getPriority().intValue());
        assertEquals("Process definition - workflow's task was read incorrectly!", 1, task.getOrdering().intValue());

        task = workflow.getTasks().get(1);
        assertEquals("Process definition - workflow's task was read incorrectly!", "Test script", task.getScriptName());
        assertNull("Process definition - workflow's task was read incorrectly!", task.getScriptPath());
        assertEquals("Process definition - workflow's task was read incorrectly!", 2, task.getOrdering().intValue());
    }
}
