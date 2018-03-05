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

import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.IStatisticalQuestionLimitedTimeframe;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;
import org.kitodo.dto.BaseDTO;
import org.kitodo.dto.ProcessDTO;

/**
 * Implementation of {@link IStatisticalQuestion}. Statistical Request with
 * predefined Values in data Table
 * 
 * @author Wulf Riebensahm
 */
public class StatQuestStorage implements IStatisticalQuestionLimitedTimeframe {

    private Date timeFilterFrom;
    private TimeUnit timeGrouping;
    private Date timeFilterTo;

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit(
     * org.goobi.production.flow.statistics.enums.TimeUnit)
     */
    @Override
    public void setTimeUnit(TimeUnit timeGrouping) {
        this.timeGrouping = timeGrouping;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables(
     * List)
     */
    @Override
    public List<DataTable> getDataTables(List<? extends BaseDTO> dataSource) {
        List<DataTable> allTables = new ArrayList<>();

        // gathering IDs from the filter passed by dataSource
        List<Integer> idList = getIds(dataSource);

        if (idList == null || idList.size() == 0) {
            return new ArrayList<>();
        }

        // TODO: filter results according to date without sql query
        // adding time restrictions
        String natSQL = "";

        Session session = Helper.getHibernateSession();
        SQLQuery query = session.createSQLQuery(natSQL);

        // needs to be there otherwise an exception is thrown
        query.addScalar("storage", StandardBasicTypes.DOUBLE);
        query.addScalar("intervall", StandardBasicTypes.STRING);

        @SuppressWarnings("rawtypes")
        List list = query.list();

        DataTable dtbl = new DataTable(
                StatisticsMode.getByClassName(this.getClass()).getTitle() + " " + Helper.getTranslation("_inGB"));

        DataRow dataRow;

        // each data row comes out as an Array of Objects the only way to
        // extract the data is by knowing
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

        // a list of DataTables is expected as return Object, even if there is
        // only one Data Table as it is here in this implementation
        dtbl.setUnitLabel(Helper.getTranslation(this.timeGrouping.getSingularTitle()));
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
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#
     * setCalculationUnit(
     * org.goobi.production.flow.statistics.enums.CalculationUnit)
     */
    @Override
    public void setCalculationUnit(CalculationUnit cu) {
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
     * isRendererInverted( de.intranda.commons.chart.renderer.IRenderer)
     */
    @Override
    public Boolean isRendererInverted(IRenderer inRenderer) {
        // return false;
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
        return "#.####";
    }

}
