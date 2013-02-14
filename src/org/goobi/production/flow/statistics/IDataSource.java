/*
 * This file is part of the Goobi Application - a Workflow tool for the support of
 * mass digitization.
 *
 * Visit the websites for more information.
 *     - http://gdz.sub.uni-goettingen.de
 *     - http://www.goobi.org
 *     - http://launchpad.net/goobi-production
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
 * Suite 330, Boston, MA 02111-1307 USA
 */

package org.goobi.production.flow.statistics;

import java.io.Serializable;
import java.util.List;

/**
 * This interface defines some general dataset for all future kind of
 * statistical questions
 * 
 * @author Wulf Riebensahm
 * @author Steffen Hankiewicz
 */
public interface IDataSource extends Serializable {

	/**
	 * This method returns the original Data and enables the continued use of
	 * older Statistic functions in the restructured data flow
	 ****************************************************************************/
	public List<Object> getSourceData();

}
