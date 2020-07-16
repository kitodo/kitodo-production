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

package org.kitodo.production.forms;

import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.exceptions.RulesetNotFoundException;
import org.kitodo.export.ExportDms;
import org.kitodo.production.dto.ProcessDTO;
import org.kitodo.production.enums.ChartMode;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.WebDav;
import org.kitodo.production.process.ProcessMetadataStatistic;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataformat.MetsService;
import org.primefaces.PrimeFaces;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.pie.PieChartModel;

public class ProcessListBaseView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(ProcessListBaseView.class);
    private ChartMode chartMode;
    private HorizontalBarChartModel stackedBarModel;
    private PieChartModel pieModel;
    private Map<String,Integer> statisticResult;
    private final List<ProcessMetadataStatistic> processMetadataStatistics = new ArrayList<>();
    private int numberOfGlobalImages;
    private int numberOfGlobalStructuralElements;
    private int numberOfGlobalMetadata;
    List<Process> selectedProcesses = new ArrayList<>();
    private boolean showClosedProcesses = false;
    private final String doneDirectoryName = ConfigCore.getParameterOrDefaultValue(ParameterCore.DONE_DIRECTORY_NAME);
    DeleteProcessDialog deleteProcessDialog = new DeleteProcessDialog();

    /**
     * Get selectedProcesses.
     *
     * @return value of selectedProcesses
     */
    public List<Process> getSelectedProcesses() {
        return selectedProcesses;
    }

    /**
     * Get stackedBarModel.
     *
     * @return value of stackedBarModel
     */
    public HorizontalBarChartModel getStackedBarModel() {
        return stackedBarModel;
    }

    /**
     * Shows the state of volumes from the selected processes.
     */
    public void showDurationOfTasks() {
        chartMode = ChartMode.BAR;
        stackedBarModel = ServiceManager.getProcessService().getBarChartModel(selectedProcesses);
    }

    /**
     * Shows the state of volumes from the selected processes.
     */
    public void showStateOfVolume() {
        chartMode = ChartMode.PIE;
        statisticResult = ServiceManager.getProcessService().getProcessTaskStates(selectedProcesses);
        pieModel = ServiceManager.getProcessService().getPieChardModel(statisticResult);
    }

    /**
     * Shows the number of images, metadata and structuralElements.
     */
    public void showProcessMetadataStatistic() {
        chartMode = ChartMode.METADATA_STATISTIC;
        resetGlobalStatisticValues();
        Workpiece workpiece;
        for (Process selectedProcess : selectedProcesses) {
            try {
                URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(selectedProcess);
                workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
            } catch (IOException e) {
                Helper.setErrorMessage(ERROR_LOADING_ONE,
                        new Object[] {ObjectType.PROCESS.getTranslationSingular(), selectedProcess.getId() }, logger, e);
                return;
            }
            int numberOfProcessImages = workpiece.getAllMediaUnitsSorted().size();
            this.numberOfGlobalImages += numberOfProcessImages;
            int numberOfProcessStructuralElements = workpiece.getAllIncludedStructuralElements().size();
            this.numberOfGlobalStructuralElements += numberOfProcessStructuralElements;
            int numberOfProcessMetadata = Math
                    .toIntExact(MetsService.countLogicalMetadata(workpiece));
            this.numberOfGlobalMetadata += numberOfProcessMetadata;

            processMetadataStatistics.add(new ProcessMetadataStatistic(selectedProcess.getTitle(),
                    numberOfProcessImages, numberOfProcessStructuralElements, numberOfProcessMetadata));
        }
    }

    /**
     * Return whether to display bar model or not.
     *
     * @return whether to display bar model or not
     */
    public boolean showBarModel() {
        return ChartMode.BAR.equals(chartMode);
    }

    /**
     * Return whether to display pie model or not.
     *
     * @return whether to display pie model or not
     */
    public boolean showPieModel() {
        return ChartMode.PIE.equals(chartMode);
    }

    /**
     * Return whether to display metadata statistics or not
     *
     * @return whether to display metadata statistics or not
     */
    public boolean showProcessMetadataStatisticTable() {
        return ChartMode.METADATA_STATISTIC.equals(chartMode);
    }

    /**
     * Get statistic result.
     *
     * @return statistic result
     */
    public Map<String, Integer> getStatisticResult() {
        return statisticResult;
    }

    /**
     * Get pie model.
     *
     * @return pie model
     */
    public PieChartModel getPieModel() {
        return pieModel;
    }

    /**
     * Set pie model.
     *
     * @param pieModel as org.primefaces.model.charts.piePieChardModel
     */
    public void setPieModel(PieChartModel pieModel) {
        this.pieModel = pieModel;
    }

    /**
     * Get process metadata statistics.
     *
     * @return process metadata statistics
     */
    public List<ProcessMetadataStatistic> getProcessMetadataStatistics() {
        return processMetadataStatistics;
    }

    /**
     * Get relative image amount.
     *
     * @param numberOfImages number of images
     * @return relative image amount
     */
    public int getRelativeImageAmount(int numberOfImages) {
        return numberOfImages == 0 ? 0 : numberOfImages * 100 / this.numberOfGlobalImages;
    }

    /**
     * Get relative structural element amount.
     *
     * @param numberOfStructuralElements number of structural elements
     * @return relative structural element amount
     */
    public int getRelativeStructuralElementAmount(int numberOfStructuralElements) {
        return numberOfStructuralElements == 0 ? 0
                : numberOfStructuralElements * 100 / this.numberOfGlobalStructuralElements;
    }

    /**
     * Get relative metadata amount.
     *
     * @param numberOfMetadata number of metadata
     * @return relative metadata amount
     */
    public int getRelativeMetadataAmount(int numberOfMetadata) {
        return numberOfMetadata == 0 ? 0 : numberOfMetadata * 100 / this.numberOfGlobalMetadata;
    }

    /**
     * Return whether closed processes should be displayed or not.
     *
     * @return parameter controlling whether closed processes should be displayed or
     *         not
     */
    public boolean isShowClosedProcesses() {
        return this.showClosedProcesses;
    }

    /**
     * Set whether closed processes should be displayed or not.
     *
     * @param showClosedProcesses
     *            boolean flag signaling whether closed processes should be
     *            displayed or not
     */
    public void setShowClosedProcesses(boolean showClosedProcesses) {
        this.showClosedProcesses = showClosedProcesses;
        ServiceManager.getProcessService().setShowClosedProcesses(showClosedProcesses);
    }

    /**
     * Export DMS for selected processes.
     */
    public void exportDMSForSelection() {
        exportDMSForProcesses(this.selectedProcesses);
    }

    /**
     * Generate result set.
     */
    public void generateResult() {
        try {
            ServiceManager.getProcessService().generateResult(this.filter);
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_CREATING, new Object[] {Helper.getTranslation("resultSet") }, logger, e);
        }
    }

    /**
     * Generate result as PDF.
     */
    public void generateResultAsPdf() {
        try {
            ServiceManager.getProcessService().generateResultAsPdf(this.filter);
        } catch (IOException | DocumentException e) {
            Helper.setErrorMessage(ERROR_CREATING, new Object[] {Helper.getTranslation("resultPDF") }, logger, e);
        }
    }

    /**
     * Upload all processes from home.
     */
    public void uploadFromHomeForAll() {
        WebDav myDav = new WebDav();
        List<URI> folder = myDav.uploadAllFromHome(doneDirectoryName);
        myDav.removeAllFromHome(folder, URI.create(doneDirectoryName));
        Helper.setMessage("directoryRemovedAll", doneDirectoryName);
    }

    void resetGlobalStatisticValues() {
        this.numberOfGlobalStructuralElements = 0;
        this.numberOfGlobalImages = 0;
        this.numberOfGlobalMetadata = 0;
    }

    private void exportDMSForProcesses(List<Process> processes) {
        ExportDms export = new ExportDms();
        for (Process processToExport : processes) {
            try {
                export.startExport(processToExport);
                Helper.setMessage(EXPORT_FINISHED);
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_EXPORTING,
                        new Object[] {ObjectType.PROCESS.getTranslationSingular(), processToExport.getId() }, logger, e);
            }
        }
    }

    /**
     * If processes are generated with calendar.
     *
     * @param processDTO
     *            the process dto to check.
     * @return true if processes are created with calendar, false otherwise
     */
    public boolean createProcessesWithCalendar(ProcessDTO processDTO) {
        try {
            return ProcessService.canCreateProcessWithCalendar(processDTO);
        } catch (IOException | DAOException | RulesetNotFoundException e) {
            Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
        }
        return false;
    }

    /**
     * If a process can be created as child.
     *
     * @param processDTO
     *            the process dto to check.
     * @return true if processes can be created as child, false otherwise
     */
    public boolean createProcessAsChildPossible(ProcessDTO processDTO) {
        try {
            return ProcessService.canCreateChildProcess(processDTO);
        } catch (IOException | DAOException | RulesetNotFoundException e) {
            Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
        }
        return false;
    }

    /**
     * Download to home for single process. First check if this volume is currently
     * being edited by another user and placed in his home directory, otherwise
     * download.
     */
    public void downloadToHome(int processId) {
        try {
            ProcessService.downloadToHome(new WebDav(), processId);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error downloading process " + processId + " to home directory!");
        }
    }

    /**
     * Starts generation of xml logfile for current process.
     */
    public void createXML(ProcessDTO processDTO) {
        try {
            ProcessService.createXML(ServiceManager.getProcessService().getById(processDTO.getId()), getUser());
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage("Error creating log file in home directory", logger, e);
        }
    }

    /**
     * Export METS.
     */
    public void exportMets(int processId) {
        try {
            ProcessService.exportMets(processId);
            Helper.setMessage(EXPORT_FINISHED);
        } catch (DAOException | DataException | IOException e) {
            Helper.setErrorMessage("An error occurred while trying to export METS file for process "
                    + processId, logger, e);
        }
    }

    /**
     * Export DMS.
     */
    public void exportDMS(int id) {
        ExportDms export = new ExportDms();
        try {
            export.startExport(ServiceManager.getProcessService().getById(id));
            Helper.setMessage(EXPORT_FINISHED);
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_EXPORTING,
                    new Object[] {ObjectType.PROCESS.getTranslationSingular(), id }, logger, e);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] {ObjectType.PROCESS.getTranslationSingular(), id }, logger, e);
        }
    }

    /**
     * Downloads a docket for process.
     */
    public void downloadDocket(int id) {
        try {
            ServiceManager.getProcessService().downloadDocket(ServiceManager.getProcessService().getById(id));
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Upload from home for single process.
     */
    public void uploadFromHome(ProcessDTO processDTO) {
        try {
            WebDav myDav = new WebDav();
            myDav.uploadFromHome(ServiceManager.getProcessService().getById(processDTO.getId()));
            Helper.setMessage("directoryRemoved", processDTO.getTitle());
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] {ObjectType.PROCESS.getTranslationSingular(), processDTO.getId() }, logger, e);
        }
    }

    /**
     * Delete Process.
     *
     * @param processDTO
     *            process to delete.
     */
    public void delete(ProcessDTO processDTO) {
        try {
            Process process = ServiceManager.getProcessService().getById(processDTO.getId());
            if (process.getChildren().isEmpty()) {
                try {
                    ProcessService.deleteProcess(process);
                } catch (DataException | IOException e) {
                    Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROCESS.getTranslationSingular() },
                            logger, e);
                }
            } else {
                this.deleteProcessDialog = new DeleteProcessDialog();
                this.deleteProcessDialog.setProcess(process);
                PrimeFaces.current().executeScript("PF('deleteChildrenDialog').show();");
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
        }
    }

    /**
     * Return delete process dialog.
     *
     * @return delete process dialog
     */
    public DeleteProcessDialog getDeleteProcessDialog() {
        return this.deleteProcessDialog;
    }
}
