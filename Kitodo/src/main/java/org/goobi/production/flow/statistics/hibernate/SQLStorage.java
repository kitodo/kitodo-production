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
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;

/**
 * Class provides SQL for storage statistics.
 * 
 * @author Wulf Riebensahm
 */
public class SQLStorage extends SQLGenerator {

    public SQLStorage(Date timeFrom, Date timeTo, TimeUnit timeUnit, List<Integer> ids) {
        // "history.processid overrides the defautl value of prozesseID
        super(timeFrom, timeTo, timeUnit, ids, "history.process_id");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.statistics.hibernate.SQLGenerator#getSQL()
     */
    @Override
    public String getSQL() {
        String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(myTimeFrom, myTimeTo, "timeLimiter");
        String outerWhereClause = "";

        if (outerWhereClauseTimeFrame.length() > 0) {
            outerWhereClause = "WHERE " + outerWhereClauseTimeFrame;
        }

        // inner table -> alias "table_1"
        String innerWhereClause;

        if (myIdsCondition != null) {
            // adding ids to the where clause
            innerWhereClause = "(history.type=" + HistoryTypeEnum.storageDifference.getValue().toString() + ")  AND ("
                    + myIdsCondition + ")";
        } else {
            innerWhereClause = "(history.type=" + HistoryTypeEnum.storageDifference.getValue().toString() + ") ";
        }

        String subQuery = "(SELECT numericValue AS 'storage', " + getIntervallExpression(myTimeUnit, "history.date") + " "
                + "AS 'intervall', history.date AS 'timeLimiter' FROM history WHERE " + innerWhereClause
                + ") AS table_1";

        mySql = "SELECT sum(table_1.storage) AS 'storage', table_1.intervall AS 'intervall' FROM " + subQuery + " "
                + outerWhereClause + " GROUP BY table_1.intervall " + "ORDER BY table_1.timeLimiter";

        return mySql;
    }

}
