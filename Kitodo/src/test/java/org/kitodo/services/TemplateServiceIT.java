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

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Template;
import org.kitodo.data.database.exceptions.DAOException;

import static org.junit.Assert.*;

/**
 * Tests for TemplateService class.
 */
public class TemplateServiceIT {

    @BeforeClass
    public static void prepareDatabase() throws DAOException, IOException {
        MockDatabase.insertProcessesFull();
    }

    @AfterClass
    public static void cleanDatabase() {
        //MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldFindTemplate() throws Exception {
        TemplateService templateService = new TemplateService();

        Template template = templateService.find(1);
        assertTrue("Template was not found in database!", template.getOrigin().equals("test"));
    }

    @Test
    public void shouldGetPropertiesSize() throws Exception {
        TemplateService templateService = new TemplateService();

        Template template = templateService.find(1);
        int actual = templateService.getPropertiesSize(template);
        assertEquals("Template's properties size is not equal to given value!", 2, actual);
    }
}
