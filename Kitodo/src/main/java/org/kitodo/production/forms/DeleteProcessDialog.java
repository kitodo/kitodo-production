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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kitodo.data.database.beans.Process;
import org.kitodo.data.database.exceptions.DAOException;
import org.kitodo.production.enums.ObjectType;
import org.kitodo.production.helper.Helper;
import org.kitodo.production.services.ServiceManager;
import org.kitodo.production.services.data.ProcessService;

public class DeleteProcessDialog {

    private static final Logger logger = LogManager.getLogger(DeleteProcessDialog.class);
    private Process process;
    private static final String ERROR_SAVING = "errorSaving";
    private static final String ERROR_DELETING = "errorDeleting";

    /**
     * Get process.
     *
     * @return process
     */
    public Process getProcess() {
        return this.process;
    }

    /**
     * Set process.
     *
     * @param process Process
     */
    public void setProcess(Process process) {
        this.process = process;
    }

    /**
     * Delete with children processes.
     */
    public void deleteWithChildren() {
        List<Process> children = new CopyOnWriteArrayList<>(process.getChildren());
        try {
            for (Process child : children) {
                ProcessService.deleteProcess(child);
            }
            ProcessService.deleteProcess(process);
        } catch (DAOException | IOException e) {
            Helper.setErrorMessage(ERROR_DELETING, new Object[] {ObjectType.PROCESS.getTranslationSingular() },
                    logger, e);
        }
    }

    /**
     * Delete without children processes.
     */
    public void deleteWithoutChildren() {
        List<Process> children = new CopyOnWriteArrayList<>(process.getChildren());
        process.getChildren().clear();

        for (Process child : children) {
            child.setParent(null);
            try {
                ServiceManager.getProcessService().save(child);
                ProcessService.deleteProcess(process);
            } catch (DAOException | IOException e) {
                Helper.setErrorMessage(ERROR_SAVING, new Object[]{ObjectType.PROCESS.getTranslationSingular()}, logger,
                        e);
            }
        }
    }
}
