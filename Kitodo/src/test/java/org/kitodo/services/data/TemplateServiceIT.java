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

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.elasticsearch.exceptions.CustomResponseException;
import org.kitodo.data.elasticsearch.search.SearchResult;

/**
 * Tests for TemplateService class.
 */
public class TemplateServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException, CustomResponseException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        // MockDatabase.cleanDatabase();
    }

    @Before
    public void multipleInit() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void shouldFindTemplate() throws Exception {
        TemplateService templateService = new TemplateService();

        Template template = templateService.find(1);
        assertTrue("Template was not found in database!", template.getOrigin().equals("test"));
    }

    @Test
    public void shouldRemoveTemplate() throws Exception {
        TemplateService templateService = new TemplateService();

        Template template = new Template();
        template.setOrigin("To Remove");
        templateService.save(template);
        Template foundTemplate = templateService.convertSearchResultToObject(templateService.findById(3));
        assertEquals("Additional template was not inserted in database!", "To Remove", foundTemplate.getOrigin());

        templateService.remove(foundTemplate);
        foundTemplate = templateService.convertSearchResultToObject(templateService.findById(3));
        assertEquals("Additional template was not removed from database!", null, foundTemplate);

        template = new Template();
        template.setOrigin("To remove");
        templateService.save(template);
        foundTemplate = templateService.convertSearchResultToObject(templateService.findById(4));
        assertEquals("Additional template was not inserted in database!", "To remove", foundTemplate.getOrigin());

        templateService.remove(4);
        foundTemplate = templateService.convertSearchResultToObject(templateService.findById(4));
        assertEquals("Additional template was not removed from database!", null, foundTemplate);
    }

    @Test
    public void shouldFindById() throws Exception {
        TemplateService templateService = new TemplateService();

        SearchResult template = templateService.findById(1);
        Integer actual = template.getId();
        Integer expected = 1;
        assertEquals("Template was not found in index!", expected, actual);
    }

    @Test
    public void shouldFindByOrigin() throws Exception {
        TemplateService templateService = new TemplateService();

        List<SearchResult> templates = templateService.findByOrigin("test");
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
        TemplateService templateService = new TemplateService();

        List<SearchResult> templates = templateService.findByProcessId(1);
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
        TemplateService templateService = new TemplateService();

        List<SearchResult> templates = templateService.findByProcessTitle("First process");
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
        TemplateService templateService = new TemplateService();

        List<SearchResult> templates = templateService.findByProperty("firstTemplate title", "first value");
        Integer actual = templates.size();
        Integer expected = 1;
        assertEquals("Template was not found in index!", expected, actual);

        templates = templateService.findByProperty("FirstUserProperty", "first value");
        actual = templates.size();
        expected = 0;
        assertEquals("Templates were found in index!", expected, actual);
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        TemplateService templateService = new TemplateService();

        Template template = templateService.find(1);
        int actual = templateService.getPropertiesSize(template);
        assertEquals("Template's properties size is not equal to given value!", 2, actual);
    }
}
