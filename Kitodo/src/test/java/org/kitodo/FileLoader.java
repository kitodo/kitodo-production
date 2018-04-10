package org.kitodo;

import de.sub.goobi.config.ConfigCore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.goobi.production.constants.FileNames;

public class FileLoader {

    private static String digitalCollectionsPath = ConfigCore.getKitodoConfigDirectory() + FileNames.DIGITAL_COLLECTIONS_FILE;
    private static String rulesetPath = ConfigCore.getKitodoConfigDirectory() + "ruleset_test.xml";

    public static  void createDigitalCollectionsFile() throws IOException {
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

    public static void deleteDigitalCollectionsFile() throws IOException {
        Files.deleteIfExists(Paths.get(digitalCollectionsPath));
    }

    public static void deleteRulesetFile() throws IOException {
        Files.deleteIfExists(Paths.get(rulesetPath));
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
