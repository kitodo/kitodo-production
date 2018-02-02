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

package org.goobi.production.flow.statistics;

import de.intranda.commons.chart.renderer.CSVRenderer;
import de.intranda.commons.chart.renderer.ChartRenderer;
import de.intranda.commons.chart.renderer.ExcelRenderer;
import de.intranda.commons.chart.renderer.HtmlTableRenderer;
import de.intranda.commons.chart.renderer.IRenderer;
import de.intranda.commons.chart.renderer.PieChartRenderer;
import de.intranda.commons.chart.results.DataTable;
import de.sub.goobi.config.ConfigCore;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatisticsRenderingElement implements Serializable {

    private static final long serialVersionUID = 9211752003070422596L;
    private IStatisticalQuestion myQuestion;
    private DataTable dataTable;
    private HtmlTableRenderer htmlTableRenderer;
    private CSVRenderer csvRenderer;
    private ExcelRenderer excelRenderer;
    private URI localImagePath;
    private String imageUrl;
    private static final Logger logger = LogManager.getLogger(StatisticsRenderingElement.class);

    public StatisticsRenderingElement(DataTable inDataTable, IStatisticalQuestion inQuestion) {
        dataTable = inDataTable;
        myQuestion = inQuestion;
    }

    /**
     * Create render.
     *
     * @param inShowAverage
     *            Boolean
     */
    public void createRenderer(Boolean inShowAverage) {
        /*
         * create image path
         */
        localImagePath = ConfigCore.getTempImagesPathAsCompleteDirectory();

        /* create html renderer */
        createHtmlRenderer();
        /* create chart */
        createChart(inShowAverage);
    }

    /**
     * generate html presentation of datatable.
     */
    private void createHtmlRenderer() {
        htmlTableRenderer = new HtmlTableRenderer();
        csvRenderer = new CSVRenderer();
        excelRenderer = new ExcelRenderer();
        if (myQuestion.isRendererInverted(htmlTableRenderer)) {
            htmlTableRenderer.setDataTable(dataTable.getDataTableInverted());
            csvRenderer.setDataTable(dataTable.getDataTableInverted());
            excelRenderer.setDataTable(dataTable.getDataTableInverted());
        } else {
            htmlTableRenderer.setDataTable(dataTable);
            csvRenderer.setDataTable(dataTable);
            excelRenderer.setDataTable(dataTable);
        }
        htmlTableRenderer.setFormatPattern(myQuestion.getNumberFormatPattern());
        csvRenderer.setFormatPattern(myQuestion.getNumberFormatPattern());
        excelRenderer.setFormatPattern(myQuestion.getNumberFormatPattern());
    }

    /**
     * generate chart at defined folder of datatable.
     */
    private void createChart(Boolean showAverage) {
        imageUrl = System.currentTimeMillis() + ".png";

        IRenderer renderer;
        if (dataTable.isShowableInPieChart()) {
            renderer = new PieChartRenderer();
        } else {
            ChartRenderer chartRenderer = new ChartRenderer();
            chartRenderer.setShowMeanValues(showAverage);
            renderer = chartRenderer;
        }
        if (myQuestion.isRendererInverted(renderer)) {
            renderer.setDataTable(dataTable.getDataTableInverted());
        } else {
            renderer.setDataTable(dataTable);
        }
        File outputFile = new File(localImagePath.getPath() + "/" + imageUrl);
        BufferedImage image = (BufferedImage) renderer.getRendering();
        try {
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    /**
     * Getter for dataTable.
     *
     * @return the dataTable
     */
    public DataTable getDataTable() {
        return dataTable;
    }

    /**
     * Getter for htmlTableRenderer.
     *
     * @return the htmlTableRenderer
     */
    public HtmlTableRenderer getHtmlTableRenderer() {
        return htmlTableRenderer;
    }

    /**
     * Getter for title.
     *
     * @return the title
     */
    public String getTitle() {
        return dataTable.getName() + " " + dataTable.getSubname();
    }

    /**
     * Getter for imageUrl.
     *
     * @return the imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    public CSVRenderer getCsvRenderer() {
        return csvRenderer;
    }

    public ExcelRenderer getExcelRenderer() {
        return excelRenderer;
    }

}
