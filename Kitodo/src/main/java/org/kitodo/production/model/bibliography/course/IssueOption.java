package org.kitodo.production.model.bibliography.course;

import java.time.LocalDate;

/**
 * The class IssuesOption represents the option that an Issue may have been
 * issued on a certain day in history.
 */
public class IssueOption {
    /**
     * The field color holds the colour representative for optically
     * distinguishing the given issue.
     */
    private final String color;

    /**
     * The field date holds the date of this possible issue in the course of
     * time.
     */
    private final LocalDate date;

    /**
     * The field issue holds the issue this that this possible issue would
     * be of.
     */
    private final Issue issue;

    /**
     * Constructor for an IssueOption.
     *
     * @param controller
     *            IssueController class for that issue
     * @param date
     *            date of the issue option
     */
    public IssueOption(IssueController controller, LocalDate date) {
        this.color = controller.getColour();
        this.issue = controller.getIssue();
        this.date = date;
    }

    /**
     * Returns a color representative for
     * optically distinguishing the given issue as read-only property
     * “color”.
     *
     * @return the HTML color code of the issue
     */
    public String getColor() {
        return color;
    }

    /**
     * Returns the issue’s name as read-only
     * property “issue”.
     *
     * @return the issue’s name
     */
    public String getIssue() {
        return issue.getHeading();
    }

    /**
     * Returns whether the issue appeared on the
     * given date as read-only property “selected”, taking into
     * consideration the daysOfWeek of regular appearance, the Set of
     * exclusions and the Set of additions.
     *
     * @return whether the issue appeared that day
     */
    public boolean getSelected() {
        return issue.isMatch(date);
    }

    /**
     * The method is executed if the user clicks an issue
     * option in unselected state. If this is an exception, the exception
     * will be removed. Otherwise, an additional issue will be added.
     */
    public void select() {
        if (issue.isDayOfWeek(date.getDayOfWeek().getValue())) {
            issue.removeExclusion(date);
        } else {
            issue.addAddition(date);
        }
    }

    /**
     * The method is executed if the user clicks an issue
     * option in selected state. If this is regular appearance of that
     * issue, an exception will be added. Otherwise, the additional issue
     * will be removed.
     */
    public void unselect() {
        if (issue.isDayOfWeek(date.getDayOfWeek().getValue())) {
            issue.addExclusion(date);
        } else {
            issue.removeAddition(date);
        }
    }
}

