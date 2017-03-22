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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.junit.Test;

import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.TemplateProperty;

import static org.junit.Assert.*;

/**
 * Test class for TemplateType.
 */
public class TemplateTypeTest {

    private static List<Template> prepareData() {

        List<Template> templates = new ArrayList<>();
        List<TemplateProperty> templateProperties = new ArrayList<>();

        Process firstProcess = new Process();
        firstProcess.setId(1);

        Process secondProcess = new Process();
        secondProcess.setId(2);

        Process thirdProcess = new Process();
        thirdProcess.setId(3);

        TemplateProperty firstTemplateProperty = new TemplateProperty();
        firstTemplateProperty.setTitle("first");
        firstTemplateProperty.setValue("1");
        templateProperties.add(firstTemplateProperty);

        TemplateProperty secondTemplateProperty = new TemplateProperty();
        secondTemplateProperty.setTitle("second");
        secondTemplateProperty.setValue("2");
        templateProperties.add(secondTemplateProperty);

        Template firstTemplate = new Template();
        firstTemplate.setId(1);
        firstTemplate.setProcess(firstProcess);
        firstTemplate.setProperties(templateProperties);
        templates.add(firstTemplate);

        Template secondTemplate = new Template();
        secondTemplate.setId(2);
        secondTemplate.setProcess(secondProcess);
        templates.add(secondTemplate);

        return templates;
    }

    @Test
    //problem with ordering of objects
    public void shouldCreateDocument() throws Exception {
        TemplateType templateType = new TemplateType();
        JSONParser parser = new JSONParser();

        Template template = prepareData().get(0);
        HttpEntity document = templateType.createDocument(template);
        JSONObject actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        JSONObject excepted = (JSONObject) parser.parse("{\"process\":\"1\",\"properties\":[{\"title\":\"first\","
                + "\"value\":\"1\"},{\"title\":\"second\",\"value\":\"2\"}]}");
        assertEquals("Template JSONObject doesn't match to given JSONObject!", excepted, actual);

        template = prepareData().get(1);
        document = templateType.createDocument(template);
        actual = (JSONObject) parser.parse(EntityUtils.toString(document));
        excepted = (JSONObject) parser.parse("{\"process\":\"2\",\"properties\":[]}");
        assertEquals("Template JSONObject doesn't match to given JSONObject!", excepted, actual);
    }

    @Test
    public void shouldCreateDocuments() throws Exception {
        TemplateType processType = new TemplateType();

        List<Template> processes = prepareData();
        HashMap<Integer, HttpEntity> documents = processType.createDocuments(processes);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 2, documents.size());
    }
}
