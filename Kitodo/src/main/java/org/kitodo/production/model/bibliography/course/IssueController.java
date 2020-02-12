package org.kitodo.production.model.bibliography.course;

public class IssueController {
    /**
     * The field index holds a consecutive index representing its position
     * in the list of issues held by the block.
     */
    protected final int index;

    /**
     * The field issue holds the issue that is managed by this controller.
     */
    protected final Issue issue;

    /**
     * Constructor. Creates a new IssueController for the given issue and
     * sets its index value.
     *
     * @param issue
     *            Issue that shall be managed by this controller
     * @param index
     *            consecutive index of the issue in the block
     */
    public IssueController(Issue issue, int index) {
        this.issue = issue;
        this.index = index;
    }

    /**
     * Deletes the issue wrapped by this controller
     * from the set of issues held by the block currently showing.
     */
    public void deleteClick() {
        // TODO implement?
    }

    /**
     * Returns a color representative for optically
     * distinguishing the given issue as read-only property "colour".
     *
     * @return the HTML color code of the issue
     */
    public String getColour() {
        return "";
        // TODO implement
        //return issueColours[index % issueColours.length];
    }

    /**
     * Returns whether the issue held by this
     * controller regularly appears on Fridays as read-write property
     * "friday".
     *
     * @return whether the issue appears on Fridays
     */
    public boolean getFriday() {
        return issue.isFriday();
    }

    /**
     * Returns the issue’s name as read-write
     * property "heading".
     *
     * @return the issue’s name
     */
    public String getHeading() {
        return issue.getHeading();
    }

    /**
     * Returns the issue held by this controller.
     *
     * @return the issue managed by this adapter
     */
    public Issue getIssue() {
        return issue;
    }

    /**
     * Returns whether the issue held by this
     * controller regularly appears on Mondays as read-write property
     * "monday".
     *
     * @return whether the issue appears on Mondays
     */
    public boolean getMonday() {
        return issue.isMonday();
    }

    /**
     * Returns whether the issue held by this
     * controller regularly appears on Saturdays as read-write property
     * "saturday".
     *
     * @return whether the issue appears on Saturdays
     */
    public boolean getSaturday() {
        return issue.isSaturday();
    }

    /**
     * Returns whether the issue held by this
     * controller regularly appears on Sundays as read-write property
     * "sunday".
     *
     * @return whether the issue appears on Sundays
     */
    public boolean getSunday() {
        return issue.isSunday();
    }

    /**
     * Returns whether the issue held by this
     * controller regularly appears on Thursdays as read-write property
     * "thursday".
     *
     * @return whether the issue appears on Thursdays
     */
    public boolean getThursday() {
        return issue.isThursday();
    }

    /**
     * Returns whether the issue held by this
     * controller regularly appears on Tuesdays as read-write property
     * "tuesday".
     *
     * @return whether the issue appears on Tuesdays
     */
    public boolean getTuesday() {
        return issue.isTuesday();
    }

    /**
     * Returns whether the issue held by this
     * controller regularly appears on Wednesdays as read-write property
     * "wednesday".
     *
     * @return whether the issue appears on Wednesdays
     */
    public boolean getWednesday() {
        return issue.isWednesday();
    }

    /**
     * The method will be called by Faces to store a new value
     * of the read-write property "friday" which represents whether the
     * issue held by this controller regularly appears on Fridays.
     *
     * @param appears
     *            whether the issue appears on Fridays
     */
    public void setFriday(boolean appears) {
        if (appears) {
            issue.addFriday();
        } else {
            issue.removeFriday();
        }
    }

    /**
     * The method will be called by Faces to store a new value
     * of the read-write property "heading" which represents the issue’s
     * name.
     *
     * @param heading
     *            heading to be used
     */
    public void setHeading(String heading) {
        issue.setHeading(heading);
    }

    /**
     * The method will be called by Faces to store a new value
     * of the read-write property "monday" which represents whether the
     * issue held by this controller regularly appears on Mondays.
     *
     * @param appears
     *            whether the issue appears on Mondays
     */
    public void setMonday(boolean appears) {
        if (appears) {
            issue.addMonday();
        } else {
            issue.removeMonday();
        }
    }

    /**
     * The method will be called by Faces to store a new value
     * of the read-write property "saturday" which represents whether the
     * issue held by this controller regularly appears on Saturdays.
     *
     * @param appears
     *            whether the issue appears on Saturdays
     */
    public void setSaturday(boolean appears) {
        if (appears) {
            issue.addSaturday();
        } else {
            issue.removeSaturday();
        }
    }

    /**
     * The method will be called by Faces to store a new value
     * of the read-write property "sunday" which represents whether the
     * issue held by this controller regularly appears on Sundays.
     *
     * @param appears
     *            whether the issue appears on Sundays
     */
    public void setSunday(boolean appears) {
        if (appears) {
            issue.addSunday();
        } else {
            issue.removeSunday();
        }
    }

    /**
     * The method will be called by Faces to store a new value
     * of the read-write property "thursday" which represents whether the
     * issue held by this controller regularly appears on Thursdays.
     *
     * @param appears
     *            whether the issue appears on Thursdays
     */
    public void setThursday(boolean appears) {
        if (appears) {
            issue.addThursday();
        } else {
            issue.removeThursday();
        }
    }

    /**
     * The method will be called by Faces to store a new value
     * of the read-write property "tuesday" which represents whether the
     * issue held by this controller regularly appears on Tuesdays.
     *
     * @param appears
     *            whether the issue appears on Tuesdays
     */
    public void setTuesday(boolean appears) {
        if (appears) {
            issue.addTuesday();
        } else {
            issue.removeTuesday();
        }
    }

    /**
     * The method will be called by Faces to store a new
     * value of the read-write property "wednesday" which represents whether
     * the issue held by this controller regularly appears on Wednesdays.
     *
     * @param appears
     *            whether the issue appears on Wednesdays
     */
    public void setWednesday(boolean appears) {
        if (appears) {
            issue.addWednesday();
        } else {
            issue.removeWednesday();
        }
    }
}
