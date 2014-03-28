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

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.goobi.production.model.bibliography.course.Course;
import org.goobi.production.model.bibliography.course.Granularity;
import org.w3c.dom.Document;

import de.sub.goobi.helper.FacesUtils;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.XMLUtils;

/**
 * The class GranularityForm provides the screen logic for a JSF page to choose
 * the granularity to split up the course of appearance of a newspaper into
 * Goobi processes.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class GranularityForm {
	private static final Logger logger = Logger.getLogger(GranularityForm.class);

	/**
	 * The field granularity holds the granularity chosen by the user. It is
	 * null initially indicating that the user didn’t choose anything yet.
	 */
	protected Granularity granularity;

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
	 * The procedure breakModeClick() is called from the procedures which are
	 * called if the user clicks one of the button to select the granularity
	 * level. It sets the granularity to the given BreakMode and triggers the
	 * recalculation of the processes in the course of appearance data model.
	 */
	private void alterGranularityClick(Granularity granularity) {
		this.granularity = granularity;
		course.splitInto(granularity);
	}

	/**
	 * The procedure daysClick() is called if the user clicks the button to
	 * select the granularity level “days”. It sets the granularity to
	 * BreakMode.DAYS and triggers the recalculation of the breaks in the course
	 * of appearance data model.
	 */
	public void daysClick() {
		alterGranularityClick(Granularity.DAYS);
	}

	/**
	 * The procedure downloadClick() is called if the user clicks the button to
	 * download the course of appearance in XML format.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs
	 * @throws TransformerException
	 *             when it is not possible to create a Transformer instance or
	 *             if an unrecoverable error occurs during the course of the
	 *             transformation
	 */
	public void downloadClick() {
		try {
			course.recalculateRegularityOfIssues();
			Document courseXML = course.toXML();
			byte[] data = XMLUtils.documentToByteArray(courseXML, 4);
			FacesUtils.sendDownload(data, course.get(0).getHeading() + ".xml");
		} catch (TransformerException e) {
			Helper.setFehlerMeldung("granularity.download.error", "error.TransformerException");
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			Helper.setFehlerMeldung("granularity.download.error", "error.IOException");
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * The function getGranularity() returns the granularity level chosen by the
	 * user in lower case as read-only property “granularity”. If there are no
	 * processes—indicating that the user didn’t choose anything yet or didn’t
	 * choose anything again after clicking back in the bread crumbs and
	 * altering the course of appearance in a way that the processes need to be
	 * recalculated—it literally returns “null” as String. If there are
	 * processes loaded from a foreign source, it returns “foreign”.
	 * 
	 * @return the granularity level chosen by the user
	 */
	public String getGranularity() {
		if (course.getNumberOfProcesses() == 0)
			return "null";
		if (granularity == null)
			return "foreign";
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
	 * The function getNumberOfPages returns the total number of pages of the
	 * digitization project entered by the user or a guessed value as read-only
	 * property “numberOfPagesOptionallyGuessed”
	 * 
	 * @return an (optionally guessed) total number of pages
	 */
	public Long getNumberOfPagesOptionallyGuessed() {
		if (numberOfPages == null)
			return course.guessTotalNumberOfPages();
		else
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
		return course.getNumberOfProcesses();
	}

	/**
	 * The procedure issuesClick() is called if the user clicks the button to
	 * select the granularity level “issues”. It sets the granularity to
	 * BreakMode.ISSUES and triggers the recalculation of the breaks in the
	 * course of appearance data model.
	 */
	public void issuesClick() {
		alterGranularityClick(Granularity.ISSUES);
	}

	/**
	 * The procedure monthsClick() is called if the user clicks the button to
	 * select the granularity level “months”. It sets the granularity to
	 * BreakMode.MONTHS and triggers the recalculation of the breaks in the
	 * course of appearance data model.
	 */
	public void monthsClick() {
		alterGranularityClick(Granularity.MONTHS);
	}

	/**
	 * The procedure monthsClick() is called if the user clicks the button to
	 * select the granularity level “quarters”. It sets the granularity to
	 * BreakMode.MONTHS and triggers the recalculation of the breaks in the
	 * course of appearance data model.
	 */
	public void quartersClick() {
		alterGranularityClick(Granularity.QUARTERS);
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
		alterGranularityClick(Granularity.WEEKS);
	}

	/**
	 * The procedure yearsClick() is called if the user clicks the button to
	 * select the granularity level “years”. It sets the granularity to
	 * BreakMode.YEARS and triggers the recalculation of the breaks in the
	 * course of appearance data model.
	 */
	public void yearsClick() {
		alterGranularityClick(Granularity.YEARS);
	}
}
