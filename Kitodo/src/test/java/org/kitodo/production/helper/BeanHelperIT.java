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

package org.kitodo.production.helper;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Template;
import org.kitodo.production.MockDatabase;
import org.kitodo.production.workflow.model.Converter;

import static org.junit.Assert.assertEquals;

public class BeanHelperIT {

    @BeforeClass
    public static void setUp() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertRolesFull();
        MockDatabase.insertDockets();
        MockDatabase.insertRulesets();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldCopyTasks() throws Exception {
        Converter converter = new Converter("gateway-test1");

        Template template = new Template();
        template.setTitle("Title");
        converter.convertWorkflowToTemplate(template);
        Process process = new Process();

        BeanHelper.copyTasks(template, process);
        int actual = process.getTasks().size();
        assertEquals("Task were copied incorrectly!", 5, actual);
    }
}
