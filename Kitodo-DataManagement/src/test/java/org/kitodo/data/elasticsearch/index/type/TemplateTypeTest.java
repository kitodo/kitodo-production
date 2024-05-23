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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;

public class TemplateTypeTest {

    private static List<Template> prepareData() {

        List<Template> templates = new ArrayList<>();
        List<Task> tasks = new ArrayList<>();

        Project project = new Project();
        project.setTitle("Project");
        project.setId(1);

        Ruleset ruleset = new Ruleset();
        ruleset.setId(1);

        Docket docket = new Docket();
        docket.setId(1);

        Task firstTask = new Task();
        firstTask.setId(1);
        firstTask.setTitle("Task one");
        tasks.add(firstTask);

        Task secondTask = new Task();
        secondTask.setId(2);
        secondTask.setTitle("Task two");
        tasks.add(secondTask);

        Template firstTemplate = new Template();
        firstTemplate.setId(1);
        firstTemplate.setTitle("Testing");
        LocalDate localDate = LocalDate.of(2017, 1, 1);
        firstTemplate.setCreationDate(Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()));
        firstTemplate.setActive(false);
        firstTemplate.setTasks(tasks);
        firstTemplate.getProjects().add(project);
        firstTemplate.setRuleset(ruleset);
        templates.add(firstTemplate);

        Template secondTemplate = new Template();
        secondTemplate.setId(2);
        secondTemplate.setTitle("Rendering");
        secondTemplate.setActive(true);
        secondTemplate.getProjects().add(project);
        secondTemplate.setDocket(docket);
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
        Map<String, Object> actual = templateType.createDocument(template);

        assertEquals("Testing", TemplateTypeField.TITLE.getStringValue(actual), "Key title doesn't match to given value!");
        assertEquals("2017-01-01 00:00:00", TemplateTypeField.CREATION_DATE.getStringValue(actual), "Key creationDate doesn't match to given value!");
        assertEquals("", TemplateTypeField.SORT_HELPER_STATUS.getStringValue(actual), "Key sortHelperStatus doesn't match to given value!");
        assertFalse(TemplateTypeField.ACTIVE.getBooleanValue(actual), "Key active doesn't match to given value!");
        assertEquals(0, TemplateTypeField.DOCKET.getIntValue(actual), "Key docket doesn't match to given value!");
        assertEquals(1, TemplateTypeField.RULESET_ID.getIntValue(actual), "Key ruleset doesn't match to given value!");

        List<Map<String, Object>> projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        assertEquals(1, projects.size(), "Size projects doesn't match to given value!");

        Map<String, Object> project = projects.get(0);
        assertEquals(1, ProjectTypeField.ID.getIntValue(project), "Key projects.id doesn't match to given value!");
        assertEquals("Project", ProjectTypeField.TITLE.getStringValue(project), "Key projects.title doesn't match to given value!");

        List<Map<String, Object>> tasks = TemplateTypeField.TASKS.getJsonArray(actual);
        assertEquals(2, tasks.size(), "Size tasks doesn't match to given value!");

        Map<String, Object> task = tasks.get(0);
        assertEquals(1, TaskTypeField.ID.getIntValue(task), "Key tasks.id doesn't match to given value!");
        assertEquals("Task one", TaskTypeField.TITLE.getStringValue(task), "Key tasks.title doesn't match to given value!");

        task = tasks.get(1);
        assertEquals(2, TaskTypeField.ID.getIntValue(task), "Key tasks.id doesn't match to given value!");
        assertEquals("Task two", TaskTypeField.TITLE.getStringValue(task), "Key tasks.title doesn't match to given value!");
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        TemplateType templateType = new TemplateType();

        Template template = prepareData().get(1);
        Map<String, Object> actual = templateType.createDocument(template);

        assertEquals("Rendering", TemplateTypeField.TITLE.getStringValue(actual), "Key title doesn't match to given value!");
        assertEquals(formatDate(template.getCreationDate()), TemplateTypeField.CREATION_DATE.getStringValue(actual), "Key creationDate doesn't match to given value!");
        assertEquals("", TemplateTypeField.SORT_HELPER_STATUS.getStringValue(actual), "Key sortHelperStatus doesn't match to given value!");
        assertTrue(TemplateTypeField.ACTIVE.getBooleanValue(actual), "Key active doesn't match to given value!");
        assertEquals(1, TemplateTypeField.DOCKET.getIntValue(actual), "Key docket doesn't match to given value!");
        assertEquals(0, TemplateTypeField.RULESET_ID.getIntValue(actual), "Key ruleset doesn't match to given value!");

        List<Map<String, Object>> projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        assertEquals(1, projects.size(), "Size projects doesn't match to given value!");

        Map<String, Object> project = projects.get(0);
        assertEquals(1, ProjectTypeField.ID.getIntValue(project), "Key projects.id doesn't match to given value!");
        assertEquals("Project", ProjectTypeField.TITLE.getStringValue(project), "Key projects.title doesn't match to given value!");

        List<Map<String, Object>> tasks = TemplateTypeField.TASKS.getJsonArray(actual);
        assertEquals(0, tasks.size(), "Size tasks doesn't match to given value!");
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        TemplateType templateType = new TemplateType();

        Template template = prepareData().get(2);
        Map<String, Object> actual = templateType.createDocument(template);

        assertEquals("Incomplete", TemplateTypeField.TITLE.getStringValue(actual), "Key title doesn't match to given value!");
        assertEquals(formatDate(template.getCreationDate()), TemplateTypeField.CREATION_DATE.getStringValue(actual), "Key creationDate doesn't match to given value!");
        assertEquals("", TemplateTypeField.SORT_HELPER_STATUS.getStringValue(actual), "Key sortHelperStatus doesn't match to given value!");
        assertTrue(TemplateTypeField.ACTIVE.getBooleanValue(actual), "Key active doesn't match to given value!");
        assertEquals(0, TemplateTypeField.DOCKET.getIntValue(actual), "Key docket doesn't match to given value!");
        assertEquals(0, TemplateTypeField.RULESET_ID.getIntValue(actual), "Key ruleset doesn't match to given value!");

        List<Map<String, Object>> projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        assertEquals(0, projects.size(), "Size projects doesn't match to given value!");

        List<Map<String, Object>> tasks = TemplateTypeField.TASKS.getJsonArray(actual);
        assertEquals(0, tasks.size(), "Size tasks doesn't match to given value!");
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        TemplateType templateType = new TemplateType();

        Template template = prepareData().get(0);
        Map<String, Object> actual = templateType.createDocument(template);

        assertEquals(12, actual.keySet().size(), "Amount of keys is incorrect!");

        List<Map<String, Object>> projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        Map<String, Object> project = projects.get(0);
        assertEquals(3, project.keySet().size(), "Amount of keys in projects is incorrect!");

        List<Map<String, Object>> tasks = TemplateTypeField.TASKS.getJsonArray(actual);
        Map<String, Object> task = tasks.get(0);
        assertEquals(2, task.keySet().size(), "Amount of keys in tasks is incorrect!");
    }

    @Test
    public void shouldCreateDocuments() {
        TemplateType templateType = new TemplateType();

        List<Template> templates = prepareData();
        Map<Integer, Map<String, Object>> documents = templateType.createDocuments(templates);
        assertEquals(3, documents.size(), "HashMap of documents doesn't contain given amount of elements!");
    }

    private String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }
}
