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

package org.kitodo.production.forms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;
import org.kitodo.config.ConfigCore;
import org.kitodo.data.database.beans.Workflow;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.file.FileService;

public class WorkflowFormTest {

    private static FileService fileService = ServiceManager.getFileService();

    @BeforeClass
    public static void createDiagrams() throws Exception {
        FileLoader.createDiagramBaseFile();
        FileLoader.createDiagramTestFile();
    }

    @AfterClass
    public static void removeDiagram() throws Exception {
        fileService.delete(new File(ConfigCore.getKitodoDiagramDirectory() + "new.bpmn20.xml").toURI());
        fileService.delete(new File(ConfigCore.getKitodoDiagramDirectory() + "test2.bpmn20.xml").toURI());

        FileLoader.deleteDiagramBaseFile();
        FileLoader.deleteDiagramTestFile();
    }

    @Test
    public void shouldReadXMLDiagram() {
        WorkflowForm modelerForm = new WorkflowForm();
        modelerForm.setXmlDiagram(null);
        modelerForm.setWorkflow(new Workflow("test"));
        modelerForm.readXMLDiagram();

        assertNotNull("Diagram XML was not read!", modelerForm.getXmlDiagram());
    }

    @Test
    public void shouldSaveXMLDiagram() {
        String fileName = "test";
        String fileNameWithExtension = fileName + ".bpmn20.xml";

        String xmlDiagram = "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:template=\"http://www.kitodo.org/template\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"1.11.2\">\n" +
                "  <bpmn:process id=\"say_hello\" name=\"say-hello\" isExecutable=\"true\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" name=\"Start Event\">\n" +
                "      <bpmn:outgoing>SequenceFlow_0f2vwms</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\" name=\"End Event\">\n" +
                "      <bpmn:incoming>SequenceFlow_1jf1dm1</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_0f2vwms\" sourceRef=\"StartEvent_1\" targetRef=\"ScriptTask_1\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_1jf1dm1\" sourceRef=\"ScriptTask_1\" targetRef=\"EndEvent_1\" />\n" +
                "    <bpmn:scriptTask id=\"ScriptTask_1\" name=\"Say hello\" template:permittedUserRole=\"1\">\n" +
                "      <bpmn:extensionElements>\n" +
                "        <camunda:inputOutput>\n" +
                "          <camunda:inputParameter name=\"name\" />\n" +
                "          <camunda:outputParameter name=\"Output\" />\n" +
                "        </camunda:inputOutput>\n" +
                "      </bpmn:extensionElements>\n" +
                "      <bpmn:incoming>SequenceFlow_0f2vwms</bpmn:incoming>\n" +
                "      <bpmn:outgoing>SequenceFlow_1jf1dm1</bpmn:outgoing>\n" +
                "      <bpmn:script><![CDATA[kcontext.setVariable(\"welcomeText\",\"Hello, \" + name);\n" +
                "\n" +
                "System.out.println(\"Hello, \" + name);]]></bpmn:script>\n" +
                "    </bpmn:scriptTask>\n" +
                "  </bpmn:process>\n" +
                "</bpmn:definitions>\n";

        File file = new File(ConfigCore.getKitodoDiagramDirectory() + fileNameWithExtension);

        URI xmlDiagramURI = file.toURI();

        WorkflowForm modelerForm = new WorkflowForm();
        modelerForm.setXmlDiagram(xmlDiagram);
        modelerForm.setWorkflow(new Workflow(fileName));
        modelerForm.saveFile(xmlDiagramURI, xmlDiagram);

        assertEquals("Diagram XML was not saved!", xmlDiagram, modelerForm.getXmlDiagram());
        assertTrue("Diagram XML was not saved!", file.exists());

        file.deleteOnExit();
    }

    @Test
    public void shouldSaveSVGDiagram() {
        String fileName = "testSVG";
        String fileNameWithExtension = fileName + ".svg";
        String svgDiagram = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!-- created with bpmn-js / http://bpmn.io -->\n" +
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n" +
                "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" width=\"284\" " +
                "height=\"92\" viewBox=\"406 212 284 92\" version=\"1.1\"><defs><marker " +
                "id=\"sequenceflow-end-white-black-a7fvb5f1j60y9j2p4e4twgf4c\" viewBox=\"0 0 20 20\" refX=\"11\" " +
                "refY=\"10\" markerWidth=\"10\" markerHeight=\"10\" orient=\"auto\"><path d=\"M 1 5 L 11 10 L 1 15 Z\" " +
                "style=\"fill: black; stroke-width: 1px; stroke-linecap: round; stroke-dasharray: 10000, 1; stroke: black;\"/>" +
                "</marker></defs><g class=\"djs-group\"><g class=\"djs-element djs-connection\"" +
                "data-element-id=\"SequenceFlow_1k0kaov\" style=\"display: block;\"><g class=\"djs-visual\">" +
                "<path d=\"m  448,258L498,258 \" style=\"fill: none; stroke-width: 2px; stroke: black; " +
                "stroke-linejoin: round; marker-end: url('#sequenceflow-end-white-black-a7fvb5f1j60y9j2p4e4twgf4c');\"/>" +
                "</g><polyline points=\"448,258 498,258 \" class=\"djs-hit\" style=\"fill: none; stroke-opacity: 0; " +
                "stroke: white; stroke-width: 15px;\"/><rect x=\"442\" y=\"252\" width=\"62\" height=\"12\" " +
                "class=\"djs-outline\" style=\"fill: none;\"/></g></g><g class=\"djs-group\">" +
                "<g class=\"djs-element djs-connection\" data-element-id=\"SequenceFlow_1nsd8wl\" " +
                "style=\"display: block;\"><g class=\"djs-visual\"><path d=\"m  598,258L648,258 \" " +
                "style=\"fill: none; stroke-width: 2px; stroke: black; stroke-linejoin: round; " +
                "marker-end: url('#sequenceflow-end-white-black-a7fvb5f1j60y9j2p4e4twgf4c');\"/></g>" +
                "<polyline points=\"598,258 648,258 \" class=\"djs-hit\" style=\"fill: none; stroke-opacity: 0; " +
                "stroke: white; stroke-width: 15px;\"/><rect x=\"592\" y=\"252\" width=\"62\" height=\"12\" " +
                "class=\"djs-outline\" style=\"fill: none;\"/></g></g><g class=\"djs-group\">" +
                "<g class=\"djs-element djs-shape\" data-element-id=\"StartEvent_1\" style=\"display: block;\" " +
                "transform=\"translate(412 240)\"><g class=\"djs-visual\"><circle cx=\"18\" cy=\"18\" r=\"18\" " +
                "style=\"stroke: black; stroke-width: 2px; fill: white; fill-opacity: 0.95;\"/></g><rect x=\"0\" y=\"0\" " +
                "width=\"36\" height=\"36\" class=\"djs-hit\" style=\"fill: none; stroke-opacity: 0; stroke: white; " +
                "stroke-width: 15px;\"/><rect x=\"-6\" y=\"-6\" width=\"48\" height=\"48\" class=\"djs-outline\" " +
                "style=\"fill: none;\"/></g></g><g class=\"djs-group\"><g class=\"djs-element djs-shape\" " +
                "data-element-id=\"StartEvent_1_label\" style=\"display: none;\" transform=\"translate(385 276)\">" +
                "<g class=\"djs-visual\"><text class=\"djs-label\" style=\"font-family: Arial, sans-serif; " +
                "font-size: 11px;\"><tspan x=\"0\" y=\"12\"/></text></g><rect x=\"0\" y=\"0\" width=\"90\" height=\"20\" " +
                "class=\"djs-hit\" style=\"fill: none; stroke-opacity: 0; stroke: white; stroke-width: 15px;\"/" +
                "><rect x=\"-6\" y=\"-6\" width=\"102\" height=\"32\" class=\"djs-outline\" style=\"fill: none;\"/></g></g>" +
                "<g class=\"djs-group\"><g class=\"djs-element djs-shape\" data-element-id=\"Task_1atal27\" " +
                "style=\"display: block;\" transform=\"translate(498 218)\"><g class=\"djs-visual\"><rect x=\"0\" y=\"0\" " +
                "width=\"100\" height=\"80\" rx=\"10\" ry=\"10\" style=\"stroke: black; stroke-width: 2px; fill: white; " +
                "fill-opacity: 0.95;\"/><text class=\"djs-label\" style=\"font-family: Arial, sans-serif; font-size: 12px; " +
                "fill: black;\"><tspan x=\"50\" y=\"43.5\"/></text></g><rect x=\"0\" y=\"0\" width=\"100\" height=\"80\" " +
                "class=\"djs-hit\" style=\"fill: none; stroke-opacity: 0; stroke: white; stroke-width: 15px;\"/>" +
                "<rect x=\"-6\" y=\"-6\" width=\"112\" height=\"92\" class=\"djs-outline\" style=\"fill: none;\"/></g></g>" +
                "<g class=\"djs-group\"><g class=\"djs-element djs-shape\" data-element-id=\"SequenceFlow_1k0kaov_label\" " +
                "style=\"display: none;\" transform=\"translate(473 237)\"><g class=\"djs-visual\"><text class=\"djs-label\" " +
                "style=\"font-family: Arial, sans-serif; font-size: 11px;\"><tspan x=\"0\" y=\"12\"/></text></g>" +
                "<rect x=\"0\" y=\"0\" width=\"0\" height=\"12\" class=\"djs-hit\" style=\"fill: none; stroke-opacity: 0; " +
                "stroke: white; stroke-width: 15px;\"/><rect x=\"-6\" y=\"-6\" width=\"12\" height=\"24\" " +
                "class=\"djs-outline\" style=\"fill: none;\"/></g></g><g class=\"djs-group\">" +
                "<g class=\"djs-element djs-shape\" data-element-id=\"EndEvent_0fbp90j\" style=\"display: block;\" " +
                "transform=\"translate(648 240)\"><g class=\"djs-visual\"><circle cx=\"18\" cy=\"18\" r=\"18\" " +
                "style=\"stroke: black; stroke-width: 4px; fill: white; fill-opacity: 0.95;\"/></g><rect x=\"0\" y=\"0\" " +
                "width=\"36\" height=\"36\" class=\"djs-hit\" style=\"fill: none; stroke-opacity: 0; stroke: white; " +
                "stroke-width: 15px;\"/><rect x=\"-6\" y=\"-6\" width=\"48\" height=\"48\" class=\"djs-outline\" " +
                "style=\"fill: none;\"/></g></g><g class=\"djs-group\"><g class=\"djs-element djs-shape\" " +
                "data-element-id=\"EndEvent_0fbp90j_label\" style=\"display: none;\" transform=\"translate(666 280)\">" +
                "<g class=\"djs-visual\"><text class=\"djs-label\" style=\"font-family: Arial, sans-serif; font-size: 11px;\">" +
                "<tspan x=\"0\" y=\"12\"/></text></g><rect x=\"0\" y=\"0\" width=\"0\" height=\"12\" class=\"djs-hit\" " +
                "style=\"fill: none; stroke-opacity: 0; stroke: white; stroke-width: 15px;\"/><rect x=\"-6\" y=\"-6\" " +
                "width=\"12\" height=\"24\" class=\"djs-outline\" style=\"fill: none;\"/></g></g><g class=\"djs-group\">" +
                "<g class=\"djs-element djs-shape\" data-element-id=\"SequenceFlow_1nsd8wl_label\" style=\"display: none;\" " +
                "transform=\"translate(623 237)\"><g class=\"djs-visual\"><text class=\"djs-label\" " +
                "style=\"font-family: Arial, sans-serif; font-size: 11px;\"><tspan x=\"0\" y=\"12\"/></text></g>" +
                "<rect x=\"0\" y=\"0\" width=\"0\" height=\"12\" class=\"djs-hit\" style=\"fill: none; stroke-opacity: 0; " +
                "stroke: white; stroke-width: 15px;\"/><rect x=\"-6\" y=\"-6\" width=\"12\" height=\"24\" " +
                "class=\"djs-outline\" style=\"fill: none;\"/></g></g></svg>";

        File file = new File(ConfigCore.getKitodoDiagramDirectory() + fileNameWithExtension);

        URI svgDiagramURI = file.toURI();

        WorkflowForm modelerForm = new WorkflowForm();
        modelerForm.setSvgDiagram(svgDiagram);
        modelerForm.setWorkflow(new Workflow(fileName));
        modelerForm.saveFile(svgDiagramURI, svgDiagram);

        assertEquals("Diagram SVG was not saved!", svgDiagram, modelerForm.getSvgDiagram());
        assertTrue("Diagram SVG was not saved!", file.exists());

        file.deleteOnExit();
    }
}
