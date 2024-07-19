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

import com.opencsv.exceptions.CsvException;
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.constants.StringConstants;
import org.kitodo.exceptions.ImportException;
import org.kitodo.production.forms.CsvCell;
import org.kitodo.production.forms.CsvRecord;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.MassImportService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    public void shouldParseLines() throws IOException, CsvException {
        MassImportService service = ServiceManager.getMassImportService();
        // test parsing CSV lines with correct delimiter
        List<CsvRecord> csvRecords = service.parseLines(CSV_LINES, StringConstants.COMMA_DELIMITER);
        Assert.assertEquals("Wrong number of CSV records", 3, csvRecords.size());
        List<CsvCell> cells = csvRecords.get(0).getCsvCells();
        Assert.assertEquals("Wrong number of cells in first CSV record", 3, cells.size());
        Assert.assertEquals("Wrong value in first cell of first CSV record", "123", cells.get(0).getValue());
        Assert.assertEquals("Wrong value in second cell of first CSV record", "Band 1", cells.get(1).getValue());
        Assert.assertEquals("Wrong value in third cell of first CSV record", "Hamburg", cells.get(2).getValue());
        cells = csvRecords.get(1).getCsvCells();
        Assert.assertEquals("Wrong number of cells in second CSV record", 3, cells.size());
        Assert.assertEquals("Wrong value in first cell of second CSV record", "456", cells.get(0).getValue());
        Assert.assertEquals("Wrong value in second cell of second CSV record", "Band 2", cells.get(1).getValue());
        Assert.assertEquals("Wrong value in third cell of second CSV record", "Dresden", cells.get(2).getValue());
        cells = csvRecords.get(2).getCsvCells();
        Assert.assertEquals("Wrong number of cells in second CSV record", 3, cells.size());
        Assert.assertEquals("Wrong value in first cell of third CSV record", "789", cells.get(0).getValue());
        Assert.assertEquals("Wrong value in second cell of third CSV record", "Band 3", cells.get(1).getValue());
        Assert.assertEquals("Wrong value in third cell of third CSV record", "Berlin", cells.get(2).getValue());
        // test parsing CSV lines with incorrect delimiter
        csvRecords = service.parseLines(CSV_LINES, ";");
        cells = csvRecords.get(0).getCsvCells();
        Assert.assertEquals("Wrong number of cells in first CSV record", 1, cells.size());
        cells = csvRecords.get(1).getCsvCells();
        Assert.assertEquals("Wrong number of cells in second CSV record", 1, cells.size());
        cells = csvRecords.get(2).getCsvCells();
        Assert.assertEquals("Wrong number of cells in third CSV record", 1, cells.size());
    }

    /**
     * Tests parsing CSV lines with comma in values into CSV records with multiple cells.
     */
    @Test
    public void shouldParseLinesWithCommaInValues() throws IOException, CsvException {
        MassImportService service = ServiceManager.getMassImportService();
        // test parsing CSV lines with correct delimiter
        List<CsvRecord> csvRecords = service.parseLines(CSV_LINES_WITH_COMMA, StringConstants.COMMA_DELIMITER);
        List<CsvCell> cells = csvRecords.get(0).getCsvCells();
        Assert.assertEquals("Wrong number of CSV records", 3, csvRecords.size());
        Assert.assertEquals("Wrong number of cells in first CSV record", 3, cells.size());
        Assert.assertEquals("Wrong value in first cell of first CSV record", "123", cells.get(0).getValue());
        Assert.assertEquals("Wrong value in second cell of first CSV record", "Band 1,2", cells.get(1).getValue());
        Assert.assertEquals("Wrong value in third cell of first CSV record", "Hamburg", cells.get(2).getValue());
        csvRecords = service.parseLines(CSV_LINES, ";");
        cells = csvRecords.get(0).getCsvCells();
        Assert.assertEquals("Wrong number of cells in first CSV record", 1, cells.size());
    }


    /**
     * Tests whether updating CSV separator character from incorrect character to correct character keeps number of
     * CSV records, but changes number of cells per record.
     */
    @Test
    public void shouldParseCorrectlyAfterSeparatorUpdate() throws IOException, CsvException {
        MassImportService service = ServiceManager.getMassImportService();
        List<CsvRecord> oldCsvRecords = service.parseLines(CSV_LINES, StringConstants.SEMICOLON_DELIMITER);
        Assert.assertTrue("CSV lines should not be separated into multiple records when using incorrect separator character",
                oldCsvRecords.stream().noneMatch(csvRecord -> csvRecord.getCsvCells().size() > 1));
        List<CsvRecord> newCsvRecords = service.parseLines(CSV_LINES, StringConstants.COMMA_DELIMITER);
        Assert.assertEquals("Updating separator character should not alter number of CSV records", oldCsvRecords.size(),
                newCsvRecords.size());
        Assert.assertTrue("CSV lines be separated into multiple records when using correct separator character",
                newCsvRecords.stream().allMatch(csvRecord -> csvRecord.getCsvCells().size() > 1));
        List<CsvRecord> newCsvRecordsWithComma = service.parseLines(CSV_LINES_WITH_COMMA, StringConstants.COMMA_DELIMITER);
        Assert.assertEquals("Updating separator character should not alter number of CSV records", oldCsvRecords.size(),
                newCsvRecordsWithComma.size());
        Assert.assertTrue("CSV lines be separated into multiple records when using correct separator character",
                newCsvRecordsWithComma.stream().allMatch(csvRecord -> csvRecord.getCsvCells().size() > 1));
    }

    /**
     * Tests whether parsing data entered in mass import form succeeds or not.
     */
    @Test
    public void shouldPrepareMetadata() throws ImportException, IOException, CsvException {
        MassImportService service = ServiceManager.getMassImportService();
        List<CsvRecord> csvRecords = service.parseLines(CSV_LINES, StringConstants.COMMA_DELIMITER);
        Map<String, Map<String, List<String>>> metadata = service.prepareMetadata(METADATA_KEYS, csvRecords);
        Assert.assertEquals("Wrong number of metadata sets prepared", 3, metadata.size());
        Map<String, List<String>> metadataSet = metadata.get("123");
        Assert.assertNotNull("Metadata for record with ID 123 is null", metadataSet);
        Assert.assertEquals("Wrong number of metadata sets prepared", 2,
                metadataSet.size());
        Assert.assertEquals("Metadata for record with ID 123 contains wrong title", "Band 1",
                metadataSet.get(TITLE).get(0));
        Assert.assertEquals("Metadata for record with ID 123 has wrong size of place list",
                1, metadataSet.get(PLACE).size());
        Assert.assertEquals("Metadata for record with ID 123 contains wrong place", "Hamburg",
                metadataSet.get(PLACE).get(0));

        List<CsvRecord> csvRecordsMultipleValues = service.parseLines(CSV_LINES_MULTIPLE_VALUES,
                StringConstants.COMMA_DELIMITER);
        Map<String, Map<String, List<String>>> metadataMultipleValues = service.
                prepareMetadata(METADATA_KEYS_MUTLIPLE_VALUES, csvRecordsMultipleValues);
        Map<String, List<String>> metadataSetMultipleValues = metadataMultipleValues.get("321");
        Assert.assertNotNull("Metadata for record with ID 321 is null", metadataSetMultipleValues);
        Assert.assertEquals("Wrong number of metadata sets prepared", 2,
                metadataSetMultipleValues.size());
        Assert.assertTrue("Metadata for record with ID 321 does not contain place metadata",
                metadataSetMultipleValues.containsKey(PLACE));
        Assert.assertEquals("Metadata for record with ID 123 has wrong size of place list", 2,
                metadataSetMultipleValues.get(PLACE).size());
        Assert.assertEquals("Metadata for record with ID 321 contains wrong place", "Hamburg",
                metadataSetMultipleValues.get(PLACE).get(0));
        Assert.assertEquals("Metadata for record with ID 321 contains wrong place", "Berlin",
                metadataSetMultipleValues.get(PLACE).get(1));

        List<CsvRecord> csvRecordsMultipleValuesWithComma = service.parseLines(CSV_LINES_MULTIPLE_VALUES_WITH_COMMA,
                StringConstants.COMMA_DELIMITER);
        Map<String, Map<String, List<String>>> metadataMultipleValuesWithComma = service.
                prepareMetadata(METADATA_KEYS_MUTLIPLE_VALUES, csvRecordsMultipleValuesWithComma);

        Map<String, List<String>> metadataSetMultipleValuesWithComma = metadataMultipleValuesWithComma.get("978");
        Assert.assertNotNull("Metadata for record with ID 978 is null", metadataSetMultipleValuesWithComma);
        Assert.assertEquals("Wrong number of metadata sets prepared", 2,
                metadataSetMultipleValuesWithComma.size());
        Assert.assertTrue("Metadata for record with ID 978 does not contain place metadata",
                metadataSetMultipleValuesWithComma.containsKey(PLACE));
        Assert.assertEquals("Metadata for record with ID 978 has wrong size of place list", 2,
                metadataSetMultipleValuesWithComma.get(PLACE).size());
        Assert.assertEquals("Metadata for record with ID 978 contains wrong place", "Hamburg",
                metadataSetMultipleValuesWithComma.get(PLACE).get(0));
        Assert.assertEquals("Metadata for record with ID 978 contains wrong place", "Berlin, Kopenhagen",
                metadataSetMultipleValuesWithComma.get(PLACE).get(1));

    }
}
