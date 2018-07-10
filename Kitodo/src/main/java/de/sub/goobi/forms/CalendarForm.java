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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.helper.DateUtils;
import de.sub.goobi.helper.FacesUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.XMLUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.goobi.production.model.bibliography.course.Block;
import org.goobi.production.model.bibliography.course.Course;
import org.goobi.production.model.bibliography.course.Granularity;
import org.goobi.production.model.bibliography.course.Issue;
import org.joda.time.DateTimeConstants;
import org.joda.time.IllegalFieldValueException;
import org.joda.time.LocalDate;
import org.joda.time.ReadablePartial;
import org.kitodo.config.DefaultValues;
import org.kitodo.config.Parameters;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The class CalendarForm provides the screen logic for a JSF calendar editor to
 * enter the course of appearance of a newspaper.
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
@Named("CalendarForm")
@SessionScoped
public class CalendarForm implements Serializable {
    private static final long serialVersionUID = -1267629887357681855L;
    private static final String BLOCK = "calendar.block.";
    private static final String BLOCK_NEGATIVE = BLOCK + "negative";
    private static final String UPLOAD_ERROR = "calendar.upload.error";

    /**
     * The class Cell represents a single table cell on the calendar sheet.
     *
     * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
     */
    public static class Cell {
        /**
         * The field date holds the date that this cell represents in the course
         * of time.
         */
        protected LocalDate date = null;

        /**
         * The field issues holds the possible issues for that day.
         */
        protected List<IssueOption> issues = Collections.emptyList();

        /**
         * The field onBlock contains the statement, whether the day is covered
         * by the currently showing block (or otherwise needs to be greyed-out
         * in the front end).
         */
        // do not grey out dates which aren't defined by the calendar system
        protected boolean onBlock = true;

        /**
         * The function getDay() returns the day of month (that is a number in
         * 1−31) of the date the cell represents, followed by a full stop, as
         * read-only property "day". For cells which are undefined by the
         * calendar system, it returns the empty String.
         *
         * @return the day of month in enumerative form
         */
        public String getDay() {
            if (date == null) {
                return null;
            }
            return Integer.toString(date.getDayOfMonth()).concat(".");
        }

        /**
         * The function getIssues() returns the issues that may have appeared on
         * that day as read-only field “issues”.
         *
         * @return the issues optionally appeared that day
         */
        public List<IssueOption> getIssues() {
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
            if (date == null) {
                return null;
            }
            if (onBlock) {
                switch (date.getDayOfWeek()) {
                    case DateTimeConstants.SATURDAY:
                        return "saturday";
                    case DateTimeConstants.SUNDAY:
                        return "sunday";
                    default:
                        return "weekday";
                }
            } else {
                switch (date.getDayOfWeek()) {
                    case DateTimeConstants.SATURDAY:
                        return "saturdayNoBlock";
                    case DateTimeConstants.SUNDAY:
                        return "sundayNoBlock";
                    default:
                        return "weekdayNoBlock";
                }
            }
        }

        /**
         * The method setDate() sets the date represented by this calendar sheet
         * cell.
         *
         * @param date
         *            the date represented by this calendar sheet cell
         */
        protected void setDate(LocalDate date) {
            this.date = date;
        }

        /**
         * The method setIssues() sets the list of possible issues for the date
         * represented by this calendar sheet cell.
         *
         * @param issues
         *            the list of issues possible in this cell
         */
        protected void setIssues(List<IssueOption> issues) {
            this.issues = issues;
        }

        /**
         * The method setOnBlock() can be used to change the piece of
         * information whether the day is covered by the currently showing block
         * or not.
         *
         * @param onBlock
         *            whether the day is covered by the currently showing block
         */
        protected void setOnBlock(boolean onBlock) {
            this.onBlock = onBlock;
        }
    }

    /**
     * The class IssueController backs the control elements that are necessary
     * to manage the properties of an issue using Faces, including the option of
     * its deletion.
     *
     * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
     */
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
        protected IssueController(Issue issue, int index) {
            this.issue = issue;
            this.index = index;
        }

        /**
         * The method deleteClick() deletes the issue wrapped by this controller
         * from the set of issues held by the block currently showing.
         */
        public void deleteClick() {
            blockShowing.removeIssue(issue);
        }

        /**
         * The method getColour() returns a colour representative for optically
         * distinguishing the given issue as read-only property "colour".
         *
         * @return the HTML colour code of the issue
         */
        public String getColour() {
            return issueColours[index % issueColours.length];
        }

        /**
         * The function getFriday() returns whether the issue held by this
         * controller regularly appears on Fridays as read-write property
         * "friday".
         *
         * @return whether the issue appears on Fridays
         */
        public boolean getFriday() {
            return issue.isFriday();
        }

        /**
         * The function getHeading() returns the issue’s name as read-write
         * property "heading".
         *
         * @return the issue’s name
         */
        public String getHeading() {
            return issue.getHeading();
        }

        /**
         * The function getIssue() returns the issue held by this controller.
         *
         * @return the issue managed by this adapter
         */
        protected Issue getIssue() {
            return issue;
        }

        /**
         * The function getMonday() returns whether the issue held by this
         * controller regularly appears on Mondays as read-write property
         * "monday".
         *
         * @return whether the issue appears on Mondays
         */
        public boolean getMonday() {
            return issue.isMonday();
        }

        /**
         * The function getSaturday() returns whether the issue held by this
         * controller regularly appears on Saturdays as read-write property
         * "saturday".
         *
         * @return whether the issue appears on Saturdays
         */
        public boolean getSaturday() {
            return issue.isSaturday();
        }

        /**
         * The function getSunday() returns whether the issue held by this
         * controller regularly appears on Sundays as read-write property
         * "sunday".
         *
         * @return whether the issue appears on Sundays
         */
        public boolean getSunday() {
            return issue.isSunday();
        }

        /**
         * The function getThursday() returns whether the issue held by this
         * controller regularly appears on Thursdays as read-write property
         * "thursday".
         *
         * @return whether the issue appears on Thursdays
         */
        public boolean getThursday() {
            return issue.isThursday();
        }

        /**
         * The function getTuesday() returns whether the issue held by this
         * controller regularly appears on Tuesdays as read-write property
         * "tuesday".
         *
         * @return whether the issue appears on Tuesdays
         */
        public boolean getTuesday() {
            return issue.isTuesday();
        }

        /**
         * The function getWednesday() returns whether the issue held by this
         * controller regularly appears on Wednesdays as read-write property
         * "wednesday".
         *
         * @return whether the issue appears on Wednesdays
         */
        public boolean getWednesday() {
            return issue.isWednesday();
        }

        /**
         * The method setFriday() will be called by Faces to store a new value
         * of the read-write property "friday" which represents whether the
         * issue held by this controller regularly appears on Fridays.
         *
         * @param appears
         *            whether the issue appears on Fridays
         */
        public void setFriday(boolean appears) {
            if (blockChangerUnchanged) {
                if (appears) {
                    issue.addFriday();
                } else {
                    issue.removeFriday();
                }
            }
        }

        /**
         * The method setMonday() will be called by Faces to store a new value
         * of the read-write property "heading" which represents the issue’s
         * name.
         *
         * @param heading
         *            heading to be used
         */
        public void setHeading(String heading) {
            if (blockChangerUnchanged) {
                issue.setHeading(heading);
            }
        }

        /**
         * The method setMonday() will be called by Faces to store a new value
         * of the read-write property "monday" which represents whether the
         * issue held by this controller regularly appears on Mondays.
         *
         * @param appears
         *            whether the issue appears on Mondays
         */
        public void setMonday(boolean appears) {
            if (blockChangerUnchanged) {
                if (appears) {
                    issue.addMonday();
                } else {
                    issue.removeMonday();
                }
            }
        }

        /**
         * The method setSaturday() will be called by Faces to store a new value
         * of the read-write property "saturday" which represents whether the
         * issue held by this controller regularly appears on Saturdays.
         *
         * @param appears
         *            whether the issue appears on Saturdays
         */
        public void setSaturday(boolean appears) {
            if (blockChangerUnchanged) {
                if (appears) {
                    issue.addSaturday();
                } else {
                    issue.removeSaturday();
                }
            }
        }

        /**
         * The method setSunday() will be called by Faces to store a new value
         * of the read-write property "sunday" which represents whether the
         * issue held by this controller regularly appears on Sundays.
         *
         * @param appears
         *            whether the issue appears on Sundays
         */
        public void setSunday(boolean appears) {
            if (blockChangerUnchanged) {
                if (appears) {
                    issue.addSunday();
                } else {
                    issue.removeSunday();
                }
            }
        }

        /**
         * The method setThursday() will be called by Faces to store a new value
         * of the read-write property "thursday" which represents whether the
         * issue held by this controller regularly appears on Thursdays.
         *
         * @param appears
         *            whether the issue appears on Thursdays
         */
        public void setThursday(boolean appears) {
            if (blockChangerUnchanged) {
                if (appears) {
                    issue.addThursday();
                } else {
                    issue.removeThursday();
                }
            }
        }

        /**
         * The method setTuesday() will be called by Faces to store a new value
         * of the read-write property "tuesday" which represents whether the
         * issue held by this controller regularly appears on Tuesdays.
         *
         * @param appears
         *            whether the issue appears on Tuesdays
         */
        public void setTuesday(boolean appears) {
            if (blockChangerUnchanged) {
                if (appears) {
                    issue.addTuesday();
                } else {
                    issue.removeTuesday();
                }
            }
        }

        /**
         * The method setWednesday() will be called by Faces to store a new
         * value of the read-write property "wednesday" which represents whether
         * the issue held by this controller regularly appears on Wednesdays.
         *
         * @param appears
         *            whether the issue appears on Wednesdays
         */
        public void setWednesday(boolean appears) {
            if (blockChangerUnchanged) {
                if (appears) {
                    issue.addWednesday();
                } else {
                    issue.removeWednesday();
                }
            }
        }
    }

    /**
     * The class IssuesOption represents the option that an Issue may have been
     * issued on a certain day in history.
     *
     * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
     */
    public static class IssueOption {
        /**
         * The field colour holds the colour representative for optically
         * distinguishing the given issue.
         */
        protected final String colour;

        /**
         * The field date holds the date of this possible issue in the course of
         * time.
         */
        protected final LocalDate date;

        /**
         * The field issue holds the issue this that this possible issue would
         * be of.
         */
        protected final Issue issue;

        /**
         * Constructor for an IssueOption.
         *
         * @param controller
         *            IssueController class for that issue
         * @param date
         *            date of the issue option
         */
        public IssueOption(IssueController controller, LocalDate date) {
            this.colour = controller.getColour();
            this.issue = controller.getIssue();
            this.date = date;
        }

        /**
         * The function getColour() returns a colour representative for
         * optically distinguishing the given issue as read-only property
         * “colour”.
         *
         * @return the HTML colour code of the issue
         */
        public String getColour() {
            return colour;
        }

        /**
         * The function getIssue() returns the issue’s name as read-only
         * property “issue”.
         *
         * @return the issue’s name
         */
        public String getIssue() {
            return issue.getHeading();
        }

        /**
         * The function getSelected() returns whether the issue appeared on the
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
         * The method selectClick() is executed if the user clicks an issue
         * option in unselected state. If this is an exception, the exception
         * will be removed. Otherwise, an additional issue will be added.
         */
        public void selectClick() {
            if (issue.isDayOfWeek(date.getDayOfWeek())) {
                issue.removeExclusion(date);
            } else {
                issue.addAddition(date);
            }
        }

        /**
         * The method unselectClick() is executed if the user clicks an issue
         * option in selected state. If this is regular appearance of that
         * issue, an exception will be added. Otherwise, the additional issue
         * will be removed.
         */
        public void unselectClick() {
            if (issue.isDayOfWeek(date.getDayOfWeek())) {
                issue.addExclusion(date);
            } else {
                issue.removeAddition(date);
            }
        }
    }

    /**
     * The constant field issueColours holds a regular expression to parse date
     * inputs in a flexible way.
     */
    private static final Pattern FLEXIBLE_DATE = Pattern.compile("\\D*(\\d+)\\D+(\\d+)\\D+(\\d+)\\D*");

    /**
     * The constant field issueColours holds the colours used to represent the
     * issues in the calendar editor. It is populated on form bean creation, so
     * changing the configuration should take effect without need to restart the
     * servlet container.
     */
    protected static String[] issueColours;

    private static final Logger logger = LogManager.getLogger(CalendarForm.class);

    /**
     * The constant field START_RELATION hold the date the course of publication
     * of the the German-language “Relation aller Fürnemmen und gedenckwürdigen
     * Historien”, which is often recognized as the first newspaper, began. If
     * the user tries to create a block before that date, a hint will be shown.
     */
    private static final LocalDate START_RELATION = new LocalDate(1605, 9, 12);

    /**
     * The Map blockChangerResolver is populated with the IDs used in the block
     * changer drop down element and the references to the block objects
     * referenced by the IDs for easily looking them up upon change.
     */
    protected Map<String, Block> blockChangerResolver;

    /**
     * The field blockChangerUnchanged is of importance during the update model
     * values phase of the JSF life-cycle. During that phase several setter
     * methods are sequentially called. The first method called is
     * setBlockChangerSelected(). If the user chose a different block to be
     * displayed, blockShowing will be altered. This would cause the subsequent
     * calls to other setter methods to overwrite the values in the newly
     * selected block with the values of the previously displayed block which
     * come back in in the form that is submitted by the browser if this is not
     * blocked. Therefore setBlockChangerSelected() sets blockChangerUnchanged
     * to control whether the other setter methods shall or shall not write the
     * incoming data into the respective fields.
     */
    protected boolean blockChangerUnchanged = true;

    /**
     * The field blockShowing holds the block currently showing in this calendar
     * instance. The block held in blockShowing must be part of the course
     * object, too.
     */
    protected Block blockShowing;

    /**
     * The field course holds the course of appearance currently under edit by
     * this calendar form instance.
     */
    protected Course course;

    /**
     * The field firstAppearanceInToChange is set in the setter method
     * setFirstAppearance to notify the setter method setLastAppearance that the
     * date of first appearance has to be changed. Java Server Faces tries to
     * update the data model by sequentially calling two setter methods. By
     * allowing the user to alter both fields at one time this may lead to an
     * illegal intermediate state in the data model which the latter
     * successfully rejects (which it should). Imagine the case that one block
     * is from March until September and the second one from October to
     * November. Now the second block shall be moved to January until February.
     * Setting the start date from October to January will cause an overlapping
     * state with the other block which is prohibited by definition. Therefore
     * changing the beginning date must be forwarded to the setter method to
     * change the end date to allow this change, which is allowed, if taken
     * atomically.
     */
    private LocalDate firstAppearanceIsToChange = null;

    /**
     * The constant field today hold the date of today. Reading the system clock
     * requires much synchronisation throughout the JVM and is therefore only
     * done once on form creation.
     */
    private final LocalDate today = LocalDate.now();

    protected UploadedFile uploadedFile;

    /**
     * The field uploadShowing indicates whether the dialogue box to upload a
     * course of appearance XML description is showing or not.
     */
    protected boolean uploadShowing = false;

    /**
     * The field yearShowing tells the year currently showing in this calendar
     * instance.
     */
    protected int yearShowing = 1979; // Cf. 42

    /**
     * Empty constructor. Creates a new form without yet any data.
     *
     * <p>
     * The issue colour presets are samples which have been chosen to provide
     * distinguishability also for users with red-green color vision deficiency.
     * Arbitrary colours can be defined in kitodo_config.properties by setting
     * the property “issue.colours”.
     * </p>
     */
    public CalendarForm() {
        issueColours = ConfigCore.getParameter(Parameters.ISSUE_COLOURS, DefaultValues.ISSUE_COLOURS)
                .split(";");
        course = new Course();
        blockChangerResolver = new HashMap<>();
        blockShowing = null;
    }

    /**
     * The method addIssueClick() adds a new issue to the set of issues held by
     * the block currently showing.
     */
    public void addIssueClick() {
        blockShowing.addIssue(new Issue(course));
    }

    /**
     * The method backwardClick() flips the calendar sheet back one year in
     * time.
     */
    public void backwardClick() {
        yearShowing -= 1;
    }

    /**
     * The function buildIssueOptions() creates a list of issueOptions for a
     * given date.
     *
     * @param issueControllers
     *            the list of issue controllers in question
     * @param date
     *            the date in question
     * @return a list of issue options for the date
     */
    protected List<IssueOption> buildIssueOptions(List<IssueController> issueControllers, LocalDate date) {
        List<IssueOption> result = new ArrayList<>();
        for (IssueController controller : issueControllers) {
            result.add(new IssueOption(controller, date));
        }
        return result;
    }

    /**
     * The function checkBlockPlausibility compares the dates entered against
     * some plausibility assumptions and sets hints otherwise.
     */
    private void checkBlockPlausibility() {
        if (blockShowing.getFirstAppearance() != null && blockShowing.getLastAppearance() != null) {
            if (blockShowing.getFirstAppearance().plusYears(100).isBefore(blockShowing.getLastAppearance())) {
                Helper.setMessage(BLOCK + "long");
            }
            if (blockShowing.getFirstAppearance().isAfter(blockShowing.getLastAppearance())) {
                Helper.setErrorMessage(BLOCK_NEGATIVE);
            }
            if (blockShowing.getFirstAppearance().isBefore(START_RELATION)) {
                Helper.setMessage(BLOCK + "firstAppearance.early");
            }
            if (blockShowing.getFirstAppearance().isAfter(today)) {
                Helper.setMessage(BLOCK + "firstAppearance.fiction");
            }
            if (blockShowing.getLastAppearance().isBefore(START_RELATION)) {
                Helper.setMessage(BLOCK + "lastAppearance.early");
            }
            if (blockShowing.getLastAppearance().isAfter(today)) {
                Helper.setMessage(BLOCK + "lastAppearance.fiction");
            }
        }
    }

    /**
     * The method copyBlockClick() creates and adds a copy of the currently
     * showing block.
     */
    public void copyBlockClick() {
        Block copy = blockShowing.clone(course);
        LocalDate lastAppearance = course.getLastAppearance();
        if (Objects.nonNull(lastAppearance)) {
            LocalDate firstAppearance = lastAppearance.plusDays(1);
            copy.setFirstAppearance(firstAppearance);
            copy.setLastAppearance(firstAppearance);
            course.add(copy);
            blockShowing = copy;
            navigate();
        }
    }

    /**
     * The function downloadClick() is executed if the user clicks the action
     * link to “export” the calendar data. If the course of appearance doesn’t
     * yet contain generated processes—which is always the case, except that the
     * user just came from uploading a data file and didn’t change anything
     * about it—process data will be generated. Then an XML file will be made
     * out of it and sent to the user’s browser. If the granularity was
     * temporarily added, it will be removed afterwards so that the user will
     * not be presented with the option to generate processes “as imported” if
     * he or she never ran an import before.
     *
     * <p>
     * Note: The process data will be generated with a granularity of “days”
     * (each day forms one process). This setting can be changed later after the
     * data has been re-imported, but it will remain if the user uploads the
     * saved data and then proceeds right to the next page and creates processes
     * with the granularity “as imported”. However, since this is possible
     * and—as to our knowledge in late 2014, when this was written—this is the
     * best option of all, this default has been chosen here.
     * </p>
     */
    public void downloadClick() {
        boolean granularityWasTemporarilyAdded = false;
        try {
            if (course == null || course.countIndividualIssues() == 0) {
                Helper.setErrorMessage("errorDataIncomplete", "calendar.isEmpty");
                return;
            }
            if (course.getNumberOfProcesses() == 0) {
                granularityWasTemporarilyAdded = true;
                course.splitInto(Granularity.DAYS);
            }
            byte[] data = XMLUtils.documentToByteArray(course.toXML(), 4);
            FacesUtils.sendDownload(data, "course.xml");
        } catch (TransformerException e) {
            Helper.setErrorMessage("granularity.download.error", "error.TransformerException", logger, e);
        } catch (IOException e) {
            Helper.setErrorMessage("granularity.download.error", e.getLocalizedMessage(), logger, e);
        } finally {
            if (granularityWasTemporarilyAdded) {
                course.clearProcesses();
            }
        }
    }

    /**
     * The method forwardClick() flips the calendar sheet forward one year in
     * time.
     */
    public void forwardClick() {
        yearShowing += 1;
    }

    /**
     * The function getBlank() returns whether the calendar editor is in mint
     * condition, i.e. there is no block defined yet, as read-only property
     * “blank”.
     *
     * <p>
     * Side note: “empty” is a reserved word in JSP and cannot be used as
     * property name.
     * </p>
     *
     * @return whether there is no block yet
     */
    public boolean getBlank() {
        return course.isEmpty();
    }

    /**
     * The function getBlockChangerOptions() returns the elements for the block
     * changer drop down element as read only property "blockChangerOptions". It
     * returns a List of Map with each two entries: "value" and "label". "value"
     * is the hashCode() in hex of the block which will later be used if the
     * field "blockChangerSelected" is altered to choose the currently selected
     * block, "label" is the readable description to be shown to the user.
     *
     * @return the elements for the block changer drop down element
     */
    public List<Map<String, String>> getBlockChangerOptions() {
        List<Map<String, String>> result = new ArrayList<>();
        for (Block block : course) {
            String value = Integer.toHexString(block.hashCode());
            blockChangerResolver.put(value, block);
            Map<String, String> item = new HashMap<>();
            item.put("value", value);
            item.put("label", block.toString(DateUtils.DATE_FORMATTER));
            result.add(item);
        }
        return result;
    }

    /**
     * The function getBlockChangerSelected() returns the hashCode() value of
     * the block currently selected as read-write property
     * "blockChangerSelected". If a new block is under edit, returns the empty
     * String.
     *
     * @return identifier of the selected block
     */
    public String getBlockChangerSelected() {
        return blockShowing == null ? "" : Integer.toHexString(blockShowing.hashCode());
    }

    /**
     * The function getCalendarSheet() returns the data required to build the
     * calendar sheet as read-only property "calendarSheet". The outer list
     * contains 31 entries, each representing a row of the calendar (the days
     * 1−31), each line then contains 12 cells representing the months. This is
     * due to HTML table being produced line by line.
     *
     * @return the table cells to build the calendar sheet
     */
    public List<List<Cell>> getCalendarSheet() {
        List<List<Cell>> result = getEmptySheet();
        populateByCalendar(result);
        return result;
    }

    /**
     * The function getCourse() will return the course created with this editor
     * as read-only property "course" to pass it to the next form.
     *
     * @return the course of appearance data model
     */
    public Course getCourse() {
        return course;
    }

    /**
     * The function getEmptySheet() builds the empty calendar sheet with 31 rows
     * of twelve cells with empty objects of type Cell().
     *
     * @return an empty calendar sheet
     */
    protected List<List<Cell>> getEmptySheet() {
        List<List<Cell>> result = new ArrayList<>(31);
        for (int day = 1; day <= 31; day++) {
            ArrayList<Cell> row = new ArrayList<>(DateTimeConstants.DECEMBER);
            for (int month = 1; month <= 12; month++) {
                row.add(new Cell());
            }
            result.add(row);
        }
        return result;
    }

    /**
     * The function getFirstAppearance() returns the date of first appearance of
     * the block currently showing as read-write property "firstAppearance".
     *
     * @return date of first appearance of currently showing block
     */
    public String getFirstAppearance() {
        if (blockShowing != null && blockShowing.getFirstAppearance() != null) {
            return DateUtils.DATE_FORMATTER.print(blockShowing.getFirstAppearance());
        } else {
            return null;
        }
    }

    /**
     * The function getIssues() returns the list of issues held by the block
     * currently showing as read-only property "issues".
     *
     * @return the list of issues
     */
    public List<IssueController> getIssues() {
        return blockShowing != null ? getIssues(blockShowing) : new ArrayList<>();
    }

    /**
     * The function getIssues() returns the list of issues for a given block.
     *
     * @param block
     *            block whose issues are to be returned
     * @return the list of issues
     */
    private List<IssueController> getIssues(Block block) {
        List<IssueController> result = new ArrayList<>();
        if (block != null) {
            for (Issue issue : block.getIssues()) {
                result.add(new IssueController(issue, result.size()));
            }
        }
        return result;
    }

    /**
     * The function getLastAppearance() returns the date of last appearance of
     * the block currently showing as read-write property "lastAppearance".
     *
     * @return date of last appearance of currently showing block
     */
    public String getLastAppearance() {
        if (blockShowing != null && blockShowing.getLastAppearance() != null) {
            return DateUtils.DATE_FORMATTER.print(blockShowing.getLastAppearance());
        } else {
            return null;
        }
    }

    /**
     * The function getUploadedFile() is the getter method for the property
     * "uploadedFile" which is write-only, however Faces requires is.
     *
     * @return always null
     */
    public UploadedFile getUploadedFile() {
        return null;
    }

    /**
     * The function getUploadShowing() returns whether the dialog to upload a
     * course of appearance XML file shall be shown or not.
     *
     * @return whether the dialog to upload a course of appearance shows
     */
    public boolean getUploadShowing() {
        return uploadShowing;
    }

    /**
     * The function getYear() returns the year to be shown in the calendar sheet
     * as read-only property "year".
     *
     * @return the year to show on the calendar sheet
     */
    public String getYear() {
        return Integer.toString(yearShowing);
    }

    /**
     * The method hideUploadClick() will be called by Faces if the user clicks
     * the cancel button leave the dialog to upload a course of appearance XML
     * file.
     */
    public void hideUploadClick() {
        neglectEmptyBlock();
        uploadShowing = false;
    }

    /**
     * The method navigate() alters the year the calendar sheet is shown for so
     * that something of the current block is visible to prevent the user from
     * needing to click through centuries manually to get there.
     */
    protected void navigate() {
        try {
            if (yearShowing > blockShowing.getLastAppearance().getYear()) {
                yearShowing = blockShowing.getLastAppearance().getYear();
            }
            if (yearShowing < blockShowing.getFirstAppearance().getYear()) {
                yearShowing = blockShowing.getFirstAppearance().getYear();
            }
        } catch (NullPointerException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * The function neglectEmptyBlock() removes an empty block. Usually, an
     * empty block cannot be created. But if the user clicks the upload dialog
     * button, the form must be submitted, which causes the setters of the form
     * fields to create one prior to the function call. To stay consistent, it
     * is removed here again.
     */
    protected void neglectEmptyBlock() {
        if (blockShowing != null && blockShowing.isEmpty()) {
            course.remove(blockShowing);
            blockShowing = null;
        }
    }

    /**
     * The function nextClick() is executed if the user clicks the button to go
     * to the next screen. It returns either the String constant that indicates
     * Faces the next screen, or sets an error message if the user didn’t yet
     * input an issue and indicates Faces to stay on that screen by returning
     * the empty string.
     *
     * @return the screen to show next
     */
    public String nextClick() {
        if (course == null || course.countIndividualIssues() < 1) {
            Helper.setErrorMessage("errorDataIncomplete", "calendar.isEmpty");
            return null;
        }
        return "/pages/granularity";
    }

    /**
     * Tries to interpret a string entered by the user as a date as flexible as
     * possible. Supports two-digit years and imperial date field order
     * (month/day/year). In case of flexible interpretations, hints will be
     * displayed to put the user on the right track what happened to his input.
     *
     * <p>
     * If the user clicks the link to upload a course of appearance file, no
     * warning message shall show. Therefore an alternate white-space character
     * (U+00A0) will be appended to the value string by Javascript on the user
     * side because the setter methods will be called by Faces before the link
     * action will be executed, but we want to skip the error message generation
     * in that case, too.
     * </p>
     *
     * @param value
     *            value entered by the user
     * @param input
     *            input element, one of "firstAppearance" or "lastAppearance"
     * @return the date if found, or null otherwise
     */
    private LocalDate parseDate(String value, String input) {
        Matcher dateParser = FLEXIBLE_DATE.matcher(value);
        int[] numbers = new int[3];
        if (dateParser.matches()) {
            for (int i = 0; i < 3; i++) {
                numbers[i] = Integer.parseInt(dateParser.group(i + 1));
            }
            if (numbers[2] < 100) {
                new LocalDate();
                numbers[2] += 100 * today.getCenturyOfEra();
                if (numbers[2] > today.getYear()) {
                    numbers[2] -= 100;
                }
                Helper.setMessage(Helper.getTranslation(BLOCK + input + ".yearCompleted",
                    Arrays.asList(dateParser.group(3), Integer.toString(numbers[2]))));
            }
            try {
                return new LocalDate(numbers[2], numbers[1], numbers[0]);
            } catch (IllegalFieldValueException invalidDate) {
                try {
                    LocalDate swapped = new LocalDate(numbers[2], numbers[0], numbers[1]);
                    Helper.setMessage(BLOCK + input + ".swapped");
                    return swapped;
                } catch (IllegalFieldValueException stillInvalid) {
                    Helper.setErrorMessage(invalidDate.getLocalizedMessage(), logger, stillInvalid);
                }
            }
        }
        if (!uploadShowing && !value.contains("\u00A0")) {
            Helper.setErrorMessage(BLOCK + input + ".invalid");
        }
        return null;
    }

    /**
     * The method populateByCalendar() populates an empty calendar sheet by
     * iterating on LocalDate.
     *
     * @param sheet
     *            calendar sheet to populate
     */
    protected void populateByCalendar(List<List<Cell>> sheet) {
        Map<Integer, List<IssueController>> issueControllersCreatedOnce = new HashMap<>();
        Block currentBlock = null;
        ReadablePartial nextYear = new LocalDate(yearShowing + 1, DateTimeConstants.JANUARY, 1);
        for (LocalDate date = new LocalDate(yearShowing, DateTimeConstants.JANUARY, 1); date
                .isBefore(nextYear); date = date.plusDays(1)) {
            Cell cell = sheet.get(date.getDayOfMonth() - 1).get(date.getMonthOfYear() - 1);
            cell.setDate(date);
            if (currentBlock == null || !currentBlock.isMatch(date)) {
                currentBlock = course.isMatch(date);
            }
            if (currentBlock == null) {
                cell.setOnBlock(false);
            } else {
                Integer hashCode = currentBlock.hashCode();
                if (!issueControllersCreatedOnce.containsKey(hashCode)) {
                    issueControllersCreatedOnce.put(hashCode, getIssues(currentBlock));
                }
                cell.setIssues(buildIssueOptions(issueControllersCreatedOnce.get(hashCode), date));
            }
        }
    }

    /**
     * The method removeBlockClick() deletes the currently selected block from
     * the course of appearance. If there is only one block left, the editor
     * will instead be reset.
     *
     * @throws IndexOutOfBoundsException
     *             if the block referenced by “blockShowing” isn’t contained in
     *             the course of appearance
     */
    public void removeBlockClick() {
        if (course.size() < 2) {
            course.clear();
            blockChangerResolver.clear();
            blockShowing = null;
        } else {
            int index = course.indexOf(blockShowing);
            course.remove(index);
            if (index > 0) {
                index--;
            }
            blockShowing = course.get(index);
            navigate();
        }
    }

    /**
     * The method setBlockChangerSelected() will be called by Faces to store a
     * new value of the read-write property "blockChangerSelected". If it is
     * different from the current one, this means that the user selected a
     * different Block in the block changer drop down element. The event will be
     * used to alter the “blockShowing” field which keeps the block currently
     * showing. “updateAllowed” will be set accordingly to update the contents
     * of the current block or to prevent fields containing data from the
     * previously displaying block to overwrite the data inside the newly
     * selected one.
     *
     * @param value
     *            hashCode() in hex of the block to be selected
     */
    public void setBlockChangerSelected(String value) {
        if (value == null) {
            return;
        }
        blockChangerUnchanged = value.equals(Integer.toHexString(blockShowing.hashCode()));
        if (!blockChangerUnchanged) {
            blockShowing = blockChangerResolver.get(value);
            checkBlockPlausibility();
            navigate();
        }
    }

    /**
     * The method setFirstAppearance() will be called by Faces to store a new
     * value of the read-write property "firstAppearance", which represents the
     * date of first appearance of the block currently showing. The event will
     * be used to either alter the date of first appearance of the block defined
     * by the “blockShowing” field or, in case that a new block is under edit,
     * to initially set its the date of first appearance.
     *
     * @param firstAppearance
     *            new date of first appearance
     */
    public void setFirstAppearance(String firstAppearance) {
        LocalDate newFirstAppearance;
        try {
            newFirstAppearance = parseDate(firstAppearance, "firstAppearance");
        } catch (IllegalArgumentException e) {
            newFirstAppearance = blockShowing != null ? blockShowing.getFirstAppearance() : null;
        }
        try {
            if (blockShowing != null) {
                if (blockChangerUnchanged && (Objects.isNull(blockShowing.getFirstAppearance())
                        || !blockShowing.getFirstAppearance().isEqual(newFirstAppearance))) {
                    firstAppearanceIsToChange = newFirstAppearance;
                }
            } else {
                if (newFirstAppearance != null) {
                    blockShowing = new Block(course);
                    blockShowing.setFirstAppearance(newFirstAppearance);
                    course.add(blockShowing);
                }
            }
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(BLOCK + "firstAppearance.rejected", logger, e);
        }
    }

    /**
     * The method setLastAppearance() will be called by Faces to store a new
     * value of the read-write property "lastAppearance", which represents the
     * date of last appearance of the block currently showing. The event will be
     * used to either alter the date of last appearance of the block defined by
     * the “blockShowing” field or, in case that a new block is under edit, to
     * initially set its the date of last appearance.
     *
     * @param lastAppearance
     *            new date of last appearance
     */
    public void setLastAppearance(String lastAppearance) {
        LocalDate newLastAppearance;
        try {
            newLastAppearance = parseDate(lastAppearance, "lastAppearance");
        } catch (IllegalArgumentException e) {
            newLastAppearance = blockShowing != null ? blockShowing.getLastAppearance() : null;
        }
        try {
            if (blockShowing != null) {
                if (blockChangerUnchanged) {
                    if (firstAppearanceIsToChange == null) {
                        executeForFirstAppearanceToChangeNull(newLastAppearance);
                    } else {
                        executeForFirstAppearanceToChange(newLastAppearance);
                    }
                }
            } else {
                if (newLastAppearance != null) {
                    blockShowing = new Block(course);
                    blockShowing.setLastAppearance(newLastAppearance);
                    course.add(blockShowing);
                }
            }
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage(BLOCK + "lastAppearance.rejected", logger, e);
        } finally {
            firstAppearanceIsToChange = null;
        }
    }

    private void executeForFirstAppearanceToChangeNull(LocalDate newLastAppearance) {
        if (blockShowing.getLastAppearance() == null || !blockShowing.getLastAppearance().isEqual(newLastAppearance)) {
            if (blockShowing.getFirstAppearance() != null
                    && newLastAppearance.isBefore(blockShowing.getFirstAppearance())) {
                Helper.setErrorMessage(BLOCK_NEGATIVE);
                return;
            }
            blockShowing.setLastAppearance(newLastAppearance);
            checkBlockPlausibility();
            navigate();
        }
    }

    private void executeForFirstAppearanceToChange(LocalDate newLastAppearance) {
        if (blockShowing.getLastAppearance() == null || !blockShowing.getLastAppearance().isEqual(newLastAppearance)) {
            if (newLastAppearance.isBefore(firstAppearanceIsToChange)) {
                Helper.setErrorMessage(BLOCK_NEGATIVE);
                return;
            }
            blockShowing.setPublicationPeriod(firstAppearanceIsToChange, newLastAppearance);
        } else {
            if (blockShowing.getLastAppearance() != null
                    && blockShowing.getLastAppearance().isBefore(firstAppearanceIsToChange)) {
                Helper.setErrorMessage(BLOCK_NEGATIVE);
                return;
            }
            blockShowing.setFirstAppearance(firstAppearanceIsToChange);
        }
        checkBlockPlausibility();
        navigate();
    }

    /**
     * The method setUploadedFile() will be called by Faces to store the new
     * value of the read-write property "uploadedFile", which is a reference to
     * the binary data the user provides for upload.
     *
     * @param data
     *            the UploadedFile object generated by the Tomahawk library
     */
    public void setUploadedFile(UploadedFile data) {
        uploadedFile = data;
    }

    /**
     * The method showUploadClick() will be called by Faces if the user clicks
     * the button to upload a course of appearance XML file.
     */
    public void showUploadClick() {
        neglectEmptyBlock();
        uploadShowing = true;
    }

    /**
     * The method uploadClick() will be called by Faces if the user has selected
     * a course of appearance XML file for upload in the window and clicks the
     * button to upload it. Old values of the granularity picker are removed—if
     * any—so that the screen is reinitialised with the current calendar state
     * next time.
     */
    public void uploadClick() {
        try {
            if (uploadedFile == null) {
                Helper.setMessage(UPLOAD_ERROR, "calendar.upload.isEmpty");
                return;
            }
            Document xml = XMLUtils.load(uploadedFile.getInputStream());
            course = new Course(xml);
            blockShowing = course.get(0);
            Helper.removeManagedBean("GranularityForm");
            navigate();
        } catch (SAXException e) {
            Helper.setErrorMessage(UPLOAD_ERROR, "error.SAXException", logger, e);
            neglectEmptyBlock();
        } catch (IOException e) {
            Helper.setErrorMessage(UPLOAD_ERROR, e.getLocalizedMessage(), logger, e);
            neglectEmptyBlock();
        } catch (IllegalArgumentException e) {
            Helper.setErrorMessage("calendar.upload.overlappingDateRanges", logger, e);
            neglectEmptyBlock();
        } catch (NoSuchElementException e) {
            Helper.setErrorMessage(UPLOAD_ERROR, "calendar.upload.missingMandatoryElement", logger, e);
            neglectEmptyBlock();
        } catch (NullPointerException e) {
            Helper.setErrorMessage("calendar.upload.missingMandatoryValue", logger, e);
            neglectEmptyBlock();
        } finally {
            uploadedFile = null;
            uploadShowing = false;
        }
    }
}
