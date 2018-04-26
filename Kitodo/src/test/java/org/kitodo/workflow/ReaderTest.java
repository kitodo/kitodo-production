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

package org.kitodo.workflow;

import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;
import org.kitodo.workflow.model.Reader;
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
        Reader reader = new Reader("test");
        reader.loadProcess();
        boolean result = Objects.nonNull(reader.getModelInstance());

        assertTrue("Process definition was not loaded!", result);
    }

    @Test
    public void shouldGetWorkflow() throws Exception {
        Reader reader = new Reader("test");
        reader.loadProcess();
        Diagram workflow = reader.getWorkflow();

        assertEquals("Process definition - workflow was read incorrectly!", workflow.getTitle(), "say-hello");
        assertEquals("Process definition - workflow was read incorrectly!", workflow.getOutputName(), "Say Hello");
    }

    @Test
    public void shouldConvertWorkflowToTemplate() throws Exception {
        Reader reader = new Reader("test");
        reader.loadProcess();

        Template template = reader.convertWorkflowToTemplate();

        assertEquals("Process definition - workflow was read incorrectly!", template.getTitle(), "say-hello");
        assertEquals("Process definition - workflow was read incorrectly!", template.getOutputName(), "Say Hello");

        Task task = template.getTasks().get(0);
        assertEquals("Process definition - workflow's task was read incorrectly!", task.getTitle(), "Say hello");
        assertEquals("Process definition - workflow's task was read incorrectly!", task.getPriority().intValue(), 1);
        assertEquals("Process definition - workflow's task was read incorrectly!", task.getOrdering().intValue(), 1);

        task = template.getTasks().get(1);
        assertEquals("Process definition - workflow's task was read incorrectly!", task.getScriptName(), "Test script");
        assertNull("Process definition - workflow's task was read incorrectly!", task.getScriptPath());
        assertEquals("Process definition - workflow's task was read incorrectly!", task.getOrdering().intValue(), 2);
    }
}
