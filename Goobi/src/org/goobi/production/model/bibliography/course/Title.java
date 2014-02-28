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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import de.sub.goobi.helper.DateFuncs;

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
	 * Constructor for a title with a given heading and dates of appearance
	 * 
	 * @param heading
	 *            the name of the title
	 * @param firstAppearance
	 *            the first day this title appeared
	 * @param lastAppearance
	 *            the last day this title appeared
	 */
	public Title(String heading, LocalDate firstAppearance, LocalDate lastAppearance) {
		this.heading = heading;
		this.firstAppearance = firstAppearance;
		this.lastAppearance = lastAppearance;
		this.issues = new ArrayList<Issue>();
	}

	/**
	 * Fully qualified constructor for a title with all details
	 * 
	 * @param heading
	 *            the name of the title
	 * @param firstAppearance
	 *            the first day this title appeared
	 * @param lastAppearance
	 *            the last day this title appeared
	 * @param issues
	 *            Issues to be held by this title
	 */
	public Title(String heading, LocalDate firstAppearance, LocalDate lastAppearance, List<Issue> issues) {
		this.heading = heading;
		this.firstAppearance = firstAppearance;
		this.lastAppearance = lastAppearance;
		this.issues = issues;
	}

	/**
	 * Adds an Issue to this title if it is not already present.
	 * <p>
	 * The same Issue mustn’t be added to several blocks because optimise()ing
	 * these blocks will delete the irregularities outside the block, which
	 * means that all irregularities [in detail: all irregularities which aren’t
	 * part of <em>all</em> blocks] will be forgotten, therefore the add method
	 * does clone the issue object.
	 * </p>
	 * 
	 * @param issue
	 *            Issue to add
	 * @return true if the set was changed
	 */
	public boolean addIssue(Issue issue) {
		return issues.add(issue.clone());
	}

	/**
	 * Adds all of the Issues in the specified collection to this Title if they
	 * are not already present.
	 * 
	 * @param issues
	 *            collection containing Issues to be added
	 * @return true if the set was changed
	 */
	public boolean addAllIssues(Collection<? extends Issue> issues) {
		boolean result = false;
		for (Issue issue : issues)
			result |= addIssue(issue);
		return result;
	}

	/**
	 * Removes all of the Issues from this Title.
	 */
	public void clearIssues() {
		issues.clear();
	}

	/**
	 * Creates and returns a copy of this Title.
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Title clone() {
		Title result = new Title(heading, firstAppearance, lastAppearance);
		result.setIssues(new ArrayList<Issue>(issues));
		return result;
	}

	/**
	 * Returns true if this Title contains the specified issue
	 * 
	 * @param issue
	 *            Issue whose presence in this Title is to be tested
	 * @return true if this set contains the specified element
	 */
	public boolean containsIssue(Issue issue) {
		return issues.contains(issue);
	}

	/**
	 * Returns true if this Title contains all of the Issues of the specified
	 * collection.
	 * 
	 * @param issues
	 *            collection to be checked for containment in this Title
	 * @return true if this Title contains all elements of the collection
	 */
	public boolean containsAllIssues(Collection<? extends Issue> issues) {
		return this.issues.containsAll(issues);
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
	 * The function isEmptyIssues() returns true if this Title contains no
	 * Issues.
	 * 
	 * @return true if this Title contains no Issues
	 */
	public boolean isEmptyIssues() {
		return issues.isEmpty();
	}

	/**
	 * The function issuesIterator() returns an iterator over the Issues in this
	 * Title. If the underlying Set contains more than Integer.MAX_VALUE
	 * elements, Integer.MAX_VALUE is returned.
	 * 
	 * @return an iterator over the elements in the set of issues
	 */
	public Iterator<Issue> issuesIterator() {
		return issues.iterator();
	}

	/**
	 * The function issuesSize() returns the number of Issues in this Title.
	 * 
	 * @return the cardinality of the set of issues
	 */
	public int issuesSize() {
		return issues.size();
	}

	/**
	 * The function getRealFirstAppearance() returns the first date at least one
	 * of the issues assigned to this Title was published, starting from the
	 * given date. This can be used to correct the date of first appearance
	 * forwardly to reduce the number of exclusions that need to be handled
	 * separately.
	 * 
	 * <p>
	 * The two main use cases are
	 * <ul>
	 * <li>to shrink the current date of first appearance:<br/>
	 * <code>obj.setFirstAppearance(obj.getRealFirstAppearance(obj.getFirstAppearance(), obj.getLastAppearance()))</code>
	 * </li>
	 * <li>to initially set the date of first appearance, if unknown:<br/>
	 * <code>obj.setFirstAppearance(obj.getRealFirstAppearance(new LocalDate(1582, DateTimeConstants.OCTOBER, 15), new LocalDate()))</code>
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @return the first date at least one issue was published
	 */
	public LocalDate getRealFirstAppearance(LocalDate from, LocalDate tillTo) {
		for (LocalDate day = from; !day.isAfter(tillTo); day = day.plusDays(1))
			for (Issue issue : issues)
				if (issue.isMatch(day))
					return day;
		return null;
	}

	/**
	 * The function getRealLastAppearance() returns the last date at least one
	 * of the issues assigned to this Title was published, starting backwards
	 * from the given date. This can be used to correct the date of last
	 * appearance reversely to reduce the number of exclusions that need to be
	 * handled separately.
	 * 
	 * <p>
	 * The two main use cases are
	 * <ul>
	 * <li>to shrink the current date of last appearance:<br/>
	 * <code>obj.setLastAppearance(obj.getRealLastAppearance(obj.getFirstAppearance(), obj.getLastAppearance()))</code>
	 * </li>
	 * <li>to initially set the date of first appearance, if unknown:<br/>
	 * <code>obj.setLastAppearance(obj.getRealLastAppearance(new LocalDate(1582, DateTimeConstants.OCTOBER, 15), new LocalDate()))</code>
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @return the last date at least one issue was published
	 */
	public LocalDate getRealLastAppearance(LocalDate from, LocalDate tillTo) {
		for (LocalDate day = tillTo; !day.isBefore(from); day = day.minusDays(1))
			for (Issue issue : issues)
				if (issue.isMatch(day))
					return day;
		return null;
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
	 * Removes the specified Issue from this Title if it is present.
	 * 
	 * @param issue
	 *            Issue to be removed from the set
	 * @return true if the set was changed
	 */
	public boolean removeIssue(Issue issue) {
		return issues.remove(issue);
	}

	/**
	 * The function removeAllIssues() removes all Issues that are contained in
	 * the specified collection from this Title
	 * 
	 * @param issues
	 *            collection containing elements to be removed
	 * @return true if the set was changed
	 */
	public boolean removeAllIssues(Collection<? extends Issue> issues) {
		return this.issues.removeAll(issues);
	}

	/**
	 * The function retainAllIssues() removes all Issues from this Title that
	 * are not contained in the specified collection
	 * 
	 * @param issues
	 *            collection containing Issues to be retained
	 * @return true if the set was changed
	 */
	public boolean retainAllIssues(Collection<? extends Issue> issues) {
		return this.issues.retainAll(issues);
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
	 * The method setIssues() allows to assign any Set of Issues to the Title
	 * 
	 * @param issues
	 *            new Set to be subsequently used
	 */
	public void setIssues(List<Issue> issues) {
		this.issues = issues;
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
		if (firstAppearance != null)
			result.append(DateFuncs.DATE_CONVERTER.print(firstAppearance));
		result.append(" − ");
		if (lastAppearance != null)
			result.append(DateFuncs.DATE_CONVERTER.print(lastAppearance));
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
