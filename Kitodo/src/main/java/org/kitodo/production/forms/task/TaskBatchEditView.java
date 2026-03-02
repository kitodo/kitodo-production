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


package org.kitodo.production.forms.task;

import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Batch;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.forms.BaseForm;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.helper.batch.BatchTaskHelper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.utils.Stopwatch;

public class TaskBatchEditView extends BaseForm {

    private static final Logger logger = LogManager.getLogger(TaskEditView.class);

    public static final String VIEW_PATH = MessageFormat.format(REDIRECT_PATH, "taskBatchEdit");
    
    private transient BatchTaskHelper batchHelper;

    public static String getViewPath(Task task) {
        return VIEW_PATH + "id=" + task.getId();
    }

    /**
     * View action that loads a batch task.
     * 
     * @param id the id of the batch task
     */
    public void loadFromTemplate(int id) {
        try {
            if (id != 0) {
                Task batchTask = ServiceManager.getTaskService().getById(id);
                List<Batch> batches = batchTask.getProcess().getBatches();
                if (batches.size() == 1) {
                    Integer batchId = batches.get(0).getId();
                    List<Task> currentTasksOfBatch = ServiceManager.getTaskService().getCurrentTasksOfBatch(batchTask.getTitle(), batchId);
                    if (currentTasksOfBatch.size() > 1) {
                        this.batchHelper = new BatchTaskHelper(currentTasksOfBatch);
                    } else {
                        // batch only consists of single task, meaning the user should have been navigated to the regular task edit view
                    }
                } else {
                    // there are multiple batches assign, which is not correct,
                    // and the user should never have been navigated to this view
                }
            }
        } catch (DAOException e) {
            Helper.setErrorMessage(ERROR_LOADING_ONE, new Object[] {ObjectType.TASK.getTranslationSingular(), id },
                logger, e);
        }
    }

    /**
     * Get batch helper.
     *
     * @return batch helper as BatchHelper object
     */
    public BatchTaskHelper getBatchHelper() {
        Stopwatch stopwatch = new Stopwatch(this, "getBatchHelper");
        return stopwatch.stop(this.batchHelper);
    }

    /**
     * Set batch helper.
     *
     * @param batchHelper
     *            as BatchHelper object
     */
    public void setBatchHelper(BatchTaskHelper batchHelper) {
        Stopwatch stopwatch = new Stopwatch(this, "setBatchHelper", "batchHelper", Objects.toString(batchHelper));
        this.batchHelper = batchHelper;
        stopwatch.stop();
    }

}
