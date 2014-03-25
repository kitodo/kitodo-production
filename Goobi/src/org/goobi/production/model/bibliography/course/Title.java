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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

/**
 * The class Title is a bean class that represents an interval of time in the
 * course of appearance of a newspaper within which it wasn’t suspended and
 * didn’t change its name either. A Title instance handles one or more Issue
 * objects.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Title implements Cloneable {
	protected String heading;
	protected LocalDate firstAppearance;
	protected LocalDate lastAppearance;
	protected List<Issue> issues;

	/**
	 * Constructor for a Title object without any data.
	 */
	public Title() {
		this.heading = "";
		this.firstAppearance = null;
		this.lastAppearance = null;
		this.issues = new ArrayList<Issue>();
	}

	/**
	 * Constructor for a title with a given heading.
	 * 
	 * @param heading
	 *            the name of the title
	 */
	public Title(String heading) {
		this.heading = heading;
		this.firstAppearance = null;
		this.lastAppearance = null;
		this.issues = new ArrayList<Issue>();
	}

	/**
	 * Adds an Issue to this title if it is not already present.
	 * 
	 * @param issue
	 *            Issue to add
	 * @return true if the set was changed
	 */
	public boolean addIssue(Issue issue) {
		return issues.add(issue.clone());
	}

	/**
	 * Creates and returns a copy of this Title.
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Title clone() {
		Title result = new Title();
		result.heading = heading;
		result.firstAppearance = firstAppearance;
		result.lastAppearance = lastAppearance;
		ArrayList<Issue> result_issues = new ArrayList<Issue>(issues.size());
		for (Issue issue : issues)
			result_issues.add(issue.clone());
		result.issues = result_issues;
		return result;
	}

	/**
	 * The function getIssues() returns the list of issues contained in this
	 * Title.
	 * 
	 * @return the list of issues from this Title
	 */
	public List<Issue> getIssues() {
		return new ArrayList<Issue>(issues);
	}

	/**
	 * The function getHeading() returns the heading of the Title.
	 * 
	 * @return the name of the newspaper
	 */
	public String getHeading() {
		return heading;
	}

	/**
	 * The function getIssue() returns an issue from the Title by the issue’s
	 * heading, or null if the title doesn’t contain an issue with that heading.
	 * 
	 * @param heading
	 *            Heading of the issue to look for
	 * @return Issue with that heading
	 */
	public Issue getIssue(String heading) {
		for (Issue issue : issues)
			if (heading.equals(issue.getHeading()))
				return issue;
		return null;
	}

	/**
	 * The function getFirstAppearance() returns the date the regularity of this
	 * title begins with.
	 * 
	 * @return the date of first appearance
	 */
	public LocalDate getFirstAppearance() {
		return firstAppearance;
	}

	/**
	 * The function getLastAppearance() returns the date the regularity of this
	 * title ends with.
	 * 
	 * @return the date of last appearance
	 */
	public LocalDate getLastAppearance() {
		return lastAppearance;
	}

	/**
	 * The function isEmpty() returns whether the title is in an empty state or
	 * not.
	 * 
	 * @return whether the title is dataless
	 */
	public boolean isEmpty() {
		return (heading == null || heading.equals("")) && firstAppearance == null && lastAppearance == null
				&& (issues == null || issues.isEmpty());
	}

	/**
	 * The function isMatch() returns whether a given LocalDate comes within the
	 * limits of this title. Defaults to false if either the argument or one of
	 * the fields to compare against is null.
	 * 
	 * @param date
	 *            a LocalDate to examine
	 * @return whether the date is within the limits of this title block
	 */
	public boolean isMatch(LocalDate date) {
		try {
			return !date.isBefore(firstAppearance) && !date.isAfter(lastAppearance);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * The method recalculateRegularityOfIssues() recalculates for each Issue
	 * the daysOfWeek of its regular appearance within the interval of time of
	 * the Title. This is especially sensible to detect the underlying
	 * regularity after lots of issues whose existence is known have been added
	 * one by one as additions to the underlying issue(s).
	 */
	public void recalculateRegularityOfIssues() {
		final int APPEARED = 1;
		final int NOT_APPEARED = 0;

		for (Issue issue : issues) {
			Set<LocalDate> remainingAdditions = new HashSet<LocalDate>();
			Set<LocalDate> remainingExclusions = new HashSet<LocalDate>();

			@SuppressWarnings("unchecked")
			HashSet<LocalDate>[][] subsets = new HashSet[DateTimeConstants.SUNDAY][APPEARED + 1];
			for (int dayOfWeek = DateTimeConstants.MONDAY; dayOfWeek <= DateTimeConstants.SUNDAY; dayOfWeek++) {
				subsets[dayOfWeek - 1][NOT_APPEARED] = new HashSet<LocalDate>();
				subsets[dayOfWeek - 1][APPEARED] = new HashSet<LocalDate>();
			}

			for (LocalDate day = firstAppearance; !day.isAfter(lastAppearance); day = day.plusDays(1))
				subsets[day.getDayOfWeek() - 1][issue.isMatch(day) ? APPEARED : NOT_APPEARED].add(day);

			for (int dayOfWeek = DateTimeConstants.MONDAY; dayOfWeek <= DateTimeConstants.SUNDAY; dayOfWeek++) {
				if (subsets[dayOfWeek - 1][APPEARED].size() > subsets[dayOfWeek - 1][NOT_APPEARED].size()) {
					issue.addDayOfWeek(dayOfWeek);
					remainingExclusions.addAll(subsets[dayOfWeek - 1][NOT_APPEARED]);
				} else {
					issue.removeDayOfWeek(dayOfWeek);
					remainingAdditions.addAll(subsets[dayOfWeek - 1][APPEARED]);
				}
			}

			issue.setAdditions(remainingAdditions);
			issue.setExclusions(remainingExclusions);
		}
	}

	/**
	 * The function removeIssue() removes the specified Issue from this Title if
	 * it is present.
	 * 
	 * @param issue
	 *            Issue to be removed from the set
	 * @return true if the set was changed
	 */
	public boolean removeIssue(Issue issue) {
		return issues.remove(issue);
	}

	/**
	 * The method setHeading() sets the title of the newspaper.
	 * 
	 * @param heading
	 *            Title for the Title
	 */
	public void setHeading(String heading) {
		this.heading = heading;
	}

	/**
	 * The method setFirstAppearance() sets a LocalDate as day of first
	 * appearance for this Title.
	 * 
	 * @param firstAppearance
	 *            date of first appearance
	 */
	public void setFirstAppearance(LocalDate firstAppearance) {
		this.firstAppearance = firstAppearance;
	}

	/**
	 * The method setLastAppearance() sets a LocalDate as day of last appeanance
	 * for this Title.
	 * 
	 * @param lastAppearance
	 *            date of last appearance
	 */
	public void setLastAppearance(LocalDate lastAppearance) {
		this.lastAppearance = lastAppearance;
	}

	/**
	 * The function toString() provides returns a string that contains a concise
	 * but informative representation of this title that is easy for a person to
	 * read.
	 * 
	 * @return a string representation of the title
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(heading);
		result.append(" (");
		if (firstAppearance != null)
			result.append(firstAppearance.toString());
		result.append(" - ");
		if (lastAppearance != null)
			result.append(lastAppearance.toString());
		result.append(") [");
		boolean first = true;
		for (Issue issue : issues) {
			if (!first)
				result.append(", ");
			result.append(issue.toString());
			first = false;
		}
		result.append("]");
		return result.toString();
	}

	/**
	 * The function toString() provides returns a string that contains a textual
	 * representation of this title that is easy for a person to read.
	 * 
	 * @param dateConverter
	 *            a DateTimeFormatter for formatting the local dates
	 * @return a string to identify the title
	 */
	public String toString(DateTimeFormatter dateConverter) {
		StringBuilder result = new StringBuilder();
		if (firstAppearance != null)
			result.append(dateConverter.print(firstAppearance));
		result.append(" − ");
		if (lastAppearance != null)
			result.append(dateConverter.print(lastAppearance));
		result.append(", ");
		result.append(heading);
		return result.toString();
	}

	/**
	 * Returns a hash code for the object which depends on the content of its
	 * variables. Whenever Title objects are held in HashSet objects, a
	 * hashCode() is essentially necessary.
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
		result = prime * result + ((firstAppearance == null) ? 0 : firstAppearance.hashCode());
		result = prime * result + ((heading == null) ? 0 : heading.hashCode());
		result = prime * result + ((issues == null) ? 0 : issues.hashCode());
		result = prime * result + ((lastAppearance == null) ? 0 : lastAppearance.hashCode());
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Title))
			return false;
		Title other = (Title) obj;
		if (firstAppearance == null) {
			if (other.firstAppearance != null)
				return false;
		} else if (!firstAppearance.equals(other.firstAppearance))
			return false;
		if (heading == null) {
			if (other.heading != null)
				return false;
		} else if (!heading.equals(other.heading))
			return false;
		if (issues == null) {
			if (other.issues != null)
				return false;
		} else if (!issues.equals(other.issues))
			return false;
		if (lastAppearance == null) {
			if (other.lastAppearance != null)
				return false;
		} else if (!lastAppearance.equals(other.lastAppearance))
			return false;
		return true;
	}
}
