/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.kitodo.production.model.bibliography.course;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;


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
 *
 * <p>
 * Each issue can be modeled by one Issue object each, which are held by a Block
 * object which provides the dates of first and last appearance.
 */
public class Issue {

    private static final int APPEARED = 1;
    private static final int NOT_APPEARED = 0;

    /**
     * Course of appearance this issue is in.
     */
    private final Course course;

    /**
     * Dates with issue on days of week without regular appearance.
     *
     * <p>
     * Implementors note: SortedSet and SortedMap do not declare HashCode &
     * Equals and cannot be used in a sensible way here.
     */
    private Set<LocalDate> additions;

    /**
     * Days of week of regular appearance. JodaTime uses int in [1 = monday … 7
     * = Sunday]
     *
     * <p>
     * Implementors note: SortedSet and SortedMap do not declare HashCode &
     * Equals and cannot be used in a sensible way here.
     */
    private Set<Integer> daysOfWeek;

    /**
     * Dates of days without issue on days of regular appearance (i.e. holidays)
     *
     * <p>
     * Implementors note: SortedSet and SortedMap do not declare HashCode &
     * Equals and cannot be used in a sensible way here.
     */
    private Set<LocalDate> exclusions;

    /**
     * Issue name, i.e. “Evening issue”
     */
    private String heading;

    /**
     * Empty issue constructor.
     *
     * @param course
     *            course of appearance this issue is in
     */
    public Issue(Course course) {
        this.course = course;
        this.heading = "";
        this.additions = new HashSet<>();
        this.daysOfWeek = new HashSet<>();
        this.exclusions = new HashSet<>();
    }

    /**
     * Issue constructor with the option to set the issue heading.
     *
     * @param course
     *            course of appearance this issue is in
     * @param heading
     *            issue heading
     */
    public Issue(Course course, String heading) {
        this.course = course;
        this.heading = heading;
        this.additions = new HashSet<>();
        this.daysOfWeek = new HashSet<>();
        this.exclusions = new HashSet<>();
    }

    /**
     * Adds a LocalDate to the set of exclusions.
     *
     * @param addition
     *            date to add
     * @return true if the set was changed
     */
    public boolean addAddition(LocalDate addition) {
        course.clearProcesses();
        return additions.add(addition);
    }

    /**
     * Adds the given dayOfWeek to the Set of daysOfWeek.
     *
     * @param dayOfWeek
     *            An int representing the day of week (1 = monday … 7 = sunday)
     * @return true if the Set was changed
     */
    private boolean addDayOfWeek(DayOfWeek dayOfWeek) {
        boolean modified = daysOfWeek.add(dayOfWeek.getValue());
        if (modified) {
            course.clearProcesses();
        }
        return modified;
    }

    /**
     * Adds a LocalDate to the set of exclusions.
     *
     * @param exclusion
     *            date to add
     * @return true if the set was changed
     */
    public boolean addExclusion(LocalDate exclusion) {
        course.clearProcesses();
        return exclusions.add(exclusion);
    }

    /**
     * Adds Monday to the set of days of week of regular appearance.
     *
     * @return true if the set was changed
     */
    public boolean addMonday() {
        return addDayOfWeek(DayOfWeek.MONDAY);
    }

    /**
     * Adds Tuesday to the set of days of week of regular appearance.
     *
     * @return true if the set was changed
     */
    public boolean addTuesday() {
        return addDayOfWeek(DayOfWeek.TUESDAY);
    }

    /**
     * Adds Wednesday to the set of days of week of regular appearance.
     *
     * @return true if the set was changed
     */
    public boolean addWednesday() {
        return addDayOfWeek(DayOfWeek.WEDNESDAY);
    }

    /**
     * Adds Thursday to the set of days of week of regular appearance.
     *
     * @return true if the set was changed
     */
    public boolean addThursday() {
        return addDayOfWeek(DayOfWeek.THURSDAY);
    }

    /**
     * Adds Friday to the set of days of week of regular appearance.
     *
     * @return true if the set was changed
     */
    public boolean addFriday() {
        return addDayOfWeek(DayOfWeek.FRIDAY);
    }

    /**
     * Adds Saturday to the set of days of week of regular appearance.
     *
     * @return true if the set was changed
     */
    public boolean addSaturday() {
        return addDayOfWeek(DayOfWeek.SATURDAY);
    }

    /**
     * Adds Sunday to the set of days of week of regular appearance.
     *
     * @return true if the set was changed
     */
    public boolean addSunday() {
        return addDayOfWeek(DayOfWeek.SUNDAY);
    }

    /**
     * Creates a copy of the object. All instance variables will be copied—this
     * is done in the getter methods—so that modifications to the copied object
     * will not impact to the copy master.
     *
     * @param course
     *            course the copy shall belong to
     * @return a copy of this object for the new course
     */
    public Issue clone(Course course) {
        Issue copy = new Issue(course);
        copy.heading = heading;
        copy.additions = new HashSet<>(additions);
        copy.daysOfWeek = new HashSet<>(daysOfWeek);
        copy.exclusions = new HashSet<>(exclusions);
        return copy;
    }

    /**
     * Determines how many stampings of
     * this issue physically appeared without generating a list of
     * IndividualIssue objects.
     *
     * @param firstAppearance
     *            first day of the time range to inspect
     * @param lastAppearance
     *            last day of the time range to inspect
     * @return the count of issues
     * @throws IllegalArgumentException
     *             if lastAppearance is null
     */
    long countIndividualIssues(LocalDate firstAppearance, LocalDate lastAppearance) {
        long numberOfIndividualIssues = 0;
        for (LocalDate day = firstAppearance; !day.isAfter(lastAppearance); day = day.plusDays(1)) {
            if (isMatch(day)) {
                numberOfIndividualIssues += 1;
            }
        }
        return numberOfIndividualIssues;
    }

    /**
     * Getter function for the Set of additions.
     *
     * @return the set of additions
     */
    public Set<LocalDate> getAdditions() {
        return additions;
    }

    /**
     * Getter function for the Set of days of week the issue regularly appears.
     *
     * @return the set of days of week the issue regularly appears
     */
    public Set<Integer> getDaysOfWeek() {
        return daysOfWeek;
    }

    /**
     * Getter function for the Set of exclusions.
     *
     * @return the set of exclusions
     */
    public Set<LocalDate> getExclusions() {
        return exclusions;
    }

    /**
     * Getter function for the issue’s name.
     *
     * @return the issue’s name
     */
    public String getHeading() {
        return heading;
    }

    /**
     * Returns whether the issue regularly appeared on the given day of week.
     *
     * @param dayOfWeek
     *            day of week to look up
     * @return whether the issue appeared on that day of week
     */
    public boolean isDayOfWeek(int dayOfWeek) {
        return daysOfWeek.contains(dayOfWeek);
    }

    /**
     * Returns whether the issue appeared on a given
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
     * Can be used to determine whether the issue
     * regularly appears on Mondays.
     *
     * @return true, if the issue regularly appears on Mondays.
     */
    public boolean isMonday() {
        return daysOfWeek.contains(DayOfWeek.MONDAY.getValue());
    }

    /**
     * Can be used to determine whether the issue
     * regularly appears on Tuesdays.
     *
     * @return true, if the issue regularly appears on Tuesdays.
     */
    public boolean isTuesday() {
        return daysOfWeek.contains(DayOfWeek.TUESDAY.getValue());
    }

    /**
     * Can be used to determine whether the issue
     * regularly appears on Wednesdays.
     *
     * @return true, if the issue regularly appears on Wednesdays.
     */
    public boolean isWednesday() {
        return daysOfWeek.contains(DayOfWeek.WEDNESDAY.getValue());
    }

    /**
     * Can be used to determine whether the issue
     * regularly appears on Sundays.
     *
     * @return true, if the issue regularly appears on Thursdays.
     */
    public boolean isThursday() {
        return daysOfWeek.contains(DayOfWeek.THURSDAY.getValue());
    }

    /**
     * Can be used to determine whether the issue
     * regularly appears on Fridays.
     *
     * @return true, if the issue regularly appears on Fridays.
     */
    public boolean isFriday() {
        return daysOfWeek.contains(DayOfWeek.FRIDAY.getValue());
    }

    /**
     * Can be used to determine whether the issue
     * regularly appears on Saturdays.
     *
     * @return true, if the issue regularly appears on Saturdays.
     */
    public boolean isSaturday() {
        return daysOfWeek.contains(DayOfWeek.SATURDAY.getValue());
    }

    /**
     * Can be used to determine whether the issue
     * regularly appears on Sundays.
     *
     * @return true, if the issue regularly appears on Sundays.
     */
    public boolean isSunday() {
        return daysOfWeek.contains(DayOfWeek.SUNDAY.getValue());
    }

    /**
     * Recalculates for each Issue
     * the daysOfWeek of its regular appearance within the given interval of
     * time. This is especially sensible to detect the underlying regularity
     * after lots of individual issues whose existence is known have been added
     * one by one as additions.
     *
     * @param firstAppearance
     *            first day of the date range
     * @param lastAppearance
     *            last day of the date range
     */
    void recalculateRegularity(LocalDate firstAppearance, LocalDate lastAppearance) {
        Set<LocalDate> remainingAdditions = new HashSet<>();
        Set<LocalDate> remainingExclusions = new HashSet<>();

        @SuppressWarnings("unchecked")
        HashSet<LocalDate>[][] subsets = new HashSet[DayOfWeek.SUNDAY.getValue()][APPEARED + 1];
        for (int dayOfWeek = DayOfWeek.MONDAY.getValue(); dayOfWeek <= DayOfWeek.SUNDAY.getValue(); dayOfWeek++) {
            subsets[dayOfWeek - 1][NOT_APPEARED] = new HashSet<>();
            subsets[dayOfWeek - 1][APPEARED] = new HashSet<>();
        }

        for (LocalDate day = firstAppearance; !day.isAfter(lastAppearance); day = day.plusDays(1)) {
            subsets[day.getDayOfWeek().getValue() - 1][isMatch(day) ? APPEARED : NOT_APPEARED].add(day);
        }

        for (int dayOfWeek = DayOfWeek.MONDAY.getValue(); dayOfWeek <= DayOfWeek.SUNDAY.getValue(); dayOfWeek++) {
            if (subsets[dayOfWeek - 1][APPEARED].size() > subsets[dayOfWeek - 1][NOT_APPEARED].size()) {
                daysOfWeek.add(dayOfWeek);
                remainingExclusions.addAll(subsets[dayOfWeek - 1][NOT_APPEARED]);
            } else {
                daysOfWeek.remove(dayOfWeek);
                remainingAdditions.addAll(subsets[dayOfWeek - 1][APPEARED]);
            }
        }

        additions = remainingAdditions;
        exclusions = remainingExclusions;

    }

    /**
     * Removes the given LocalDate from the set of addition.
     *
     * @param addition
     *            date to remove
     * @return true if the Set was changed
     */
    public boolean removeAddition(LocalDate addition) {
        course.clearProcesses();
        return additions.remove(addition);
    }

    /**
     * Removes the given dayOfWeek from the Set of daysOfWeek.
     *
     * @param dayOfWeek
     *            An int representing the day of week (1 = monday … 7 = sunday)
     * @return true if the Set was changed
     */
    private boolean removeDayOfWeek(DayOfWeek dayOfWeek) {
        boolean modified = daysOfWeek.remove(dayOfWeek.getValue());
        if (modified) {
            course.clearProcesses();
        }
        return modified;
    }

    /**
     * Removes the given LocalDate from the set of exclusions.
     *
     * @param exclusion
     *            date to remove
     * @return true if the Set was changed
     */
    public boolean removeExclusion(LocalDate exclusion) {
        course.clearProcesses();
        return exclusions.remove(exclusion);
    }

    /**
     * Removes Monday from daysOfWeek.
     *
     * @return true if the Set was changed
     */
    public boolean removeMonday() {
        return removeDayOfWeek(DayOfWeek.MONDAY);
    }

    /**
     * Removes Tuesday from daysOfWeek.
     *
     * @return true if the Set was changed
     */
    public boolean removeTuesday() {
        return removeDayOfWeek(DayOfWeek.TUESDAY);
    }

    /**
     * Removes Wednesday from daysOfWeek.
     *
     * @return true if the Set was changed
     */
    public boolean removeWednesday() {
        return removeDayOfWeek(DayOfWeek.WEDNESDAY);
    }

    /**
     * Removes Thursday from daysOfWeek.
     *
     * @return true if the Set was changed
     */
    public boolean removeThursday() {
        return removeDayOfWeek(DayOfWeek.THURSDAY);
    }

    /**
     * Removes Friday from daysOfWeek.
     *
     * @return true if the Set was changed
     */
    public boolean removeFriday() {
        return removeDayOfWeek(DayOfWeek.FRIDAY);
    }

    /**
     * Removes Saturday from daysOfWeek.
     *
     * @return true if the Set was changed
     */
    public boolean removeSaturday() {
        return removeDayOfWeek(DayOfWeek.SATURDAY);
    }

    /**
     * Removes Sunday from daysOfWeek.
     *
     * @return true if the Set was changed
     */
    public boolean removeSunday() {
        return removeDayOfWeek(DayOfWeek.SUNDAY);
    }

    /**
     * Setter method for the issue’s name.
     *
     * @param heading
     *            heading to be used
     */
    public void setHeading(String heading) {
        if (!Objects.equals(this.heading, heading)) {
            course.clearProcesses();
        }
        this.heading = heading;
    }

    /**
     * Provides returns a string that contains a concise
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
        result.append(daysOfWeek.contains(DayOfWeek.MONDAY.getValue()) ? 'M' : '-');
        result.append(daysOfWeek.contains(DayOfWeek.TUESDAY.getValue()) ? 'T' : '-');
        result.append(daysOfWeek.contains(DayOfWeek.WEDNESDAY.getValue()) ? 'W' : '-');
        result.append(daysOfWeek.contains(DayOfWeek.THURSDAY.getValue()) ? 'T' : '-');
        result.append(daysOfWeek.contains(DayOfWeek.FRIDAY.getValue()) ? 'F' : '-');
        result.append(daysOfWeek.contains(DayOfWeek.SATURDAY.getValue()) ? 'S' : '-');
        result.append(daysOfWeek.contains(DayOfWeek.SUNDAY.getValue()) ? 'S' : '-');
        result.append(") +");
        if (additions.size() <= 5) {
            result.append(additions.toString());
        } else {
            result.append("[…(");
            result.append(additions.size());
            result.append(")…]");
        }
        result.append(" -");
        if (exclusions.size() <= 5) {
            result.append(exclusions.toString());
        } else {
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
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int hashCode = 1;
        hashCode = prime * hashCode + ((additions == null) ? 0 : additions.hashCode());
        hashCode = prime * hashCode + ((daysOfWeek == null) ? 0 : daysOfWeek.hashCode());
        hashCode = prime * hashCode + ((exclusions == null) ? 0 : exclusions.hashCode());
        hashCode = prime * hashCode + ((heading == null) ? 0 : heading.hashCode());
        return hashCode;
    }

    /**
     * Returns whether two individual issues are equal; the decision depends on
     * the content of its variables.
     *
     * <p>
     * The method was generated by Eclipse using right-click → Source → Generate
     * hashCode() and equals()…. If you will ever change the classes’ fields,
     * just re-generate it.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Issue) {
            Issue other = (Issue) obj;

            if (Objects.isNull(additions)) {
                if (Objects.nonNull(other.additions)) {
                    return false;
                }
            } else if (!additions.equals(other.additions)) {
                return false;
            }

            if (Objects.isNull(daysOfWeek)) {
                if (Objects.nonNull(other.daysOfWeek)) {
                    return false;
                }
            } else if (!daysOfWeek.equals(other.daysOfWeek)) {
                return false;
            }

            if (Objects.isNull(exclusions)) {
                if (Objects.nonNull(other.exclusions)) {
                    return false;
                }
            } else if (!exclusions.equals(other.exclusions)) {
                return false;
            }

            if (Objects.isNull(heading)) {
                return Objects.isNull(other.heading);
            } else {
                return heading.equals(other.heading);
            }
        }
        return false;
    }
}
