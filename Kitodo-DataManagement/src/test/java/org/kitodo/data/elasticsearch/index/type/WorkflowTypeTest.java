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

package org.kitodo.data.elasticsearch.index.type;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.WorkflowTypeField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkflowTypeTest {

    private static List<Workflow> prepareData() {

        List<Workflow> workflows = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();

        Task firstTask = new Task();
        firstTask.setId(1);
        firstTask.setTitle("Task one");
        tasks.add(firstTask);

        Task secondTask = new Task();
        secondTask.setId(2);
        secondTask.setTitle("Task two");
        tasks.add(secondTask);

        Workflow firstWorkflow = new Workflow();
        firstWorkflow.setId(1);
        firstWorkflow.setTitle("Testing");
        firstWorkflow.setFileName("Testing");
        firstWorkflow.setTasks(tasks);
        workflows.add(firstWorkflow);

        Workflow secondWorkflow = new Workflow();
        secondWorkflow.setId(2);
        secondWorkflow.setTitle("Rendering");
        secondWorkflow.setFileName("Rendering");
        workflows.add(secondWorkflow);

        Workflow thirdWorkflow = new Workflow();
        thirdWorkflow.setId(3);
        thirdWorkflow.setTitle("Incomplete");
        workflows.add(thirdWorkflow);

        return workflows;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        WorkflowType workflowType = new WorkflowType();

        Workflow workflow = prepareData().get(0);
        HttpEntity document = workflowType.createDocument(workflow);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Testing",
                WorkflowTypeField.TITLE.getStringValue(actual));
        assertEquals("Key fileName doesn't match to given value!", "Testing",
                WorkflowTypeField.FILE_NAME.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!",
                WorkflowTypeField.ACTIVE.getBooleanValue(actual));
        assertFalse("Key ready doesn't match to given value!",
                WorkflowTypeField.READY.getBooleanValue(actual));

        JsonArray tasks = WorkflowTypeField.TASKS.getJsonArray(actual);
        assertEquals("Size tasks doesn't match to given value!", 2, tasks.size());

        JsonObject task = tasks.getJsonObject(0);
        assertEquals("Key tasks.id doesn't match to given value!", 1, TaskTypeField.ID.getIntValue(task));
        assertEquals("Key tasks.title doesn't match to given value!", "Task one",
                TaskTypeField.TITLE.getStringValue(task));

        task = tasks.getJsonObject(1);
        assertEquals("Key tasks.id doesn't match to given value!", 2, TaskTypeField.ID.getIntValue(task));
        assertEquals("Key tasks.title doesn't match to given value!", "Task two",
                TaskTypeField.TITLE.getStringValue(task));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        WorkflowType workflowType = new WorkflowType();

        Workflow workflow = prepareData().get(1);
        HttpEntity document = workflowType.createDocument(workflow);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Rendering",
                WorkflowTypeField.TITLE.getStringValue(actual));
        assertEquals("Key outputName doesn't match to given value!", "Rendering",
                WorkflowTypeField.FILE_NAME.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!",
                WorkflowTypeField.ACTIVE.getBooleanValue(actual));
        assertFalse("Key ready doesn't match to given value!",
                WorkflowTypeField.READY.getBooleanValue(actual));

        JsonArray tasks = WorkflowTypeField.TASKS.getJsonArray(actual);
        assertEquals("Size tasks doesn't match to given value!", 0, tasks.size());
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        WorkflowType workflowType = new WorkflowType();

        Workflow workflow = prepareData().get(2);
        HttpEntity document = workflowType.createDocument(workflow);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Incomplete",
                WorkflowTypeField.TITLE.getStringValue(actual));
        assertEquals("Key outputName doesn't match to given value!", "",
                WorkflowTypeField.FILE_NAME.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!",
                WorkflowTypeField.ACTIVE.getBooleanValue(actual));
        assertFalse("Key ready doesn't match to given value!",
                WorkflowTypeField.READY.getBooleanValue(actual));

        JsonArray tasks = WorkflowTypeField.TASKS.getJsonArray(actual);
        assertEquals("Size tasks doesn't match to given value!", 0, tasks.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        WorkflowType workflowType = new WorkflowType();

        Workflow workflow = prepareData().get(0);
        HttpEntity document = workflowType.createDocument(workflow);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 7, actual.keySet().size());

        JsonArray tasks = WorkflowTypeField.TASKS.getJsonArray(actual);
        JsonObject task = tasks.getJsonObject(0);
        assertEquals("Amount of keys in tasks is incorrect!", 2, task.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        WorkflowType workflowType = new WorkflowType();

        List<Workflow> workflows = prepareData();
        Map<Integer, HttpEntity> documents = workflowType.createDocuments(workflows);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}
