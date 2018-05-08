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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.elasticsearch.index.type.enums.BatchTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.PropertyTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;

/**
 * Test class for ProcessType.
 */
public class ProcessTypeTest {

    private static List<Process> prepareData() {

        List<Process> processes = new ArrayList<>();
        List<Property> properties = new ArrayList<>();
        List<Batch> batches = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();

        Batch batch = new Batch();
        batch.setId(1);
        batch.setTitle("First");
        batches.add(batch);

        Project project = new Project();
        project.setTitle("Project");
        project.setId(1);

        Ruleset ruleset = new Ruleset();
        ruleset.setId(1);

        Docket docket = new Docket();
        docket.setId(1);

        Property firstProperty = new Property();
        firstProperty.setId(1);
        properties.add(firstProperty);

        Property secondProperty = new Property();
        secondProperty.setId(2);
        properties.add(secondProperty);

        Task firstTask = new Task();
        firstTask.setId(1);
        firstTask.setTitle("Task one");
        tasks.add(firstTask);

        Task secondTask = new Task();
        secondTask.setId(2);
        secondTask.setTitle("Task two");
        tasks.add(secondTask);

        Process firstProcess = new Process();
        firstProcess.setId(1);
        firstProcess.setTitle("Testing");
        firstProcess.setOutputName("Test");
        LocalDate localDate = new LocalDate(2017, 1, 1);
        firstProcess.setCreationDate(localDate.toDate());
        firstProcess.setSortHelperImages(20);
        firstProcess.setBatches(batches);
        firstProcess.setTasks(tasks);
        firstProcess.setWikiField("Wiki");
        firstProcess.setProject(project);
        firstProcess.setRuleset(ruleset);
        processes.add(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setId(2);
        secondProcess.setTitle("Rendering");
        secondProcess.setOutputName("Render");
        secondProcess.setWikiField("Field");
        secondProcess.setSortHelperImages(30);
        secondProcess.setProject(project);
        secondProcess.setDocket(docket);
        secondProcess.setProperties(properties);
        processes.add(secondProcess);

        Process thirdProcess = new Process();
        thirdProcess.setId(3);
        thirdProcess.setTitle("Incomplete");
        processes.add(thirdProcess);

        return processes;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        ProcessType processType = new ProcessType();

        Process process = prepareData().get(0);
        HttpEntity document = processType.createDocument(process);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Testing",
            actual.getString(ProcessTypeField.TITLE.getName()));
        assertEquals("Key outputName doesn't match to given value!", "Test",
            actual.getString(ProcessTypeField.OUTPUT_NAME.getName()));
        assertEquals("Key wikiField doesn't match to given value!", "Wiki",
            actual.getString(ProcessTypeField.WIKI_FIELD.getName()));
        assertEquals("Key processBaseUri doesn't match to given value!", "",
            actual.getString(ProcessTypeField.PROCESS_BASE_URI.getName()));
        assertEquals("Key template.id doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.TEMPLATE_ID.getName()));
        assertEquals("Key template.title doesn't match to given value!", "",
            actual.getString(ProcessTypeField.TEMPLATE_TITLE.getName()));
        assertEquals("Key creationDate doesn't match to given value!", "2017-01-01",
            actual.getString(ProcessTypeField.CREATION_DATE.getName()));
        assertEquals("Key sortHelperStatus doesn't match to given value!", "",
            actual.getString(ProcessTypeField.SORT_HELPER_STATUS.getName()));
        assertEquals("Key sortHelperImages doesn't match to given value!", 20,
            actual.getInt(ProcessTypeField.SORT_HELPER_IMAGES.getName()));
        assertEquals("Key sortHelperArticles doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.SORT_HELPER_ARTICLES.getName()));
        assertEquals("Key sortHelperDocstructs doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getName()));
        assertEquals("Key sortHelperMetadata doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.SORT_HELPER_METADATA.getName()));
        assertEquals("Key project.id doesn't match to given value!", 1,
            actual.getInt(ProcessTypeField.PROJECT_ID.getName()));
        assertEquals("Key project.title doesn't match to given value!", "Project",
            actual.getString(ProcessTypeField.PROJECT_TITLE.getName()));
        assertTrue("Key project.active doesn't match to given value!",
            actual.getBoolean(ProcessTypeField.PROJECT_ACTIVE.getName()));
        assertEquals("Key docket doesn't match to given value!", 0, actual.getInt(ProcessTypeField.DOCKET.getName()));
        assertEquals("Key ruleset doesn't match to given value!", 1, actual.getInt(ProcessTypeField.RULESET.getName()));

        JsonArray templates = actual.getJsonArray(ProcessTypeField.TEMPLATES.getName());
        assertEquals("Size templates doesn't match to given value!", 0, templates.size());

        JsonArray properties = actual.getJsonArray(ProcessTypeField.PROPERTIES.getName());
        assertEquals("Size properties doesn't match to given value!", 0, properties.size());

        JsonArray workpieces = actual.getJsonArray(ProcessTypeField.WORKPIECES.getName());
        assertEquals("Size workpieces doesn't match to given value!", 0, workpieces.size());

        JsonArray batches = actual.getJsonArray(ProcessTypeField.BATCHES.getName());
        assertEquals("Size batches doesn't match to given value!", 1, batches.size());

        JsonObject batch = batches.getJsonObject(0);
        assertEquals("Key batches.id doesn't match to given value!", 1, batch.getInt(BatchTypeField.ID.getName()));
        assertEquals("Key batches.title doesn't match to given value!", "First",
            batch.getString(BatchTypeField.TITLE.getName()));

        JsonArray tasks = actual.getJsonArray(ProcessTypeField.TASKS.getName());
        assertEquals("Size batches doesn't match to given value!", 2, tasks.size());

        JsonObject task = tasks.getJsonObject(0);
        assertEquals("Key tasks.id doesn't match to given value!", 1, task.getInt(TaskTypeField.ID.getName()));
        assertEquals("Key tasks.title doesn't match to given value!", "Task one",
            task.getString(TaskTypeField.TITLE.getName()));

        task = tasks.getJsonObject(1);
        assertEquals("Key tasks.id doesn't match to given value!", 2, task.getInt(TaskTypeField.ID.getName()));
        assertEquals("Key tasks.title doesn't match to given value!", "Task two",
            task.getString(TaskTypeField.TITLE.getName()));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        ProcessType processType = new ProcessType();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Process process = prepareData().get(1);
        HttpEntity document = processType.createDocument(process);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Rendering",
            actual.getString(ProcessTypeField.TITLE.getName()));
        assertEquals("Key outputName doesn't match to given value!", "Render",
            actual.getString(ProcessTypeField.OUTPUT_NAME.getName()));
        assertEquals("Key wikiField doesn't match to given value!", "Field",
            actual.getString(ProcessTypeField.WIKI_FIELD.getName()));
        assertEquals("Key processBaseUri doesn't match to given value!", "",
            actual.getString(ProcessTypeField.PROCESS_BASE_URI.getName()));
        assertEquals("Key template.id doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.TEMPLATE_ID.getName()));
        assertEquals("Key template.title doesn't match to given value!", "",
            actual.getString(ProcessTypeField.TEMPLATE_TITLE.getName()));
        assertEquals("Key creationDate doesn't match to given value!", dateFormat.format(process.getCreationDate()),
            actual.getString(ProcessTypeField.CREATION_DATE.getName()));
        assertEquals("Key sortHelperStatus doesn't match to given value!", "",
            actual.getString(ProcessTypeField.SORT_HELPER_STATUS.getName()));
        assertEquals("Key sortHelperImages doesn't match to given value!", 30,
            actual.getInt(ProcessTypeField.SORT_HELPER_IMAGES.getName()));
        assertEquals("Key sortHelperArticles doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.SORT_HELPER_ARTICLES.getName()));
        assertEquals("Key sortHelperDocstructs doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getName()));
        assertEquals("Key sortHelperMetadata doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.SORT_HELPER_METADATA.getName()));
        assertEquals("Key project.id doesn't match to given value!", 1,
            actual.getInt(ProcessTypeField.PROJECT_ID.getName()));
        assertEquals("Key project.title doesn't match to given value!", "Project",
            actual.getString(ProcessTypeField.PROJECT_TITLE.getName()));
        assertTrue("Key project.active doesn't match to given value!",
            actual.getBoolean(ProcessTypeField.PROJECT_ACTIVE.getName()));
        assertEquals("Key docket doesn't match to given value!", 1, actual.getInt(ProcessTypeField.DOCKET.getName()));
        assertEquals("Key ruleset doesn't match to given value!", 0, actual.getInt(ProcessTypeField.RULESET.getName()));

        JsonArray templates = actual.getJsonArray(ProcessTypeField.TEMPLATES.getName());
        assertEquals("Size templates doesn't match to given value!", 0, templates.size());

        JsonArray tasks = actual.getJsonArray(ProcessTypeField.TASKS.getName());
        assertEquals("Size batches doesn't match to given value!", 0, tasks.size());

        JsonArray workpieces = actual.getJsonArray(ProcessTypeField.WORKPIECES.getName());
        assertEquals("Size workpieces doesn't match to given value!", 0, workpieces.size());

        JsonArray batches = actual.getJsonArray(ProcessTypeField.BATCHES.getName());
        assertEquals("Size batches doesn't match to given value!", 0, batches.size());

        JsonArray properties = actual.getJsonArray(ProcessTypeField.PROPERTIES.getName());
        assertEquals("Size properties doesn't match to given value!", 2, properties.size());

        JsonObject property = properties.getJsonObject(0);
        assertEquals("Key properties.id doesn't match to given value!", 1,
            property.getInt(PropertyTypeField.ID.getName()));

        property = properties.getJsonObject(1);
        assertEquals("Key properties.id doesn't match to given value!", 2,
            property.getInt(PropertyTypeField.ID.getName()));
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        ProcessType processType = new ProcessType();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Process process = prepareData().get(2);
        HttpEntity document = processType.createDocument(process);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Incomplete",
            actual.getString(ProcessTypeField.TITLE.getName()));
        assertEquals("Key outputName doesn't match to given value!", "",
            actual.getString(ProcessTypeField.OUTPUT_NAME.getName()));
        assertEquals("Key wikiField doesn't match to given value!", "",
            actual.getString(ProcessTypeField.WIKI_FIELD.getName()));
        assertEquals("Key processBaseUri doesn't match to given value!", "",
            actual.getString(ProcessTypeField.PROCESS_BASE_URI.getName()));
        assertEquals("Key template.id doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.TEMPLATE_ID.getName()));
        assertEquals("Key template.title doesn't match to given value!", "",
            actual.getString(ProcessTypeField.TEMPLATE_TITLE.getName()));
        assertEquals("Key creationDate doesn't match to given value!", dateFormat.format(process.getCreationDate()),
            actual.getString(ProcessTypeField.CREATION_DATE.getName()));
        assertEquals("Key sortHelperStatus doesn't match to given value!", "",
            actual.getString(ProcessTypeField.SORT_HELPER_STATUS.getName()));
        assertEquals("Key sortHelperImages doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.SORT_HELPER_IMAGES.getName()));
        assertEquals("Key sortHelperArticles doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.SORT_HELPER_ARTICLES.getName()));
        assertEquals("Key sortHelperDocstructs doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getName()));
        assertEquals("Key sortHelperMetadata doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.SORT_HELPER_METADATA.getName()));
        assertEquals("Key project.id doesn't match to given value!", 0,
            actual.getInt(ProcessTypeField.PROJECT_ID.getName()));
        assertEquals("Key project.title doesn't match to given value!", "",
            actual.getString(ProcessTypeField.PROJECT_TITLE.getName()));
        assertFalse("Key project.active doesn't match to given value!",
            actual.getBoolean(ProcessTypeField.PROJECT_ACTIVE.getName()));
        assertEquals("Key docket doesn't match to given value!", 0, actual.getInt(ProcessTypeField.DOCKET.getName()));
        assertEquals("Key ruleset doesn't match to given value!", 0, actual.getInt(ProcessTypeField.RULESET.getName()));

        JsonArray templates = actual.getJsonArray(ProcessTypeField.TEMPLATES.getName());
        assertEquals("Size templates doesn't match to given value!", 0, templates.size());

        JsonArray tasks = actual.getJsonArray(ProcessTypeField.TASKS.getName());
        assertEquals("Size batches doesn't match to given value!", 0, tasks.size());

        JsonArray workpieces = actual.getJsonArray(ProcessTypeField.WORKPIECES.getName());
        assertEquals("Size workpieces doesn't match to given value!", 0, workpieces.size());

        JsonArray batches = actual.getJsonArray(ProcessTypeField.BATCHES.getName());
        assertEquals("Size batches doesn't match to given value!", 0, batches.size());

        JsonArray properties = actual.getJsonArray(ProcessTypeField.PROPERTIES.getName());
        assertEquals("Size properties doesn't match to given value!", 0, properties.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        ProcessType processType = new ProcessType();

        Process process = prepareData().get(0);
        HttpEntity document = processType.createDocument(process);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 22, actual.keySet().size());

        JsonArray batches = actual.getJsonArray(ProcessTypeField.BATCHES.getName());
        JsonObject batch = batches.getJsonObject(0);
        assertEquals("Amount of keys in batches is incorrect!", 2, batch.keySet().size());

        JsonArray tasks = actual.getJsonArray(ProcessTypeField.TASKS.getName());
        JsonObject task = tasks.getJsonObject(0);
        assertEquals("Amount of keys in tasks is incorrect!", 2, task.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        ProcessType processType = new ProcessType();

        List<Process> processes = prepareData();
        Map<Integer, HttpEntity> documents = processType.createDocuments(processes);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}
