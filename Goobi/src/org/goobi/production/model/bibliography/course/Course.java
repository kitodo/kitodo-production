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
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.sub.goobi.helper.XMLFuncs;

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
	 * List of IDs of stamping of issues which each do represent the first issue
	 * in a process.
	 */
	protected List<String> breakIDs = new ArrayList<String>();

	/**
	 * Empty constructor. Creates a Course which doesn’t hold any Title yet with
	 * an initial capacity of ten.
	 */
	public Course() {
	}

	/**
	 * Creates a Course holding the elements of the specified collection, in the
	 * order they are returned by the collection's iterator.
	 * 
	 * @param collection
	 *            the collection whose elements are to be placed into this list
	 * @throws NullPointerException
	 *             if the specified collection is null
	 */
	public Course(Collection<? extends Title> collection) {
		super(collection);
	}

	/**
	 * Creates a Course which doesn’t hold any Title yet with the specified
	 * initial capacity of the underlying ArrayList.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the list
	 * @throws IllegalArgumentException
	 *             if the specified initial capacity is negative
	 */
	public Course(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * The method setBreaks() calculates and sets the break IDs depending on the
	 * given BreakMode.
	 * 
	 * @param mode
	 *            how the course shall be broken into issues
	 */
	public void setBreaks(BreakMode mode) {
		breakIDs.clear();
		Integer lastMark = null;
		for (IndividualIssue issue : getIndividualIssues()) {
			Integer mark = issue.getBreakMark(mode);
			if (!mark.equals(lastMark) && lastMark != null)
				breakIDs.add(issue.getId());
			lastMark = mark;
		}
	}

	/**
	 * The method getIndividualIssues generates a list of IndividualIssue
	 * objects, each of them representing a stamping of an (one physically
	 * appeared) issue.
	 * 
	 * @return a SortedSet of IndividualIssue objects, each of them representing
	 *         one physically appeared issue
	 */
	public SortedSet<IndividualIssue> getIndividualIssues() {
		SortedSet<IndividualIssue> result = new TreeSet<IndividualIssue>();
		for (Title title : this) {
			LocalDate lastAppearance = title.getLastAppearance();
			for (LocalDate day = title.getFirstAppearance(); !day.isAfter(lastAppearance); day = day.plusDays(1)) {
				for (Issue issue : title.getIssues()) {
					if (issue.isMatch(day)) {
						result.add(new IndividualIssue(title.getHeading(), day, issue.getHeading()));
					}
				}
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
	 * @throws ParserConfigurationException
	 *             if a DocumentBuilder cannot be created which satisfies the
	 *             configuration requested
	 */
	public Document toXML(Locale lang) throws ParserConfigurationException {
		Document result = XMLFuncs.newDocument();
		Element course = result.createElement("course");

		Element description = result.createElement("description");
		description.appendChild(result.createTextNode(StringUtils.join(verbalise(lang), "\n\n")));
		course.appendChild(description);

		Element appearances = result.createElement("appearances");
		for (IndividualIssue issue : getIndividualIssues())
			appearances.appendChild(issue.populate(result.createElement("appeared")));
		course.appendChild(appearances);

		Element processes = result.createElement("processes");
		for (String breakID : breakIDs) {
			Element process = result.createElement("process");
			process.setAttribute("break", "#".concat(breakID));
			processes.appendChild(process);
		}
		course.appendChild(processes);

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
			return CourseToGerman.toString(this);
		// add more languages here
		// …

		// default: - TODO change to English as soon as available
		return CourseToGerman.toString(this);
	}

}
