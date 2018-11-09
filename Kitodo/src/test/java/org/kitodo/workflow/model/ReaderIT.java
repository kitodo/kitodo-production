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
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReaderIT {

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
    public void shouldConvertFirstConditionalWorkflowToTemplate() throws Exception {
        Reader reader = new Reader("gateway-test1");

        Template template = new Template();
        template.setTitle("Title");
        template = reader.convertWorkflowToTemplate(template);
        assertEquals("Process definition - workflow was read incorrectly!", 5, template.getTasks().size());

        List<Task> tasks = template.getTasks();
        tasks.sort(Comparator.comparing(Task::getWorkflowId, Comparator.nullsFirst(Comparator.naturalOrder())));

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(0).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "",
            tasks.get(0).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(0).getConcurrentTasks());
        String nextTasks = tasks.get(0).getNextTasks();
        assertTrue("Process definition - workflow's next tasks were determined incorrectly!",
            nextTasks.contains("Task2") && nextTasks.contains("Task3") && nextTasks.contains("Task4"));

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "${type==1}",
            tasks.get(1).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task1",
            tasks.get(1).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(1).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task5",
            tasks.get(1).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "${type==2}",
            tasks.get(2).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task1",
            tasks.get(2).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(2).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task5",
            tasks.get(2).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "default",
            tasks.get(3).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task1",
            tasks.get(3).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(3).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task5",
            tasks.get(3).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(4).getWorkflowCondition());
        String previousTasks = tasks.get(4).getPreviousTasks();
        assertTrue("Process definition - workflow's previous tasks were determined incorrectly!",
            previousTasks.contains("Task2") && previousTasks.contains("Task3") && previousTasks.contains("Task4"));
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(4).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "",
            tasks.get(4).getNextTasks());
    }

    @Test
    public void shouldConvertSecondConditionalWorkflowToTemplate() throws Exception {
        Reader reader = new Reader("gateway-test2");

        Template template = new Template();
        template.setTitle("Title");
        template = reader.convertWorkflowToTemplate(template);
        assertEquals("Process definition - workflow was read incorrectly!", 14, template.getTasks().size());

        List<Task> tasks = template.getTasks();
        tasks.sort(Comparator.comparing(Task::getWorkflowId, Comparator.nullsFirst(Comparator.naturalOrder())));

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(0).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "",
            tasks.get(0).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(0).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task2",
            tasks.get(0).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(1).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task9",
            tasks.get(1).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(1).getConcurrentTasks());
        String nextTasks = tasks.get(1).getNextTasks();
        assertTrue("Process definition - workflow's next tasks were determined incorrectly!",
                nextTasks.contains("Task11") && nextTasks.contains("Task12") && nextTasks.contains("Task13"));

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(2).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task10",
            tasks.get(2).getPreviousTasks());
        String concurrentTasks = tasks.get(2).getConcurrentTasks();
        assertTrue("Process definition - workflow's concurrent tasks were determined incorrectly!",
            concurrentTasks.contains("Task12") && concurrentTasks.contains("Task13"));
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task14",
            tasks.get(2).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(3).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task10",
            tasks.get(3).getPreviousTasks());
        concurrentTasks = tasks.get(3).getConcurrentTasks();
        assertTrue("Process definition - workflow's concurrent tasks were determined incorrectly!",
            concurrentTasks.contains("Task11") && concurrentTasks.contains("Task13"));
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task14",
            tasks.get(3).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(4).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task10",
            tasks.get(4).getPreviousTasks());
        concurrentTasks = tasks.get(4).getConcurrentTasks();
        assertTrue("Process definition - workflow's concurrent tasks were determined incorrectly!",
                concurrentTasks.contains("Task11") && concurrentTasks.contains("Task12"));
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task14",
            tasks.get(4).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(5).getWorkflowCondition());
        String previousTasks = tasks.get(5).getPreviousTasks();
        assertTrue("Process definition - workflow's previous tasks were determined incorrectly!",
            previousTasks.contains("Task11") && previousTasks.contains("Task12") && previousTasks.contains("Task13"));
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(5).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "",
            tasks.get(5).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(6).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!",
            "Task1", tasks.get(6).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(6).getConcurrentTasks());
        nextTasks = tasks.get(6).getNextTasks();
        assertTrue("Process definition - workflow's next tasks were determined incorrectly!",
            nextTasks.contains("Task3") && nextTasks.contains("Task5") && nextTasks.contains("Task6"));

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=1",
            tasks.get(7).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!",
            "Task2", tasks.get(7).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(7).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task4",
            tasks.get(7).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=1",
            tasks.get(8).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!",
            "Task3", tasks.get(8).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(8).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task9",
            tasks.get(8).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=2",
            tasks.get(9).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!",
            "Task2", tasks.get(9).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(9).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task9",
            tasks.get(9).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=3",
            tasks.get(10).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!",
            "Task2", tasks.get(10).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(10).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task7",
            tasks.get(10).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=3",
            tasks.get(11).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!",
            "Task6", tasks.get(11).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(11).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task8",
            tasks.get(11).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=3",
            tasks.get(12).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!",
            "Task7", tasks.get(12).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(12).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task9",
            tasks.get(12).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(13).getWorkflowCondition());
        previousTasks = tasks.get(13).getPreviousTasks();
        assertTrue("Process definition - workflow's previous tasks were determined incorrectly!",
            previousTasks.contains("Task4") && previousTasks.contains("Task5") && previousTasks.contains("Task8"));
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(13).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task10",
            tasks.get(13).getNextTasks());
    }

    @Test
    public void shouldConvertThirdConditionalWorkflowToTemplate() throws Exception {
        Reader reader = new Reader("gateway-test3");

        Template template = new Template();
        template.setTitle("Title");
        template = reader.convertWorkflowToTemplate(template);
        assertEquals("Process definition - workflow was read incorrectly!", 12, template.getTasks().size());

        List<Task> tasks = template.getTasks();
        tasks.sort(Comparator.comparing(Task::getWorkflowId, Comparator.nullsFirst(Comparator.naturalOrder())));

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(0).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "",
            tasks.get(0).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(0).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task2",
            tasks.get(0).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=2",
            tasks.get(1).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task2",
            tasks.get(1).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(1).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task11",
            tasks.get(1).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=2",
            tasks.get(2).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task10",
            tasks.get(2).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(2).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "",
            tasks.get(2).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=3",
            tasks.get(3).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task2",
            tasks.get(3).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(3).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "",
            tasks.get(3).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "",
            tasks.get(4).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task1",
            tasks.get(4).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(4).getConcurrentTasks());
        String nextTasks = tasks.get(4).getNextTasks();
        assertTrue("Process definition - workflow's next tasks were determined incorrectly!",
            nextTasks.contains("Task3") && nextTasks.contains("Task10") && nextTasks.contains("Task12"));

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=1",
            tasks.get(5).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task2",
            tasks.get(5).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(5).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task4",
            tasks.get(5).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=1",
            tasks.get(6).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task3",
            tasks.get(6).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(6).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task5",
            tasks.get(6).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=1",
            tasks.get(7).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task4",
            tasks.get(7).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(7).getConcurrentTasks());
        nextTasks = tasks.get(7).getNextTasks();
        assertTrue("Process definition - workflow's next tasks were determined incorrectly!",
                nextTasks.contains("Task6") && nextTasks.contains("Task8"));

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=1",
            tasks.get(8).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task5",
            tasks.get(8).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "Task8",
            tasks.get(8).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task7",
                tasks.get(8).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=1",
            tasks.get(9).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task6",
            tasks.get(9).getPreviousTasks());
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "Task8",
            tasks.get(9).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task9",
            tasks.get(9).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=1",
            tasks.get(10).getWorkflowCondition());
        assertEquals("Process definition - workflow's previous tasks were determined incorrectly!", "Task5",
            tasks.get(10).getPreviousTasks());
        //TODO: fix cases for different amount of concurrent tasks in branches
        String concurrentTasks = tasks.get(10).getConcurrentTasks();
        System.out.println(concurrentTasks);
        /*assertTrue("Process definition - workflow's concurrent tasks were determined incorrectly!",
            concurrentTasks.contains("Task6") && concurrentTasks.contains("Task7"));*/
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "Task9",
            tasks.get(10).getNextTasks());

        assertEquals("Process definition - workflow's task conditions were determined incorrectly!", "type=1",
            tasks.get(11).getWorkflowCondition());
        String previousTasks = tasks.get(11).getPreviousTasks();
        assertTrue("Process definition - workflow's previous tasks were determined incorrectly!",
            previousTasks.contains("Task7") && previousTasks.contains("Task8"));
        assertEquals("Process definition - workflow's concurrent tasks were determined incorrectly!", "",
            tasks.get(11).getConcurrentTasks());
        assertEquals("Process definition - workflow's next tasks were determined incorrectly!", "",
            tasks.get(11).getNextTasks());
    }
}
