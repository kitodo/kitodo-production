package de.sub.goobi.helper.enums;

//CHECKSTYLE:OFF
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.kitodo.org
 *     		- https://github.com/goobi/goobi-production
 * 		    - http://gdz.sub.uni-goettingen.de
 * 			- http://www.intranda.com
 * 			- http://digiverso.com 
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
//CHECKSTYLE:ON

/**
 * This enum contains property types, which can be used for display and validation purpose Validation can be done by
 * engaging validation classes, which could be returned by the validation type Enum, contained in here
 *
 * @author Wulf
 */
public enum PropertyType {

	unknown(0, "unknown", false, false),

	/** general type */
	general(1, "general", false, false),

	/** normal message */
	messageNormal(2, "messageNormal", false, false),

	/** important message */
	messageImportant(3, "messageImportant", false, false),

	/** error message */
	messageError(4, "messageError", false, false), String(5, "String", true, true), Boolean(6, "Boolean", true, true),
	List(7, "List", true, true), Number(8, "Number", true, true), Container(9, "Container", true, true),
	Date(10, "Date", true, true), Integer(11, "Integer", true, true), SpecialView(12, "SpecialView", false, true),
	Textarea(13, "Textarea", true, true), ListMultiSelect(14, "ListMultiSelect", true, true), WikiField(15, "WikiField",
			false, true),
	// special Properties
	Hidden(16, "Hidden", false, false), ErrorMessage(17, "ErrorMessage", true, false), CommandLink(18, "CommandLink",
			true, false), NoEdit(19, "NoEdit", true, false), Filter(20, "Filter", false, false);

	private int id;
	private String name;

	private Boolean showInDisplay;

	private PropertyType(int id, String inName, Boolean showInDisplay, Boolean editable) {
		this.id = id;
		this.name = inName;

		this.showInDisplay = showInDisplay;
	}

	public String getName() {
		return this.name.toLowerCase();
	}

	@Override
	public java.lang.String toString() {
		return this.name();
	}

	/**
	 * @param inName add description
	 * @return add description
	 */
	public static PropertyType getByName(String inName) {
		for (PropertyType p : PropertyType.values()) {
			if (p.getName().equals(inName.toLowerCase())) {
				return p;
			}
		}
		return String;
	}

	public Boolean getShowInDisplay() {
		return showInDisplay;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @param id add description
	 * @return add description
	 */
	public static PropertyType getById(int id) {
		for (PropertyType p : PropertyType.values()) {
			if (p.getId() == (id)) {
				return p;
			}
		}
		return String;
	}

}
