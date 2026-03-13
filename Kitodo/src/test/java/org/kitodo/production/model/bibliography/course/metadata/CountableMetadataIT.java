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

package org.kitodo.production.model.bibliography.course.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.time.MonthDay;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kitodo.MockDatabase;
import org.kitodo.SecurityTestUtils;
import org.kitodo.data.database.beans.User;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.production.forms.createprocess.ProcessDetail;
import org.kitodo.production.forms.createprocess.ProcessTextMetadata;
import org.kitodo.production.model.bibliography.course.Block;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.Granularity;
import org.kitodo.production.model.bibliography.course.Issue;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.test.utils.ProcessTestUtils;
import org.kitodo.test.utils.TestConstants;

public class CountableMetadataIT {

    private Issue issue;
    private CountableMetadata countableMetadata;
    private static int processId = -1;
    private static final int EXPECTED_NUMBER_OF_METADATA_TYPES = 14;
    private static final int PUBLICATION_YEAR = 1990;
    private static final Month PUBLICATION_MONTH = Month.MARCH;
    private static final LocalDate BLOCK_START = LocalDate.of(PUBLICATION_YEAR, PUBLICATION_MONTH, 1);
    private static final LocalDate BLOCK_END = LocalDate.of(PUBLICATION_YEAR, PUBLICATION_MONTH, 30);
    private static final LocalDate HIDDEN_DATE = LocalDate.of(PUBLICATION_YEAR, PUBLICATION_MONTH, 5);
    private static final LocalDate DEFINE_DATE = LocalDate.of(PUBLICATION_YEAR, PUBLICATION_MONTH, 12);
    private static final LocalDate CONTINUE_DATE = LocalDate.of(PUBLICATION_YEAR, PUBLICATION_MONTH, 19);
    private static final LocalDate DELETE_DATE = LocalDate.of(PUBLICATION_YEAR, PUBLICATION_MONTH, 26);
    private static final MonthDay YEAR_START = MonthDay.of(Month.JANUARY, 1);
    private static final String ISSUE_HEADING = "Abendausgabe";
    private static final String PROCESS_TITLE = "Countable_Metadata_Test_Process";
    private static final String METADATA_START_VALUE = "Ausgabe 1";

    @BeforeAll
    public static void setup() throws Exception {
        MockDatabase.startNode();
        MockDatabase.insertProcessesFull();
        MockDatabase.setUpAwaitility();
        User userOne = ServiceManager.getUserService().getById(1);
        SecurityTestUtils.addUserDataToSecurityContext(userOne, 1);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        MockDatabase.stopNode();
        MockDatabase.cleanDatabase();
    }

    @BeforeEach
    public void init() {
        Course course = new Course();
        issue = new Issue(course);
        issue.setHeading(ISSUE_HEADING);
        issue.setMonday(true);
        Pair<LocalDate, Issue> pair = new ImmutablePair<>(DEFINE_DATE, issue);
        Block block = new Block(course);
        block.setPublicationPeriod(BLOCK_START, BLOCK_END);
        block.addIssue(issue);
        countableMetadata = new CountableMetadata(block, pair);
        countableMetadata.setStepSize(Granularity.ISSUES);
        countableMetadata.setDelete(new ImmutablePair<>(DELETE_DATE, issue));
    }

    @AfterEach
    public void removeTestProcess() throws DAOException {
        if (processId > 0) {
            ProcessTestUtils.removeTestProcess(processId);
            processId = -1;
        }
    }

    @Test
    public void shouldGetEditMode() {
        Pair<LocalDate, Issue> priorPair = new ImmutablePair<>(HIDDEN_DATE, issue);
        MetadataEditMode shouldBeHiddenMode = countableMetadata.getEditMode(priorPair);
        assertEquals(MetadataEditMode.HIDDEN, shouldBeHiddenMode);

        Pair<LocalDate, Issue> concurrentPair = new ImmutablePair<>(DEFINE_DATE, issue);
        MetadataEditMode shouldBeDefineMode = countableMetadata.getEditMode(concurrentPair);
        assertEquals(MetadataEditMode.DEFINE, shouldBeDefineMode);

        Pair<LocalDate, Issue> laterPair = new ImmutablePair<>(CONTINUE_DATE, issue);
        MetadataEditMode shouldBeContinueMode = countableMetadata.getEditMode(laterPair);
        assertEquals(MetadataEditMode.CONTINUE, shouldBeContinueMode);

        Pair<LocalDate, Issue> deletePair = new ImmutablePair<>(DELETE_DATE, issue);
        MetadataEditMode shouldBeDeleteMode = countableMetadata.getEditMode(deletePair);
        assertEquals(MetadataEditMode.DELETE, shouldBeDeleteMode);
    }

    @Test
    public void shouldGetValue() throws DAOException, InvalidMetadataValueException {
        List<ProcessDetail> metadataTypes = getMetadataTypes();
        ProcessDetail firstMetadataType = metadataTypes.stream().filter(ProcessTextMetadata.class::isInstance)
                .collect(Collectors.toList()).getFirst();
        countableMetadata.setMetadataDetail(firstMetadataType);
        countableMetadata.setStartValue(METADATA_START_VALUE);
        Pair<LocalDate, Issue> issuePair = new ImmutablePair<>(DEFINE_DATE.plusWeeks(1), issue);
        String value = countableMetadata.getValue(issuePair, YEAR_START);
        assertTrue(StringUtils.isNotBlank(value));
    }

    @Test
    public void shouldMatch() throws DAOException, InvalidMetadataValueException {
        List<ProcessDetail> metadataTypes = getMetadataTypes();
        ProcessDetail firstMetadataType = metadataTypes.stream().filter(ProcessTextMetadata.class::isInstance)
                .collect(Collectors.toList()).getFirst();
        countableMetadata.setMetadataDetail(firstMetadataType);
        countableMetadata.setStartValue(METADATA_START_VALUE);
        Pair<LocalDate, Issue> deletePair = new ImmutablePair<>(DELETE_DATE, issue);
        assertTrue(countableMetadata.matches(firstMetadataType.getMetadataID(), deletePair, false));
        assertFalse(countableMetadata.matches(firstMetadataType.getMetadataID(), deletePair, true));
    }

    @Test
    public void shouldGetAllMetadataTypes() throws DAOException {
        List<ProcessDetail> allMetadataTypes = getMetadataTypes();
        assertEquals(EXPECTED_NUMBER_OF_METADATA_TYPES, allMetadataTypes.size());
        List<String> labels = allMetadataTypes.stream().map(ProcessDetail::getLabel).collect(Collectors.toList());
        assertTrue(labels.contains(TestConstants.TITLE_MAIN));
        assertTrue(labels.contains(TestConstants.METS_LABEL));
    }

    private List<ProcessDetail> getMetadataTypes() throws DAOException {
        processId = MockDatabase.insertTestProcess(PROCESS_TITLE, 1, 1, 1);
        List<ProcessDetail> allMetadataTypes = countableMetadata.getAllMetadataTypes(processId);
        assertTrue(Objects.nonNull(allMetadataTypes));
        assertFalse(allMetadataTypes.isEmpty());
        return allMetadataTypes;
    }
}
