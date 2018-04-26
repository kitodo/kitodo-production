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

import de.sub.goobi.config.ConfigCore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.goobi.production.constants.FileNames;

public class FileLoader {

    private static String diagramReaderTestPath = ConfigCore.getKitodoDiagramDirectory() + "test.bpmn20.xml";
    private static String diagramReaderGatewayPath = ConfigCore.getKitodoDiagramDirectory() + "gateway.bpmn20.xml";
    private static String digitalCollectionsPath = ConfigCore.getKitodoConfigDirectory() + FileNames.DIGITAL_COLLECTIONS_FILE;
    private static String metadataPath = ConfigCore.getKitodoDataDirectory() + "1/meta.xml";
    private static String metadataTemplatePath = ConfigCore.getKitodoDataDirectory() + "1/template.xml";
    private static String rulesetPath = ConfigCore.getKitodoConfigDirectory() + "ruleset_test.xml";

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
        content.add("<bpmn:process id=\"say_hello\" name=\"say-hello\" isExecutable=\"true\" template:outputName=\"Say Hello\">");
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
        content.add("<bpmn2:process id=\"Process_1\" name=\"test-gateway\" template:docket=\"1\" template:outputName=\"Test Gateway\" isExecutable=\"false\">");
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

    public static void createMetadataFile() throws IOException {
        Files.write(Paths.get(metadataPath), getMetadataTemplate());
    }

    public static void createMetadataTemplateFile() throws IOException {
        Files.write(Paths.get(metadataTemplatePath), getMetadataTemplate());
    }

    public static void createRulesetFile() throws IOException {
        List<String> content = new ArrayList<>();
        content.add("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        content.add("<Preferences>");
        content.add("<debug>0</debug>");
        content.addAll(addMetadataPersonType("Autor", "Author"));
        content.addAll(addMetadataPersonType("Herausgeber", "Editor"));
        content.addAll(addMetadataPersonType("Übersetzer", "Translator"));
        content.addAll(addMetadataType("TitleDocMain", "Haupttitel", "main title"));
        content.addAll(addMetadataType("TitleDocMainShort", "Haupttitel (Sortierung)", "main title (sorting)"));
        content.add("<DocStrctType>");
        content.add("<Name>Acknowledgment</Name>");
        content.add("<language name=\"de\">Danksagung</language>");
        content.add("<language name=\"en\">Acknowledgment</language>");
        content.addAll(addAllowedChildType("OtherDocStrct"));
        content.addAll(addMetadataDefaultDisplay("TitleDocMain"));
        content.add("<metadata num=\"1o\">TitleDocMainShort</metadata>");
        content.add("</DocStrctType>");
        content.add("<DocStrctType>");
        content.add("<Name>Article</Name>");
        content.add("<language name=\"de\">Artikel</language>");
        content.add("<language name=\"de\">Artikel</language>");
        content.addAll(addAllowedChildType("Acknowledgment", "Appendix", "Bibliography", "Introduction", "Chapter"));
        content.addAll(addAllowedChildType("TableDescription", "IllustrationDescription", "Errata", "Epilogue"));
        content.addAll(addAllowedChildType("Figure", "Index", "OtherDocStrct", "TitlePage", "TableOfContents"));
        content.addAll(addAllowedChildType("TableOfLiteratureRefs", "Preface", "ListOfIllustrations", "ListOfMaps"));
        content.addAll(addAllowedChildType("ListOfTables", "Theses", "Obituary", "Advertising"));
        content.addAll(addMetadataNum("Author", "Editor", "Translator"));
        content.addAll(addMetadataDefaultDisplay("TitleDocMain", "TitleDocMainShort"));
        content.add("</DocStrctType>");
        content.add("<DocStrctType>");
        content.add("<Name>Monograph</Name>");
        content.add("<language name=\"de\">Monograph</language>");
        content.add("<language name=\"de\">Monographie</language>");
        content.addAll(addMetadataDefaultDisplay("TitleDocMain", "TitleDocMainShort"));
        content.add("</DocStrctType>");
        content.add("<Formats>");
        content.add("<PicaPlus>");
        content.addAll(addFormatPicaPlusPerson("028A"));
        content.addAll(addFormatPicaPlusPerson("028B"));
        content.addAll(addFormatPicaPlusMetadata("010@", "a", "DocLanguage"));
        content.addAll(addFormatPicaPlusMetadata("021A", "a", "TitleDocMain"));
        content.addAll(addFormatPicaPlusMetadata("021A", "d", "TitleDocSub1"));
        content.add("<DocStruct>");
        content.add("<picaMainTag>002@</picaMainTag>");
        content.add("<picaSubTag>0</picaSubTag>");
        content.add("<picaContent>ON</picaContent>");
        content.add("<Name>Newspaper</Name>");
        content.add("</DocStruct>");
        content.add("</PicaPlus>");
        content.add("<METS>");
        content.add("<class>ugh.fileformats.mets.MetsModsImportExport</class>");
        content.add("<Metadata>");
        content.add("<InternalName>TitleDocMain</InternalName>");
        content.add("<WriteXPath>./mods:mods/mods:titleInfo/#mods:title</WriteXPath>");
        content.add("</Metadata>");
        content.add("<DocStruct>");
        content.add("<InternalName>Monograph</InternalName>");
        content.add("<MetsType>monograph</MetsType>");
        content.add("</DocStruct>");
        content.add("</METS>");
        content.add("</Formats>");
        content.add("</Preferences>");

        Files.write(Paths.get(rulesetPath), content);
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

    public static void deleteMetadataFile() throws IOException {
        Files.deleteIfExists(Paths.get(metadataPath));
    }

    public static void deleteMetadataTemplateFile() throws IOException {
        Files.deleteIfExists(Paths.get(metadataTemplatePath));
    }

    public static void deleteRulesetFile() throws IOException {
        Files.deleteIfExists(Paths.get(rulesetPath));
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

    private static List<String> addMetadataType(String name, String deName, String enName) {
        List<String> content = new ArrayList<>();
        content.add("<MetadataType>");
        content.add("<Name>" + name + "</Name>");
        content.add("<language name=\"en\">" + enName + "</language>");
        content.add("<language name=\"de\">" + deName + "</language>");
        content.add("</MetadataType>");
        return content;
    }

    private static List<String> addMetadataPersonType(String deName, String enName) {
        List<String> content = new ArrayList<>();
        content.add("<MetadataType type=\"person\">");
        content.add("<Name>" + enName + "</Name>");
        content.add("<language name=\"en\">" + enName.toLowerCase() + "</language>");
        content.add("<language name=\"de\">" + deName + "</language>");
        content.add("</MetadataType>");
        return content;
    }

    private static List<String> addAllowedChildType(String... params) {
        List<String> content = new ArrayList<>();
        for (String param : params) {
            content.add("<allowedchildtype>" + param + "</allowedchildtype>");
        }
        return content;
    }

    private static List<String> addMetadataNum(String... params) {
        List<String> content = new ArrayList<>();
        for (String param : params) {
            content.add("<metadata num=\"*\">" + param + "</metadata>");
        }
        return content;
    }

    private static List<String> addMetadataDefaultDisplay(String... params) {
        List<String> content = new ArrayList<>();
        for (String param : params) {
            content.add("<metadata DefaultDisplay=\"true\" num=\"1o\">" + param + "</metadata>");
        }
        return content;
    }

    private static List<String> addFormatPicaPlusPerson(String picaMainTag) {
        List<String> content = new ArrayList<>();
        content.add("<Person>");
        content.add("<picaMainTag>" + picaMainTag + "</picaMainTag>");
        content.add("<Name>Author</Name>");
        content.add("<picaSubTag type=\"firstname\">d</picaSubTag>");
        content.add("<picaSubTag type=\"lastname\">a</picaSubTag>");
        content.add("<picaSubTag type=\"identifier\">9</picaSubTag>");
        content.add("<picaSubTag type=\"expansion\">8</picaSubTag>");
        content.add("</Person>");
        return content;
    }

    private static List<String> addFormatPicaPlusMetadata(String picaMainTag, String picaSubTag, String name) {
        List<String> content = new ArrayList<>();
        content.add("<Metadata>");
        content.add("<picaMainTag>" + picaMainTag + "</picaMainTag>");
        content.add("<picaSubTag>" + picaSubTag + "</picaSubTag>");
        content.add("<Name>" + name + "</Name>");
        content.add("</Metadata>");
        return content;
    }
}
