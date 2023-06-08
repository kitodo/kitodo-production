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

package org.kitodo.xmlschemaconverter;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.api.schemaconverter.MetadataFormatConversion;
import org.kitodo.config.KitodoConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class XmlSchemaConverterTest {

    private static final XMLSchemaConverter converter = new XMLSchemaConverter();
    private static final String MODS_TEST_FILE_PATH = "src/test/resources/modsXmlTestRecord.xml";
    private static final String MARC_TEST_FILE_PATH = "src/test/resources/marcXmlTestRecord.xml";

    @Test
    public void shouldConvertModsToInternalFormat() throws IOException, ParserConfigurationException, SAXException,
            URISyntaxException {
        DataRecord testRecord = new DataRecord();
        testRecord.setMetadataFormat(MetadataFormat.MODS);
        testRecord.setFileFormat(FileFormat.XML);

        DataRecord internalFormatRecord;

        try (InputStream inputStream = Files.newInputStream(Paths.get(MODS_TEST_FILE_PATH))) {
            testRecord.setOriginalData(IOUtils.toString(inputStream, Charset.defaultCharset()));
            List<File> xsltFiles = getXsltFiles(MetadataFormat.MODS);
            internalFormatRecord = converter.convert(testRecord, MetadataFormat.KITODO, FileFormat.XML, xsltFiles);
        }

        Assert.assertNotNull("Conversion result is empty!", internalFormatRecord);
        Assert.assertEquals("Conversion result has wrong MetadataFormat!",
                MetadataFormat.KITODO, internalFormatRecord.getMetadataFormat());
        Assert.assertEquals("Conversion result has wrong FileFormat!", FileFormat.XML, internalFormatRecord.getFileFormat());
        Assert.assertThat("Wrong class of original data object!", internalFormatRecord.getOriginalData(), instanceOf(String.class));
        Document resultDocument = parseInputStreamToDocument((String) internalFormatRecord.getOriginalData());
        NodeList metadataNodes = resultDocument.getElementsByTagName("kitodo:metadata");

        String title = "";
        String catalogId = "";
        String year = "";
        for (int i = 0; i < metadataNodes.getLength(); i++) {
            Element element = (Element) metadataNodes.item(i);
            switch (element.getAttribute("name")) {
                case "CatalogIDDigital":
                    catalogId = element.getTextContent();
                    break;
                case "TitleDocMain":
                    title = element.getTextContent();
                    break;
                case "PublicationYear":
                    year = element.getTextContent();
                    break;
                default:
                    // ignore other elements
                    break;
            }
        }

        Assert.assertEquals("Title after conversion is wrong!", "Test-Title", title);
        Assert.assertEquals("Catalog ID after conversion is wrong!", "67890", catalogId);
        Assert.assertEquals("PublicationYear after conversion is wrong!", "1999", year);
    }

    @Test
    public void shouldConvertMarcToInternalFormat() throws IOException, ParserConfigurationException, SAXException,
            URISyntaxException {
        DataRecord testRecord = new DataRecord();
        testRecord.setMetadataFormat(MetadataFormat.MARC);
        testRecord.setFileFormat(FileFormat.XML);

        DataRecord internalFormatRecord;

        try (InputStream inputStream = Files.newInputStream(Paths.get(MARC_TEST_FILE_PATH))) {
            testRecord.setOriginalData(IOUtils.toString(inputStream, Charset.defaultCharset()));
            List<File> xsltFiles = getXsltFiles(MetadataFormat.MARC);
            internalFormatRecord = converter.convert(testRecord, MetadataFormat.KITODO, FileFormat.XML, xsltFiles);
        }

        Assert.assertNotNull("Conversion result is empty!", internalFormatRecord);
        Assert.assertEquals("Conversion result has wrong MetadataFormat!",
                MetadataFormat.KITODO, internalFormatRecord.getMetadataFormat());
        Assert.assertEquals("Conversion result has wrong FileFormat!", FileFormat.XML, internalFormatRecord.getFileFormat());
        Assert.assertThat("Wrong class of original data object!", internalFormatRecord.getOriginalData(), instanceOf(String.class));
        Document resultDocument = parseInputStreamToDocument((String) internalFormatRecord.getOriginalData());
        NodeList metadataNodes = resultDocument.getElementsByTagName("kitodo:metadata");

        String title = "";
        String catalogId = "";
        String place = "";
        String shelfmarksource = "";
        for (int i = 0; i < metadataNodes.getLength(); i++) {
            Element element = (Element) metadataNodes.item(i);
            switch (element.getAttribute("name")) {
                case "CatalogIDDigital":
                    catalogId = element.getTextContent();
                    break;
                case "TitleDocMain":
                    title = element.getTextContent();
                    break;
                case "PlaceOfPublication":
                    place = element.getTextContent();
                    break;
                case "shelfmarksource":
                    shelfmarksource = element.getTextContent();
                    break;
                default:
                    // ignore other elements
                    break;
            }
        }

        Assert.assertEquals("Title after conversion is wrong!", "Test-Title", title);
        Assert.assertEquals("Catalog ID after conversion is wrong!", "67890", catalogId);
        Assert.assertEquals("PlaceOfPublication after conversion is wrong!", "Test-Place", place);
        Assert.assertEquals("shelfmarksource after conversion is wrong!", "Test-Shelflocator", shelfmarksource);
    }

    private Document parseInputStreamToDocument(String inputString) throws ParserConfigurationException,
            IOException, SAXException {
        try (InputStream inputStream = new ByteArrayInputStream(inputString.getBytes())) {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            return documentBuilderFactory.newDocumentBuilder().parse(inputStream);
        }
    }

    private List<File> getXsltFiles(MetadataFormat metadataFormat) throws URISyntaxException, IOException {
        List<MetadataFormatConversion> metadataConversions = MetadataFormatConversion
                .getDefaultConfigurationFileName(metadataFormat);
        List<File> xsltFiles = new LinkedList<>();
        if (Objects.nonNull(metadataConversions)) {
            URI xsltDir = Paths.get(KitodoConfig.getParameter("directory.xslt")).toUri();
            for (MetadataFormatConversion conversion : metadataConversions) {
                URI xsltFileUri = xsltDir.resolve(new URI(conversion.getFileName()));
                File xsltFile = new File(xsltFileUri);
                if (!xsltFile.exists() && Objects.nonNull(conversion.getSource())) {
                    FileUtils.copyURLToFile(conversion.getSource(), xsltFile);
                }
                xsltFiles.add(xsltFile);
            }
        }
        return xsltFiles;
    }
}
