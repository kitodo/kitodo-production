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

package org.kitodo.model.bibliography.course;

import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;
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
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public enum Granularity {
    ISSUES,
    DAYS,
    WEEKS,
    MONTHS,
    QUARTERS,
    YEARS;

    /**
     * The function format() converts a given LocalDate to a String
     * representation of the date in the given granularity. For the 1st January
     * 2000 it will return:
     * <p/>
     * • for DAYS: 2000-01-01 • for WEEKS: 1999-W52 • for MONTHS: 2000-01 • for
     * QUARTERS: 2000/Q1 • for YEARS: 2000
     *
     * <p>
     * The remaining cases are undefined and will throw NotImplementedException.
     * </p>
     *
     * @param date
     *            date to format
     * @return an expression of the date in the given granularity
     */
    public String format(LocalDate date) {
        switch (this) {
            case DAYS:
                return ISODateTimeFormat.date().print(date);
            case MONTHS:
                return ISODateTimeFormat.yearMonth().print(date);
            case QUARTERS:
                return ISODateTimeFormat.year().print(date) + "/Q"
                        + ((date.getMonthOfYear() - 1) / 3) + 1;
            case WEEKS:
                return ISODateTimeFormat.weekyearWeek().print(date);
            case YEARS:
                return ISODateTimeFormat.year().print(date);
            default:
                throw new NotImplementedException();
        }
    }
}
