package org.goobi.production.flow.statistics.hibernate;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */

import java.util.Date;
import java.util.List;

import org.goobi.production.flow.statistics.enums.TimeUnit;

import de.sub.goobi.helper.enums.HistoryEventType;

/**
 * Class provides SQL for storage statistics
 * 
 * 
 * @author Wulf Riebensahm
 *
 */
public class SQLStorage extends SQLGenerator {

	public SQLStorage(Date timeFrom, Date timeTo, TimeUnit timeUnit,
			List<Integer> ids) {
		// "history.processid overrides the defautl value of prozesseID
		super(timeFrom, timeTo, timeUnit, ids, "history.processID");
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.SQLGenerator#getSQL()
	 */
	public String getSQL() {

		String subQuery = "";
		String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(
				myTimeFrom, myTimeTo, "timeLimiter");
		String outerWhereClause = "";

		if (outerWhereClauseTimeFrame.length() > 0) {
			outerWhereClause = "WHERE " + outerWhereClauseTimeFrame;
		}

		//inner table -> alias "table_1"
		String innerWhereClause;

		if (myIdsCondition != null) {
			// adding ids to the where clause
			innerWhereClause = "(history.type="
					+ HistoryEventType.storageDifference.getValue().toString()
					+ ")  AND (" + myIdsCondition + ")";
		} else {
			innerWhereClause = "(history.type="
					+ HistoryEventType.storageDifference.getValue().toString()
					+ ") ";
		}

		subQuery = "(SELECT numericvalue AS 'storage', "
				+ getIntervallExpression(myTimeUnit, "history.date")
				+ " "
				+ "AS 'intervall', history.date AS 'timeLimiter' FROM history WHERE "
				+ innerWhereClause + ") AS table_1";

		mySql = "SELECT sum(table_1.storage) AS 'storage', table_1.intervall AS 'intervall' FROM "
				+ subQuery
				+ " "
				+ outerWhereClause
				+ " GROUP BY table_1.intervall "
				+ "ORDER BY table_1.timeLimiter";

		return mySql;
	}

}
