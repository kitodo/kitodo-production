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

package org.kitodo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.KitodoConfigFile;
import org.kitodo.config.enums.ParameterCore;

public class FileLoader {

    private static String configProjectsPath = ConfigCore.getKitodoConfigDirectory() + KitodoConfigFile.PROJECT_CONFIGURATION;
    private static String diagramBasePath = ConfigCore.getKitodoDiagramDirectory() + "base.bpmn20.xml";
    private static String diagramTestPath = ConfigCore.getKitodoDiagramDirectory() + "test.bpmn20.xml";
    private static String diagramReaderTestPath = ConfigCore.getKitodoDiagramDirectory() + "extended-test.bpmn20.xml";
    private static String diagramReaderGatewayPath = ConfigCore.getKitodoDiagramDirectory() + "gateway.bpmn20.xml";
    private static String digitalCollectionsPath = ConfigCore.getKitodoConfigDirectory() + KitodoConfigFile.DIGITAL_COLLECTIONS;
    private static String metadataDisplayRulesPath = ConfigCore.getKitodoConfigDirectory() + KitodoConfigFile.METADATA_DISPLAY_RULES;
    private static String metadataPath = ConfigCore.getKitodoDataDirectory() + "1/meta.xml";
    private static String metadataTemplatePath = ConfigCore.getKitodoDataDirectory() + "1/template.xml";
    private static String errorsCustomBundle = ConfigCore.getParameter(ParameterCore.DIR_LOCAL_MESSAGES) + "errors_en.properties";
    private static String messagesCustomBundle = ConfigCore.getParameter(ParameterCore.DIR_LOCAL_MESSAGES) + "messages_en.properties";

    public static void createConfigProjectsFile() throws IOException {
        List<String> content = new ArrayList<>();
        content.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.add("<kitodoProjects>");
        content.add("<project name=\"default\">");
        content.add("<createNewProcess>");
        content.add("<itemlist>");
        content.add("<item from=\"werk\" multiselect=\"true\">Artist");
        content.add("<select label=\"CHANGEME\">CHANGEME BIBLIOTHEKSLABEL</select>");
        content.add("</item>");
        content.add("<item from=\"werk\" multiselect=\"false\">Schrifttyp");
        content.add("<select label=\"Antiqua\">Antiqua</select>");
        content.add("<select label=\"Fraktur\">Fraktur</select>");
        content.add("<select label=\"Antiqua\">Antiqua</select>");
        content.add("</item>");
        content.add("<item from=\"vorlage\" isnotdoctype=\"multivolume\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\"TitleDocMain\">Titel");
        content.add("</item>");
        content.add("<item from=\"vorlage\" isnotdoctype=\"multivolume\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\"TitleDocMainShort\">Titel (Sortierung)");
        content.add("</item>");
        content.add("<item from=\"vorlage\" isdoctype=\"multivolume\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\"TitleDocMain\">Titel");
        content.add("</item>");
        content.add("<item from=\"vorlage\" isdoctype=\"multivolume\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\"TitleDocMainShort\">Titel (Sortierung)");
        content.add("</item>");
        content.add("<item from=\"vorlage\" isdoctype=\"monograph|multivolume|periodical\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\"ListOfCreators\">Autoren");
        content.add("</item>");
        content.add("<item from=\"werk\" isnotdoctype=\"periodical\" ughbinding=\"true\" metadata=\"TSL_ATS\" docstruct=\"topstruct\"> ATS");
        content.add("</item>");
        content.add("<item from=\"vorlage\" isdoctype=\"monograph\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\"CatalogIDSource\">PPN analog a-Satz");
        content.add("</item>");
        content.add("<item from=\"werk\" isdoctype=\"monograph\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\"CatalogIDDigital\">PPN digital a-Satz");
        content.add("</item>");
        content.add("<processtitle isdoctype=\"multivolume\">ATS+TSL+'_'+PPN digital f-Satz+'_'+Nummer (Benennung)</processtitle>");
        content.add("<processtitle isdoctype=\"monograph\">ATS+TSL+'_'+PPN digital a-Satz</processtitle>");
        content.add("<processtitle isdoctype=\"periodical\">TSL+'_'+PPN digital b-Satz+'_'+Nummer (Benennung)</processtitle>");
        content.add("<processtitle isnotdoctype=\"periodical\">TSL+'_'+PPN digital c/a-Aufnahmel+'_'+Bandnummer</processtitle>");
        content.add("<processtitle isnotdoctype=\"multivolume\">ATS+TSL+'_'+PPN digital c/a-Aufnahmel+'_'+Bandnummer</processtitle>");
        content.add("</itemlist>");
        content.add("</createNewProcess>");
        content.add("</project>");
        content.add("</kitodoProjects>");
        Files.write(Paths.get(configProjectsPath), content);
    }

    public static void createDiagramBaseFile() throws IOException {
        Files.write(Paths.get(diagramBasePath), prepareDiagram());
    }

    public static void createDiagramTestFile() throws IOException {
        Files.write(Paths.get(diagramTestPath), prepareDiagram());
    }

    public static void createDigitalCollectionsFile() throws IOException {
        List<String> content = new ArrayList<>();
        content.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.add("<DigitalCollections>");
        content.add("<default>");
        content.add("<DigitalCollection>Collection 1</DigitalCollection>");
        content.add("<DigitalCollection default=\"true\">Collection 2</DigitalCollection>");
        content.add("<DigitalCollection>Collection 3</DigitalCollection>");
        content.add("</default>");
        content.add("<project>");
        content.add("<name>Project A</name>");
        content.add("<DigitalCollection>Collection 1</DigitalCollection>");
        content.add("<DigitalCollection>Collection 2</DigitalCollection>");
        content.add("<DigitalCollection>Collection 3</DigitalCollection>");
        content.add("<DigitalCollection>Collection 4</DigitalCollection>");
        content.add("<DigitalCollection>Collection 5</DigitalCollection>");
        content.add("</project>");
        content.add("<project>");
        content.add("<name>Project B</name>");
        content.add("<DigitalCollection>Collection 1</DigitalCollection>");
        content.add("</project>");
        content.add("</DigitalCollections>");
        Files.write(Paths.get(digitalCollectionsPath), content);
    }

    public static void createExtendedDiagramTestFile() throws IOException {
        List<String> content = new ArrayList<>();
        content.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.add("<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" xmlns:template=\"http://www.kitodo.org/template\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"1.11.2\">");
        content.add("<bpmn:process id=\"say_hello\" name=\"say-hello\" isExecutable=\"true\">");
        content.add("<bpmn:startEvent id=\"StartEvent_1\" name=\"Start Event\">");
        content.add("<bpmn:outgoing>SequenceFlow_0f2vwms</bpmn:outgoing>");
        content.add("</bpmn:startEvent>");
        content.add("<bpmn:endEvent id=\"EndEvent_1\" name=\"End Event\">");
        content.add("<bpmn:incoming>SequenceFlow_1jf1dm2</bpmn:incoming>");
        content.add("</bpmn:endEvent>");
        content.add("<bpmn:sequenceFlow id=\"SequenceFlow_0f2vwms\" sourceRef=\"StartEvent_1\" targetRef=\"Task_1\" />");
        content.add("<bpmn:sequenceFlow id=\"SequenceFlow_1jf1dm1\" sourceRef=\"Task_1\" targetRef=\"ScriptTask_1\" />");
        content.add("<bpmn:sequenceFlow id=\"SequenceFlow_1jf1dm2\" sourceRef=\"ScriptTask_1\" targetRef=\"EndEvent_1\" />");
        content.add("<bpmn:task id=\"Task_1\" name=\"Say hello\" template:priority=\"1\">");
        content.add("<bpmn:incoming>SequenceFlow_0f2vwms</bpmn:incoming>");
        content.add("<bpmn:outgoing>SequenceFlow_1jf1dm1</bpmn:outgoing>");
        content.add("</bpmn:task>");
        content.add("<bpmn:scriptTask id=\"ScriptTask_1\" name=\"Execute script\" template:priority=\"2\" template:scriptName=\"Test script\">");
        content.add("<bpmn:incoming>SequenceFlow_1jf1dm1</bpmn:incoming>");
        content.add("<bpmn:outgoing>SequenceFlow_1jf1dm2</bpmn:outgoing>");
        content.add("</bpmn:scriptTask>");
        content.add("</bpmn:process>");
        content.add("</bpmn:definitions>");

        Files.write(Paths.get(diagramReaderTestPath), content);
    }

    public static void createExtendedGatewayDiagramTestFile() throws IOException {
        List<String> content = new ArrayList<>();
        content.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.add("<bpmn2:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:bpmn2=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:template=\"http://www.kitodo.org/template\" id=\"sample-diagram\" targetNamespace=\"http://bpmn.io/schema/bpmn\" xsi:schemaLocation=\"http://www.omg.org/spec/BPMN/20100524/MODEL BPMN20.xsd\">");
        content.add("<bpmn2:process id=\"Process_1\" name=\"test-gateway\" isExecutable=\"false\">");
        content.add("<bpmn2:startEvent id=\"StartEvent_1\">");
        content.add("<bpmn2:outgoing>SequenceFlow_0651lvf</bpmn2:outgoing>");
        content.add("</bpmn2:startEvent>");
        content.add("<bpmn2:task id=\"Task_0q73jg6\" name=\"First task\">");
        content.add("<bpmn2:incoming>SequenceFlow_0651lvf</bpmn2:incoming>");
        content.add("<bpmn2:outgoing>SequenceFlow_1uym1c8</bpmn2:outgoing>");
        content.add("</bpmn2:task>");
        content.add("<bpmn2:sequenceFlow id=\"SequenceFlow_0651lvf\" sourceRef=\"StartEvent_1\" targetRef=\"Task_0q73jg6\" />");
        content.add("<bpmn2:task id=\"Task_02rdayp\" name=\"Some normal task\">");
        content.add("<bpmn2:incoming>SequenceFlow_0k77eji</bpmn2:incoming>");
        content.add("<bpmn2:outgoing>SequenceFlow_08oilfh</bpmn2:outgoing>");
        content.add("</bpmn2:task>");
        content.add("<bpmn2:task id=\"Task_1m88bz0\" name=\"Some other normal\">");
        content.add("<bpmn2:incoming>SequenceFlow_08oilfh</bpmn2:incoming>");
        content.add("<bpmn2:outgoing>SequenceFlow_10opxw7</bpmn2:outgoing>");
        content.add("</bpmn2:task>");
        content.add("<bpmn2:task id=\"Task_0dzgqnz\" name=\"Ending task\">");
        content.add("<bpmn2:incoming>SequenceFlow_10opxw7</bpmn2:incoming>");
        content.add("<bpmn2:incoming>SequenceFlow_171r48b</bpmn2:incoming>");
        content.add("<bpmn2:incoming>SequenceFlow_035iml6</bpmn2:incoming>");
        content.add("<bpmn2:outgoing>SequenceFlow_1ntrtjs</bpmn2:outgoing>");
        content.add("</bpmn2:task>");
        content.add("<bpmn2:endEvent id=\"EndEvent_1jlzp4p\">");
        content.add("<bpmn2:incoming>SequenceFlow_1ntrtjs</bpmn2:incoming>");
        content.add("</bpmn2:endEvent>");
        content.add("<bpmn2:exclusiveGateway id=\"ExclusiveGateway_0dclvs2\" default=\"SequenceFlow_035iml6\">");
        content.add("<bpmn2:incoming>SequenceFlow_1uym1c8</bpmn2:incoming>");
        content.add("<bpmn2:outgoing>SequenceFlow_0w0rghc</bpmn2:outgoing>");
        content.add("<bpmn2:outgoing>SequenceFlow_035iml6</bpmn2:outgoing>");
        content.add("<bpmn2:outgoing>SequenceFlow_0k77eji</bpmn2:outgoing>");
        content.add("</bpmn2:exclusiveGateway>");
        content.add("<bpmn2:sequenceFlow id=\"SequenceFlow_0w0rghc\" sourceRef=\"ExclusiveGateway_0dclvs2\" targetRef=\"Task_0w8342u\" name=\"${type==1}\">");
        content.add("<bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${type == 1}</bpmn2:conditionExpression>");
        content.add("</bpmn2:sequenceFlow>");
        content.add("<bpmn2:sequenceFlow id=\"SequenceFlow_0k77eji\" sourceRef=\"ExclusiveGateway_0dclvs2\" targetRef=\"Task_02rdayp\" name=\"${type==2}\">");
        content.add("<bpmn2:conditionExpression xsi:type=\"bpmn2:tFormalExpression\">${type == 2}</bpmn2:conditionExpression>");
        content.add("</bpmn2:sequenceFlow>");
        content.add("<bpmn2:sequenceFlow id=\"SequenceFlow_035iml6\" sourceRef=\"ExclusiveGateway_0dclvs2\" targetRef=\"Task_0dzgqnz\" name=\"else\" />");
        content.add("<bpmn2:sequenceFlow id=\"SequenceFlow_1uym1c8\" sourceRef=\"Task_0q73jg6\" targetRef=\"ExclusiveGateway_0dclvs2\" />");
        content.add("<bpmn2:sequenceFlow id=\"SequenceFlow_08oilfh\" sourceRef=\"Task_02rdayp\" targetRef=\"Task_1m88bz0\" />");
        content.add("<bpmn2:sequenceFlow id=\"SequenceFlow_10opxw7\" sourceRef=\"Task_1m88bz0\" targetRef=\"Task_0dzgqnz\" />");
        content.add("<bpmn2:sequenceFlow id=\"SequenceFlow_171r48b\" sourceRef=\"Task_0w8342u\" targetRef=\"Task_0dzgqnz\" />");
        content.add("<bpmn2:sequenceFlow id=\"SequenceFlow_1ntrtjs\" sourceRef=\"Task_0dzgqnz\" targetRef=\"EndEvent_1jlzp4p\" />");
        content.add("<bpmn2:scriptTask id=\"Task_0w8342u\" name=\"Script task\">");
        content.add("<bpmn2:incoming>SequenceFlow_0w0rghc</bpmn2:incoming>");
        content.add("<bpmn2:outgoing>SequenceFlow_171r48b</bpmn2:outgoing>");
        content.add("</bpmn2:scriptTask>");
        content.add("</bpmn2:process>");
        content.add("</bpmn2:definitions>");

        Files.write(Paths.get(diagramReaderGatewayPath), content);
    }

    public static void createMetadataDisplayRulesFile() throws IOException {
        List<String> content = new ArrayList<>();
        content.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.add("<displayRules xmlns=\"http://meta.goobi.org/displayRules\" xmlns:tns=\"http://meta.goobi.org/displayRules\">");
        content.add("<ruleSet>");
        content.add("<context projectName=\"DigiNews\">");
        content.add("<bind>edit</bind>");
        content.add("<select1 tns:ref=\"NotePreImport\">");
        content.add("<item tns:selected=\"false\">");
        content.add("<label>Hinweistext einfügen</label>");
        content.add("<value>Hinweis für Nutzer: ...</value>");
        content.add("</item>");
        content.add("<item tns:selected=\"false\">");
        content.add("<label>Hinweistext ausblenden</label>");
        content.add("<value></value>");
        content.add("</item>");
        content.add("</select1>");
        content.add("<select1 tns:ref=\"AccessLicenseGlobal\">");
        content.add("<item tns:selected=\"false\">");
        content.add("<label>DigiNews Abo</label>");
        content.add("<value>Gesamtabo</value>");
        content.add("</item>");
        content.add("<item tns:selected=\"false\">");
        content.add("<label>Open Access</label>");
        content.add("<value>free</value>");
        content.add("</item>");
        content.add("<item tns:selected=\"false\">");
        content.add("<label>Gesperrt</label>");
        content.add("<value>Gesperrt</value>");
        content.add("</item>");
        content.add("</select1>");
        content.add("</context>");
        content.add("</ruleSet>");
        content.add("</displayRules>");

        Files.write(Paths.get(metadataDisplayRulesPath), content);
    }

    public static void createMetadataFile() throws IOException {
        Files.write(Paths.get(metadataPath), getMetadataTemplate());
    }

    public static void createMetadataTemplateFile() throws IOException {
        Files.write(Paths.get(metadataTemplatePath), getMetadataTemplate());
    }

    public static void createCustomErrors() throws IOException {
        List<String> content = new ArrayList<>();
        content.add("custom=Test custom error");
        content.add("error=Test custom error");

        Files.write(Paths.get(errorsCustomBundle), content);
    }

    public static void createCustomMessages() throws IOException {
        List<String> content = new ArrayList<>();
        content.add("custom=Test custom message");
        content.add("ready=Test custom message");

        Files.write(Paths.get(messagesCustomBundle), content);
    }

    public static void deleteConfigProjectsFile() throws IOException {
        Files.deleteIfExists(Paths.get(configProjectsPath));
    }

    public static void deleteDiagramBaseFile() throws IOException {
        Files.deleteIfExists(Paths.get(diagramBasePath));
    }

    public static void deleteDiagramTestFile() throws IOException {
        Files.deleteIfExists(Paths.get(diagramTestPath));
    }

    public static void deleteExtendedDiagramTestFile() throws IOException {
        Files.deleteIfExists(Paths.get(diagramReaderTestPath));
    }

    public static void deleteExtendedGatewayDiagramTestFile() throws IOException {
        Files.deleteIfExists(Paths.get(diagramReaderGatewayPath));
    }

    public static void deleteDigitalCollectionsFile() throws IOException {
        Files.deleteIfExists(Paths.get(digitalCollectionsPath));
    }

    public static void deleteMetadataDisplayRulesFile() throws IOException {
        Files.deleteIfExists(Paths.get(metadataDisplayRulesPath));
    }

    public static void deleteMetadataFile() throws IOException {
        Files.deleteIfExists(Paths.get(metadataPath));
    }

    public static void deleteMetadataTemplateFile() throws IOException {
        Files.deleteIfExists(Paths.get(metadataTemplatePath));
    }

    public static void deleteCustomErrors() throws IOException {
        Files.deleteIfExists(Paths.get(errorsCustomBundle));
    }

    public static void deleteCustomMessages() throws IOException {
        Files.deleteIfExists(Paths.get(messagesCustomBundle));
    }

    private static List<String> getMetadataTemplate() {
        List<String> content = new ArrayList<>();
        content.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.add("<mets:mets xsi:schemaLocation=\"info:lc/xmlns/premis-v2 http://www.loc.gov/standards/premis/v2/premis-v2-0.xsd "
                + "http://www.loc.gov/mods/v3 http://www.loc.gov/standards/mods/mods.xsd http://www.loc.gov/METS/ "
                + "http://www.loc.gov/standards/mets/mets.xsd http://www.loc.gov/mix/v10 http://www.loc.gov/standards/mix/mix10/mix10.xsd\" "
                + "xmlns:mets=\"http://www.loc.gov/METS/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        content.add("<mets:dmdSec ID=\"DMDLOG_0000\">");
        content.add("<mets:mdWrap MDTYPE=\"MODS\">");
        content.add("<mets:xmlData>");
        content.add("<mods:mods xmlns:mods=\"http://www.loc.gov/mods/v3\">");
        content.add("<mods:extension>");
        content.add("<goobi:goobi xmlns:goobi=\"http://meta.goobi.org/v1.5.1/\">");
        content.add("<goobi:metadata name=\"TitleDocMain\">First process</goobi:metadata>");
        content.add("</goobi:goobi>");
        content.add("</mods:extension>");
        content.add("</mods:mods>");
        content.add("</mets:xmlData>");
        content.add("</mets:mdWrap>");
        content.add("</mets:dmdSec>");
        content.add("<mets:structMap TYPE=\"LOGICAL\">");
        content.add("<mets:div DMDID=\"DMDLOG_0000\" ID=\"LOG_0000\" TYPE=\"Monograph\"/>");
        content.add("</mets:structMap>");
        content.add("</mets:mets>");
        return content;
    }

    private static List<String> prepareDiagram() {
        List<String> content = new ArrayList<>();
        content.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.add("<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"1.11.2\">");
        content.add("<bpmn:process id=\"say_hello\" name=\"say-hello\" isExecutable=\"true\">");
        content.add("<bpmn:startEvent id=\"StartEvent_1\" name=\"Start Event\">");
        content.add("<bpmn:outgoing>SequenceFlow_0f2vwms</bpmn:outgoing>");
        content.add("</bpmn:startEvent>");
        content.add("<bpmn:endEvent id=\"EndEvent_1\" name=\"End Event\">");
        content.add("<bpmn:incoming>SequenceFlow_1jf1dm1</bpmn:incoming>");
        content.add("</bpmn:endEvent>");
        content.add("<bpmn:sequenceFlow id=\"SequenceFlow_0f2vwms\" sourceRef=\"StartEvent_1\" targetRef=\"ScriptTask_1\" />");
        content.add("<bpmn:sequenceFlow id=\"SequenceFlow_1jf1dm1\" sourceRef=\"ScriptTask_1\" targetRef=\"EndEvent_1\" />");
        content.add("<bpmn:scriptTask id=\"ScriptTask_1\" name=\"Say hello\">");
        content.add("<bpmn:extensionElements>");
        content.add("<camunda:inputOutput>");
        content.add("<camunda:inputParameter name=\"name\" />");
        content.add("<camunda:outputParameter name=\"Output\" />");
        content.add("</camunda:inputOutput>");
        content.add("</bpmn:extensionElements>");
        content.add("<bpmn:incoming>SequenceFlow_0f2vwms</bpmn:incoming>");
        content.add("<bpmn:outgoing>SequenceFlow_1jf1dm1</bpmn:outgoing>");
        content.add("<bpmn:script><![CDATA[kcontext.setVariable(\"welcomeText\",\"Hello, \" + name);");
        content.add("");
        content.add("System.out.println(\"Hello, \" + name);]]></bpmn:script>");
        content.add("</bpmn:scriptTask>");
        content.add("</bpmn:process>");
        content.add("</bpmn:definitions>");
        return content;
    }
}
