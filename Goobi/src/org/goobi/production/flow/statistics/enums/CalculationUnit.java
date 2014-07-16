package org.goobi.production.flow.statistics.enums;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
 *     		- http://launchpad.net/goobi-production
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
 * Enum of all calculation units for the statistics
 * 
 * @author Steffen Hankiewicz
 * @version 21.05.2009
 ****************************************************************************/
public enum CalculationUnit {

	volumes("1", "volumes"), pages("2", "pages"), volumesAndPages("3",
			"volumesAndPages");

	private String id;
	private String title;

	/**
	 * private constructor for setting id and title
	 * 
	 * @param inTitle
	 *            title as String
	 * @param inId
	 *            id as string
	 ****************************************************************************/
	private CalculationUnit(String inId, String inTitle) {
		id = inId;
		title = inTitle;
	}

	/**
	 * return unique ID for CalculationUnit
	 * 
	 * @return unique ID as String
	 ****************************************************************************/
	public String getId() {
		return id;
	}

	/**
	 * return localized title for CalculationUnit
	 * 
	 * @return localized title
	 ****************************************************************************/
	public String getTitle() {
		return Helper.getTranslation(title);
	}

	/**
	 * get CalculationUnit by unique ID
	 * 
	 * @param inId
	 *            the unique ID
	 * @return {@link CalculationUnit} with given ID
	 ****************************************************************************/
	public static CalculationUnit getById(String inId) {
		for (CalculationUnit unit : CalculationUnit.values()) {
			if (unit.getId().equals(inId)) {
				return unit;
			}
		}
		return volumes;
	}

}
