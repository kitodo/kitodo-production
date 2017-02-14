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

package org.kitodo.services;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;

/**
 * Tests for TemplateService class.
 */
public class TemplateServiceTestIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException {
        MockDatabase.insertBatches();
        MockDatabase.insertDockets();
        MockDatabase.insertRulesets();
        MockDatabase.insertLdapGroups();
        MockDatabase.insertUsers();
        MockDatabase.insertUserGroups();
        MockDatabase.insertProjects();
        MockDatabase.insertProcesses();
        MockDatabase.insertTemplates();
        MockDatabase.insertTemplateProperties();
    }

    @Ignore("problem with lazy fetching?")
    @Test
    public void shouldFindTemplate() throws Exception {
        TemplateService templateService = new TemplateService();

        Template template = templateService.find(1);
        boolean condition = template.getProperties().size() == 2;
        assertTrue("Template was not found in database!", condition);
    }

    @Ignore("problem with lazy fetching?")
    @Test
    public void shouldGetPropertiesSize() throws Exception {
        TemplateService templateService = new TemplateService();

        Template template = templateService.find(1);
        int actual = templateService.getPropertiesSize(template);
        assertEquals("Template's properties size is not equal to given value!", 2, actual);
    }
}
