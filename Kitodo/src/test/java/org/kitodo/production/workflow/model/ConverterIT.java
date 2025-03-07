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

package org.kitodo.production.workflow.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.exceptions.WorkflowException;
import org.kitodo.production.helper.Helper;

public class ConverterIT {

    @BeforeAll
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertRolesFull();
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldValidateConditionalWorkflowTaskList() throws Exception {
        Converter converter = new Converter("gateway-test1");

        List<Task> tasks = converter.validateWorkflowTaskList();
        assertEquals(5, tasks.size(), "Process definition - workflow was read incorrectly!");

        tasks.sort(Comparator.comparingInt(Task::getOrdering).thenComparing(Task::getWorkflowId));

        assertCorrectTask(tasks.get(0), "Task1", 1);
        assertFalse(tasks.get(0).isLast(), "Process definition - workflow's task last property were determined incorrectly!");

        assertCorrectTask(tasks.get(1), "ScriptTask", 2, "/mets:mets/mets:metsHdr");

        assertCorrectTask(tasks.get(2), "Task3", 2, "/mets:nothing");

        assertCorrectTask(tasks.get(3), "Task4", 2, "/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/kitodo:kitodo");

        assertCorrectTask(tasks.get(4), "Task5", 3);
        assertTrue(tasks.get(4).isLast(), "Process definition - workflow's task last property were determined incorrectly!");
    }

    @Test
    public void shouldConvertConditionalWorkflowToTemplate() throws Exception {
        Converter converter = new Converter("gateway-test1");

        Template template = new Template();
        converter.convertWorkflowToTemplate(template);
        assertEquals(5, template.getTasks().size(), "Process definition - workflow was read incorrectly!");

        List<Task> tasks = template.getTasks();
        tasks.sort(Comparator.comparingInt(Task::getOrdering).thenComparing(Task::getWorkflowId));

        assertCorrectTask(tasks.get(0), "Task1", 1);
        assertFalse(tasks.get(0).isLast(), "Process definition - workflow's task last property were determined incorrectly!");

        assertCorrectTask(tasks.get(0), "Task1", 1);
        assertFalse(tasks.get(0).isLast(), "Process definition - workflow's task last property were determined incorrectly!");

        assertCorrectTask(tasks.get(1), "ScriptTask", 2, "/mets:mets/mets:metsHdr");

        assertCorrectTask(tasks.get(2), "Task3", 2, "/mets:nothing");

        assertCorrectTask(tasks.get(3), "Task4", 2, "/mets:mets/mets:dmdSec/mets:mdWrap/mets:xmlData/kitodo:kitodo");

        assertCorrectTask(tasks.get(4), "Task5", 3);
        assertTrue(tasks.get(4).isLast(), "Process definition - workflow's task last property were determined incorrectly!");
    }

    @Test
    public void shouldNotConvertConditionalWorkflowToTemplate() throws Exception {
        Converter converter = new Converter("gateway-test2");

        Template template = new Template();
        Exception exception = assertThrows(WorkflowException.class, () -> converter.convertWorkflowToTemplate(template));
        assertEquals(Helper.getTranslation("workflowExceptionParallelBranch", "Task9"), exception.getMessage());
    }

    @Test
    public void shouldNotConvertWorkflowWithoutRoleToTemplate() throws Exception {
        Converter converter = new Converter("gateway-test4");

        Template template = new Template();
        Exception exception = assertThrows(WorkflowException.class, () -> converter.convertWorkflowToTemplate(template));
        assertEquals(Helper.getTranslation("workflowExceptionMissingRoleAssignment", "Task1"), exception.getMessage());
    }

    @Test
    public void shouldConvertConditionalWorkflowWithTwoEndsToTemplate() throws Exception {
        Converter converter = new Converter("gateway-test3");

        Template template = new Template();
        converter.convertWorkflowToTemplate(template);
        assertEquals(7, template.getTasks().size(), "Process definition - workflow was read incorrectly!");

        List<Task> tasks = template.getTasks();
        tasks.sort(Comparator.comparingInt(Task::getOrdering).thenComparing(Task::getWorkflowId));

        assertCorrectTask(tasks.get(0), "Task1", 1);
        assertFalse(tasks.get(0).isLast(), "Process definition - workflow's task last property were determined incorrectly!");

        assertCorrectTask(tasks.get(1), "Task2", 2);

        assertCorrectTask(tasks.get(2), "Task3", 3, "type=2");
        assertFalse(tasks.get(2).isLast(), "Process definition - workflow's task last property were determined incorrectly!");

        assertCorrectTask(tasks.get(3), "Task7", 3, "type=1");
        assertTrue(tasks.get(3).isLast(), "Process definition - workflow's task last property were determined incorrectly!");

        assertCorrectTask(tasks.get(4), "Task4", 4, "type=2");

        assertCorrectTask(tasks.get(5), "Task5", 4, "type=2");

        assertCorrectTask(tasks.get(6), "Task6", 5, "type=2");
        assertTrue(tasks.get(6).isLast(), "Process definition - workflow's task last property were determined incorrectly!");
    }

    private void assertCorrectTask(Task task, String title, int ordering) {
        assertCorrectTask(task, title, ordering, null);
    }

    private void assertCorrectTask(Task task, String title, int ordering, String workflowCondition) {
        assertEquals(title, task.getTitle(), "Process definition - workflow's task title was read incorrectly!");
        assertEquals(ordering, task.getOrdering().intValue(), "Process definition - workflow's task ordering was determined incorrectly!");
        if (Objects.nonNull(workflowCondition)) {
            assertEquals(workflowCondition, task.getWorkflowCondition().getValue(), "Process definition - workflow's task conditions were determined incorrectly!");
        }
    }
}
