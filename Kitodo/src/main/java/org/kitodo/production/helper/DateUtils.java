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

package org.kitodo.production.helper;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.TreeSet;

/**
 * The class DateUtils contains an omnium-gatherum of functions that work on
 * calendar dates.
 */
public class DateUtils {
    /**
     * The field DATE_FORMATTER provides a DateTimeFormatter that is used to
     * convert between LocalDate objects and String in common German notation.
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    /**
     * Private constructor to hide the implicit public one.
     */
    private DateUtils() {

    }

    /**
     * Returns from an ordered set of dates for
     * a given year the last month which a date can be found for in that year.
     *
     * <p>
     * Example: Let the ordered data set contain: …, 5th May 1954, 20th May
     * 1954, 13th October 1954, 5th February 1955, 23th March 1955, 15th October
     * 1956, …. Then the function will return 10 (DateTimeConstants.OCTOBER) for
     * 1954 and 3 (DateTimeConstants.MARCH) for 1955.
     *
     * @param data
     *            an ordered set of dates
     * @param year
     *            year in question
     * @return the last month which can be found up to the end of that year
     */
    public static int lastMonthForYear(TreeSet<LocalDate> data, int year) {
        return data.headSet(LocalDate.of(year, Month.DECEMBER, 31), true).last().getMonthValue();
    }

    /**
     * Compares two LocalDate objects in regard to the
     * question whether their two dates reside in the same month of the calendar
     * system presumed. Two dates are considered to be in the same month exactly
     * if both their year and month of year fields are equal.
     *
     * @param compared
     *            date to compare against
     * @param comparee
     *            date to compare, may be null
     * @return whether the two dates are in the same month
     */
    public static boolean sameMonth(LocalDate compared, LocalDate comparee) {
        if (!sameYear(compared, comparee)) {
            return false;
        }
        return compared.getMonthValue() == comparee.getMonthValue();
    }

    /**
     * Compares two LocalDate objects in regard to the
     * question whether their two dates reside in the same year of the calendar
     * system presumed. Two dates are considered to be in the same year exactly
     * if none of them is null and their year fields are equal.
     *
     * @param current
     *            date to compare against
     * @param next
     *            date to compare, may be null
     * @return whether the two dates are in the same year
     */
    public static boolean sameYear(LocalDate current, LocalDate next) {
        if (Objects.isNull(next)) {
            return false;
        }
        return current.getYear() == next.getYear();
    }
}
