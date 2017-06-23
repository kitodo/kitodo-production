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

package org.goobi.production.flow.statistics.hibernate;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.goobi.production.flow.statistics.enums.TimeUnit;

/**
 * This is the superclass for SQL generation and it provides some common data
 * collection in the constructor and abstract methods which needs to be
 * implemented in the sub classes.
 *
 * @author Wulf Riebensahm
 */

public abstract class SQLGenerator {

    String mySql = null;
    Date myTimeFrom = null;
    Date myTimeTo = null;
    TimeUnit myTimeUnit = null;
    String myIdsCondition = null;
    String myIdFieldName = "process.id";
    List<Integer> ids;

    private SQLGenerator() {
        super();
    }

    public SQLGenerator(Date timeFrom, Date timeTo, TimeUnit timeUnit, List<Integer> ids, String idFieldName) {
        this();
        myTimeFrom = timeFrom;
        myTimeTo = timeTo;
        myTimeUnit = timeUnit;
        this.ids = ids;
        if (idFieldName != null) {
            this.myIdFieldName = idFieldName;
        }

        // if ids are passed on build a where clause about ids
        // this will be a condition of the inner table
        conditionGeneration();

    }

    private void conditionGeneration() {
        if (ids != null) {
            myIdsCondition = myIdFieldName + " in (";
            for (Integer i : ids) {
                myIdsCondition = myIdsCondition.concat(i.toString() + ",");
            }
            myIdsCondition = myIdsCondition.substring(0, myIdsCondition.length() - ",".length()) + ")";
        }
    }

    /**
     * Get actual SQL Query as String. Depends on the done step of process.
     *
     * @return String - SQL Query as String
     */
    public abstract String getSQL();

    /**
     * Generates SQL-WHERE for the time frame.
     *
     * @param timeFrom
     *            start time
     * @param timeTo
     *            end time
     * @param timeLimiter
     *            name of field used to apply the timeframe
     */
    protected static String getWhereClauseForTimeFrame(Date timeFrom, Date timeTo, String timeLimiter) {

        if (timeFrom == null && timeTo == null) {
            return "";
        }

        if (timeFrom != null && timeTo != null) {
            return " date_format(" + timeLimiter + ",'%Y%m%d%H%i%s')+0>=date_format('" + dateToSqlTimestamp(timeFrom)
                    + "','%Y%m%d%H%i%s')+0 AND " + " date_format(" + timeLimiter + ",'%Y%m%d%H%i%s')+0<=date_format('"
                    + dateToSqlTimestamp(timeTo) + "','%Y%m%d%H%i%s')+0 ";
        }

        if (timeFrom != null) {
            return " date_format(" + timeLimiter + ",'%Y%m%d%H%i%s')+0>=date_format('" + dateToSqlTimestamp(timeFrom)
                    + "','%Y%m%d%H%i%s')+0";
        }

        return " date_format(" + timeLimiter + ",'%Y%m%d%H%i%s')+0<=date_format('" + dateToSqlTimestamp(timeTo)
                + "','%Y%m%d%H%i%s')+0";
    }

    /**
     * Generates time format from {@link TimeUnit}.
     *
     * @param fieldExpression
     * @param timeUnit
     * @return String - simple date format
     */
    protected static String getIntervallExpression(TimeUnit timeUnit, String fieldExpression) {

        if (timeUnit == null) {
            return "'Total'";
        }

        switch (timeUnit) {
            case years:
                return "year(" + fieldExpression + ")";
            case months:
                return "concat(year(" + fieldExpression + ") , '/' , date_format(" + fieldExpression + ",'%m'))";
            case quarters:
                return "concat(year(" + fieldExpression + ") , '/' , quarter(" + fieldExpression + "))";
            case weeks:
                return "concat(left(yearweek(" + fieldExpression + ",3),4), '/', right(yearweek(" + fieldExpression
                        + ",3),2))";
            case days:
                return "concat(year(" + fieldExpression + ") , '-' , date_format(" + fieldExpression
                        + ",'%m') , '-' , date_format(" + fieldExpression + ",'%d'))";
            default:
                return "'timeUnit(" + timeUnit.getTitle() + ") undefined'";
        }
    }

    /**
     * Converts the format of a date to match MySQL Timestamp format.
     *
     * @param date
     *            as Date
     * @return time stamp
     */
    private static Timestamp dateToSqlTimestamp(Date date) {
        return new Timestamp(date.getTime());
    }

    public void setMyIdFieldName(String name) {
        myIdFieldName = name;
        conditionGeneration();
    }
}
