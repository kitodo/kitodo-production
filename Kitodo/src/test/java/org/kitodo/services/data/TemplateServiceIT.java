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

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Template;
import org.kitodo.dto.TemplateDTO;
import org.kitodo.services.ServiceManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void shouldCountAllTemplates() throws Exception {
        Long amount = templateService.count();
        assertEquals("Templates were not counted correctly!", Long.valueOf(3), amount);
    }

    @Test
    public void shouldFindAll() throws Exception {
        List<TemplateDTO> templates = templateService.findAll();
        assertEquals("Found incorrect amount of templates!", 3, templates.size());
    }

    @Test
    public void shouldFindTemplatesOfActiveProjects() throws Exception {
        List<TemplateDTO> activeTemplates = templateService.findTemplatesOfActiveProjects(null);
        assertEquals("Found " + activeTemplates.size() + " processes, instead of 1",1, activeTemplates.size());
    }

    @Test
    public void shouldGetTemplates() {
        List<Template> templates = templateService.getAll();
        assertEquals("Found incorrect amount of templates!", 3, templates.size());
    }

    @Test
    public void shouldGetTemplatesWithTitle() {
        List<Template> templates = templateService.getProcessTemplatesWithTitle("First template");
        assertEquals("Incorrect size of templates with given title!", 1, templates.size());
    }

    @Ignore("IN clause doesn't work correctly here - to deeper check out")
    @Test
    public void shouldGetTemplatesForUser() {
        List<Integer> projects = new ArrayList<>();
        projects.add(1);
        List<Template> templates = templateService.getProcessTemplatesForUser(projects);
        assertEquals("Found " + templates.size() + " processes, instead of 1", 1, templates.size());
    }

    @Test
    public void shouldGetContainsUnreachableSteps() throws Exception {
        Template template = templateService.getById(1);
        boolean condition = templateService.containsBeanUnreachableSteps(template.getTasks());
        assertTrue("Process contains unreachable tasks!", !condition);

        TemplateDTO templateDTO = templateService.findById(1);
        condition = templateService.containsDtoUnreachableSteps(templateDTO.getTasks());
        assertTrue("Process DTO contains unreachable tasks!", !condition);

        template = templateService.getById(3);
        condition = templateService.containsBeanUnreachableSteps(template.getTasks());
        assertTrue("Process doesn't contain unreachable tasks!", condition);

        templateDTO = templateService.findById(3);
        condition = templateService.containsDtoUnreachableSteps(templateDTO.getTasks());
        assertTrue("Process DTO doesn't contain unreachable tasks!", condition);
    }
}
