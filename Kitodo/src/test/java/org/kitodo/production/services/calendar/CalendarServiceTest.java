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

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.kitodo.production.model.bibliography.course.Block;
import org.kitodo.production.model.bibliography.course.Course;
import org.kitodo.production.model.bibliography.course.Issue;

import java.time.LocalDate;
import java.time.Month;

public class CalendarServiceTest {

    @Test
    public void shouldGetIndividualIssues() {
        Course course = new Course();
        Block block = new Block(course);
        Issue firstIssue = block.addIssue();
        Issue secondIssue = block.addIssue();
        firstIssue.setMonday(true);
        firstIssue.setTuesday(true);
        secondIssue.setSaturday(true);
        Assert.assertEquals(0, CalendarService.getIndividualIssues(block).size());

        block.setFirstAppearance(LocalDate.of(2024, Month.MARCH, 1));
        block.setLastAppearance(LocalDate.of(2024, Month.MARCH, 15));
        Assert.assertEquals(6, CalendarService.getIndividualIssues(block).size());

        firstIssue.addExclusion(LocalDate.of(2024, Month.MARCH, 11));
        firstIssue.addExclusion(LocalDate.of(2024, Month.MARCH, 12));
        Assert.assertEquals(4, CalendarService.getIndividualIssues(block).size());

        secondIssue.addAddition(LocalDate.of(2024, Month.MARCH, 1));
        Assert.assertEquals(5, CalendarService.getIndividualIssues(block).size());
    }

    @Test
    public void shouldGetAddableMetadata() {
        // TODO implement
    }

    @Test
    public void shouldGetMetadataTranslation() {
        // TODO implement
    }

    @Test
    public void shouldGetMetadata() {
        // TODO implement
    }

    @Test
    public void shouldGetMetadataSummary() {
        // TODO implement
    }

    @Test
    public void shouldConvertDateIssueToString() {
        Course course = new Course();
        LocalDate date = LocalDate.of(2024, Month.MARCH, 15);
        Issue issue = new Issue(course, "Issue One");

        Pair<LocalDate, Issue> pairOne = Pair.of(date, issue);
        Pair<LocalDate, Issue> pairTwo = Pair.of(date, null);
        Pair<LocalDate, Issue> pairThree = Pair.of(null, issue);
        Pair<LocalDate, Issue> pairFour = Pair.of(null, null);
        Pair<LocalDate, Issue> pairFive = null;

        Assert.assertEquals("2024-03-15, Issue One", CalendarService.dateIssueToString(pairOne));
        Assert.assertEquals("2024-03-15", CalendarService.dateIssueToString(pairTwo));
        Assert.assertEquals("", CalendarService.dateIssueToString(pairThree));
        Assert.assertEquals("", CalendarService.dateIssueToString(pairFour));
        Assert.assertEquals("", CalendarService.dateIssueToString(pairFive));
    }

    @Test
    public void shouldGetAddableMetadataTable() {
        // TODO implement
    }
}
