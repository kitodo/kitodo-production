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
package org.goobi.production.model.bibliography.course.metadata;

import org.apache.commons.lang3.tuple.Pair;
import org.goobi.production.model.bibliography.course.*;
import org.joda.time.*;
import org.joda.time.format.*;
import org.kitodo.production.lugh.pagination.Paginator;

import ugh.dl.MetadataType;

/**
 * Generic meta-data that is created using a counter.
 */
public class CountableMetadata {
    /**
     * Block this meta-data counter belongs to. The block is needed to have
     * access to the other issues, which—together with the start value and step
     * size—define the value of the counter on a given day.
     */
    private Block block;

    /**
     * Date and issue this counter appears the first time.
     */
    private Pair<LocalDate, Issue> create;

    /**
     * Date and issue this counter does no longer appear on. May be null, indicating that no end issue has been set.
     */
    private Pair<LocalDate, Issue> delelte;

    /**
     * Metadata type to create with this counter.
     */
    private MetadataType metadataType;

    /**
     * Value to start counting with.
     */
    private String startValue = "";

    /**
     * Step size of the counter (for each issue, or for each day, or for each
     * week, etc.) May be null for meta-data constants which are not
     * incremented.
     */
    private Granularity stepSize;

    /**
     * Creates a new countable metadata.
     *
     * @param block
     *            Block this metadata counter is defined in
     * @param create
     *            first issue to be counted
     */
    public CountableMetadata(Block block, Pair<LocalDate, Issue> create) {
        this.block = block;
        this.create = create;
    }

    /**
     * Returns the edit mode for the given issue, or {@code null} if the
     * meta-data is not at this issue.
     *
     * @param selectedIssue
     *            issue to return the edit mode for
     * @return the edit mode for that issue
     */
    public MetadataEditMode getEditMode(Pair<LocalDate, Issue> selectedIssue) {
        int creation = new IssueComparator(block).compare(selectedIssue, create);
        if (creation < 0) {
            return MetadataEditMode.HIDDEN;
        }
        if (creation == 0) {
            return MetadataEditMode.DEFINE;
        }
        int deletion = new IssueComparator(block).compare(selectedIssue, delelte);
        if (deletion < 0) {
            return MetadataEditMode.CONTINUE;
        } else if (deletion == 0) {
            if (block.getMetadata(metadataType, selectedIssue, true) != null) {
                return MetadataEditMode.HIDDEN;
            } else {
                return MetadataEditMode.DELETE;
            }
        } else {
            return MetadataEditMode.HIDDEN;
        }
    }

    /**
     * Returns the meta-data type of this meta-data.
     *
     * @return the meta-data type of this meta-data
     */
    public MetadataType getMetadataType() {
        return metadataType;
    }

    /**
     * Returns the start value of the countable meta-data.
     *
     * @return the start value
     */
    public String getStartValue() {
        return startValue;
    }

    /**
     * Returns the step size for counting this meta-data.
     *
     * @return the step size
     */
    public Granularity getStepSize() {
        return stepSize;
    }

    /**
     * Returns the counter value for a given issue.
     *
     * @param selectedIssue
     *            issue to return the counter value for
     * @param yearStart
     *            first day of the year
     * @return the counter value for that issue
     */
    public String getValue(Pair<LocalDate, Issue> selectedIssue, MonthDay yearStart) {
        assert new IssueComparator(block).compare(selectedIssue, create) >= 0;
        Paginator values = new Paginator(startValue);
        int breakMark = 0;
        for (LocalDate i = create.getLeft(); i.compareTo(selectedIssue.getLeft()) <= 0; i = i.plusDays(1)) {
            boolean first = i.equals(create.getLeft());
            for (IndividualIssue issue : block.getIndividualIssues(i)) {
                if (first && block.getIssueIndex(issue.getIssue()) < block.getIssueIndex(create.getRight())) {
                    continue;
                } else if (first) {
                    if (stepSize != null) {
                        first = false;
                        breakMark = issue.getBreakMark(stepSize, yearStart);
                    }
                } else if (stepSize != null) {
                    int currentBreakMark = issue.getBreakMark(stepSize, yearStart);
                    if (breakMark != currentBreakMark) {
                        values.next();
                        breakMark = currentBreakMark;
                    }
                }
                if (new IssueComparator(block).compare(selectedIssue, Pair.of(i, issue.getIssue())) == 0) {
                    return values.next();
                }
            }
        }
        throw new IllegalStateException("Issue “" + selectedIssue.getRight().getHeading() + "” not found on "
                + DateTimeFormat.mediumDate().print(selectedIssue.getLeft()));
    }

    /**
     * Returns true, if the given metadataType and time point of creation or
     * deletion match.
     *
     * @param metadataType
     *            meta-data type to compare, null will be true with any
     *            meta-data type
     * @param issue
     *            issue time point
     * @param create
     *            if true, compare create time, else compare delete time
     * @return true, if the given metadataType and time point of creation match
     */
    public boolean matches(MetadataType metadataType, Pair<LocalDate, Issue> issue, Boolean create) {
        return (metadataType == null || metadataType.equals(this.metadataType))
                && (null == create && new IssueComparator(block).compare(this.create, issue) <= 0
                        && (delelte == null || new IssueComparator(block).compare(issue, delelte) < 0)
                        || Boolean.TRUE.equals(create) && this.create.equals(issue) || Boolean.FALSE.equals(create)
                                && (issue == null && this.delelte == null || issue.equals(this.delelte)));
    }

    /**
     * Sets the delete of the countable meta-data.
     *
     * @param delete
     *            the delete to set
     */
    public void setDelete(Pair<LocalDate, Issue> delete) {
        this.delelte = delete;
    }

    /**
     * Sets the metadata type of the countable metadata.
     *
     * @param metadataType
     *            the metadata type to set
     */
    public void setMetadataType(MetadataType metadataType) {
        this.metadataType = metadataType;
    }

    /**
     * Sets the step size of the countable metadata
     *
     * @param stepSize
     *            the step size to set
     */
    public void setStepSize(Granularity stepSize) {
        this.stepSize = stepSize;
    }

    /**
     * Sets the start value of the countable metadata
     *
     * @param startValue
     *            the start value to set
     */
    public void setStartValue(String startValue) {
        this.startValue = startValue;
    }

    /**
     * Returns a human-readable concise description of this countable meta-data.
     *
     * @retun a human-readable description of this meta-data
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(startValue);
        stringBuilder.append(" from ");
        stringBuilder.append(ISODateTimeFormat.date().print(create.getLeft()));
        stringBuilder.append(", ");
        stringBuilder.append(create.getRight().getHeading());
        if (delelte == null) {
            stringBuilder.append(" infinitely");
        } else {
            stringBuilder.append(" to ");
            stringBuilder.append(ISODateTimeFormat.date().print(delelte.getLeft()));
            stringBuilder.append(", ");
            stringBuilder.append(delelte.getRight().getHeading());
        }
        stringBuilder.append(", step size ");
        stringBuilder.append(stepSize);
        return stringBuilder.toString();
    }
}
