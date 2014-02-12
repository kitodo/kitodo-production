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

import java.util.HashSet;
import java.util.Set;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

/**
 * The class Issue represents the regular appearance of one (or the) issue of a
 * newspaper.
 * 
 * <p>
 * Newspapers, especially bigger ones, can have several issues that may differ
 * in time of publication (morning issue, evening issue, …), geographic
 * distribution (Edinburgh issue, London issue, …) and/or their days of
 * appearance (weekday issue: Mon—Fri, weekend issue: Sat, sports supplement:
 * Mon, property market: Wed, broadcasting programme: Thu). Furthermore there
 * may be exceptions in either that an issue didn’t appear on a date where, due
 * to the day of week, it usually does (i.e. on holidays) or an issue may have
 * appeared where, due to the day of week, it should not have.
 * </p>
 * 
 * <p>
 * Each issue can be modeled by one Issue object each, which are held by a Title
 * object which provides the dates of first and last appearance.
 * </p>
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Issue implements Cloneable {

	/**
	 * Dates with issue on days of week without regular appearance
	 * 
	 * <p>
	 * Implementors note: SortedSet and SortedMap do not declare HashCode &
	 * Equals and cannot be used in a sensible way here.
	 * </p>
	 */
	protected Set<LocalDate> additions;

	/**
	 * Days of week of regular appearance. JodaTime uses int in [1 = monday … 7
	 * = Sunday]
	 * 
	 * <p>
	 * Implementors note: SortedSet and SortedMap do not declare HashCode &
	 * Equals and cannot be used in a sensible way here.
	 * </p>
	 */
	protected Set<Integer> daysOfWeek;

	/**
	 * Dates of days without issue on days of regular appearance (i.e. holidays)
	 * 
	 * <p>
	 * Implementors note: SortedSet and SortedMap do not declare HashCode &
	 * Equals and cannot be used in a sensible way here.
	 * </p>
	 */
	protected Set<LocalDate> exclusions;

	/**
	 * Issue name, i.e. “Evening issue”
	 */
	protected String heading;

	/**
	 * Empty issue constructor
	 */
	public Issue() {
		heading = "";
		additions = new HashSet<LocalDate>();
		daysOfWeek = new HashSet<Integer>();
		exclusions = new HashSet<LocalDate>();
	}

	/**
	 * Simple Issue constructor.
	 * 
	 * @param heading
	 *            issue name
	 */
	public Issue(String heading) {
		this();
		this.heading = heading;
	}

	/**
	 * Extended Issue constructor.
	 * 
	 * @param heading
	 *            issue name
	 * @param monday
	 *            whether the issue regularly appears on Mondays
	 * @param tuesday
	 *            whether the issue regularly appears on Tuesdays
	 * @param wednesday
	 *            whether the issue regularly appears on Wednesdays
	 * @param thursday
	 *            whether the issue regularly appears on Thursdays
	 * @param friday
	 *            whether the issue regularly appears on Fridays
	 * @param saturday
	 *            whether the issue regularly appears on Saturdays
	 * @param sunday
	 *            whether the issue regularly appears on Sundays
	 */
	public Issue(String heading, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday,
			boolean saturday, boolean sunday) {
		this(heading);
		if (monday)
			daysOfWeek.add(DateTimeConstants.MONDAY);
		if (tuesday)
			daysOfWeek.add(DateTimeConstants.TUESDAY);
		if (wednesday)
			daysOfWeek.add(DateTimeConstants.WEDNESDAY);
		if (thursday)
			daysOfWeek.add(DateTimeConstants.THURSDAY);
		if (friday)
			daysOfWeek.add(DateTimeConstants.FRIDAY);
		if (saturday)
			daysOfWeek.add(DateTimeConstants.SATURDAY);
		if (sunday)
			daysOfWeek.add(DateTimeConstants.SUNDAY);
	}

	/**
	 * Adds a LocalDate to the set of exclusions.
	 * 
	 * @param exclusion
	 *            date to add
	 * @return true if the set was changed
	 */
	public boolean addAddition(LocalDate addition) {
		return additions.add(addition);
	}

	/**
	 * Adds the given dayOfWeek to the Set of daysOfWeek
	 * 
	 * @param dayOfWeek
	 *            An int representing the day of week (1 = monday … 7 = sunday)
	 * @return true if the Set was changed
	 */
	public boolean addDayOfWeek(int dayOfWeek) {
		return daysOfWeek.add(dayOfWeek);
	}

	/**
	 * Adds a LocalDate to the set of exclusions.
	 * 
	 * @param exclusion
	 *            date to add
	 * @return true if the set was changed
	 */
	public boolean addExclusion(LocalDate exclusion) {
		return exclusions.add(exclusion);
	}

	/**
	 * Adds Monday to the set of days of week of regular appearance.
	 * 
	 * @return true if the set was changed
	 */
	public boolean addMonday() {
		return addDayOfWeek(DateTimeConstants.MONDAY);
	}

	/**
	 * Adds Tuesday to the set of days of week of regular appearance.
	 * 
	 * @return true if the set was changed
	 */
	public boolean addTuesday() {
		return addDayOfWeek(DateTimeConstants.TUESDAY);
	}

	/**
	 * Adds Wednesday to the set of days of week of regular appearance.
	 * 
	 * @return true if the set was changed
	 */
	public boolean addWednesday() {
		return addDayOfWeek(DateTimeConstants.WEDNESDAY);
	}

	/**
	 * Adds Thursday to the set of days of week of regular appearance.
	 * 
	 * @return true if the set was changed
	 */
	public boolean addThursday() {
		return addDayOfWeek(DateTimeConstants.THURSDAY);
	}

	/**
	 * Adds Friday to the set of days of week of regular appearance.
	 * 
	 * @return true if the set was changed
	 */
	public boolean addFriday() {
		return addDayOfWeek(DateTimeConstants.FRIDAY);
	}

	/**
	 * Adds Saturday to the set of days of week of regular appearance.
	 * 
	 * @return true if the set was changed
	 */
	public boolean addSaturday() {
		return addDayOfWeek(DateTimeConstants.SATURDAY);
	}

	/**
	 * Adds Sunday to the set of days of week of regular appearance.
	 * 
	 * @return true if the set was changed
	 */
	public boolean addSunday() {
		return addDayOfWeek(DateTimeConstants.SUNDAY);
	}

	/**
	 * Creates a copy of the object. All instance variables will be copied—this
	 * is done in the getter methods—so that modifications to the copied object
	 * will not impact to the copy master.
	 * 
	 * <p>
	 * Implementors note: java.lang.Object.clone() is “protected”, it must be
	 * made public here. In addition the clone() method is defined so that it
	 * throws an exception if it is called on an object which doesn’t implement
	 * the Cloneable interface.
	 * </p>
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Issue clone() {
		Issue copy = new Issue(heading); // String is final
		copy.setAdditions(getAdditions()); // getAdditions() creates a copy, LocalDate is final
		copy.setDaysOfWeek(getDaysOfWeek()); // getAdditions() creates a copy, Integer is final
		copy.setExclusions(getExclusions()); // getAdditions() creates a copy, LocalDate is final
		return copy;
	}

	/**
	 * Getter function for the Set of additions. Returns a copy, i.e.
	 * modifications of the returned object will not have an influence to the
	 * internals of the Issue object.
	 * 
	 * @return a copy of the set of additions
	 */
	public Set<LocalDate> getAdditions() {
		return new HashSet<LocalDate>(additions);
	}

	/**
	 * Getter function for the Set of days of week the issue regularly appears.
	 * Returns a copy, i.e. modifications of the returned object will not have
	 * an influence to the internals of the Issue object.
	 * 
	 * @return a copy of the set of days of week the issue regularly appears
	 */
	public Set<Integer> getDaysOfWeek() {
		return new HashSet<Integer>(daysOfWeek);
	}

	/**
	 * Getter function for the Set of exclusions. Returns a copy, i.e.
	 * modifications of the returned object will not have an influence to the
	 * internals of the Issue object.
	 * 
	 * @return a copy of the set of exclusions
	 */
	public HashSet<LocalDate> getExclusions() {
		return new HashSet<LocalDate>(exclusions);
	}

	/**
	 * Getter function for the issue’s name
	 * 
	 * @return the issue’s name
	 */
	public String getHeading() {
		return heading;
	}

	public boolean isDayOfWeek(int dayOfWeek) {
		return daysOfWeek.contains(dayOfWeek);
	}

	/**
	 * The function isMatch() returns whether the issue appeared on a given
	 * LocalDate, taking into consideration the daysOfWeek of regular
	 * appearance, the Set of exclusions and the Set of additions.
	 * 
	 * @param date
	 *            a LocalDate to examine
	 * @return whether the issue appeared that day
	 */
	public boolean isMatch(LocalDate date) {
		return daysOfWeek.contains(date.getDayOfWeek()) && !exclusions.contains(date) || additions.contains(date);
	}

	/**
	 * The function isMonday() can be used to determine whether the issue
	 * regularly appears on Mondays.
	 * 
	 * @return true, if the issue regularly appears on Mondays.
	 */
	public boolean isMonday() {
		return daysOfWeek.contains(DateTimeConstants.MONDAY);
	}

	/**
	 * The function isTuesday() can be used to determine whether the issue
	 * regularly appears on Tuesdays.
	 * 
	 * @return true, if the issue regularly appears on Tuesdays.
	 */
	public boolean isTuesday() {
		return daysOfWeek.contains(DateTimeConstants.TUESDAY);
	}

	/**
	 * The function isWednesday() can be used to determine whether the issue
	 * regularly appears on Wednesdays.
	 * 
	 * @return true, if the issue regularly appears on Wednesdays.
	 */
	public boolean isWednesday() {
		return daysOfWeek.contains(DateTimeConstants.WEDNESDAY);
	}

	/**
	 * The function isThursday() can be used to determine whether the issue
	 * regularly appears on Sundays.
	 * 
	 * @return true, if the issue regularly appears on Thursdays.
	 */
	public boolean isThursday() {
		return daysOfWeek.contains(DateTimeConstants.THURSDAY);
	}

	/**
	 * The function isFriday() can be used to determine whether the issue
	 * regularly appears on Fridays.
	 * 
	 * @return true, if the issue regularly appears on Fridays.
	 */
	public boolean isFriday() {
		return daysOfWeek.contains(DateTimeConstants.FRIDAY);
	}

	/**
	 * The function isSaturday() can be used to determine whether the issue
	 * regularly appears on Saturdays.
	 * 
	 * @return true, if the issue regularly appears on Saturdays.
	 */
	public boolean isSaturday() {
		return daysOfWeek.contains(DateTimeConstants.SATURDAY);
	}

	/**
	 * The function isSunday() can be used to determine whether the issue
	 * regularly appears on Sundays.
	 * 
	 * @return true, if the issue regularly appears on Sundays.
	 */
	public boolean isSunday() {
		return daysOfWeek.contains(DateTimeConstants.SUNDAY);
	}

	/**
	 * Removes the given LocalDate from the set of addition
	 * 
	 * @return true if the Set was changed
	 */
	public boolean removeAddition(LocalDate addition) {
		return additions.remove(addition);
	}

	/**
	 * Removes the given dayOfWeek from the Set of daysOfWeek
	 * 
	 * @param dayOfWeek
	 *            An int representing the day of week (1 = monday … 7 = sunday)
	 * @return true if the Set was changed
	 */
	public boolean removeDayOfWeek(int dayOfWeek) {
		return daysOfWeek.remove(dayOfWeek);
	}

	/**
	 * Removes the given LocalDate from the set of exclusions
	 * 
	 * @return true if the Set was changed
	 */
	public boolean removeExclusion(LocalDate exclusion) {
		return exclusions.remove(exclusion);
	}

	/**
	 * Removes Monday from daysOfWeek
	 * 
	 * @return true if the Set was changed
	 */
	public boolean removeMonday() {
		return removeDayOfWeek(DateTimeConstants.MONDAY);
	}

	/**
	 * Removes Tuesday from daysOfWeek
	 * 
	 * @return true if the Set was changed
	 */
	public boolean removeTuesday() {
		return removeDayOfWeek(DateTimeConstants.TUESDAY);
	}

	/**
	 * Removes Wednesday from daysOfWeek
	 * 
	 * @return true if the Set was changed
	 */
	public boolean removeWednesday() {
		return removeDayOfWeek(DateTimeConstants.WEDNESDAY);
	}

	/**
	 * Removes Thursday from daysOfWeek
	 * 
	 * @return true if the Set was changed
	 */
	public boolean removeThursday() {
		return removeDayOfWeek(DateTimeConstants.THURSDAY);
	}

	/**
	 * Removes Friday from daysOfWeek
	 * 
	 * @return true if the Set was changed
	 */
	public boolean removeFriday() {
		return removeDayOfWeek(DateTimeConstants.FRIDAY);
	}

	/**
	 * Removes Saturday from daysOfWeek
	 * 
	 * @return true if the Set was changed
	 */
	public boolean removeSaturday() {
		return removeDayOfWeek(DateTimeConstants.SATURDAY);
	}

	/**
	 * Removes Sunday from daysOfWeek
	 * 
	 * @return true if the Set was changed
	 */
	public boolean removeSunday() {
		return removeDayOfWeek(DateTimeConstants.SUNDAY);
	}

	/**
	 * Setter method for the set of additions
	 * 
	 * @param additions
	 *            set to be used
	 */
	public void setAdditions(Set<LocalDate> additions) {
		this.additions = additions;
	}

	/**
	 * Setter method for the set of daysOfWeek
	 * 
	 * @param daysOfWeek
	 *            set to be used
	 */
	public void setDaysOfWeek(Set<Integer> daysOfWeek) {
		this.daysOfWeek = daysOfWeek;
	}

	/**
	 * Setter method for the set of exclusions
	 * 
	 * @param exclusions
	 *            set to be used
	 */
	public void setExclusions(Set<LocalDate> exclusions) {
		this.exclusions = exclusions;
	}

	/**
	 * Setter method for the issue’s name
	 * 
	 * @param heading
	 *            heading to be used
	 */
	public void setHeading(String heading) {
		this.heading = heading;
	}

	/**
	 * Toggles a day of week, i.e. if the given day of week is contained in
	 * daysOfWeek it will be removed, if not, it will be added.
	 * 
	 * @param dayOfWeek
	 *            An int representing the day of week (1 = monday … 7 = sunday)
	 * @return true if the Set was changed
	 */
	public boolean toggleDayOfWeek(int dayOfWeek) {
		if (daysOfWeek.contains(dayOfWeek))
			return removeDayOfWeek(dayOfWeek);
		else
			return addDayOfWeek(dayOfWeek);
	}

	/**
	 * Toggles monday, i.e. removes it from daysOfWeek if it is contained, adds
	 * it otherwise.
	 * 
	 * @return true if the Set was changed
	 */
	public boolean toggleMonday() {
		return toggleDayOfWeek(DateTimeConstants.MONDAY);
	}

	/**
	 * Toggles tuesday, i.e. removes it from daysOfWeek if it is contained, adds
	 * it otherwise.
	 * 
	 * @return true if the Set was changed
	 */
	public boolean toggleTuesday() {
		return toggleDayOfWeek(DateTimeConstants.TUESDAY);
	}

	/**
	 * Toggles wednesday, i.e. removes it from daysOfWeek if it is contained,
	 * adds it otherwise.
	 * 
	 * @return true if the Set was changed
	 */
	public boolean toggleWednesday() {
		return toggleDayOfWeek(DateTimeConstants.WEDNESDAY);
	}

	/**
	 * Toggles thursday, i.e. removes it from daysOfWeek if it is contained,
	 * adds it otherwise.
	 * 
	 * @return true if the Set was changed
	 */
	public boolean toggleThursday() {
		return toggleDayOfWeek(DateTimeConstants.THURSDAY);
	}

	/**
	 * Toggles friday, i.e. removes it from daysOfWeek if it is contained, adds
	 * it otherwise.
	 * 
	 * @return true if the Set was changed
	 */
	public boolean toggleFriday() {
		return toggleDayOfWeek(DateTimeConstants.FRIDAY);
	}

	/**
	 * Toggles saturday, i.e. removes it from daysOfWeek if it is contained,
	 * adds it otherwise.
	 * 
	 * @return true if the Set was changed
	 */
	public boolean toggleSaturday() {
		return toggleDayOfWeek(DateTimeConstants.SATURDAY);
	}

	/**
	 * Toggles sunday, i.e. removes it from daysOfWeek if it is contained, adds
	 * it otherwise.
	 * 
	 * @return true if the Set was changed
	 */
	public boolean toggleSunday() {
		return toggleDayOfWeek(DateTimeConstants.SUNDAY);
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
		StringBuilder result = new StringBuilder();
		result.append(heading);
		result.append(" (");
		result.append(daysOfWeek.contains(DateTimeConstants.MONDAY) ? 'M' : '-');
		result.append(daysOfWeek.contains(DateTimeConstants.TUESDAY) ? 'T' : '-');
		result.append(daysOfWeek.contains(DateTimeConstants.WEDNESDAY) ? 'W' : '-');
		result.append(daysOfWeek.contains(DateTimeConstants.THURSDAY) ? 'T' : '-');
		result.append(daysOfWeek.contains(DateTimeConstants.FRIDAY) ? 'F' : '-');
		result.append(daysOfWeek.contains(DateTimeConstants.SATURDAY) ? 'S' : '-');
		result.append(daysOfWeek.contains(DateTimeConstants.SUNDAY) ? 'S' : '-');
		result.append(") +");
		if (additions.size() <= 5)
			result.append(additions.toString());
		else {
			result.append("[…(");
			result.append(additions.size());
			result.append(")…]");
		}
		result.append(" -");
		if (exclusions.size() <= 5)
			result.append(exclusions.toString());
		else {
			result.append("[…(");
			result.append(exclusions.size());
			result.append(")…]");
		}
		return result.toString();
	}

	/**
	 * Returns a hash code for the object which depends on the content of its
	 * variables. Whenever Issue objects are held in HashSet objects, a
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
		result = prime * result + ((additions == null) ? 0 : additions.hashCode());
		result = prime * result + ((exclusions == null) ? 0 : exclusions.hashCode());
		result = prime * result + ((heading == null) ? 0 : heading.hashCode());
		result = prime * result + ((daysOfWeek == null) ? 0 : daysOfWeek.hashCode());
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
		if (!(obj instanceof Issue))
			return false;
		Issue other = (Issue) obj;
		if (additions == null) {
			if (other.additions != null)
				return false;
		} else if (!additions.equals(other.additions))
			return false;
		if (exclusions == null) {
			if (other.exclusions != null)
				return false;
		} else if (!exclusions.equals(other.exclusions))
			return false;
		if (heading == null) {
			if (other.heading != null)
				return false;
		} else if (!heading.equals(other.heading))
			return false;
		if (daysOfWeek == null) {
			if (other.daysOfWeek != null)
				return false;
		} else if (!daysOfWeek.equals(other.daysOfWeek))
			return false;
		return true;
	}
}
