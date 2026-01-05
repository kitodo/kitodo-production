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


package org.kitodo.production.services.catalogimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.kitodo.exceptions.ImportException;
import org.kitodo.exceptions.KitodoCsvImportException;
import org.kitodo.production.enums.SeparatorCharacter;
import org.kitodo.production.forms.CsvCell;
import org.kitodo.production.forms.CsvRecord;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.MassImportService;

public class MassImportTest {

    private static final String ID = "ID";
    private static final String TITLE = "Title";
    private static final String PLACE = "Place";
    private static final List<String> METADATA_KEYS  = Arrays.asList(ID, TITLE, PLACE);
    private static final String CSV_FIRST_LINE = "123, Band 1, Hamburg";
    private static final String CSV_FIRST_LINE_WITH_COMMA = "123, \"Band 1,2\", Hamburg";
    private static final String CSV_SECOND_LINE = "456, Band 2, Dresden";
    private static final String CSV_THIRD_LINE = "789, Band 3, Berlin";
    private static final List<String> CSV_LINES = Arrays.asList(CSV_FIRST_LINE, CSV_SECOND_LINE, CSV_THIRD_LINE);
    private static final List<String> CSV_LINES_WITH_COMMA = Arrays.asList(CSV_FIRST_LINE_WITH_COMMA, CSV_SECOND_LINE, CSV_THIRD_LINE);
    private static final List<String> METADATA_KEYS_MUTLIPLE_VALUES  = Arrays.asList(ID, TITLE, PLACE, PLACE);
    private static final String CSV_FIRST_LINE_MULTIPLE_VALUES  = "321, Band 1, Hamburg, Berlin";
    private static final String CSV_FIRST_LINE_MULTIPLE_VALUES_WITH_COMMA  = "978, Band 1, Hamburg, \"Berlin, Kopenhagen\"";
    private static final String CSV_SECOND_LINE_MULTIPLE_VALUES  = "654, Band 2, Dresden, Hannover";
    private static final List<String> CSV_LINES_MULTIPLE_VALUES = Arrays.asList(CSV_FIRST_LINE_MULTIPLE_VALUES,
            CSV_SECOND_LINE_MULTIPLE_VALUES);
    private static final List<String> CSV_LINES_MULTIPLE_VALUES_WITH_COMMA = Arrays.asList(CSV_FIRST_LINE_MULTIPLE_VALUES_WITH_COMMA,
            CSV_SECOND_LINE_MULTIPLE_VALUES);

    /**
     * Tests parsing CSV lines into CSV records with multiple cells.
     */
    @Test
    public void shouldParseLines() throws IOException, CsvException, KitodoCsvImportException {
        MassImportService service = ServiceManager.getMassImportService();
        // test parsing CSV lines with correct delimiter
        List<CsvRecord> csvRecords = service.parseLines(CSV_LINES, SeparatorCharacter.COMMA.getSeparator());
        assertEquals(3, csvRecords.size(), "Wrong number of CSV records");
        List<CsvCell> cells = csvRecords.getFirst().getCsvCells();
        assertEquals(3, cells.size(), "Wrong number of cells in first CSV record");
        assertEquals("123", cells.get(0).getValue(), "Wrong value in first cell of first CSV record");
        assertEquals("Band 1", cells.get(1).getValue(), "Wrong value in second cell of first CSV record");
        assertEquals("Hamburg", cells.get(2).getValue(), "Wrong value in third cell of first CSV record");
        cells = csvRecords.get(1).getCsvCells();
        assertEquals(3, cells.size(), "Wrong number of cells in second CSV record");
        assertEquals("456", cells.get(0).getValue(), "Wrong value in first cell of second CSV record");
        assertEquals("Band 2", cells.get(1).getValue(), "Wrong value in second cell of second CSV record");
        assertEquals("Dresden", cells.get(2).getValue(), "Wrong value in third cell of second CSV record");
        cells = csvRecords.get(2).getCsvCells();
        assertEquals(3, cells.size(), "Wrong number of cells in second CSV record");
        assertEquals("789", cells.get(0).getValue(), "Wrong value in first cell of third CSV record");
        assertEquals("Band 3", cells.get(1).getValue(), "Wrong value in second cell of third CSV record");
        assertEquals("Berlin", cells.get(2).getValue(), "Wrong value in third cell of third CSV record");
        // test parsing CSV lines with incorrect delimiter
        csvRecords = service.parseLines(CSV_LINES, SeparatorCharacter.SEMICOLON.getSeparator());
        cells = csvRecords.get(0).getCsvCells();
        assertEquals(1, cells.size(), "Wrong number of cells in first CSV record");
        cells = csvRecords.get(1).getCsvCells();
        assertEquals(1, cells.size(), "Wrong number of cells in second CSV record");
        cells = csvRecords.get(2).getCsvCells();
        assertEquals(1, cells.size(), "Wrong number of cells in third CSV record");
    }

    /**
     * Tests parsing CSV lines with comma in values into CSV records with multiple cells.
     */
    @Test
    public void shouldParseLinesWithCommaInValues() throws IOException, CsvException, KitodoCsvImportException {
        MassImportService service = ServiceManager.getMassImportService();
        // test parsing CSV lines with correct delimiter
        List<CsvRecord> csvRecords = service.parseLines(CSV_LINES_WITH_COMMA, SeparatorCharacter.COMMA.getSeparator());
        List<CsvCell> cells = csvRecords.getFirst().getCsvCells();
        assertEquals(3, csvRecords.size(), "Wrong number of CSV records");
        assertEquals(3, cells.size(), "Wrong number of cells in first CSV record");
        assertEquals("123", cells.get(0).getValue(), "Wrong value in first cell of first CSV record");
        assertEquals("Band 1,2", cells.get(1).getValue(), "Wrong value in second cell of first CSV record");
        assertEquals("Hamburg", cells.get(2).getValue(), "Wrong value in third cell of first CSV record");
        csvRecords = service.parseLines(CSV_LINES, SeparatorCharacter.SEMICOLON.getSeparator());
        cells = csvRecords.getFirst().getCsvCells();
        assertEquals(1, cells.size(), "Wrong number of cells in first CSV record");
    }


    /**
     * Tests whether updating CSV separator character from incorrect character to correct character keeps number of
     * CSV records, but changes number of cells per record.
     */
    @Test
    public void shouldParseCorrectlyAfterSeparatorUpdate() throws IOException, CsvException, KitodoCsvImportException {
        MassImportService service = ServiceManager.getMassImportService();
        List<CsvRecord> oldCsvRecords = service.parseLines(CSV_LINES, SeparatorCharacter.SEMICOLON.getSeparator());
        assertTrue(oldCsvRecords.stream().noneMatch(csvRecord -> csvRecord.getCsvCells().size() > 1),
                "CSV lines should not be separated into multiple records when using incorrect separator character");
        List<CsvRecord> newCsvRecords = service.parseLines(CSV_LINES, SeparatorCharacter.COMMA.getSeparator());
        assertEquals(oldCsvRecords.size(), newCsvRecords.size(),
                "Updating separator character should not alter number of CSV records");
        assertTrue(newCsvRecords.stream().allMatch(csvRecord -> csvRecord.getCsvCells().size() > 1),
                "CSV lines be separated into multiple records when using correct separator character");
        List<CsvRecord> newCsvRecordsWithComma = service.parseLines(CSV_LINES_WITH_COMMA, SeparatorCharacter.COMMA.getSeparator());
        assertEquals(oldCsvRecords.size(), newCsvRecordsWithComma.size(),
                "Updating separator character should not alter number of CSV records");
        assertTrue(newCsvRecordsWithComma.stream().allMatch(csvRecord -> csvRecord.getCsvCells().size() > 1),
                "CSV lines be separated into multiple records when using correct separator character");
    }

    /**
     * Tests whether parsing data entered in mass import form succeeds or not.
     */
    @Test
    public void shouldPrepareMetadata() throws ImportException, IOException, CsvException, KitodoCsvImportException {
        MassImportService service = ServiceManager.getMassImportService();
        List<CsvRecord> csvRecords = service.parseLines(CSV_LINES, SeparatorCharacter.COMMA.getSeparator());
        LinkedList<LinkedHashMap<String, List<String>>> metadata = service.prepareMetadata(METADATA_KEYS, csvRecords);
        assertEquals(3, metadata.size(), "Wrong number of metadata sets prepared");
        Map<String, List<String>> metadataSet = metadata.getFirst();
        assertNotNull(metadataSet, "Metadata for record is null");
        assertEquals(3, metadataSet.size(), "Wrong number of metadata sets prepared");
        assertEquals("Band 1", metadataSet.get(TITLE).getFirst(), "Metadata for record with ID 123 contains wrong title");
        assertEquals(1, metadataSet.get(PLACE).size(), "Metadata for record with ID 123 has wrong size of place list");
        assertEquals("Hamburg", metadataSet.get(PLACE).getFirst(), "Metadata for record with ID 123 contains wrong place");

        List<CsvRecord> csvRecordsMultipleValues = service.parseLines(CSV_LINES_MULTIPLE_VALUES,
                SeparatorCharacter.COMMA.getSeparator());
        LinkedList<LinkedHashMap<String, List<String>>> metadataMultipleValues = service.prepareMetadata(METADATA_KEYS_MUTLIPLE_VALUES, csvRecordsMultipleValues);
        Map<String, List<String>> metadataSetMultipleValues = metadataMultipleValues.getFirst();
        assertNotNull(metadataSetMultipleValues, "Metadata for record is null");
        assertEquals(3, metadataSetMultipleValues.size(), "Wrong number of metadata sets prepared");
        assertTrue(metadataSetMultipleValues.containsKey(PLACE), "Metadata for record with ID 321 does not contain place metadata");
        assertEquals(2, metadataSetMultipleValues.get(PLACE).size(), "Metadata for record with ID 123 has wrong size of place list");
        assertEquals("Hamburg", metadataSetMultipleValues.get(PLACE).get(0), "Metadata for record with ID 321 contains wrong place");
        assertEquals("Berlin", metadataSetMultipleValues.get(PLACE).get(1), "Metadata for record with ID 321 contains wrong place");

        List<CsvRecord> csvRecordsMultipleValuesWithComma = service.parseLines(CSV_LINES_MULTIPLE_VALUES_WITH_COMMA,
                SeparatorCharacter.COMMA.getSeparator());
        LinkedList<LinkedHashMap<String, List<String>>> metadataMultipleValuesWithComma =
                service.prepareMetadata(METADATA_KEYS_MUTLIPLE_VALUES, csvRecordsMultipleValuesWithComma);

        Map<String, List<String>> metadataSetMultipleValuesWithComma = metadataMultipleValuesWithComma.getFirst();
        assertNotNull(metadataSetMultipleValuesWithComma, "Metadata for record is null");
        assertEquals(3, metadataSetMultipleValuesWithComma.size(), "Wrong number of metadata sets prepared");
        assertTrue(metadataSetMultipleValuesWithComma.containsKey(PLACE),
                "Metadata for record with ID 978 does not contain place metadata");
        assertEquals(2, metadataSetMultipleValuesWithComma.get(PLACE).size(),
                "Metadata for record with ID 978 has wrong size of place list");
        assertEquals("Hamburg", metadataSetMultipleValuesWithComma.get(PLACE).get(0),
                "Metadata for record with ID 978 contains wrong place");
        assertEquals("Berlin, Kopenhagen", metadataSetMultipleValuesWithComma.get(PLACE).get(1),
                "Metadata for record with ID 978 contains wrong place");
    }
}
