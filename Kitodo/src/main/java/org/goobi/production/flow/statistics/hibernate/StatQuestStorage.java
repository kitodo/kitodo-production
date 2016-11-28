package org.goobi.production.flow.statistics.hibernate;

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

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;

import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.production.flow.statistics.IDataSource;
import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

/**
 * Implementation of {@link IStatisticalQuestion}. Statistical Request with predefined Values in data Table
 *
 * @author Wulf Riebensahm
 */
public class StatQuestStorage implements IStatisticalQuestionLimitedTimeframe {

	private Date timeFilterFrom;
	private TimeUnit timeGrouping;
	private Date timeFilterTo;

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit(
	 * org.goobi.production.flow.statistics.enums.TimeUnit)
	 */
	@Override
	public void setTimeUnit(TimeUnit timeGrouping) {
		this.timeGrouping = timeGrouping;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables(
	 * org.goobi.production.flow.statistics.IDataSource)
	 */
	@Override
	public List<DataTable> getDataTables(IDataSource dataSource) {

		List<DataTable> allTables = new ArrayList<DataTable>();

		IEvaluableFilter originalFilter;

		if (dataSource instanceof IEvaluableFilter) {
			originalFilter = (IEvaluableFilter) dataSource;
		} else {
			throw new UnsupportedOperationException(
					"This implementation of IStatisticalQuestion needs an IDataSource for method getDataSets()");
		}

		// gathering IDs from the filter passed by dataSource
		List<Integer> IDlist = null;
		try {
			IDlist = originalFilter.getIDList();
		} catch (UnsupportedOperationException e) {
		}
		if (IDlist == null || IDlist.size() == 0) {
			return null;
		}

		// adding time restrictions
		String natSQL = new SQLStorage(this.timeFilterFrom, this.timeFilterTo, this.timeGrouping, IDlist).getSQL();

		Session session = Helper.getHibernateSession();

		SQLQuery query = session.createSQLQuery(natSQL);

		// needs to be there otherwise an exception is thrown
		query.addScalar("storage", StandardBasicTypes.DOUBLE);
		query.addScalar("intervall", StandardBasicTypes.STRING);

		@SuppressWarnings("rawtypes")
		List list = query.list();

		DataTable dtbl = new DataTable(StatisticsMode.getByClassName(this.getClass()).getTitle() + " "
				+ Helper.getTranslation("_inGB"));

		DataRow dataRow;

		// each data row comes out as an Array of Objects
		// the only way to extract the data is by knowing
		// in which order they come out
		for (Object obj : list) {
			dataRow = new DataRow(null);
			// TODO: Don't use arrays
			Object[] objArr = (Object[]) obj;
			try {

				// getting localized time group unit

				// setting row name with date/time extraction based on the group

				dataRow.setName(new Converter(objArr[1]).getString() + "");

				dataRow.addValue(Helper.getTranslation("storageDifference"), (new Converter(objArr[0]).getGB()));

			} catch (Exception e) {
				dataRow.addValue(e.getMessage(), 0.0);
			}

			// finally adding dataRow to DataTable and fetching next row
			dtbl.addDataRow(dataRow);
		}

		// a list of DataTables is expected as return Object, even if there is only one
		// Data Table as it is here in this implementation
		dtbl.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));
		allTables.add(dtbl);
		return allTables;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setCalculationUnit(
	 * org.goobi.production.flow.statistics.enums.CalculationUnit)
	 */
	@Override
	public void setCalculationUnit(CalculationUnit cu) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe#setTimeFrame(java.util.Date,
	 * java.util.Date)
	 */
	@Override
	public void setTimeFrame(Date timeFrom, Date timeTo) {
		this.timeFilterFrom = timeFrom;
		this.timeFilterTo = timeTo;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#isRendererInverted(
	 * de.intranda.commons.chart.renderer.IRenderer)
	 */
	@Override
	public Boolean isRendererInverted(IRenderer inRenderer) {
		// return false;
		return inRenderer instanceof ChartRenderer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getNumberFormatPattern()
	 */
	@Override
	public String getNumberFormatPattern() {
		return "#.####";
	}

}
