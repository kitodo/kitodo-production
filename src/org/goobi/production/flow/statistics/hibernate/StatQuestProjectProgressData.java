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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.goobi.production.flow.statistics.IDataSource;
import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe;
import org.goobi.production.flow.statistics.StepInformation;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.joda.time.DateTime;

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.enums.HistoryEventType;

/*****************************************************************************
 * Imlpementation of {@link IStatisticalQuestion}. 
 * This is used for the generation of a Datatable relfecting the progress of a project, based on 
 * it's processes workflow. Only the workflow common to all processes is used. A reference step
 * is taken and it's progress is calculated against the average throughput. The average throughput
 * is based on the duration and volume of a project.
 * 
 * @author Wulf Riebensahm
 ****************************************************************************/
public class StatQuestProjectProgressData implements IStatisticalQuestionLimitedTimeframe {
	private static final Logger logger = Logger.getLogger(StatQuestProjectProgressData.class);
	private Date timeFilterFrom;
	private TimeUnit timeGrouping = TimeUnit.months;
	private Date timeFilterTo;
	private List<Integer> myIDlist;
	private Boolean flagIncludeLoops = false;
	private String terminatingStep; //stepDone title
	private List<String> selectedSteps;
	private Double requiredDailyOutput;
	private Boolean flagReferenceCurve = false;
	private List<StepInformation> commonWorkFlow = null;
	private DataTable myDataTable = null;
	private String errMessage;
	private boolean isDirty = true;

	/**
	 * loops included means that all step open all stepdone are considered
	 * loops not included means that only min(date) or max(date) - depending on option in 
	 * @see historyEventType
	 * 
	 * @return status of loops included or not
	 */
	public Boolean getIncludeLoops() {
		return flagIncludeLoops;
	}
	
	public String getErrMessage(){
		return this.errMessage;
	}
	
	/**
	 * 
	 * @returns true if all Data for the generation is set
	 */
	
	public Boolean isDataComplete(){

		Boolean error = false;
		if (timeFilterFrom==null ){
			logger.debug("time from is not set");
			error = true;
		}
		if (timeFilterTo==null ){
			logger.debug("time to is not set");
			error = true;
		}
		if (requiredDailyOutput==null ){
			logger.debug("daily output is not set");
			error = true;
		}
		if (this.terminatingStep == null){
			logger.debug("terminating step is not set");
			error = true;
		}
		if (myIDlist ==null ){
			logger.debug("processes filter is not set");
			error = true;
		}
		return !error;

	}
	
	public void setReferenceCurve(Boolean flagIn){
		if (flagIn == null){
			this.flagReferenceCurve = false;
		}else{
			this.flagReferenceCurve = flagIn;
		}
		this.isDirty = true;
	}

	public void setRequiredDailyOutput(Double requiredDailyOutput) {
		this.requiredDailyOutput = requiredDailyOutput;
		this.isDirty = true;
	}

	/**
	 * Set status of loops included
	 * 
	 * @param includeLoops
	 */
	public void setIncludeLoops(Boolean includeLoops) {
		this.flagIncludeLoops = includeLoops;
	}

	/*
	 * generate requiredOutputLine
	 */
	private DataRow requiredOutput() {
		DataRow dataRow = new DataRow(Helper.getTranslation("requiredOutput"));
		dataRow.setShowPoint(false);

		Double requiredOutputPerTimeUnit = this.requiredDailyOutput * this.timeGrouping.getDayFactor();

		//assembling a requiredOutputRow from the labels in the reference row and the calculated  requiredOutputPerTimeUnit
		for(String title: this.timeGrouping.getDateRow(this.timeFilterFrom, this.timeFilterTo)){
			dataRow.addValue(title, requiredOutputPerTimeUnit);
		}
		return dataRow;
		
	}

	/*
	 * generate referenceCurve
	 */
	private DataRow referenceCurve(DataRow referenceRow) {
		DataRow orientationRow = requiredOutput(); //new DataRow(Helper.getTranslation("ReferenceCurve"));
		DataRow dataRow = new DataRow(Helper.getTranslation("ReferenceCurve"));
		dataRow.setShowPoint(false);
		// may have to be calculated differently

		Integer count = orientationRow.getNumberValues();

		Double remainingOutput = this.requiredDailyOutput * this.timeGrouping.getDayFactor() * count;
		Double remainingAverageOutput = remainingOutput / count;

		// the way this is calculated is by subtracting each value from the total remaining output
		// and calculating the averageOutput based on the remaining output and the remaining periods
		for(int i=0; i< orientationRow.getNumberValues(); i++){
			dataRow.addValue(orientationRow.getLabel(i), remainingAverageOutput);
			Double doneValue = referenceRow.getValue(orientationRow.getLabel(i));
			if (doneValue!=null){
				remainingOutput = remainingOutput - doneValue;
			}
			count--;
			Date breakOffDate = new DateTime(this.timeFilterFrom).plusDays((int) (i * this.timeGrouping.getDayFactor())).toDate();
			if (breakOffDate.before(new Date())){
				remainingAverageOutput = remainingOutput/count;
			}
		}

		return dataRow;
	}

	public void setDataSource(IDataSource inSource){
		//gathering IDs from the filter passed by dataSource
		try {
			myIDlist = ((IEvaluableFilter) inSource).getIDList();
		} catch (UnsupportedOperationException e) {
			logger.warn(e);
		}
		this.isDirty = true;
	}
	
	/**
	 * 
	 * @returns if reference curve is used of average production
	 */
	public Boolean getReferenceCurve(){
		return this.flagReferenceCurve;
	}
	
	
	public DataRow getRefRow(){
		if (this.flagReferenceCurve){
			return this.referenceCurve(this.getDataRow(terminatingStep));
		}else{
			return this.requiredOutput();
		}
	}
	
	
	public DataRow getDataRow(String stepName){
		Boolean flagNoContent = true;
		for(int i = 0; i< this.getDataTable().getDataRows().size(); i++){
			flagNoContent = false;
			DataRow dr = this.getDataTable().getDataRows().get(i);
			if (dr.getName().equals(stepName)){
				return dr;
			}			
		}
		//TODO: Retireve from messages
		String message = "couldn't retrieve requested DataRow by name '" + stepName + "'";
		if (flagNoContent){
			message = message + " - empty DataTable";
		}
		
		logger.error(message);
		return null;
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables(org.goobi.production.flow.statistics.IDataSource)
	 */
	private DataTable getDataTable() {
		if (this.myDataTable != null && !this.isDirty){
			return this.myDataTable;
		}

		DataTable tableStepCompleted = getAllSteps(HistoryEventType.stepDone);

		tableStepCompleted.setUnitLabel(Helper.getTranslation(timeGrouping.getSingularTitle()));
		tableStepCompleted.setName(Helper.getTranslation("doneSteps"));

		// show in line graph
		tableStepCompleted.setShowableInChart(true);
		tableStepCompleted.setShowableInTable(false);
		tableStepCompleted.setShowableInPieChart(false);
		tableStepCompleted = tableStepCompleted.getDataTableInverted();

		this.myDataTable = tableStepCompleted;
		this.isDirty = false;
		return tableStepCompleted;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setCalculationUnit(org.goobi.production.flow.statistics.enums.CalculationUnit)
	 */
	public void setCalculationUnit(CalculationUnit cu) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe#setTimeFrame(java.util.Date, java.util.Date)
	 */
	public void setTimeFrame(Date timeFrom, Date timeTo) {
		this.timeFilterFrom = timeFrom;
		this.timeFilterTo = timeTo;
		this.isDirty = true;
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

	/**returns a DataTable populated with the specified events
	 * 
	 * @param requestedType
	 * @return
	 */
	private DataTable getAllSteps(HistoryEventType requestedType) {

		// adding time restrictions
		String natSQL = new SQLStepRequestByName(timeFilterFrom, timeFilterTo, timeGrouping, myIDlist).getSQL(requestedType, null, true,
				flagIncludeLoops);

		return buildDataTableFromSQL(natSQL);
	}

	/** Method generates a DataTable based on the input SQL.
	 *  Methods success is depending on a very specific data
	 *  structure ... so don't use it if you don't exactly 
	 *  understand it
	 *  
	 * 
	 * @param natSQL, headerFromSQL -> to be used, if headers need to be 
	 * 				read in first in order to get a certain sorting 
	 * @return DataTable
	 */
	@SuppressWarnings("unchecked")
	private DataTable buildDataTableFromSQL(String natSQL) {
		Session session = Helper.getHibernateSession();

		if (this.commonWorkFlow == null) {
			return null;
		}

		DataRow headerRow = new DataRow("Header - delete again");

		for (StepInformation step : this.commonWorkFlow) {
			String stepName = step.getTitle();
			headerRow.setName("header - delete again");
			headerRow.addValue(stepName, Double.parseDouble("0"));
		}

		SQLQuery query = session.createSQLQuery(natSQL);

		//needs to be there otherwise an exception is thrown
		query.addScalar("stepCount", Hibernate.DOUBLE);
		query.addScalar("stepName", Hibernate.STRING);
		query.addScalar("intervall", Hibernate.STRING);

		List list = query.list();

		DataTable dtbl = new DataTable("");

		// if headerRow is set then add it to the DataTable to set columns
		// needs to be removed later
		if (headerRow != null) {
			dtbl.addDataRow(headerRow);
		}

		DataRow dataRow = null;

		// each data row comes out as an Array of Objects
		// the only way to extract the data is by knowing
		// in which order they come out 

		//checks if intervall has changed which then triggers the start for a new row
		//intervall here is the timeGroup Expression (e.g. "2006/05" or "2006-10-05")
		String observeIntervall = "";

		for (Object obj : list) {
			Object[] objArr = (Object[]) obj;
			String stepName = new Converter(objArr[1]).getString();
			if (this.isInWorkFlow(stepName)) {
				try {
					String intervall = new Converter(objArr[2]).getString();
					
					if (!observeIntervall.equals(intervall)) {
						observeIntervall = intervall;

						// row cannot be added before it is filled because the add process triggers 
						// a testing for header alignement -- this is where we add it after iterating it first
						if (dataRow != null) {
							dtbl.addDataRow(dataRow);
						}

						//setting row name with localized time group and the date/time extraction based on the group
						dataRow = new DataRow(intervall);
					}
					Double count = new Converter(objArr[0]).getDouble();
					dataRow.addValue(stepName, count);

				} catch (Exception e) {
					dataRow.addValue(e.getMessage(), new Double(0));
				}
			}
		}
		// to add also the last row
		if (dataRow != null) {
			dtbl.addDataRow(dataRow);
		}

		// now removing headerRow 
		if (headerRow != null) {
			dtbl.removeDataRow(headerRow);
			// if a row showing the total count over all intervalls should be added to the grid
			//the folloing line can be commented in (adding the header to the bottom)
			//dtbl.addDataRow(headerRow);
		}

		return dtbl;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getRendererInverted(de.intranda.commons.chart.renderer.IRenderer)
	 */
	public Boolean getRendererInverted(IRenderer inRenderer) {
		return null;
	}

	public void setCommonWorkflow(List<StepInformation> commonWorkFlow) {
		this.commonWorkFlow = commonWorkFlow;
		this.isDirty = true;
	}

	/** sets the terminating Step for this view
	 * 
	 * @param terminatingStep
	 */
	public void setTerminatingStep(String terminatingStep) {
		this.terminatingStep = terminatingStep;
		this.isDirty = true;
	}

	/**
	 * 
	 * @returns List of Steps that are selectable for this View
	 */

	public List<String> getSelectableSteps() {
		List<String> selectableList = new ArrayList<String>();
		selectableList.add(Helper.getTranslation("selectAll"));
		for (StepInformation steps : this.commonWorkFlow) {
			selectableList.add(steps.getTitle());
		}
		return selectableList;
	}
	
	public void setSelectedSteps(List<String> inSteps){
		isDirty=true;
		if (inSteps.contains(Helper.getTranslation("selectAll"))) {
			this.selectedSteps = new ArrayList<String>();
			for (StepInformation steps : this.commonWorkFlow) {
				this.selectedSteps.add(steps.getTitle());
				this.terminatingStep = steps.getTitle();
			}
		} else {
			this.selectedSteps = inSteps;
			if (inSteps.size() > 0) {
				this.terminatingStep = inSteps.get(inSteps.size() - 1);
			}
		}
	}

	public List<String> getSelectedSteps(){
		return this.selectedSteps;
	}

	/**
	 * 
	 * @return list of Timeunits to select
	 */
	public List<TimeUnit> getSelectableTimeUnits() {
		return TimeUnit.getAllVisibleValues();
	}
	
	/*
	 * checks if testString is contained in workflow
	 */
	private Boolean isInWorkFlow(String testString) {
		for (StepInformation step : this.commonWorkFlow) {
			if (step.getTitle().equals(testString)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @returns DataTable generated from the selected step names and the selected reference curve
	 */
	public DataTable getSelectedTable(){
		getDataTable();
		DataTable returnTable = new DataTable(terminatingStep);
		returnTable.addDataRow(this.getRefRow());
		for (String stepTitle: this.selectedSteps){
			returnTable.addDataRow(this.getDataRow(stepTitle));
		}
		//rest this, so that unit knows that no changes were made in between calls
		return returnTable;
	}

	public List<DataTable> getDataTables(IDataSource dataSource) {
		return null;
	}

	public boolean hasChanged() {
		return this.isDirty;
	}

	public TimeUnit getTimeUnit(){
		return this.timeGrouping;
	}

	/*
	 * (non-Javadoc)
	 * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit(org.goobi.production.flow.statistics.enums.TimeUnit)
	 */
	public void setTimeUnit(TimeUnit timeUnit) {
		this.isDirty = true;
		this.timeGrouping = timeUnit;
	}
}
