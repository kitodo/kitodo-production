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
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.kitodo.data.database.beans.Process;
import org.kitodo.dto.BaseDTO;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.production.exceptions.UnreachableCodeException;

/**
 * This class is an implementation of
 * {@link IStatisticalQuestionLimitedTimeframe} and retrieves statistical Data
 * about the productivity of the selected processes, which are passed into this
 * class via implementations of the IDataSource interface.
 *
 * <p>
 * According to {@link IStatisticalQuestionLimitedTimeframe} other parameters
 * can be set before the productivity of the selected {@link Process}es is
 * valuated.
 * </p>
 * 
 * @author Wulf Riebensahm
 * @author Robert Sehr
 */
public class StatQuestProduction implements IStatisticalQuestionLimitedTimeframe {

    // default value time filter is open
    Date timeFilterFrom;
    Date timeFilterTo;

    // default values set to days and volumesAndPages
    TimeUnit timeGrouping = TimeUnit.days;
    private CalculationUnit cu = CalculationUnit.volumesAndPages;
    private static final Logger logger = LogManager.getLogger(StatQuestProduction.class);

    /**
     * List objects here need to extend BaseDTO.
     *
     * <p>
     * (non-Javadoc)
     * </p>
     *
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables(
     *      List)
     */
    @Override
    public List<DataTable> getDataTables(List<? extends BaseDTO> dataSource) {

        // contains an intger representing "reihenfolge" in schritte, as defined
        // for this request
        // if not defined it will trigger a fall back on a different way of
        // retrieving the statistical data
        Integer exactStepDone = null;
        String stepname = null;

        // gathering some information from the filter passed by dataSource
        // exactStepDone is very important ...

        // TODO; find way to replace it
        /*
         * try { exactStepDone = originalFilter.stepDone(); } catch
         * (UnsupportedOperationException e1) { logger.error(e1); } try {
         * stepname = originalFilter.stepDoneName(); } catch
         * (UnsupportedOperationException e1) { logger.error(e1); }
         */

        // we have to build a query from scratch by reading the ID's
        List<Integer> idList = getIds(dataSource);
        if (idList == null || idList.size() == 0) {
            return null;
        }
        String natSQL = "";
        // adding time restrictions
        if (stepname == null) {
            natSQL = new ImprovedSQLProduction(this.timeFilterFrom, this.timeFilterTo, this.timeGrouping, idList)
                    .getSQL(exactStepDone);
        } else {
            natSQL = new ImprovedSQLProduction(this.timeFilterFrom, this.timeFilterTo, this.timeGrouping, idList)
                    .getSQL(stepname);
        }
        Session session = Helper.getHibernateSession();

        SQLQuery query = session.createSQLQuery(natSQL);

        // needs to be there otherwise an exception is thrown
        query.addScalar("volumes", StandardBasicTypes.INTEGER);
        query.addScalar("pages", StandardBasicTypes.INTEGER);
        query.addScalar("intervall", StandardBasicTypes.STRING);

        StringBuilder title = new StringBuilder(StatisticsMode.PRODUCTION.getTitle());
        title.append(" (");
        title.append(this.cu.getTitle());
        if (stepname == null || stepname.equals("")) {
            title.append(")");
        } else {
            title.append(", ");
            title.append(stepname);
            title.append(" )");
        }

        // building table for the Table
        DataTable dtbl = new DataTable(title.toString());
        // building a second table for the chart
        DataTable dtblChart = new DataTable(title.toString());
        //
        DataRow dataRowChart;
        DataRow dataRow;

        @SuppressWarnings("rawtypes")
        List list = query.list();

        // each data row comes out as an array of objects
        // the only way to extract the data is by knowing
        // in which order they come out
        for (Object obj : list) {
            dataRowChart = new DataRow(null);
            dataRow = new DataRow(null);
            Object[] objArr = (Object[]) obj;
            try {

                // getting localized time group unit

                // String identifier = timeGrouping.getTitle();
                // setting row name with localized time group and the date/time
                // extraction based on the group

                dataRowChart.setName(new Converter(objArr[2]).getString() + "");
                dataRow.setName(new Converter(objArr[2]).getString() + "");
                // dataRow.setName(new Converter(objArr[2]).getString());

                // building up row depending on requested output having
                // different fields
                switch (this.cu) {
                    case volumesAndPages:
                        dataRowChart.addValue(CalculationUnit.volumes.getTitle(),
                            (new Converter(objArr[0]).getDouble()));
                        dataRowChart.addValue(CalculationUnit.pages.getTitle() + " (*100)",
                            (new Converter(objArr[1]).getDouble()) / 100);
                        dataRow.addValue(CalculationUnit.volumes.getTitle(), (new Converter(objArr[0]).getDouble()));
                        dataRow.addValue(CalculationUnit.pages.getTitle(), (new Converter(objArr[1]).getDouble()));
                        break;
                    case volumes:
                        dataRowChart.addValue(CalculationUnit.volumes.getTitle(),
                            (new Converter(objArr[0]).getDouble()));
                        dataRow.addValue(CalculationUnit.volumes.getTitle(), (new Converter(objArr[0]).getDouble()));
                        break;
                    case pages:
                        dataRowChart.addValue(CalculationUnit.pages.getTitle(), (new Converter(objArr[1]).getDouble()));
                        dataRow.addValue(CalculationUnit.pages.getTitle(), (new Converter(objArr[1]).getDouble()));
                        break;
                    default:
                        throw new UnreachableCodeException("Complete switch");
                }

                // fall back, if conversion triggers an exception
            } catch (Exception e) {
                dataRowChart.addValue(e.getMessage(), 0.0);
                dataRow.addValue(e.getMessage(), 0.0);
            }

            // finally adding dataRow to DataTable and fetching next row
            // adding the extra table
            dtblChart.addDataRow(dataRowChart);
            dtbl.addDataRow(dataRow);
        }

        // a list of DataTables is expected as return Object, even if there is
        // only one
        // Data Table as it is here in this implementation
        dtblChart.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));
        dtbl.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));

        dtblChart.setShowableInTable(false);
        dtbl.setShowableInChart(false);

        List<DataTable> allTables = new ArrayList<>();
        allTables.add(dtblChart);
        allTables.add(dtbl);
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
     * @see org.goobi.production.flow.statistics.
     * IStatisticalQuestionLimitedTimeframe# setTimeFrame(java.util.Date,
     * java.util.Date)
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
     * setTimeUnit(org.goobi.production.flow.statistics.enums.TimeUnit)
     */
    @Override
    public void setTimeUnit(TimeUnit timeGrouping) {
        this.timeGrouping = timeGrouping;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#
     * setCalculationUnit(org.goobi.production.flow.statistics.enums.
     * CalculationUnit)
     */
    @Override
    public void setCalculationUnit(CalculationUnit cu) {
        this.cu = cu;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#
     * isRendererInverted(de.intranda.commons.chart.renderer.IRenderer)
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
}
