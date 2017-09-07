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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.goobi.production.flow.statistics.IStatisticalQuestion;
import org.goobi.production.flow.statistics.enums.CalculationUnit;
import org.goobi.production.flow.statistics.enums.StatisticsMode;
import org.goobi.production.flow.statistics.enums.TimeUnit;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.BaseDTO;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.dto.TaskDTO;
import org.kitodo.services.ServiceManager;

/**
 * Implementation of {@link IStatisticalQuestion}. Statistical Request with
 * predefined Values in data Table
 * 
 * @author Steffen Hankiewicz
 */
public class StatQuestVolumeStatus implements IStatisticalQuestion {

    private final ServiceManager serviceManager = new ServiceManager();
    private static final Logger logger = LogManager.getLogger(StatQuestVolumeStatus.class);

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestion#getDataTables(
     * List)
     */
    @Override
    public List<DataTable> getDataTables(List<? extends BaseDTO> dataSource) {
        BoolQueryBuilder taskQuery = new BoolQueryBuilder();

        Set<Integer> processingStatus = new HashSet<>();
        processingStatus.add(1);
        processingStatus.add(2);

        taskQuery.should(serviceManager.getTaskService().getQueryForProcessingStatus(processingStatus));
        taskQuery.should(serviceManager.getTaskService().getQueryProcessIds(getIds(dataSource)));

        List<TaskDTO> taskDTOS = new ArrayList<>();
        try {
            taskDTOS = serviceManager.getTaskService().findByQuery(taskQuery, true);
        } catch (DataException e) {
            logger.error(e);
        }

        String title = StatisticsMode.getByClassName(this.getClass()).getTitle();

        DataTable dtbl = new DataTable(title);
        dtbl.setShowableInPieChart(true);
        DataRow dRow = new DataRow(Helper.getTranslation("count"));

        for (TaskDTO taskDTO : taskDTOS) {
            String shortTitle = (taskDTO.getTitle().length() > 60 ? taskDTO.getTitle().substring(0, 60) + "..."
                    : taskDTO.getTitle());
            dRow.addValue(shortTitle, dRow.getValue(shortTitle) + 1);
        }

        dtbl.addDataRow(dRow);
        List<DataTable> allTables = new ArrayList<>();

        dtbl.setUnitLabel(Helper.getTranslation("arbeitsschritt"));
        allTables.add(dtbl);
        return allTables;
    }

    @SuppressWarnings("unchecked")
    private Set<Integer> getIds(List<? extends BaseDTO> dataSource) {
        Set<Integer> ids = new HashSet<>();
        for (ProcessDTO process : (List<ProcessDTO>) dataSource) {
            ids.add(process.getId());
        }
        return ids;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.goobi.production.flow.statistics.IStatisticalQuestion#
     * isRendererInverted( de.intranda.commons.chart.renderer.IRenderer)
     */
    @Override
    public Boolean isRendererInverted(IRenderer inRenderer) {
        return inRenderer instanceof HtmlTableRenderer;
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
     * @see
     * org.goobi.production.flow.statistics.IStatisticalQuestion#setTimeUnit(
     * org.goobi.production.flow.statistics.enums.TimeUnit)
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
