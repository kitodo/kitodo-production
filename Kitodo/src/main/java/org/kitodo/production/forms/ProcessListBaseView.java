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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.api.dataformat.PhysicalDivision;
import org.kitodo.api.dataformat.Workpiece;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.data.database.beans.Process;
import org.kitodo.export.ExportDms;
import org.kitodo.production.enums.ChartMode;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.WebDav;
import org.kitodo.production.model.LazyProcessDTOModel;
import org.kitodo.production.process.ProcessMetadataStatistic;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataformat.MetsService;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.pie.PieChartModel;

public class ProcessListBaseView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(ProcessListBaseView.class);
    private ChartMode chartMode;
    private HorizontalBarChartModel stackedBarModel;
    private PieChartModel pieModel;
    private Map<String,Integer> statisticResult;
    private List<ProcessMetadataStatistic> processMetadataStatistics = new ArrayList<>();
    private int numberOfGlobalImages;
    private int numberOfGlobalStructuralElements;
    private int numberOfGlobalMetadata;
    List<? extends Object> selectedProcesses = new ArrayList<>();
    private final String doneDirectoryName = ConfigCore.getParameterOrDefaultValue(ParameterCore.DONE_DIRECTORY_NAME);
    DeleteProcessDialog deleteProcessDialog = new DeleteProcessDialog();

    private final HashMap<Integer, Boolean> exportable = new HashMap<>();

    boolean allSelected = false;
    HashSet<Integer> excludedProcessIds = new HashSet<>();

    /**
     * Constructor.
     */
    public ProcessListBaseView() {
        super();
        super.setLazyDTOModel(new LazyProcessDTOModel(ServiceManager.getProcessService()));
    }

    /**
     * Gets excludedProcessIds.
     *
     * @return value of excludedProcessIds
     */
    public HashSet<Integer> getExcludedProcessIds() {
        return excludedProcessIds;
    }

    /**
     * Sets excludedProcessIds.
     *
     * @param excludedProcessIds value of excludedProcessIds
     */
    public void setExcludedProcessIds(HashSet<Integer> excludedProcessIds) {
        this.excludedProcessIds = excludedProcessIds;
    }

    /**
     * Gets allSelected.
     *
     * @return value of allSelected
     */
    public boolean isAllSelected() {
        return allSelected;
    }

    /**
     * Sets allSelected.
     *
     * @param allSelected value of allSelected
     */
    public void setAllSelected(boolean allSelected) {
        this.allSelected = allSelected;
        excludedProcessIds.clear();
    }

    /**
     * Returns the list of the processes currently selected in the user interface.
     * Converts ProcessInterface instances to Process instances in case of displaying search results.
     *
     * @return value of selectedProcesses
     */
    @SuppressWarnings("unchecked")
    public List<Process> getSelectedProcesses() {
        List<Process> result = new ArrayList<>();
        ProcessService processService = ServiceManager.getProcessService();
        if (allSelected) {
            try {
                this.selectedProcesses = processService.findSelectedProcesses(
                    this.isShowClosedProcesses(), isShowInactiveProjects(), getFilter(),
                    new ArrayList<>(excludedProcessIds));
            } catch (DataException e) {
                logger.error(e.getMessage());
            }
        }
        if (!selectedProcesses.isEmpty()) {
            if (selectedProcesses.get(0) instanceof ProcessInterface) {
                // list contains ProcessInterface instances
                try {
                    result = ServiceManager.getProcessService()
                            .convertDtosToBeans((List<ProcessInterface>) selectedProcesses);
                } catch (DAOException e) {
                    Helper.setErrorMessage(ERROR_LOADING_MANY,
                            new Object[]{ObjectType.PROCESS.getTranslationPlural()}, logger, e);
                }
            } else if (selectedProcesses.get(0) instanceof Process) {
                // list contains Process instances
                result = (List<Process>) selectedProcesses;
            }
        }
        return result;
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
        stackedBarModel = ServiceManager.getProcessService().getBarChartModel(getSelectedProcesses());
        PrimeFaces.current().executeScript("PF('statisticsDialog').show();");
        PrimeFaces.current().ajax().update("statisticsDialog");
    }

    /**
     * Shows the state of volumes from the selected processes.
     */
    public void showStateOfVolume() {
        chartMode = ChartMode.PIE;
        statisticResult = ServiceManager.getProcessService().getProcessTaskStates(getSelectedProcesses());
        pieModel = ServiceManager.getProcessService().getPieChardModel(statisticResult);
        PrimeFaces.current().executeScript("PF('statisticsDialog').show();");
        PrimeFaces.current().ajax().update("statisticsDialog");
    }

    /**
     * Shows the number of images, metadata and structuralElements.
     */
    public void showProcessMetadataStatistic() {
        chartMode = ChartMode.METADATA_STATISTIC;
        processMetadataStatistics = new ArrayList<>();
        resetGlobalStatisticValues();
        Workpiece workpiece;
        for (Process selectedProcess : getSelectedProcesses()) {
            try {
                URI metadataFilePath = ServiceManager.getFileService().getMetadataFilePath(selectedProcess);
                workpiece = ServiceManager.getMetsService().loadWorkpiece(metadataFilePath);
            } catch (IOException e) {
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
     * Return whether to display metadata statistics or not.
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
        return ((LazyProcessDTOModel)this.lazyDTOModel).isShowClosedProcesses();
    }

    /**
     * Set whether closed processes should be displayed or not.
     *
     * @param showClosedProcesses
     *            boolean flag signaling whether closed processes should be
     *            displayed or not
     */
    public void setShowClosedProcesses(boolean showClosedProcesses) {
        ((LazyProcessDTOModel)this.lazyDTOModel).setShowClosedProcesses(showClosedProcesses);
    }

    /**
     * Set whether inactive projects should be displayed or not.
     *
     * @param showInactiveProjects
     *            boolean flag signaling whether inactive projects should be
     *            displayed or not
     */
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        ((LazyProcessDTOModel)this.lazyDTOModel).setShowInactiveProjects(showInactiveProjects);
    }

    /**
     * Return whether inactive projects should be displayed or not.
     *
     * @return parameter controlling whether inactive projects should be displayed
     *         or not
     */
    public boolean isShowInactiveProjects() {
        return ((LazyProcessDTOModel)this.lazyDTOModel).isShowInactiveProjects();
    }

    /**
     * Export DMS for selected processes.
     */
    public void exportDMSForSelection() {
        exportDMSForProcesses(getSelectedProcesses());
    }

    /**
     * Generate result set.
     */
    public void generateResult() {
        try {
            ServiceManager.getProcessService().generateResult(this.filter, this.isShowClosedProcesses(),
                    this.isShowInactiveProjects());
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_CREATING, new Object[] {Helper.getTranslation("resultSet") }, logger, e);
        }
    }

    /**
     * Generate result as PDF.
     */
    public void generateResultAsPdf() {
        try {
            ServiceManager.getProcessService().generateResultAsPdf(this.filter, this.isShowClosedProcesses(),
                    this.isShowInactiveProjects());
        } catch (IOException | DocumentException e) {
            Helper.setErrorMessage(ERROR_CREATING, new Object[] {Helper.getTranslation("resultPDF") }, logger, e);
        }
    }

    /**
     * Download to home for selected processes.
     */
    public void downloadToHomeForSelection() {
        try {
            ProcessService.downloadToHome(this.getSelectedProcesses());
            Helper.setMessage("createdInUserHomeAll");
        } catch (DAOException e) {
            Helper.setErrorMessage("Error downloading processes to home directory!");
        }
    }

    /**
     * Upload selected processes from home.
     */
    public void uploadFromHomeForSelection() {
        ProcessService.uploadFromHome(this.getSelectedProcesses());
        Helper.setMessage("directoryRemovedSelected");
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
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_EXPORTING,
                        new Object[] {ObjectType.PROCESS.getTranslationSingular(), processToExport.getId() }, logger, e);
            }
        }
    }

    /**
     * If processes are generated with calendar.
     *
     * @param process
     *            the process dto to check.
     * @return true if processes are created with calendar, false otherwise
     */
    public boolean createProcessesWithCalendar(ProcessInterface process) {
        try {
            return ProcessService.canCreateProcessWithCalendar(process);
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
        }
        return false;
    }

    /**
     * If a process can be created as child.
     *
     * @param process
     *            the process dto to check.
     * @return true if processes can be created as child, false otherwise
     */
    public boolean createProcessAsChildPossible(ProcessInterface process) {
        try {
            return ProcessService.canCreateChildProcess(process);
        } catch (IOException | DAOException e) {
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
    public void createXML(ProcessInterface process) {
        try {
            ProcessService.createXML(ServiceManager.getProcessService().getById(process.getId()), getUser());
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
    public void uploadFromHome(ProcessInterface process) {
        try {
            WebDav myDav = new WebDav();
            myDav.uploadFromHome(ServiceManager.getProcessService().getById(process.getId()));
            Helper.setMessage("directoryRemoved", process.getTitle());
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] {ObjectType.PROCESS.getTranslationSingular(), process.getId() }, logger, e);
        }
    }

    /**
     * Delete Process.
     *
     * @param process
     *            process to delete.
     */
    public void delete(ProcessInterface process) {
        try {
            Process processBean = ServiceManager.getProcessService().getById(process.getId());
            if (processBean.getChildren().isEmpty()) {
                try {
                    ProcessService.deleteProcess(processBean.getId());
                } catch (DataException | IOException e) {
                    Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROCESS.getTranslationSingular() },
                            logger, e);
                }
            } else {
                this.deleteProcessDialog = new DeleteProcessDialog();
                this.deleteProcessDialog.setProcess(processBean);
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

    /**
     * Check and return whether process with given ID can be exported or not.
     *
     * @param processId process ID
     * @return whether process with given ID can be exported or not
     */
    public boolean canBeExported(int processId) {
        try {
            if (!exportable.containsKey(processId)) {
                exportable.put(processId, ProcessService.canBeExported(processId));
            }
            return exportable.get(processId);
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(e);
            return false;
        }
    }

    /**
     * Returns the list of currently selected processes. This list is used both when displaying search results 
     * and when displaying the process list, which is why it may contain either instances of Process or 
     * instances of ProcessInterface.
     * 
     * @return list of instances of Process or ProcessInterface
     */
    public List<? extends Object> getSelectedProcessesOrProcessDTOs() {
        return selectedProcesses;
    }

    public void setSelectedProcessesOrProcessDTOs(List<? extends Object> selectedProcesses) {
        this.selectedProcesses = selectedProcesses;
    }

    /**
     * Update selection and first row to show in datatable on PageEvent.
     * @param pageEvent PageEvent triggered by data tables paginator
     */
    @Override
    public void onPageChange(PageEvent pageEvent) {
        this.setFirstRow(((DataTable) pageEvent.getSource()).getFirst());
        if (allSelected) {
            PrimeFaces.current()
                    .executeScript("PF('processesTable').selectAllRows();");
            excludedProcessIds.forEach(processId -> PrimeFaces.current()
                    .executeScript("PF('processesTable').unselectRow($('tr[data-rk=\"" + processId + "\"]'), true);"));
        }
    }

    /**
     * Returns the number of global images of the process list base view.
     * 
     * @return the number of global images
     */
    public int getNumberOfGlobalImages() {
        return numberOfGlobalImages;
    }

    /**
     * Returns the number of global structural elements of the process list base
     * view.
     * 
     * @return the number of global structural elements
     */
    public int getNumberOfGlobalStructuralElements() {
        return numberOfGlobalStructuralElements;
    }

    /**
     * Returns the number of global metadata of the process list base view.
     * 
     * @return the number of global metadata
     */
    public int getNumberOfGlobalMetadata() {
        return numberOfGlobalMetadata;
    }

    /**
     * Returns the number of global process metadata statistics of the process
     * list base view.
     * 
     * @return the number of global process metadata statistics
     */
    public int getNumberOfGlobalProcessMetadataStatistics() {
        return processMetadataStatistics.size();
    }
}
