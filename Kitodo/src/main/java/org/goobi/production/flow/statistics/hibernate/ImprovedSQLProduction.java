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

import java.util.Date;
import java.util.List;

import org.goobi.production.flow.statistics.enums.TimeUnit;

/**
 * This Class provide methods to prepare SQL Queries needed for production
 * statistics. It depends on time frame and time unit.
 *
 * @author Robert Sehr
 */
class ImprovedSQLProduction extends SQLGenerator {

    public ImprovedSQLProduction(Date timeFrom, Date timeTo, TimeUnit timeUnit, List<Integer> ids) {
        super(timeFrom, timeTo, timeUnit, ids, "h.process_id");
    }

    /**
     * Get actual SQL Query as String.
     *
     * @return String
     */
    @Override
    public String getSQL() {
        super.setMyIdFieldName("process.id");
        String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(this.myTimeFrom, this.myTimeTo, "timeLimiter");
        String outerWhereClause = "";

        // inner table -> alias "table_1"
        String innerWhereClause;

        if (this.myIdsCondition != null) {
            // adding ids to the where clause
            innerWhereClause = "(processingEnd IS NOT NULL AND (" + this.myIdsCondition + ")) ";
        } else {
            innerWhereClause = "(processingEnd IS NOT NULL) ";
        }
        if (outerWhereClauseTimeFrame.length() > 0) {
            outerWhereClause = "WHERE (" + outerWhereClauseTimeFrame + ") ";
        }

        String subQuery = "(SELECT process.id AS singleProcess, " + "process.sortHelperImages AS pages, "
                + getIntervallExpression(this.myTimeUnit, "processingEnd")
                + " AS intervall, processingEnd AS timeLimiter "
                + "FROM task inner join process on task.process_id=process.id " + "WHERE " + innerWhereClause
                + "GROUP BY process.id) AS table_1";

        this.mySql = "SELECT count(table_1.singleProcess) AS volumes, "
                + "sum(table_1.pages) AS pages, table_1.intervall " + "FROM " + subQuery + " " + outerWhereClause
                + " GROUP BY intervall " + "ORDER BY timeLimiter";

        return this.mySql;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.statistics.hibernate.SQLGenerator#getSQL(java.
     * lang.Integer)
     */
    public String getSQL(Integer stepDone) {

        if (stepDone == null) {
            return getSQL();
        }

        String outerWhereClause = "";
        String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(this.myTimeFrom, this.myTimeTo, "timeLimiter");

        if (outerWhereClauseTimeFrame.length() > 0) {
            outerWhereClause = "WHERE".concat(outerWhereClauseTimeFrame);
        }

        // inserting the required step to be done from the parameter
        String innerWhereClause = " h.type=6 AND h.numericValue=" + stepDone.toString();

        // adding ids to conditions if exist
        if (this.myIdsCondition != null) {
            innerWhereClause = "WHERE ".concat(innerWhereClause).concat(" AND (" + this.myIdsCondition + ")");
        } else {
            innerWhereClause = "WHERE ".concat(innerWhereClause);
        }

        // building the inner SQL
        String subQuery = "(SELECT table1.id AS singleProcess, table1.sortHelperImages AS pages, h.date AS timeLimiter, "
                + getIntervallExpression(this.myTimeUnit, "h.date") + "  AS intervall from history h "
                + " JOIN process AS table1 ON  h.process_id=table1.id  " + innerWhereClause
                + "GROUP BY h.process_id order by h.date) AS table_1";

        // building complete query
        this.mySql = "SELECT count(table_1.singleProcess ) AS volumes , sum(table_1.pages) AS pages, table_1.intervall FROM "
                + subQuery + " " + outerWhereClause + " GROUP BY intervall ";
        return this.mySql;
    }

    public String getSQL(String stepname) {
        String outerWhereClause = "";
        String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(this.myTimeFrom, this.myTimeTo, "timeLimiter");

        if (outerWhereClauseTimeFrame.length() > 0) {
            outerWhereClause = "WHERE".concat(outerWhereClauseTimeFrame);
        }

        // inserting the required step to be done from the parameter
        String innerWhereClause = " h.type=6 AND h.stringvalue like '%" + stepname + "%' ";

        // adding ids to conditions if exist
        if (this.myIdsCondition != null) {
            innerWhereClause = "WHERE ".concat(innerWhereClause).concat(" AND " + this.myIdsCondition);
        } else {
            innerWhereClause = "WHERE ".concat(innerWhereClause);
        }

        // building the inner SQL
        String subQuery = "(SELECT table1.id AS singleProcess, table1.sortHelperImages AS pages, h.date AS timeLimiter, "
                + getIntervallExpression(this.myTimeUnit, "h.date") + "  AS intervall from history h "
                + " JOIN process AS table1 ON  h.process_id=table1.id  " + innerWhereClause
                + " GROUP BY h.processID order by h.date) AS table_1";

        // building complete query
        this.mySql = "SELECT count(table_1.singleProcess ) AS volumes , sum(table_1.pages) AS pages, table_1.intervall FROM "
                + subQuery + " " + outerWhereClause + " GROUP BY intervall ";
        return this.mySql;
    }
}
