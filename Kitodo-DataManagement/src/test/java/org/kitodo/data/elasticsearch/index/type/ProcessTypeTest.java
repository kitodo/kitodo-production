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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.joda.time.LocalDate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Property;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;

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
    public void shouldCreateDocument() throws Exception {
        ProcessType processType = new ProcessType();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        JSONParser parser = new JSONParser();

        Process process = prepareData().get(0);
        HttpEntity document = processType.createDocument(process);
        JSONObject actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        JSONObject expected = (JSONObject) parser.parse("{\"title\":\"Testing\",\"outputName\":\"Test\","
                + "\"wikiField\":\"Wiki\",\"docket\":null,\"ruleset\":1,\"project.id\":1,\"sortHelperStatus\":null,"
                + "\"creationDate\":\"2017-01-01\",\"processBaseUri\":null,\"template\":false,\"sortHelperImages\":20,"
                + "\"batches\":[{\"id\":1,\"title\":\"First\"}]\"workpieces\":[],\"tasks\":[{\"id\":1,\"title\":"
                + "\"Task one\"},{\"id\":2,\"title\":\"Task two\"}],\"project.title\":\"Project\",\"properties\":[]}");
        assertEquals("Process JSONObject doesn't match to given JSONObject!", expected, actual);

        process = prepareData().get(1);
        document = processType.createDocument(process);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser.parse("{\"title\":\"Rendering\",\"outputName\":\"Render\",\"batches\":[],"
                + "\"wikiField\":\"Field\",\"docket\":1,\"ruleset\":null,\"project.id\":1,\"template\":false,"
                + "\"project.title\":\"Project\",\"sortHelperStatus\":null,\"processBaseUri\":null,\"creationDate\":\""
                + dateFormat.format(process.getCreationDate()) + "\",\"sortHelperImages\":30,\"workpieces\":[],"
                + "\"tasks\":[],\"properties\":[{\"id\":1},{\"id\":2}]}");
        assertEquals("Process JSONObject doesn't match to given JSONObject!", expected, actual);

        process = prepareData().get(2);
        document = processType.createDocument(process);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        expected = (JSONObject) parser.parse("{\"title\":\"Incomplete\",\"outputName\":null,\"wikiField\":\"\","
                + "\"docket\":null,\"ruleset\":null,\"project.id\":null,\"project.title\":null,\"template\":false,"
                + "\"creationDate\":\"" + dateFormat.format(process.getCreationDate())
                + "\",\"tasks\":[],\"properties\":[],\"batches\":[],"
                + "\"workpieces\":[],\"sortHelperImages\":0,\"sortHelperStatus\":null,\"processBaseUri\":null}");
        assertEquals("Process JSONObject doesn't match to given JSONObject!", expected, actual);
    }

    @Test
    public void shouldCreateDocuments() {
        ProcessType processType = new ProcessType();

        List<Process> processes = prepareData();
        HashMap<Integer, HttpEntity> documents = processType.createDocuments(processes);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}
