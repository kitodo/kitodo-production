package org.goobi.production.flow.statistics;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information. 
 *     		- http://www.goobi.org
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


import java.util.List;

import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;

import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataTable;


/**
 * This interface defines the required methods for any implementations of statistical requests
 * to be used with the StatisticalManager. There is an extension to this interface for 
 * implementations which needs time restriction to be set
 * 
 *  @author Wulf Riebensahm
 *  @author Steffen Hankiewicz
 ****************************************************************************/
public interface IStatisticalQuestion {

	/**
	 * This method returns a list of DataTable Objects, which can be used to display
	 * the results of the data request
	 * 
	 * @param dataSource
	 * @return List<DataTable> 
	 ****************************************************************************/
	public List<DataTable> getDataTables(IDataSource dataSource);

	/**
	 * This method is used to set a grouping unit for time based data 
	 * 
	 * @param timeUnit
	 ****************************************************************************/
	public void setTimeUnit(TimeUnit timeUnit);
	
	/**
	 * This method sets the calculation unit needed for specific requests.
	 * Since not all implementations will support this feature future implementations
	 * of this method will throw an UnsupportedOperationException
	 * 
	 * @param cu
	 */
	public void setCalculationUnit(CalculationUnit cu);

	/**
	 * get all IRenderer where the DataTable matrix should be used inverted
	 * 
	 * @param inRenderer as {@link IRenderer}
	 * @return true, if rendering should use invertet {@link DataTable}
	 ****************************************************************************/
	public Boolean isRendererInverted(IRenderer inRenderer);
	
	/**
	 * get number format pattern
	 * 
	 * @return number format pattern for rendering the results
	 ****************************************************************************/
	public String getNumberFormatPattern();
}
