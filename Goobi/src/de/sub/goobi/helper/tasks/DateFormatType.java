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

import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

/**
 * An enumeration of the possible date format types for newspaper processes.
 */
enum DateFormatType {
    /**
     * New date format, where month levels is {@code yyyy-mm} and day level is
     * {@code yyyy-mm-dd}.
     */
    NEW {
        @Override
        String formatMonthLevel(LocalDate date) {
            return ISODateTimeFormat.yearMonth().print(date);
        }

        @Override
        String formatDayLevel(LocalDate date) {
            return ISODateTimeFormat.yearMonthDay().print(date);

        }
    },

    /**
     * Old date format, where month and day levels are int values 1-12 / 1-31.
     */
    OLD {
        @Override
        String formatMonthLevel(LocalDate date) {
            return String.valueOf(date.getMonthOfYear());
        }

        @Override
        String formatDayLevel(LocalDate date) {
            return String.valueOf(date.getDayOfMonth());
        }
    };

    /**
     * Format a date for the month level.
     * 
     * @param date
     *            date to format
     * @return formatted date
     */
    abstract String formatMonthLevel(LocalDate date);

    /**
     * Format a date for the day level.
     * 
     * @param date
     *            date to format
     * @return formatted date
     */
    abstract String formatDayLevel(LocalDate date);
}
