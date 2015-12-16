package org.goobi.production.flow.statistics.hibernate;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- https://github.com/goobi/goobi-production
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
//TODO: Remove redundant code
/************************************************************************
 * 
 * This Class provide methods to prepare SQL Queries needed for production statistics. 
 * It depends on time frame and time unit.
 * 
 * @author Wulf Riebensahm
 * 
 ***********************************************************************/
class SQLProduction extends SQLGenerator {

	public SQLProduction(Date timeFrom, Date timeTo, TimeUnit timeUnit,
			List<Integer> ids) {
		super(timeFrom, timeTo, timeUnit, ids, null);
	}

	/**********************************************************************
	 * get actual SQL Query as String.
	 * 
	 * @return String
	 **********************************************************************/
	@Override
	public String getSQL() {

		String subQuery = "";
		String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(
				myTimeFrom, myTimeTo, "timeLimiter");
		String outerWhereClause = "";

		//inner table -> alias "table_1"
		String innerWhereClause;

		if (myIdsCondition != null) {
			// adding ids to the where clause
			innerWhereClause = "(bearbeitungsende IS NOT NULL AND ("
					+ myIdsCondition + ")) ";
		} else {
			innerWhereClause = "(bearbeitungsende IS NOT NULL) ";
		}
		if (outerWhereClauseTimeFrame.length() > 0) {
			outerWhereClause = "WHERE (" + outerWhereClauseTimeFrame + ") ";
		}

		subQuery = "(SELECT prozesse.prozesseid AS singleProcess, "
				+ "prozesse.sortHelperImages AS pages, "
				+ getIntervallExpression(myTimeUnit, "BearbeitungsEnde")
				+ " "
				+ "AS intervall, BearbeitungsEnde AS timeLimiter "
				+ "FROM  schritte inner join prozesse on schritte.prozesseid=prozesse.prozesseid "
				+ "WHERE " + innerWhereClause
				+ "GROUP BY prozesse.prozesseid) AS table_1";

		mySql = "SELECT count(table_1.singleProcess) AS volumes, "
				+ "sum(table_1.pages) AS pages, table_1.intervall " + "FROM "
				+ subQuery + " " + outerWhereClause + " GROUP BY intervall "
				+ "ORDER BY timeLimiter";

		return mySql;
	}
	
	/* (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.hibernate.SQLGenerator#getSQL(java.lang.Integer)
	 */
	public String getSQL(Integer stepDone) {

		if (stepDone == null) {
			return getSQL();
		}

		String subQuery = "";
		String outerWhereClause = "";
		String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(
				myTimeFrom, myTimeTo, "timeLimiter");

		if (outerWhereClauseTimeFrame.length() > 0) {
			outerWhereClause = "WHERE".concat(outerWhereClauseTimeFrame);
		}

		// inserting the required step to be done from the parameter
		String innerWhereClause = "schritte.reihenfolge=" + stepDone.toString()
				+ " AND " + "schritte.bearbeitungsstatus>2";

		// adding ids to conditions if exist
		if (myIdsCondition != null) {
			innerWhereClause = "WHERE ".concat(innerWhereClause).concat(
					" AND (" + myIdsCondition + ")");
		} else {
			innerWhereClause = "WHERE ".concat(innerWhereClause);
		}

		// building the inner SQL
		subQuery = "(SELECT prozesse.prozesseid AS singleProcess, "
				+ "prozesse.sortHelperImages AS pages, "
				+ getIntervallExpression(myTimeUnit, "BearbeitungsEnde")
				+ " "
				+ "AS intervall, bearbeitungsende AS timeLimiter "
				+ "FROM  schritte inner join prozesse on schritte.prozesseid=prozesse.prozesseid "
				+ innerWhereClause + "GROUP BY prozesse.prozesseid) AS table_1";

		// building complete query
		mySql = "SELECT count(table_1.singleProcess) AS volumes, "
				+ "sum(table_1.pages) AS pages, table_1.intervall " + "FROM "
				+ subQuery + " " + outerWhereClause + " GROUP BY intervall "
				+ "ORDER BY timeLimiter";

		return mySql;
	}
}
