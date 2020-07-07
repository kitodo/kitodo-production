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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Comparator;
import java.util.List;

import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Task;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class TasksToWorkflowConverter {

    private static final int BOX_DISTANCE = 150;

    /**
     * Convert given tasks to workflow file.
     *
     * @param title
     *            for workflow
     * @param tasks
     *            for conversion
     */
    public void convertTasksToWorkflowFile(String title, List<Task> tasks) throws IOException {
        String workflow = createWorkflow(title, tasks);
        saveFile(title, workflow);
    }

    private String createWorkflow(String title, List<Task> tasks) {
        tasks.sort(Comparator.comparing(Task::getOrdering));

        StringBuilder xmlDiagram = new StringBuilder(
                "<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                        + "                   xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\"\n"
                        + "                   xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\"\n"
                        + "                   xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\"\n"
                        + "                   xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\"\n"
                        + "                   xmlns:template=\"http://www.kitodo.org/template\" id=\"sample-diagram\"\n"
                        + "                   targetNamespace=\"http://bpmn.io/schema/bpmn\"\n"
                        + "                   xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">\n"
                        + "    <bpmn2:process id=\"" + title + "\" name=\"" + title + "\" isExecutable=\"false\"\n"
                        + "                   template:outputName=\"" + title + "\">\n"
                        + "        <bpmn2:startEvent id=\"StartEvent_1\">\n"
                        + "            <bpmn2:outgoing>SequenceFlow_1</bpmn2:outgoing>\n"
                        + "        </bpmn2:startEvent>\n");

        convertTasksToXmlWorkflow(xmlDiagram, tasks);
        XmlGenerator.generateEndEvent(xmlDiagram, tasks.size());

        XmlGenerator.generateBpmnDiagram(xmlDiagram, title);
        convertTasksToWorkflowCoordinates(xmlDiagram, tasks);
        XmlGenerator.generateCloseBpmnDiagram(xmlDiagram);

        return xmlDiagram.toString();
    }

    private void convertTasksToXmlWorkflow(StringBuilder diagram, List<Task> tasks) {
        for (int i = 0; i<tasks.size(); i++) {
            int diagramOrder = i+1;
            if (i==0) {
                diagram.append(XmlGenerator.generateTask(tasks.get(i), "StartEvent_1", "Task_" + diagramOrder, diagramOrder));
            } else {
                diagram.append(XmlGenerator.generateTask(tasks.get(i), diagramOrder));
            }
        }
    }

    private void convertTasksToWorkflowCoordinates(StringBuilder diagram, List<Task> tasks) {
        int diagramStart = 498;

        for (int i = 1; i<=tasks.size(); i++) {
            diagram.append(XmlGenerator.generateTaskShape(i, diagramStart));
            diagramStart = diagramStart + BOX_DISTANCE;
        }

        XmlGenerator.generateBpmnEndEvent(diagram, diagramStart);
    }

    private void saveFile(String title, String fileContent) throws IOException {
        FileService fileService = ServiceManager.getFileService();
        URI xmlDiagramURI = new File(ConfigCore.getKitodoDiagramDirectory() + title + ".bpmn20.xml").toURI();
        if (fileService.fileExist(xmlDiagramURI)) {
            fileService.delete(xmlDiagramURI);
        }

        try (OutputStream outputStream = fileService.write(xmlDiagramURI);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            bufferedWriter.write(fileContent);
        }
    }
}
