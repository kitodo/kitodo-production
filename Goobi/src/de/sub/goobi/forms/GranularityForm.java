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
package de.sub.goobi.forms;

// import javax.faces.bean.ManagedProperty;

import org.goobi.production.model.bibliography.course.BreakMode;
import org.goobi.production.model.bibliography.course.Course;

/**
 * The class GranularityForm provides the screen logic for a JSF page to choose
 * the granularity to split up the course of appearance of a newspaper into
 * Goobi processes.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class GranularityForm {

	/**
	 * The field granularity holds the granularity chosen by the user. It is
	 * null initially indicating that the user didn’t choose anything yet.
	 */
	protected BreakMode granularity;

	/**
	 * The field course holds the course of appearance previously created by the
	 * calendar form instance that from the information about the issues that
	 * appeared is taken to be shown. This field is a managed property which is
	 * automatically populated by JSF upon form creation by calling setCourse().
	 * This behaviour is configured in faces-config.xml
	 */
	// @ManagedProperty(value = "#{CalendarForm.course}")
	protected Course course;

	/**
	 * The field numberOfPages holds the total number of pages of the
	 * digitization project guessed by the user. It is null initially indicating
	 * that the user didn’t enter anything yet.
	 */
	protected Long numberOfPages;

	/**
	 * The procedure daysClick() is called if the user clicks the button to
	 * select the granularity level “days”. It sets the granularity to
	 * BreakMode.DAYS and triggers the recalculation of the breaks in the course
	 * of appearance data model.
	 */
	public void daysClick() {
		granularity = BreakMode.DAYS;
		course.calculateBreaks(granularity);
	}

	/**
	 * The function getGranularity() returns the granularity level chosen by the
	 * user in lower case as read-only property “granularity”. If it is
	 * null—indicating that the user didn’t choose anything yet—it literally
	 * returns “null” as String.
	 * 
	 * @return the granularity level chosen by the user
	 */
	public String getGranularity() {
		if (granularity == null)
			return "null";
		return granularity.toString().toLowerCase();
	}

	/**
	 * The function getIssueCount() returns the number of issues that physically
	 * appeared as to the underlying course of appearance data model as
	 * read-only property “issueCount”.
	 * 
	 * @return the number of issues physically appeared
	 */
	public long getIssueCount() {
		return course.countIndividualIssues();
	}

	/**
	 * The function getNumberOfPages returns the total number of pages of the
	 * digitization project guessed and entered by the user—or null indicating
	 * that the user didn’t enter anything yet—as read-write property
	 * “numberOfPages”
	 * 
	 * @return the total number of pages of the digitization project
	 */
	public Long getNumberOfPages() {
		return numberOfPages;
	}

	/**
	 * The function getNumberOfProcesses() returns the number of processes that
	 * will be created if the currently set BreakMode is used as read-only
	 * property “numberOfProcesses”.
	 * 
	 * @return the number of processes that will be created
	 */
	public int getNumberOfProcesses() {
		return course.getBreaksCount() + 1;
	}

	/**
	 * The procedure issuesClick() is called if the user clicks the button to
	 * select the granularity level “issues”. It sets the granularity to
	 * BreakMode.ISSUES and triggers the recalculation of the breaks in the
	 * course of appearance data model.
	 */
	public void issuesClick() {
		granularity = BreakMode.ISSUES;
		course.calculateBreaks(granularity);
	}

	/**
	 * The procedure monthsClick() is called if the user clicks the button to
	 * select the granularity level “months”. It sets the granularity to
	 * BreakMode.MONTHS and triggers the recalculation of the breaks in the
	 * course of appearance data model.
	 */
	public void monthsClick() {
		granularity = BreakMode.MONTHS;
		course.calculateBreaks(granularity);
	}

	/**
	 * The procedure setNumberOfPages() is called by Faces on postbacks to save
	 * the received value of the read-write property “numberOfPages”.
	 * 
	 * @param value
	 *            new value to be stored
	 */
	public void setNumberOfPages(Long value) {
		numberOfPages = value;
	}

	/**
	 * The method setCourse() is called by JSF to inject the course data model
	 * into the form. This behaviour is configured in faces-config.xml
	 * 
	 * @param course
	 *            Course of appearance data model to be used
	 */
	public void setCourse(Course course) {
		this.course = course;
	}

	/**
	 * The procedure weeksClick() is called if the user clicks the button to
	 * select the granularity level “weeks”. It sets the granularity to
	 * BreakMode.WEEKS and triggers the recalculation of the breaks in the
	 * course of appearance data model.
	 */
	public void weeksClick() {
		granularity = BreakMode.WEEKS;
		course.calculateBreaks(granularity);
	}

	/**
	 * The procedure yearsClick() is called if the user clicks the button to
	 * select the granularity level “years”. It sets the granularity to
	 * BreakMode.YEARS and triggers the recalculation of the breaks in the
	 * course of appearance data model.
	 */
	public void yearsClick() {
		granularity = BreakMode.YEARS;
		course.calculateBreaks(granularity);
	}
}
