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

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.exceptions.ImportException;
import org.kitodo.production.forms.CsvCell;
import org.kitodo.production.forms.CsvRecord;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.primefaces.model.file.UploadedFile;

public class MassImportService {

    private static MassImportService instance = null;

    /**
     * Return singleton variable of type MassImportService.
     *
     * @return unique instance of MassImportService
     */
    public static MassImportService getInstance() {
        MassImportService localReference = instance;
        if (Objects.isNull(localReference)) {
            synchronized (MassImportService.class) {
                localReference = instance;
                if (Objects.isNull(localReference)) {
                    localReference = new MassImportService();
                    instance = localReference;
                }
            }
        }
        return localReference;
    }

    /**
     * Read and return lines from UploadedFile 'file'.
     * @param file UploadedFile for mass import
     * @return list of lines from UploadedFile 'file'
     * @throws IOException thrown if InputStream cannot be read from provided UploadedFile 'file'.
     */
    public List<String> getLines(UploadedFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            return reader.lines().collect(Collectors.toList());
        }
    }

    /**
     * Split provided lines by given 'separator'-String and return list of CsvRecord.
     * The method also handles quoted csv values, which contain comma or semicolon to allow
     * csv separators in csv cells.
     * @param lines lines to parse
     * @param separator String used to split lines into individual parts
     * @return list of CsvRecord
     */
    public List<CsvRecord> parseLines(List<String> lines, String separator) throws IOException, CsvValidationException {
        List<CsvRecord> records = new LinkedList<>();
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(separator.charAt(0))
                .withQuoteChar('\"')
                .build();
        for (String line : lines) {
            if (!Objects.isNull(line) && !line.isBlank()) {
                List<CsvCell> cells = new LinkedList<>();
                CSVReader csvReader = new CSVReaderBuilder(new StringReader(line))
                        .withSkipLines(0)
                        .withCSVParser(parser)
                        .build();
                String[] values = csvReader.readNext();
                if (!Objects.isNull(values)) {
                    for (String value : values) {
                        cells.add(new CsvCell(value));
                    }
                    records.add(new CsvRecord(cells));
                }
            }
        }
        return records;
    }

    /**
     * Import records for given rows, containing IDs for individual download and optionally additional metadata
     * to be added to each record.
     * @param metadataKeys metadata keys for additional metadata added to individual records during import
     * @param records list of CSV records
     */
    public Map<String, Map<String, List<String>>> prepareMetadata(List<String> metadataKeys, List<CsvRecord> records)
            throws ImportException {
        Map<String, Map<String, List<String>>> presetMetadata = new LinkedHashMap<>();
        for (CsvRecord record : records) {
            Map<String, List<String>> processMetadata = new HashMap<>();
            // skip first metadata key as it always contains the record ID to be used for search
            for (int index = 1; index < metadataKeys.size(); index++) {
                String metadataKey = metadataKeys.get(index);
                if (StringUtils.isNotBlank(metadataKey)) {
                    List<String> values = processMetadata.computeIfAbsent(metadataKey, k -> new ArrayList<>());
                    values.add(record.getCsvCells().get(index).getValue());
                }
            }
            presetMetadata.put(record.getCsvCells().get(0).getValue(), processMetadata);
        }
        return presetMetadata;
    }

    /**
     * Get all allowed metadata.
     * @param divisions list of StructuralElementViewInterface
     * @param enteredMetadata collection of preset metadata
     * @return list of allowed metadata as List of ProcessDetail
     */
    public List<ProcessDetail> getAddableMetadataTable(List<StructuralElementViewInterface> divisions,
                                                       Collection<Metadata> enteredMetadata) {
        ProcessFieldedMetadata table = new ProcessFieldedMetadata();
        List<MetadataViewInterface> commonMetadata = new ArrayList<>();
        for (int i = 0; i < divisions.size(); i++) {
            List<MetadataViewInterface> metadataView =
                    divisions.get(i).getAddableMetadata(enteredMetadata, Collections.emptyList())
                            .stream().sorted(Comparator.comparing(MetadataViewInterface::getLabel))
                            .collect(Collectors.toList());
            if (i == 0) {
                commonMetadata = new ArrayList<>(List.copyOf(metadataView));
            } else {
                commonMetadata.removeIf(item -> metadataView.stream()
                        .noneMatch(metadataElement -> Objects.equals(item.getId(), metadataElement.getId())));
            }
            if (commonMetadata.isEmpty()) {
                break;
            }
        }
        for (MetadataViewInterface keyView : commonMetadata) {
            if (!keyView.isComplex()) {
                table.createMetadataEntryEdit((SimpleMetadataViewInterface) keyView, Collections.emptyList());
            }
        }
        return table.getRows();
    }
}
