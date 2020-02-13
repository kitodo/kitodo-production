package org.kitodo.production.model.bibliography.course;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Cell {
    /**
     * The field date holds the date that this cell represents in the course
     * of time.
     */
    protected LocalDate date = null;

    /**
     * The field issues holds the possible issues for that day.
     */
    protected List<Issue> issues = Collections.emptyList();

    /**
     * The field onBlock contains the statement, whether the day is covered
     * by the currently showing block (or otherwise needs to be greyed-out
     * in the front end).
     */
    // do not grey out dates which aren't defined by the calendar system
    protected boolean onBlock = true;

    /**
     * Returns the day of month (that is a number in
     * 1−31) of the date the cell represents, followed by a full stop, as
     * read-only property "day". For cells which are undefined by the
     * calendar system, it returns the empty String.
     *
     * @return the day of month in enumerative form
     */
    public String getDay() {
        if (Objects.isNull(date)) {
            return null;
        }
        return Integer.toString(date.getDayOfMonth()).concat(".");
    }

    /**
     * Returns the issues that may have appeared on
     * that day as read-only field “issues”.
     *
     * @return the issues optionally appeared that day
     */
    public List<Issue> getIssues() {
        return issues;
    }

    /**
     * The function getStyleClass returns the CSS class names to be printed
     * into the HTML to display the table cell state as read-only property
     * “styleClass”.
     *
     * @return the cell’s CSS style class name
     */
    public String getStyleClass() {
        if (Objects.isNull(date)) {
            return null;
        }
        if (onBlock) {
            switch (date.getDayOfWeek()) {
                case SATURDAY:
                    return "saturday";
                case SUNDAY:
                    return "sunday";
                default:
                    return "weekday";
            }
        } else {
            switch (date.getDayOfWeek()) {
                case SATURDAY:
                    return "saturdayNoBlock";
                case SUNDAY:
                    return "sundayNoBlock";
                default:
                    return "weekdayNoBlock";
            }
        }
    }

    /**
     * Sets the date represented by this calendar sheet
     * cell.
     *
     * @param date
     *            the date represented by this calendar sheet cell
     */
    public void setDate(LocalDate date) {
        this.date = date;
    }

    /**
     * Sets the list of possible issues for the date
     * represented by this calendar sheet cell.
     *
     * @param issues
     *            the list of issues possible in this cell
     */
    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    /**
     * Can be used to change the piece of
     * information whether the day is covered by the currently showing block
     * or not.
     *
     * @param onBlock
     *            whether the day is covered by the currently showing block
     */
    public void setOnBlock(boolean onBlock) {
        this.onBlock = onBlock;
    }
}
