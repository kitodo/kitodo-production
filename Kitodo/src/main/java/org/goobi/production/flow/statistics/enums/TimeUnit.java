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

package org.goobi.production.flow.statistics.enums;

import de.sub.goobi.helper.Helper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Enum of all time units for the statistics
 *
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 */
public enum TimeUnit {

    days("1", "days", "day", "day", true, 1.0),
    weeks("2", "weeks", "week", "week", true, 5.0),
    months("3", "months", "month", "month", true, 21.3),
    quarters("4", "quarters", "quarter", "quarter", true, 64.0),
    years("5", "years", "year", "year", true, 256.0);

    private String id;
    private String title;
    private String sqlKeyword;
    private String singularTitle;
    private boolean visible;
    private Double dayFactor;

    /**
     * private constructor for setting id and title.
     *
     * @param inTitle
     *            title as String
     */
    TimeUnit(String inId, String inTitle, String inKeyword, String inSingularTitle, Boolean visible, Double dayFactor) {
        id = inId;
        title = inTitle;
        singularTitle = inSingularTitle;
        sqlKeyword = inKeyword;
        this.visible = visible;
        this.dayFactor = dayFactor;
    }

    /**
     * return unique ID for TimeUnit.
     *
     * @return unique ID as String
     */
    public String getId() {
        return id;
    }

    /**
     * Get SQL keyword.
     *
     * @return sqlKeyword for use in querys
     */
    public String getSqlKeyword() {
        return sqlKeyword;
    }

    /**
     * return singular name for timeUnit.
     *
     * @return singularTitle
     */
    public String getSingularTitle() {
        return singularTitle;
    }

    /**
     * return localized title for TimeUnit from standard-jsf-messages-files.
     *
     * @return localized title
     */
    public String getTitle() {
        return Helper.getTranslation(title);
    }

    /**
     * return the internal String representing the Title, use this for
     * localisation.
     *
     * @return the internal title
     */
    @Override
    public String toString() {
        return title;
    }

    /**
     * get TimeUnit by unique ID.
     *
     * @param inId
     *            the unique ID
     * @return {@link TimeUnit} with given ID
     */
    public static TimeUnit getById(String inId) {
        for (TimeUnit unit : TimeUnit.values()) {
            if (unit.getId().equals(inId)) {
                return unit;
            }
        }
        return days;
    }

    /**
     * Get all visible values.
     *
     * @return list of TimeUnit objects
     */
    public static List<TimeUnit> getAllVisibleValues() {
        ArrayList<TimeUnit> list = new ArrayList<>();
        for (TimeUnit tu : TimeUnit.values()) {
            if (tu.visible) {
                list.add(tu);
            }
        }
        return list;
    }

    /**
     *
     * @return a day factor for the selected time unit based on an average year
     *         of 365.25 days
     */
    public Double getDayFactor() {
        return this.dayFactor;
    }

    /**
     * function allows to retrieve a date row based on start and end date.
     *
     * @param start
     *            Date
     * @param end
     *            Date
     * @return date row
     */
    public List<String> getDateRow(Date start, Date end) {
        List<String> dateRow = new ArrayList<>();
        Date nextDate = start;

        while (nextDate.before(end)) {
            dateRow.add(getTimeFormat(nextDate));
            nextDate = getNextDate(nextDate);
        }

        return dateRow;
    }

    private String getTimeFormat(Date date) {
        switch (this) {
            case days:
            case months:
            case weeks:
            case years:
                return new DateTime(date).toString(getFormatter());
            case quarters:
                LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return new DateTime(date).toString(getFormatter()) + "/"
                        + Integer.toString((localDate.getMonthValue() - 2) / 3 + 1);
            default:
                return date.toString();
        }
    }

    private Date getNextDate(Date date) {
        switch (this) {
            case days:
                return new DateTime(date).plusDays(1).toDate();
            case months:
                return new DateTime(date).plusMonths(1).toDate();
            case quarters:
                return new DateTime(date).plusMonths(3).toDate();
            case weeks:
                return new DateTime(date).plusWeeks(1).toDate();
            case years:
                return new DateTime(date).plusYears(1).toDate();
            default:
                return date;
        }
    }

    private DateTimeFormatter getFormatter() {
        switch (this) {
            case days:
                return DateTimeFormat.forPattern("yyyy-MM-dd");
            case months:
                return DateTimeFormat.forPattern("yyyy/MM");
            case weeks:
                return DateTimeFormat.forPattern("yyyy/ww");
            case years:
                return DateTimeFormat.forPattern("yyyy");
            case quarters:
                // has to be extended by the calling function
                return DateTimeFormat.forPattern("yyyy");
            default:
                return null;
        }
    }

}
