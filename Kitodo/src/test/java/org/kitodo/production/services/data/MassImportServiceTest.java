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

import org.junit.jupiter.api.Test;
import org.kitodo.production.enums.SeparatorCharacter;
import org.kitodo.production.forms.CsvCell;
import org.kitodo.production.forms.CsvRecord;
import org.kitodo.production.services.ServiceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MassImportServiceTest {

    private static final List<String> METADATA_KEYS = Arrays.asList("Surname", "FirstName", "Title", "Place");

    private static final List<String> csvLinesWithCommaSeparator = Arrays.asList(
            "Value11, Value12, Value13;Contains SpecialSemicolon",
            "Value21, Value22, Value23",
            "Value31, Value32,");

    private static final List<String> csvLinesWithSemicolonSeparator = Arrays.asList(
            "Value11; Value12; LastName1, FirstName1",
            "Value21; Value22; LastName2, FirstName2",
            "Value31; Value32; LastName3, FirstName3");

    @Test
    public void shouldGuessCsvSeparatorCharacter() {
        MassImportService massImportService = ServiceManager.getMassImportService();
        assertEquals(SeparatorCharacter.COMMA, massImportService.guessCsvSeparator(csvLinesWithCommaSeparator));
        assertEquals(SeparatorCharacter.SEMICOLON, massImportService.guessCsvSeparator(csvLinesWithSemicolonSeparator));
    }

    @Test
    public void shouldGetColumnSkipIndices() {
        MassImportService massImportService = ServiceManager.getMassImportService();
        List<Integer> skipIndices = massImportService.getColumnSkipIndices(getCsvRecords(), METADATA_KEYS.size());
        assertEquals(1, skipIndices.size(), "Only one column should be skipped");
        assertEquals(2, skipIndices.iterator().next().intValue(), "Wrong column index");
    }

    @Test
    public void shouldDiscardMetadataKeysOfEmptyColumns() {
        List<String> metadataKeys = new ArrayList<>(METADATA_KEYS);
        assertTrue(metadataKeys.contains("Title"));
        MassImportService massImportService = ServiceManager.getMassImportService();
        metadataKeys = massImportService.discardMetadataKeysOfEmptyColumns(metadataKeys, Collections.singletonList(2));
        assertFalse(metadataKeys.contains("Title"));
    }

    @Test
    public void shouldDiscardEmptyColumns() {
        List<Integer> skipIndices = Collections.singletonList(2);
        List<CsvRecord> csvRecords = getCsvRecords();
        for (CsvRecord csvRecord : csvRecords) {
            assertEquals(4, csvRecord.getCsvCells().size(), "Wrong number of CSV cells in records before skipping empty columns");
        }
        MassImportService massImportService = ServiceManager.getMassImportService();
        csvRecords = massImportService.discardEmptyColumns(csvRecords, skipIndices);
        for (CsvRecord csvRecord : csvRecords) {
            assertEquals(3, csvRecord.getCsvCells().size(), "Wrong number of CSV cells in records after skipping empty columns");
        }
    }

    private List<CsvRecord> getCsvRecords() {
        List<CsvRecord> records = new ArrayList<>();
        CsvRecord firstRecord = new CsvRecord(METADATA_KEYS.size());
        List<CsvCell> firstRecordCells = new ArrayList<>();
        firstRecordCells.add(new CsvCell("Mustermann"));
        firstRecordCells.add(new CsvCell("Max"));
        firstRecordCells.add(new CsvCell(""));
        firstRecordCells.add(new CsvCell("Berlin"));
        firstRecord.setCsvCells(firstRecordCells);
        records.add(firstRecord);

        CsvRecord secondRecord = new CsvRecord(METADATA_KEYS.size());
        List<CsvCell> secondRecordsCells =  new ArrayList<>();
        secondRecordsCells.add(new CsvCell("Nowak"));
        secondRecordsCells.add(new CsvCell("Adam"));
        secondRecordsCells.add(new CsvCell(null));
        secondRecordsCells.add(new CsvCell(""));
        secondRecord.setCsvCells(secondRecordsCells);
        records.add(secondRecord);

        CsvRecord thirdRecord = new CsvRecord(METADATA_KEYS.size());
        List<CsvCell> thirdRecordsCells =  new ArrayList<>();
        thirdRecordsCells.add(new CsvCell("Dora"));
        thirdRecordsCells.add(new CsvCell("Anna"));
        thirdRecordsCells.add(new CsvCell(""));
        thirdRecordsCells.add(new CsvCell("Leipzig"));
        thirdRecord.setCsvCells(thirdRecordsCells);
        records.add(thirdRecord);

        return records;
    }

}
