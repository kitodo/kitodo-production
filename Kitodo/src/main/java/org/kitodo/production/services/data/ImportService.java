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

package org.kitodo.production.services.data;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UnknownFormatConversionException;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.externaldatamanagement.ExternalDataImportInterface;
import org.kitodo.api.externaldatamanagement.SearchResult;
import org.kitodo.api.schemaconverter.DataRecord;
import org.kitodo.api.schemaconverter.FileFormat;
import org.kitodo.api.schemaconverter.MetadataFormat;
import org.kitodo.api.schemaconverter.SchemaConverterInterface;
import org.kitodo.config.OPACConfig;
import org.kitodo.exceptions.NoRecordFoundException;
import org.kitodo.exceptions.UnsupportedFormatException;
import org.kitodo.production.helper.XMLUtils;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.serviceloader.KitodoServiceLoader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class ImportService {

    private static final Logger logger = LogManager.getLogger(ImportService.class);

    private static volatile ImportService instance = null;
    private static ExternalDataImportInterface importModule;

    /**
     * Return singleton variable of type ImportService.
     *
     * @return unique instance of ImportService
     */
    public static ImportService getInstance() {
        ImportService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (ImportService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new ImportService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Load ExternalDataImportInterface implementation with KitodoServiceLoader and perform given query string
     * with loaded module.
     *
     * @param searchField field to query
     * @param searchTerm  given search term
     * @param catalogName catalog to search
     * @return search result
     */
    public SearchResult performSearch(String searchField, String searchTerm, String catalogName) {
        importModule = initializeImportModule();
        try {
            OPACConfig.getOPACConfiguration(catalogName);
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: OPAC '" + catalogName + "' is not supported!");
        }
        return importModule.search(catalogName, searchField, searchTerm, 10);
    }

    private ExternalDataImportInterface initializeImportModule() {
        KitodoServiceLoader<ExternalDataImportInterface> loader =
                new KitodoServiceLoader<>(ExternalDataImportInterface.class);
        return loader.loadModule();
    }

    /**
     * Load search fields of catalog with given name 'opac' from library catalog configuration file and return them as a list
     * of Strings.
     *
     * @param opac name of catalog whose search fields are loaded
     * @return list containing search fields
     */
    public List<String> getAvailableSearchFields(String opac) {
        try {
            HierarchicalConfiguration searchFields = OPACConfig.getSearchFields(opac);
            List<String> fields = new ArrayList<>();
            for (HierarchicalConfiguration searchField : searchFields.configurationsAt("searchField")) {
                fields.add(searchField.getString("[@label]"));
            }
            return fields;
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: OPAC '" + opac + "' is not supported!");
        }
    }

    /**
     * Load catalog names from library catalog configuration file and return them as a list of Strings.
     *
     * @return list of catalog names
     */
    public List<String> getAvailableCatalogs() {
        try {
            return OPACConfig.getCatalogs();
        } catch (IllegalArgumentException e) {
            logger.error(e.getLocalizedMessage());
            throw new IllegalArgumentException("Error: no supported OPACs found in configuration file!");
        }
    }

    /**
     * Get the full record with the given ID from the catalog.
     *
     * @param opac The ID of the catalog that will be queried.
     * @param identifier   The ID of the record that will be imported.
     * @return The queried record transformed into Kitodo internal format.
     */
    public Document getSelectedRecord(String opac, String identifier) throws IOException, SAXException,
            ParserConfigurationException, URISyntaxException, NoRecordFoundException, UnsupportedFormatException {

        // ################ IMPORT #################
        importModule = initializeImportModule();
        DataRecord dataRecord = importModule.getFullRecordById(opac, identifier);

        // ################# CONVERT ################
        // depending on metadata and return form, call corresponding schema converter module!
        SchemaConverterInterface converter = getSchemaConverter(dataRecord);

        // transform dataRecord to Kitodo internal format using appropriate SchemaConverter!
        DataRecord resultRecord = converter.convert(dataRecord, MetadataFormat.KITODO, FileFormat.XML,
                ServiceManager.getFileService().getFile(new URI(OPACConfig.getXsltMappingFile(opac))));

        if (resultRecord.getOriginalData() instanceof String) {
            return XMLUtils.parseXMLString((String) resultRecord.getOriginalData());
        } else {
            throw new UnknownFormatConversionException("Result data is not a String!");
        }
    }

    /**
     * Iterate over "SchemaConverterInterface" implementations using KitodoServiceLoader and return
     * first implementation that supports the Metadata and File formats of the given DataRecord object
     * as source formats and the Kitodo internal format and XML as target formats, respectively.
     *
     * @param record
     *      Record whose metadata and return formats are used to filter the SchemaConverterInterface implementations
     *
     * @return List of SchemaConverterInterface implementations that support the metadata and return formats of the
     *      given Record.
     *
     * @throws UnsupportedFormatException when no SchemaConverter module with matching formats could be found
     */
    private SchemaConverterInterface getSchemaConverter(DataRecord record) throws UnsupportedFormatException {
        KitodoServiceLoader<SchemaConverterInterface> loader =
                new KitodoServiceLoader<>(SchemaConverterInterface.class);
        List<SchemaConverterInterface> converterModules = loader.loadModules().stream()
                .filter(c -> c.supportsSourceMetadataFormat(record.getMetadataFormat())
                        && c.supportsSourceFileFormat(record.getFileFormat())
                        && c.supportsTargetMetadataFormat(MetadataFormat.KITODO)
                        && c.supportsTargetFileFormat(FileFormat.XML))
                .collect(Collectors.toList());
        if (converterModules.isEmpty()) {
            throw new UnsupportedFormatException("No SchemaConverter found that supports '"
                    + record.getMetadataFormat() + "' and '" + record.getFileFormat() + "'!");
        }
        return converterModules.get(0);
    }
}
