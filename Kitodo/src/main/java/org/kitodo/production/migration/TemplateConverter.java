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
import org.kitodo.data.database.beans.Template;
import org.kitodo.production.services.ServiceManager;

public class TemplateConverter {

    /**
     * Convert given template to workflow file.
     * 
     * @param template
     *            for conversion
     */
    public void convertTemplateToWorkflowFile(Template template) throws IOException {
        String workflow = createWorkflow(template.getTitle(), template.getTasks());
        saveFile(template.getTitle(), workflow);
    }

    /**
     * Convert given tasks to workflow file.
     * 
     * @param title
     *            for workflow
     * @param tasks
     *            for conversion
     */
    public void convertTemplateToWorkflowFile(String title, List<Task> tasks) throws IOException {
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
        for (Task task : tasks) {
            if (task.getOrdering().equals(1)) {
                diagram.append(XmlGenerator.generateTask(task, "StartEvent_1", "Task_" + task.getOrdering()));
            } else {
                diagram.append(XmlGenerator.generateTask(task));
            }
        }
    }

    private void convertTasksToWorkflowCoordinates(StringBuilder diagram, List<Task> tasks) {
        int xAxe = 498;

        for (Task task : tasks) {
            diagram.append(XmlGenerator.generateTaskShape(task.getOrdering(), xAxe));
            xAxe = xAxe + 150;
        }

        XmlGenerator.generateBpmnEndEvent(diagram, xAxe);
    }

    private void saveFile(String title, String fileContent) throws IOException {
        URI xmlDiagramURI = new File(ConfigCore.getKitodoDiagramDirectory() + title + ".bpmn20.xml").toURI();

        try (OutputStream outputStream = ServiceManager.getFileService().write(xmlDiagramURI);
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            bufferedWriter.write(fileContent);
        }
    }
}
