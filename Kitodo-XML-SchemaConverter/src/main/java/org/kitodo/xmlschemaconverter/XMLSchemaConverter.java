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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UnknownFormatConversionException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.api.schemaconverter.SchemaConverterInterface;
import org.kitodo.exceptions.ConfigException;
import org.xml.sax.InputSource;

public class XMLSchemaConverter implements SchemaConverterInterface {

    /*
    Map of supported source metadata formats.
    Each value contains a list of paths to XSLT files to transform the source format to internal Kitodo format.
    The order of XSLT file paths will determine the order of execution.
     */
    private static Map<MetadataFormat, List<String>> supportedSourceMetadataFormats = new HashMap<>();

    static {
        supportedSourceMetadataFormats.put(MetadataFormat.MODS, Collections.singletonList("src/main/resources/xslt/mods2kitodo.xsl"));
        supportedSourceMetadataFormats.put(MetadataFormat.MARC,
                Arrays.asList("target/downloaded-sources/xslt/marc21slim2mods3-4.xsl", "src/main/resources/xslt/mods2kitodo.xsl"));
    }

    private static MetadataFormat supportedTargetMetadataFormat = MetadataFormat.KITODO;
    private static FileFormat supportedSourceFileFormat = FileFormat.XML;
    private static FileFormat supportedTargetFileFormat = FileFormat.XML;

    /**
     * Converts a given DataRecord to the given MetadataFormat 'targetMetadataFormat' and FileFormat 'targetFileFormat'.
     *
     * @param record DataRecord to be converted
     * @param targetMetadataFormat MetadataFormat to which the given DataRecord is converted
     * @param targetFileFormat FileFormat to which the given DataRecord is converted
     * @param mappingFile mapping file; if null, the schema converter module uses a default mapping
     * @return The result of the conversion as a DataRecord.
     */
    @Override
    public DataRecord convert(DataRecord record, MetadataFormat targetMetadataFormat, FileFormat targetFileFormat,
                              File mappingFile) throws IOException {
        if (!(supportsSourceMetadataFormat(record.getMetadataFormat())
                && supportsSourceFileFormat(record.getFileFormat())
                && supportsTargetMetadataFormat(targetMetadataFormat)
                && supportsTargetFileFormat(targetFileFormat))) {
            throw new UnknownFormatConversionException("Unable to convert from " + record.getMetadataFormat() + "/"
                    + record.getFileFormat() + " to " + targetMetadataFormat + "/" + targetFileFormat + "!");
        }

        if (record.getOriginalData() instanceof String) {
            String xmlString = (String)record.getOriginalData();
            String conversionResult;

            if (Objects.nonNull(mappingFile)) {
                try (InputStream fileStream = Files.newInputStream(mappingFile.toPath())) {
                    conversionResult = transformXmlByXslt(xmlString, fileStream);
                }
            } else {
                List<String> xslFiles = supportedSourceMetadataFormats.get(record.getMetadataFormat());
                for (String xsltFile : xslFiles) {
                    try (InputStream fileStream = Files.newInputStream(Paths.get(xsltFile))) {
                        xmlString = transformXmlByXslt(xmlString, fileStream);
                    } catch (NoSuchFileException e) {
                        try (InputStream alternativeFileStream = Files.newInputStream(Paths.get("Kitodo-XML-SchemaConverter/" + xsltFile))) {
                            xmlString = transformXmlByXslt(xmlString, alternativeFileStream);
                        }
                    }
                }
                conversionResult = xmlString;
            }

            DataRecord resultRecord = new DataRecord();
            resultRecord.setOriginalData(conversionResult);
            resultRecord.setFileFormat(targetFileFormat);
            resultRecord.setMetadataFormat(targetMetadataFormat);
            return resultRecord;
        } else {
            throw new InvalidClassException("OriginalData of DataRecord should be instance of class 'String', is '"
                    + record.getOriginalData().getClass().getName() + "' instead!");
        }
    }

    @Override
    public boolean supportsTargetMetadataFormat(MetadataFormat format) {
        return supportedTargetMetadataFormat.equals(format);
    }

    @Override
    public boolean supportsSourceMetadataFormat(MetadataFormat format) {
        return supportedSourceMetadataFormats.containsKey(format);
    }

    @Override
    public boolean supportsTargetFileFormat(FileFormat format) {
        return supportedTargetFileFormat.equals(format);
    }

    @Override
    public boolean supportsSourceFileFormat(FileFormat format) {
        return supportedSourceFileFormat.equals(format);
    }

    private String transformXmlByXslt(String xmlString, InputStream stylesheetFile) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (ParserConfigurationException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        factory.setNamespaceAware(true);
        try {
            StringWriter stringWriter = new StringWriter();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            System.setProperty("http.agent", "Chrome");
            Transformer xsltTransformer = transformerFactory.newTransformer(new StreamSource(stylesheetFile));
            TransformerHandler handler
                    = ((SAXTransformerFactory) SAXTransformerFactory.newInstance()).newTransformerHandler();
            handler.setResult(new StreamResult(stringWriter));
            Result saxResult = new SAXResult(handler);
            SAXSource saxSource = new SAXSource(new InputSource(new StringReader(xmlString)));
            xsltTransformer.transform(saxSource, saxResult);
            return stringWriter.toString();
        } catch (TransformerException e) {
            throw new ConfigException("Error in transforming the response in intern format : ", e);
        }
    }
}
