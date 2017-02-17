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

package org.kitodo.data.index.elasticsearch.type;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import org.joda.time.LocalDate;
import org.junit.Test;

import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.ProcessProperty;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;

import static org.junit.Assert.*;

/**
 * Test class for ProcessType.
 */
public class ProcessTypeTest {

    private static List<Process> prepareData() {

        List<Process> processes = new ArrayList<>();
        List<ProcessProperty> processProperties = new ArrayList<>();

        Project project = new Project();
        project.setId(1);

        Ruleset ruleset = new Ruleset();
        ruleset.setId(1);

        Docket docket = new Docket();
        docket.setId(1);

        ProcessProperty firstProcessProperty = new ProcessProperty();
        firstProcessProperty.setTitle("first");
        firstProcessProperty.setValue("1");
        processProperties.add(firstProcessProperty);

        ProcessProperty secondProcessProperty = new ProcessProperty();
        secondProcessProperty.setTitle("second");
        secondProcessProperty.setValue("2");
        processProperties.add(secondProcessProperty);

        Process firstProcess = new Process();
        firstProcess.setId(1);
        firstProcess.setTitle("Testing");
        firstProcess.setOutputName("Test");
        LocalDate localDate = new LocalDate(2017,1,1);
        firstProcess.setCreationDate(localDate.toDate());
        firstProcess.setWikiField("Wiki");
        firstProcess.setProject(project);
        firstProcess.setRuleset(ruleset);
        processes.add(firstProcess);

        Process secondProcess = new Process();
        secondProcess.setId(2);
        secondProcess.setTitle("Rendering");
        secondProcess.setOutputName("Render");
        secondProcess.setWikiField("Field");
        secondProcess.setProject(project);
        secondProcess.setDocket(docket);
        secondProcess.setProperties(processProperties);
        processes.add(secondProcess);

        Process thirdProcess = new Process();
        thirdProcess.setId(3);
        thirdProcess.setTitle("Incomplete");
        processes.add(thirdProcess);

        return processes;
    }

    @Test
    //problem with ordering of objects
    public void shouldCreateDocument() throws Exception {
        ProcessType processType = new ProcessType();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Process process = prepareData().get(0);
        HttpEntity document = processType.createDocument(process);
        String actual = EntityUtils.toString(document);
        String excepted = "{\"outputName\":\"Test\",\"wikiField\":\"Wiki\",\"ldapGroup\":\"null\","
                + "\"name\":\"Testing\",\"ruleset\":\"1\",\"project\":\"1\",\"creationDate\":\"2017-01-01\","
                + "\"properties\":[]}";
        assertEquals("Process JSON string doesn't match to given plain text!", excepted, actual);

        process = prepareData().get(1);
        document = processType.createDocument(process);
        actual = EntityUtils.toString(document);
        excepted = "{\"outputName\":\"Render\",\"wikiField\":\"Field\",\"ldapGroup\":\"1\",\"name\":\"Rendering\","
                + "\"ruleset\":\"null\",\"project\":\"1\",\"creationDate\":\"" + dateFormat.format(process.getCreationDate())
                + "\",\"properties\":[{\"title\":\"first\",\"value\":\"1\"},{\"title\":\"second\",\"value\":\"2\"}]}";
        assertEquals("Process JSON string doesn't match to given plain text!", excepted, actual);

        process = prepareData().get(2);
        document = processType.createDocument(process);
        actual = EntityUtils.toString(document);
        excepted = "{\"outputName\":null,\"wikiField\":\"\",\"ldapGroup\":\"null\",\"name\":\"Incomplete\","
                + "\"ruleset\":\"null\",\"project\":\"null\",\"creationDate\":\"" + dateFormat.format(process.getCreationDate())
                + "\",\"properties\":[]}";
        assertEquals("Process JSON string doesn't match to given plain text!", excepted, actual);
    }

    @Test
    public void shouldCreateDocuments() throws Exception {
        ProcessType processType = new ProcessType();

        List<Process> processes = prepareData();
        HashMap<Integer, HttpEntity> documents = processType.createDocuments(processes);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }
}
