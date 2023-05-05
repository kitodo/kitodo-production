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

package org.kitodo.production.helper.tasks;

import java.io.IOException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.beans.Task;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.data.exceptions.DataException;
import org.kitodo.export.ExportDms;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.workflow.WorkflowControllerService;

/**
 * The class ExportDmsTask accepts an {@link org.kitodo.export.ExportDms} for a
 * process and provides the ability to run the export in the background this
 * way. This is especially valuable if the export has a big load of images to
 * copy.
 */
public class ExportDmsTask extends EmptyTask {

    private static final Logger logger = LogManager.getLogger(ExportDmsTask.class);

    private final ExportDms exportDms;
    private final Process process;

    /**
     * ExportDmsTask constructor. Creates a ExportDmsTask.
     *
     * @param exportDms
     *            exportDMS configuration
     * @param process
     *            the process to export
     */
    public ExportDmsTask(ExportDms exportDms, Process process) {
        super(process.getTitle());
        this.exportDms = exportDms;
        this.process = process;
    }

    /**
     * Clone constructor. Provides the ability to restart an export that was
     * previously interrupted by the user.
     *
     * @param source
     *            terminated thread
     */
    private ExportDmsTask(ExportDmsTask source) {
        super(source);
        this.exportDms = source.exportDms;
        this.process = source.process;
    }

    /**
     * If the task is started, it will execute this run() method which will
     * start the export on the ExportDms. This task instance is passed in
     * addition so that the ExportDms can update the task’s state.
     *
     * @see org.kitodo.production.helper.tasks.EmptyTask#run()
     */
    @Override
    public void run() {
        boolean exportSuccessful;
        try {
            exportSuccessful = exportDms.startExport(process, this);
            Task workflowTask = exportDms.getWorkflowTask();
            // close only the task of the original process and not of other processes (parent process, other child processes)
            if (Objects.nonNull(workflowTask)
                    && workflowTask.getProcess().getId().equals(process.getId())
                    && exportSuccessful) {
                setProgress(100);
                new WorkflowControllerService().close(exportDms.getWorkflowTask());
            }
        } catch (RuntimeException | DataException | IOException | DAOException e) {
            setException(e);
            exportSuccessful = false;
        }
        try {
            process.setExported(exportSuccessful);
            ServiceManager.getProcessService().save(process);
        } catch (DataException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Calls the clone constructor to create a not yet executed instance of this
     * thread object. This is necessary for threads that have terminated in
     * order to render possible to restart them.
     *
     * @return a not-yet-executed replacement of this thread
     * @see org.kitodo.production.helper.tasks.EmptyTask#replace()
     */
    @Override
    public ExportDmsTask replace() {
        return new ExportDmsTask(this);
    }
}
