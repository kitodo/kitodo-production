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
import com.opencsv.exceptions.CsvException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.kitodo.api.Metadata;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.RulesetManagementInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.SimpleMetadataViewInterface;
import org.kitodo.api.dataeditor.rulesetmanagement.StructuralElementViewInterface;
import org.kitodo.exceptions.ImportException;
import org.kitodo.exceptions.KitodoCsvImportException;
import org.kitodo.production.enums.SeparatorCharacter;
import org.kitodo.production.forms.CsvCell;
import org.kitodo.production.forms.CsvRecord;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessFieldedMetadata;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.primefaces.model.file.UploadedFile;

public class MassImportService {

    private static MassImportService instance = null;
    private final SeparatorCharacter[] csvSeparatorCharacters = SeparatorCharacter.values();

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
     * @param separator Character used to split lines into individual parts
     * @return list of CsvRecord
     */
    public List<CsvRecord> parseLines(List<String> lines, String separator) throws IOException, CsvException,
            KitodoCsvImportException {
        List<CsvRecord> records = new LinkedList<>();
        CSVParser parser = new CSVParserBuilder()
                .withSeparator(separator.charAt(0))
                .withQuoteChar('\"')
                .build();
        try (StringReader reader = new StringReader(String.join("\n", lines));
             CSVReader csvReader = new CSVReaderBuilder(reader)
                     .withSkipLines(0)
                     .withCSVParser(parser)
                     .build()) {
            int targetNumberOfColumns = -1;
            for (String[] entries : csvReader.readAll()) {
                if (isSingleEmptyEntry(entries) || isLineEmpty(entries)) {
                    continue; // Skip processing this line
                }
                // throw exception if different number of metadata entries were parsed for different CSV lines
                if (targetNumberOfColumns >= 0 && entries.length != targetNumberOfColumns) {
                    throw new KitodoCsvImportException(Helper.getTranslation("massImport.separatorCountMismatchEntries",
                            Pattern.quote(separator)));
                }
                if (targetNumberOfColumns < 0) {
                    targetNumberOfColumns = entries.length;
                }
                List<CsvCell> cells = new LinkedList<>();
                for (String value : entries) {
                    cells.add(new CsvCell(value.trim()));
                }
                records.add(new CsvRecord(cells));
            }
        }
        return records;
    }

    /**
     * Determines the indices of columns in a CSV file that can be skipped based on their metadata completeness.
     * A column is skipped if all associated metadata values for that column are blank or null across all records.
     *
     * @param records list of CsvRecord objects representing rows of a CSV file
     * @param numberOfMetadataKeys the number of metadata keys representing columns in the CSV file
     * @return a list of integers representing the indices of columns that can be skipped
     * @throws RuntimeException if the size of csvCells in any CsvRecord does not match the number of metadata keys
     */
    public List<Integer> getColumnSkipIndices(List<CsvRecord> records, int numberOfMetadataKeys) {
        List<Integer> skipIndices = IntStream.range(0, numberOfMetadataKeys).boxed().collect(Collectors.toList());
        for (int index = numberOfMetadataKeys - 1; index >= 0; index--) {
            for (CsvRecord csvRecord : records) {
                if (Objects.nonNull(csvRecord.getCsvCells()) && csvRecord.getCsvCells().size() != numberOfMetadataKeys) {
                    throw new RuntimeException(Helper.getTranslation("massImport.csvCellMismatch"));
                }
                // if at least one record has a non-null value in the column with the current index,
                // remove the column's index from the list of indices to be skipped
                if (StringUtils.isNotBlank(csvRecord.getCsvCells().get(index).getValue())) {
                    skipIndices.remove(index);
                    break;
                }
            }
        }
        return skipIndices;
    }

    /**
     * Removes metadata keys from the provided list based on the specified indices of empty columns.
     * The method iterates over the list of indices in reverse order to preserve the integrity of the column indices
     * while removing the associated metadata keys.
     *
     * @param metadataKeys the list of metadata keys representing the columns in a CSV file
     * @param skipIndices the list of column indices that correspond to empty columns that need to be removed
     * @return the modified list of metadata keys after removing entries for the specified empty column indices
     * @throws RuntimeException if any index in skipIndices is greater than the size of the metadataKeys list
     */
    public List<String> discardMetadataKeysOfEmptyColumns(List<String> metadataKeys, List<Integer> skipIndices) {
        if (Collections.max(skipIndices) > metadataKeys.size()) {
            throw new RuntimeException(Helper.getTranslation("massImport.metadataKeysMismatch"));
        }
        // iterate over "skipIndices" in reverse order to preserve column indices in "metadataKeys" list
        for (int index = skipIndices.size() - 1; index >= 0; index--) {
            metadataKeys.remove(skipIndices.get(index).intValue());
        }
        return metadataKeys;
    }

    /**
     * Removes columns from the provided list of CSV records based on specified indices.
     * This method modifies the CSV records by discarding the cells at the given column indices.
     *
     * @param csvRecords the list of {@link CsvRecord} objects representing rows of a CSV file
     * @param skipIndices a list of integers representing the indices of columns to be removed
     * @return the modified list of {@link CsvRecord} objects with specified columns removed
     * @throws RuntimeException if any index in skipIndices is greater than the number of columns in the first record
     */
    public List<CsvRecord> discardEmptyColumns(List<CsvRecord> csvRecords, List<Integer> skipIndices) {
        if (!csvRecords.isEmpty() && Collections.max(skipIndices) > csvRecords.getFirst().getCsvCells().size()) {
            throw new RuntimeException(Helper.getTranslation("massImport.csvCellMismatch"));
        }
        // iterate over "skipIndices" in reverse order to preserve column indices in "metadataKeys" list
        for (int index = skipIndices.size() - 1; index >= 0; index--) {
            int currentSkipIndex = skipIndices.get(index);
            // discard ith cell in all csv records
            for (CsvRecord csvRecord : csvRecords) {
                if (currentSkipIndex < csvRecord.getCsvCells().size()) {
                    csvRecord.getCsvCells().remove(currentSkipIndex);
                }
            }
        }
        return csvRecords;
    }

    // Helper method to check if a line has a single empty entry
    private boolean isSingleEmptyEntry(String[] entries) {
        return entries.length == 1 && entries[0].isEmpty();
    }

    // Helper method to check if line is completely empty
    private boolean isLineEmpty(String[] entries) {
        return entries.length == 0
                || Arrays.stream(entries).allMatch(StringUtils::isBlank);
    }

    /**
     * Import records for given rows, containing IDs for individual download and optionally additional metadata
     * to be added to each record.
     * @param metadataKeys metadata keys for additional metadata added to individual records during import
     * @param records list of CSV records
     */
    public LinkedList<LinkedHashMap<String, List<String>>> prepareMetadata(List<String> metadataKeys, List<CsvRecord> records)
            throws ImportException {
        LinkedList<LinkedHashMap<String, List<String>>> presetMetadata = new LinkedList<>();
        for (CsvRecord record : records) {
            LinkedHashMap<String, List<String>> processMetadata = new LinkedHashMap<>();
            for (int index = 0; index < metadataKeys.size(); index++) {
                String metadataKey = metadataKeys.get(index);
                String metadataValue = record.getCsvCells().get(index).getValue();
                if (StringUtils.isNotBlank(metadataKey) && StringUtils.isNotBlank(metadataValue)) {
                    List<String> values = processMetadata.computeIfAbsent(metadataKey, k -> new ArrayList<>());
                    values.add(metadataValue);
                }
            }
            presetMetadata.add(processMetadata);
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

    /**
     * This method takes a list of lines from a CSV file and tries to guess separator character used in this file
     * (e.g. comma or semicolon). To achieve this, the method gathers the number of occurrences of each candidate in all
     * lines and selects the character with the highest number of lines containing one specific count of that character.
     * character
     * @param csvLines lines from CSV file
     * @return character that is determined to be most likely used as separator character in uploaded CSV file
     */
    public SeparatorCharacter guessCsvSeparator(List<String> csvLines) {
        Map<String, Map<Integer, Integer>> separatorOccurrences = new HashMap<>();
        for (SeparatorCharacter separator : SeparatorCharacter.values()) {
            Map<Integer, Integer> currentOccurrences = new HashMap<>();
            for (String line : csvLines) {
                int occurrences = StringUtils.countMatches(line, separator.getSeparator());
                if (currentOccurrences.containsKey(occurrences)) {
                    currentOccurrences.put(occurrences, currentOccurrences.get(occurrences) + 1);
                } else {
                    currentOccurrences.put(occurrences, 1);
                }
            }
            separatorOccurrences.put(separator.getSeparator(), currentOccurrences);
        }
        SeparatorCharacter probablyCharacter = csvSeparatorCharacters[0];
        int maxOccurrence = 0;
        int maxKey = 0;
        for (Map.Entry<String, Map<Integer, Integer>> characterStatistics : separatorOccurrences.entrySet()) {
            Optional<Map.Entry<Integer, Integer>> highestOccurrence = characterStatistics.getValue().entrySet()
                    .stream().max(Map.Entry.comparingByValue());
            if (highestOccurrence.isPresent()) {
                Map.Entry<Integer, Integer> occurrence = highestOccurrence.get();
                // skip count of lines that did not contain current separator, e.g. occurrences are "0", which would
                // otherwise be the most common count in the statistic for each unused separator character!
                if (occurrence.getKey() > 0 && occurrence.getValue() > maxOccurrence && occurrence.getKey() > maxKey) {
                    probablyCharacter = SeparatorCharacter.getByCharacter(characterStatistics.getKey());
                    maxKey = occurrence.getKey();
                }
            }
        }
        return probablyCharacter;
    }

    public SeparatorCharacter[] getCsvSeparatorCharacters() {
        return csvSeparatorCharacters;
    }

    /**
     * Check and return whether given list of strings contains the key of a metadata configured as functional metadata
     * 'recordIdentifier' in given RulesetManagementInterface 'ruleset'.
     *
     * @param metadataKeys list of strings representing metadata keys
     * @param ruleset RulesetManagementInterface defining metadata
     * @return 'true' if at least one string in the given list is the key of a functional metadata of type 'recordIdentifier'
     *         'false' otherwise
     */
    public static boolean metadataKeyListContainsRecordIdentifier(List<String> metadataKeys, RulesetManagementInterface ruleset) {
        if (metadataKeys.isEmpty()) {
            return false;
        } else {
            for (String metadataKey : metadataKeys) {
                if (ServiceManager.getImportService().isRecordIdentifierMetadata(ruleset, metadataKey)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get String containing label of 'recordIdentifier' functional metadata from given ruleset.
     *
     * @param metadataKeys list of strings representing metadata keys
     * @param ruleset RulesetManagementInterface defining metadata
     * @return localized label of 'recordIdentifier' functional metadata
     * @throws IOException if ruleset could not be opened
     */
    public static String getRecordIdentifierMetadataLabel(List<String> metadataKeys, RulesetManagementInterface ruleset)
            throws IOException {
        if (metadataKeys.isEmpty()) {
            return "";
        } else {
            for (String metadataKey : metadataKeys) {
                if (ServiceManager.getImportService().isRecordIdentifierMetadata(ruleset, metadataKey)) {
                    return ServiceManager.getRulesetService().getMetadataTranslation(ruleset, metadataKey, null);
                }
            }
        }
        return "";
    }

}
