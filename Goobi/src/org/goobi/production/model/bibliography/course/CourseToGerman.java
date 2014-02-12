/**
 * This file is part of the Goobi Application - a Workflow tool for the support
 * of mass digitization.
 * 
 * (c) 2013 Goobi. Digialisieren im Verein e.V. &lt;contact@goobi.org&gt;
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
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 * The static class CourseToGerman provides a toString() method to convert a
 * course of appearance into a verbal description in German language.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class CourseToGerman {
	/**
	 * Days of week’s names in German.
	 * 
	 * <p>
	 * Joda time’s days of week are 1-based, where 1 references monday through 7
	 * references sunday. Therefore the “null” in first place.
	 * </p>
	 */
	private static final String[] DAYS_OF_WEEK_NAMES = new String[] { null, "Montag", "Dienstag", "Mittwoch",
			"Donnerstag", "Freitag", "Samstag", "Sonntag" };

	/**
	 * Month names in German.
	 * 
	 * <p>
	 * Joda time’s months are 1-based, therefore the “null” in first place.
	 * </p>
	 */
	private static final String[] MONTH_NAMES = new String[] { null, "Januar", "Februar", "März", "April", "Mai",
			"Juni", "Juli", "August", "September", "Oktober", "November", "Dezember" };

	/**
	 * Returns a verbal description of the course of appearance in German.
	 * 
	 * @return Verbal description of the course in German
	 * @throws NoSuchElementException
	 *             if the course doesn’t contain a Title
	 * @see org.goobi.production.model.bibliography.course.Course#toString()
	 */
	public static List<String> toString(Course course) {
		List<String> result = new ArrayList<String>();
		Iterator<Title> blocks = course.iterator();
		String previousTitle = null;
		do {
			Title title = blocks.next(); // throws NoSuchElementException if empty. This behaviour is intended
			result.add(titleToString(title, previousTitle));
			for (Issue issue : title.getIssues()) {
				String irregularities = irregularitiesToString(issue);
				if (irregularities != null)
					result.add(irregularities);
			}
			previousTitle = title.getHeading();
		} while (blocks.hasNext());
		return result;
	}

	/**
	 * The method appendTitle() formulates the regular appearance of a Title.
	 * 
	 * @param buffer
	 *            buffer to write to
	 * @param current
	 *            Titel to formulate
	 * @param previousTitle
	 *            previous title (may be null)
	 */
	protected static String titleToString(Title current, String previousTitle) {
		StringBuilder result = new StringBuilder(500);
		if (previousTitle == null) {
			result.append("Die Zeitung „");
			result.append(current.getHeading());
			result.append("“ erschien vom ");
			appendDate(result, current.getFirstAppearance());
		} else {
			result.append("Ab dem ");
			appendDate(result, current.getFirstAppearance());
			result.append(" erschien die Zeitung „");
			result.append(previousTitle);
			result.append("“ unter dem ");
			if (current.getHeading().equals(previousTitle))
				result.append("gleichen Titel");
			else {
				result.append("geänderten Titel „");
				result.append(current.getHeading());
				result.append("“");
			}
		}
		result.append(" bis zum ");
		appendDate(result, current.getLastAppearance());
		result.append(" regelmäßig ");

		Iterator<Issue> issueIterator = current.getIssues().iterator();
		for (int issueIndex = 0; issueIndex < current.getIssues().size(); issueIndex++) {
			Issue issue = issueIterator.next();
			result.append("an allen ");
			int daysOfWeekCount = 0;
			for (int dayOfWeek = DateTimeConstants.MONDAY; dayOfWeek <= DateTimeConstants.SUNDAY; dayOfWeek++) {
				if (issue.isDayOfWeek(dayOfWeek)) {
					result.append(DAYS_OF_WEEK_NAMES[dayOfWeek]);
					result.append("en");
					daysOfWeekCount++;
					if (daysOfWeekCount < issue.getDaysOfWeek().size() - 1)
						result.append(", ");
					if (daysOfWeekCount == issue.getDaysOfWeek().size() - 1)
						result.append(" und ");
				}
			}
			result.append(" als ");
			result.append(issue.getHeading());
			if (issueIndex < current.getIssues().size() - 2)
				result.append(", ");
			if (issueIndex == current.getIssues().size() - 2)
				result.append(" sowie ");
			if (issueIndex == current.getIssues().size() - 1)
				result.append(".");
		}
		return result.toString();
	}

	/**
	 * The method appendIrregularities() formulates the irregularities of the
	 * individual issues.
	 * 
	 * @param issues
	 *            issues whose irregularities shall be formulated
	 */
	protected static String irregularitiesToString(Issue issue) {
		int additionsSize = issue.getAdditions().size();
		int exclusionsSize = issue.getExclusions().size();
		StringBuilder buffer = new StringBuilder((int) (Math.ceil(32.763 * (additionsSize + exclusionsSize)) + 100));

		if (additionsSize == 0 && exclusionsSize == 0)
			return null;

		buffer.append("\n\nDie Ausgabe „");
		buffer.append(issue.getHeading());
		buffer.append("“ erschien ");

		if (exclusionsSize > 0) {
			buffer.append("nicht ");
			Iterator<LocalDate> exclusionIterator = issue.getExclusions().iterator();
			for (int exclusionIndex = 0; exclusionIndex < exclusionsSize; exclusionIndex++) {
				LocalDate exclusion = exclusionIterator.next();
				prependDayOfWeek(buffer, exclusion);
				appendDate(buffer, exclusion);
				if (exclusionIndex < exclusionsSize - 2)
					buffer.append(", ");
				if (exclusionIndex == exclusionsSize - 2)
					buffer.append(" sowie ");
			}

			if (additionsSize > 0)
				buffer.append(", dafür jedoch ");
		}

		if (additionsSize > 0) {
			buffer.append("zusätzlich ");
			Iterator<LocalDate> additionsIterator = issue.getAdditions().iterator();
			for (int additionIndex = 0; additionIndex < additionsSize; additionIndex++) {
				LocalDate addition = additionsIterator.next();
				prependDayOfWeek(buffer, addition);
				appendDate(buffer, addition);
				if (additionIndex < additionsSize - 2)
					buffer.append(", ");
				if (additionIndex == additionsSize - 2)
					buffer.append(" sowie ");
			}
		}
		buffer.append(".");

		return buffer.toString();
	}

	/**
	 * The method appendDate() writes a date to the buffer.
	 * 
	 * @param buffer
	 *            Buffer to write to
	 * @param date
	 *            Date to write
	 */
	protected static void appendDate(StringBuilder buffer, LocalDate date) {
		buffer.append(date.getDayOfMonth());
		buffer.append(". ");
		buffer.append(MONTH_NAMES[date.getMonthOfYear()]);
		buffer.append(' ');
		buffer.append(date.getYear());
		return;
	}

	/**
	 * The method prependDayOfWeek writes a day of week in prepended notation to
	 * the buffer.
	 * 
	 * @param buffer
	 *            Buffer to write to
	 * @param date
	 *            Date whose day of week is to write
	 */
	protected static void prependDayOfWeek(StringBuilder buffer, LocalDate date) {
		buffer.append("am ");
		buffer.append(DAYS_OF_WEEK_NAMES[date.getDayOfWeek()]);
		buffer.append(", den ");
		return;
	}

	/**
	 * The method postpendDayOfWeek writes a day of week in postpended notation
	 * to the buffer.
	 * 
	 * @param buffer
	 *            Buffer to write to
	 * @param date
	 *            Date whose day of week is to write
	 */
	protected static void postpendDayOfWeek(StringBuilder buffer, LocalDate date) {
		buffer.append(", einem ");
		buffer.append(DAYS_OF_WEEK_NAMES[date.getDayOfWeek()]);
		return;
	}
}
