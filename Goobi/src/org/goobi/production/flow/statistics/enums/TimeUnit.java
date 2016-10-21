package org.goobi.production.flow.statistics.enums;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 *
 * Visit the websites for more information.
 *     		- http://www.kitodo.org
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.sub.goobi.helper.Helper;

/**
 * Enum of all time units for the statistics
 *
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 ****************************************************************************/
public enum TimeUnit {


	days("1", "days", "day", "day", true, 1.0),
	weeks("2", "weeks", "week", "week", true, 5.0),
	months("3", "months", "month", "month", true, 21.3 ),
	quarters("4", "quarters","quarter", "quarter", true, 64.0 ),
	years("5", "years", "year", "year", true, 256.0);

	private String id;
	private String title;
	private String sqlKeyword;
	private String singularTitle;
	private boolean visible;
	private Double dayFactor;

	/**
	 * private constructor for setting id and title
	 *
	 * @param inTitle
	 *            title as String
	 ****************************************************************************/
	private TimeUnit(String inId, String inTitle, String inKeyword, String inSingularTitle, Boolean visible, Double dayFactor) {
		id = inId;
		title = inTitle;
		singularTitle = inSingularTitle;
		sqlKeyword = inKeyword;
		this.visible = visible;
		this.dayFactor = dayFactor;
	}

	/**
	 * return unique ID for TimeUnit
	 *
	 * @return unique ID as String
	 ****************************************************************************/
	public String getId() {
		return id;
	}

	/**
	 *
	 * @return sqlKeyword for use in querys
	 */
	public String getSqlKeyword() {
		return sqlKeyword;
	}

	/**
	 * return singular name for timeUnit
	 *
	 * @return singularTitle
	 ****************************************************************************/
	public String getSingularTitle() {
		return singularTitle;
	}

	/**
	 * return localized title for TimeUnit from standard-jsf-messages-files
	 *
	 * @return localized title
	 ****************************************************************************/
	public String getTitle() {
		return Helper.getTranslation(title);
	}

	/**
	 * return the internal String representing the Title, use this for localisation
	 *
	 * @return  the internal title
	 ****************************************************************************/
	@Override
	public String toString() {
		return title;
	}

	/**
	 * get TimeUnit by unique ID
	 *
	 * @param inId
	 *            the unique ID
	 * @return {@link TimeUnit} with given ID
	 ****************************************************************************/
	public static TimeUnit getById(String inId) {
		for (TimeUnit unit : TimeUnit.values()) {
			if (unit.getId().equals(inId)) {
				return unit;
			}
		}
		return days;
	}

	public static List<TimeUnit> getAllVisibleValues() {
		ArrayList<TimeUnit> mylist = new ArrayList<TimeUnit>();
		for (TimeUnit tu : TimeUnit.values()) {
			if (tu.visible) {
				mylist.add(tu);
			}
		}
		return mylist;
	}

	/**
	 *
	 * @return a day factor for the selected time unit based on an average year of 365.25 days
	 */
	public Double getDayFactor(){
		return this.dayFactor;
	}


	/**
	 * function allows to retrieve a date row based on start and end date
	 *
	 * @param start
	 * @param end
	 * @return date row
	 */
	public List<String> getDateRow(Date start, Date end){
		List<String> dateRow = new ArrayList<String>();

		Date nextDate = start;

		while(nextDate.before(end)){
			dateRow.add(getTimeFormat(nextDate));
			nextDate = getNextDate(nextDate);
		}

		return dateRow;
	}

	@SuppressWarnings("deprecation")
	private String getTimeFormat(Date inDate) {

		switch (this){

		case days:
		case months:
		case weeks:
		case years:
			return new DateTime(inDate).toString(getFormatter());

		case quarters:
			return new DateTime(inDate).toString(getFormatter()) + "/" +
			//TODO: Remove use of deprecated method
			Integer.toString((inDate.getMonth() - 1)/3+1);
		}
		return inDate.toString();

	}

	private Date getNextDate(Date inDate){

		switch (this){

		case days:
			return new DateTime(inDate).plusDays(1).toDate();

		case months:
			return new DateTime(inDate).plusMonths(1).toDate();

		case quarters:
			return new DateTime(inDate).plusMonths(3).toDate();

		case weeks:
			return new DateTime(inDate).plusWeeks(1).toDate();

		case years:
			return new DateTime(inDate).plusYears(1).toDate();
		}
		return inDate;
	}

	private DateTimeFormatter getFormatter(){

		switch (this){

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
		}
		return null;
	}

}
