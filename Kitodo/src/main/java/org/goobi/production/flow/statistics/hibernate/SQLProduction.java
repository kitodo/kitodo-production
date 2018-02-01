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

// TODO: Remove redundant code

/**
 * This Class provide methods to prepare SQL Queries needed for production
 * statistics. It depends on time frame and time unit.
 *
 * @author Wulf Riebensahm
 */
class SQLProduction extends SQLGenerator {

    public SQLProduction(Date timeFrom, Date timeTo, TimeUnit timeUnit, List<Integer> ids) {
        super(timeFrom, timeTo, timeUnit, ids, null);
    }

    /**
     * Get actual SQL Query as String.
     *
     * @return String
     */
    @Override
    public String getSQL() {
        String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(myTimeFrom, myTimeTo, "timeLimiter");
        String outerWhereClause = "";

        // inner table -> alias "table_1"
        String innerWhereClause;

        if (myIdsCondition != null) {
            // adding ids to the where clause
            innerWhereClause = "(processingEnd IS NOT NULL AND (" + myIdsCondition + ")) ";
        } else {
            innerWhereClause = "(processingEnd IS NOT NULL) ";
        }
        if (outerWhereClauseTimeFrame.length() > 0) {
            outerWhereClause = "WHERE (" + outerWhereClauseTimeFrame + ") ";
        }

        String subQuery = "(SELECT process.id AS singleProcess, " + "process.sortHelperImages AS pages, "
                + getIntervallExpression(myTimeUnit, "processingEnd") + " AS intervall, processingEnd AS timeLimiter "
                + "FROM task inner join process on task.process_id=process.id " + "WHERE " + innerWhereClause
                + "GROUP BY process.id) AS table_1";

        mySql = "SELECT count(table_1.singleProcess) AS volumes, " + "sum(table_1.pages) AS pages, table_1.intervall "
                + "FROM " + subQuery + " " + outerWhereClause + " GROUP BY intervall " + "ORDER BY timeLimiter";

        return mySql;
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
        String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(myTimeFrom, myTimeTo, "timeLimiter");

        if (outerWhereClauseTimeFrame.length() > 0) {
            outerWhereClause = "WHERE".concat(outerWhereClauseTimeFrame);
        }

        // inserting the required step to be done from the parameter
        String innerWhereClause = "task.ordering=" + stepDone.toString() + " AND " + "task.processingStatus>2";

        // adding ids to conditions if exist
        if (myIdsCondition != null) {
            innerWhereClause = "WHERE ".concat(innerWhereClause).concat(" AND (" + myIdsCondition + ")");
        } else {
            innerWhereClause = "WHERE ".concat(innerWhereClause);
        }

        // building the inner SQL
        String subQuery = "(SELECT process.id AS singleProcess, " + "process.sortHelperImages AS pages, "
                + getIntervallExpression(myTimeUnit, "processingEnd") + " AS intervall, processingEnd AS timeLimiter "
                + "FROM task inner join process on task.process_id=process.id " + innerWhereClause
                + "GROUP BY process.id) AS table_1";

        // building complete query
        mySql = "SELECT count(table_1.singleProcess) AS volumes, " + "sum(table_1.pages) AS pages, table_1.intervall "
                + "FROM " + subQuery + " " + outerWhereClause + " GROUP BY intervall " + "ORDER BY timeLimiter";

        return mySql;
    }
}
