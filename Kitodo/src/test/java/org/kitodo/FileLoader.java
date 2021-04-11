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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
        content.add("<processtitle isdoctype=\"multivolume\">TSL_ATS+'_'+CatalogIDDigital</processtitle>");
        content.add("<processtitle isdoctype=\"monograph\">TSL_ATS+'_'+CatalogIDDigital</processtitle>");
        content.add("<processtitle isdoctype=\"periodical\">TSL_ATS+'_'+CatalogIDDigital</processtitle>");
        content.add("<processtitle isnotdoctype=\"periodical\">TSL_ATS+'_'+CatalogIDDigital</processtitle>");
        content.add("<processtitle isnotdoctype=\"multivolume\">TSL_ATS+'_'+CatalogIDDigital)</processtitle>");
        content.add("</itemlist>");
        content.add("</createNewProcess>");
        content.add("</project>");
        content.add("</kitodoProjects>");
        Files.write(Paths.get(configProjectsPath), content);
    }

    /**
     * Writes a project configuration file as needed for the newspaper processes
     * generator tests.
     */
    public static void createConfigProjectsFileForCalendarHierarchyTests() throws IOException {
        List<String> content = new ArrayList<>();
        content.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.add("<kitodoProjects>");
        addContentToConfigProjectsFileForCalendarHierarchyTests(content);
        addOtherContentToConfigProjectsFileForCalendarHierarchyTests(content);
        addRemainingContentToConfigProjectsFileForCalendarHierarchyTests(content);
        content.add("  </project>");
        content.add("</kitodoProjects>");
        Files.write(Paths.get(configProjectsPath), content);
    }

    private static void addRemainingContentToConfigProjectsFileForCalendarHierarchyTests(List<String> content) {
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"periodical|multivolume|inventory\" ughbinding=\"true\" docstruct=\"firstchild\" me"
                    + "tadata=\"PublicationYear\"> Erscheinungsjahr </item>");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"inventory\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\"slub_link\"> Li"
                    + "nk </item>");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"multivolume|periodical\" ughbinding=\"true\" docstruct=\"firstchild\" metadata=\"P"
                    + "ublisherName\"> Verlag </item>");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"monograph\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\"PublisherName\""
                    + "> Verlag </item>");
        content.add(
            "        <item from=\"vorlage\" ughbinding=\"true\" docstruct=\"physSequence\" metadata=\"ShelfMark\"> Signatur </item>");
        content.add(
            "        <processtitle isdoctype=\"multivolume\">ATS+TSL+'_'+PPN digital f-Satz+'_'+Nummer (Benennung)</processtitle>");
        content.add("        <processtitle isdoctype=\"monograph\">ATS+TSL+'_'+PPN digital a-Satz</processtitle>");
        content.add(
            "        <processtitle isdoctype=\"periodical\">TSL+'_'+PPN digital b-Satz+'_'+Nummer (Benennung)</processtitle>");
        content.add(
            "        <!-- <processtitle isnotdoctype=\"multivolume\">ATS+TSL+'_'+PPN digital c/a-Aufnahmel+'_'+Bandnummer</processtitle> -"
                    + "->");
        content.add(
            "        <processtitle isdoctype=\"newspaper\">Signatur+'_'+#YEAR+#MONTH+#DAY+#ISSU</processtitle>");
        content.add("        <hide/>");
        content.add("      </itemlist>");
        content.add("      <opac use=\"true\">");
        content.add("        <catalogue>GBV</catalogue>");
        content.add("      </opac>");
        content.add("      <templates use=\"true\"/>");
        content.add("      <defaultdoctype>newspaper</defaultdoctype>");
        content.add("      <metadatageneration use=\"true\"/>");
        content.add("    </createNewProcess>");
        content.add("    <tifheader>");
        content.add(
            "      <monograph>'|[[DOC_TYPE]]'+$Doctype+'|[[HAUPTTITEL]]'+TitleDocMain+'|[[AUTOREN/HERAUSGEBER]]'+Autoren+'|[[JAHR]]'"
                    + "+PublicationYear+'|[[ERSCHEINUNGSORT]]'+PlaceOfPublication+'|[[VERZ_STRCT]]'+TSL_ATS+'_'+CatalogIDDigital"
                    + "+'|'</monograph>");
        content.add(
            "      <multivolume>'|[[DOC_TYPE]]'+$Doctype+'|[[HAUPTTITEL]]'+TitleDocMain+'|[[AUTOREN/HERAUSGEBER]]'+Autoren+'|[[JAHR]]'"
                    + "+PublicationYear+'|[[ERSCHEINUNGSORT]]'+PlaceOfPublication+'|[[VERZ_STRCT]]'+TSL_ATS+'_'+CatalogIDDigital+'"
                    + "|'</multivolume>");
        content.add(
            "      <periodical>'|[[DOC_TYPE]]'+$Doctype+'|[[HAUPTTITEL]]'+TitleDocMain+'|[[AUTOREN/HERAUSGEBER]]'+Autoren+'|[[JAHR]]'"
                    + "+PublicationYear+'|[[ERSCHEINUNGSORT]]'+PlaceOfPublication+'|[[VERZ_STRCT]]'+TSL_ATS+'_'+CatalogIDDigital+'|"
                    + "'</periodical>");
        content.add("    </tifheader>");
        content.add("    <dmsImport/>");
        content.add("    <fileUploadActive>false</fileUploadActive>");
    }

    private static void addOtherContentToConfigProjectsFileForCalendarHierarchyTests(List<String> content) {

        content.add(
            "        <item from=\"vorlage\" isdoctype=\"periodical\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadat"
                    + "a=\"CatalogIDSource\"> PPN analog b-Satz </item>");
        content.add(
            "        <item from=\"werk\" isdoctype=\"periodical\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata="
                    + "\"CatalogIDDigital\"> PPN digital b-Satz</item>");
        content.add(
            "        <item from=\"werk\" isdoctype=\"periodical\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata="
                    + "\"ISSN\"> ISSN </item>");
        content.add(
            "        <item from=\"vorlage\" required=\"true\" isdoctype=\"periodical\" ughbinding=\"true\" docstruct=\"firstchild\" metada"
                    + "ta=\"CatalogIDSource\"> PPN analog Band </item>");
        content.add(
            "        <item from=\"werk\" required=\"true\" isdoctype=\"periodical\" ughbinding=\"true\" docstruct=\"firstchild\" metadata="
                    + "\"CatalogIDDigital\"> PPN digital Band </item>");
        content.add("        <!--title, number and authors for multivolumes and periodicals -->");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"multivolume|periodical\" required=\"true\" ughbinding=\"true\" docstruct=\"firstch"
                    + "ild\" metadata=\"TitleDocMain\"> Titel (Band)</item>");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"multivolume|periodical\" required=\"true\" ughbinding=\"true\" docstruct=\"firstch"
                    + "ild\" metadata=\"TitleDocMainShort\"> Titel (Band) (Sortierung)</item>");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"multivolume\" ughbinding=\"true\" docstruct=\"firstchild\" metadata=\"ListOfCreato"
                    + "rs\"> Autoren (Band)</item>");
        content.add(
            "        <item from=\"vorlage\" isnotdoctype=\"monograph\" ughbinding=\"true\" docstruct=\"firstchild\" metadata=\"CurrentNo\""
                    + "> Bandnummer </item>");
        content.add(
            "        <item from=\"vorlage\" isnotdoctype=\"monograph\" ughbinding=\"true\" docstruct=\"firstchild\" metadata=\"CurrentNoSo"
                    + "rting\"> Nummer (Sortierung) </item>");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"multivolume|periodical\" required=\"true\"> Nummer (Benennung) </item>");
        content.add(
            "        <item from=\"vorlage\" required=\"true\" isdoctype=\"multivolume\" ughbinding=\"true\" docstruct=\"firstchild\" metad"
                    + "ata=\"CatalogIDSource\"> PPN analog f-Satz </item>");
        content.add(
            "        <item from=\"werk\" required=\"true\" isdoctype=\"multivolume\" ughbinding=\"true\" docstruct=\"firstchild\" metadata"
                    + "=\"CatalogIDDigital\"> PPN digital f-Satz </item>");
        content.add("        <!-- other metadata for all -->");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"monograph|multivolume|periodical|inventory\" ughbinding=\"true\" docstruct=\"topst"
                    + "ruct\" metadata=\"PlaceOfPublication\"> Erscheinungsort </item>");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"monograph|inventory\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\"Publi"
                    + "cationYear\"> Erscheinungsjahr </item>");
    }

    private static void addContentToConfigProjectsFileForCalendarHierarchyTests(List<String> content) {
        content.add("  <project name=\"First project\">");
        content.add("    <createNewProcess>");
        content.add("      <itemlist>");
        content.add("        <item from=\"werk\"> Artist");
        content.add("              <select label=\"CHANGEME\"> CHANGEME BIBLIOTHEKSLABEL </select>");
        content.add("        </item>");
        content.add("        <item from=\"werk\"> Schrifttyp");
        content.add("            <select label=\"Antiqua\">Antiqua</select>");
        content.add("            <select label=\"Fraktur\">Fraktur</select>");
        content.add("            <select label=\"anderes\">anderes</select>");
        content.add("        </item>");
        content.add("        <!-- title for monograph and periodical -->");
        content.add(
            "        <item from=\"vorlage\" isnotdoctype=\"multivolume\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" met"
                    + "adata=\"TitleDocMain\"> Titel </item>");
        content.add(
            "        <item from=\"vorlage\" isnotdoctype=\"multivolume\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" met"
                    + "adata=\"TitleDocMainShort\"> Titel (Sortierung)</item>");
        content.add("        <!-- title  just for the multivolume -->");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"multivolume\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metada"
                    + "ta=\"TitleDocMain\"> Titel </item>");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"multivolume\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metada"
                    + "ta=\"TitleDocMainShort\"> Titel (Sortierung)</item>");
        content.add("        <!-- authors and creators-->");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"monograph|multivolume|periodical\" ughbinding=\"true\" docstruct=\"topstruct\" met"
                    + "adata=\"ListOfCreators\"> Autoren </item>");
        content.add("        <!-- identifer -->");
        content.add(
            "        <item from=\"werk\" isnotdoctype=\"periodical\" ughbinding=\"true\" metadata=\"TSL_ATS\" docstruct=\"topstruct\">ATS<"
                    + "/item>");
        content.add(
            "        <item from=\"werk\" isdoctype=\"periodical\" ughbinding=\"true\" metadata=\"TSL_ATS\" docstruct=\"topstruct\">TSL</it"
                    + "em>");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"multivolume\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metada"
                    + "ta=\"CatalogIDSource\"> PPN analog c-Satz </item>");
        content.add(
            "        <item from=\"werk\" isdoctype=\"multivolume\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata="
                    + "\"CatalogIDDigital\"> PPN digital c-Satz</item>");
        content.add(
            "        <item from=\"vorlage\" isdoctype=\"monograph\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata"
                    + "=\"CatalogIDSource\"> PPN analog a-Satz </item>");
        content.add(
            "        <item from=\"werk\" isdoctype=\"monograph\" required=\"true\" ughbinding=\"true\" docstruct=\"topstruct\" metadata=\""
                    + "CatalogIDDigital\"> PPN digital a-Satz</item>");
    }

    public static void createDiagramBaseFile() throws IOException {
        Files.write(Paths.get(diagramBasePath), prepareDiagram());
    }

    public static void createDiagramTestFile() throws IOException {
        Files.write(Paths.get(diagramTestPath), prepareDiagram());
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
        content.add("<bpmn:task id=\"Task_1\" name=\"Say hello\" template:priority=\"1\" template:permittedUserRole=\"1\">");
        content.add("<bpmn:incoming>SequenceFlow_0f2vwms</bpmn:incoming>");
        content.add("<bpmn:outgoing>SequenceFlow_1jf1dm1</bpmn:outgoing>");
        content.add("</bpmn:task>");
        content.add("<bpmn:scriptTask id=\"ScriptTask_1\" name=\"Execute script\" template:permittedUserRole=\"1\" template:priority=\"2\" template:scriptName=\"Test script\">");
        content.add("<bpmn:incoming>SequenceFlow_1jf1dm1</bpmn:incoming>");
        content.add("<bpmn:outgoing>SequenceFlow_1jf1dm2</bpmn:outgoing>");
        content.add("</bpmn:scriptTask>");
        content.add("</bpmn:process>");
        content.add("</bpmn:definitions>");

        Files.write(Paths.get(diagramReaderTestPath), content);
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
        content.add("<kitodo:kitodo xmlns:kitodo=\"http://meta.kitodo.org/v1/\">");
        content.add("<kitodo:metadata name=\"TitleDocMain\">First process</kitodo:metadata>");
        content.add("</kitodo:kitodo>");
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
        content.add("<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:camunda=\"http://camunda.org/schema/1.0/bpmn\" xmlns:template=\"http://www.kitodo.org/template\" id=\"Definitions_1\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"1.11.2\">");
        content.add("<bpmn:process id=\"say_hello\" name=\"say-hello\" isExecutable=\"true\">");
        content.add("<bpmn:startEvent id=\"StartEvent_1\" name=\"Start Event\">");
        content.add("<bpmn:outgoing>SequenceFlow_0f2vwms</bpmn:outgoing>");
        content.add("</bpmn:startEvent>");
        content.add("<bpmn:endEvent id=\"EndEvent_1\" name=\"End Event\">");
        content.add("<bpmn:incoming>SequenceFlow_1jf1dm1</bpmn:incoming>");
        content.add("</bpmn:endEvent>");
        content.add("<bpmn:sequenceFlow id=\"SequenceFlow_0f2vwms\" sourceRef=\"StartEvent_1\" targetRef=\"ScriptTask_1\" />");
        content.add("<bpmn:sequenceFlow id=\"SequenceFlow_1jf1dm1\" sourceRef=\"ScriptTask_1\" targetRef=\"EndEvent_1\" />");
        content.add("<bpmn:scriptTask id=\"ScriptTask_1\" name=\"Say hello\" template:permittedUserRole=\"1\">");
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

    public static byte[] readFileToByteArray(File file) throws IOException {
        InputStream fileInputStream;
        byte[] byteArray = new byte[(int) file.length()];
        fileInputStream = Files.newInputStream(file.toPath());
        fileInputStream.read(byteArray);
        fileInputStream.close();
        return byteArray;
    }
}
