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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.kitodo.constants.StringConstants.DEFAULT_DATE_FORMAT;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Client;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.elasticsearch.index.type.enums.BatchTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.ProcessTypeField;
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

        Client client = new Client();
        client.setId(1);

        Project project = new Project();
        project.setTitle("Project");
        project.setClient(client);
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
        LocalDate localDate = LocalDate.of(2017, 1, 1);
        firstProcess.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
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
        Map<String, Object> actual = processType.createDocument(process);

        assertEquals("Testing", ProcessTypeField.TITLE.getStringValue(actual), "Key title doesn't match to given value!");
        assertEquals("Wiki", ProcessTypeField.WIKI_FIELD.getStringValue(actual), "Key wikiField doesn't match to given value!");
        assertEquals("", ProcessTypeField.PROCESS_BASE_URI.getStringValue(actual), "Key processBaseUri doesn't match to given value!");
        assertEquals(0, ProcessTypeField.TEMPLATE_ID.getIntValue(actual), "Key template.id doesn't match to given value!");
        assertEquals("", ProcessTypeField.TEMPLATE_TITLE.getStringValue(actual), "Key template.title doesn't match to given value!");
        assertEquals("2017-01-01 00:00:00", ProcessTypeField.CREATION_DATE.getStringValue(actual), "Key creationDate doesn't match to given value!");
        assertEquals("", ProcessTypeField.SORT_HELPER_STATUS.getStringValue(actual), "Key sortHelperStatus doesn't match to given value!");
        assertEquals(20, ProcessTypeField.SORT_HELPER_IMAGES.getIntValue(actual), "Key sortHelperImages doesn't match to given value!");
        assertEquals(0, ProcessTypeField.SORT_HELPER_ARTICLES.getIntValue(actual), "Key sortHelperArticles doesn't match to given value!");
        assertEquals(0, ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getIntValue(actual), "Key sortHelperDocstructs doesn't match to given value!");
        assertEquals(0, ProcessTypeField.SORT_HELPER_METADATA.getIntValue(actual), "Key sortHelperMetadata doesn't match to given value!");
        assertEquals(1, ProcessTypeField.PROJECT_ID.getIntValue(actual), "Key project.id doesn't match to given value!");
        assertEquals("Project", ProcessTypeField.PROJECT_TITLE.getStringValue(actual), "Key project.title doesn't match to given value!");
        assertTrue(ProcessTypeField.PROJECT_ACTIVE.getBooleanValue(actual), "Key project.active doesn't match to given value!");
        assertEquals(0, ProcessTypeField.DOCKET.getIntValue(actual), "Key docket doesn't match to given value!");
        assertEquals(1, ProcessTypeField.RULESET.getIntValue(actual), "Key ruleset doesn't match to given value!");

        List<Map<String, Object>> properties = ProcessTypeField.PROPERTIES.getJsonArray(actual);
        assertEquals(0, properties.size(), "Size properties doesn't match to given value!");

        List<Map<String, Object>> batches = ProcessTypeField.BATCHES.getJsonArray(actual);
        assertEquals(1, batches.size(), "Size batches doesn't match to given value!");

        Map<String, Object> batch = batches.get(0);
        assertEquals(1, BatchTypeField.ID.getIntValue(batch), "Key batches.id doesn't match to given value!");
        assertEquals("First", BatchTypeField.TITLE.getStringValue(batch), "Key batches.title doesn't match to given value!");

        List<Map<String, Object>> tasks = ProcessTypeField.TASKS.getJsonArray(actual);
        assertEquals(2, tasks.size(), "Size batches doesn't match to given value!");

        Map<String, Object> task = tasks.get(0);
        assertEquals(1, TaskTypeField.ID.getIntValue(task), "Key tasks.id doesn't match to given value!");
        assertEquals("Task one", TaskTypeField.TITLE.getStringValue(task), "Key tasks.title doesn't match to given value!");

        task = tasks.get(1);
        assertEquals(2, TaskTypeField.ID.getIntValue(task), "Key tasks.id doesn't match to given value!");
        assertEquals("Task two", TaskTypeField.TITLE.getStringValue(task), "Key tasks.title doesn't match to given value!");
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        ProcessType processType = new ProcessType();

        Process process = prepareData().get(1);
        Map<String, Object> actual = processType.createDocument(process);

        assertEquals("Rendering", ProcessTypeField.TITLE.getStringValue(actual), "Key title doesn't match to given value!");
        assertEquals("Field", ProcessTypeField.WIKI_FIELD.getStringValue(actual), "Key wikiField doesn't match to given value!");
        assertEquals("", ProcessTypeField.PROCESS_BASE_URI.getStringValue(actual), "Key processBaseUri doesn't match to given value!");
        assertEquals(0, ProcessTypeField.TEMPLATE_ID.getIntValue(actual), "Key template.id doesn't match to given value!");
        assertEquals("", ProcessTypeField.TEMPLATE_TITLE.getStringValue(actual), "Key template.title doesn't match to given value!");
        assertEquals(formatDate(process.getCreationDate()), ProcessTypeField.CREATION_DATE.getStringValue(actual), "Key creationDate doesn't match to given value!");
        assertEquals("", ProcessTypeField.SORT_HELPER_STATUS.getStringValue(actual), "Key sortHelperStatus doesn't match to given value!");
        assertEquals(30, ProcessTypeField.SORT_HELPER_IMAGES.getIntValue(actual), "Key sortHelperImages doesn't match to given value!");
        assertEquals(0, ProcessTypeField.SORT_HELPER_ARTICLES.getIntValue(actual), "Key sortHelperArticles doesn't match to given value!");
        assertEquals(0, ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getIntValue(actual), "Key sortHelperDocstructs doesn't match to given value!");
        assertEquals(0, ProcessTypeField.SORT_HELPER_METADATA.getIntValue(actual), "Key sortHelperMetadata doesn't match to given value!");
        assertEquals(1, ProcessTypeField.PROJECT_ID.getIntValue(actual), "Key project.id doesn't match to given value!");
        assertEquals("Project", ProcessTypeField.PROJECT_TITLE.getStringValue(actual), "Key project.title doesn't match to given value!");
        assertTrue(ProcessTypeField.PROJECT_ACTIVE.getBooleanValue(actual), "Key project.active doesn't match to given value!");
        assertEquals(1, ProcessTypeField.DOCKET.getIntValue(actual), "Key docket doesn't match to given value!");
        assertEquals(0, ProcessTypeField.RULESET.getIntValue(actual), "Key ruleset doesn't match to given value!");

        List<Map<String, Object>> tasks = ProcessTypeField.TASKS.getJsonArray(actual);
        assertEquals(0, tasks.size(), "Size batches doesn't match to given value!");

        List<Map<String, Object>> batches = ProcessTypeField.BATCHES.getJsonArray(actual);
        assertEquals(0, batches.size(), "Size batches doesn't match to given value!");

        List<Map<String, Object>> properties = ProcessTypeField.PROPERTIES.getJsonArray(actual);
        assertEquals(2, properties.size(), "Size properties doesn't match to given value!");

    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        ProcessType processType = new ProcessType();

        Process process = prepareData().get(2);
        Map<String, Object> actual = processType.createDocument(process);

        assertEquals("Incomplete", ProcessTypeField.TITLE.getStringValue(actual), "Key title doesn't match to given value!");
        assertEquals("", ProcessTypeField.WIKI_FIELD.getStringValue(actual), "Key wikiField doesn't match to given value!");
        assertEquals("", ProcessTypeField.PROCESS_BASE_URI.getStringValue(actual), "Key processBaseUri doesn't match to given value!");
        assertEquals(0, ProcessTypeField.TEMPLATE_ID.getIntValue(actual), "Key template.id doesn't match to given value!");
        assertEquals("", ProcessTypeField.TEMPLATE_TITLE.getStringValue(actual), "Key template.title doesn't match to given value!");
        assertEquals(formatDate(process.getCreationDate()), ProcessTypeField.CREATION_DATE.getStringValue(actual), "Key creationDate doesn't match to given value!");
        assertEquals("", ProcessTypeField.SORT_HELPER_STATUS.getStringValue(actual), "Key sortHelperStatus doesn't match to given value!");
        assertEquals(0, ProcessTypeField.SORT_HELPER_IMAGES.getIntValue(actual), "Key sortHelperImages doesn't match to given value!");
        assertEquals(0, ProcessTypeField.SORT_HELPER_ARTICLES.getIntValue(actual), "Key sortHelperArticles doesn't match to given value!");
        assertEquals(0, ProcessTypeField.SORT_HELPER_DOCSTRUCTS.getIntValue(actual), "Key sortHelperDocstructs doesn't match to given value!");
        assertEquals(0, ProcessTypeField.SORT_HELPER_METADATA.getIntValue(actual), "Key sortHelperMetadata doesn't match to given value!");
        assertEquals(0, ProcessTypeField.PROJECT_ID.getIntValue(actual), "Key project.id doesn't match to given value!");
        assertEquals("", ProcessTypeField.PROJECT_TITLE.getStringValue(actual), "Key project.title doesn't match to given value!");
        assertFalse(ProcessTypeField.PROJECT_ACTIVE.getBooleanValue(actual), "Key project.active doesn't match to given value!");
        assertEquals(0, ProcessTypeField.DOCKET.getIntValue(actual), "Key docket doesn't match to given value!");
        assertEquals(0, ProcessTypeField.RULESET.getIntValue(actual), "Key ruleset doesn't match to given value!");

        List<Map<String, Object>> tasks = ProcessTypeField.TASKS.getJsonArray(actual);
        assertEquals(0, tasks.size(), "Size batches doesn't match to given value!");

        List<Map<String, Object>> batches = ProcessTypeField.BATCHES.getJsonArray(actual);
        assertEquals(0, batches.size(), "Size batches doesn't match to given value!");

        List<Map<String, Object>> properties = ProcessTypeField.PROPERTIES.getJsonArray(actual);
        assertEquals(0, properties.size(), "Size properties doesn't match to given value!");
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        ProcessType processType = new ProcessType();

        Process process = prepareData().get(0);
        Map<String, Object> actual = processType.createDocument(process);

        assertEquals(40, actual.keySet().size(), "Amount of keys is incorrect!");

        List<Map<String, Object>> batches = ProcessTypeField.BATCHES.getJsonArray(actual);
        Map<String, Object> batch = batches.get(0);
        assertEquals(2, batch.keySet().size(), "Amount of keys in batches is incorrect!");

        List<Map<String, Object>> tasks = ProcessTypeField.TASKS.getJsonArray(actual);
        Map<String, Object> task = tasks.get(0);
        assertEquals(2, task.keySet().size(), "Amount of keys in tasks is incorrect!");
    }

    @Test
    public void shouldCreateDocuments() {
        ProcessType processType = new ProcessType();

        List<Process> processes = prepareData();
        Map<Integer, Map<String, Object>> documents = processType.createDocuments(processes);
        assertEquals(3, documents.size(), "HashMap of documents doesn't contain given amount of elements!");
    }

    private String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        return dateFormat.format(date);
    }
}
