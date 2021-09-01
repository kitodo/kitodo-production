package org.kitodo.production.workflow.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.helper.Helper;

public class ConverterIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertRolesFull();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldValidateConditionalWorkflowTaskList() throws Exception {
        Converter converter = new Converter("gateway-test1");

        List<Task> tasks = converter.validateWorkflowTaskList();
        assertEquals("Process definition - workflow was read incorrectly!", 5, tasks.size());

        tasks.sort(Comparator.comparingInt(Task::getOrdering).thenComparing(Task::getWorkflowId));

        assertCorrectTask(tasks.get(0), "Task1", 1);
        assertFalse("Process definition - workflow's task last property were determined incorrectly!",
            tasks.get(0).isLast());

        assertCorrectTask(tasks.get(1), "ScriptTask", 2, "/mets:mets/mets:metsHdr");

        assertCorrectTask(tasks.get(2), "Task3", 2, "/mets:nothing");

        assertCorrectTask(tasks.get(3), "Task4", 2, "/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/kitodo:kitodo");

        assertCorrectTask(tasks.get(4), "Task5", 3);
        assertTrue("Process definition - workflow's task last property were determined incorrectly!",
            tasks.get(4).isLast());
    }

    @Test
    public void shouldConvertConditionalWorkflowToTemplate() throws Exception {
        Converter converter = new Converter("gateway-test1");

        Template template = new Template();
        converter.convertWorkflowToTemplate(template);
        assertEquals("Process definition - workflow was read incorrectly!", 5, template.getTasks().size());

        List<Task> tasks = template.getTasks();
        tasks.sort(Comparator.comparingInt(Task::getOrdering).thenComparing(Task::getWorkflowId));

        assertCorrectTask(tasks.get(0), "Task1", 1);
        assertFalse("Process definition - workflow's task last property were determined incorrectly!",
            tasks.get(0).isLast());

        assertCorrectTask(tasks.get(0), "Task1", 1);
        assertFalse("Process definition - workflow's task last property were determined incorrectly!",
            tasks.get(0).isLast());

        assertCorrectTask(tasks.get(1), "ScriptTask", 2, "/mets:mets/mets:metsHdr");

        assertCorrectTask(tasks.get(2), "Task3", 2, "/mets:nothing");

        assertCorrectTask(tasks.get(3), "Task4", 2, "/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/kitodo:kitodo");

        assertCorrectTask(tasks.get(4), "Task5", 3);
        assertTrue("Process definition - workflow's task last property were determined incorrectly!",
            tasks.get(4).isLast());
    }

    @Test
    public void shouldNotConvertConditionalWorkflowToTemplate() throws Exception {
        Converter converter = new Converter("gateway-test2");

        Template template = new Template();
        exception.expect(WorkflowException.class);
        exception.expectMessage(Helper.getTranslation("workflowExceptionParallelBranch",
            "Task9"));
        converter.convertWorkflowToTemplate(template);
    }

    @Test
    public void shouldNotConvertWorkflowWithoutRoleToTemplate() throws Exception {
        Converter converter = new Converter("gateway-test4");

        Template template = new Template();
        exception.expect(WorkflowException.class);
        exception.expectMessage(Helper.getTranslation("workflowExceptionMissingRoleAssignment",
            "Task1"));
        converter.convertWorkflowToTemplate(template);
    }

    @Test
    public void shouldConvertConditionalWorkflowWithTwoEndsToTemplate() throws Exception {
        Converter converter = new Converter("gateway-test3");

        Template template = new Template();
        converter.convertWorkflowToTemplate(template);
        assertEquals("Process definition - workflow was read incorrectly!", 7, template.getTasks().size());

        List<Task> tasks = template.getTasks();
        tasks.sort(Comparator.comparingInt(Task::getOrdering).thenComparing(Task::getWorkflowId));

        assertCorrectTask(tasks.get(0), "Task1", 1);
        assertFalse("Process definition - workflow's task last property were determined incorrectly!",
            tasks.get(0).isLast());

        assertCorrectTask(tasks.get(1), "Task2", 2);

        assertCorrectTask(tasks.get(2), "Task3", 3, "type=2");
        assertFalse("Process definition - workflow's task last property were determined incorrectly!",
            tasks.get(2).isLast());

        assertCorrectTask(tasks.get(3), "Task7", 3, "type=1");
        assertTrue("Process definition - workflow's task last property were determined incorrectly!",
            tasks.get(3).isLast());

        assertCorrectTask(tasks.get(4), "Task4", 4, "type=2");

        assertCorrectTask(tasks.get(5), "Task5", 4, "type=2");

        assertCorrectTask(tasks.get(6), "Task6", 5, "type=2");
        assertTrue("Process definition - workflow's task last property were determined incorrectly!",
            tasks.get(6).isLast());
    }

    private void assertCorrectTask(Task task, String title, int ordering) {
        assertCorrectTask(task, title, ordering, null);
    }

    private void assertCorrectTask(Task task, String title, int ordering, String workflowCondition) {
        assertEquals("Process definition - workflow's task title was read incorrectly!", title, task.getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", ordering,
            task.getOrdering().intValue());
        if (Objects.nonNull(workflowCondition)) {
            assertEquals("Process definition - workflow's task conditions were determined incorrectly!", workflowCondition,
                    task.getWorkflowCondition().getValue());
        }
    }
}
