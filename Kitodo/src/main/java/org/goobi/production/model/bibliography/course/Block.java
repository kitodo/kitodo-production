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

package org.goobi.production.model.bibliography.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;

/**
 * The class Block is a bean class that represents an interval of time in the
 * course of appearance of a newspaper within which it wasn’t suspended. A Block
 * instance handles one or more Issue objects.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Block {
    /**
     * The field course holds a reference to the course this block is in.
     */
    private final Course course;

    /**
     * The field variant may hold a variant identifier that can be used to
     * distinguish different blocks during the buildup of a course of appearance
     * from individual issues.
     *
     * <p>
     * Given a newspaper appeared three times a week for a period of time, and
     * then changed to being published six times a week without changing its
     * heading, and this change shall be represented by different blocks, the
     * variant identifier can be used to distinguish the blocks. Otherwise, both
     * time ranges would be represented in one combined block, what would be
     * factual correct but would result in a multitude of exceptions, which
     * could be undesired.
     * </p>
     */
    private final String variant;

    /**
     * The field firstAppearance holds the date representing the first day of
     * the period of time represented by this block. The date is treated as
     * inclusive.
     */
    private LocalDate firstAppearance;

    /**
     * The field lastAppearance holds the date representing the last day of the
     * period of time represented by this block. The date is treated as
     * inclusive.
     */
    private LocalDate lastAppearance;

    /**
     * The field issues holds the issues that have appeared during the period of
     * time represented by this block.
     */
    private List<Issue> issues;

    /**
     * Default constructor. Creates a Block object without any data.
     *
     * @param course
     *            course this block is in
     */
    public Block(Course course) {
        this.course = course;
        this.variant = null;
        this.firstAppearance = null;
        this.lastAppearance = null;
        this.issues = new ArrayList<>();
    }

    /**
     * Constructor for a block with a given variant identifier.
     *
     * @param course
     *            course this block is in
     * @param variant
     *            a variant identifier (may be null)
     */
    public Block(Course course, String variant) {
        this.course = course;
        this.variant = variant;
        this.firstAppearance = null;
        this.lastAppearance = null;
        this.issues = new ArrayList<>();
    }

    /**
     * The function addIssue() adds an Issue to this block if it is not already
     * present.
     *
     * @param issue
     *            Issue to add
     * @return true if the set was changed
     */
    public boolean addIssue(Issue issue) {
        clearProcessesIfNecessary(issue);
        return issues.add(issue);
    }

    /**
     * When a course of appearance has been loaded from a file or the processes
     * list has already been generated, it already contains issues which must be
     * deleted in the case that an issue is added to or removed from the course
     * of appearance which is producing issues in the selected time range. If
     * the time range cannot be evaluated because either of the variables is
     * null, we go the safe way and delete, too.
     *
     * @param issue
     *            issue to add or delete
     */
    private void clearProcessesIfNecessary(Issue issue) {
        try {
            if (issue.countIndividualIssues(firstAppearance, lastAppearance) > 0) {
                course.clearProcesses();
            }
        } catch (RuntimeException e) {
            // if firstAppearance or lastAppearance is null
            course.clearProcesses();
        }
    }

    /**
     * The function clone() creates and returns a copy of this Block.
     *
     * @param course
     *            Course this block belongs to
     * @return a copy of this
     */
    public Block clone(Course course) {
        Block copy = new Block(course);
        copy.firstAppearance = firstAppearance;
        copy.lastAppearance = lastAppearance;
        ArrayList<Issue> copiedIssues = new ArrayList<>(issues.size() > 10 ? issues.size() : 10);
        for (Issue issue : issues) {
            copiedIssues.add(issue.clone(course));
        }
        copy.issues = copiedIssues;
        return copy;
    }

    /**
     * The function countIndividualIssues() determines how many stampings of
     * issues physically appeared without generating a list of IndividualIssue
     * objects.
     *
     * @return the count of issues
     */
    public long countIndividualIssues() {
        if (firstAppearance == null || lastAppearance == null) {
            return 0;
        }
        long result = 0;
        for (LocalDate day = firstAppearance; !day.isAfter(lastAppearance); day = day.plusDays(1)) {
            for (Issue issue : getIssues()) {
                if (issue.isMatch(day)) {
                    result += 1;
                }
            }
        }
        return result;
    }

    /**
     * The function getIssues() returns the list of issues contained in this
     * Block.
     *
     * @return the list of issues from this Block
     */
    public List<Issue> getIssues() {
        return new ArrayList<>(issues);
    }

    /**
     * The function getIndividualIssues() generates a list of IndividualIssue
     * objects for a given day, each of them representing a stamping of one
     * physically appeared issue.
     *
     * @param date
     *            date to generate issues for
     * @return a List of IndividualIssue objects, each of them representing one
     *         physically appeared issue
     */
    public List<IndividualIssue> getIndividualIssues(LocalDate date) {
        if (!isMatch(date)) {
            return Collections.emptyList();
        }
        ArrayList<IndividualIssue> result = new ArrayList<>(issues.size());
        for (Issue issue : getIssues()) {
            if (issue.isMatch(date)) {
                result.add(new IndividualIssue(this, issue, date));
            }
        }
        return result;
    }

    /**
     * The function getIssue() returns an issue from the Block by the issue’s
     * heading, or null if the block doesn’t contain an issue with that heading.
     *
     * @param heading
     *            Heading of the issue to look for
     * @return Issue with that heading
     */
    public Issue getIssue(String heading) {
        for (Issue issue : issues) {
            if (heading.equals(issue.getHeading())) {
                return issue;
            }
        }
        return null;
    }

    /**
     * The function getFirstAppearance() returns the date the regularity of this
     * block begins with.
     *
     * @return the date of first appearance
     */
    public LocalDate getFirstAppearance() {
        return firstAppearance;
    }

    /**
     * The function getLastAppearance() returns the date the regularity of this
     * block ends with.
     *
     * @return the date of last appearance
     */
    public LocalDate getLastAppearance() {
        return lastAppearance;
    }

    /**
     * The function isEmpty() returns whether the block is in an empty state or
     * not.
     *
     * @return whether the block is dataless
     */
    public boolean isEmpty() {
        return firstAppearance == null && lastAppearance == null && (issues == null || issues.isEmpty());
    }

    /**
     * Can be used to find out whether the given variant string equals the
     * variant assigned to this block in a NullPointerException-safe way.
     *
     * @param variant
     *            variant to compare against
     * @return whether the given string is equals to the assigned variant
     */
    public boolean isIdentifiedBy(String variant) {
        return Objects.isNull(variant) && Objects.isNull(this.variant)
                || Objects.nonNull(this.variant) && this.variant.equals(variant);
    }

    /**
     * The function isMatch() returns whether a given LocalDate comes within the
     * limits of this block. Defaults to false if either the argument or one of
     * the fields to compare against is null.
     *
     * @param date
     *            a LocalDate to examine
     * @return whether the date is within the limits of this block
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
     * the Block. This is especially sensible to detect the underlying
     * regularity after lots of issues whose existence is known have been added
     * one by one as additions to the underlying issue(s).
     */
    public void recalculateRegularityOfIssues() {
        for (Issue issue : issues) {
            issue.recalculateRegularity(firstAppearance, lastAppearance);
        }
    }

    /**
     * The function removeIssue() removes the specified Issue from this Block if
     * it is present.
     *
     * @param issue
     *            Issue to be removed from the set
     * @return true if the set was changed
     */
    public boolean removeIssue(Issue issue) {
        clearProcessesIfNecessary(issue);
        return issues.remove(issue);
    }

    /**
     * The method setFirstAppearance() sets a LocalDate as day of first
     * appearance for this Block.
     *
     * @param firstAppearance
     *            date of first appearance
     * @throws IllegalArgumentException
     *             if the date would overlap with another block
     */
    public void setFirstAppearance(LocalDate firstAppearance) {
        prohibitOverlaps(firstAppearance, lastAppearance != null ? lastAppearance : firstAppearance);
        try {
            if (!this.firstAppearance.equals(firstAppearance)) {
                course.clearProcesses();
            }
        } catch (NullPointerException e) {
            if (this.firstAppearance == null ^ firstAppearance == null) {
                course.clearProcesses();
            }
        }
        this.firstAppearance = firstAppearance;
    }

    /**
     * The method setLastAppearance() sets a LocalDate as day of last appearance
     * for this Block.
     *
     * @param lastAppearance
     *            date of last appearance
     * @throws IllegalArgumentException
     *             if the date would overlap with another block
     */
    public void setLastAppearance(LocalDate lastAppearance) {
        prohibitOverlaps(firstAppearance != null ? firstAppearance : lastAppearance, lastAppearance);
        try {
            if (!this.lastAppearance.equals(lastAppearance)) {
                course.clearProcesses();
            }
        } catch (NullPointerException e) {
            if (this.lastAppearance == null ^ lastAppearance == null) {
                course.clearProcesses();
            }
        }
        this.lastAppearance = lastAppearance;
    }

    /**
     * The method setPublicationPeriod() sets two LocalDate instances as days of
     * first and last appearance for this Block.
     *
     * @param firstAppearance
     *            date of first appearance
     * @param lastAppearance
     *            date of last appearance
     * @throws IllegalArgumentException
     *             if the date would overlap with another block
     */
    public void setPublicationPeriod(LocalDate firstAppearance, LocalDate lastAppearance) {
        prohibitOverlaps(firstAppearance, lastAppearance);
        try {
            if (!this.firstAppearance.equals(firstAppearance)) {
                course.clearProcesses();
            }
        } catch (NullPointerException e) {
            if (this.firstAppearance == null ^ firstAppearance == null) {
                course.clearProcesses();
            }
        }
        try {
            if (!this.lastAppearance.equals(lastAppearance)) {
                course.clearProcesses();
            }
        } catch (NullPointerException e) {
            if (this.lastAppearance == null ^ lastAppearance == null) {
                course.clearProcesses();
            }
        }
        this.firstAppearance = firstAppearance;
        this.lastAppearance = lastAppearance;
    }

    /**
     * The method checkForOverlaps() tests an not yet set time range for this
     * block whether it doesn’t overlap with other titles in this course and can
     * be set. (Because this method is called prior to setting a new value as a
     * field value, it doesn’t take the values from the classes’ fields even
     * though it isn’t static.) If the given dates would cause an overlapping,
     * an IllegalArgumentException will be thrown.
     *
     * @param from
     *            date of first appearance to check
     * @param until
     *            date of last appearance to check
     * @throws IllegalArgumentException
     *             if the check fails
     */
    private void prohibitOverlaps(LocalDate from, LocalDate until) {
        for (Block block : course) {
            if (!block.equals(this) && (block.getFirstAppearance().isBefore(until)
                    && !block.getLastAppearance().isBefore(from)
                    || (block.getLastAppearance().isAfter(from) && !block.getFirstAppearance().isAfter(until)))) {
                throw new IllegalArgumentException(
                        '(' + block.variant + ") " + block.firstAppearance + " - " + block.lastAppearance);
            }
        }
    }

    /**
     * The function toString() provides returns a string that contains a concise
     * but informative representation of this block that is easy for a person to
     * read.
     *
     * @return a string representation of the block
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (firstAppearance != null) {
            result.append(firstAppearance.toString());
        }
        result.append(" - ");
        if (lastAppearance != null) {
            result.append(lastAppearance.toString());
        }
        result.append(" [");
        boolean first = true;
        for (Issue issue : issues) {
            if (!first) {
                result.append(", ");
            }
            result.append(issue.toString());
            first = false;
        }
        result.append("]");
        return result.toString();
    }

    /**
     * The function toString() provides returns a string that contains a textual
     * representation of this block that is easy for a person to read.
     *
     * @param dateConverter
     *            a DateTimeFormatter for formatting the local dates
     * @return a string to identify the block
     */
    public String toString(DateTimeFormatter dateConverter) {
        StringBuilder result = new StringBuilder();
        if (firstAppearance != null) {
            result.append(dateConverter.print(firstAppearance));
        }
        result.append(" − ");
        if (lastAppearance != null) {
            result.append(dateConverter.print(lastAppearance));
        }
        return result.toString();
    }

    /**
     * Returns a hash code for the object which depends on the content of its
     * variables. Whenever Block objects are held in HashSet objects, a
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
        result = prime * result + ((issues == null) ? 0 : issues.hashCode());
        result = prime * result + ((lastAppearance == null) ? 0 : lastAppearance.hashCode());
        result = prime * result + ((variant == null) ? 0 : variant.hashCode());
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

        if (!(obj instanceof Block)) {
            return false;
        }

        Block other = (Block) obj;
        if (firstAppearance == null) {
            if (other.firstAppearance != null) {
                return false;
            }
        } else if (!firstAppearance.equals(other.firstAppearance)) {
            return false;
        }
        if (issues == null) {
            if (other.issues != null) {
                return false;
            }
        } else if (!issues.equals(other.issues)) {
            return false;
        }
        if (lastAppearance == null) {
            if (other.lastAppearance != null) {
                return false;
            }
        } else if (!lastAppearance.equals(other.lastAppearance)) {
            return false;
        }
        if (variant == null) {
            return other.variant == null;
        } else {
            return variant.equals(other.variant);
        }
    }
}
