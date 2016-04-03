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
package de.sub.goobi.helper;

import java.util.TreeSet;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The class DateUtils contains an omnium-gatherum of functions that work on
 * calendar dates. All functionality is realized using the org.joda.time.*
 * library.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class DateUtils {
	/**
	 * The field DATE_FORMATTER provides a DateTimeFormatter that is used to
	 * convert between LocalDate objects and String in common German notation.
	 */
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd.MM.yyyy");

	/**
	 * The function lastMonthForYear() returns from an ordered set of dates for
	 * a given year the last month which a date can be found for in that year.
	 *
	 * <p>
	 * Example: Let the ordered data set contain: …, 5th May 1954, 20th May
	 * 1954, 13th October 1954, 5th February 1955, 23th March 1955, 15th October
	 * 1956, …. Then the function will return 10 (DateTimeConstants.OCTOBER) for
	 * 1954 and 3 (DateTimeConstants.MARCH) for 1955.
	 * </p>
	 *
	 * @param data
	 *            an ordered set of dates
	 * @param year
	 *            year in question
	 * @return the last month which can be found up to the end of that year
	 */
	public static int lastMonthForYear(TreeSet<LocalDate> data, int year) {
		return data.headSet(new LocalDate(year, DateTimeConstants.DECEMBER, 31), true).last().getMonthOfYear();
	}

	/**
	 * The function sameMonth() compares two LocalDate objects in regard to the
	 * question whether their two dates reside in the same month of the calendar
	 * system presumed. Two dates are considered to be in the same month exactly
	 * if both their year and month of year fields are equal.
	 *
	 * @param compared
	 *            date to compare against
	 * @param comparee
	 *            date to compare, may be null
	 * @return whether the two dates are in the same month
	 */
	public static boolean sameMonth(LocalDate compared, LocalDate comparee) {
		if (!sameYear(compared, comparee))
			return false;
		return compared.getMonthOfYear() == comparee.getMonthOfYear();
	}

	/**
	 * The function sameYear() compares two LocalDate objects in regard to the
	 * question whether their two dates reside in the same year of the calendar
	 * system presumed. Two dates are considered to be in the same year exactly
	 * if none of them is null and their year fields are equal.
	 *
	 * @param current
	 *            date to compare against
	 * @param next
	 *            date to compare, may be null
	 * @return whether the two dates are in the same year
	 */
	public static boolean sameYear(LocalDate current, LocalDate next) {
		if (next == null)
			return false;
		return current.getYear() == next.getYear();
	}
}
