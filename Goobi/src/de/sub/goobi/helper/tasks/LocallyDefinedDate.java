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

package de.sub.goobi.helper.tasks;

import org.joda.time.format.ISODateTimeFormat;

/**
 * A local date represents a date, specified as locally defined.
 */
class LocallyDefinedDate {

    /**
     * A year is an arbitrary identifier, which may or may not be a number, or a
     * cooccurrence of text and numbers. It can be used to represent gregorian
     * calendar years, business years, liturgical years, etc.
     */
    private String compoundYear;

    /**
     * A day of a month is a date that occurs once per year.
     */
    private org.joda.time.LocalDate date;

    /**
     * Creates a new local date.
     *
     * @param calendarYear
     *            the calendar year in the gregorian calendar
     * @param compoundYear
     *            an arbitrary identifier, which may or may not be a number, or
     *            a cooccurrence of text and numbers
     * @param monthOfYear
     *            month component of the date
     * @param dayOfMonth
     *            day component of the date
     */
    public LocallyDefinedDate(int calendarYear, String compoundYear, int monthOfYear, int dayOfMonth) {
        this.compoundYear = compoundYear;
        this.date = new org.joda.time.LocalDate(calendarYear, monthOfYear, dayOfMonth);
    }

    /**
     * Returns the year component of the date.
     *
     * @return the year
     */
    public String getYear() {
        return compoundYear;
    }

    /**
     * Returns the month component of the date.
     *
     * @return the month
     */
    public int getMonthOfYear() {
        return date.getMonthOfYear();
    }

    /**
     * Returns the day component of the date.
     *
     * @return the day
     */
    public int getDayOfMonth() {
        return date.getDayOfMonth();
    }

    /**
     * Returns the year and month fragment of the date in ISO format.
     *
     * @return the year and month fragment
     */
    public String getYearMonth() {
        return ISODateTimeFormat.yearMonth().print(date);
    }

    /**
     * Returns the year, month and day fragment of the date in ISO format.
     *
     * @return the year, month and day fragment
     */
    public String getYearMonthDay() {
        return ISODateTimeFormat.yearMonthDay().print(date);
    }

}
