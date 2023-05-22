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

package org.kitodo.production.model.bibliography.course;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.Metadata;
import org.kitodo.api.MetadataEntry;
import org.kitodo.exceptions.InvalidMetadataValueException;
import org.kitodo.exceptions.UnreachableCodeException;
import org.kitodo.production.model.bibliography.course.metadata.CountableMetadata;

/**
 * Represents a stamping of an Issue, that is one distinguishable physically
 * appeared issue. In opposition, the class {@link Issue} represents the
 * <em>type</em> of issue.
 */
public class IndividualIssue {
    private static final Logger logger = LogManager.getLogger(IndividualIssue.class);

    /**
     * The constant DAY holds a DateTimeFormatter used to get the a two-digit
     * day (01—31) from the newspaper’s date.
     */
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd");

    /**
     * The constant MONTH holds a DateTimeFormatter used to get the a two-digit
     * month (01—12) from the newspaper’s date.
     */
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MM");

    /**
     * The constant YEAR2 holds a DateTimeFormatter used to get the a two-digit
     * year of era (00—99, always positive) from the newspaper’s date.
     */
    private static final DateTimeFormatter YEAR2 = DateTimeFormatter.ofPattern("yy");

    /**
     * The constant YEAR4 holds a DateTimeFormatter used to get the a four-digit
     * year of era (0001—9999, always positive) from the newspaper’s date.
     */
    private static final DateTimeFormatter YEAR4 = DateTimeFormatter.ofPattern("yyyy");

    /**
     * Metadata key to store the sorting number.
     */
    public static final String RULESET_ORDER_NAME = "CurrentNoSorting";

    /**
     * Date of this issue.
     */
    protected final LocalDate date;

    /**
     * The issue this is an issue from.
     */
    protected final Issue issue;

    /**
     * The sorting number of the issue.
     */
    private Integer sortingNumber;

    /**
     * Block that the issue this is an issue from is in.
     */
    protected final Block block;

    /**
     * Constructor to create an IndividualIssue.
     *
     * @param block
     *            Block block this issue is in
     * @param issue
     *            Issue type that this issue is of
     * @param date
     *            Date of appearance
     * @param sortingNumber
     *            sorting number
     */
    IndividualIssue(Block block, Issue issue, LocalDate date, Integer sortingNumber) {
        this.block = block;
        this.issue = issue;
        this.date = date;
        this.sortingNumber = sortingNumber;
    }

    /**
     * Returns an integer which, for a given Granularity, shall indicate for two
     * neighboring individual issues whether they form the same process (break
     * mark is equal) or two different processes (break mark differs).
     *
     * @param mode
     *            how the course shall be broken into processes
     * @param yearStart
     *            the first day of the business year
     * @return an int which differs if two neighboring individual issues belong
     *         to different processes
     */
    public int getBreakMark(Granularity mode, MonthDay yearStart) {
        final int prime = 31;
        switch (mode) {
            case ISSUES:
                return this.hashCode();
            case DAYS:
                return date.hashCode();
            case WEEKS:
                return prime * getFirstYear(yearStart) + date.get(WeekFields.ISO.weekOfWeekBasedYear()); // TODO Weekfields.ISO correct?
            case MONTHS:
                return prime * getFirstYear(yearStart) + date.getMonthValue();
            case QUARTERS:
                return prime * getFirstYear(yearStart) + (date.getMonthValue() - 1) / 3;
            case YEARS:
                return getFirstYear(yearStart);
            default:
                throw new UnreachableCodeException("default case in complete switch statement");
        }
    }

    /**
     * Returns the first calendar year of the year range this issue is on.
     *
     * @param yearStart
     *            the day the new year starts
     * @return the first calendar year of the year range this issue is on
     */
    private int getFirstYear(MonthDay yearStart) {
        int year = date.getYear();
        return date.compareTo(yearStart.atYear(year)) < 0 ? year - 1 : year;
    }

    /**
     * Returns the date of this issue.
     *
     * @return the date of this issue
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Returns a map with generic fields that can be configured for process
     * title creation in kitodo_projects.xml. It provides the issue information
     * in the following fields:
     *
     * <dl>
     * <dt>{@code #DAY}</dt>
     * <dd>two-digit day of month</dd>
     * <dt>{@code #Issue}</dt>
     * <dd>issue name</dd>
     * <dt>{@code #MONTH}</dt>
     * <dd>two-digit month of year</dd>
     * <dt>{@code #YEAR}</dt>
     * <dd>four-digit year</dd>
     * </dl>
     *
     * <p>
     * In addition, the following abbreviated fields are provided:
     *
     * <dl>
     * <dt>{@code #i}</dt>
     * <dd>first letter of issue name in lower case</dd>
     * <dt>{@code #I}</dt>
     * <dd>first letter of issue name in upper case</dd>
     * <dt>{@code #is}</dt>
     * <dd>first two letters of issue name in lower case</dd>
     * <dt>{@code #IS}</dt>
     * <dd>first two letters of issue name in upper case</dd>
     * <dt>{@code #iss}</dt>
     * <dd>first three letters of issue name in lower case</dd>
     * <dt>{@code #ISS}</dt>
     * <dd>first three letters of issue name in upper case</dd>
     * <dt>{@code #issu}</dt>
     * <dd>first four letters of issue name in lower case</dd>
     * <dt>{@code #ISSU}</dt>
     * <dd>first four letters of issue name in upper case</dd>
     * <dt>{@code #YR}</dt>
     * <dd>two-digit year of century</dd>
     * </dl>
     *
     * @return the generic fields for process title creation
     */
    public Map<String, String> getGenericFields() {
        Map<String, String> genericFields = new HashMap<>(18);
        String heading = issue.getHeading();
        int length = heading.length();
        String upperCase = heading.toUpperCase();
        String lowerCase = heading.toLowerCase();
        genericFields.put("#DAY", DAY.format(date));
        genericFields.put("#I", length > 1 ? upperCase.substring(0, 1) : upperCase);
        genericFields.put("#i", length > 1 ? lowerCase.substring(0, 1) : lowerCase);
        genericFields.put("#IS", length > 2 ? upperCase.substring(0, 2) : upperCase);
        genericFields.put("#is", length > 2 ? lowerCase.substring(0, 2) : lowerCase);
        genericFields.put("#ISS", length > 3 ? upperCase.substring(0, 3) : upperCase);
        genericFields.put("#iss", length > 3 ? lowerCase.substring(0, 3) : lowerCase);
        genericFields.put("#ISSU", length > 4 ? upperCase.substring(0, 4) : upperCase);
        genericFields.put("#issu", length > 4 ? lowerCase.substring(0, 4) : lowerCase);
        genericFields.put("#Issue", heading);
        genericFields.put("#MONTH", MONTH.format(date));
        genericFields.put("#YEAR", YEAR4.format(date));
        genericFields.put("#YR", YEAR2.format(date));
        return genericFields;
    }

    /**
     * Returns the metadata for this individual issue.
     *
     * @param monthOfYear
     *            the month of the year start—relevant to correctly calculate
     *            the counter value
     * @param dayOfMonth
     *            the day of the year start—relevant to correctly calculate the
     *            counter value
     * @return a list of pairs, each consisting of the metadata type name and
     *         the value
     */
    public Iterable<Metadata> getMetadata(int monthOfYear, int dayOfMonth) {
        return getMetadata(MonthDay.of(monthOfYear, dayOfMonth));
    }

    /**
     * Returns the metadata for this individual issue.
     *
     * @param yearStart
     *            the day of the year start—relevant to correctly calculate the
     *            counter value
     * @return a list of pairs, each consisting of the metadata type name and
     *         the value
     */
    public Iterable<Metadata> getMetadata(MonthDay yearStart) {
        List<Metadata> result = new ArrayList<>();
        Pair<LocalDate, Issue> selectedIssue = Pair.of(date, issue);
        for (CountableMetadata counter : block.getMetadata(selectedIssue, null)) {
            Collection<Metadata> metadata = Collections.emptyList();
            try {
                metadata = counter.getMetadataDetail().getMetadataWithFilledValues();
            } catch (InvalidMetadataValueException e) {
                logger.error(e.getLocalizedMessage());
            }
            if (counter.getMetadataDetail().getInput().equals("inputText")
                    || counter.getMetadataDetail().getInput().equals("inputTextarea")) {
                String value = counter.getValue(selectedIssue, yearStart);
                if (metadata.stream().findFirst().isPresent()
                        && metadata.stream().findFirst().get() instanceof MetadataEntry) {
                    ((MetadataEntry) metadata.stream().findFirst().get()).setValue(value);
                }
            }
            result.addAll(metadata);
        }
        return result;
    }

    /**
     * Returns the name of the issue this is an issue from.
     *
     * @return the issue’s name
     */
    public String getHeading() {
        return issue.getHeading();
    }

    /**
     * The function getIssue() returns the issue this is an issue from.
     *
     * @return the issue
     */
    public Issue getIssue() {
        return issue;
    }

    /**
     * Returns the list of issues before this issue.
     *
     * @return the list of issues before this
     */
    public List<String> getIssuesBefore() {
        List<String> result = new ArrayList<>();
        for (Issue issue : block.getIssues()) {
            String heading = issue.getHeading();
            if (heading.equals(this.issue.getHeading())) {
                break;
            }
            result.add(heading);
        }
        return result;
    }

    /**
     * Returns the sorting number of the issue.
     *
     * @return the sorting number
     */
    public Integer getSortingNumber() {
        return sortingNumber;
    }

    /**
     * Returns the index of the first occurrence of the block of this issue in
     * the given course, or -1 if the course does not contain the element.
     *
     * @param course
     *            course to find the block in
     * @return the index of the first occurrence of the block of this issue in
     *         the course, or -1 if the course does not contain the element
     */
    int indexIn(Course course) {
        return course.indexOf(block);
    }

    /**
     * Sets the sorting number of the issue.
     *
     * @param sortingNumber
     *            the sorting number to set
     */
    public void setSortingNumber(Integer sortingNumber) {
        this.sortingNumber = sortingNumber;
    }

    /**
     * Provides returns a string that contains a concise but informative
     * representation of this issue that is easy for a person to read.
     *
     * @return a string representation of the issue
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            if (issue.getHeading().length() == 0) {
                return date.toString();
            } else {
                return date.toString() + ", " + issue.getHeading();
            }
        } catch (RuntimeException e) {
            return super.toString();
        }
    }

    /**
     * Returns a hash code for the object which depends on the content of its
     * variables. Whenever IndividualIssue objects are held in HashSet objects,
     * a hashCode() is essentially necessary.
     *
     * <p>
     * The method was generated by Eclipse using right-click → Source → Generate
     * hashCode() and equals()…. If you will ever change the classes’ fields,
     * just re-generate it.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + ((date == null) ? 0 : date.hashCode());
        hashCode = prime * hashCode + ((issue == null) ? 0 : issue.hashCode());
        hashCode = prime * hashCode + ((block == null) ? 0 : block.hashCode());
        return hashCode;
    }

    /**
     * Returns whether two individual issues are equal; the decision depends on
     * the content of its variables.
     *
     * <p>
     * The method was generated by Eclipse using right-click → Source → Generate
     * hashCode() and equals()…. If you will ever change the classes’ fields,
     * just re-generate it.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof IndividualIssue) {
            IndividualIssue other = (IndividualIssue) obj;
            if (Objects.isNull(date)) {
                if (Objects.nonNull(other.date)) {
                    return false;
                }
            } else if (!date.equals(other.date)) {
                return false;
            }
            if (Objects.isNull(issue)) {
                if (Objects.nonNull(other.issue)) {
                    return false;
                }
            } else if (!issue.equals(other.issue)) {
                return false;
            }
            if (Objects.isNull(block)) {
                return Objects.isNull(other.block);
            } else {
                return block.equals(other.block);
            }
        }
        return false;
    }
}
