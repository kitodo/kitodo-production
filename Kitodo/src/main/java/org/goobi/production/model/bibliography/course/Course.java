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

import de.sub.goobi.helper.XMLUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The class Course represents the course of appearance of a newspaper.
 *
 * <p>
 * A course of appearance consists of one or more blocks of time. Interruptions
 * in the course of appearance can be modeled by subsequent blocks.
 * </p>
 *
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Course extends ArrayList<Block> {
    private static final long serialVersionUID = 1L;

    /**
     * Attribute <code>date="…"</code> used in the XML representation of a
     * course of appearance.
     */
    private static final String ATTRIBUTE_DATE = "date";

    /**
     * Attribute <code>index="…"</code> used in the XML representation of a
     * course of appearance.
     *
     * <p>
     * The attribute <code>index="…"</code> is optional. It may be used to
     * distinguish different blocks if needed and can be omitted if only one
     * block is used.
     * </p>
     */
    private static final String ATTRIBUTE_VARIANT = "index";

    /**
     * Attribute <code>issue="…"</code> used in the XML representation of a
     * course of appearance.
     *
     * <p>
     * The attribute <code>issue="…"</code> holds the name of the issue.
     * Newspapers, especially bigger ones, can have several issues that, e.g.,
     * may differ in time of publication (morning issue, evening issue, …) or
     * geographic distribution (Edinburgh issue, London issue, …).
     * </p>
     */
    private static final String ATTRIBUTE_ISSUE_HEADING = "issue";

    /**
     * Element <code>&lt;appeared&gt;</code> used in the XML representation of a
     * course of appearance.
     *
     * <p>
     * Each <code>&lt;appeared&gt;</code> element represents one issue that
     * physically appeared. It has the attributes <code>issue="…"</code>
     * (required, may be empty) and <code>date="…"</code> (required) and cannot
     * hold child elements.
     * </p>
     */
    private static final String ELEMENT_APPEARED = "appeared";

    /**
     * Element <code>&lt;course&gt;</code> used in the XML representation of a
     * course of appearance.
     *
     * <p>
     * <code>&lt;course&gt;</code> is the root element of the XML
     * representation. It can hold two children,
     * <code>&lt;description&gt;</code> (output only, optional) and
     * <code>&lt;processes&gt;</code> (required).
     * </p>
     */
    private static final String ELEMENT_COURSE = "course";

    /**
     * Element <code>&lt;description&gt;</code> used in the XML representation
     * of a course of appearance.
     *
     * <p>
     * <code>&lt;description&gt;</code> holds a verbal, human-readable
     * description of the course of appearance, which is generated only and
     * doesn’t have an effect on input.
     * </p>
     */
    private static final String ELEMENT_DESCRIPTION = "description";

    /**
     * Element <code>&lt;process&gt;</code> used in the XML representation of a
     * course of appearance.
     *
     * <p>
     * Each <code>&lt;process&gt;</code> element represents one process to be
     * generated in Goobi Production. It can hold <code>&lt;title&gt;</code>
     * elements (of any quantity).
     * </p>
     */
    private static final String ELEMENT_PROCESS = "process";

    /**
     * Element <code>&lt;processes&gt;</code> used in the XML representation of
     * a course of appearance.
     *
     * <p>
     * Each <code>&lt;processes&gt;</code> element represents the processes to
     * be generated in Goobi Production. It can hold
     * <code>&lt;process&gt;</code> elements (of any quantity).
     * </p>
     */
    private static final String ELEMENT_PROCESSES = "processes";

    /**
     * Element <code>&lt;title&gt;</code> used in the XML representation of a
     * course of appearance. Each <code>&lt;title&gt;</code> element represents
     * a block in time the appeared issues belong to. It has the optional
     * attribute <code>index="…"</code> and can hold
     * <code>&lt;appeared&gt;</code> elements (of any quantity).
     *
     * <p>
     * Note: In the original design, the element was intended to model title
     * name changes. This was given up later, but for historical reasons, the
     * XML element’s name is still “title”. For the original design, see
     * https://github.com/kitodo/kitodo-production/issues/51#issuecomment-38035674
     * </p>
     */
    private static final String ELEMENT_BLOCK = "title";

    /**
     * List of Lists of Issues, each representing a process.
     */
    private final List<List<IndividualIssue>> processes = new ArrayList<>();
    private final Map<String, Block> resolveByBlockVariantCache = new HashMap<>();

    private boolean processesAreVolatile = true;

    /**
     * Default constructor, creates an empty course. Must be made explicit since
     * we offer other constructors, too.
     */
    public Course() {
        super();
    }

    /**
     * Constructor to create a course from an xml source.
     *
     * @param xml
     *            XML document data structure
     * @throws NoSuchElementException
     *             if ELEMENT_COURSE or ELEMENT_PROCESSES cannot be found
     * @throws IllegalArgumentException
     *             if the dates of two blocks do overlap
     * @throws NullPointerException
     *             if a mandatory element is absent
     */
    public Course(Document xml) {
        super();
        processesAreVolatile = false;
        Element rootNode = XMLUtils.getFirstChildWithTagName(xml, ELEMENT_COURSE);
        Element processesNode = XMLUtils.getFirstChildWithTagName(rootNode, ELEMENT_PROCESSES);
        int initialCapacity = 10;
        for (Node processNode = processesNode.getFirstChild(); processNode != null; processNode = processNode
                .getNextSibling()) {
            if (!(processNode instanceof Element) || !processNode.getNodeName().equals(ELEMENT_PROCESS)) {
                continue;
            }
            List<IndividualIssue> process = new ArrayList<>(initialCapacity);
            for (Node blockNode = processNode.getFirstChild(); blockNode != null; blockNode = blockNode
                    .getNextSibling()) {
                if (!(blockNode instanceof Element) || !blockNode.getNodeName().equals(ELEMENT_BLOCK)) {
                    continue;
                }
                String variant = ((Element) blockNode).getAttribute(ATTRIBUTE_VARIANT);
                for (Node issueNode = blockNode.getFirstChild(); issueNode != null; issueNode = issueNode
                        .getNextSibling()) {
                    if (!(issueNode instanceof Element) || !issueNode.getNodeName().equals(ELEMENT_APPEARED)) {
                        continue;
                    }
                    String issue = ((Element) issueNode).getAttribute(ATTRIBUTE_ISSUE_HEADING);
                    String date = ((Element) issueNode).getAttribute(ATTRIBUTE_DATE);
                    if (date == null) {
                        throw new NullPointerException(ATTRIBUTE_DATE);
                    }
                    IndividualIssue individualIssue = addAddition(variant, issue, LocalDate.parse(date));
                    process.add(individualIssue);
                }
            }
            processes.add(process);
            initialCapacity = (int) Math.round(1.1 * process.size());
        }
        recalculateRegularityOfIssues();
        processesAreVolatile = true;
    }

    /**
     * Appends the specified block to the end of this course.
     *
     * @param block
     *            block to be appended to this course
     * @return true (as specified by Collection.add(E))
     * @see java.util.ArrayList#add(java.lang.Object)
     */
    @Override
    public boolean add(Block block) {
        super.add(block);
        if (block.countIndividualIssues() > 0) {
            processes.clear();
        }
        return true;
    }

    /**
     * Adds a LocalDate to the set of additions of the issue identified by
     * issueHeading in the block optionally identified by a variant. Note that
     * in case that the date is outside the time range of the described block,
     * the time range will be expanded. Do not use this function in contexts
     * where there is one or more issues in the block that have a regular
     * appearance set, because in this case the regularly appeared issues in the
     * expanded block will show up later, too, which is probably not what you
     * want.
     *
     * @param variant
     *            block identifier (may be null)
     * @param issueHeading
     *            heading of the issue this issue is of
     * @param date
     *            date to add
     * @return an IndividualIssue representing the added issue
     * @throws IllegalArgumentException
     *             if the date would cause the block to overlap with another
     *             block
     */
    private IndividualIssue addAddition(String variant, String issueHeading, LocalDate date) {
        Block block = get(variant);
        if (block == null) {
            block = new Block(this, variant);
            try {
                block.setFirstAppearance(date);
                block.setLastAppearance(date);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(e.getMessage() + ", (" + variant + ") " + date, e);
            }
            add(block);
        } else {
            if (block.getFirstAppearance().isAfter(date)) {
                block.setFirstAppearance(date);
            }
            if (block.getLastAppearance().isBefore(date)) {
                block.setLastAppearance(date);
            }
        }
        Issue issue = block.getIssue(issueHeading);
        if (issue == null) {
            issue = new Issue(this, issueHeading);
            block.addIssue(issue);
        }
        issue.addAddition(date);
        return new IndividualIssue(block, issue, date);
    }

    /**
     * The procedure clearProcesses() deletes the process list. This is
     * necessary if the processes must be regenerated because the data structure
     * they will be derived from has changed, or if they only had been added
     * temporarily to be able to retrieve an XML file containing values.
     */
    public void clearProcesses() {
        if (processesAreVolatile) {
            processes.clear();
        }
    }

    /**
     * The method countIndividualIssues() determines how many stampings of
     * issues physically appeared without generating a list of IndividualIssue
     * objects.
     *
     * @return the count of issues
     */
    public long countIndividualIssues() {
        long result = 0;
        for (Block block : this) {
            result += block.countIndividualIssues();
        }
        return result;
    }

    /**
     * Returns the block identified by the optionally given variant, or null if
     * no block with the given variant can be found.
     *
     * @param variant
     *            the variant of the block (may be null)
     * @return the block identified by the given variant, or null if no block
     *         can be found
     */
    private Block get(String variant) {
        if (resolveByBlockVariantCache.containsKey(variant)) {
            Block potentialResult = resolveByBlockVariantCache.get(variant);
            if (potentialResult.isIdentifiedBy(variant)) {
                return potentialResult;
            } else {
                resolveByBlockVariantCache.remove(variant);
            }
        }
        for (Block candidate : this) {
            if (candidate.isIdentifiedBy(variant)) {
                resolveByBlockVariantCache.put(variant, candidate);
                return candidate;
            }
        }
        return null;
    }

    /**
     * The function getIndividualIssues() generates a list of IndividualIssue
     * objects, each of them representing a stamping of one physically appeared
     * issue.
     *
     * @return a LinkedHashSet of IndividualIssue objects, each of them
     *         representing one physically appeared issue
     */
    public LinkedHashSet<IndividualIssue> getIndividualIssues() {
        LinkedHashSet<IndividualIssue> result = new LinkedHashSet<>();
        LocalDate lastAppearance = getLastAppearance();
        LocalDate firstAppearance = getFirstAppearance();
        if (Objects.nonNull(firstAppearance)) {
            for (LocalDate day = firstAppearance; !day.isAfter(lastAppearance); day = day.plusDays(1)) {
                for (Block block : this) {
                    result.addAll(block.getIndividualIssues(day));
                }
            }
        }
        return result;
    }

    /**
     * The function getFirstAppearance() returns the date the regularity of this
     * course of appearance starts with.
     *
     * @return the date of first appearance
     */
    public LocalDate getFirstAppearance() {
        if (super.isEmpty()) {
            return null;
        }
        LocalDate result = super.get(0).getFirstAppearance();
        for (int index = 1; index < super.size(); index++) {
            LocalDate firstAppearance = super.get(index).getFirstAppearance();
            if (firstAppearance.isBefore(result)) {
                result = firstAppearance;
            }
        }
        return result;
    }

    /**
     * The function getLastAppearance() returns the date the regularity of this
     * course of appearance ends with.
     *
     * @return the date of last appearance
     */
    public LocalDate getLastAppearance() {
        if (super.isEmpty()) {
            return null;
        }
        LocalDate result = super.get(0).getLastAppearance();
        for (int index = 1; index < super.size(); index++) {
            LocalDate lastAppearance = super.get(index).getLastAppearance();
            if (lastAppearance.isAfter(result)) {
                result = lastAppearance;
            }
        }
        return result;
    }

    /**
     * The function getNumberOfProcesses() returns the number of processes into
     * which the course of appearance will be split.
     *
     * @return the number of processes
     */
    public int getNumberOfProcesses() {
        return processes.size();
    }

    /**
     * The function guessTotalNumberOfPages() calculates a guessed number of
     * pages for a course of appearance of a newspaper, presuming each issue
     * having 40 pages and Sunday issues having six times that size because most
     * people buy the Sunday issue most often and therefore advertisers buy the
     * most space on that day.
     *
     * @return a guessed total number of pages for the full course of appearance
     */
    public long guessTotalNumberOfPages() {
        final int WEEKDAY_PAGES = 40;
        final int SUNDAY_PAGES = 240;

        long result = 0;
        for (Block block : this) {
            LocalDate lastAppearance = block.getLastAppearance();
            for (LocalDate day = block.getFirstAppearance(); !day.isAfter(lastAppearance); day = day.plusDays(1)) {
                for (Issue issue : block.getIssues()) {
                    if (issue.isMatch(day)) {
                        result += day.getDayOfWeek() != DateTimeConstants.SUNDAY ? WEEKDAY_PAGES : SUNDAY_PAGES;
                    }
                }
            }
        }
        return result;
    }

    /**
     * The function getProcesses() returns the processes to create from the
     * course of appearance.
     *
     * @return the processes
     */
    public List<List<IndividualIssue>> getProcesses() {
        return processes;
    }

    /**
     * The function isMatch() iterates over the array of blocks and returns the
     * first one that matches a given date. Since there shouldn’t be overlapping
     * blocks, there should be at most one block for which this is true. If no
     * matching block is found, it will return null.
     *
     * @param date
     *            a LocalDate to examine
     * @return the block on which this date is represented, if any
     */
    public Block isMatch(LocalDate date) {
        for (Block block : this) {
            if (block.isMatch(date)) {
                return block;
            }
        }
        return null;
    }

    /**
     * The method recalculateRegularityOfIssues() recalculates for all blocks of
     * this Course for each Issue the daysOfWeek of its regular appearance
     * within the interval of time of the block. This is especially sensible to
     * detect the underlying regularity after lots of issues whose existence is
     * known have been added one by one as additions to the underlying issue(s).
     */
    public void recalculateRegularityOfIssues() {
        for (Block block : this) {
            block.recalculateRegularityOfIssues();
        }
    }

    /**
     * The function remove() removes the element at the specified position in
     * this list. Shifts any subsequent elements to the left (subtracts one from
     * their indices). Additionally, any references to the object held in the
     * map used for resolving are being removed so that the object can be
     * garbage-collected.
     *
     * @param index
     *            the index of the element to be removed
     * @return the element that was removed from the list
     * @throws IndexOutOfBoundsException
     *             if the index is out of range (index < 0 || index >= size())
     * @see java.util.ArrayList#remove(int)
     */
    @Override
    public Block remove(int index) {
        Block block = super.remove(index);
        resolveByBlockVariantCache.entrySet().removeIf(entry -> entry.getValue() == block);
        if (block.countIndividualIssues() > 0) {
            processes.clear();
        }
        return block;
    }

    /**
     * The method splitInto() calculates the processes depending on the given
     * BreakMode.
     *
     * @param mode
     *            how the course shall be broken into issues
     */

    public void splitInto(Granularity mode) {
        int initialCapacity = 10;
        Integer lastMark = null;
        List<IndividualIssue> process = null;

        processes.clear();
        for (IndividualIssue issue : getIndividualIssues()) {
            Integer mark = issue.getBreakMark(mode);
            if (!mark.equals(lastMark) && process != null) {
                initialCapacity = (int) Math.round(1.1 * process.size());
                processes.add(process);
                process = null;
            }
            if (process == null) {
                process = new ArrayList<>(initialCapacity);
            }
            process.add(issue);
            lastMark = mark;
        }
        if (process != null) {
            processes.add(process);
        }
    }

    /**
     * The function toXML() transforms a course of appearance to XML.
     *
     * @return XML as String
     */
    public Document toXML() {
        Document result = XMLUtils.newDocument();
        Element courseNode = result.createElement(ELEMENT_COURSE);

        Element description = result.createElement(ELEMENT_DESCRIPTION);
        description.appendChild(result.createTextNode(StringUtils.join(CourseToGerman.asReadableText(this), "\n\n")));
        courseNode.appendChild(description);

        Element processesNode = result.createElement(ELEMENT_PROCESSES);
        for (List<IndividualIssue> process : processes) {
            Element processNode = result.createElement(ELEMENT_PROCESS);
            Element blockNode = null;
            int previous = -1;
            for (IndividualIssue issue : process) {
                int index = issue.indexIn(this);
                if (index != previous && blockNode != null) {
                    processNode.appendChild(blockNode);
                    blockNode = null;
                }
                if (blockNode == null) {
                    blockNode = result.createElement(ELEMENT_BLOCK);
                    blockNode.setAttribute(ATTRIBUTE_VARIANT, Integer.toString(index + 1));
                }
                Element issueNode = result.createElement(ELEMENT_APPEARED);
                issueNode.setAttribute(ATTRIBUTE_ISSUE_HEADING, issue.getHeading());
                issueNode.setAttribute(ATTRIBUTE_DATE, issue.getDate().toString());
                blockNode.appendChild(issueNode);
                previous = index;
            }
            if (blockNode != null) {
                processNode.appendChild(blockNode);
            }
            processesNode.appendChild(processNode);
        }
        courseNode.appendChild(processesNode);

        result.appendChild(courseNode);
        return result;
    }
}
