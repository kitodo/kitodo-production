package org.goobi.production.model.bibliography.course;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.sub.goobi.helper.XMLUtils;

public class CourseXML extends Course {

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
	public static Course parseCourse(Document data) throws NoSuchFieldException {
		Course result = new Course();
		Map<String, Title> unordered = new HashMap<String, Title>();
		Map<Title, String> indexes = new HashMap<Title, String>();

		for (AppearedIssue appearedIssue : getAppearedIssues(data)) {
			Title newTitleAdded = addAppearedIssue(appearedIssue, unordered);
			if (newTitleAdded != null)
				indexes.put(newTitleAdded, appearedIssue.getIndexNotNull());
		}
		TreeMap<String, Title> ordered = order(unordered.values(), indexes);
		for (Map.Entry<String, Title> entry : ordered.entrySet()) {
			result.add(entry.getValue());
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
	private static Title addAppearedIssue(AppearedIssue appeared, Map<String, Title> titles) {
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
	 * The function getAppearedIssues() iterates over the XML document and
	 * returns a list of individual issues.
	 * 
	 * @param data
	 *            XML document loaded into memory
	 * @return a list appeared issues
	 * @throws NoSuchFieldException
	 *             if no node named as defined by ELEMENT_PROCESSES could be
	 *             found
	 */
	private static List<AppearedIssue> getAppearedIssues(Document data) throws NoSuchFieldException {
		List<AppearedIssue> result = new ArrayList<AppearedIssue>();
		Node processesNode = XMLUtils.getChildNodeByNodeName(XMLUtils.getChildNodeByNodeName(data, ELEMENT_COURSE),
				ELEMENT_PROCESSES);
		for (Node processNode = processesNode.getFirstChild(); processNode != null; processNode = processNode
				.getNextSibling()) {
			for (Node titleNode = processNode.getFirstChild(); titleNode != null; titleNode = titleNode
					.getNextSibling()) {
				if (!ELEMENT_TITLE.equals(titleNode.getNodeName()))
					continue;
				String titleHeading = ((Element) titleNode).getAttribute(ATTRIBUTE_TITLE_HEADING);
				String index = ((Element) titleNode).getAttribute(ATTRIBUTE_INDEX);
				for (Node issueNode = titleNode.getFirstChild(); issueNode != null; issueNode = issueNode
						.getNextSibling()) {
					String issueHeading = ((Element) titleNode).getAttribute(ATTRIBUTE_ISSUE_HEADING);
					if (issueHeading == null)
						issueHeading = "";
					String date = ((Element) titleNode).getAttribute(ATTRIBUTE_DATE);
					result.add(new CourseXML().new AppearedIssue(titleHeading, index, issueHeading, date));
				}
			}
		}
		return result;
	}

	/**
	 * The function getSortFormat() returns the sort format String that sorting
	 * is based on.
	 * 
	 * @param numeric
	 *            whether the index is numeric (true) or String (false)
	 * @param longestIndex
	 *            the longest expected index
	 * @return a sort format string
	 */
	private static String getSortFormat(boolean numeric, int longestIndex) {
		StringBuilder result = new StringBuilder(20);
		result.append("%04d%02d%02d%");
		if (longestIndex > 0) {
			result.append(numeric ? '0' : '-');
			result.append(Integer.toString(longestIndex));
		}
		result.append(numeric ? 'd' : 's');
		result.append("%s");
		return result.toString();
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
	private static TreeMap<String, Title> order(Collection<Title> titles, Map<Title, String> indexes) {
		TreeMap<String, Title> result = new TreeMap<String, Title>();
		int longestIndex = 0;
		for (String index : indexes.values())
			if (index != null)
				longestIndex = Math.max(longestIndex, index.length());
		String sortNumeric = getSortFormat(true, longestIndex);
		String sortOther = getSortFormat(false, longestIndex);
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
	 * The function toXML() transforms a course of appearance to XML.
	 * 
	 * @param lang
	 *            language to use for the “description”
	 * @return XML as String
	 */
	public static Document toXML(Course course) {
		Document result = XMLUtils.newDocument();
		Element courseNode = result.createElement(ELEMENT_COURSE);

		Element description = result.createElement(ELEMENT_DESCRIPTION);
		description.appendChild(result.createTextNode(StringUtils.join(CourseToGerman.asReadableText(course), "\n\n")));
		courseNode.appendChild(description);

		Element processesNode = result.createElement(ELEMENT_PROCESSES);
		for (List<IndividualIssue> process : course.processes) {
			Element processNode = result.createElement(ELEMENT_PROCESS);
			Element titleNode = null;
			int previous = -1;
			for (IndividualIssue issue : process) {
				int index = issue.indexIn(course);
				if (index != previous && titleNode != null) {
					processNode.appendChild(titleNode);
					titleNode = null;
				}
				if (titleNode == null) {
					titleNode = result.createElement(ELEMENT_TITLE);
					titleNode.setAttribute(ATTRIBUTE_TITLE_HEADING, course.get(index).getHeading());
					titleNode.setAttribute(ATTRIBUTE_INDEX, Integer.toString(index + 1));
				}
				Element issueNode = result.createElement(ELEMENT_APPEARED);
				if (issue != null)
					issueNode.setAttribute(CourseXML.ATTRIBUTE_ISSUE_HEADING, issue.getHeading());
				issueNode.setAttribute(CourseXML.ATTRIBUTE_DATE, issue.getDate().toString());
				titleNode.appendChild(issueNode);
				previous = index;
			}
			if (titleNode != null)
				processNode.appendChild(titleNode);
			processesNode.appendChild(processNode);
		}
		courseNode.appendChild(processesNode);

		result.appendChild(courseNode);
		return result;
	}
}
