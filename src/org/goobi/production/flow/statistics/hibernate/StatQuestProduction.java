package org.goobi.production.flow.statistics.hibernate;

/**
 * This file is part of the Goobi Application - a Workflow tool for the support of 
 * mass digitization.
 * 
 * Visit the websites for more information. 
 *   - http://gdz.sub.uni-goettingen.de 
 *   - http://www.intranda.com 
 * 
 * Copyright 2009, Center for Retrospective Digitization, Göttingen (GDZ),
 * 
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License as published by the Free Software Foundation; 
 * either version 2 of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 * Boston, MA 02111-1307 USA
 * 
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.goobi.production.flow.statistics.IDataSource;
import org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.Beans.Prozess;
import de.sub.goobi.helper.Helper;

/**
 * This class is an implementation of {@link IStatisticalQuestionLimitedTimeframe} 
 * and retrieves statistical Data about the productivity of the selected
 * processes, which are passed into this class via implemetations of the
 * IDataSource interface.
 * 
 * According to {@link IStatisticalQuestionLimitedTimeframe} other parameters
 * can be set before the productivity of the selected {@link Prozess}es is evaluated.
 * 
 * @author Wulf Riebensahm
 *
 */
public class StatQuestProduction implements
		IStatisticalQuestionLimitedTimeframe {

	//default value time filter is open
	Date timeFilterFrom;
	Date timeFilterTo;

	//default values set to days and volumesAndPages
	TimeUnit timeGrouping = TimeUnit.days;
	private CalculationUnit cu = CalculationUnit.volumesAndPages;

	

	/**
	 * IDataSource needs here to be an implementation of
	 * hibernate.IEvaluableFilter, which is a hibernate based extension of
	 * IDataSource
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables(org.goobi.production.flow.statistics.IDataSource)
	 ****************************************************************************/
	@SuppressWarnings("unchecked")
	public List<DataTable> getDataTables(IDataSource dataSource) {

		//contains an intger representing "reihenfolge" in schritte, as defined for this request
		//if not defined it will trigger a fall back on a different way of retrieving the statistical data
		Integer exactStepDone = null;

		List<DataTable> allTables = new ArrayList<DataTable>();

		IEvaluableFilter originalFilter;

		if (dataSource instanceof IEvaluableFilter) {
			originalFilter = (IEvaluableFilter) dataSource;
		} else {
			throw new UnsupportedOperationException(
					"This implementation of IStatisticalQuestion needs an IDataSource for method getDataSets()");
		}

		//gathering some information from the filter passed by dataSource
		//exactStepDone is very important ... 

		try {
			exactStepDone = originalFilter.stepDone();
		} catch (UnsupportedOperationException e1) {
		}

		// we have to build a query from scratch by reading the ID's
		List<Integer> IDlist = null;
		try {
			IDlist = originalFilter.getIDList();
		} catch (UnsupportedOperationException e) {
		}

		// adding time restrictions
		String natSQL = new SQLProduction(timeFilterFrom, timeFilterTo,
				timeGrouping, IDlist).getSQL(exactStepDone);

		Session session = Helper.getHibernateSession();

		SQLQuery query = session.createSQLQuery(natSQL);

		//needs to be there otherwise an exception is thrown
		query.addScalar("volumes", Hibernate.INTEGER);
		query.addScalar("pages", Hibernate.INTEGER);
		query.addScalar("intervall", Hibernate.STRING);

		List list = query.list();

		StringBuilder title = new StringBuilder(StatisticsMode.getByClassName(
				this.getClass()).getTitle());
		title.append(" (");
		title.append(this.cu.getTitle());
		title.append(")");

		//building table for the Table
		DataTable dtbl = new DataTable(title.toString());
		//building a second table for the chart
		DataTable dtblChart = new DataTable(title.toString());
		// 
		DataRow dataRowChart;
		DataRow dataRow;

		// each data row comes out as an Array of Objects
		// the only way to extract the data is by knowing
		// in which order they come out 
		for (Object obj : list) {
			dataRowChart = new DataRow(null);
			dataRow = new DataRow(null);
			Object[] objArr = (Object[]) obj;
			try {

				// getting localized time group unit

				//String identifier = timeGrouping.getTitle();
				//setting row name with localized time group and the date/time extraction based on the group

				dataRowChart.setName(new Converter(objArr[2]).getString() + "");
				dataRow.setName(new Converter(objArr[2]).getString() + "");
				//dataRow.setName(new Converter(objArr[2]).getString());

				// building up row depending on requested output having different fields
				switch (this.cu) {

				case volumesAndPages: {
					dataRowChart.addValue(CalculationUnit.volumes.getTitle(),
							(new Converter(objArr[0]).getDouble()));
					dataRowChart.addValue(CalculationUnit.pages.getTitle()
							+ " (*100)",
							(new Converter(objArr[1]).getDouble()) / 100);

					dataRow.addValue(CalculationUnit.volumes.getTitle(),
							(new Converter(objArr[0]).getDouble()));
					dataRow.addValue(CalculationUnit.pages.getTitle(),
							(new Converter(objArr[1]).getDouble()));

				}
					break;

				case volumes: {
					dataRowChart.addValue(CalculationUnit.volumes.getTitle(),
							(new Converter(objArr[0]).getDouble()));
					dataRow.addValue(CalculationUnit.volumes.getTitle(),
							(new Converter(objArr[0]).getDouble()));

				}
					break;

				case pages: {

					dataRowChart.addValue(CalculationUnit.pages.getTitle(),
							(new Converter(objArr[1]).getDouble()));
					dataRow.addValue(CalculationUnit.pages.getTitle(),
							(new Converter(objArr[1]).getDouble()));

				}
					break;

				}

				// fall back, if conversion triggers an exception
			} catch (Exception e) {
				dataRowChart.addValue(e.getMessage(), new Double(0));
				dataRow.addValue(e.getMessage(), new Double(0));
			}

			//finally adding dataRow to DataTable and fetching next row
			// adding the extra table
			dtblChart.addDataRow(dataRowChart);
			dtbl.addDataRow(dataRow);
		}

		// a list of DataTables is expected as return Object, even if there is only one 
		// Data Table as it is here in this implementation
		dtblChart.setUnitLabel(Helper.getTranslation(timeGrouping
				.getSingularTitle()));
		dtbl.setUnitLabel(Helper
				.getTranslation(timeGrouping.getSingularTitle()));

		dtblChart.setShowableInTable(false);
		dtbl.setShowableInChart(false);

		allTables.add(dtblChart);
		allTables.add(dtbl);
		return allTables;

	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe#setTimeFrame(java.util.Date, java.util.Date)
	 */
	public void setTimeFrame(Date timeFrom, Date timeTo) {
		this.timeFilterFrom = timeFrom;
		this.timeFilterTo = timeTo;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit(org.goobi.production.flow.statistics.enums.TimeUnit)
	 */
	public void setTimeUnit(TimeUnit timeGrouping) {
		this.timeGrouping = timeGrouping;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setCalculationUnit(org.goobi.production.flow.statistics.enums.CalculationUnit)
	 */
	public void setCalculationUnit(CalculationUnit cu) {
		this.cu = cu;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#isRendererInverted(de.intranda.commons.chart.renderer.IRenderer)
	 */
	public Boolean isRendererInverted(IRenderer inRenderer) {
		return inRenderer instanceof ChartRenderer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getNumberFormatPattern()
	 */
	public String getNumberFormatPattern() {
		return "#";
	}
}
