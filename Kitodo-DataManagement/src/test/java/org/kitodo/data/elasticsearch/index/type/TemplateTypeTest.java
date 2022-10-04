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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kitodo.data.database.beans.Docket;
import org.kitodo.data.database.beans.Project;
import org.kitodo.data.database.beans.Ruleset;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.elasticsearch.index.type.enums.ProjectTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TaskTypeField;
import org.kitodo.data.elasticsearch.index.type.enums.TemplateTypeField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

        assertEquals("Key title doesn't match to given value!", "Testing",
            TemplateTypeField.TITLE.getStringValue(actual));
        assertEquals("Key creationDate doesn't match to given value!", "2017-01-01 00:00:00",
            TemplateTypeField.CREATION_DATE.getStringValue(actual));
        assertEquals("Key sortHelperStatus doesn't match to given value!", "",
            TemplateTypeField.SORT_HELPER_STATUS.getStringValue(actual));
        assertFalse("Key active doesn't match to given value!", TemplateTypeField.ACTIVE.getBooleanValue(actual));
        assertEquals("Key docket doesn't match to given value!", 0, TemplateTypeField.DOCKET.getIntValue(actual));
        assertEquals("Key ruleset doesn't match to given value!", 1, TemplateTypeField.RULESET_ID.getIntValue(actual));

        List<Map<String, Object>> projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 1, projects.size());

        Map<String, Object> project = projects.get(0);
        assertEquals("Key projects.id doesn't match to given value!", 1,
                ProjectTypeField.ID.getIntValue(project));
        assertEquals("Key projects.title doesn't match to given value!", "Project",
                ProjectTypeField.TITLE.getStringValue(project));

        List<Map<String, Object>> tasks = TemplateTypeField.TASKS.getJsonArray(actual);
        assertEquals("Size tasks doesn't match to given value!", 2, tasks.size());

        Map<String, Object> task = tasks.get(0);
        assertEquals("Key tasks.id doesn't match to given value!", 1, TaskTypeField.ID.getIntValue(task));
        assertEquals("Key tasks.title doesn't match to given value!", "Task one",
            TaskTypeField.TITLE.getStringValue(task));

        task = tasks.get(1);
        assertEquals("Key tasks.id doesn't match to given value!", 2, TaskTypeField.ID.getIntValue(task));
        assertEquals("Key tasks.title doesn't match to given value!", "Task two",
            TaskTypeField.TITLE.getStringValue(task));
    }

    @Test
    public void shouldCreateSecondDocument() throws Exception {
        TemplateType templateType = new TemplateType();

        Template template = prepareData().get(1);
        Map<String, Object> actual = templateType.createDocument(template);

        assertEquals("Key title doesn't match to given value!", "Rendering",
            TemplateTypeField.TITLE.getStringValue(actual));
        assertEquals("Key creationDate doesn't match to given value!", formatDate(template.getCreationDate()),
            TemplateTypeField.CREATION_DATE.getStringValue(actual));
        assertEquals("Key sortHelperStatus doesn't match to given value!", "",
            TemplateTypeField.SORT_HELPER_STATUS.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!", TemplateTypeField.ACTIVE.getBooleanValue(actual));
        assertEquals("Key docket doesn't match to given value!", 1, TemplateTypeField.DOCKET.getIntValue(actual));
        assertEquals("Key ruleset doesn't match to given value!", 0, TemplateTypeField.RULESET_ID.getIntValue(actual));

        List<Map<String, Object>> projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 1, projects.size());

        Map<String, Object> project = projects.get(0);
        assertEquals("Key projects.id doesn't match to given value!", 1,
                ProjectTypeField.ID.getIntValue(project));
        assertEquals("Key projects.title doesn't match to given value!", "Project",
                ProjectTypeField.TITLE.getStringValue(project));

        List<Map<String, Object>> tasks = TemplateTypeField.TASKS.getJsonArray(actual);
        assertEquals("Size tasks doesn't match to given value!", 0, tasks.size());
    }

    @Test
    public void shouldCreateThirdDocument() throws Exception {
        TemplateType templateType = new TemplateType();

        Template template = prepareData().get(2);
        Map<String, Object> actual = templateType.createDocument(template);

        assertEquals("Key title doesn't match to given value!", "Incomplete",
            TemplateTypeField.TITLE.getStringValue(actual));
        assertEquals("Key creationDate doesn't match to given value!", formatDate(template.getCreationDate()),
            TemplateTypeField.CREATION_DATE.getStringValue(actual));
        assertEquals("Key sortHelperStatus doesn't match to given value!", "",
            TemplateTypeField.SORT_HELPER_STATUS.getStringValue(actual));
        assertTrue("Key active doesn't match to given value!", TemplateTypeField.ACTIVE.getBooleanValue(actual));
        assertEquals("Key docket doesn't match to given value!", 0, TemplateTypeField.DOCKET.getIntValue(actual));
        assertEquals("Key ruleset doesn't match to given value!", 0, TemplateTypeField.RULESET_ID.getIntValue(actual));

        List<Map<String, Object>> projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        assertEquals("Size projects doesn't match to given value!", 0, projects.size());

        List<Map<String, Object>> tasks = TemplateTypeField.TASKS.getJsonArray(actual);
        assertEquals("Size tasks doesn't match to given value!", 0, tasks.size());
    }

    @Test
    public void shouldCreateDocumentWithCorrectAmountOfKeys() throws Exception {
        TemplateType templateType = new TemplateType();

        Template template = prepareData().get(0);
        Map<String, Object> actual = templateType.createDocument(template);

        assertEquals("Amount of keys is incorrect!", 12, actual.keySet().size());

        List<Map<String, Object>> projects = TemplateTypeField.PROJECTS.getJsonArray(actual);
        Map<String, Object> project = projects.get(0);
        assertEquals("Amount of keys in projects is incorrect!", 3, project.keySet().size());

        List<Map<String, Object>> tasks = TemplateTypeField.TASKS.getJsonArray(actual);
        Map<String, Object> task = tasks.get(0);
        assertEquals("Amount of keys in tasks is incorrect!", 2, task.keySet().size());
    }

    @Test
    public void shouldCreateDocuments() {
        TemplateType templateType = new TemplateType();

        List<Template> templates = prepareData();
        Map<Integer, Map<String, Object>> documents = templateType.createDocuments(templates);
        assertEquals("HashMap of documents doesn't contain given amount of elements!", 3, documents.size());
    }

    private String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }
}
