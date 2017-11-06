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

package org.kitodo.services.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.json.simple.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.dto.TemplateDTO;
import org.kitodo.services.ServiceManager;

/**
 * Tests for TemplateService class.
 */
public class TemplateServiceIT {

    private static final TemplateService templateService = new ServiceManager().getTemplateService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(500);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldCountAllTemplates() throws Exception {
        Long amount = templateService.count();
        assertEquals("Templates were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldCountAllDatabaseRowsForTemplates() throws Exception {
        Long amount = templateService.countDatabaseRows();
        assertEquals("Templates were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldFindTemplate() throws Exception {
        Template template = templateService.getById(1);
        assertTrue("Template was not found in database!", template.getOrigin().equals("test"));
    }

    @Test
    public void shouldGetAllTemplatesInGivenRange() throws Exception {
        List<Template> templates = templateService.getAll(0,10);
        assertEquals("Not all templates were found in database!", 2, templates.size());
    }

    @Test
    public void shouldRemoveTemplate() throws Exception {
        ProcessService processService = new ServiceManager().getProcessService();

        Process process = processService.getById(1);

        Template template = new Template();
        template.setOrigin("To Remove");
        template.setProcess(process);
        templateService.save(template);
        Template foundTemplate = templateService.getById(3);
        assertEquals("Additional template was not inserted in database!", "To Remove", foundTemplate.getOrigin());

        templateService.remove(foundTemplate);
        exception.expect(DAOException.class);
        templateService.getById(3);

        template = new Template();
        template.setOrigin("To remove");
        template.setProcess(process);
        templateService.save(template);
        foundTemplate = templateService.getById(4);
        assertEquals("Additional template was not inserted in database!", "To remove", foundTemplate.getOrigin());

        templateService.remove(4);
        exception.expect(DAOException.class);
        templateService.getById(4);
    }

    @Test
    public void shouldFindById() throws Exception {
        Integer actual = templateService.findById(1).getId();
        Integer expected = 1;
        assertEquals("Template was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByOrigin() throws Exception {
        List<JSONObject> templates = templateService.findByOrigin("test");
        Integer actual = templates.size();
        Integer expected = 1;
        assertEquals("Template was not found in index!", expected, actual);

        templates = templateService.findByOrigin("tests");
        actual = templates.size();
        expected = 0;
        assertEquals("Templates were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessId() throws Exception {
        List<JSONObject> templates = templateService.findByProcessId(1);
        Integer actual = templates.size();
        Integer expected = 2;
        assertEquals("Workpiece were not found in index!", expected, actual);

        templates = templateService.findByProcessId(4);
        actual = templates.size();
        expected = 0;
        assertEquals("Workpieces were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProcessTitle() throws Exception {
        List<JSONObject> templates = templateService.findByProcessTitle("First process");
        Integer actual = templates.size();
        Integer expected = 2;
        assertEquals("Workpiece was not found in index!", expected, actual);

        templates = templateService.findByProcessTitle("DBConnectionTest");
        actual = templates.size();
        expected = 0;
        assertEquals("Workpieces were found in index!", expected, actual);
    }

    @Test
    public void shouldFindByProperty() throws Exception {
        List<JSONObject> templates = templateService.findByProperty("firstTemplate title", "first value", true);
        Integer actual = templates.size();
        Integer expected = 1;
        assertEquals("Template was not found in index!", expected, actual);

        templates = templateService.findByProperty("FirstUserProperty", "first value", true);
        actual = templates.size();
        expected = 0;
        assertEquals("Templates were found in index!", expected, actual);
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        TemplateDTO template = templateService.findById(1);
        int actual = template.getPropertiesSize();
        assertEquals("Template's properties size is not equal to given value!", 2, actual);
    }
}
