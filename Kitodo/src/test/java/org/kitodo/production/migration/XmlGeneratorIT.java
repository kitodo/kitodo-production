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

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.MockDatabase;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.beans.Template;
import org.kitodo.production.services.ServiceManager;

public class XmlGeneratorIT {

    private static Template template;

    @BeforeClass
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();

        template = ServiceManager.getTemplateService().getById(1);
    }

    @AfterClass
    public static void cleanDatabase() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGenerateTaskWithoutSourceTarget() {
        Task task = template.getTasks().stream().filter(x -> x.getOrdering().equals(2)).findAny().orElse(null);

        String taskString = XmlGenerator.generateTask(task, task.getOrdering());
        String expected = "        <bpmn2:scriptTask id=\"Task_2\" name=\"Blocking\" template:editType=\"1\" "
                + "template:processingStatus=\"3\" template:concurrent=\"false\" template:typeMetadata=\"false\" "
                + "template:typeAutomatic=\"false\" template:typeExportDMS=\"false\" "
                + "template:typeImagesRead=\"false\" template:typeImagesWrite=\"false\" template:typeAcceptClose=\"false\" "
                + "template:typeCloseVerify=\"false\" template:batchStep=\"false\" template:repeatOnCorrection=\"false\" "
                + "template:permittedUserRole=\"1\" template:scriptName=\"scriptName\" "
                + "template:scriptPath=\"../type/automatic/script/path\" >\n"
                + "            <bpmn2:incoming>SequenceFlow_2</bpmn2:incoming>\n"
                + "            <bpmn2:outgoing>SequenceFlow_3</bpmn2:outgoing>\n"
                + "        </bpmn2:scriptTask>\n"
                + "        <bpmn2:sequenceFlow id=\"SequenceFlow_2\" sourceRef=\"Task_1\" targetRef=\"Task_2\"/>\n";

        assertEquals("Generate task is incorrect!", expected, taskString);
    }

    @Test
    public void shouldGenerateTaskWithSourceTarget() {
        Task task = template.getTasks().stream().filter(x -> x.getOrdering().equals(1)).findAny().orElse(null);

        String taskString = XmlGenerator.generateTask(task, "StartEvent_1", task.getOrdering());
        String expected = "        <bpmn2:task id=\"Task_1\" name=\"Finished\" template:editType=\"3\" "
                + "template:processingStatus=\"3\" template:concurrent=\"false\" template:typeMetadata=\"false\" "
                + "template:typeAutomatic=\"false\" template:typeExportDMS=\"false\" "
                + "template:typeImagesRead=\"false\" template:typeImagesWrite=\"false\" template:typeAcceptClose=\"false\" "
                + "template:typeCloseVerify=\"false\" template:batchStep=\"false\" template:repeatOnCorrection=\"false\" "
                + "template:permittedUserRole=\"1\" >\n"
                + "            <bpmn2:incoming>SequenceFlow_1</bpmn2:incoming>\n"
                + "            <bpmn2:outgoing>SequenceFlow_2</bpmn2:outgoing>\n"
                + "        </bpmn2:task>\n"
                + "        <bpmn2:sequenceFlow id=\"SequenceFlow_1\" sourceRef=\"StartEvent_1\" targetRef=\"Task_1\"/>\n";

        assertEquals("Generate task is incorrect!", expected, taskString);
    }

    @Test
    public void shouldGenerateTaskShape() {
        String taskString = XmlGenerator.generateTaskShape(1, 498);

        String expected = "            <bpmndi:BPMNShape id=\"Task_1_di\" bpmnElement=\"Task_1\">\n"
                + "                <dc:Bounds x=\"498\" y=\"218\" width=\"100\" height=\"80\"/>\n"
                + "            </bpmndi:BPMNShape>\n"
                + "            <bpmndi:BPMNEdge id=\"SequenceFlow_2_di\" bpmnElement=\"SequenceFlow_2\">\n"
                + "                <di:waypoint x=\"598\" y=\"258\"/>\n"
                + "                <di:waypoint x=\"648\" y=\"258\"/>\n"
                + "                <bpmndi:BPMNLabel>\n"
                + "                <dc:Bounds x=\"623\" y=\"247\" width=\"0\" height=\"12\"/>\n"
                + "                </bpmndi:BPMNLabel>\n"
                + "            </bpmndi:BPMNEdge>\n";

        assertEquals("Generate task shaped is incorrect!", expected, taskString);
    }
}
