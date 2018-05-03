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

package org.kitodo.workflow.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Template;
import org.kitodo.services.ServiceManager;

public class UpdaterIT {

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
    public void shouldUpdateProcessesAssignedToTemplate() throws Exception {
        Template template = new ServiceManager().getTemplateService().getById(1);

        for (Process process : template.getProcesses()) {
            assertNotEquals("Template and Process have assigned the same docket!", template.getDocket(), process.getDocket());
            assertNotEquals("Template and Process have assigned the same ruleset!", template.getRuleset(), process.getRuleset());
        }

        Updater updater = new Updater(template);
        updater.updateProcessesAssignedToTemplate();

        for (Process process : template.getProcesses()) {
            assertEquals("Template and Process have assigned different docket!", template.getDocket(), process.getDocket());
            assertEquals("Template and Process have assigned different ruleset!", template.getRuleset(), process.getRuleset());
        }
    }
}
