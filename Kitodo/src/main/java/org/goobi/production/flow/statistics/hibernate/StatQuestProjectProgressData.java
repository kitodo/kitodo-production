/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */

package org.goobi.production.flow.statistics.hibernate;

import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.helper.Helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe;
import org.goobi.production.flow.statistics.StepInformation;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.joda.time.DateTime;
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;
import org.kitodo.dto.BaseDTO;
import org.kitodo.dto.ProcessDTO;

/**
 * Imlpementation of {@link IStatisticalQuestion}. This is used for the
 * generation of a Datatable relfecting the progress of a project, based on it's
 * processes workflow. Only the workflow common to all processes is used. A
 * reference step is taken and it's progress is calculated against the average
 * throughput. The average throughput is based on the duration and volume of a
 * project.
 * 
 * @author Wulf Riebensahm
 */
public class StatQuestProjectProgressData implements IStatisticalQuestionLimitedTimeframe, Serializable {

    private static final long serialVersionUID = 5488469945490611200L;
    private static final Logger logger = LogManager.getLogger(StatQuestProjectProgressData.class);
    private Date timeFilterFrom;
    private TimeUnit timeGrouping = TimeUnit.months;
    private Date timeFilterTo;
    private List<Integer> myIDlist;
    private Boolean flagIncludeLoops = false;
    private String terminatingStep; // stepDone title
    private List<String> selectedSteps;
    private Double requiredDailyOutput;
    private Boolean flagReferenceCurve = false;
    private List<StepInformation> commonWorkFlow = null;
    private DataTable myDataTable = null;
    private String errMessage;
    private boolean isDirty = true;

    /**
     * Loops included means that all step open all stepdone are considered loops not
     * included means that only min(date) or max(date) - depending on option in.
     *
     * @see HistoryTypeEnum
     *
     * @return status of loops included or not
     */
    public Boolean getIncludeLoops() {
        return this.flagIncludeLoops;
    }

    public String getErrMessage() {
        return this.errMessage;
    }

    /**
     * Check if data is complete.
     *
     * @return true if all Data for the generation is set
     */

    public Boolean isDataComplete() {
        Boolean error = false;
        if (this.timeFilterFrom == null) {
            logger.debug("time from is not set");
            error = true;
        }
        if (this.timeFilterTo == null) {
            logger.debug("time to is not set");
            error = true;
        }
        if (this.requiredDailyOutput == null) {
            logger.debug("daily output is not set");
            error = true;
        }
        if (this.terminatingStep == null) {
            logger.debug("terminating step is not set");
            error = true;
        }
        if (this.myIDlist == null) {
            logger.debug("processes filter is not set");
            error = true;
        }
        return !error;
    }

    public void setReferenceCurve(Boolean flagIn) {
        if (flagIn == null) {
            this.flagReferenceCurve = false;
        } else {
            this.flagReferenceCurve = flagIn;
        }
        this.isDirty = true;
    }

    public void setRequiredDailyOutput(Double requiredDailyOutput) {
        this.requiredDailyOutput = requiredDailyOutput;
        this.isDirty = true;
    }

    /**
     * Set status of loops included.
     *
     * @param includeLoops
     *            as Boolean
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

        // assembling a requiredOutputRow from the labels in the reference row
        // and the calculated requiredOutputPerTimeUnit
        for (String title : this.timeGrouping.getDateRow(this.timeFilterFrom, this.timeFilterTo)) {
            dataRow.addValue(title, requiredOutputPerTimeUnit);
        }
        return dataRow;

    }

    /*
     * generate referenceCurve
     */
    private DataRow referenceCurve(DataRow referenceRow) {
        DataRow orientationRow = requiredOutput();
        DataRow dataRow = new DataRow(Helper.getTranslation("ReferenceCurve"));
        dataRow.setShowPoint(false);
        // may have to be calculated differently

        Integer count = orientationRow.getNumberValues();

        Double remainingOutput = this.requiredDailyOutput * this.timeGrouping.getDayFactor() * count;
        Double remainingAverageOutput = remainingOutput / count;

        // the way this is calculated is by subtracting each value from the
        // total remaining output
        // and calculating the averageOutput based on the remaining output and
        // the remaining periods
        for (int i = 0; i < orientationRow.getNumberValues(); i++) {
            dataRow.addValue(orientationRow.getLabel(i), remainingAverageOutput);
            Double doneValue = referenceRow.getValue(orientationRow.getLabel(i));
            if (doneValue != null) {
                remainingOutput = remainingOutput - doneValue;
            }
            count--;
            Date breakOffDate = new DateTime(this.timeFilterFrom).plusDays((int) (i * this.timeGrouping.getDayFactor()))
                    .toDate();
            if (breakOffDate.before(new Date())) {
                remainingAverageOutput = remainingOutput / count;
            }
        }

        return dataRow;
    }

    /**
     * Set data source.
     * 
     * @param source
     *            list of DTO objects
     */
    public void setDataSource(List<? extends BaseDTO> source) {
        // gathering IDs from the filter passed by dataSource
        this.myIDlist = getIds(source);
        this.isDirty = true;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> getIds(List<? extends BaseDTO> dataSource) {
        List<Integer> ids = new ArrayList<>();
        for (ProcessDTO process : (List<ProcessDTO>) dataSource) {
            ids.add(process.getId());
        }
        return ids;
    }

    /**
     * Get reference curve.
     * 
     * @return if reference curve is used of average production
     */
    public Boolean getReferenceCurve() {
        return this.flagReferenceCurve;
    }

    public DataRow getRefRow() {
        if (this.flagReferenceCurve) {
            return referenceCurve(getDataRow(this.terminatingStep));
        } else {
            return requiredOutput();
        }
    }

    public DataRow getDataRow(String stepName) {
        Boolean flagNoContent = true;
        for (int i = 0; i < getDataTable().getDataRows().size(); i++) {
            flagNoContent = false;
            DataRow dr = getDataTable().getDataRows().get(i);
            if (dr.getName().equals(stepName)) {
                return dr;
            }
        }
        // TODO: Retireve from Messages
        String message = "couldn't retrieve requested DataRow by name '" + stepName + "'";
        if (flagNoContent) {
            message = message + " - empty DataTable";
        }

        logger.error(message);
        return null;

    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables
     * (List)
     */
    private DataTable getDataTable() {
        if (this.myDataTable != null && !this.isDirty) {
            return this.myDataTable;
        }

        DataTable tableStepCompleted = getAllSteps(HistoryTypeEnum.taskDone);

        tableStepCompleted.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));
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
     *
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#
     * setCalculationUnit
     * (org.goobi.production.flow.statistics.enums.CalculationUnit)
     */
    @Override
    public void setCalculationUnit(CalculationUnit cu) {
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe
     * #setTimeFrame(java.util.Date, java.util.Date)
     */
    @Override
    public void setTimeFrame(Date timeFrom, Date timeTo) {
        this.timeFilterFrom = timeFrom;
        this.timeFilterTo = timeTo;
        this.isDirty = true;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#
     * isRendererInverted (de.intranda.commons.chart.renderer.IRenderer)
     */
    @Override
    public Boolean isRendererInverted(IRenderer inRenderer) {
        return inRenderer instanceof ChartRenderer;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#
     * getNumberFormatPattern()
     */
    @Override
    public String getNumberFormatPattern() {
        return "#";
    }

    /**
     * Returns a DataTable populated with the specified events.
     *
     * @param requestedType
     *            as HistoryTypeEnum
     * @return DataTable with all tasks
     */
    private DataTable getAllSteps(HistoryTypeEnum requestedType) {

        // adding time restrictions
        String natSQL = new SQLStepRequestByName(this.timeFilterFrom, this.timeFilterTo, this.timeGrouping,
                this.myIDlist).getSQL(requestedType, null, true, this.flagIncludeLoops);

        return buildDataTableFromSQL(natSQL);
    }

    /**
     * Method generates a DataTable based on the input SQL. Methods success is
     * depending on a very specific data structure ... so don't use it if you don't
     * exactly understand it
     *
     *
     * @param natSQL
     *            headerFromSQL -> to be used, if headers need to be read in first
     *            in order to get a certain sorting
     * @return DataTable
     */
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

        // needs to be there otherwise an exception is thrown
        query.addScalar("stepCount", StandardBasicTypes.DOUBLE);
        query.addScalar("stepName", StandardBasicTypes.STRING);
        query.addScalar("intervall", StandardBasicTypes.STRING);

        @SuppressWarnings("rawtypes")
        List list = query.list();

        DataTable dtbl = new DataTable("");

        // Set columns to be removed later.
        dtbl.addDataRow(headerRow);

        DataRow dataRow = null;

        // each data row comes out as an Array of Objects
        // the only way to extract the data is by knowing
        // in which order they come out

        // checks if intervall has changed which then triggers the start for a
        // new row
        // intervall here is the timeGroup Expression (e.g. "2006/05" or
        // "2006-10-05")
        String observeIntervall = "";

        for (Object obj : list) {
            Object[] objArr = (Object[]) obj;
            String stepName = new Converter(objArr[1]).getString();
            if (isInWorkFlow(stepName)) {
                try {
                    String intervall = new Converter(objArr[2]).getString();

                    if (!observeIntervall.equals(intervall)) {
                        observeIntervall = intervall;

                        // row cannot be added before it is filled because the
                        // add process triggers
                        // a testing for header alignement -- this is where we
                        // add it after iterating it first
                        if (dataRow != null) {
                            dtbl.addDataRow(dataRow);
                        }

                        // setting row name with localized time group and the
                        // date/time extraction based on the group
                        dataRow = new DataRow(intervall);
                    }
                    if (dataRow != null) {
                        Double count = new Converter(objArr[0]).getDouble();
                        dataRow.addValue(stepName, count);
                    }

                } catch (Exception e) {
                    if (dataRow != null) {
                        dataRow.addValue(e.getMessage(), 0.0);
                    }
                }
            }
        }
        // to add also the last row
        if (dataRow != null) {
            dtbl.addDataRow(dataRow);
        }

        // now removing headerRow
        dtbl.removeDataRow(headerRow);

        return dtbl;
    }

    public void setCommonWorkflow(List<StepInformation> commonWorkFlow) {
        this.commonWorkFlow = commonWorkFlow;
        this.isDirty = true;
    }

    /**
     * Sets the terminating Step for this view.
     *
     * @param terminatingStep
     *            as String
     */
    public void setTerminatingStep(String terminatingStep) {
        this.terminatingStep = terminatingStep;
        this.isDirty = true;
    }

    /**
     * Get selectable tasks.
     * 
     * @return List of Steps that are selectable for this View
     */

    public List<String> getSelectableSteps() {
        List<String> selectableList = new ArrayList<>();
        selectableList.add(Helper.getTranslation("selectAll"));
        for (StepInformation steps : this.commonWorkFlow) {
            selectableList.add(steps.getTitle());
        }
        return selectableList;
    }

    public void setSelectedSteps(List<String> inSteps) {
        this.isDirty = true;
        if (inSteps.contains(Helper.getTranslation("selectAll"))) {
            this.selectedSteps = new ArrayList<>();
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

    public List<String> getSelectedSteps() {
        return this.selectedSteps;
    }

    /**
     * Get selectable time units.
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
     * Get selected tables.
     *
     * @return DataTable generated from the selected step names and the selected
     *         reference curve
     */
    public DataTable getSelectedTable() {
        getDataTable();
        DataTable returnTable = new DataTable(this.terminatingStep);
        returnTable.addDataRow(getRefRow());
        for (String stepTitle : this.selectedSteps) {
            returnTable.addDataRow(getDataRow(stepTitle));
        }
        // rest this, so that unit knows that no changes were made in between
        // calls
        return returnTable;
    }

    @Override
    public List<DataTable> getDataTables(List dataSource) {
        return null;
    }

    public boolean hasChanged() {
        return this.isDirty;
    }

    public TimeUnit getTimeUnit() {
        return this.timeGrouping;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit
     * (org.goobi.production.flow.statistics.enums.TimeUnit)
     */
    @Override
    public void setTimeUnit(TimeUnit timeUnit) {
        this.isDirty = true;
        this.timeGrouping = timeUnit;
    }
}
