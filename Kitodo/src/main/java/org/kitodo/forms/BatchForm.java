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

package org.kitodo.forms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.inject.Named;
import javax.xml.bind.JAXBException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Batch.Type;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.enums.ObjectType;
import org.kitodo.exceptions.ExportFileException;
import org.kitodo.exceptions.UnreachableCodeException;
import org.kitodo.exporter.dms.ExportDms;
import org.kitodo.helper.Helper;
import org.kitodo.helper.SelectItemList;
import org.kitodo.helper.batch.BatchProcessHelper;
import org.kitodo.helper.tasks.ExportNewspaperBatchTask;
import org.kitodo.helper.tasks.ExportSerialBatchTask;
import org.kitodo.helper.tasks.TaskManager;
import org.kitodo.model.LazyDTOModel;

@Named("BatchForm")
@SessionScoped
public class BatchForm extends BaseForm {

    private static final long serialVersionUID = 8234897225425856549L;

    private static final Logger logger = LogManager.getLogger(BatchForm.class);

    private List<Process> currentProcesses;
    private List<Process> selectedProcesses = new ArrayList<>();
    private List<Batch> currentBatches;
    private List<Batch> selectedBatches = new ArrayList<>();
    private int selectedBatchId;
    private String batchfilter;
    private String processfilter;
    private String batchTitle;
    private BatchProcessHelper batchHelper;
    private static final String NO_BATCH_SELECTED = "noBatchSelected";
    private static final String NO_PROCESS_SELECTED = "noProcessSelected";
    private static final String TOO_MANY_BATCHES_SELECTED = "tooManyBatchesSelected";

    /**
     * Constructor.
     */
    public BatchForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(serviceManager.getBatchService()));
        filterAll();
    }

    // TODO; for what is it needed - right now it is used only in new tests
    public List<Process> getCurrentProcesses() {
        return this.currentProcesses;
    }

    public void setCurrentProcesses(List<Process> currentProcesses) {
        this.currentProcesses = currentProcesses;
    }

    /**
     * Load Batch data.
     */
    public void loadBatchData() {
        if (this.selectedProcesses.isEmpty()) {
            try {
                this.currentBatches = serviceManager.getBatchService().getAll();
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_LOADING_MANY, new Object[] {ObjectType.BATCH.getTranslationPlural() },
                    logger, e);
            }
        } else {
            selectedBatches = new ArrayList<>();
            List<Batch> batchesToSelect = new ArrayList<>();
            for (Process process : selectedProcesses) {
                batchesToSelect.addAll(process.getBatches());
            }
            selectedBatches.addAll(batchesToSelect);
        }
    }

    /**
     * Load Process data.
     */
    public void loadProcessData() {
        List<Process> processes = new ArrayList<>();

        for (Batch selectedBatch : selectedBatches) {
            processes.addAll(selectedBatch.getProcesses());
        }
        currentProcesses = new ArrayList<>(processes);
    }

    /**
     * Filter processes.
     */
    public void filterProcesses() {
        List<ProcessDTO> processDTOS = new ArrayList<>();
        QueryBuilder query = new BoolQueryBuilder();

        if (this.processfilter != null) {
            try {
                query = serviceManager.getFilterService().queryBuilder(this.processfilter, ObjectType.PROCESS, false,
                    false);
            } catch (DataException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }

        Integer batchMaxSize = ConfigCore.getIntParameter(ParameterCore.BATCH_DISPLAY_LIMIT, -1);
        try {
            if (batchMaxSize > 0) {
                processDTOS = serviceManager.getProcessService().findByQuery(query,
                    serviceManager.getProcessService().sortByCreationDate(SortOrder.DESC), 0, batchMaxSize, false);
            } else {
                processDTOS = serviceManager.getProcessService().findByQuery(query,
                    serviceManager.getProcessService().sortByCreationDate(SortOrder.DESC), false);
            }
        } catch (DataException e) {
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
        try {
            this.currentProcesses = serviceManager.getProcessService().convertDtosToBeans(processDTOS);
        } catch (DAOException e) {
            this.currentProcesses = new ArrayList<>();
            Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
        }
    }

    /**
     * Filter batches.
     */
    public void filterBatches() {
        currentBatches = new ArrayList<>();
        List<Batch> batches = new ArrayList<>();
        try {
            batches = serviceManager.getBatchService().getAll();
        } catch (DAOException e) {
            logger.error(e);
        }
        for (Batch batch : batches) {
            if (serviceManager.getBatchService().contains(batch, batchfilter)) {
                currentBatches.add(batch);
            }
        }
    }

    /**
     * Get current processes as select items.
     *
     * @return list of select items
     */
    public List<SelectItem> getProcessesSelectItems() {
        return SelectItemList.getProcesses(this.currentProcesses);
    }

    /**
     * Get current batches as select items.
     *
     * @return list of select items
     */
    public List<SelectItem> getBatchesSelectItems() {
        return SelectItemList.getBatches(this.currentBatches);
    }

    public String getBatchfilter() {
        return this.batchfilter;
    }

    public void setBatchfilter(String batchfilter) {
        this.batchfilter = batchfilter;
    }

    /**
     * Get batch title.
     * 
     * @return batch title as String
     */
    public String getBatchTitle() {
        return batchTitle;
    }

    /**
     * Set batch title.
     * 
     * @param batchTitle
     *            as String
     */
    public void setBatchTitle(String batchTitle) {
        this.batchTitle = batchTitle;
    }

    public String getProcessfilter() {
        return this.processfilter;
    }

    public void setProcessfilter(String processfilter) {
        this.processfilter = processfilter;
    }

    public List<Batch> getCurrentBatches() {
        return this.currentBatches;
    }

    public void setCurrentBatches(List<Batch> currentBatches) {
        this.currentBatches = currentBatches;
    }

    public List<Process> getSelectedProcesses() {
        return this.selectedProcesses;
    }

    public void setSelectedProcesses(List<Process> selectedProcesses) {
        this.selectedProcesses = selectedProcesses;
    }

    public List<Batch> getSelectedBatches() {
        return this.selectedBatches;
    }

    public void setSelectedBatches(List<Batch> selectedBatches) {
        this.selectedBatches = selectedBatches;
    }

    /**
     * Filter all start.
     */
    private void filterAll() {
        filterBatches();
        filterProcesses();
    }

    /**
     * Download docket for all processes assigned to selected batch.
     */
    public void downloadDocket() {
        logger.debug("generate docket for process list");
        if (this.selectedBatches.isEmpty()) {
            Helper.setErrorMessage(NO_BATCH_SELECTED);
        } else if (this.selectedBatches.size() == 1) {
            try {
                serviceManager.getProcessService().downloadDocket(selectedBatches.get(0).getProcesses());
            } catch (IOException e) {
                //TODO: needed message for case of IOException
                Helper.setErrorMessage(e.getMessage(), new Object[] {ObjectType.BATCH.getTranslationSingular() }, logger,
                        e);
            }
        } else {
            Helper.setErrorMessage(TOO_MANY_BATCHES_SELECTED);
        }
    }

    /**
     * The method deleteBatch() is called if the user clicks the action link to
     * delete batches. It runs the deletion of the batches.
     */
    public void delete() {
        int selectedBatchesSize = this.selectedBatches.size();
        if (selectedBatchesSize == 0) {
            Helper.setErrorMessage(NO_BATCH_SELECTED);
            return;
        }

        try {
            serviceManager.getBatchService().removeAll(this.selectedBatches);
            filterAll();
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.BATCH.getTranslationSingular() }, logger, e);
        }
    }

    /**
     * Add processes to Batch.
     */
    public void addProcessesToBatch() {
        if (this.selectedBatches.isEmpty()) {
            Helper.setErrorMessage(NO_BATCH_SELECTED);
            return;
        }
        if (this.selectedProcesses.isEmpty()) {
            Helper.setErrorMessage(NO_PROCESS_SELECTED);
            return;
        }
        try {
            for (Batch selectedBatch : this.selectedBatches) {
                serviceManager.getBatchService().addAll(selectedBatch, this.selectedProcesses);
                serviceManager.getBatchService().save(selectedBatch);
                if (ConfigCore.getBooleanParameter(ParameterCore.BATCHES_LOG_CHANGES)) {
                    for (Process p : this.selectedProcesses) {
                        serviceManager.getProcessService().addToWikiField(
                            Helper.getTranslation("addToBatch", serviceManager.getBatchService().getLabel(selectedBatch)), p);
                    }
                    this.serviceManager.getProcessService().saveList(this.selectedProcesses);
                }
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_RELOADING, new Object[] {ObjectType.BATCH.getTranslationSingular() }, logger,
                e);
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaveList", logger, e);
        }
    }

    /**
     * Remove processes from Batch.
     */
    public void removeProcessesFromBatch() throws DAOException, DataException {
        if (this.selectedBatches.isEmpty()) {
            Helper.setErrorMessage(NO_BATCH_SELECTED);
            return;
        }
        if (this.selectedProcesses.isEmpty()) {
            Helper.setErrorMessage(NO_PROCESS_SELECTED);
            return;
        }

        for (Batch selectedBatch : this.selectedBatches) {
            serviceManager.getBatchService().removeAll(selectedBatch, this.selectedProcesses);
            serviceManager.getBatchService().save(selectedBatch);
            if (ConfigCore.getBooleanParameter(ParameterCore.BATCHES_LOG_CHANGES)) {
                for (Process p : this.selectedProcesses) {
                    serviceManager.getProcessService().addToWikiField(
                        Helper.getTranslation("removeFromBatch", serviceManager.getBatchService().getLabel(selectedBatch)), p);
                }
                this.serviceManager.getProcessService().saveList(this.selectedProcesses);
            }
        }
        filterAll();
    }

    /**
     * Rename Batch.
     */
    public void rename() {
        if (this.selectedBatches.isEmpty()) {
            Helper.setErrorMessage(NO_BATCH_SELECTED);
        } else if (this.selectedBatches.size() > 1) {
            Helper.setErrorMessage(TOO_MANY_BATCHES_SELECTED);
        } else {
            try {
                Batch selectedBatch = selectedBatches.get(0);
                for (Batch batch : currentBatches) {
                    if (selectedBatch.getId().equals(batch.getId())) {
                        batch.setTitle(batchTitle == null || batchTitle.trim().length() == 0 ? null : batchTitle);
                        serviceManager.getBatchService().save(batch);
                        batchTitle = "";
                        return;
                    }
                }
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_RELOADING, new Object[] {ObjectType.BATCH.getTranslationSingular() },
                    logger, e);
            }
        }
    }

    /**
     * Create new Batch.
     */
    public void createNew() throws DAOException, DataException {
        if (selectedProcesses.isEmpty()) {
            Helper.setErrorMessage(NO_PROCESS_SELECTED);
        } else {
            Batch batch;
            if (batchTitle != null && batchTitle.trim().length() > 0) {
                batch = new Batch(batchTitle.trim(), Type.LOGISTIC, selectedProcesses);
            } else {
                batch = new Batch(Type.LOGISTIC, selectedProcesses);
            }

            serviceManager.getBatchService().save(batch);
            if (ConfigCore.getBooleanParameter(ParameterCore.BATCHES_LOG_CHANGES)) {
                for (Process p : selectedProcesses) {
                    serviceManager.getProcessService().addToWikiField(
                        Helper.getTranslation("addToBatch", serviceManager.getBatchService().getLabel(batch)), p);
                }
                this.serviceManager.getProcessService().saveList(selectedProcesses);
            }
        }
        batchTitle = "";
        filterAll();
    }

    /**
     * Edit properties.
     */
    public void editProperties(int id) {
        if (this.selectedBatches.isEmpty()) {
            Helper.setErrorMessage(NO_BATCH_SELECTED);
        } else if (this.selectedBatches.size() > 1) {
            Helper.setErrorMessage(TOO_MANY_BATCHES_SELECTED);
        } else {
            try {
                setBatchHelper(new BatchProcessHelper(serviceManager.getBatchService().getById(id)));
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.BATCH.getTranslationSingular() }, logger,
                    e);
            }
        }
    }

    public BatchProcessHelper getBatchHelper() {
        return this.batchHelper;
    }

    public void setBatchHelper(BatchProcessHelper batchHelper) {
        this.batchHelper = batchHelper;
    }

    /**
     * Creates a batch export task to export the selected batch. The type of export
     * task depends on the batch type. If asynchronous tasks have been created, the
     * user will be redirected to the task manager page where it can observe the
     * task progressing.
     *
     * @return the next page to show as named in a &lt;from-outcome&gt; element in
     *         faces_config.xml
     */
    public String export() {
        if (this.selectedBatches.isEmpty()) {
            Helper.setErrorMessage(NO_BATCH_SELECTED);
            return null;
        }

        for (Batch selectedBatch : selectedBatches) {
            try {
                switch (selectedBatch.getType()) {
                    case LOGISTIC:
                        for (Process process : selectedBatch.getProcesses()) {
                            ExportDms dms = new ExportDms(
                                    ConfigCore.getBooleanParameter(ParameterCore.EXPORT_WITH_IMAGES, true));
                            dms.startExport(process);
                        }
                        break;
                    case NEWSPAPER:
                        TaskManager.addTask(new ExportNewspaperBatchTask(selectedBatch));
                        break;
                    case SERIAL:
                        TaskManager.addTask(new ExportSerialBatchTask(selectedBatch));
                        break;
                    default:
                        throw new UnreachableCodeException("Complete switch statement");
                }
            } catch (PreferencesException | WriteException | MetadataTypeNotAllowedException
                    | ReadException | IOException | ExportFileException | RuntimeException | JAXBException e) {
                Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.BATCH.getTranslationSingular() }, logger,
                    e);
                return null;
            }
        }

        return "/pages/taskmanager";
    }

    /**
     * Sets the type of all currently selected batches to LOGISTIC.
     */
    public void setLogistic() {
        setType(Type.LOGISTIC);
    }

    /**
     * Sets the type of all currently selected batches to NEWSPAPER.
     */
    public void setNewspaper() {
        setType(Type.NEWSPAPER);
    }

    /**
     * Sets the type of all currently selected batches to SERIAL.
     */
    public void setSerial() {
        setType(Type.SERIAL);
    }

    /**
     * Sets the type of all currently selected batches to the named one, overriding
     * a previously set type, if any.
     *
     * @param type
     *            type to set
     */
    private void setType(Type type) {
        try {
            for (Batch batch : currentBatches) {
                if (selectedBatches.contains(batch)) {
                    batch.setType(type);
                    serviceManager.getBatchService().save(batch);
                }
            }
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.BATCH.getTranslationSingular() }, logger, e);
        }
    }

    /**
     * Change id of selected batch to first selected batch. Needed for edit batch
     * properties.
     *
     * @param vcEvent
     *            change event
     */
    @SuppressWarnings("unchecked")
    public void changeSelectedBatch(ValueChangeEvent vcEvent) {
        this.selectedBatches = (List<Batch>) vcEvent.getNewValue();
        if (this.selectedBatches.size() == 1) {
            this.selectedBatchId = this.selectedBatches.get(0).getId();
        } else {
            this.selectedBatchId = 0;
        }
    }

    /**
     * Get selectedBatchId.
     *
     * @return value of selectedBatchId
     */
    public int getSelectedBatchId() {
        return selectedBatchId;
    }

    /**
     * Set selectedBatchId.
     *
     * @param selectedBatchId
     *            as int
     */
    public void setSelectedBatchId(int selectedBatchId) {
        this.selectedBatchId = selectedBatchId;
    }
}
