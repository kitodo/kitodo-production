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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TemplateTypeTest {

    private static List<Template> prepareData() {

        List<Template> templates = new ArrayList<>();

        Project project = new Project();
        project.setTitle("Project");
        project.setId(1);

        Ruleset ruleset = new Ruleset();
        ruleset.setId(1);

        Docket docket = new Docket();
        docket.setId(1);

        Workflow workflow = new Workflow();
        workflow.setTitle("Workflow");
        workflow.setFileName("Workflow");

        Template firstTemplate = new Template();
        firstTemplate.setId(1);
        firstTemplate.setTitle("Testing");
        LocalDate localDate = new LocalDate(2017, 1, 1);
        firstTemplate.setCreationDate(localDate.toDate());
        firstTemplate.setActive(false);
        firstTemplate.setWikiField("Wiki");
        firstTemplate.getProjects().add(project);
        firstTemplate.setRuleset(ruleset);
        firstTemplate.setWorkflow(workflow);
        templates.add(firstTemplate);

        Template secondTemplate = new Template();
        secondTemplate.setId(2);
        secondTemplate.setTitle("Rendering");
        secondTemplate.setWikiField("Field");
        secondTemplate.setActive(true);
        secondTemplate.getProjects().add(project);
        secondTemplate.setDocket(docket);
        secondTemplate.setWorkflow(workflow);
        templates.add(secondTemplate);

        Template thirdTemplate = new Template();
        thirdTemplate.setId(3);
        thirdTemplate.setTitle("Incomplete");
        templates.add(thirdTemplate);

        return templates;
    }

    @Test
    public void shouldCreateFirstDocument() throws Exception {
        TemplateType templateType = new TemplateType();

        Template template = prepareData().get(0);
        HttpEntity document = templateType.createDocument(template);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Testing",
            TemplateTypeField.TITLE.getStringValue(actual));
        assertEquals("Key wikiField doesn't match to given value!", "Wiki",
            TemplateTypeField.WIKI_FIELD.getStringValue(actual));
        assertEquals("Key creationDate doesn't match to given value!", "2017-01-01 00:00:00",
            TemplateTypeField.CREATION_DATE.getStringValue(actual));
        assertEquals("Key sortHelperStatus doesn't match to given value!", "",
            TemplateTypeField.SORT_HELPER_STATUS.getStringValue(actual));
        assertFalse("Key active doesn't match to given value!", TemplateTypeField.ACTIVE.getBooleanValue(actual));
        assertEquals("Key docket doesn't match to given value!", 0, TemplateTypeField.DOCKET.getIntValue(actual));
        assertEquals("Key ruleset doesn't match to given value!", 1, TemplateTypeField.RULESET.getIntValue(actual));

        JsonArray projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 1, projects.size());

        JsonObject project = projects.getJsonObject(0);
        assertEquals("Key projects.id doesn't match to given value!", 1,
                ProjectTypeField.ID.getIntValue(project));
        assertEquals("Key projects.title doesn't match to given value!", "Project",
                ProjectTypeField.TITLE.getStringValue(project));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        TemplateType templateType = new TemplateType();

        Template template = prepareData().get(1);
        HttpEntity document = templateType.createDocument(template);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Rendering",
            TemplateTypeField.TITLE.getStringValue(actual));
        assertEquals("Key wikiField doesn't match to given value!", "Field",
            TemplateTypeField.WIKI_FIELD.getStringValue(actual));
        assertEquals("Key creationDate doesn't match to given value!", formatDate(template.getCreationDate()),
            TemplateTypeField.CREATION_DATE.getStringValue(actual));
        assertEquals("Key sortHelperStatus doesn't match to given value!", "",
            TemplateTypeField.SORT_HELPER_STATUS.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!", TemplateTypeField.ACTIVE.getBooleanValue(actual));
        assertEquals("Key docket doesn't match to given value!", 1, TemplateTypeField.DOCKET.getIntValue(actual));
        assertEquals("Key ruleset doesn't match to given value!", 0, TemplateTypeField.RULESET.getIntValue(actual));

        JsonArray projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 1, projects.size());

        JsonObject project = projects.getJsonObject(0);
        assertEquals("Key projects.id doesn't match to given value!", 1,
                ProjectTypeField.ID.getIntValue(project));
        assertEquals("Key projects.title doesn't match to given value!", "Project",
                ProjectTypeField.TITLE.getStringValue(project));
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        TemplateType templateType = new TemplateType();

        Template template = prepareData().get(2);
        HttpEntity document = templateType.createDocument(template);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();

        assertEquals("Key title doesn't match to given value!", "Incomplete",
            TemplateTypeField.TITLE.getStringValue(actual));
        assertEquals("Key wikiField doesn't match to given value!", "",
            TemplateTypeField.WIKI_FIELD.getStringValue(actual));
        assertEquals("Key creationDate doesn't match to given value!", formatDate(template.getCreationDate()),
            TemplateTypeField.CREATION_DATE.getStringValue(actual));
        assertEquals("Key sortHelperStatus doesn't match to given value!", "",
            TemplateTypeField.SORT_HELPER_STATUS.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!", TemplateTypeField.ACTIVE.getBooleanValue(actual));
        assertEquals("Key docket doesn't match to given value!", 0, TemplateTypeField.DOCKET.getIntValue(actual));
        assertEquals("Key ruleset doesn't match to given value!", 0, TemplateTypeField.RULESET.getIntValue(actual));

        JsonArray projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 0, projects.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        TemplateType templateType = new TemplateType();

        Template template = prepareData().get(0);
        HttpEntity document = templateType.createDocument(template);

        JsonObject actual = Json.createReader(new StringReader(EntityUtils.toString(document))).readObject();
        assertEquals("Amount of keys is incorrect!", 10, actual.keySet().size());

        JsonArray projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        JsonObject project = projects.getJsonObject(0);
        assertEquals("Amount of keys in projects is incorrect!", 3, project.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        TemplateType templateType = new TemplateType();

        List<Template> templates = prepareData();
        Map<Integer, HttpEntity> documents = templateType.createDocuments(templates);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }

    private String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }
}
