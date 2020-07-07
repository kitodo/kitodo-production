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

import org.apache.commons.lang3.StringUtils;
import org.kitodo.data.database.beans.Role;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.enums.TaskStatus;

class XmlGenerator {

    private static final String END_LINE = "\">\n";
    private static final String SLASH_END_LINE = "\"/>\n";
    private static final String QUOTES = "\" ";
    private static final String OPEN_LABEL = "                <bpmndi:BPMNLabel>\n";
    private static final String CLOSE_LABEL = "                </bpmndi:BPMNLabel>\n";
    private static final String CLOSE_SHAPE = "            </bpmndi:BPMNShape>\n";

    private XmlGenerator() {
        // private constructor for static class
    }

    /**
     * Generate task.
     *
     * @param task
     *            for generating
     * @return generated task
     */
    static String generateTask(Task task, int order) {
        return generateTask(task, "Task_" + (order - 1), "Task_" + order, order);
    }

    /**
     * Generate task.
     *
     * @param task
     *            for generating
     * @param sourceReference
     *            for sequence flow
     * @param targetReference
     *            for sequence flow
     * @return generated task
     */
    static String generateTask(Task task, String sourceReference, String targetReference, int ordering) {
        StringBuilder taskBuilder = new StringBuilder();

        openTask(taskBuilder, task);

        taskBuilder.append("id=\"Task_");
        taskBuilder.append(ordering);
        taskBuilder.append(QUOTES);

        taskBuilder.append("name=\"");
        taskBuilder.append(task.getTitle());
        taskBuilder.append(QUOTES);

        generateTemplateTaskProperty(taskBuilder, "editType", task.getEditType().getValue());
        generateTemplateTaskProperty(taskBuilder, "processingStatus",
            task.getProcessingStatus().equals(TaskStatus.DONE) ? TaskStatus.DONE.getValue()
                    : TaskStatus.LOCKED.getValue());
        generateTemplateTaskProperty(taskBuilder, "concurrent", task.isConcurrent());
        generateTemplateTaskProperty(taskBuilder, "typeMetadata", task.isTypeMetadata());
        generateTemplateTaskProperty(taskBuilder, "separateStructure", task.isSeparateStructure());
        generateTemplateTaskProperty(taskBuilder, "typeAutomatic", task.isTypeAutomatic());
        generateTemplateTaskProperty(taskBuilder, "typeExportDMS", task.isTypeExportDMS());
        generateTemplateTaskProperty(taskBuilder, "typeImagesRead", task.isTypeImagesRead());
        generateTemplateTaskProperty(taskBuilder, "typeImagesWrite", task.isTypeImagesRead());
        generateTemplateTaskProperty(taskBuilder, "typeAcceptClose", task.isTypeAcceptClose());
        generateTemplateTaskProperty(taskBuilder, "typeCloseVerify", task.isTypeCloseVerify());
        generateTemplateTaskProperty(taskBuilder, "batchStep", task.isBatchStep());
        generateTemplateTaskProperty(taskBuilder, "repeatOnCorrection", task.isRepeatOnCorrection());

        if (!task.getRoles().isEmpty()) {
            taskBuilder.append("template:permittedUserRole=\"");
            for (Role role : task.getRoles()) {
                taskBuilder.append(role.getId());
                taskBuilder.append(",");
            }
            taskBuilder.deleteCharAt(taskBuilder.length() - 1);
            taskBuilder.append(QUOTES);
        }

        if (StringUtils.isNotBlank(task.getScriptName()) || StringUtils.isNotBlank(task.getScriptPath())) {
            generateTemplateTaskProperty(taskBuilder, "scriptName", task.getScriptName());
            generateTemplateTaskProperty(taskBuilder, "scriptPath", task.getScriptPath());
        }

        taskBuilder.append(">\n");

        generateSequences(taskBuilder, ordering);
        closeTask(taskBuilder, task);
        generateSequenceFlow(taskBuilder, ordering, sourceReference, targetReference);

        return taskBuilder.toString();
    }

    static void generateEndEvent(StringBuilder diagram, int tasksSize) {
        diagram.append("<bpmn2:endEvent id=\"EndEvent_1\">\n");
        diagram.append("            <bpmn2:incoming>SequenceFlow_");
        diagram.append(tasksSize + 1);
        diagram.append("</bpmn2:incoming>\n");
        diagram.append("        </bpmn2:endEvent>\n");
        diagram.append("<bpmn2:sequenceFlow id=\"SequenceFlow_");
        diagram.append(tasksSize + 1);
        diagram.append("\" sourceRef=\"Task_");
        diagram.append(tasksSize);
        diagram.append("\" targetRef=\"EndEvent_1\"/>\n");
        diagram.append("    </bpmn2:process>\n");
    }

    private static void openTask(StringBuilder taskBuilder, Task task) {
        if (StringUtils.isNotBlank(task.getScriptName()) || StringUtils.isNotBlank(task.getScriptPath())) {
            taskBuilder.append("        <bpmn2:scriptTask ");
        } else {
            taskBuilder.append("        <bpmn2:task ");
        }
    }

    private static void closeTask(StringBuilder taskBuilder, Task task) {
        if (StringUtils.isNotBlank(task.getScriptName()) || StringUtils.isNotBlank(task.getScriptPath())) {
            taskBuilder.append("        </bpmn2:scriptTask>\n");
        } else {
            taskBuilder.append("        </bpmn2:task>\n");
        }
    }

    private static void generateTemplateTaskProperty(StringBuilder taskBuilder, String propertyName,
            Object propertyValue) {
        taskBuilder.append("template:");
        taskBuilder.append(propertyName);
        taskBuilder.append("=\"");
        taskBuilder.append(propertyValue);
        taskBuilder.append(QUOTES);
    }

    private static void generateSequences(StringBuilder taskBuilder, Integer ordering) {
        taskBuilder.append("            <bpmn2:incoming>SequenceFlow_");
        taskBuilder.append(ordering);
        taskBuilder.append("</bpmn2:incoming>\n");
        taskBuilder.append("            <bpmn2:outgoing>SequenceFlow_");
        taskBuilder.append(ordering + 1);
        taskBuilder.append("</bpmn2:outgoing>\n");
    }

    private static void generateSequenceFlow(StringBuilder taskBuilder, Integer ordering, String sourceReference,
            String targetReference) {
        taskBuilder.append("        <bpmn2:sequenceFlow id=\"SequenceFlow_");
        taskBuilder.append(ordering);
        taskBuilder.append("\" sourceRef=\"");
        taskBuilder.append(sourceReference);
        taskBuilder.append("\" targetRef=\"");
        taskBuilder.append(targetReference);
        taskBuilder.append(SLASH_END_LINE);
    }

    /**
     * Generate task shape to display in workflow editor.
     *
     * @param taskOrdering
     *            order of task for which task shape should be generated
     * @param xAxe
     *            reference coordinate
     * @return task shape to display in workflow editor
     */
    static String generateTaskShape(Integer taskOrdering, int xAxe) {
        StringBuilder taskBuilder = new StringBuilder();

        final int diagramHeight = 218;

        taskBuilder.append("            <bpmndi:BPMNShape id=\"Task_");
        taskBuilder.append(taskOrdering);
        taskBuilder.append("_di\" bpmnElement=\"Task_");
        taskBuilder.append(taskOrdering);
        taskBuilder.append(END_LINE);
        generateBounds(taskBuilder, xAxe, diagramHeight, 100, 80);
        taskBuilder.append(CLOSE_SHAPE);
        taskBuilder.append("            <bpmndi:BPMNEdge id=\"SequenceFlow_");
        taskBuilder.append(taskOrdering + 1);
        taskBuilder.append("_di\" bpmnElement=\"SequenceFlow_");
        taskBuilder.append(taskOrdering + 1);
        taskBuilder.append(END_LINE);
        generateWayPoint(taskBuilder, xAxe + 100, diagramHeight + 40);
        generateWayPoint(taskBuilder, xAxe + 150, diagramHeight + 40);
        taskBuilder.append(OPEN_LABEL);
        generateBounds(taskBuilder, xAxe + 125, diagramHeight + 29, 0, 12);
        taskBuilder.append(CLOSE_LABEL);
        taskBuilder.append("            </bpmndi:BPMNEdge>\n");

        return taskBuilder.toString();
    }

    static void generateBpmnDiagram(StringBuilder diagram, String title) {
        final int xAxe = 498;

        diagram.append("    <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n");
        diagram.append("        <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"");
        diagram.append(title);
        diagram.append(END_LINE);
        diagram.append("            <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_1\" bpmnElement=\"StartEvent_1\">\n");
        generateBounds(diagram, 412, 240, 36, 36);
        diagram.append(CLOSE_SHAPE);
        diagram.append("            <bpmndi:BPMNEdge id=\"SequenceFlow_1_di\" bpmnElement=\"SequenceFlow_1\">\n");
        generateWayPoint(diagram, xAxe - 50, 258);
        generateWayPoint(diagram, xAxe, 258);
        diagram.append(OPEN_LABEL);
        generateBounds(diagram, xAxe - 25, 237, 0, 12);
        diagram.append(CLOSE_LABEL);
        diagram.append("            </bpmndi:BPMNEdge>\n");
    }

    static void generateBpmnEndEvent(StringBuilder diagram, int xAxe) {
        diagram.append("            <bpmndi:BPMNShape id=\"EndEvent_1_di\" bpmnElement=\"EndEvent_1\">\n");
        generateBounds(diagram, xAxe, 240, 36, 36);
        diagram.append(OPEN_LABEL);
        generateBounds(diagram, xAxe + 18, 280, 0, 12);
        diagram.append(CLOSE_LABEL);
        diagram.append(CLOSE_SHAPE);
    }

    static void generateCloseBpmnDiagram(StringBuilder diagram) {
        diagram.append("        </bpmndi:BPMNPlane>\n");
        diagram.append("    </bpmndi:BPMNDiagram>\n");
        diagram.append("</bpmn2:definitions>");
    }

    private static void generateBounds(StringBuilder diagram, int xAxe, int yAxe, int width, int height) {
        diagram.append("                <dc:Bounds x=\"");
        diagram.append(xAxe);
        diagram.append("\" y=\"");
        diagram.append(yAxe);
        diagram.append("\" width=\"");
        diagram.append(width);
        diagram.append("\" height=\"");
        diagram.append(height);
        diagram.append(SLASH_END_LINE);
    }

    private static void generateWayPoint(StringBuilder diagram, int xAxe, int yAxe) {
        diagram.append("                <di:waypoint x=\"");
        diagram.append(xAxe);
        diagram.append("\" y=\"");
        diagram.append(yAxe);
        diagram.append(SLASH_END_LINE);
    }
}
