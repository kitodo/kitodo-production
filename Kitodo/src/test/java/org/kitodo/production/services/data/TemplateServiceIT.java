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

package org.kitodo.production.services.data;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.Template;
import org.kitodo.exceptions.ProcessGenerationException;
import org.kitodo.production.dto.TemplateDTO;
import org.kitodo.production.services.ServiceManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TemplateServiceIT {

    private static final TemplateService templateService = ServiceManager.getTemplateService();

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCountAllTemplates() throws Exception {
        Long amount = templateService.count();
        assertEquals("Templates were not counted correctly!", Long.valueOf(4), amount);
    }

    @Test
    public void shouldFindAll() throws Exception {
        List<TemplateDTO> templates = templateService.findAll();
        assertEquals("Found incorrect amount of templates!", 4, templates.size());
    }

    @Test
    public void shouldGetTemplate() throws Exception {
        Template template = templateService.getById(1);
        boolean condition = template.getTitle().equals("First template") && template.getId().equals(1);
        assertTrue("Template was not found in database!", condition);

        assertEquals("Template was found but processes were not inserted!", 2, template.getProcesses().size());
        assertEquals("Template was found but tasks were not inserted!", 5, template.getTasks().size());
    }

    @Test
    public void shouldGetTemplates() throws Exception {
        List<Template> templates = templateService.getAll();
        assertEquals("Found incorrect amount of templates!", 4, templates.size());
    }

    @Test
    public void shouldGetTemplatesWithTitle() {
        List<Template> templates = templateService.getProcessTemplatesWithTitle("First template");
        assertEquals("Incorrect size of templates with given title!", 1, templates.size());
    }

    @Test(expected = Test.None.class)
    public void shouldCheckForUnreachableTasks() throws Exception {
        Template template = templateService.getById(1);
        templateService.checkForUnreachableTasks(template.getTasks());
    }

    @Test(expected = ProcessGenerationException.class)
    public void shouldCheckForUnreachableTasksWithException() throws Exception {
        Template template = templateService.getById(3);
        templateService.checkForUnreachableTasks(template.getTasks());
    }

    @Test
    public void shouldHasCompleteTasks() throws Exception {
        TemplateDTO templateDTO = templateService.findById(1);
        boolean condition = templateService.hasCompleteTasks(templateDTO.getTasks());
        assertTrue("Process DTO doesn't have complete tasks!", condition);

        templateDTO = templateService.findById(3);
        condition = templateService.hasCompleteTasks(templateDTO.getTasks());
        assertFalse("Process DTO has complete tasks!", condition);
    }
}
