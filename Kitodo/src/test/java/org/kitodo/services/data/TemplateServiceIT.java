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

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.dto.ProcessDTO;
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
        Long amount = templateService.countTemplates();
        assertEquals("Templates were not counted correctly!", Long.valueOf(2), amount);
    }

    @Test
    public void shouldFindAllTemplates() throws Exception {
        List<ProcessDTO> allTemplates = templateService.findAllTemplates(null);
        assertTrue("Found " + allTemplates.size() + " processes, instead of 2", allTemplates.size() == 2);
    }
}
