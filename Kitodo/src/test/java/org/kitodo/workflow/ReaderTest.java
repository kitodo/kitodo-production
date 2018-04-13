package org.kitodo.workflow;

import java.io.IOException;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
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
import org.kitodo.workflow.model.beans.Workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ReaderTest {

    private static FileService fileService = new ServiceManager().getFileService();

    @BeforeClass
    public static void setUp() throws Exception {
        fileService.createDirectory(URI.create(""), "diagrams");

        FileLoader.createExtendedDiagramTestFile();
        FileLoader.createExtendedGatewayDiagramTestFile();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        FileLoader.deleteExtendedDiagramTestFile();
        FileLoader.deleteExtendedGatewayDiagramTestFile();

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
        Workflow workflow = reader.getWorkflow();

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
