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
import java.io.InvalidClassException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.UnknownFormatConversionException;

import javax.xml.XMLConstants;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;

import org.apache.commons.io.ByteOrderMark;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.api.schemaconverter.SchemaConverterInterface;
import org.kitodo.exceptions.ConfigException;

public class XMLSchemaConverter implements SchemaConverterInterface {
    private static final FileFormat supportedSourceFileFormat = FileFormat.XML;
    private static final FileFormat supportedTargetFileFormat = FileFormat.XML;

    /**
     * Converts a given DataRecord to the given MetadataFormat 'targetMetadataFormat' and FileFormat 'targetFileFormat'.
     *
     * @param record DataRecord to be converted
     * @param targetFileFormat FileFormat to which the given DataRecord is converted
     * @param mappingFiles list of mapping files; if empty, the schema converter module uses a default mapping
     * @return The result of the conversion as a DataRecord.
     */
    @Override
    public DataRecord convert(DataRecord record, MetadataFormat targetMetadataFormat, FileFormat targetFileFormat,
                              List<File> mappingFiles) throws IOException {
        if (!(supportsSourceFileFormat(record.getFileFormat()) && supportsTargetFileFormat(targetFileFormat))) {
            throw new UnknownFormatConversionException("Unable to convert from " + record.getFileFormat()
                    + " to " + targetFileFormat + "!");
        }

        if (record.getOriginalData() instanceof String) {
            String xmlString = (String)record.getOriginalData();
            String conversionResult;

            if (mappingFiles.isEmpty()) {
                throw new ConfigException("No mapping files found!");
            } else {
                for (File mappingFile : mappingFiles) {
                    xmlString = transformXmlByXslt(xmlString, mappingFile);
                }
            }
            conversionResult = xmlString;

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
    public boolean supportsTargetFileFormat(FileFormat format) {
        return supportedTargetFileFormat.equals(format);
    }

    @Override
    public boolean supportsSourceFileFormat(FileFormat format) {
        return supportedSourceFileFormat.equals(format);
    }

    private String transformXmlByXslt(String xmlString, File stylesheetFile) {
        try (StringWriter writer = new StringWriter()) {
            StreamSource xsltSource = new StreamSource(stylesheetFile);
            xsltSource.setSystemId(stylesheetFile.toURI().toString());
            TransformerFactory transformerFactory = new TransformerFactoryImpl();
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            transformerFactory.setURIResolver((href, base) -> new StreamSource(href.replace("http:", "https:")));
            System.setProperty("http.agent", "Chrome");
            Transformer transformer = transformerFactory.newTransformer(xsltSource);
            xmlString = removeBom(xmlString);
            transformer.transform(new StreamSource(new StringReader(xmlString)), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException | IOException e) {
            throw new ConfigException("Error in transforming the response to internal format: " + e.getMessage(), e);
        }
    }

    /**
     * Remove potential BOM character because XML parser do not handle it properly.
     * @param xmlStringWithBom String with potential BOM character
     * @return xml String without BOM character
     */
    private String removeBom(String xmlStringWithBom) {
        if (Objects.equals(xmlStringWithBom.charAt(0), ByteOrderMark.UTF_BOM)) {
            return xmlStringWithBom.substring(1);
        }
        return xmlStringWithBom;
    }
}
