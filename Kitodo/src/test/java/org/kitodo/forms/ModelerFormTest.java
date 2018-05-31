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

package org.kitodo.forms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import de.sub.goobi.config.ConfigCore;

import java.io.File;
import java.net.URI;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kitodo.FileLoader;
import org.kitodo.services.ServiceManager;
import org.kitodo.services.file.FileService;

public class ModelerFormTest {

    private static FileService fileService = new ServiceManager().getFileService();


    @BeforeClass
    public static void createDiagrams() throws Exception {
        fileService.createDirectory(URI.create(""), "diagrams");

        FileLoader.createDiagramBaseFile();
        FileLoader.createDiagramTestFile();
    }

    @AfterClass
    public static void removeDiagram() throws Exception {
        fileService.delete(new File(ConfigCore.getKitodoDiagramDirectory() + "new.bpmn20.xml").toURI());
        fileService.delete(new File(ConfigCore.getKitodoDiagramDirectory() + "test2.bpmn20.xml").toURI());

        FileLoader.deleteDiagramBaseFile();
        FileLoader.deleteDiagramTestFile();

        fileService.delete(URI.create("diagrams"));
    }

    @Test
    public void shouldCreateXMLDiagram() {
        ModelerForm modelerForm = new ModelerForm();
        modelerForm.setXmlDiagram(null);

        modelerForm.setNewXMLDiagramName("new.bpmn20.xml");
        modelerForm.createXMLDiagram();
        assertTrue("Diagram XML was not read!", fileService.fileExist(new File(ConfigCore.getKitodoDiagramDirectory() + "new.bpmn20.xml").toURI()));

        modelerForm.readXMLDiagram();
        assertNotNull("Diagram XML was not read!", modelerForm.getXmlDiagram());
    }

    @Test
    public void shouldReadXMLDiagram() {
        ModelerForm modelerForm = new ModelerForm();
        modelerForm.setXmlDiagram(null);
        modelerForm.setXmlDiagramName("test.bpmn20.xml");
        modelerForm.readXMLDiagram();

        assertNotNull("Diagram XML was not read!", modelerForm.getXmlDiagram());
    }

    @Test
    public void shouldSaveXMLDiagram() {
        String xmlDiagram = "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"1.11.2\">\n" +
                "  <bpmn:process id=\"say_hello\" name=\"say-hello\" isExecutable=\"true\">\n" +
                "    <bpmn:startEvent id=\"StartEvent_1\" name=\"Start Event\">\n" +
                "      <bpmn:outgoing>SequenceFlow_0f2vwms</bpmn:outgoing>\n" +
                "    </bpmn:startEvent>\n" +
                "    <bpmn:endEvent id=\"EndEvent_1\" name=\"End Event\">\n" +
                "      <bpmn:incoming>SequenceFlow_1jf1dm1</bpmn:incoming>\n" +
                "    </bpmn:endEvent>\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_0f2vwms\" sourceRef=\"StartEvent_1\" targetRef=\"ScriptTask_1\" />\n" +
                "    <bpmn:sequenceFlow id=\"SequenceFlow_1jf1dm1\" sourceRef=\"ScriptTask_1\" targetRef=\"EndEvent_1\" />\n" +
                "    <bpmn:scriptTask id=\"ScriptTask_1\" name=\"Say hello\">\n" +
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

        ModelerForm modelerForm = new ModelerForm();
        modelerForm.setXmlDiagram(xmlDiagram);
        modelerForm.setXmlDiagramName("test2.bpmn20.xml");
        modelerForm.saveXMLDiagram();

        assertEquals("Diagram XML was not saved!", xmlDiagram, modelerForm.getXmlDiagram());
    }
}
