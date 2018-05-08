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

package de.sub.goobi.forms;

import de.sub.goobi.config.ConfigCore;
import de.sub.goobi.export.dms.ExportDms;
import de.sub.goobi.helper.BatchProcessHelper;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.ExportFileException;
import de.sub.goobi.helper.tasks.ExportNewspaperBatchTask;
import de.sub.goobi.helper.tasks.ExportSerialBatchTask;
import de.sub.goobi.helper.tasks.TaskManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.goobi.production.constants.Parameters;
import org.kitodo.api.ugh.exceptions.MetadataTypeNotAllowedException;
import org.kitodo.api.ugh.exceptions.PreferencesException;
import org.kitodo.api.ugh.exceptions.ReadException;
import org.kitodo.api.ugh.exceptions.WriteException;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Batch.Type;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.dto.ProcessDTO;
import org.kitodo.enums.ObjectMode;
import org.kitodo.enums.ObjectType;
import org.kitodo.exceptions.UnreachableCodeException;
import org.kitodo.services.ServiceManager;

@Named("BatchForm")
@SessionScoped
public class BatchForm extends BasisForm {

    private static final long serialVersionUID = 8234897225425856549L;

    private static final Logger logger = LogManager.getLogger(BatchForm.class);

    private List<Process> currentProcesses;
    private List<Process> selectedProcesses;
    private List<Batch> currentBatches;
    private List<Integer> selectedBatches;
    private String batchfilter;
    private String processfilter;
    private ObjectMode editMode = ObjectMode.NONE;
    private String batchTitle;
    private static final String ERROR_READ = "errorReading";
    private static final String NO_BATCH_SELECTED = "noBatchSelected";
    private static final String TOO_MANY_BATCHES_SELECTED = "tooManyBatchesSelected";
    private static final String BATCHES_LOG_CHANGES = "batches.logChangesToWikiField";
    private transient ServiceManager serviceManager = new ServiceManager();

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
        if (selectedProcesses == null || selectedProcesses.size() == 0) {
            this.currentBatches = serviceManager.getBatchService().getAll();
            this.selectedBatches = new ArrayList<>();
        } else {
            selectedBatches = new ArrayList<>();
            List<Batch> batchesToSelect = new ArrayList<>();
            for (Process process : selectedProcesses) {
                batchesToSelect.addAll(process.getBatches());
            }
            for (Batch batch : batchesToSelect) {
                selectedBatches.add(Integer.valueOf(serviceManager.getBatchService().getIdString(batch)));
            }
        }
    }

    /**
     * Load Process data.
     */
    public void loadProcessData() {
        List<Process> processes = new ArrayList<>();
        try {
            for (int b : selectedBatches) {
                processes.addAll(serviceManager.getBatchService().getById(b).getProcesses());
            }
            currentProcesses = new ArrayList<>(processes);
        } catch (NumberFormatException | DAOException e) {
            Helper.setErrorMessage(ERROR_READ, logger, e);
        }
    }

    /**
     * Filter processes.
     */
    public void filterProcesses() {
        List<ProcessDTO> processDTOS = new ArrayList<>();
        QueryBuilder query = new BoolQueryBuilder();

        if (this.processfilter != null) {
            try {
                query = serviceManager.getFilterService().queryBuilder(this.processfilter, ObjectType.PROCESS,
                        false, false);
            } catch (DataException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        }

        Integer batchMaxSize = ConfigCore.getIntParameter(Parameters.BATCH_DISPLAY_LIMIT, -1);
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
        for (Batch batch : serviceManager.getBatchService().getAll()) {
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
    public List<SelectItem> getCurrentProcessesAsSelectItems() {
        List<SelectItem> answer = new ArrayList<>();
        for (Process p : this.currentProcesses) {
            answer.add(new SelectItem(p, p.getTitle()));
        }
        return answer;
    }

    public String getBatchfilter() {
        return this.batchfilter;
    }

    public void setBatchfilter(String batchfilter) {
        this.batchfilter = batchfilter;
    }

    public String getBatchName() {
        return batchTitle;
    }

    public void setBatchName(String batchName) {
        batchTitle = batchName;
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

    public List<Integer> getSelectedBatches() {
        return this.selectedBatches;
    }

    public void setSelectedBatches(List<Integer> selectedBatches) {
        this.selectedBatches = selectedBatches;
    }

    /**
     * Filter all start.
     *
     * @return page - all batches
     */
    public String filterAll() {
        filterBatches();
        filterProcesses();
        return "/pages/BatchesAll";
    }

    /**
     * This method initializes the batch list without any filter whenever the bean
     * is constructed.
     */
    @PostConstruct
    public void initializeBatchList() {
        filterAll();
        setEditMode(ObjectMode.NONE);
    }

    /**
     * Download docket.
     *
     * @return String
     */
    public String downloadDocket() throws IOException {
        logger.debug("generate docket for process list");
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
        } else if (this.selectedBatches.size() == 1) {
            try {
                serviceManager.getProcessService().downloadDocket(
                    serviceManager.getBatchService().getById(selectedBatches.get(0)).getProcesses());
            } catch (DAOException e) {
                Helper.setErrorMessage(ERROR_READ, logger, e);
                return null;
            }
        } else {
            Helper.setFehlerMeldung(TOO_MANY_BATCHES_SELECTED);
        }
        return null;
    }

    /**
     * The method deleteBatch() is called if the user clicks the action link to
     * delete batches. It runs the deletion of the batches.
     */
    public void deleteBatch() {
        int selectedBatchesSize = this.selectedBatches.size();
        if (selectedBatchesSize == 0) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
            return;
        }
        List<Integer> ids = new ArrayList<>(selectedBatchesSize);
        ids.addAll(this.selectedBatches);
        try {
            serviceManager.getBatchService().removeAll(ids);
            filterAll();
        } catch (DAOException e) {
            Helper.setErrorMessage("fehlerNichtSpeicherbar", logger, e);
        }
    }

    /**
     * Add processes to Batch.
     */
    public void addProcessesToBatch() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
            return;
        }
        if (this.selectedProcesses.size() == 0) {
            Helper.setFehlerMeldung("noProcessSelected");
            return;
        }
        try {
            for (Integer entry : this.selectedBatches) {
                Batch batch = serviceManager.getBatchService().getById(entry);
                serviceManager.getBatchService().addAll(batch, this.selectedProcesses);
                serviceManager.getBatchService().save(batch);
                if (ConfigCore.getBooleanParameter(BATCHES_LOG_CHANGES, false)) {
                    for (Process p : this.selectedProcesses) {
                        serviceManager.getProcessService().addToWikiField(Helper.getTranslation("addToBatch",
                            serviceManager.getBatchService().getLabel(batch)), p);
                    }
                    this.serviceManager.getProcessService().saveList(this.selectedProcesses);
                }
            }
        } catch (DAOException e) {
            Helper.setErrorMessage("fehlerNichtAktualisierbar", logger, e);
        } catch (DataException e) {
            Helper.setErrorMessage("errorSaveList", logger, e);
        }
    }

    /**
     * Remove processes from Batch.
     */
    public void removeProcessesFromBatch() throws DAOException, DataException {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
            return;
        }
        if (this.selectedProcesses.size() == 0) {
            Helper.setFehlerMeldung("noProcessSelected");
            return;
        }

        for (Integer entry : this.selectedBatches) {
            Batch batch = serviceManager.getBatchService().getById(entry);
            serviceManager.getBatchService().removeAll(batch, this.selectedProcesses);
            serviceManager.getBatchService().save(batch);
            if (ConfigCore.getBooleanParameter(BATCHES_LOG_CHANGES, false)) {
                for (Process p : this.selectedProcesses) {
                    serviceManager.getProcessService().addToWikiField(Helper.getTranslation("removeFromBatch",
                        serviceManager.getBatchService().getLabel(batch)), p);
                }
                this.serviceManager.getProcessService().saveList(this.selectedProcesses);
            }
        }
        filterAll();
    }

    /**
     * Rename Batch.
     */
    public void renameBatch() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
        } else if (this.selectedBatches.size() > 1) {
            Helper.setFehlerMeldung(TOO_MANY_BATCHES_SELECTED);
        } else {
            try {
                Integer selected = selectedBatches.get(0);
                for (Batch batch : currentBatches) {
                    if (selected.equals(batch.getId())) {
                        batch.setTitle(batchTitle == null || batchTitle.trim().length() == 0 ? null : batchTitle);
                        serviceManager.getBatchService().save(batch);
                        return;
                    }
                }
            } catch (DataException e) {
                Helper.setErrorMessage("fehlerNichtAktualisierbar", logger, e);
            }
        }
    }

    /**
     * Create new Batch.
     */
    public void createNewBatch() throws DAOException, DataException {
        if (selectedProcesses.size() > 0) {
            Batch batch;
            if (batchTitle != null && batchTitle.trim().length() > 0) {
                batch = new Batch(batchTitle.trim(), Type.LOGISTIC, selectedProcesses);
            } else {
                batch = new Batch(Type.LOGISTIC, selectedProcesses);
            }

            serviceManager.getBatchService().save(batch);
            if (ConfigCore.getBooleanParameter(BATCHES_LOG_CHANGES, false)) {
                for (Process p : selectedProcesses) {
                    serviceManager.getProcessService().addToWikiField(Helper.getTranslation("addToBatch",
                        serviceManager.getBatchService().getLabel(batch)), p);
                }
                this.serviceManager.getProcessService().saveList(selectedProcesses);
            }
        }
        filterAll();
    }

    /*
     * properties
     */
    private BatchProcessHelper batchHelper;

    /**
     * Edit properties.
     *
     * @return page
     */
    public String editProperties() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
            return null;
        } else if (this.selectedBatches.size() > 1) {
            Helper.setFehlerMeldung(TOO_MANY_BATCHES_SELECTED);
            return null;
        } else {
            if (this.selectedBatches.get(0) != null && !this.selectedBatches.get(0).equals(0)) {
                Batch batch;
                try {
                    batch = serviceManager.getBatchService().getById(selectedBatches.get(0));
                    this.batchHelper = new BatchProcessHelper(batch);
                    return "/pages/BatchProperties";
                } catch (DAOException e) {
                    Helper.setErrorMessage(ERROR_READ, logger, e);
                    return null;
                }
            } else {
                Helper.setFehlerMeldung(NO_BATCH_SELECTED);
                return null;
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
     * Get mode for edition.
     *
     * @return mode for edition as ObjectMode objects
     */
    public ObjectMode getEditMode() {
        return this.editMode;
    }

    /**
     * Set mode for edition.
     *
     * @param editMode
     *            mode for edition as ObjectMode objects
     */
    public void setEditMode(ObjectMode editMode) {
        this.editMode = editMode;
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
    public String exportBatch() {
        if (this.selectedBatches.size() == 0) {
            Helper.setFehlerMeldung(NO_BATCH_SELECTED);
            return null;
        }

        for (Integer batchID : selectedBatches) {
            try {
                Batch batch = serviceManager.getBatchService().getById(batchID);
                switch (batch.getType()) {
                    case LOGISTIC:
                        for (Process process : batch.getProcesses()) {
                            ExportDms dms = new ExportDms(
                                    ConfigCore.getBooleanParameter(Parameters.EXPORT_WITH_IMAGES, true));
                            dms.startExport(process);
                        }
                        break;
                    case NEWSPAPER:
                        TaskManager.addTask(new ExportNewspaperBatchTask(batch));
                        break;
                    case SERIAL:
                        TaskManager.addTask(new ExportSerialBatchTask(batch));
                        break;
                    default:
                        throw new UnreachableCodeException("Complete switch statement");
                }
            } catch (DAOException | PreferencesException | WriteException | MetadataTypeNotAllowedException
                    | ReadException | IOException | ExportFileException | RuntimeException e) {
                Helper.setErrorMessage(ERROR_READ, logger, e);
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
                if (selectedBatches.contains(batch.getId())) {
                    batch.setType(type);
                    serviceManager.getBatchService().save(batch);
                }
            }
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_READ, logger, e);
        }
    }
}
