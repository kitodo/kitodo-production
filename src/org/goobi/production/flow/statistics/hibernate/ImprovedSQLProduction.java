/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.flow.statistics.hibernate;
import java.util.Date;
import java.util.List;

import org.goobi.production.flow.statistics.enums.TimeUnit;
/************************************************************************
 * 
 * This Class provide methods to prepare SQL Queries needed for production statistics. 
 * It depends on time frame and time unit.
 * 
 * @author Robert Sehr
 * 
 ***********************************************************************/
class ImprovedSQLProduction extends SQLGenerator {

	public ImprovedSQLProduction(Date timeFrom, Date timeTo, TimeUnit timeUnit,
			List<Integer> ids) {
		super(timeFrom, timeTo, timeUnit, ids, "h.processID");
	}

	/**********************************************************************
	 * get actual SQL Query as String.
	 * 
	 * @return String
	 **********************************************************************/
	public String getSQL() {
		

		super.setMyIdFieldName("prozesse.prozesseid");
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
		String innerWhereClause = " h.type=6 AND h.numericvalue=" + stepDone.toString();

		// adding ids to conditions if exist
		if (myIdsCondition != null) {
			innerWhereClause = "WHERE ".concat(innerWhereClause).concat(
					" AND (" + myIdsCondition + ")");
		} else {
			innerWhereClause = "WHERE ".concat(innerWhereClause);
		}
		
		// building the inner SQL
		subQuery = "(SELECT table1.prozesseid AS singleProcess, table1.sortHelperImages AS pages, h.date AS timeLimiter, "
				+ getIntervallExpression(myTimeUnit, "h.date")
				+ "  AS intervall from history h "
				+ " JOIN prozesse AS table1 ON  h.processID=table1.prozesseid  "
				+ innerWhereClause + "GROUP BY h.processID order by h.date) AS table_1";

		// building complete query
		mySql = "SELECT count(table_1.singleProcess ) AS volumes , sum(table_1.pages) AS pages, table_1.intervall FROM  "
				+ subQuery + " " + outerWhereClause + " GROUP BY intervall ";
		return mySql;
	}

	public String getSQL(String stepname) {
		String subQuery = "";
		String outerWhereClause = "";
		String outerWhereClauseTimeFrame = getWhereClauseForTimeFrame(
				myTimeFrom, myTimeTo, "timeLimiter");

		if (outerWhereClauseTimeFrame.length() > 0) {
			outerWhereClause = "WHERE".concat(outerWhereClauseTimeFrame);
		}
		
		// inserting the required step to be done from the parameter
		String innerWhereClause = " h.type=6 AND h.stringvalue like '%" + stepname + "%' ";

		// adding ids to conditions if exist
		if (myIdsCondition != null) {
			innerWhereClause = "WHERE ".concat(innerWhereClause).concat(
					" AND " + myIdsCondition );
		} else {
			innerWhereClause = "WHERE ".concat(innerWhereClause);
		}
		
		// building the inner SQL
		subQuery = "(SELECT table1.prozesseid AS singleProcess, table1.sortHelperImages AS pages, h.date AS timeLimiter, "
				+ getIntervallExpression(myTimeUnit, "h.date")
				+ "  AS intervall from history h "
				+ " JOIN prozesse AS table1 ON  h.processID=table1.prozesseid  "
				+ innerWhereClause + " GROUP BY h.processID order by h.date) AS table_1";

		// building complete query
		mySql = "SELECT count(table_1.singleProcess ) AS volumes , sum(table_1.pages) AS pages, table_1.intervall FROM  "
				+ subQuery + " " + outerWhereClause + " GROUP BY intervall ";
		return mySql;
	}
}
