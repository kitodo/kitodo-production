/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2013 Goobi. Digitalisieren im Verein e.V. <contact@goobi.org>
 * 
 * Visit the websites for more information.
 *     		- http://www.goobi.org/en/
 *     		- https://github.com/goobi
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination. As a special
 * exception, the copyright holders of this library give you permission to link
 * this library with independent modules to produce an executable, regardless of
 * the license terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is not
 * derived from or based on this library. If you modify this library, you may
 * extend this exception to your version of the library, but you are not obliged
 * to do so. If you do not wish to do so, delete this exception statement from
 * your version.
 */

package org.goobi.production.model.bibliography.course;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.sharkysoft.util.NotImplementedException;

/**
 * The Granularity indicates one out of six options how a course of appearance
 * of a newspaper can be broken into processes. These are as follows:
 * 
 * <dl>
 * <dt>ISSUES</dt>
 * <dd>Each issue is scanned in an individual process.</dd>
 * <dt>DAYS</dt>
 * <dd>All issues of one day are scanned in one process.</dd>
 * <dt>WEEKS</dt>
 * <dd>All issues of a week are scanned in one process. A week starts on
 * Mondays. Keep in mind that week borders do not necessarily match month and
 * not even year borders.</dd>
 * <dt>MONTHS</dt>
 * <dd>All issues of a month are scanned in one process.</dd>
 * <dt>MONTHS</dt>
 * <dd>All issues of a quarter of a year are scanned in one process.</dd>
 * <dt>YEARS</dt>
 * <dd>All issues of a year are scanned in one process.</dd>
 * </dl>
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public enum Granularity {
	ISSUES, DAYS, WEEKS, MONTHS, QUARTERS, YEARS;

	/**
	 * The function format() converts a given LocalDate to a String
	 * representation of the date in the given granularity. For the 1st January
	 * 2000 it will return:
	 * 
	 *   • for DAYS:     2000-01-01
	 *   • for WEEKS:    1999-W52
	 *   • for MONTHS:   2000-01
	 *   • for QUARTERS: 2000/Q1
	 *   • for YEARS:    2000
	 * 
	 * The remaining cases are undefined and will throw NotImplementedException.
	 * 
	 * @param date
	 *            date to format
	 * @return an expression of the date in the given granularity
	 */
	@SuppressWarnings("incomplete-switch")
	public String format(LocalDate date) {
		switch (this) {
		case DAYS:
			return ISODateTimeFormat.date().print(date);
		case MONTHS:
			return ISODateTimeFormat.yearMonth().print(date);
		case QUARTERS:
			return ISODateTimeFormat.year().print(date) + "/Q"
					+ Integer.toString(((date.getMonthOfYear() - 1) / 3) + 1);
		case WEEKS:
			return ISODateTimeFormat.weekyearWeek().print(date);

		case YEARS:
			return ISODateTimeFormat.year().print(date);

		}
		throw new NotImplementedException();
	}
}
