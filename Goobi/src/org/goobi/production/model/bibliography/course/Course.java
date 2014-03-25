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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.sub.goobi.helper.XMLUtils;

/**
 * The class Course represents the course of appearance of a newspaper.
 * 
 * <p>
 * A course of appearance consists of one or more Title elements. Interruptions
 * in the course of appearance can be modeled by subsequent Titles with the same
 * heading. In case that the newspaper changed its name, a new Title is
 * required, too.
 * </p>
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class Course extends ArrayList<Title> {
	private static final long serialVersionUID = 1L;

	/**
	 * Attribute <code>date="…"</code> used in the XML representation of a
	 * course of appearance.
	 */
	static final String ATTRIBUTE_DATE = "date";
	/**
	 * Attribute <code>heading="…"</code> used in the XML representation of a
	 * course of appearance.
	 */
	private static final String ATTRIBUTE_TITLE_HEADING = "heading";

	/**
	 * Attribute <code>index="…"</code> used in the XML representation of a
	 * course of appearance.
	 * 
	 * <p>
	 * The attribute <code>index="…"</code> is optional. It may be used to
	 * distinguish different title block with the same title (value from
	 * <code>heading="…"</code> attribute). If it is present, it is respected in
	 * sorting the title blocks when reading the XML file, second to the date of
	 * their first appearance. That is, if two title blocks have the same date
	 * of their first appearance, they are sorted by their index. Indexes are
	 * sorted numerically if they can be parsed as integers, otherwise they are
	 * sorted as String. If two title blocks with the same index but different
	 * title are encountered, they will result in two different title blocks.
	 * </p>
	 */
	private static final String ATTRIBUTE_INDEX = "index";

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
	static final String ATTRIBUTE_ISSUE_HEADING = "issue";

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
	 * course of appearance.
	 * 
	 * <p>
	 * Each <code>&lt;title&gt;</code> element represents the title block the
	 * appeared issues belong to. It has the attributes <code>heading="…"</code>
	 * (required, must not be empty) and <code>index="…"</code> (optional) and
	 * can hold <code>&lt;appeared&gt;</code> elements (of any quantity).
	 * </p>
	 */
	private static final String ELEMENT_TITLE = "title";

	/**
	 * List of Lists of Issues, each representing a process.
	 */
	private final List<List<IndividualIssue>> processes = new ArrayList<List<IndividualIssue>>();

	/**
	 * Default constructor, creates an empty course. Must be made explicit since
	 * we offer other constructors, too.
	 */
	public Course() {
		super();
	}

	/**
	 * The class AppearedIssue is a bean class which is used in the constructor
	 * Course(Document).
	 * 
	 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
	 */
	public class AppearedIssue {
		private final LocalDate date;
		private final String index;
		private final String issueHeading;
		private final String titleHeading;

		private AppearedIssue(String titleHeading, String index, String issueHeading, String date) {
			this.titleHeading = titleHeading;
			this.index = index;
			this.issueHeading = issueHeading;
			this.date = LocalDate.parse(date);
		}

		private LocalDate getDate() {
			return date;
		}

		private String getIndexNotNull() {
			return index != null ? index : "";
		}

		private String getIssueHeading() {
			return issueHeading;
		}

		private String getTitleHeading() {
			return titleHeading;
		}

		private String getTitleIdentifier() {
			return index != null ? titleHeading + '\u001E' + index : titleHeading;
		}
	}

	/**
	 * Constructor to creates a Course from an XML document.
	 * 
	 * @param data
	 *            XML document to parse
	 * @throws NoSuchFieldException
	 *             if a field expected in the XML document wasn’t found
	 */
	public Course(Document data) throws NoSuchElementException {
		super();
		Map<String, Title> unordered = new HashMap<String, Title>();
		Map<Title, String> indexes = new HashMap<Title, String>();

		for (AppearedIssue appearedIssue : getAppearedIssues(data)) {
			Title newTitleAdded = addAppearedIssue(appearedIssue, unordered);
			if (newTitleAdded != null)
				indexes.put(newTitleAdded, appearedIssue.getIndexNotNull());
		}
		TreeMap<String, Title> ordered = order(unordered.values(), indexes);
		for (Map.Entry<String, Title> entry : ordered.entrySet()) {
			super.add(entry.getValue());
		}
	}

	/**
	 * The function getAppearedIssues() iterates over the XML document and
	 * returns a list of individual issues.
	 * 
	 * @param data
	 *            XML document loaded into memory
	 * @return a list appeared issues
	 */
	private List<AppearedIssue> getAppearedIssues(Document data) {
		List<AppearedIssue> result = new ArrayList<AppearedIssue>();
		for (Node processNode = XMLUtils.getChildNodeByNodeName(data, ELEMENT_PROCESSES).getFirstChild(); processNode != null; processNode = processNode
				.getNextSibling()) {
			for (Node titleNode = processNode.getFirstChild(); titleNode != null; titleNode = titleNode
					.getNextSibling()) {
				String titleHeading = ((Element) titleNode).getAttribute(ATTRIBUTE_TITLE_HEADING);
				String index = ((Element) titleNode).getAttribute(ATTRIBUTE_INDEX);
				for (Node issueNode = titleNode.getFirstChild(); issueNode != null; issueNode = issueNode
						.getNextSibling()) {
					String issueHeading = ((Element) titleNode).getAttribute(ATTRIBUTE_ISSUE_HEADING);
					if (issueHeading == null)
						issueHeading = "";
					String date = ((Element) titleNode).getAttribute(ATTRIBUTE_DATE);
					result.add(new AppearedIssue(titleHeading, index, issueHeading, date));
				}
			}
		}
		return result;
	}

	/**
	 * The function addAppearedIssue() adds an appeared issue to the relevant
	 * data structure.
	 * 
	 * @param appeared
	 *            an issue to add
	 * @param titles
	 *            a map holding the title blocks to create
	 * @return null, or the new object if it was necessary to insert one into
	 *         titles
	 */
	private Title addAppearedIssue(AppearedIssue appeared, Map<String, Title> titles) {
		Title newTitleAdded = null;
		String identifier = appeared.getTitleIdentifier();
		if (!titles.containsKey(identifier)) {
			newTitleAdded = new Title(appeared.getTitleHeading());
			titles.put(identifier, newTitleAdded);
		}
		Title title = titles.get(identifier);
		if (title.getFirstAppearance() == null || title.getFirstAppearance().isAfter(appeared.getDate()))
			title.setFirstAppearance(appeared.getDate());
		if (title.getLastAppearance() == null || title.getLastAppearance().isBefore(appeared.getDate()))
			title.setLastAppearance(appeared.getDate());
		Issue issue = title.getIssue(appeared.getIssueHeading());
		if (issue == null) {
			issue = new Issue();
			issue.setHeading(appeared.getIssueHeading());
			title.addIssue(issue);
		}
		issue.addAddition(appeared.getDate());
		return newTitleAdded;
	}

	/**
	 * The function order() returns a TreeMap with the elements from “titles” as
	 * values. As keys, a sort string is created. The elements are sorted by the
	 * date of first appearance, then index (numeric, if applicable, String
	 * otherwise), then title heading.
	 * 
	 * @param titles
	 *            Titles to sort
	 * @param indexes
	 *            Indexes to use for sorting
	 * @return a TreeMap with the titles, sorted by date of first appearance,
	 *         index and title heading
	 */
	private TreeMap<String, Title> order(Collection<Title> titles, Map<Title, String> indexes) {
		TreeMap<String, Title> result = new TreeMap<String, Title>();
		int longestIndex = 0;
		for (String index : indexes.values())
			if (index != null)
				longestIndex = Math.max(longestIndex, index.length());
		String sortNumeric = "%04d%02d%02d%" + (longestIndex > 0 ? "0".concat(Integer.toString(longestIndex)) : "")
				+ "d%s";
		String sortOther = "%04d%02d%02d%" + (longestIndex > 0 ? "-".concat(Integer.toString(longestIndex)) : "")
				+ "s%s";
		for (Title title : titles) {
			LocalDate firstAppearance = title.getFirstAppearance();
			try {
				result.put(String.format(sortNumeric, firstAppearance.getYear(), firstAppearance.getMonthOfYear(),
						firstAppearance.getDayOfMonth(), Integer.parseInt(indexes.get(title)), title.getHeading()),
						title);
			} catch (NumberFormatException e) {
				result.put(String.format(sortOther, firstAppearance.getYear(), firstAppearance.getMonthOfYear(),
						firstAppearance.getDayOfMonth(), indexes.get(title), title.getHeading()), title);
			}
		}
		return result;
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
		for (Title title : this) {
			LocalDate lastAppearance = title.getLastAppearance();
			for (LocalDate day = title.getFirstAppearance(); !day.isAfter(lastAppearance); day = day.plusDays(1)) {
				for (Issue issue : title.getIssues()) {
					if (issue.isMatch(day))
						result += 1;
				}
			}
		}
		return result;
	}

	/**
	 * The method getIndividualIssues generates a list of IndividualIssue
	 * objects, each of them representing a stamping of an (one physically
	 * appeared) issue.
	 * 
	 * @return a SortedSet of IndividualIssue objects, each of them representing
	 *         one physically appeared issue
	 */
	public Set<IndividualIssue> getIndividualIssues() {
		LinkedHashSet<IndividualIssue> result = new LinkedHashSet<IndividualIssue>();
		for (Title title : this) {
			LocalDate lastAppearance = title.getLastAppearance();
			for (LocalDate day = title.getFirstAppearance(); !day.isAfter(lastAppearance); day = day.plusDays(1)) {
				for (Issue issue : title.getIssues()) {
					if (issue.isMatch(day)) {
						result.add(new IndividualIssue(title, issue, day));
					}
				}
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
		if (super.isEmpty())
			return null;
		else {
			LocalDate result = super.get(0).getLastAppearance();
			for (int index = 1; index < super.size(); index++) {
				LocalDate lastAppearance = super.get(index).getLastAppearance();
				if (lastAppearance.isAfter(result))
					result = lastAppearance;
			}
			return result;
		}
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
		for (Title title : this) {
			LocalDate lastAppearance = title.getLastAppearance();
			for (LocalDate day = title.getFirstAppearance(); !day.isAfter(lastAppearance); day = day.plusDays(1)) {
				for (Issue issue : title.getIssues()) {
					if (issue.isMatch(day))
						result += day.getDayOfWeek() != DateTimeConstants.SUNDAY ? WEEKDAY_PAGES : SUNDAY_PAGES;
				}
			}
		}
		return result;
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
			if (process == null)
				process = new ArrayList<IndividualIssue>(initialCapacity);
			process.add(issue);
			lastMark = mark;
		}
		if (process != null)
			processes.add(process);
	}

	/**
	 * The function toXML() transforms a course of appearance to XML.
	 * 
	 * @param lang
	 *            language to use for the “description”
	 * @return XML as String
	 */
	public Document toXML(Locale lang) {
		Document result = XMLUtils.newDocument();
		Element course = result.createElement(ELEMENT_COURSE);

		Element description = result.createElement(ELEMENT_DESCRIPTION);
		description.appendChild(result.createTextNode(StringUtils.join(verbalise(lang), "\n\n")));
		course.appendChild(description);

		Element processesNode = result.createElement(ELEMENT_PROCESSES);
		for (List<IndividualIssue> process : processes) {
			Element processNode = result.createElement(ELEMENT_PROCESS);
			Element title = null;
			int previous = -1;
			for (IndividualIssue issue : process) {
				int index = issue.indexIn(this);
				if (index != previous && title != null) {
					processNode.appendChild(title);
					title = null;
				}
				if (title == null) {
					title = result.createElement(ELEMENT_TITLE);
					title.setAttribute(ATTRIBUTE_TITLE_HEADING, get(index).getHeading());
					title.setAttribute(ATTRIBUTE_INDEX, Integer.toString(index + 1));
				}
				title.appendChild(issue.populate(result.createElement(ELEMENT_APPEARED)));
				previous = index;
			}
			if (title != null)
				processNode.appendChild(title);
			processesNode.appendChild(processNode);
		}
		course.appendChild(processesNode);

		result.appendChild(course);
		return result;
	}

	/**
	 * The function verbalise() returns a verbal description of the object in
	 * the given language. If the lang parameter is null or the given language
	 * is not available, the default is used.
	 * 
	 * @param lang
	 *            language to verbalise in
	 * @return verbal description as text
	 */
	protected List<String> verbalise(Locale lang) {
		if (Locale.GERMAN.equals(lang))
			return CourseToGerman.asReadableText(this);
		// add more languages here
		// …

		// default: // TODO: change to English as soon as available
		return CourseToGerman.asReadableText(this);
	}
}
