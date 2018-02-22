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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.kitodo.data.database.helper.enums.HistoryTypeEnum;
import org.kitodo.dto.BaseDTO;
import org.kitodo.dto.ProcessDTO;

/**
 * Implementation of {@link IStatisticalQuestion}. Statistical Request with
 * predefined Values in data Table
 * 
 * @author Wulf Riebensahm
 */
public class StatQuestThroughput implements IStatisticalQuestionLimitedTimeframe {

    private Date timeFilterFrom;
    private TimeUnit timeGrouping;
    private Date timeFilterTo;
    private List<Integer> myIDlist;
    private Boolean flagIncludeLoops = false;

    /**
     * loops included means that all step open all stepdone are considered loops not
     * included means that only min(date) or max(date) - depending on option in.
     *
     * @see HistoryTypeEnum
     *
     * @return status of loops included or not
     */
    public Boolean getIncludeLoops() {
        return this.flagIncludeLoops;
    }

    /**
     * Set status of loops included.
     *
     * @param includeLoops
     *            Boolean
     */
    public void setIncludeLoops(Boolean includeLoops) {
        this.flagIncludeLoops = includeLoops;
    }

    private final Logger logger = LogManager.getLogger(StatQuestThroughput.class);

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit
     * (org.goobi.production.flow.statistics.enums.TimeUnit)
     */
    @Override
    public void setTimeUnit(TimeUnit timeGrouping) {
        this.timeGrouping = timeGrouping;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables
     * (List)
     */
    @Override
    public List<DataTable> getDataTables(List<? extends BaseDTO> dataSource) {

        List<DataTable> allTables = new ArrayList<>();

        // gathering IDs from the filter passed by dataSource
        this.myIDlist = getIds(dataSource);

        if (myIDlist == null || myIDlist.size() == 0) {
            return null;
        }

        // a list of DataTables is expected as return Object, even if there is
        // only one Data Table as it is here in this implementation
        DataTable tableStepOpenAndDone = getAllSteps(HistoryTypeEnum.taskOpen);
        tableStepOpenAndDone.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));
        tableStepOpenAndDone.setName(StatisticsMode.getByClassName(this.getClass()).getTitle() + " ("
                + Helper.getTranslation("openSteps") + ")");
        tableStepOpenAndDone = tableStepOpenAndDone.getDataTableInverted();
        tableStepOpenAndDone = tableStepOpenAndDone.getDataTableInverted();
        tableStepOpenAndDone.setShowableInChart(false);
        allTables.add(tableStepOpenAndDone);

        tableStepOpenAndDone = getAllSteps(HistoryTypeEnum.taskDone);
        tableStepOpenAndDone.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));
        tableStepOpenAndDone.setName(StatisticsMode.getByClassName(this.getClass()).getTitle() + " ("
                + Helper.getTranslation("doneSteps") + ")");
        tableStepOpenAndDone.setShowableInChart(false);
        tableStepOpenAndDone = tableStepOpenAndDone.getDataTableInverted();
        tableStepOpenAndDone = tableStepOpenAndDone.getDataTableInverted();
        allTables.add(tableStepOpenAndDone);

        // what do we do here?
        // okay ... first we find out how many steps the selected set has
        // finding lowest step and highest step (no step name discrimination)
        Integer uBound;
        Integer uBoundOpen = getMaxStepCount(HistoryTypeEnum.taskOpen);
        Integer uBoundDone = getMaxStepCount(HistoryTypeEnum.taskDone);
        if (uBoundOpen < uBoundDone) {
            uBound = uBoundDone;
        } else {
            uBound = uBoundOpen;
        }

        Integer lBound;
        Integer lBoundOpen = getMinStepCount(HistoryTypeEnum.taskOpen);
        Integer lBoundDone = getMinStepCount(HistoryTypeEnum.taskDone);

        if (lBoundOpen < lBoundDone) {
            lBound = lBoundDone;
        } else {
            lBound = lBoundOpen;
        }

        // then for each step we get both the open and the done count within the
        // selected intervalls and merge it within one table
        for (Integer i = lBound; i <= uBound; i++) {

            DataTable tableStepOpen;
            tableStepOpen = getSpecificSteps(i, HistoryTypeEnum.taskOpen);

            tableStepOpen.setShowableInTable(true);

            DataTable tableStepDone;
            tableStepDone = getSpecificSteps(i, HistoryTypeEnum.taskDone);

            tableStepDone.setShowableInTable(true);

            // to merge we just take each table and dump the entire content in a
            // row for the open step
            DataRow rowOpenSteps = new DataRow(Helper.getTranslation("openSteps") + " " + i.toString());
            for (DataRow dtr : tableStepOpen.getDataRows()) {
                rowOpenSteps.addValue(dtr.getName(), dtr.getValue(0));
            }

            // adding the first row
            String title;
            if (tableStepOpen.getName().length() > 0) {
                title = tableStepOpen.getName();
            } else {
                title = tableStepDone.getName();
            }

            tableStepOpenAndDone = new DataTable(
                    Helper.getTranslation("throughput") + " " + Helper.getTranslation("steps") + " " + title);
            tableStepOpenAndDone.addDataRow(rowOpenSteps);

            // row for the done step
            rowOpenSteps = new DataRow(Helper.getTranslation("doneSteps") + " " + i.toString());
            for (DataRow dtr : tableStepDone.getDataRows()) {
                rowOpenSteps.addValue(dtr.getName(), dtr.getValue(0));
            }

            // adding that row
            tableStepOpenAndDone.addDataRow(rowOpenSteps);

            // turning off table rendering
            tableStepOpenAndDone.setShowableInTable(false);

            // inverting the orientation
            tableStepOpenAndDone = tableStepOpenAndDone.getDataTableInverted();
            tableStepOpenAndDone.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));

            // Dates may not be all in the right order because of it's
            // composition from 2 tables
            List<DataRow> allTempRows = tableStepOpenAndDone.getDataRows();
            // this fixes the sorting problem
            allTempRows.sort(Comparator.comparing(DataRow::getName));

            allTables.add(tableStepOpenAndDone);
        }

        return allTables;
    }

    @SuppressWarnings("unchecked")
    private List<Integer> getIds(List<? extends BaseDTO> dataSource) {
        List<Integer> ids = new ArrayList<>();
        for (ProcessDTO process : (List<ProcessDTO>) dataSource) {
            ids.add(process.getId());
        }
        return ids;
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
     * returns a DataTable populated with the specified events.
     *
     * @param requestedType
     *            HistoryTypeEnum object
     * @return DataTable object
     */
    private DataTable getAllSteps(HistoryTypeEnum requestedType) {

        // adding time restrictions
        String natSQL = new SQLStepRequestsImprovedDiscrimination(this.timeFilterFrom, this.timeFilterTo,
                this.timeGrouping, this.myIDlist).getSQL(requestedType, null, true, this.flagIncludeLoops);

        // this one is supposed to make sure, that all possible headers will be
        // thrown out in the first row to build header columns
        String headerFromSQL = new SQLStepRequestsImprovedDiscrimination(this.timeFilterFrom, this.timeFilterTo, null,
                this.myIDlist).getSQL(requestedType, null, true, true);

        this.logger.trace(natSQL);
        this.logger.trace(headerFromSQL);

        return buildDataTableFromSQL(natSQL, headerFromSQL);
    }

    /**
     * returns a DataTable populated with the specified events.
     *
     * @param step
     *            Integer
     * @param requestedType
     *            HistoryTypeEnum object
     * @return DataTable object
     */

    private DataTable getSpecificSteps(Integer step, HistoryTypeEnum requestedType) {

        // adding time restrictions
        String natSQL = new SQLStepRequests(this.timeFilterFrom, this.timeFilterTo, this.timeGrouping, this.myIDlist)
                .getSQL(requestedType, step, true, this.flagIncludeLoops);

        this.logger.trace(natSQL);

        return buildDataTableFromSQL(natSQL, null);
    }

    /**
     * Method generates a DataTable based on the input SQL. Methods success is
     * depending on a very specific data structure ... so don't use it if you don't
     * exactly understand it
     *
     * @param natSQL
     *            headerFromSQL -> to be used, if headers need to be read in first
     *            in order to get a certain sorting
     * @return DataTable
     */

    // TODO Remove redundant code
    private DataTable buildDataTableFromSQL(String natSQL, String headerFromSQL) {
        Session session = Helper.getHibernateSession();

        // creating header row from headerSQL (gets all columns in one row
        DataRow headerRow = null;
        if (headerFromSQL != null) {
            headerRow = new DataRow(null);
            SQLQuery headerQuery = session.createSQLQuery(headerFromSQL);

            // needs to be there otherwise an exception is thrown
            headerQuery.addScalar("stepCount", StandardBasicTypes.DOUBLE);
            headerQuery.addScalar("stepName", StandardBasicTypes.STRING);
            headerQuery.addScalar("stepOrder", StandardBasicTypes.DOUBLE);
            headerQuery.addScalar("intervall", StandardBasicTypes.STRING);

            @SuppressWarnings("rawtypes")
            List headerList = headerQuery.list();
            for (Object obj : headerList) {
                Object[] objArr = (Object[]) obj;
                try {
                    headerRow.setName(new Converter(objArr[3]).getString() + "");
                    headerRow.addValue(
                            new Converter(new Converter(objArr[2]).getInteger()).getString() + " ("
                                    + new Converter(objArr[1]).getString() + ")",
                            (new Converter(objArr[0]).getDouble()));

                } catch (Exception e) {
                    headerRow.addValue(e.getMessage(), 0.0);
                }
            }
        }

        SQLQuery query = session.createSQLQuery(natSQL);

        // needs to be there otherwise an exception is thrown
        query.addScalar("stepCount", StandardBasicTypes.DOUBLE);
        query.addScalar("stepName", StandardBasicTypes.STRING);
        query.addScalar("stepOrder", StandardBasicTypes.DOUBLE);
        query.addScalar("intervall", StandardBasicTypes.STRING);

        @SuppressWarnings("rawtypes")
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

        // checks if intervall has changed which then triggers the start for a
        // new row
        // intervall here is the timeGroup Expression (e.g. "2006/05" or
        // "2006-10-05")
        String observeIntervall = "";

        for (Object obj : list) {
            Object[] objArr = (Object[]) obj;
            try {
                // objArr[3]
                if (!observeIntervall.equals(new Converter(objArr[3]).getString())) {
                    observeIntervall = new Converter(objArr[3]).getString();

                    // row cannot be added before it is filled because the add
                    // process triggers
                    // a testing for header alignement -- this is where we add
                    // it after iterating it first
                    if (dataRow != null) {
                        dtbl.addDataRow(dataRow);
                    }

                    dataRow = new DataRow(null);
                    // setting row name with localized time group and the
                    // date/time extraction based on the group
                    dataRow.setName(new Converter(objArr[3]).getString() + "");
                }
                if (Objects.nonNull(dataRow)) {
                    dataRow.addValue(new Converter(new Converter(objArr[2]).getInteger()).getString() + " ("
                            + new Converter(objArr[1]).getString() + ")", (new Converter(objArr[0]).getDouble()));
                }

            } catch (Exception e) {
                if (Objects.nonNull(dataRow)) {
                    dataRow.addValue(e.getMessage(), 0.0);
                }
            }
        }
        // to add the last row
        if (dataRow != null) {
            dtbl.addDataRow(dataRow);
        }

        // now removing headerRow
        if (headerRow != null) {
            dtbl.removeDataRow(headerRow);
        }

        return dtbl;
    }

    /**
     * method retrieves the highest step order in the requested history range.
     *
     * @param requestedType
     *            HistoryTypeEnum object
     */
    private Integer getMaxStepCount(HistoryTypeEnum requestedType) {

        // adding time restrictions
        String natSQL = new SQLStepRequestsImprovedDiscrimination(this.timeFilterFrom, this.timeFilterTo,
                this.timeGrouping, this.myIDlist).getSQLMaxStepOrder(requestedType);

        Session session = Helper.getHibernateSession();
        SQLQuery query = session.createSQLQuery(natSQL);

        // needs to be there otherwise an exception is thrown
        query.addScalar("maxStep", StandardBasicTypes.DOUBLE);

        @SuppressWarnings("rawtypes")
        List list = query.list();

        if (list != null && list.size() > 0 && list.get(0) != null) {
            return new Converter(list.get(0)).getInteger();
        } else {
            return 0;
        }

    }

    /**
     * method retrieves the lowest step order in the requested history range.
     *
     * @param requestedType
     *            HistoryTypeEnum object
     */
    private Integer getMinStepCount(HistoryTypeEnum requestedType) {
        // adding time restrictions
        String natSQL = new SQLStepRequestsImprovedDiscrimination(this.timeFilterFrom, this.timeFilterTo,
                this.timeGrouping, this.myIDlist).getSQLMinStepOrder(requestedType);

        Session session = Helper.getHibernateSession();
        SQLQuery query = session.createSQLQuery(natSQL);

        // needs to be there otherwise an exception is thrown
        query.addScalar("minStep", StandardBasicTypes.DOUBLE);

        @SuppressWarnings("rawtypes")
        List list = query.list();

        if (list != null && list.size() > 0 && list.get(0) != null) {
            return new Converter(list.get(0)).getInteger();
        } else {
            return 0;
        }

    }

}
