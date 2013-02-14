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

package org.goobi.production.flow.statistics.hibernate;

import java.util.List;
import java.util.Observable;

import org.goobi.production.flow.statistics.IDataSource;
import org.hibernate.Criteria;

/**
 * This interface defines a filter, which can be used in the goobi/hibernate
 * context. It may be implemented serializable so it could be saved and loaded.
 * It manages the creation and building of a criteria according to the users
 * or programmers input, holding the result.
 * 
 * @author Wulf Riebensahm
 ****************************************************************************/
public interface IEvaluableFilter extends IDataSource {

	/**
	 * As an option name could be set, so that user could select filter by name
	 * in case this feature is going to be implemented in leter versions 
	 ****************************************************************************/
	public void setName(String name);

	/**
	 * @return name
	 */
	public String getName();

	/**
	 * @return Criteria based on the implemented filter  
	 */
	public Criteria getCriteria();

	/**
	 * allows the creation of a second filter, independent from the original one
	 */
	public IEvaluableFilter clone();

	/**
	 * @param Filter - allows passing on a String which may define a filter
	 * 
	 */
	public void setFilter(String Filter);

	/**
	 * @param sqlString allows passing on a String which may set an sql statement
	 */
	public void setSQL(String sqlString);

	/**
	 * @return List containing all ID's from selected filter
	 */
	public List<Integer> getIDList();

	/**
	 * 
	 * @return Observable, exposing an Observable Object to register an Observer
	 */

	public Observable getObservable();

	/**
	 * 
	 * @return Integer step if an exact stepDone filter is set (needed for Statistic AP2)
	 */
	public Integer stepDone();

	public String stepDoneName();

}
