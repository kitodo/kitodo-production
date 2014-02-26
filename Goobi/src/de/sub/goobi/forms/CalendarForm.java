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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.goobi.production.model.bibliography.course.Course;
import org.goobi.production.model.bibliography.course.Title;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * The class CalendarForm provides the screen logic for a JSF calendar editor to
 * enter the course of apparance of a newspaper.
 * 
 * @author Matthias Ronge &lt;matthias.ronge@zeutschel.de&gt;
 */
public class CalendarForm {

	/**
	 * The dateConverter is used to convert between LocalDate objects and String
	 */
	protected static final DateTimeFormatter dateConverter = DateTimeFormat.forPattern("dd.MM.yyyy");

	/**
	 * The field course holds the course of appearance currently under edit by
	 * this calendar form instance.
	 */
	protected Course course;

	/**
	 * The field titleShowing holds the Title block currently showing in this
	 * calendar instance. The Title held in titleShowing must be part of the
	 * course object, too.
	 */
	protected Title titleShowing;

	/**
	 * The Map titlePickerResolver is populated with the IDs used in the title
	 * picker list box and the references to the Title objects referenced by the
	 * IDs for easily looking them up on change.
	 */
	protected Map<String, Title> titlePickerResolver;

	/**
	 * The field updateTitleAllowed is of importance only during the update
	 * model values phase of the JSF lifecycle. During that phase several setter
	 * methods are sequentially called. The first method called is
	 * setTitlePickerSelected(). If the user chose a different title block to be
	 * displayed, titleShowing will be altered. This would cause the subsequent
	 * calls to other setter methods to overwrite the values in the newly
	 * selected title block with the values of the previously displayed block
	 * which come back in in the form that is submitted by the browser if this
	 * is not blocked. Therefore setTitlePickerSelected() sets
	 * updateTitleAllowed to control whether the other setter methods shall or
	 * shall not write the incoming data to the title block referenced in
	 * titleShowing.
	 */
	protected boolean updateTitleAllowed = true;

	/**
	 * Empty constructor. Creates a new form without yet any data.
	 */
	public CalendarForm() {
		course = new Course();
		titleShowing = null;
		titlePickerResolver = new HashMap<String, Title>();
	}

	/**
	 * The function getTitlePickerOptions() returns the elements for the title
	 * picker list box as read only property "titlePickerOptions". It returns a
	 * List of Map with each two entries: "value" and "label". "value" is the
	 * hashCode() in hex of the Title which will later be used if the field
	 * "titlePickerSelected" is altered to choose the currently selected title
	 * block, "label" is the readable description to be shown to the user.
	 * 
	 * @return the elements for the title picker list box
	 */
	public List<Map<String, String>> getTitlePickerOptions() {
		List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		for (Title title : course) {
			String value = Integer.toHexString(title.hashCode());
			titlePickerResolver.put(value, title);
			Map<String, String> item = new HashMap<String, String>();
			item.put("value", value);
			item.put("label", title.toString());
			result.add(item);
		}
		return result;
	}

	/**
	 * The function getTitlePickerSelected() returns the hashCode() value of the
	 * title block currently selected as read-write property
	 * "titlePickerSelected". If a new block is under edit, returns the empty
	 * String.
	 * 
	 * @return identifier of the selected title
	 */
	public String getTitlePickerSelected() {
		return titleShowing == null ? "" : Integer.toHexString(titleShowing.hashCode());
	}

	/**
	 * The method setTitlePickerSelected() will be called by Faces to store a
	 * new value of the read-write property "titlePickerSelected". If it is
	 * different from the current one, this means that the user selected a
	 * different Title block in the title picker list box. The event will be
	 * used to alter the “titleShowing” field which keeps the Title block
	 * currently showing. “updateTitleAllowed” will be set accordingly to update
	 * the current title or to prevent fields containing data from the
	 * previously displaying title block to overwrite the data inside the newly
	 * selected one.
	 * 
	 * @param value
	 *            hashCode() in hex of the Title to be selected
	 */
	public void setTitlePickerSelected(String value) {
		if (value == null)
			return;
		updateTitleAllowed = value.equals(Integer.toHexString(titleShowing.hashCode()));
		if (!updateTitleAllowed)
			titleShowing = titlePickerResolver.get(value);
	}

	/**
	 * The method addTitleClick() creates a copy of the currently showing title
	 * block.
	 */
	public void addTitleClick() {
		Title copy = titleShowing.clone();
		LocalDate firstAppearance = course.getLastAppearance().plusDays(1);
		copy.setFirstAppearance(firstAppearance);
		copy.setLastAppearance(firstAppearance);
		course.add(copy);
		titleShowing = copy;
	}

	/**
	 * The method removeTitleClick() deletes the currently selected Title block
	 * from the course of appearance.The method is not intended to be used if
	 * there is only one block left.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if the title referenced by “titleShowing” isn’t contained in
	 *             the course of appearance
	 */
	public void removeTitleClick() {
		assert course.size() > 1;
		int index = course.indexOf(titleShowing);
		course.remove(index);
		if (index > 0)
			index--;
		titleShowing = course.get(index);
	}

	/**
	 * The function getTitleHeading() returns the heading of the Title block
	 * currently showing as read-write property "titleHeading".
	 * 
	 * @return heading of currently showing title
	 */
	public String getTitleHeading() {
		if (titleShowing == null)
			return "";
		return titleShowing.getHeading();
	}

	/**
	 * The method setTitleHeading() will be called by Faces to store a new value
	 * of the read-write property "titleHeading", which represents the heading
	 * of the Title block currently showing. The event will be used to either
	 * alter the heading of the Title block defined by the “titleShowing” field
	 * or, in case that a new title block is under edit, to initially set its
	 * title.
	 * 
	 * @param heading
	 *            new heading for the title block
	 */
	public void setTitleHeading(String heading) {
		if (titleShowing != null) {
			if (updateTitleAllowed)
				titleShowing.setHeading(heading);
		} else {
			titleShowing = new Title(heading);
			course.add(titleShowing);
		}
	}

	/**
	 * The function getFirstAppearance() returns the date of first appearance of
	 * the Title block currently showing as read-write property
	 * "firstAppearance".
	 * 
	 * @return date of first appearance of currently showing title
	 */
	public String getFirstAppearance() {
		if (titleShowing != null && titleShowing.getFirstAppearance() != null)
			return dateConverter.print(titleShowing.getFirstAppearance());
		else
			return "";
	}

	/**
	 * The method setTitleHeading() will be called by Faces to store a new value
	 * of the read-write property "firstAppearance", which represents the the
	 * date of first appearance of the Title block currently showing. The event
	 * will be used to either alter the date of first appearance of the Title
	 * block defined by the “titleShowing” field or, in case that a new title
	 * block is under edit, to initially set its the date of first appearance.
	 * 
	 * @param firstAppearance
	 *            new date of first appearance
	 */
	public void setFirstAppearance(String firstAppearance) {
		LocalDate newFirstAppearance = dateConverter.parseLocalDate(firstAppearance);
		if (titleShowing != null) {
			if (updateTitleAllowed)
				titleShowing.setFirstAppearance(newFirstAppearance);
		} else {
			titleShowing = new Title();
			titleShowing.setFirstAppearance(newFirstAppearance);
			course.add(titleShowing);
		}
	}

	/**
	 * The function getLastAppearance() returns the date of last appearance of
	 * the Title block currently showing as read-write property
	 * "lastAppearance".
	 * 
	 * @return date of last appearance of currently showing title
	 */
	public String getLastAppearance() {
		if (titleShowing != null && titleShowing.getLastAppearance() != null)
			return dateConverter.print(titleShowing.getLastAppearance());
		else
			return "";
	}

	/**
	 * The method setTitleHeading() will be called by Faces to store a new value
	 * of the read-write property "lastAppearance", which represents the the
	 * date of last appearance of the Title block currently showing. The event
	 * will be used to either alter the date of last appearance of the Title
	 * block defined by the “titleShowing” field or, in case that a new title
	 * block is under edit, to initially set its the date of last appearance.
	 * 
	 * @param lastAppearance
	 *            new date of last appearance
	 */
	public void setLastAppearance(String lastAppearance) {
		LocalDate newLastAppearance = dateConverter.parseLocalDate(lastAppearance);
		if (titleShowing != null) {
			if (updateTitleAllowed)
				titleShowing.setLastAppearance(newLastAppearance);
		} else {
			titleShowing = new Title();
			titleShowing.setLastAppearance(newLastAppearance);
			course.add(titleShowing);
		}
	}
}
