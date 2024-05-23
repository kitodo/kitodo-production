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

package org.kitodo.production.migration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Template;
import org.kitodo.production.services.ServiceManager;

public class TasksToWorkflowConverterIT {

    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldConvertTemplateToWorkflowFile() throws Exception {
        Template template = ServiceManager.getTemplateService().getById(1);

        TasksToWorkflowConverter templateConverter = new TasksToWorkflowConverter();
        templateConverter.convertTasksToWorkflowFile(template.getTitle(), template.getTasks());

        File xmlDiagram = new File(ConfigCore.getKitodoDiagramDirectory() + template.getTitle() + ".bpmn20.xml");
        assertTrue(xmlDiagram.exists(), "Template was not converted to xml file!");

        assertTrue(FileUtils.readFileToString(xmlDiagram, StandardCharsets.UTF_8)
                .contains("<bpmn2:process id=\"First template\" name=\"First template\" isExecutable=\"false\"\n"
                        + "                   template:outputName=\"First template\">"));

        FileUtils.forceDelete(xmlDiagram);
    }
}
