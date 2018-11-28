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

import java.util.Comparator;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.exceptions.WorkflowException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ReaderIT {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldConvertConditionalWorkflowToTemplate() throws Exception {
        Reader reader = new Reader("gateway-test1");

        Template template = new Template();
        template = reader.convertWorkflowToTemplate(template);
        assertEquals("Process definition - workflow was read incorrectly!", 5, template.getTasks().size());

        List<Task> tasks = template.getTasks();
        tasks.sort(Comparator.comparingInt(Task::getOrdering).thenComparing(Task::getWorkflowId));

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task1",
                tasks.get(0).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 1,
                tasks.get(0).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
                tasks.get(0).getWorkflowCondition());
        assertFalse("Process definition - workflow's task last property were determined incorrectly!",
                tasks.get(0).isLast());

        assertEquals("Process definition - workflow's task title was read incorrectly!", "ScriptTask",
                tasks.get(1).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 2,
                tasks.get(1).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "${type==1}",
                tasks.get(1).getWorkflowCondition());

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task3",
                tasks.get(2).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 2,
                tasks.get(2).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "${type==2}",
                tasks.get(2).getWorkflowCondition());

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task4",
                tasks.get(3).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 2,
                tasks.get(3).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "default",
                tasks.get(3).getWorkflowCondition());

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task5",
                tasks.get(4).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 3,
                tasks.get(4).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
                tasks.get(4).getWorkflowCondition());
        assertTrue("Process definition - workflow's task last property were determined incorrectly!",
                tasks.get(4).isLast());
    }

    @Test
    public void shouldNotConvertConditionalWorkflowToTemplate() throws Exception {
        Reader reader = new Reader("gateway-test2");

        Template template = new Template();
        exception.expect(WorkflowException.class);
        exception.expectMessage("Task in parallel branch can not have second task. Please remove task after task with name 'Task9'.");
        reader.convertWorkflowToTemplate(template);
    }

    @Test
    public void shouldConvertConditionalWorkflowWithTwoEndsToTemplate() throws Exception {
        Reader reader = new Reader("gateway-test3");

        Template template = new Template();
        template = reader.convertWorkflowToTemplate(template);
        assertEquals("Process definition - workflow was read incorrectly!", 7, template.getTasks().size());

        List<Task> tasks = template.getTasks();
        tasks.sort(Comparator.comparingInt(Task::getOrdering).thenComparing(Task::getWorkflowId));

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task1",
                tasks.get(0).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 1,
                tasks.get(0).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
                tasks.get(0).getWorkflowCondition());
        assertFalse("Process definition - workflow's task last property were determined incorrectly!",
                tasks.get(0).isLast());

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task2",
                tasks.get(1).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 2,
                tasks.get(1).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
                tasks.get(1).getWorkflowCondition());

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task3",
                tasks.get(2).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 3,
                tasks.get(2).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=2",
                tasks.get(2).getWorkflowCondition());
        assertFalse("Process definition - workflow's task last property were determined incorrectly!",
                tasks.get(2).isLast());

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task7",
                tasks.get(3).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 3,
                tasks.get(3).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=1",
                tasks.get(3).getWorkflowCondition());
        assertTrue("Process definition - workflow's task last property were determined incorrectly!",
                tasks.get(3).isLast());

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task4",
                tasks.get(4).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 4,
                tasks.get(4).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=2",
                tasks.get(4).getWorkflowCondition());

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task5",
                tasks.get(5).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 4,
                tasks.get(5).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=2",
                tasks.get(5).getWorkflowCondition());

        assertEquals("Process definition - workflow's task title was read incorrectly!", "Task6",
                tasks.get(6).getTitle());
        assertEquals("Process definition - workflow's task ordering was determined incorrectly!", 5,
                tasks.get(6).getOrdering().intValue());
        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=2",
                tasks.get(6).getWorkflowCondition());
        assertTrue("Process definition - workflow's task last property were determined incorrectly!",
                tasks.get(6).isLast());
    }
}
