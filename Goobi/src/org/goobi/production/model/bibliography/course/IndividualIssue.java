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

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.sharkysoft.util.UnreachableCodeException;

/**
 * The class IndividualIssue represents a stamping of an Issue, that is one
 * distinguishable physically appeared issue (meanwhile the class Issue
 * represents the <em>type</em> of issue).
 * 
 * <p>
 * The class IndividualIssue is final.
 * </p>
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class IndividualIssue {
	/**
	 * The constant DAY holds a DateTimeFormatter used to get the a two-digit
	 * day (01—31) from the newspaper’s date.
	 */
	private static final DateTimeFormatter DAY = DateTimeFormat.forPattern("dd");

	/**
	 * The constant MONTH holds a DateTimeFormatter used to get the a two-digit
	 * month (01—12) from the newspaper’s date.
	 */
	private static final DateTimeFormatter MONTH = DateTimeFormat.forPattern("MM");

	/**
	 * The constant YEAR2 holds a DateTimeFormatter used to get the a four-digit
	 * year of era (00—99, always positive) from the newspaper’s date.
	 */
	private static final DateTimeFormatter YEAR2 = DateTimeFormat.forPattern("YY");

	/**
	 * The constant YEAR4 holds a DateTimeFormatter used to get the a four-digit
	 * year of era (0001—9999, always positive) from the newspaper’s date.
	 */
	private static final DateTimeFormatter YEAR4 = DateTimeFormat.forPattern("YYYY");

	/**
	 * Date of this issue
	 */
	protected final LocalDate date;

	/**
	 * The issue this is an issue from
	 */
	protected final Issue issue;

	/**
	 * Title block that the issue this is an issue from is in
	 */
	protected final Title title;

	/**
	 * Constructor to create an IndividualIssue
	 * 
	 * @param title
	 *            Title block this issue is in
	 * @param issue
	 *            Issue type that this issue is of
	 * @param date
	 *            Date of appearance
	 */
	IndividualIssue(Title title, Issue issue, LocalDate date) {
		this.title = title;
		this.issue = issue;
		this.date = date;
	}

	/**
	 * Returns an integer which, for a given Granularity, shall indicate for two
	 * neighbouring individual issues whether they form the same process (break
	 * mark is equal) or to different processes (break mark differs).
	 * 
	 * @param mode
	 *            how the course shall be broken into processes
	 * @return an int which differs if two neighbouring individual issues belong
	 *         to different processes
	 */
	public int getBreakMark(Granularity mode) {
		final int prime = 31;
		switch (mode) {
		case ISSUES:
			return this.hashCode();
		case DAYS:
			return date.hashCode();
		case WEEKS:
			return prime * date.getYear() + date.getWeekOfWeekyear();
		case MONTHS:
			return prime * date.getYear() + date.getMonthOfYear();
		case QUARTERS:
			return prime * date.getYear() + (date.getMonthOfYear() - 1) / 3;
		case YEARS:
			return date.getYear();
		default:
			throw new UnreachableCodeException("default case in complete switch statement");
		}
	}

	/**
	 * The function getDate() returns the date of this issue.
	 * 
	 * @return the date of this issue
	 */
	public LocalDate getDate() {
		return date;
	}

	/**
	 * The function getGenericFields() returns a map with generic fields that
	 * can be configured for process title creation in goobi_projects.xml. It
	 * provides the issue information in the following fields:
	 * 
	 * <dl>
	 * <dt><code>#DAY</code></dt>
	 * <dd>two-digit day of month</dd>
	 * <dt><code>#Issue</code></dt>
	 * <dd>issue name</dd>
	 * <dt><code>#MONTH</code></dt>
	 * <dd>two-digit month of year</dd>
	 * <dt><code>#YEAR</code></dt>
	 * <dd>four-digit year</dd>
	 * 
	 * </dl>
	 * <p>
	 * In addition, the following abbreviated fields are provided:
	 * </p>
	 * 
	 * <dl>
	 * <dt><code>#i</code></dt>
	 * <dd>first letter of issue name in lower case</dd>
	 * <dt><code>#I</code></dt>
	 * <dd>first letter of issue name in upper case</dd>
	 * <dt><code>#is</code></dt>
	 * <dd>first two letters of issue name in lower case</dd>
	 * <dt><code>#IS</code></dt>
	 * <dd>first two letters of issue name in upper case</dd>
	 * <dt><code>#iss</code></dt>
	 * <dd>first three letters of issue name in lower case</dd>
	 * <dt><code>#ISS</code></dt>
	 * <dd>first three letters of issue name in upper case</dd>
	 * <dt><code>#issu</code></dt>
	 * <dd>first four letters of issue name in lower case</dd>
	 * <dt><code>#ISSU</code></dt>
	 * <dd>first four letters of issue name in upper case</dd>
	 * <dt><code>#YR</code></dt>
	 * <dd>two-digit year of century</dd>
	 * </dl>
	 * 
	 * @return the generic fields for process title creation
	 */
	public Map<String, String> getGenericFields() {
		Map<String, String> result = new HashMap<String, String>(18);
		String heading = issue.getHeading();
		int length = heading.length();
		String upperCase = heading.toUpperCase();
		String lowerCase = heading.toLowerCase();
		result.put("#DAY", DAY.print(date));
		result.put("#I", length > 1 ? upperCase.substring(0, 1) : upperCase);
		result.put("#i", length > 1 ? lowerCase.substring(0, 1) : lowerCase);
		result.put("#IS", length > 2 ? upperCase.substring(0, 2) : upperCase);
		result.put("#is", length > 2 ? lowerCase.substring(0, 2) : lowerCase);
		result.put("#ISS", length > 3 ? upperCase.substring(0, 3) : upperCase);
		result.put("#iss", length > 3 ? lowerCase.substring(0, 3) : lowerCase);
		result.put("#ISSU", length > 4 ? upperCase.substring(0, 4) : upperCase);
		result.put("#issu", length > 4 ? lowerCase.substring(0, 4) : lowerCase);
		result.put("#Issue", heading);
		result.put("#MONTH", MONTH.print(date));
		result.put("#YEAR", YEAR4.print(date));
		result.put("#YR", YEAR2.print(date));
		return result;
	}

	/**
	 * The function getHeading() returns the name of the issue this is an issue
	 * from.
	 * 
	 * @return the issue’s name
	 */
	public String getHeading() {
		return issue.getHeading();
	}

	/**
	 * The function indexIn() returns the index of the first occurrence of the
	 * title of this issue in the given course, or -1 if the course does not
	 * contain the element.
	 * 
	 * @param course
	 *            course to find the title in
	 * @return the index of the first occurrence of the title of this issue in
	 *         the course, or -1 if the course does not contain the element
	 */
	int indexIn(Course course) {
		return course.indexOf(title);
	}

	/**
	 * The function toString() provides returns a string that contains a concise
	 * but informative representation of this issue that is easy for a person to
	 * read.
	 * 
	 * @return a string representation of the issue
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			if (issue.getHeading().length() == 0) {
				return date.toString();
			} else {
				return date.toString() + ", " + issue.getHeading();
			}
		} catch (RuntimeException e) {
			return super.toString();
		}
	}

	/**
	 * Returns a hash code for the object which depends on the content of its
	 * variables. Whenever IndividualIssue objects are held in HashSet objects,
	 * a hashCode() is essentially necessary.
	 * 
	 * <p>
	 * The method was generated by Eclipse using right-click → Source → Generate
	 * hashCode() and equals()…. If you will ever change the classes’ fields,
	 * just re-generate it.
	 * </p>
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((issue == null) ? 0 : issue.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	/**
	 * Returns whether two individual issues are equal; the decision depends on
	 * the content of its variables.
	 * 
	 * <p>
	 * The method was generated by Eclipse using right-click → Source → Generate
	 * hashCode() and equals()…. If you will ever change the classes’ fields,
	 * just re-generate it.
	 * </p>
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof IndividualIssue)) {
			return false;
		}
		IndividualIssue other = (IndividualIssue) obj;
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		if (issue == null) {
			if (other.issue != null) {
				return false;
			}
		} else if (!issue.equals(other.issue)) {
			return false;
		}
		if (title == null) {
			if (other.title != null) {
				return false;
			}
		} else if (!title.equals(other.title)) {
			return false;
		}
		return true;
	}
}
