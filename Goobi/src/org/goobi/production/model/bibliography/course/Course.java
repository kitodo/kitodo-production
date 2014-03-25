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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

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
	 * List of Lists of Issues, each representing a process.
	 */
	final List<List<IndividualIssue>> processes;

	/**
	 * Default constructor, creates an empty course. Must be made explicit since
	 * we offer other constructors, too.
	 */
	public Course() {
		super();
		processes = new ArrayList<List<IndividualIssue>>();
	}

	/**
	 * Copy constructor, required for constructor CourseXML(Course). Doesn’t
	 * deep copy.
	 */
	protected Course(Course course) {
		super(course);
		processes = course.processes;
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
}
