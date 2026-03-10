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

package org.kitodo.production.forms.process;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.data.database.beans.Process;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.production.enums.ChartMode;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.process.ProcessMetadataStatistic;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.utils.Stopwatch;
import org.primefaces.PrimeFaces;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.pie.PieChartModel;
import org.xml.sax.SAXException;

@Named("ProcessListViewStatisticsDialog")
@ViewScoped
public class ProcessListViewStatisticsDialog extends BaseForm {

    private static final Logger logger = LogManager.getLogger(ProcessListViewStatisticsDialog.class);
    
    private ChartMode chartMode;
    private PieChartModel pieModel;
    private HorizontalBarChartModel stackedBarModel;
    private Map<String,Integer> statisticResult;
    private List<ProcessMetadataStatistic> processMetadataStatistics = new ArrayList<>();
    private int numberOfGlobalImages;
    private int numberOfGlobalStructuralElements;
    private int numberOfGlobalMetadata;

    /**
     * Return whether to display bar model or not.
     *
     * @return whether to display bar model or not
     */
    public boolean showBarModel() {
        Stopwatch stopwatch = new Stopwatch(this, "showBarModel");
        return stopwatch.stop(ChartMode.BAR.equals(chartMode));
    }

    /**
     * Return whether to display pie model or not.
     *
     * @return whether to display pie model or not
     */
    public boolean showPieModel() {
        Stopwatch stopwatch = new Stopwatch(this, "showPieModel");
        return stopwatch.stop(ChartMode.PIE.equals(chartMode));
    }

    /**
     * Return whether to display metadata statistics or not.
     *
     * @return whether to display metadata statistics or not
     */
    public boolean showProcessMetadataStatisticTable() {
        Stopwatch stopwatch = new Stopwatch(this, "showProcessMetadataStatisticTable");
        return stopwatch.stop(ChartMode.METADATA_STATISTIC.equals(chartMode));
    }

    /**
     * Get statistic result.
     *
     * @return statistic result
     */
    public Map<String, Integer> getStatisticResult() {
        Stopwatch stopwatch = new Stopwatch(this, "getStatisticResult");
        return stopwatch.stop(statisticResult);
    }

    /**
     * Get pie model.
     *
     * @return pie model
     */
    public PieChartModel getPieModel() {
        Stopwatch stopwatch = new Stopwatch(this, "getPieModel");
        return stopwatch.stop(pieModel);
    }

    /**
     * Set pie model.
     *
     * @param pieModel as org.primefaces.model.charts.piePieChardModel
     */
    public void setPieModel(PieChartModel pieModel) {
        Stopwatch stopwatch = new Stopwatch(this, "setPieModel", "pieModel", Objects.toString(pieModel));
        this.pieModel = pieModel;
        stopwatch.stop();
    }

    /**
     * Get process metadata statistics.
     *
     * @return process metadata statistics
     */
    public List<ProcessMetadataStatistic> getProcessMetadataStatistics() {
        Stopwatch stopwatch = new Stopwatch(this, "getProcessMetadataStatistics");
        return stopwatch.stop(processMetadataStatistics);
    }

    /**
     * Get relative image amount.
     *
     * @param numberOfImages number of images
     * @return relative image amount
     */
    public int getRelativeImageAmount(int numberOfImages) {
        Stopwatch stopwatch = new Stopwatch(this, "getRelativeImageAmount", "numberOfImages", Integer.toString(
            numberOfImages));
        return stopwatch.stop(numberOfImages == 0 ? 0 : numberOfImages * 100 / this.numberOfGlobalImages);
    }

    /**
     * Get relative structural element amount.
     *
     * @param numberOfStructuralElements number of structural elements
     * @return relative structural element amount
     */
    public int getRelativeStructuralElementAmount(int numberOfStructuralElements) {
        Stopwatch stopwatch = new Stopwatch(this, "getRelativeStructuralElementAmount", "numberOfStructuralElements",
                Integer.toString(numberOfStructuralElements));
        return stopwatch.stop(numberOfStructuralElements == 0 ? 0
                : numberOfStructuralElements * 100 / this.numberOfGlobalStructuralElements);
    }

    /**
     * Get stackedBarModel.
     *
     * @return value of stackedBarModel
     */
    public HorizontalBarChartModel getStackedBarModel() {
        Stopwatch stopwatch = new Stopwatch(this, "getStackedBarModel");
        return stopwatch.stop(stackedBarModel);
    }

    /**
     * Get relative metadata amount.
     *
     * @param numberOfMetadata number of metadata
     * @return relative metadata amount
     */
    public int getRelativeMetadataAmount(int numberOfMetadata) {
        Stopwatch stopwatch = new Stopwatch(this, "getRelativeMetadataAmount", "numberOfMetadata", Integer.toString(
            numberOfMetadata));
        return stopwatch.stop(numberOfMetadata == 0 ? 0 : numberOfMetadata * 100 / this.numberOfGlobalMetadata);
    }

    /**
     * Returns the number of global images of the process list base view.
     * 
     * @return the number of global images
     */
    public int getNumberOfGlobalImages() {
        Stopwatch stopwatch = new Stopwatch(this, "getNumberOfGlobalImages");
        return stopwatch.stop(numberOfGlobalImages);
    }

    /**
     * Returns the number of global structural elements of the process list base
     * view.
     * 
     * @return the number of global structural elements
     */
    public int getNumberOfGlobalStructuralElements() {
        Stopwatch stopwatch = new Stopwatch(this, "getNumberOfGlobalStructuralElements");
        return stopwatch.stop(numberOfGlobalStructuralElements);
    }

    /**
     * Returns the number of global metadata of the process list base view.
     * 
     * @return the number of global metadata
     */
    public int getNumberOfGlobalMetadata() {
        Stopwatch stopwatch = new Stopwatch(this, "getNumberOfGlobalMetadata");
        return stopwatch.stop(numberOfGlobalMetadata);
    }

    /**
     * Returns the number of global process metadata statistics of the process
     * list base view.
     * 
     * @return the number of global process metadata statistics
     */
    public int getNumberOfGlobalProcessMetadataStatistics() {
        Stopwatch stopwatch = new Stopwatch(this, "getNumberOfGlobalProcessMetadataStatistics");
        return stopwatch.stop(processMetadataStatistics.size());
    }

    /**
     * Shows the state of volumes from the selected processes.
     */
    public void showDurationOfTasks(List<Process> selectedProcesses) {
        final Stopwatch stopwatch = new Stopwatch(this, "showDurationOfTasks");
        chartMode = ChartMode.BAR;
        stackedBarModel = ServiceManager.getProcessService().getBarChartModel(selectedProcesses);
        PrimeFaces.current().executeScript("PF('statisticsDialog').show();");
        PrimeFaces.current().ajax().update("statisticsDialog");
        stopwatch.stop();
    }

    /**
     * Shows the state of volumes from the selected processes.
     */
    public void showStateOfVolume(List<Process> selectedProcesses) {
        final Stopwatch stopwatch = new Stopwatch(this, "showStateOfVolume");
        chartMode = ChartMode.PIE;
        statisticResult = ServiceManager.getProcessService().getProcessTaskStates(selectedProcesses);
        pieModel = ServiceManager.getProcessService().getPieChardModel(statisticResult);
        PrimeFaces.current().executeScript("PF('statisticsDialog').show();");
        PrimeFaces.current().ajax().update("statisticsDialog");
        stopwatch.stop();
    }

    /**
     * Shows the number of images, metadata and structuralElements.
     */
    public void showProcessMetadataStatistic(List<Process> selectedProcesses) {
        final Stopwatch stopwatch = new Stopwatch(this, "showProcessMetadataStatistic");
        chartMode = ChartMode.METADATA_STATISTIC;
        processMetadataStatistics = new ArrayList<>();
        resetGlobalStatisticValues();
        Workpiece workpiece;
        for (Process selectedProcess : selectedProcesses) {
            try {
                URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(selectedProcess);
                workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
            } catch (IOException | SAXException | FileStructureValidationException e) {
                Helper.setErrorMessage(ERROR_LOADING_ONE,
                        new Object[] {ObjectType.PROCESS.getTranslationSingular(), selectedProcess.getId() }, logger, e);
                return;
            }
            int numberOfProcessImages = (int) Workpiece.treeStream(workpiece.getPhysicalStructure())
                    .filter(physicalDivision -> Objects.equals(physicalDivision.getType(), PhysicalDivision.TYPE_PAGE)).count();
            this.numberOfGlobalImages += numberOfProcessImages;
            int numberOfProcessStructuralElements = (int) Workpiece.treeStream(workpiece.getLogicalStructure()).count();
            this.numberOfGlobalStructuralElements += numberOfProcessStructuralElements;
            int numberOfProcessMetadata = Math
                    .toIntExact(MetsService.countLogicalMetadata(workpiece));
            this.numberOfGlobalMetadata += numberOfProcessMetadata;

            processMetadataStatistics.add(new ProcessMetadataStatistic(selectedProcess.getTitle(),
                    numberOfProcessImages, numberOfProcessStructuralElements, numberOfProcessMetadata));
        }
        PrimeFaces.current().executeScript("PF('statisticsDialog').show();");
        PrimeFaces.current().ajax().update("statisticsDialog");
        stopwatch.stop();
    }

    private void resetGlobalStatisticValues() {
        this.numberOfGlobalStructuralElements = 0;
        this.numberOfGlobalImages = 0;
        this.numberOfGlobalMetadata = 0;
    }

}
