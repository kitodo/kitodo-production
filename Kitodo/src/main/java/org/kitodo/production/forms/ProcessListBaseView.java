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
import java.util.EnumMap;
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
import org.kitodo.data.database.enums.TaskStatus;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.exceptions.FileStructureValidationException;
import org.kitodo.data.database.persistence.TaskDAO;
import org.kitodo.export.ExportDms;
import org.kitodo.production.enums.ChartMode;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.WebDav;
import org.kitodo.production.model.LazyProcessModel;
import org.kitodo.production.process.ProcessMetadataStatistic;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;
import org.kitodo.production.services.dataformat.MetsService;
import org.kitodo.utils.Stopwatch;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.charts.hbar.HorizontalBarChartModel;
import org.primefaces.model.charts.pie.PieChartModel;
import org.xml.sax.SAXException;

public class ProcessListBaseView extends ValidatableForm {

    private static final Logger logger = LogManager.getLogger(ProcessListBaseView.class);
    private ChartMode chartMode;
    private HorizontalBarChartModel stackedBarModel;
    private PieChartModel pieModel;
    private Map<String,Integer> statisticResult;
    private List<ProcessMetadataStatistic> processMetadataStatistics = new ArrayList<>();
    private int numberOfGlobalImages;
    private int numberOfGlobalStructuralElements;
    private int numberOfGlobalMetadata;
    List<Process> selectedProcesses = new ArrayList<>();
    private final String doneDirectoryName = ConfigCore.getParameterOrDefaultValue(ParameterCore.DONE_DIRECTORY_NAME);
    DeleteProcessDialog deleteProcessDialog = new DeleteProcessDialog();

    private final HashMap<Integer, Boolean> exportable = new HashMap<>();
    private static final String NL = "\n";

    boolean allSelected = false;
    HashSet<Integer> excludedProcessIds = new HashSet<>();


    /**
     * Constructor.
     */
    public ProcessListBaseView() {
        super();
        Stopwatch stopwatch = new Stopwatch(this, "ProcessListBaseView");
        super.setLazyBeanModel(new LazyProcessModel(ServiceManager.getProcessService()));
        stopwatch.stop();
    }

    /**
     * Gets excludedProcessIds.
     *
     * @return value of excludedProcessIds
     */
    public HashSet<Integer> getExcludedProcessIds() {
        Stopwatch stopwatch = new Stopwatch(this, "getExcludedProcessIds");
        return stopwatch.stop(excludedProcessIds);
    }

    /**
     * Sets excludedProcessIds.
     *
     * @param excludedProcessIds value of excludedProcessIds
     */
    public void setExcludedProcessIds(HashSet<Integer> excludedProcessIds) {
        Stopwatch stopwatch = new Stopwatch(this, "setExcludedProcessIds", "excludedProcessIds", Objects.toString(
            excludedProcessIds));
        this.excludedProcessIds = excludedProcessIds;
        stopwatch.stop();
    }

    /**
     * Gets allSelected.
     *
     * @return value of allSelected
     */
    public boolean isAllSelected() {
        Stopwatch stopwatch = new Stopwatch(this, "isAllSelected");
        return stopwatch.stop(allSelected);
    }

    /**
     * Sets allSelected.
     *
     * @param allSelected value of allSelected
     */
    public void setAllSelected(boolean allSelected) {
        Stopwatch stopwatch = new Stopwatch(this, "setAllSelected", "allSelected", Boolean.toString(allSelected));
        this.allSelected = allSelected;
        excludedProcessIds.clear();
        stopwatch.stop();
    }

    /**
     * Returns the list of the processes currently selected in the user interface.
     * Converts Process instances to Process instances in case of displaying search results.
     *
     * @return value of selectedProcesses
     */
    public List<Process> getSelectedProcesses() {
        Stopwatch stopwatch = new Stopwatch(this, "getSelectedProcesses");
        ProcessService processService = ServiceManager.getProcessService();
        if (allSelected) {
            try {
                this.selectedProcesses = processService.findSelectedProcesses(
                    this.isShowClosedProcesses(), isShowInactiveProjects(), getFilter(),
                    new ArrayList<>(excludedProcessIds));
            } catch (DAOException e) {
                logger.error(e.getMessage());
            }
        }
        return stopwatch.stop(selectedProcesses);
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
     * Shows the state of volumes from the selected processes.
     */
    public void showDurationOfTasks() {
        final Stopwatch stopwatch = new Stopwatch(this, "showDurationOfTasks");
        chartMode = ChartMode.BAR;
        stackedBarModel = ServiceManager.getProcessService().getBarChartModel(getSelectedProcesses());
        PrimeFaces.current().executeScript("PF('statisticsDialog').show();");
        PrimeFaces.current().ajax().update("statisticsDialog");
        stopwatch.stop();
    }

    private LazyProcessModel getLazyProcessModel() {
        return (LazyProcessModel) this.lazyBeanModel;
    }

    /**
     * Checks whether the given process has any tasks at all.
     *
     * @param process the process to check
     * @return true if at least one task exists, otherwise false
     */
    public boolean hasAnyTasks(Process process) {
        Map<TaskStatus, Integer> counts = getCachedTaskStatusCounts(process);
        return counts.values().stream().mapToInt(Integer::intValue).sum() > 0;
    }

    /**
     * Checks whether the given process has child processes.
     *
     * @param process the process to check
     * @return true if the process has children, otherwise false
     */
    public boolean hasChildren(Process process) {
        return getLazyProcessModel().getProcessesWithChildren().contains(process.getId());
    }

    /**
     * Returns the titles of open and in-work tasks for the given process.
     *
     * <p>For parent processes, no task titles are returned.</p>
     *
     * @param process the process to get task titles for
     * @return formatted task titles or an empty string if none exist
     */
    public String getCurrentTaskTitles(Process process) {
        Map<TaskStatus, List<String>> titles =
                getLazyProcessModel().getTaskTitleCache().get(process.getId());

        if (hasChildren(process)) {
            return null;
        }

        if (Objects.isNull(titles) || titles.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        appendTitles(sb, TaskStatus.OPEN, titles);
        appendTitles(sb, TaskStatus.INWORK, titles);
        return sb.toString();
    }

    /**
     * Appends task titles of the given status to the provided StringBuilder.
     *
     * @param sb the StringBuilder to append to
     * @param status the task status to append
     * @param titles task titles grouped by status
     */
    private void appendTitles(StringBuilder sb,
                              TaskStatus status,
                              Map<TaskStatus, List<String>> titles) {
        List<String> list = titles.get(status);
        if (Objects.isNull(list) || list.isEmpty()) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append(NL);
        }
        sb.append(Helper.getTranslation(status.getTitle())).append(":");
        for (String t : list) {
            sb.append(NL).append(" - ").append(Helper.getTranslation(t));
        }
    }

    /**
     * Returns cached task status counts for the given process.
     *
     * <p>If no cached data exists, a fallback database query is executed.</p>
     *
     * @param process the process to get task status counts for
     * @return a map of task status to count
     */
    public Map<TaskStatus, Integer> getCachedTaskStatusCounts(Process process) {
        LazyProcessModel model = getLazyProcessModel();
        EnumMap<TaskStatus, Integer> cached = model.getTaskStatusCounts(process);

        if (Objects.nonNull(cached)) {
            return cached;
        }
        // fallback (should rarely happen)
        try {
            return new TaskDAO().countTaskStatusForProcessAndItsAncestors(process);
        } catch (DAOException e) {
            logger.warn("Fallback task status counting failed", e);
            return Map.of();
        }
    }

    /**
     * Calculates the percentage of tasks with the given status.
     *
     * @param process the process to calculate progress for
     * @param status the task status to calculate
     * @return progress percentage for the given status
     */
    public double progress(Process process, TaskStatus status) {
        Map<TaskStatus, Integer> counts = getCachedTaskStatusCounts(process);
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        // keep legacy semantics
        if (total == 0) {
            return status == TaskStatus.LOCKED ? 100.0 : 0.0;
        }
        return 100.0 * counts.getOrDefault(status, 0) / total;
    }

    /**
     * Returns the percentage of tasks in the process that are completed. The
     * total of tasks awaiting preconditions, startable, in progress, and
     * completed is {@code 100.0d}.
     *
     * @param process the process
     * @return percentage of tasks completed
     */
    public double progressClosed(Process process) {
        return progress(process, TaskStatus.DONE);
    }

    /**
     * Returns the percentage of tasks in the process that are currently being
     * processed. The progress total of tasks waiting for preconditions,
     * startable, in progress, and completed is {@code 100.0d}.
     *
     * @param process the process
     * @return percentage of tasks in progress
     */
    public double progressInProcessing(Process process) {
        return progress(process, TaskStatus.INWORK);
    }

    /**
     * Returns the percentage of the process's tasks that are now ready to be
     * processed but have not yet been started. The progress total of tasks
     * waiting for preconditions, startable, in progress, and completed is
     * {@code 100.0d}.
     *
     * @param process the process
     * @return percentage of startable tasks
     */
    public double progressOpen(Process process) {
        return progress(process, TaskStatus.OPEN);
    }

    /**
     * Shows the state of volumes from the selected processes.
     */
    public void showStateOfVolume() {
        final Stopwatch stopwatch = new Stopwatch(this, "showStateOfVolume");
        chartMode = ChartMode.PIE;
        statisticResult = ServiceManager.getProcessService().getProcessTaskStates(getSelectedProcesses());
        pieModel = ServiceManager.getProcessService().getPieChardModel(statisticResult);
        PrimeFaces.current().executeScript("PF('statisticsDialog').show();");
        PrimeFaces.current().ajax().update("statisticsDialog");
        stopwatch.stop();
    }

    /**
     * Shows the number of images, metadata and structuralElements.
     */
    public void showProcessMetadataStatistic() {
        final Stopwatch stopwatch = new Stopwatch(this, "showProcessMetadataStatistic");
        chartMode = ChartMode.METADATA_STATISTIC;
        processMetadataStatistics = new ArrayList<>();
        resetGlobalStatisticValues();
        Workpiece workpiece;
        for (Process selectedProcess : getSelectedProcesses()) {
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
     * Return whether closed processes should be displayed or not.
     *
     * @return parameter controlling whether closed processes should be displayed or
     *         not
     */
    public boolean isShowClosedProcesses() {
        Stopwatch stopwatch = new Stopwatch(this, "isShowClosedProcesses");
        return stopwatch.stop(((LazyProcessModel) this.lazyBeanModel).isShowClosedProcesses());
    }

    /**
     * Set whether closed processes should be displayed or not.
     *
     * @param showClosedProcesses
     *            boolean flag signaling whether closed processes should be
     *            displayed or not
     */
    public void setShowClosedProcesses(boolean showClosedProcesses) {
        Stopwatch stopwatch = new Stopwatch(this, "setShowClosedProcesses", "showClosedProcesses", Boolean.toString(
            showClosedProcesses));
        ((LazyProcessModel)this.lazyBeanModel).setShowClosedProcesses(showClosedProcesses);
        stopwatch.stop();
    }

    /**
     * Set whether inactive projects should be displayed or not.
     *
     * @param showInactiveProjects
     *            boolean flag signaling whether inactive projects should be
     *            displayed or not
     */
    public void setShowInactiveProjects(boolean showInactiveProjects) {
        Stopwatch stopwatch = new Stopwatch(this, "setShowInactiveProjects", "showInactiveProjects", Boolean.toString(
            showInactiveProjects));
        ((LazyProcessModel)this.lazyBeanModel).setShowInactiveProjects(showInactiveProjects);
        stopwatch.stop();
    }

    /**
     * Return whether inactive projects should be displayed or not.
     *
     * @return parameter controlling whether inactive projects should be displayed
     *         or not
     */
    public boolean isShowInactiveProjects() {
        Stopwatch stopwatch = new Stopwatch(this, "isShowInactiveProjects");
        return stopwatch.stop(((LazyProcessModel) this.lazyBeanModel).isShowInactiveProjects());
    }

    /**
     * Export DMS for selected processes.
     */
    public void exportDMSForSelection() {
        Stopwatch stopwatch = new Stopwatch(this, "exportDMSForSelection");
        exportDMSForProcesses(getSelectedProcesses());
        stopwatch.stop();
    }

    /**
     * Generate result set.
     */
    public void generateResult() {
        Stopwatch stopwatch = new Stopwatch(this, "generateResult");
        try {
            ServiceManager.getProcessService().generateResult(this.filter, this.isShowClosedProcesses(),
                    this.isShowInactiveProjects());
        } catch (IOException e) {
            Helper.setErrorMessage(ERROR_CREATING, new Object[] {Helper.getTranslation("resultSet") }, logger, e);
        }
        stopwatch.stop();
    }

    /**
     * Generate result as PDF.
     */
    public void generateResultAsPdf() {
        Stopwatch stopwatch = new Stopwatch(this, "generateResultAsPdf");
        try {
            ServiceManager.getProcessService().generateResultAsPdf(this.filter, this.isShowClosedProcesses(),
                    this.isShowInactiveProjects());
        } catch (IOException | DocumentException e) {
            Helper.setErrorMessage(ERROR_CREATING, new Object[] {Helper.getTranslation("resultPDF") }, logger, e);
        }
        stopwatch.stop();
    }

    /**
     * Download to home for selected processes.
     */
    public void downloadToHomeForSelection() {
        Stopwatch stopwatch = new Stopwatch(this, "downloadToHomeForSelection");
        try {
            ProcessService.downloadToHome(this.getSelectedProcesses());
            Helper.setMessage("createdInUserHomeAll");
        } catch (DAOException e) {
            Helper.setErrorMessage("Error downloading processes to home directory!");
        }
        stopwatch.stop();
    }

    /**
     * Upload selected processes from home.
     */
    public void uploadFromHomeForSelection() {
        Stopwatch stopwatch = new Stopwatch(this, "uploadFromHomeForSelection");
        ProcessService.uploadFromHome(this.getSelectedProcesses());
        Helper.setMessage("directoryRemovedSelected");
        stopwatch.stop();
    }

    /**
     * Upload all processes from home.
     */
    public void uploadFromHomeForAll() {
        Stopwatch stopwatch = new Stopwatch(this, "uploadFromHomeForAll");
        WebDav myDav = new WebDav();
        List<URI> folder = myDav.uploadAllFromHome(doneDirectoryName);
        myDav.removeAllFromHome(folder, URI.create(doneDirectoryName));
        Helper.setMessage("directoryRemovedAll", doneDirectoryName);
        stopwatch.stop();
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
            } catch (DAOException e) {
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
    public boolean createProcessesWithCalendar(Process process) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), process, "createProcessesWithCalendar");
        try {
            return stopwatch.stop(ProcessService.canCreateProcessWithCalendar(process));
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
        }
        return stopwatch.stop(false);
    }

    /**
     * If a process can be created as child.
     *
     * @param process
     *            the process dto to check.
     * @return true if processes can be created as child, false otherwise
     */
    public boolean createProcessAsChildPossible(Process process) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), process, "createProcessAsChildPossible");
        try {
            return stopwatch.stop(ProcessService.canCreateChildProcess(process));
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.PROCESS.getTranslationSingular() }, logger,
                    e);
        }
        return stopwatch.stop(false);
    }

    /**
     * Download to home for single process. First check if this volume is currently
     * being edited by another user and placed in his home directory, otherwise
     * download.
     */
    public void downloadToHome(int processId) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), processId, "downloadToHome");
        try {
            ProcessService.downloadToHome(new WebDav(), processId);
        } catch (DAOException e) {
            Helper.setErrorMessage("Error downloading process " + processId + " to home directory!");
        }
        stopwatch.stop();
    }

    /**
     * Starts generation of xml logfile for current process.
     */
    public void createXML(Process process) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), process, "createXML");
        try {
            ProcessService.createXML(process, getUser());
        } catch (IOException e) {
            Helper.setErrorMessage("Error creating log file in home directory", logger, e);
        }
        stopwatch.stop();
    }

    /**
     * Export METS.
     */
    public void exportMets(int processId) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), processId, "exportMets");
        try {
            ProcessService.exportMets(processId);
        } catch (DAOException | IOException | SAXException | FileStructureValidationException e) {
            Helper.setErrorMessage("An error occurred while trying to export METS file for process "
                    + processId, logger, e);
        }
        stopwatch.stop();
    }

    /**
     * Export DMS.
     */
    public void exportDMS(Process process) {
        ExportDms export = new ExportDms();
        try {
            export.startExport(process);
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_EXPORTING,
                    new Object[] {ObjectType.PROCESS.getTranslationSingular(), process }, logger, e);
            Helper.setErrorMessage(ERROR_LOADING_ONE,
                    new Object[] {ObjectType.PROCESS.getTranslationSingular(), process }, logger, e);
        }
    }

    /**
     * Downloads a docket for process.
     */
    public void downloadDocket(int id) {
        Stopwatch stopwatch = new Stopwatch(this, "downloadDocket", "id", Integer.toString(id));
        try {
            ServiceManager.getProcessService().downloadDocket(ServiceManager.getProcessService().getById(id));
        } catch (IOException | DAOException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        stopwatch.stop();
    }

    /**
     * Upload from home for single process.
     */
    public void uploadFromHome(Process process) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), process, "uploadFromHome");
        WebDav myDav = new WebDav();
        myDav.uploadFromHome(process);
        Helper.setMessage("directoryRemoved", process.getTitle());
        stopwatch.stop();
    }

    /**
     * Delete Process.
     *
     * @param process
     *            process to delete.
     */
    public void delete(Process process) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), process, "delete");
        if (process.getChildren().isEmpty()) {
            try {
                ProcessService.deleteProcess(process.getId());
            } catch (DAOException | IOException | SAXException | FileStructureValidationException e) {
                Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROCESS.getTranslationSingular() },
                    logger, e);
            }
        } else {
            this.deleteProcessDialog = new DeleteProcessDialog();
            this.deleteProcessDialog.setProcess(process);
            PrimeFaces.current().executeScript("PF('deleteChildrenDialog').show();");
        }
        stopwatch.stop();
    }

    /**
     * Return delete process dialog.
     *
     * @return delete process dialog
     */
    public DeleteProcessDialog getDeleteProcessDialog() {
        Stopwatch stopwatch = new Stopwatch(this, "getDeleteProcessDialog");
        return stopwatch.stop(this.deleteProcessDialog);
    }

    /**
     * Check and return whether process with given ID can be exported or not.
     *
     * @param process the process
     * @return whether process with given ID can be exported or not
     */
    public boolean canBeExported(Process process) {
        Stopwatch stopwatch = new Stopwatch(this.getClass(), process, "canBeExported");
        try {
            if (!exportable.containsKey(process.getId())) {
                boolean processHasChildren = hasChildren(process);
                if (processHasChildren) {
                    exportable.put(process.getId(), true);
                } else {
                    exportable.put(process.getId(), ProcessService.canBeExported(process, false));
                }
            }
            return stopwatch.stop(exportable.get(process.getId()));
        } catch (DAOException e) {
            Helper.setErrorMessage(e);
            return stopwatch.stop(false);
        }
    }

    /**
     * Specifies the selected processes.
     * 
     * @param selectedProcesses
     *            the selected processes
     */
    public void setSelectedProcesses(List<Process> selectedProcesses) {
        Stopwatch stopwatch = new Stopwatch(this, "setSelectedProcesses", "selectedProcesses", Objects.toString(
            selectedProcesses));
        this.selectedProcesses = selectedProcesses;
        stopwatch.stop();
    }

    /**
     * Update selection and first row to show in datatable on PageEvent.
     * @param pageEvent PageEvent triggered by data tables paginator
     */
    @Override
    public void onPageChange(PageEvent pageEvent) {
        Stopwatch stopwatch = new Stopwatch(this, "onPageChange");
        this.setFirstRow(((DataTable) pageEvent.getSource()).getFirst());
        if (allSelected) {
            PrimeFaces.current()
                    .executeScript("PF('processesTable').selectAllRows();");
            excludedProcessIds.forEach(processId -> PrimeFaces.current()
                    .executeScript("PF('processesTable').unselectRow($('tr[data-rk=\"" + processId + "\"]'), true);"));
        }
        stopwatch.stop();
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
}
