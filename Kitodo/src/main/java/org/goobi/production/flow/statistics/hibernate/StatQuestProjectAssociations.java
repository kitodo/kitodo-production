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

import de.intranda.commons.chart.renderer.HtmlTableRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.results.DataRow;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.helper.Helper;

import java.util.ArrayList;
import java.util.List;

import org.goobi.production.flow.statistics.IDataSource;
import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.hibernate.Criteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

/**
 * Implementation of {@link IStatisticalQuestion}. Statistical Request with
 * predefined Values in data Table
 * 
 * @author Wulf Riebensahm
 */
public class StatQuestProjectAssociations implements IStatisticalQuestion {

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables(
     * org.goobi.production.flow.statistics.IDataSource)
     */
    @Override
    public List<DataTable> getDataTables(IDataSource dataSource) {

        IEvaluableFilter originalFilter;

        if (dataSource instanceof IEvaluableFilter) {
            originalFilter = (IEvaluableFilter) dataSource;
        } else {
            throw new UnsupportedOperationException(
                    "This implementation of IStatisticalQuestion needs an IDataSource for method getDataSets()");
        }

        ProjectionList proj = Projections.projectionList();
        proj.add(Projections.count("id"));
        proj.add(Projections.groupProperty("project.title"));

        Criteria crit;

        if (originalFilter instanceof UserDefinedFilter) {
            crit = new UserDefinedFilter(originalFilter.getIDList()).getCriteria();
            crit.createCriteria("project", "project");
        } else {
            crit = originalFilter.clone().getCriteria();
        }

        // use a clone on the filter and apply the projection on the clone
        crit.setProjection(proj);

        String title = StatisticsMode.getByClassName(this.getClass()).getTitle();

        DataTable dtbl = new DataTable(title);
        dtbl.setShowableInPieChart(true);
        DataRow dRow = new DataRow(Helper.getTranslation("count"));

        for (Object obj : crit.list()) {
            Object[] objArr = (Object[]) obj;
            dRow.addValue(new Converter(objArr[1]).getString(),
                    new Converter(new Converter(objArr[0]).getInteger()).getDouble());
        }
        dtbl.addDataRow(dRow);

        List<DataTable> allTables = new ArrayList<>();

        dtbl.setUnitLabel(Helper.getTranslation("project"));
        allTables.add(dtbl);
        return allTables;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#
     * isRendererInverted(de.intranda.commons.chart.renderer.IRenderer)
     */
    @Override
    public Boolean isRendererInverted(IRenderer inRenderer) {
        return inRenderer instanceof HtmlTableRenderer;
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
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit(org
     * .goobi.production.flow.statistics.enums.TimeUnit)
     */
    @Override
    public void setTimeUnit(TimeUnit timeUnit) {
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
