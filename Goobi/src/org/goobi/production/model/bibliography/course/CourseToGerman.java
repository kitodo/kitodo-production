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

package org.goobi.production.model.bibliography.course;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import de.sub.goobi.helper.*;

/**
 * The static class CourseToGerman provides a toString() method to convert a
 * course of appearance into a verbal description in German language.
 *
 * @author Matthias Ronge
 */
public class CourseToGerman {
    /**
     * Days of week’s names in German.
     *
     * <p>
     * Joda time’s days of week are 1-based, where 1 references Monday through 7
     * references Sunday. Therefore the “null” in first place.
     * </p>
     */
    private static final String[] DAYS_OF_WEEK_NAMES = new String[] { null, "Montag", "Dienstag", "Mittwoch",
            "Donnerstag", "Freitag", "Samstag", "Sonntag" };

    /**
     * Month’s names in German.
     *
     * <p>
     * Joda time’s months are 1-based, therefore the “null” in first place.
     * </p>
     */
    private static final String[] MONTH_NAMES = new String[] { null, "Januar", "Februar", "März", "April", "Mai",
            "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" };

    /**
     * The function toString() returns a verbal description of the course of
     * appearance in German.
     *
     * @return Verbal description of the course in German
     */
    public static List<String> asReadableText(Course course) {
        List<String> result = new ArrayList<String>();
        if (course.isEmpty()) {
            return result;
        }
        Iterator<Block> blocks = course.iterator();
        boolean hasPreviousBlock = false;
        do {
            Block block = blocks.next();
            result.add(titleToString(block, hasPreviousBlock));
            for (Issue issue : block.getIssues()) {
                boolean regular = false;
                for (int dayOfWeek = DateTimeConstants.MONDAY; dayOfWeek <= DateTimeConstants.SUNDAY; dayOfWeek++) {
                    if (issue.isDayOfWeek(dayOfWeek)) {
                        regular = true;
                        break;
                    }
                }
                String irregularities = irregularitiesToString(issue, regular);
                if (irregularities != null) {
                    result.add(irregularities);
                }
            }
            hasPreviousBlock = true;
        } while (blocks.hasNext());
        return result;
    }

    /**
     * The function titleToString() formulates the regular appearance of a block
     * in German language.
     *
     * @param block
     *            Titel to formulate
     * @param subsequentBlock
     *            false for the first block, true otherwise
     */
    private static String titleToString(Block block, boolean subsequentBlock) {
        StringBuilder result = new StringBuilder(500);
        if (subsequentBlock == false) {
            result.append("Die Gesamtausgabe erschien vom ");
            appendDate(result, block.getFirstAppearance());
        } else {
            result.append("Ab dem ");
            appendDate(result, block.getFirstAppearance());
            result.append(" erschien sie");
        }
        result.append(" bis zum ");
        appendDate(result, block.getLastAppearance());
        List<Issue> issues = block.getIssues();
        result.append(" ");
        int currentIssuesSize = issues.size();
        Boolean regularly = null;
        for (int issueIndex = 0; issueIndex < currentIssuesSize; issueIndex++) {
            Issue issue = issues.get(issueIndex);
            boolean appearedRegularly = false;
            for (int testDayOfWeek = DateTimeConstants.MONDAY; testDayOfWeek <= DateTimeConstants.SUNDAY; testDayOfWeek++) {
                if (issue.isDayOfWeek(testDayOfWeek)) {
                    if (!Boolean.TRUE.equals(regularly)) {
                        result.append("regelmäßig ");
                        regularly = Boolean.TRUE;
                    }
                    result.append("an allen ");
                    int daysOfWeekCount = 0;
                    for (int dayOfWeek = DateTimeConstants.MONDAY; dayOfWeek <= DateTimeConstants.SUNDAY; dayOfWeek++) {
                        if (issue.isDayOfWeek(dayOfWeek)) {
                            result.append(DAYS_OF_WEEK_NAMES[dayOfWeek]);
                            result.append("en");
                            daysOfWeekCount++;
                            if (daysOfWeekCount < issue.getDaysOfWeek().size() - 1) {
                                result.append(", ");
                            }
                            if (daysOfWeekCount == issue.getDaysOfWeek().size() - 1) {
                                result.append(" und ");
                            }
                        }
                    }
                    appearedRegularly = true;
                    break;
                }
            }
            if (!appearedRegularly && !Boolean.FALSE.equals(regularly)) {
                result.append("nur unregelmäßig");
                regularly = Boolean.FALSE;
            }
            if (!issue.getHeading().isEmpty()) {
                result.append(" als ");
                result.append(issue.getHeading());
            }

            if (issueIndex < currentIssuesSize - 2) {
                result.append(", ");
            }
            if (issueIndex == currentIssuesSize - 2) {
                result.append(" sowie ");
            }
            if (issueIndex == currentIssuesSize - 1) {
                result.append(".");
            }
        }

        return result.toString();
    }

    /**
     * The function irregularitiesToString() formulates the irregularities of a
     * given issue in German language.
     *
     * @param issues
     *            issues whose irregularities shall be formulated
     * @param regular
     *            whether the course of appearance has dates of regular
     *            appearance
     */
    private static String irregularitiesToString(Issue issue, boolean regular) {
        int additionsSize = issue.getAdditions().size();
        int exclusionsSize = issue.getExclusions().size();
        StringBuilder buffer = new StringBuilder((int) (Math.ceil(10.907 * (additionsSize + exclusionsSize)) + 500));

        if (additionsSize == 0 && exclusionsSize == 0) {
            return null;
        }

        buffer.append("Die Ausgabe");
        if (!issue.getHeading().isEmpty()) {
            buffer.append(" „");
            buffer.append(issue.getHeading());
            buffer.append("“");
        }
        buffer.append(" erschien ");

        if (exclusionsSize > 0) {
            appendManyDates(buffer, issue.getExclusions(), Ternary.FALSE);
            if (additionsSize > 0) {
                buffer.append(", dafür jedoch ");
            }
        }
        if (additionsSize > 0) {
            appendManyDates(buffer, issue.getAdditions(), regular ? Ternary.TRUE : Ternary.UNKNOWN);
        }
        buffer.append(".");
        return buffer.toString();
    }

    /**
     * The method appendManyDates() converts a lot of date objects into readable
     * text in German language.
     *
     * @param buffer
     *            StringBuilder to write to
     * @param dates
     *            Set of dates to convert to text
     * @param signum
     *            sign, i.e. true for additions, false for exclusions
     * @throws NoSuchElementException
     *             if dates has no elements
     * @throws NullPointerException
     *             if buffer or dates is null
     */
    private static void appendManyDates(StringBuilder buffer, Set<LocalDate> dates, Ternary signum) {
        if (signum.equals(Ternary.TRUE)) {
            buffer.append("zusätzlich ");
        } else if (signum.equals(Ternary.FALSE)) {
            buffer.append("nicht ");
        }

        TreeSet<LocalDate> orderedDates = dates instanceof TreeSet ? (TreeSet<LocalDate>) dates
                : new TreeSet<LocalDate>(dates);

        Iterator<LocalDate> datesIterator = orderedDates.iterator();

        LocalDate current = datesIterator.next();
        LocalDate next = datesIterator.hasNext() ? datesIterator.next() : null;
        LocalDate overNext = datesIterator.hasNext() ? datesIterator.next() : null;
        int previousYear = Integer.MIN_VALUE;
        boolean nextInSameMonth = false;
        boolean nextBothInSameMonth = next != null ? DateUtils.sameMonth(current, next) : false;
        int lastMonthOfYear = DateUtils.lastMonthForYear(orderedDates, current.getYear());

        do {
            nextInSameMonth = nextBothInSameMonth;
            nextBothInSameMonth = DateUtils.sameMonth(next, overNext);

            if (previousYear != current.getYear()) {
                buffer.append("am ");
            }

            buffer.append(current.getDayOfMonth());
            buffer.append('.');

            if (!nextInSameMonth) {
                buffer.append(' ');
                buffer.append(MONTH_NAMES[current.getMonthOfYear()]);
            }

            if (!DateUtils.sameYear(current, next)) {
                buffer.append(' ');
                buffer.append(current.getYear());
                if (next != null) {
                    if (!DateUtils.sameYear(next, orderedDates.last())) {
                        buffer.append(", ");
                    } else {
                        buffer.append(" und ebenfalls ");
                        if (signum.equals(Ternary.FALSE)) {
                            buffer.append("nicht ");
                        }
                    }
                }
                if (next != null) {
                    lastMonthOfYear = DateUtils.lastMonthForYear(orderedDates, next.getYear());
                }
            } else if (next != null) {
                if (nextInSameMonth && nextBothInSameMonth || !nextInSameMonth
                        && next.getMonthOfYear() != lastMonthOfYear) {
                    buffer.append(", ");
                } else {
                    buffer.append(" und ");
                }
            }

            previousYear = current.getYear();
            current = next;
            next = overNext;
            overNext = datesIterator.hasNext() ? datesIterator.next() : null;
        } while (current != null);
    }

    /**
     * The method appendDate() writes a date to the buffer.
     *
     * @param buffer
     *            Buffer to write to
     * @param date
     *            Date to write
     */
    private static void appendDate(StringBuilder buffer, LocalDate date) {
        buffer.append(date.getDayOfMonth());
        buffer.append(". ");
        buffer.append(MONTH_NAMES[date.getMonthOfYear()]);
        buffer.append(' ');
        buffer.append(date.getYear());
        return;
    }
}
