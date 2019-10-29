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
import java.time.format.DateTimeFormatter;

import org.kitodo.exceptions.NotImplementedException;

/**
 * The Granularity indicates one out of six options how a course of appearance
 * of a newspaper can be broken into processes. These are as follows:
 *
 * <dl>
 * <dt>ISSUES</dt>
 * <dd>Each issue is scanned in an individual process.</dd>
 * <dt>DAYS</dt>
 * <dd>All issues of one day are scanned in one process.</dd>
 * <dt>WEEKS</dt>
 * <dd>All issues of a week are scanned in one process. A week starts on
 * Mondays. Keep in mind that week borders do not necessarily match month and
 * not even year borders.</dd>
 * <dt>MONTHS</dt>
 * <dd>All issues of a month are scanned in one process.</dd>
 * <dt>MONTHS</dt>
 * <dd>All issues of a quarter of a year are scanned in one process.</dd>
 * <dt>YEARS</dt>
 * <dd>All issues of a year are scanned in one process.</dd>
 * </dl>
 */
public enum Granularity {
    ISSUES,
    DAYS,
    WEEKS,
    MONTHS,
    QUARTERS,
    YEARS;

    /**
     * Converts a given LocalDate to a String representation of the date in the
     * given granularity. For the 1st January 2000 it will return:
     * <ul>
     * <li>for DAYS: 2000-01-01
     * <li>for WEEKS: 1999-W52
     * <li>for MONTHS: 2000-01
     * <li>for QUARTERS: 2000/Q1
     * <li>for YEARS: 2000
     * </ul>
     *
     * <p>
     * The remaining cases are undefined and will throw NotImplementedException.
     *
     * @param date
     *            date to format
     * @return an expression of the date in the given granularity
     */
    public String format(LocalDate date) {
        switch (this) {
            case DAYS:
                return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
            case MONTHS:
                return DateTimeFormatter.ofPattern("yyyy-MM").format(date);
            case QUARTERS:
                return DateTimeFormatter.ofPattern("yyyy").format(date) + "/Q"
                        + ((date.getMonthValue() - 1) / 3) + 1;
            case WEEKS:
                return DateTimeFormatter.ofPattern("yyyy-'W'ww").format(date);
            case YEARS:
                return DateTimeFormatter.ofPattern("yyyy").format(date);
            default:
                throw new NotImplementedException();
        }
    }
}
