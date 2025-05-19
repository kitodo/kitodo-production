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
package org.kitodo.production.services.calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.api.dataeditor.rulesetmanagement.MetadataViewInterface;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.model.bibliography.course.Block;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.Issue;
import org.kitodo.production.model.bibliography.course.metadata.CountableMetadata;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.test.utils.ProcessTestUtils;

public class CalendarServiceIT {

    private static int newspaperTestProcessId = -1;
    private static final String NEWSPAPER_TEST_METADATA_FILE = "testmetaNewspaper.xml";
    private static final String NEWSPAPER_TEST_PROCESS_TITLE = "NewspaperOverallProcess";


    @BeforeAll
    public static void prepareDatabase() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        int rulesetId = MockDatabase.insertRuleset("Newspaper", "newspaper.xml", 1);
        newspaperTestProcessId = MockDatabase.insertTestProcess(NEWSPAPER_TEST_PROCESS_TITLE, 1, 1, rulesetId);
        ProcessTestUtils.copyTestMetadataFile(newspaperTestProcessId, NEWSPAPER_TEST_METADATA_FILE);
        SecurityTestUtils.addUserDataToSecurityContext(ServiceManager.getUserService().getById(1), 1);
    }

    @AfterAll
    public static void cleanDatabase() throws Exception {
        ProcessTestUtils.removeTestProcess(newspaperTestProcessId);
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @Test
    public void shouldGetAddableMetadata() throws DAOException, DataException, IOException {
        Process process = ServiceManager.getProcessService().getById(newspaperTestProcessId);
        List<MetadataViewInterface> actualMetadata = CalendarService.getAddableMetadata(process);

        List<String> expectedMetadata = Arrays.asList("ORDERLABEL", "LABEL", "CONTENTIDS", "ProcessTitle", "ShelfMark");
        assertEquals(expectedMetadata, actualMetadata.stream()
                .map(MetadataViewInterface::getId)
                .collect(Collectors.toList()));
    }

    @Test
    public void shouldGetMetadata() {
        Course course = new Course();
        Block block = new Block(course);
        Issue firstIssue = block.addIssue();
        Issue secondIssue = block.addIssue();
        firstIssue.setMonday(true);
        firstIssue.setTuesday(true);
        secondIssue.setSaturday(true);

        block.setFirstAppearance(LocalDate.of(2024, Month.MARCH, 1));
        block.setLastAppearance(LocalDate.of(2024, Month.MARCH, 15));

        Pair<LocalDate, Issue> firstIssueFirstAppearance = Pair.of(LocalDate.of(2024, Month.MARCH, 4), firstIssue);
        CountableMetadata processTitle = new CountableMetadata(block, firstIssueFirstAppearance);
        processTitle.setMetadataType("ProcessTitle");
        block.addMetadata(processTitle);

        Pair<LocalDate, Issue> secondIssueFirstAppearance = Pair.of(LocalDate.of(2024, Month.MARCH, 5), secondIssue);
        CountableMetadata shelfMark = new CountableMetadata(block, secondIssueFirstAppearance);
        shelfMark.setMetadataType("ShelfMark");
        block.addMetadata(shelfMark);

        assertEquals(1,
                CalendarService.getMetadata(block, LocalDate.of(2024, Month.MARCH, 4), firstIssue).size());
        assertEquals(2,
                CalendarService.getMetadata(block, LocalDate.of(2024, Month.MARCH, 5), secondIssue).size());
        assertEquals(2,
                CalendarService.getMetadata(block, LocalDate.of(2024, Month.MARCH, 12), secondIssue).size());
    }

    @Test
    public void shouldGetMetadataSummary() throws Exception {
        Process process = ServiceManager.getProcessService().getById(newspaperTestProcessId);
        List<ProcessDetail> addableMetadata = CalendarService.getAddableMetadataTable(process);
        Course course = new Course();
        Block block = new Block(course);
        Issue firstIssue = block.addIssue();
        Issue secondIssue = block.addIssue();
        firstIssue.setMonday(true);
        firstIssue.setTuesday(true);
        secondIssue.setSaturday(true);

        block.setFirstAppearance(LocalDate.of(2024, Month.MARCH, 1));
        block.setLastAppearance(LocalDate.of(2024, Month.MARCH, 15));

        Pair<LocalDate, Issue> firstIssueFirstAppearance = Pair.of(LocalDate.of(2024, Month.MARCH, 4), firstIssue);
        CountableMetadata processTitle = new CountableMetadata(block, firstIssueFirstAppearance);
        processTitle.setMetadataDetail(addableMetadata.get(3));
        processTitle.setStartValue("Test Process Title");
        block.addMetadata(processTitle);

        Pair<LocalDate, Issue> secondIssueFirstAppearance = Pair.of(LocalDate.of(2024, Month.MARCH, 5), secondIssue);
        CountableMetadata shelfMark = new CountableMetadata(block, secondIssueFirstAppearance);
        shelfMark.setMetadataDetail(addableMetadata.get(4));
        shelfMark.setStartValue("Test Shelf Mark");
        block.addMetadata(shelfMark);

        List<String> actualMetadataSummary = CalendarService.getMetadataSummary(block).stream()
                .map(entry -> entry.getKey().getLabel() + " - " + entry.getValue().toString())
                .sorted().collect(Collectors.toList());
        List<String> expectedMetadataSummary = Arrays.asList("Process title - 2024-03-04", "Signatur - 2024-03-05");
        assertEquals(expectedMetadataSummary, actualMetadataSummary);
    }

    @Test
    public void shouldGetAddableMetadataTable() throws DAOException, DataException, IOException {
        Process process = ServiceManager.getProcessService().getById(newspaperTestProcessId);
        List<ProcessDetail> actualMetadata = CalendarService.getAddableMetadataTable(process);

        List<String> expectedMetadata = Arrays.asList(
                "METS Reihenfolge-Etikett", "METS-Beschriftung", "METS-Inhalts-ID", "Process title", "Signatur");
        assertEquals(expectedMetadata, actualMetadata.stream().map(ProcessDetail::getLabel).collect(Collectors.toList()));
    }
}
