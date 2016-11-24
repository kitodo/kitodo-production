package de.sub.goobi.helper.enums;

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
import de.sub.goobi.helper.Helper;

/**
 * Enum for edit type of task steps each one has an integer value, and a title
 *
 * @author Steffen Hankiewicz
 * @version 17.05.2009
 */
public enum StepEditType {
	/**
	 * default type is unknown for all steps, which still don't have a specific type
	 */
	UNNOWKN(0, "unbekannt"),

	/** manual single workflow for regular workflow handling */
	MANUAL_SINGLE(1, "manuellSingleWorkflow"),

	/**
	 * manual multi workflow for lots of data like image processing with pages of steps
	 */
	MANUAL_MULTI(2, "manuellMultiWorkflow"),

	/** administrativ = all kinds of steps changed through administrative gui */
	ADMIN(3, "administrativ"),

	/** automatic = all kinds of automatic steps */
	AUTOMATIC(4, "automatic");

	private int value;
	private String title;

	/**
	 * private constructor, initializes integer value and title
	 */
	private StepEditType(int inValue, String inTitle) {
		this.value = inValue;
		this.title = inTitle;
	}

	/**
	 * return integer value for database savings
	 *
	 * @return value as integer
	 */
	public Integer getValue() {
		return this.value;
	}

	/**
	 * get title from editType
	 *
	 * @return title as translated string for current locale
	 */
	public String getTitle() {
		return Helper.getTranslation(this.title);
	}

	/**
	 * retrieve editType by integer value, necessary for database handlings, where only integer is saved but not type
	 * safe
	 *
	 * @param editType as integer value
	 * @return {@link StepEditType} for given integer
	 */
	public static StepEditType getTypeFromValue(Integer editType) {
		if (editType != null) {
			for (StepEditType ss : values()) {
				if (ss.getValue() == editType.intValue()) {
					return ss;
				}
			}
		}
		return UNNOWKN;
	}

}
