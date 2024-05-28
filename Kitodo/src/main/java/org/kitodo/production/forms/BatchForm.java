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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.config.ConfigCore;
import org.kitodo.config.enums.ParameterCore;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Comment;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.enums.CommentType;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.export.ExportDms;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.batch.BatchProcessHelper;
import org.kitodo.production.model.LazyDTOModel;
import org.kitodo.production.services.ServiceManager;

@Named("BatchForm")
@ViewScoped
public class BatchForm extends BaseForm {

    private static final Logger logger = LogManager.getLogger(BatchForm.class);

    private List<Process> currentProcesses;
    private List<Process> selectedProcesses = new ArrayList<>();
    private List<Batch> currentBatches;
    private List<Batch> selectedBatches = new ArrayList<>();
    private int selectedBatchId;
    private String batchfilter;
    private String processfilter;
    private String batchTitle;
    private transient BatchProcessHelper batchHelper;
    private static final String NO_BATCH_SELECTED = "noBatchSelected";
    private static final String NO_PROCESS_SELECTED = "noProcessSelected";
    private static final String TOO_MANY_BATCHES_SELECTED = "tooManyBatchesSelected";

    /**
     * Constructor.
     */
    public BatchForm() {
        super();
        super.setLazyDTOModel(new LazyDTOModel(ServiceManager.getBatchService()));
        filterAll();
    }

    /**
     * Load Batch data.
     */
    public void loadBatchData() {
        if (this.selectedProcesses.isEmpty()) {
            try {
                this.currentBatches = ServiceManager.getBatchService().getAll();
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
        Map<String, Object> parameters = new HashMap<>();
        String query = "FROM Process";

        if (Objects.nonNull(this.processfilter)) {
            parameters.put("processfilter", this.processfilter);
            query += " WHERE title LIKE '%:processfilter%'";
        }
        query += " ORDER BY creationDate DESC";
        int batchMaxSize = ConfigCore.getIntParameter(ParameterCore.BATCH_DISPLAY_LIMIT, -1);
        if (batchMaxSize > 0) {
            this.currentProcesses = ServiceManager.getProcessService().getByQuery(query, parameters, batchMaxSize);
        } else {
            this.currentProcesses = ServiceManager.getProcessService().getByQuery(query, parameters);
        }
    }

    /**
     * Filter batches.
     */
    public void filterBatches() {
        currentBatches = new ArrayList<>();
        List<Batch> batches = new ArrayList<>();
        try {
            batches = ServiceManager.getBatchService().getAll();
        } catch (DAOException e) {
            logger.error(e);
        }
        if (Objects.nonNull(batchfilter) && batchfilter.isEmpty()) {
            currentBatches.addAll(batches);
        } else {
            for (Batch batch : batches) {
                if (ServiceManager.getBatchService().contains(batch, batchfilter)) {
                    currentBatches.add(batch);
                }
            }
        }
    }

    /**
     * Get current processes as select items.
     *
     * @return list of processes
     */
    public List<Process> getCurrentProcesses() {
        return currentProcesses;
    }

    /**
     * Get current batches as select items.
     *
     * @return list of bacthes
     */
    public List<Batch> getCurrentBatches() {
        return currentBatches;
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
                ServiceManager.getProcessService().downloadDocket(selectedBatches.get(0).getProcesses());
            } catch (IOException e) {
                Helper.setErrorMessage(e.getLocalizedMessage(), logger, e);
            }
        } else {
            Helper.setErrorMessage(TOO_MANY_BATCHES_SELECTED);
        }
    }

    /**
     * The method is called if the user clicks the action link to
     * delete batches. It runs the deletion of the batches.
     */
    public void delete() {
        int selectedBatchesSize = this.selectedBatches.size();
        if (selectedBatchesSize == 0) {
            Helper.setErrorMessage(NO_BATCH_SELECTED);
            return;
        }

        try {
            ServiceManager.getBatchService().removeAll(this.selectedBatches);
            filterAll();
        } catch (DataException e) {
            Helper.setErrorMessage(ERROR_SAVING, new Object[] {ObjectType.BATCH.getTranslationSingular() }, logger, e);
        }
    }

    private void addCommentsToBatchProcesses(String message) throws DAOException {
        List<Comment> commentList = new ArrayList<>();
        for (Process process : this.selectedProcesses) {
            Comment comment = new Comment();
            comment.setProcess(process);
            comment.setAuthor(ServiceManager.getUserService().getCurrentUser());
            comment.setMessage(message);
            comment.setType(CommentType.INFO);
            comment.setCreationDate(new Date());
            commentList.add(comment);
        }
        ServiceManager.getCommentService().saveList(commentList);
    }

    /**
     * Add processes to Batch.
     */
    public void addProcessesToBatch() {
        if (areSelectedListsEmpty()) {
            return;
        }

        try {
            for (Batch selectedBatch : this.selectedBatches) {
                selectedBatch.getProcesses().addAll(this.selectedProcesses);
                ServiceManager.getBatchService().save(selectedBatch, true);
                if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.BATCHES_LOG_CHANGES)) {
                    addCommentsToBatchProcesses(Helper.getTranslation("addToBatch",
                            ServiceManager.getBatchService().getLabel(selectedBatch)));
                    ServiceManager.getProcessService().saveList(this.selectedProcesses);
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
        if (areSelectedListsEmpty()) {
            return;
        }

        for (Batch selectedBatch : this.selectedBatches) {
            selectedBatch.getProcesses().removeAll(this.selectedProcesses);
            ServiceManager.getBatchService().save(selectedBatch, true);
            if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.BATCHES_LOG_CHANGES)) {
                addCommentsToBatchProcesses(Helper.getTranslation("removeFromBatch",
                        ServiceManager.getBatchService().getLabel(selectedBatch)));
                ServiceManager.getProcessService().saveList(this.selectedProcesses);
            }
        }
        filterAll();
    }

    private boolean areSelectedListsEmpty() {
        if (this.selectedBatches.isEmpty()) {
            Helper.setErrorMessage(NO_BATCH_SELECTED);
            return true;
        }
        if (this.selectedProcesses.isEmpty()) {
            Helper.setErrorMessage(NO_PROCESS_SELECTED);
            return true;
        }
        return false;
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
                        batch.setTitle(Objects.isNull(batchTitle) || batchTitle.trim().isEmpty() ? null : batchTitle);
                        ServiceManager.getBatchService().save(batch);
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
            if (Objects.nonNull(batchTitle) && !batchTitle.trim().isEmpty()) {
                batch = new Batch(batchTitle.trim(), selectedProcesses);
            } else {
                batch = new Batch(selectedProcesses);
            }

            ServiceManager.getBatchService().save(batch, true);
            if (ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.BATCHES_LOG_CHANGES)) {
                addCommentsToBatchProcesses(Helper.getTranslation("addToBatch", ServiceManager.getBatchService().getLabel(batch)));
                ServiceManager.getProcessService().saveList(selectedProcesses);
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
                setBatchHelper(new BatchProcessHelper(ServiceManager.getBatchService().getById(id)));
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
            return this.stayOnCurrentPage;
        }

        for (Batch selectedBatch : selectedBatches) {
            try {
                for (Process process : selectedBatch.getProcesses()) {
                    ExportDms dms = new ExportDms(
                            ConfigCore.getBooleanParameterOrDefaultValue(ParameterCore.EXPORT_WITH_IMAGES));
                    dms.startExport(process);
                }
            } catch (DataException e) {
                Helper.setErrorMessage(ERROR_READING, new Object[] {ObjectType.BATCH.getTranslationSingular() }, logger,
                    e);
                return this.stayOnCurrentPage;
            }
        }

        return "/pages/taskmanager";
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
        List<Process> processes = new LinkedList<>();
        for (Batch batch : this.selectedBatches) {
            processes.addAll(batch.getProcesses());
        }
        setSelectedProcesses(processes);
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

    /**
     * Creates the label for the batchlist.
     * @param batch the batch to create the label for.
     * @return the label.
     */
    public String getBatchLabel(Batch batch) {
        return ServiceManager.getBatchService().createLabel(batch);
    }
}
